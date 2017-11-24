package com.scarfaiz.cluckinbell;

import android.os.AsyncTask;
import android.util.Log;
import libs.JSONParser;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

class NewEntryActivity extends AsyncTask<String, String, String> {

    JSONParser jsonParser = new JSONParser();
    String TAG = "LogDebug";

    private static String server_address;
    private static String server_db;
    private static String db_table;
    private static List<NameValuePair> new_entry_data = null;

    private static final String TAG_SUCCESS = "success";

    public  NewEntryActivity(String server_address, String server_db, String db_table, List<NameValuePair> new_entry_data) {
        this.server_address = server_address;
        this.server_db = server_db;
        this.db_table = db_table;
        this.new_entry_data = new_entry_data;
    }



        @Override
        protected String doInBackground(String[] args){
            // получаем JSON объект
            Log.d(TAG,"Creating new entry to " + server_db + ": " + db_table);
            JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", new_entry_data);
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
            Log.d(TAG, "New Entry created with code: " + result);
        }

    }