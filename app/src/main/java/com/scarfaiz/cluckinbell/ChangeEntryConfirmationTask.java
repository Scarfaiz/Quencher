package com.scarfaiz.cluckinbell;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import libs.JSONParser;

class ChangeEntryConfirmationTask extends AsyncTask<String, String, String> {

    private JSONParser jsonParser = new JSONParser();
    private String TAG = "LogDebug";

    private static String server_address;
    private static List<NameValuePair> conf_data = null;

    private static final String TAG_SUCCESS = "success";

    ChangeEntryConfirmationTask(String server_address, List<NameValuePair> conf_data) {
        ChangeEntryConfirmationTask.server_address = server_address;
        ChangeEntryConfirmationTask.conf_data = conf_data;
    }



    @Override
    protected String doInBackground(String[] args){
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", conf_data);
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
        Log.d(TAG, "Conf status changed with code: " + result);
    }

}