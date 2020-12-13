package com.app.biketracker.activity;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.preference.PowerPreference;

public class MyApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(MyApp.this);
        PowerPreference.init(MyApp.this);
    }
}
