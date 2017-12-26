package libs;

import android.content.Context;
import android.util.Log;
import com.neatherbench.quencher.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.security.KeyStore;
import java.util.List;

public class JSONParser{

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    static String TAG = "LogDebug";
    static Context context;
    // constructor
    public JSONParser(Context context){
        this.context = context;
    }

    // метод получение json объекта по url
    // используя HTTP запрос и методы POST или GET

    protected org.apache.http.conn.ssl.SSLSocketFactory createAdditionalCertsSSLSocketFactory() {
        try {
            final KeyStore ks = KeyStore.getInstance("BKS");

            // the bks file we generated above
            final InputStream in = context.getResources().openRawResource( R.raw.server);
            try {
                // don't forget to put the password used above in strings.xml/mystore_password
                ks.load(in, context.getString( R.string.mystore_password ).toCharArray());
            } finally {
                in.close();
            }

            return new AdditionalKeyStoresSSLSocketFactory(ks);

        } catch( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject makeHttpRequest(String url, String method, List<NameValuePair> params) {

        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", createAdditionalCertsSSLSocketFactory(), 443));
        final BasicHttpParams basicHttpParamsrams = new BasicHttpParams();
        final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(basicHttpParamsrams,schemeRegistry);
        DefaultHttpClient httpClient = new DefaultHttpClient(cm, null);
        // Создаем HTTP запрос
        try {

            // проверяем метод HTTP запроса
            if(method == "POST"){

                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }else if(method == "GET"){
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                Log.d(TAG, "Result url for sending to the server: " + url);
                HttpGet httpGet = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "An error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.d(TAG, "An error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            StringBuilder sb = null;
            sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
                Log.d(TAG, "Line appended " + line);
            }
            is.close();
            json = sb.toString();
            Log.d(TAG, json);
        } catch (Exception e) {
            Log.d(TAG, "Error converting result " + e.toString());
        }

        // пытаемся распарсить строку в JSON объект
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.d(TAG, "Error parsing data " + e.toString());
        }

        // возвращаем JSON строку
        return jObj;
    }

}
