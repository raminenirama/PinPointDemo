package com.tech.awspinpointdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AnalyticsManager.INSTANCE.initialize(getApplicationContext());
        AnalyticsManager.INSTANCE.start();
        AnalyticsManager.INSTANCE.logEvent("Test123", "Test123");
    }


    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsManager.INSTANCE.stop();
    }
}
