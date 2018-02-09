package com.neatherbench.quencher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import libs.JSONParser;

class StartTrialTask extends AsyncTask<String, String, String> {

    private String TAG = "LogDebug";
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static String server_address;
    private static List<NameValuePair> username_data = null;
    private AsyncResponse delegate = null;

    private static final String TAG_SUCCESS = "success";

    StartTrialTask(String server_address, List<NameValuePair> username_data, Context context, AsyncResponse delegate) {
        StartTrialTask.server_address = server_address;
        StartTrialTask.username_data = username_data;
        StartTrialTask.context = context;
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
                return e.getMessage();
            }
        }catch (NullPointerException e)
        {
            return e.getMessage();
        }
    }
    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }
}