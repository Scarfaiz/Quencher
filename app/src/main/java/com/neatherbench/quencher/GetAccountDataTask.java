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

class GetAccountDataTask extends AsyncTask<String, List<String>, List<String>> {

    private static final String TAG_SUCCESS = "success";
    private static String server_address;
    private static String username;
    private static String TAG = "LogDebug";
    private AsyncResponse delegate = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public GetAccountDataTask(String server_address, String username, Context context, AsyncResponse delegate) {
        GetAccountDataTask.server_address = server_address;
        GetAccountDataTask.username = username;
        GetAccountDataTask.context = context;
        this.delegate = delegate;
    }

    @Override
    protected List<String> doInBackground(String[] params) {

        Log.d(TAG, "Sending JSON request");
        JSONParser jsonParser = new JSONParser(context);
        int success;
        // Список параметров
        List<NameValuePair> username_data = new ArrayList<>();
        List<String> account_data = new ArrayList<>();
        username_data.add(new BasicNameValuePair("username", username));
        Log.d(TAG, "server address: " + server_address + "   enrty data: " + username_data.toString());
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", username_data);
        try {
            //Log.d(TAG, json.toString());

            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                account_data.add(0, json.getString("reputation"));
                account_data.add(1, json.getString("coins"));
                return account_data;
            } else {
                // продукт с pid не найден
                account_data.add(0,"0");
                account_data.add(1, "0");
                account_data.add("No entry found");
                return account_data;
            }
        } catch (NullPointerException | JSONException e) {
            account_data.add(0, "0");
            account_data.add(1, "0");
            account_data.add(e.getMessage());
            return account_data;
        }
    }

    @Override
    protected void onPostExecute(List<String> result) {
        // закрываем диалог прогресс
        Log.d(TAG, "Account data was written with the result: " + result);
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(List<String> output);
    }
}