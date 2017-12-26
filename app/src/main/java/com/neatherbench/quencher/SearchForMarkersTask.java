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

public class SearchForMarkersTask extends AsyncTask<String, List<String>, List<String>> {

    private static final String TAG_SUCCESS = "success";
    private static String TAG = "LogDebug";
    private static String city;
    private static String latitude;
    private static String longitude;
    private AsyncResponse delegate = null;
    @SuppressLint("StaticFieldLeak")
    private static Context context;


    public SearchForMarkersTask(String latitude, String longitude, String city, Context context, AsyncResponse delegate) {
        SearchForMarkersTask.latitude = latitude;
        SearchForMarkersTask.longitude = longitude;
        SearchForMarkersTask.city = city;
        SearchForMarkersTask.context = context;
        this.delegate = delegate;
    }

    public interface AsyncResponse {
        void processFinish(List<String> output);
    }

    @Override
    protected List<String> doInBackground(String[] params) {
        String server_address = "https://178.162.41.115/search_for_markers.php";
        Log.d(TAG, "Sending JSON request");
        JSONParser jsonParser = new JSONParser(context);
        int success;
        // Список параметров
        List<NameValuePair> entry_data = new ArrayList<>();
        List<String> markers_found = new ArrayList<>();
        entry_data.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
        entry_data.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
        entry_data.add(new BasicNameValuePair("city", String.valueOf(city)));
        Log.d(TAG, "server address: " + server_address + "   enrty data: " + entry_data.toString());
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", entry_data);
        try {
            //Log.d(TAG, json.toString());

            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                markers_found.add(0, json.getString("id"));
                markers_found.add(1, json.getString("latitude"));
                markers_found.add(2, json.getString("longitude"));
                markers_found.add(3, json.getString("confirmation_status"));
                markers_found.add(4, String.valueOf(json.length()));
                return markers_found;
            } else {
                // продукт с pid не найден
                return null;
            }
        } catch (NullPointerException | JSONException e) {
            markers_found.add(e.getMessage());
            return markers_found;
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