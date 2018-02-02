package com.neatherbench.quencher;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetUrlContentTask extends AsyncTask<String, Void, List<XMLParser.Entry>> {

    private SharedPreferences prefs;
    private AsyncResponse delegate = null;
    private String url;
    public String tag;
    public String subtag;


    public GetUrlContentTask(SharedPreferences prefs, String url, String tag, String subtag, AsyncResponse delegate) {
        this.prefs = prefs;
        this.tag = tag;
        this.subtag = subtag;
        this.delegate = delegate;
        this.url = url;
    }

    @Override
    protected List<XMLParser.Entry> doInBackground(String... args) {
        try {
            return loadXmlFromNetwork(url);
        } catch (XmlPullParserException | IOException e) {
            return null;
        }
    }

    private List<XMLParser.Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        XMLParser stackOverflowXmlParser = new XMLParser(tag, subtag);
        List<XMLParser.Entry> entries;

        //StringBuilder htmlString = new StringBuilder();
        try {
            stream = downloadUrl(urlString);
            entries = stackOverflowXmlParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
        // Each Entry object represents a single post in the XML feed.
        // This section processes the entries list to combine each entry with HTML markup.
        // Each entry is displayed in the UI as a link that optionally includes
        // a text summary.
            /*for (XMLParser.Entry entry : entries) {
                htmlString.append(entry.city);
                htmlString.append(entry.road);
                htmlString.append(entry.house_number);
            }*/

        //return htmlString.toString();

        return entries;
    }

    // Given a string representation of a URL, sets up a connection and gets
// an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        int statusCode = 0;
        statusCode = conn.getResponseCode();
        InputStream is = null;
        if (statusCode >= 200 && statusCode < 400) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }
        return is;
    }

    public interface AsyncResponse {
        void processFinish(List<XMLParser.Entry> output);
    }

    @Override
    protected void onPostExecute(List<XMLParser.Entry> result) {
        delegate.processFinish(result);
    }
}