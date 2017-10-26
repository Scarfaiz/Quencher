package com.scarfaiz.cluckinbell;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class GetUrlContentTask extends AsyncTask<String, Integer, String> {

    static public String requestResult;

    public AsyncResponse delegate = null;//Call back interface

    public GetUrlContentTask(AsyncResponse asyncResponse) {
        delegate = asyncResponse;//Assigning call back interfacethrough constructor
    }


    protected String doInBackground(String... urls) {
        URL url = null;
        try {
            url = new URL(urls[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            connection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader rd = null;


// Get the response code
        int statusCode = 0;
        try {
            statusCode = connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream is = null;

        if (statusCode >= 200 && statusCode < 400) {
            // Create an InputStream in order to extract the response object
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            is = connection.getErrorStream();
        }
        rd = new BufferedReader(new InputStreamReader(is));
        String content = "", line;
        try {
            while ((line = rd.readLine()) != null) {
                content += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(String result) {
        // this is executed on the main thread after the process is over
        // update your UI here
        requestResult = result;
        delegate.processFinish(result);
    }
}