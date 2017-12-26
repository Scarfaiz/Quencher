package com.neatherbench.quencher;

import android.annotation.SuppressLint;
import android.content.Context;
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

class GetCommentsTask extends AsyncTask<String, List<String>, List<String>> {

    private static final String TAG_SUCCESS = "success";
    private static String server_address;
    private static int id;
    private static String TAG = "LogDebug";
    private AsyncResponse delegate = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public GetCommentsTask(String server_address, int id, Context context, AsyncResponse delegate) {
        GetCommentsTask.server_address = server_address;
        GetCommentsTask.id = id;
        GetCommentsTask.context = context;
        this.delegate = delegate;
    }

    @Override
    protected List<String> doInBackground(String[] params) {

        Log.d(TAG, "Sending JSON request");
        JSONParser jsonParser = new JSONParser(context);
        int success;
        // Список параметров
        List<NameValuePair> entry_data = new ArrayList<>();
        List<String> marker_data = new ArrayList<>();
        entry_data.add(new BasicNameValuePair("id", String.valueOf(id)));
        Log.d(TAG, "server address: " + server_address + "   enrty data: " + entry_data.toString());
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", entry_data);
        try {
            success = json.getInt(TAG_SUCCESS);
            marker_data.add(0, String.valueOf(success));
            if (success == 1) {
                marker_data.add(1, json.getString("username"));
                marker_data.add(2, json.getString("comments"));
                return marker_data;
            } else {
                // продукт с pid не найден
                marker_data.add("No entry found");
                return marker_data;
            }
        } catch (NullPointerException | JSONException e) {
            marker_data.add(0, "0");
            marker_data.add(1, e.getMessage());
            return marker_data;
        }
    }

    @Override
    protected void onPostExecute(List<String> result) {
        // закрываем диалог прогресс
        Log.d(TAG, "Comments data was written with the result: " + result);
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(List<String> output);
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