package com.scarfaiz.cluckinbell;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by higgs on 07.10.2017.
 */

public class NewMarkerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_marker);
        Toolbar toolbar = (Toolbar)findViewById(R.id.marker_layout_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMarkerActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
    public void uphandler(View v){
        this.finish();    // This will kill current activity, and if previous activity is still opened in background, it will come in front.
    }
}
