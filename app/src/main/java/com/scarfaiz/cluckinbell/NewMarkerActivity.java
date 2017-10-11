package com.scarfaiz.cluckinbell;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by higgs on 07.10.2017.
 */

public class NewMarkerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_marker);



        Toolbar toolbar = findViewById(R.id.marker_layout_toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMarkerActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
