package com.neatherbench.quencher;

import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import static com.neatherbench.quencher.R.color.colorPrimary;


public class NewMarkerActivity extends AppCompatActivity {

    public EditText marker_name;
    public EditText marker_address;
    public Spinner marker_o_h_spinner;
    public Spinner marker_range_spinner;
    public EditText marker_comments;
    public EditText marker_time;
    public Spinner marker_type_spinner;
    private static Double latitude;
    private static Double longitude;
    private static String city;
    private static String server_address;
    private static String reputation;
    private static String time = "22:00";
    private boolean name_changed = false;
    private boolean address_changed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_marker);
        Window window = this.getWindow();
        marker_name = findViewById(R.id.marker_name);
        marker_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                name_changed = true;
            }
        });
        marker_address = findViewById(R.id.marker_address);
        marker_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                address_changed = true;
            }
        });
        marker_o_h_spinner = findViewById(R.id.marker_o_h_spinner);
        marker_range_spinner = findViewById(R.id.marker_range_spinner);
        marker_comments = findViewById(R.id.marker_comments);
        marker_time = findViewById(R.id.timeText);
        marker_type_spinner = findViewById(R.id.marker_type_spinner);
        final TimePickerDialog tpd = new TimePickerDialog(NewMarkerActivity.this, R.style.DialogTheme,new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                time = String.valueOf(i) + ":" + String.valueOf(i1);
                marker_time.setText(String.valueOf(i) + ":" + String.valueOf(i1));
            }
        }, 22, 0, true);

        marker_o_h_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0) {
                        time = "22:00";
                        marker_time.setClickable(false);
                        marker_time.setFocusable(false);
                        marker_time.setText("");
                        marker_time.setOnClickListener(null);
                    }
                    else if(i==1) {
                        time = "22:00";
                        marker_time.setClickable(false);
                        marker_time.setFocusable(false);
                        marker_time.setText(time);
                        marker_time.setOnClickListener(null);
                    }
                    else if(i==2) {
                        marker_time.setClickable(true);
                        marker_time.setFocusable(true);
                    marker_time.setText("22:00");
                        marker_time.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                tpd.show();
                            }
                        });
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Bundle b = getIntent().getExtras();
        assert b != null;
        latitude = b.getDouble("latitude");
        longitude = b.getDouble("longitude");
        city = b.getString("city");
        String address = b.getString("address");
        reputation = b.getString("reputation");
        marker_address.setText(address);
        server_address = "https://178.162.41.115/add_entry.php";
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        List<NameValuePair> marker_data = new ArrayList<>();
        marker_data.add(new BasicNameValuePair("title", marker_name.getText().toString()));
        marker_data.add(new BasicNameValuePair("address", marker_address.getText().toString()));
        marker_data.add(new BasicNameValuePair("city", city));
        marker_data.add(new BasicNameValuePair("working_hours", String.valueOf(marker_o_h_spinner.getSelectedItemPosition())));
        marker_data.add(new BasicNameValuePair("product_range", String.valueOf(marker_range_spinner.getSelectedItemPosition())));
        marker_data.add(new BasicNameValuePair("confirmation_status", reputation));
        marker_data.add(new BasicNameValuePair("username", prefs.getString("username", "skipped")));
        marker_data.add(new BasicNameValuePair("comments", marker_comments.getText().toString()));
        marker_data.add(new BasicNameValuePair("latitude", latitude.toString()));
        marker_data.add(new BasicNameValuePair("longitude", longitude.toString()));
        marker_data.add(new BasicNameValuePair("close_hours", time));
        marker_data.add(new BasicNameValuePair("type", String.valueOf(marker_type_spinner.getSelectedItem())));
        if(name_changed && address_changed) {
            executeAsyncTask(new AddEntryTask(server_address, marker_data, NewMarkerActivity.this.getApplicationContext()));
            Toast.makeText(NewMarkerActivity.this, "Ваша заяка отправлена на модерацию", Toast.LENGTH_SHORT).show();
            this.finish();
        }
        else {
            Toast.makeText(NewMarkerActivity.this, "Необходимо заполнить обязательные поля", Toast.LENGTH_SHORT).show();
        }
    }

    @SafeVarargs
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }
}
