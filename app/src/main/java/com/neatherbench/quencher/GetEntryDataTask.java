package com.neatherbench.quencher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import libs.JSONParser;

class GetEntryDataTask extends AsyncTask<String, List<String>, List<String>> {

    private static final String TAG_SUCCESS = "success";
    private static String server_address;
    private static int id;
    private static String username;
    private static String TAG = "LogDebug";
    private AsyncResponse delegate = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public GetEntryDataTask(String server_address, int id, String username, Context context, AsyncResponse delegate) {
        GetEntryDataTask.server_address = server_address;
        GetEntryDataTask.id = id;
        GetEntryDataTask.username = username;
        GetEntryDataTask.context = context;
        this.delegate = delegate;
    }

    @Override
    protected List<String> doInBackground(String[] params) {

        Log.d(TAG, "Sending JSON request");
        JSONParser jsonParser = new JSONParser(context);
        int success;
        List<NameValuePair> entry_data = new ArrayList<>();
        List<String> marker_data = new ArrayList<>();
        entry_data.add(new BasicNameValuePair("id", String.valueOf(id)));
        entry_data.add(new BasicNameValuePair("username", username));
        Log.d(TAG, "server address: " + server_address + "   enrty data: " + entry_data.toString());
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", entry_data);
        try {
            //Log.d(TAG, json.toString());

            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                /*JSONArray productObj = json.getJSONArray(marker_data);
                JSONObject entry = productObj.getJSONObject(0);*/
                marker_data.add(0, json.getString("title"));
                marker_data.add(1, json.getString("address"));
                marker_data.add(2, json.getString("working_hours"));
                marker_data.add(3, json.getString("product_range"));
                marker_data.add(4, json.getString("confirmation_status"));
                marker_data.add(5, json.getString("comments"));
                marker_data.add(6, json.getString("latitude"));
                marker_data.add(7, json.getString("longitude"));
                marker_data.add(8, json.getString("username"));
                marker_data.add(9, json.getString("valid"));
                marker_data.add(10, json.getString("id"));
                marker_data.add(11, json.getString("close_hours"));
                marker_data.add(12, json.getString("type"));
                return marker_data;
            } else {
                // продукт с pid не найден
                marker_data.add("No entry found");
                return marker_data;
            }
        } catch (NullPointerException | JSONException e) {
            marker_data.add(e.getMessage());
            return marker_data;
        }
    }

    @Override
    protected void onPostExecute(List<String> result) {
        // закрываем диалог прогресс
        Log.d(TAG, "Entry data was written with the result: " + result);
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(List<String> output);
    }
}