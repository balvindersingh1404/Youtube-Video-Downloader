package com.example.balvinder.downloader;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.commit451.youtubeextractor.YouTubeExtractionResult;
import com.commit451.youtubeextractor.YouTubeExtractor;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity {

    public static final int progress_bar_type = 0;
    //***********************************************Initialize**********************************************************
    private static String YOUTUBE_ID = "";
    private final YouTubeExtractor mExtractor = YouTubeExtractor.create();
    Button downloadMP4;
    Button downloadMP3;
    String file_url = "";
    private WebView wv1;
    private ProgressDialog pDialog;
    //***********************************************YOUTUBE Callback**********************************************************
    private Callback<YouTubeExtractionResult> mExtractionCallback = new Callback<YouTubeExtractionResult>() {
        @Override
        public void onResponse(Call<YouTubeExtractionResult> call, Response<YouTubeExtractionResult> response) {
            bindVideoResult(response.body());
        }

        @Override
        public void onFailure(Call<YouTubeExtractionResult> call, Throwable t) {
            onError(t);
        }
    };


    //***********************************************Oncreat function**********************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadMP4 = (Button) findViewById(R.id.downloadMP4);
        downloadMP4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mExtractor.extract(YOUTUBE_ID).enqueue(mExtractionCallback);
                new DownloadFileFromURL(wv1.getTitle(), "mp4").execute(file_url);
            }
        });

        downloadMP3 = (Button) findViewById(R.id.downloadMP3);
        downloadMP3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mExtractor.extract(YOUTUBE_ID).enqueue(mExtractionCallback);
                new DownloadFileFromURL(wv1.getTitle(), "mp3").execute(file_url);
            }
        });

        wv1 = (WebView) findViewById(R.id.webView);
        wv1.getSettings().setLoadsImagesAutomatically(true);
        wv1.getSettings().setJavaScriptEnabled(true);
        wv1.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wv1.setWebViewClient(new MyBrowser());
        wv1.loadUrl("https://www.youtube.com/");
    }

    //***********************************************Dialog Box**********************************************************

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                //  pDialog.setMessage("Downloading " + wv1.getTitle() + "....Please wait...");
                pDialog.setMessage("Downloading ...Please wait...!!!!");

                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }
    //***********************************************YOUTUBE Hadling**********************************************************

    private void onError(Throwable t) {
        t.printStackTrace();
        Toast.makeText(MainActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();
    }

    private void bindVideoResult(YouTubeExtractionResult result) {

        file_url = result.getBestAvailableQualityVideoUri() + "";

    }

    //***********************************************ExtractID function*************************************************

    public String extractID() {
        String path = wv1.getUrl();
        String id = "";
        if (!path.equals("https://m.youtube.com/")) {
            URL youtubeURL = null;
            try {
                youtubeURL = new URL(path);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String temp = youtubeURL.getQuery();
            id = temp.substring(2);
        }
        return id;
    }


    //***********************************************onBack track *************************************************
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (wv1.canGoBack()) {
                        wv1.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    //***********************************************MYBrowser Class**********************************************************
    private class MyBrowser extends WebViewClient {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.i("CALLE ONPAGE FINISHED22", wv1.getUrl());       //called
            YOUTUBE_ID = extractID();
            if (wv1.getUrl().contains("https://m.youtube.com/watch?v=")) {
                downloadMP3.setVisibility(View.VISIBLE);
                downloadMP4.setVisibility(View.VISIBLE);
            } else {
                downloadMP3.setVisibility(View.GONE);
                downloadMP4.setVisibility(View.GONE);
            }
        }

    }

    //***********************************************DownloadFileFromURL Class**********************************************************

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        String name = "";
        String format = "";

        public DownloadFileFromURL(String name, String format) {
            this.name = name;
            this.format = format;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
            downloadMP4.setEnabled(false);
            downloadMP3.setEnabled(false);


        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {

                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                int lenghtOfFile = conection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/" + name + "." + format);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;

                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
            if(format.equals("mp3")){
                downloadMP3.setText(Integer.parseInt(progress[0])+"%");
            }
            else if(format.equals("mp4")){
                downloadMP4.setText(Integer.parseInt(progress[0])+"%");
            }

            if(Integer.parseInt(progress[0])==100){
                Toast.makeText(getApplicationContext(),"Download Completed",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(String file_url) {
            dismissDialog(progress_bar_type);
            downloadMP3.setText("DOWNLOAD MP3");
            downloadMP4.setText("DOWNLOAD MP4");
            downloadMP4.setEnabled(true);
            downloadMP3.setEnabled(true);
        }

    }
    //***********************************************DownloadFileFromURL Class END**********************************************************


}









