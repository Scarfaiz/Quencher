package com.scarfaiz.cluckinbell;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import libs.JSONParser;

/**
 * Created by USER on 22.11.2017.
 */

public class GetEntryDetails extends AsyncTask<String, Integer, Integer> {
    //Add your ID for identification

    private static String json;
    private ArrayList<String> marker_data;

    private static Integer success = 0;
    private static String id;
    public static String TAG = "LogDebug";


    public GetEntryDetails(String id) {
        this.id = id;
    }

    @Override
    protected Integer doInBackground(String... params) {

        try {
            //Objects of HttpClient and HttpPost
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://178.162.41.115/get_entry_details.php");
            HttpParams httpParams = new BasicHttpParams();
            int timeoutConnection = 5000;
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
            int timeoutSocket = 7000;
            HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
            Log.d(TAG, id);
            httpPost.setHeader("id", id); //ID

            //Post request
            try {
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream inputStream = null;
                inputStream = httpEntity.getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.d(TAG, line);
                    stringBuilder.append(line + "\n");
                }
                String result = null;
                result = stringBuilder.toString();
                json = result;
            } catch (ClientProtocolException cpe) {
                cpe.printStackTrace();
                Log.d(TAG, cpe.getMessage());
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.d(TAG, ioe.getMessage());
            }

            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            Log.d(TAG, ie.getMessage());
        }

        //JSON response
        try {
            JSONObject jsonObject = new JSONObject(json);
            success = jsonObject.getInt("success");

            if (success == 1) { //If success is 1 get data
                marker_data = new ArrayList<String>();
                marker_data.add(0, jsonObject.getString("title"));
                marker_data.add(1, jsonObject.getString("address"));
                marker_data.add(2, jsonObject.getString("image"));
                marker_data.add(3, jsonObject.getString("working_hours"));
                marker_data.add(4, jsonObject.getString("product_range"));
                marker_data.add(5, jsonObject.getString("confirmation_status"));
                marker_data.add(6, jsonObject.getString("comments"));
                marker_data.add(7, jsonObject.getString("latitude"));
                marker_data.add(8, jsonObject.getString("longitude"));
            }
        } catch (JSONException je) { //Otherveis exception
            je.printStackTrace();
            Log.d(TAG, je.getMessage());
        }

        return success;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        integer = success;
        Log.d(TAG, String.valueOf(integer));
    }
}