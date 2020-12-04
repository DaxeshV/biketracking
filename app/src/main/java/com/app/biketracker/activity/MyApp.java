package com.app.biketracker.activity;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.preference.PowerPreference;

import ovh.karewan.knble.KnBle;

public class MyApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        KnBle.getInstance().init(MyApp.this);
        MultiDex.install(this);
        PowerPreference.init(MyApp.this);
    }
}
