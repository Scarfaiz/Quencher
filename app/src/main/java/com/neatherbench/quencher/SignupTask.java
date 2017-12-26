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

class SignupTask extends AsyncTask<String, String, String> {

    private static final String TAG_SUCCESS = "success";
    private String server_address;
    private String TAG = "LogDebug";
    private List<NameValuePair> login_data;
    private SignupTask.AsyncResponse delegate = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public SignupTask(String server_address, List<NameValuePair> login_data, Context context, AsyncResponse delegate)
    {
        this.context = context;
        this.server_address = server_address;
        this.login_data = login_data;
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String[] args){
        JSONParser jsonParser = new JSONParser(context);
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", login_data);
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
    protected void onPostExecute(String result) {
        // закрываем диалог прогресс
        Log.d(TAG, "New account was created with the result: " + result);
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }

}