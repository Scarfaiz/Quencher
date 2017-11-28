package com.scarfaiz.cluckinbell;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;


public class NewMarkerActivity extends AppCompatActivity {

    public EditText marker_name;
    public EditText marker_address;
    public Spinner marker_o_h_spinner;
    public Spinner marker_range_spinner;
    public EditText marker_comments;
    private static Double latitude;
    private static Double longitude;
    private static String city;
    private static String server_address;
    private static String address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_marker);
        Window window = this.getWindow();
        marker_name = findViewById(R.id.marker_name);
        marker_address = findViewById(R.id.marker_address);
        marker_o_h_spinner = findViewById(R.id.marker_o_h_spinner);
        marker_range_spinner = findViewById(R.id.marker_range_spinner);
        marker_comments = findViewById(R.id.marker_comments);
        Bundle b = getIntent().getExtras();
        assert b != null;
        latitude = b.getDouble("latitude");
        longitude = b.getDouble("longitude");
        city = b.getString("city");
        address = b.getString("address");
        marker_address.setText(address);
        server_address = "http://178.162.41.115/add_entry.php";
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        Toolbar toolbar = findViewById(R.id.marker_layout_toolbar);
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
        List<NameValuePair> marker_data = new ArrayList<>();
        marker_data.add(new BasicNameValuePair("title", marker_name.getText().toString()));
        marker_data.add(new BasicNameValuePair("address", marker_address.getText().toString()));
        marker_data.add(new BasicNameValuePair("city", city));
        marker_data.add(new BasicNameValuePair("working_hours", String.valueOf(marker_o_h_spinner.getSelectedItemPosition())));
        marker_data.add(new BasicNameValuePair("product_range", String.valueOf(marker_range_spinner.getSelectedItemPosition())));
        marker_data.add(new BasicNameValuePair("comments", marker_comments.getText().toString()));
        marker_data.add(new BasicNameValuePair("latitude", latitude.toString()));
        marker_data.add(new BasicNameValuePair("longitude", longitude.toString()));
        if(marker_name.getText() !=null && marker_address.getText() !=null) {
            new AddEntryTask(server_address, marker_data).execute();
            Toast.makeText(NewMarkerActivity.this, "Ваша заяка отправлена на модерацию", Toast.LENGTH_SHORT);
            this.finish();
        }
        else {
            Toast.makeText(NewMarkerActivity.this, "Необходимо заполнить обязательные поля", Toast.LENGTH_SHORT);
        }
    }

}
