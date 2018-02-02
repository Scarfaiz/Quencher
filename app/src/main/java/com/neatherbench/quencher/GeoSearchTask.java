package com.neatherbench.quencher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

class GeoSearchTask extends AsyncTask<String, ArrayList<JSONObject>, ArrayList<JSONObject>> {

    private String TAG = "LogDebug";
    private String search;
    static String json = "";
    @SuppressLint("StaticFieldLeak")
    private GeoSearchTask.AsyncResponse delegate = null;
    static InputStream is = null;
    private static JSONArray jObj = null;

    GeoSearchTask(String search, String city, Context context, AsyncResponse delegate)
    {
        this.search = search;
        String city1 = city;
        this.delegate = delegate;
        Context context1 = context;
    }

    @Override
    protected ArrayList<JSONObject> doInBackground(String[] args){
        DefaultHttpClient httpClient = new DefaultHttpClient(null);
        ArrayList<JSONObject> result = new ArrayList<JSONObject>();
        String url = "https://nominatim.openstreetmap.org/search?email=neatherbench@gmail.com&q=" + search + "&format=xml&polygon=1&addressdetails=1&format=json";
        Log.d(TAG, "Result url for sending to the server: " + url);
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (IOException e) {
        Log.d(TAG, "An error occurred: " + e.getMessage());
        e.printStackTrace();
    }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            StringBuilder sb = null;
            sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
                Log.d(TAG, "Line appended " + line);
            }
            is.close();
            json = sb.toString();
            Log.d(TAG, json);
        } catch (Exception e) {
            Log.d(TAG, "Error converting result " + e.toString());
        }
        try {
            jObj = new JSONArray(json);
        } catch (JSONException e) {
            Log.d(TAG, "Error parsing data " + e.toString());
        }
        try {
            for(int i = 0; i<jObj.length(); i++)
            {
                JSONObject jsonObject = jObj.getJSONObject(i);
                /*result.add(jsonObject.getString("display_name"));
                result.add(jsonObject.getString("lat"));
                result.add(jsonObject.getString("lon"));*/
                result.add(jsonObject);
            }
            return result;
        }catch (NullPointerException | JSONException e)
        {
            return result;
        }
    }

    protected void onPostExecute(ArrayList<JSONObject> result) {
        // закрываем диалог прогресс
        Log.d(TAG, "JSON response: " + result);
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(ArrayList<JSONObject> output);
    }

}