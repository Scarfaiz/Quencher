package com.scarfaiz.cluckinbell;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import libs.JSONParser;

public class LeaderboardTask extends AsyncTask<String, List<String>, List<String>> {

    private static final String TAG_SUCCESS = "success";
    private static String TAG = "LogDebug";
    private static String username;
    private AsyncResponse delegate = null;


    public LeaderboardTask(String username, AsyncResponse delegate) {
        LeaderboardTask.username = username;
        this.delegate = delegate;
    }

    public interface AsyncResponse {
        void processFinish(List<String> output);
    }

    @Override
    protected List<String> doInBackground(String[] params) {
        String server_address = "http://178.162.41.115/leaderboard_data.php";
        Log.d(TAG, "Sending JSON request");
        JSONParser jsonParser = new JSONParser();
        int success;
        // Список параметров
        List<NameValuePair> username_data = new ArrayList<>();
        List<String> entries_found = new ArrayList<>();
        username_data.add(new BasicNameValuePair("username", username));
        Log.d(TAG, "server address: " + server_address + "   username: " + username_data.toString());
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", username_data);
        try {
            //Log.d(TAG, json.toString());

            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                entries_found.add(0, json.getString("username"));
                entries_found.add(1, json.getString("reputation"));
                return entries_found;
            } else {
                // продукт с pid не найден
                return null;
            }
        } catch (NullPointerException | JSONException e) {
            entries_found.add(e.getMessage());
            return entries_found;
        }
    }

    @Override
    protected void onPostExecute(List<String> result) {
        Log.d(TAG, "Database was searched for entries with result: " + result);
        delegate.processFinish(result);

    }

    static class GenSet<E> {

        E[] a;

        GenSet(Class<E> c, int s) {
            // Use Array native method to create array
            // of a type only known at run time
            @SuppressWarnings("unchecked") final E[] a = (E[]) Array.newInstance(c, s);
            this.a = a;
        }

        E get(int i) {
            return a[i];
        }
    }
}