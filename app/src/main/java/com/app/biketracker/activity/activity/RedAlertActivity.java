package com.app.biketracker.activity.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.biketracker.R;
import com.preference.PowerPreference;

public class RedAlertActivity extends AppCompatActivity {

    TextView txtLog;
    ImageView imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_alert);
        txtLog=findViewById(R.id.txtLog);
        imgBack=findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        txtLog.setText(PowerPreference.getDefaultFile().getString("log"));
    }
}