package com.neatherbench.quencher;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import libs.JSONParser;

class LoginTask extends AsyncTask<String, List<String>  , List<String> > {

    private JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private String server_address;
    private String TAG = "LogDebug";
    private List<NameValuePair> login_data;
    private LoginTask.AsyncResponse delegate = null;

    public LoginTask(String server_address, List<NameValuePair> login_data,  AsyncResponse delegate)
    {
        this.server_address = server_address;
        this.login_data = login_data;
        this.delegate = delegate;
    }

    @Override
    protected List<String>  doInBackground(String[] args){
        JSONObject json = jsonParser.makeHttpRequest(server_address, "GET", login_data);
        List<String> result = new ArrayList<>();
        try {
            Log.d(TAG, "JSON response: " + json.toString());
            try {
                int success = json.getInt(TAG_SUCCESS);
                result.add(0, String.valueOf(success));
                if(json.has("username") && json.has("reputation")) {
                    String name = json.getString("username");
                    int reputation = json.getInt("reputation");
                    result.add(1, name);
                    result.add(2, String.valueOf(reputation));
                }
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
                result.add(1, e.toString());
                return result;
            }
        }catch (NullPointerException e)
        {
            result.add(0, e.toString());
            return result;
        }

    }
    protected void onPostExecute(List<String> result) {
        // закрываем диалог прогресс
        Log.d(TAG, "Login was proceeded with the result: " + result);
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(List<String> output);
    }

}