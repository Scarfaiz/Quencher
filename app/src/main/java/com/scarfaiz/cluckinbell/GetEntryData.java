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

class GetEntryData extends AsyncTask<String, String, String> {

    private static JSONParser jsonParser;
    private List<NameValuePair> entry_data;

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
    protected String doInBackground(String[] params) {

        Log.d(TAG, "Sending JSON request");
        jsonParser = new JSONParser();
        int success;
            // Список параметров
        entry_data = new ArrayList<NameValuePair>();
        entry_data.add(new BasicNameValuePair("id", String.valueOf(id)));
            Log.d(TAG, "server address: " + server_address + "   enrty data: " + entry_data.toString());
            JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", entry_data);
            try {
            //Log.d(TAG, json.toString());

            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                JSONArray productObj = json.getJSONArray(db_table);
                JSONObject entry = productObj.getJSONObject(0);
                return entry.toString();
            } else {
                // продукт с pid не найден
                return null;
            }
        } catch (NullPointerException e) {
            return e.getMessage();
        }catch (JSONException e){
                return e.getMessage();
            }
    }
    @Override
    protected void onPostExecute(String result) {
        // закрываем диалог прогресс
        Log.d(TAG, "Entry data was written with the result: " + result);
    }
}