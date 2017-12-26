package com.neatherbench.quencher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import libs.JSONParser;

class EntranceTask extends AsyncTask<String, String, String> {

    private String TAG = "LogDebug";
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static String server_address;
    private static List<NameValuePair> username_data = null;
    private AsyncResponse delegate = null;

    private static final String TAG_SUCCESS = "success";

    EntranceTask(String server_address, List<NameValuePair> username_data, Context context, AsyncResponse delegate) {
        EntranceTask.server_address = server_address;
        EntranceTask.username_data = username_data;
        EntranceTask.context = context;
        this.delegate = delegate;
    }



    @Override
    protected String doInBackground(String[] args){
        JSONParser jsonParser = new JSONParser(context);
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", username_data);
        try {
            Log.d(TAG, "JSON response: " + json.toString());
            try {
                int success = json.getInt(TAG_SUCCESS);
                return String.valueOf(success);
            } catch (JSONException e) {
                e.printStackTrace();
                return e.toString();
            }
        }catch (NullPointerException e)
        {
            return e.getMessage();
        }

    }
    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, "Username registered with the code: " + result);
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }
}