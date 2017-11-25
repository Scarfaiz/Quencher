package com.scarfaiz.cluckinbell;

import android.os.AsyncTask;
import android.util.Log;
import libs.JSONParser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 19.11.2017.
 */

class GetEntryData extends AsyncTask<String, List<String>, List<String>> {

    private static JSONParser jsonParser;
    private List<NameValuePair> entry_data;
    private List<String> marker_data;

    private static String server_address;
    private static String server_db;
    private static String db_table;
    private static int id;
    public static String TAG = "LogDebug";

    private static final String TAG_SUCCESS = "success";

    public  GetEntryData(String server_address, String server_db, String db_table, int id) {
        this.server_address = server_address;
        this.server_db = server_db;
        this.db_table = db_table;
        this.id = id;
    }

    @Override
    protected List<String> doInBackground(String[] params) {

        Log.d(TAG, "Sending JSON request");
        jsonParser = new JSONParser();
        int success;
            // Список параметров
        entry_data = new ArrayList<NameValuePair>();
        marker_data = new ArrayList<String>();
        entry_data.add(new BasicNameValuePair("id", String.valueOf(id)));
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
                marker_data.add(2, json.getString("image"));
                marker_data.add(3, json.getString("working_hours"));
                marker_data.add(4, json.getString("product_range"));
                marker_data.add(5, json.getString("confirmation_status"));
                marker_data.add(6, json.getString("comments"));
                marker_data.add(7, json.getString("latitude"));
                marker_data.add(8, json.getString("longitude"));
                return marker_data;
            } else {
                // продукт с pid не найден
                return null;
            }
        } catch (NullPointerException e) {
                marker_data.add(e.getMessage());
            return marker_data;
        }catch (JSONException e){
                marker_data.add(e.getMessage());
                return marker_data;
            }
    }
    @Override
    protected void onPostExecute(List<String> result) {
        // закрываем диалог прогресс

        Log.d(TAG, "Entry data was written with the result: " + result);
    }
}