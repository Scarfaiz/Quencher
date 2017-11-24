package com.scarfaiz.cluckinbell;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class NewMarkerActivity extends AppCompatActivity {

    public EditText marker_name;
    public EditText marker_address;
    public ImageView marker_photos_view;
    public Spinner marker_o_h_spinner;
    public Spinner marker_range_spinner;
    public EditText marker_comments;
    private static Double latitude;
    private static Double longitude;
    private static List<NameValuePair> marker_data;
    private static String server_address;
    private static String server_db;
    private static String db_table;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_marker);
        Window window = this.getWindow();

        marker_name = (EditText)findViewById(R.id.marker_name);
        marker_address = (EditText)findViewById(R.id.marker_address);
        marker_photos_view = (ImageView)findViewById(R.id.marker_photos_view);
        marker_o_h_spinner = (Spinner)findViewById(R.id.marker_o_h_spinner);
        marker_range_spinner = (Spinner)findViewById(R.id.marker_range_spinner);
        marker_comments = (EditText)findViewById(R.id.marker_comments);
        Bundle b = getIntent().getExtras();
        latitude = b.getDouble("latitude");
        longitude = b.getDouble("longitude");

        server_address = "http://178.162.41.115/add_entry.php";
        server_db = "cb_database";
        db_table = "marker_data";

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        Toolbar toolbar = (Toolbar)findViewById(R.id.marker_layout_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMarkerActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
    public void uphandler(View v){
        marker_data = new ArrayList<NameValuePair>();
        marker_data.add(new BasicNameValuePair("title", marker_name.getText().toString()));
        marker_data.add(new BasicNameValuePair("address", marker_address.getText().toString()));
        //marker_data.add(new BasicNameValuePair("image", marker_photos_view.getDrawable().toString()));
        marker_data.add(new BasicNameValuePair("working_hours", String.valueOf(marker_o_h_spinner.getSelectedItemPosition())));
        marker_data.add(new BasicNameValuePair("product_range", String.valueOf(marker_range_spinner.getSelectedItemPosition())));
        marker_data.add(new BasicNameValuePair("comments", marker_comments.getText().toString()));
        marker_data.add(new BasicNameValuePair("latitude", latitude.toString()));
        marker_data.add(new BasicNameValuePair("longitude", longitude.toString()));
        Log.d("LogDebug", "marker_data completed");
        new NewEntryActivity(server_address,server_db,db_table,marker_data).execute();
        this.finish();    // This will kill current activity, and if previous activity is still opened in background, it will come in front.
    }

}
