package com.scarfaiz.cluckinbell;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import libs.JSONParser;

import static com.scarfaiz.cluckinbell.MainActivity.map;

/**
 * Created by USER on 24.11.2017.
 */

public class SearchForMarkers extends AsyncTask<String, List<String>, List<String>> {

    private static JSONParser jsonParser;
    private List<NameValuePair> entry_data;
    private List<String> markers_found;

    private MapView map;
    private static String city;
    private static String latitude;
    private static String longitude;
    private static String server_address;
    public static String TAG = "LogDebug";

    private static final String TAG_SUCCESS = "success";

    public  SearchForMarkers(String latitude, String longitude, String city, MapView map) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.map = map;
    }

    @Override
    protected List<String> doInBackground(String[] params) {
        server_address = "http://178.162.41.115/search_for_markers.php";
        Log.d(TAG, "Sending JSON request");
        jsonParser = new JSONParser();
        int success;
        // Список параметров
        entry_data = new ArrayList<NameValuePair>();
        markers_found = new ArrayList<String>();
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
                markers_found.add(3, String.valueOf(json.length()));
                return markers_found;
            } else {
                // продукт с pid не найден
                return null;
            }
        } catch (NullPointerException e) {
            markers_found.add(e.getMessage());
            return markers_found;
        }catch (JSONException e){
            markers_found.add(e.getMessage());
            return markers_found;
        }
    }
    @Override
    protected void onPostExecute(List<String> markers_found) {
        // закрываем диалог прогресс

        Double latitude;
        Double longitude;
        int id;
        String[] latitude_splited = markers_found.get(1).split("\\s+");
        String[] longitude_splited = markers_found.get(2).split("\\s+");
        String[] id_splited = markers_found.get(0).split("\\s+");
        GenSet<Marker> startMarker = new GenSet<Marker>(Marker.class, id_splited.length);
        for(int i = 0; i< Integer.valueOf(id_splited.length); i++) {
            latitude = Double.valueOf(latitude_splited[i]);
            longitude = Double.valueOf(longitude_splited[i]);
            id = Integer.valueOf(id_splited[i]);
            Log.d(TAG, "Split markers data info: " + latitude + " " + longitude + " " + id + " with array length: " + id_splited.length);
            GeoPoint p = new GeoPoint(latitude, longitude);
            startMarker.a[i] = new Marker(map);
            startMarker.a[i].setPosition(p);
            startMarker.a[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.a[i].setTitle(String.valueOf(id));
            map.getOverlays().add(startMarker.a[i]);
            map.invalidate();
        }
        Log.d(TAG, "Database was searched for entries with result: " + markers_found);
    }

    public class GenSet<E> {

        private E[] a;

        public GenSet(Class<E> c, int s) {
            // Use Array native method to create array
            // of a type only known at run time
            @SuppressWarnings("unchecked")
            final E[] a = (E[]) Array.newInstance(c, s);
            this.a = a;
        }

        E get(int i) {
            return a[i];
        }
    }
}