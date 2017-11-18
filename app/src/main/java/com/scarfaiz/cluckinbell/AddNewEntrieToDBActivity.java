package com.scarfaiz.cluckinbell;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import libs.JSONParser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewEntryActivity extends Activity {
    JSONParser jsonParser = new JSONParser();
    String TAG = "LogDebug";

    private static String server_address;
    private static String server_db;
    private static String db_table;
    private static ArrayList<String> entry_data;

    private static final String TAG_SUCCESS = "success";

    public  NewEntryActivity(String server_address, String server_db, String db_table, ArrayList<String> entry_data) {
        this.server_address = server_address;
        this.server_db = server_db;
        this.db_table = db_table;
        this.entry_data = entry_data;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    class NewEntry extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG,"Creating new entry to " + server_db + ":" + db_table);
        }
        protected String doInBackground(String[] args) {
            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("title", entry_data.get(0)));
            params.add(new BasicNameValuePair("address", entry_data.get(1)));
            params.add(new BasicNameValuePair("image", entry_data.get(2)));
            params.add(new BasicNameValuePair("working hours", entry_data.get(3)));
            params.add(new BasicNameValuePair("product_range", entry_data.get(4)));
            params.add(new BasicNameValuePair("comments", entry_data.get(5)));
            params.add(new BasicNameValuePair("confirmation_status", entry_data.get(6)));
            params.add(new BasicNameValuePair("latitude", entry_data.get(7)));
            params.add(new BasicNameValuePair("longitude", entry_data.get(8)));
            // получаем JSON объект
            JSONObject json = jsonParser.makeHttpRequest(server_address, "POST", params);

            Log.d(TAG, "JSON response: " + json.toString());

            /*try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // продукт удачно создан
                    Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                    startActivity(i);

                    // закрываем это окно
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;*/
            try {
                int success = json.getInt(TAG_SUCCESS);
                return String.valueOf(success);
            } catch (JSONException e) {
                e.printStackTrace();
                return e.toString();
            }
        }
        /**
         * После оконачния скрываем прогресс диалог
         **/
        protected void onPostExecute(String file_url) {
            Log.d(TAG, "New Entry created with code: " );
        }

    }

}