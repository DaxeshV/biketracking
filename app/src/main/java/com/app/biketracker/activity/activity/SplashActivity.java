package com.app.biketracker.activity.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.biketracker.R;
import com.app.biketracker.activity.utils.ConstantMethod;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.preference.PowerPreference;

public class SplashActivity extends AppCompatActivity {
    LocationManager manager;
    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    BluetoothAdapter mBluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        ImageView animateWow=findViewById(R.id.animateWow);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setupGPS();
        setupLogPermission();
        drawOverApps();
        setupLogPermission();
        setupLocation();
        setupBLE();
        try{
            Glide.with(SplashActivity.this)
                    .asGif()
                    .load(R.raw.anim)
                    .into(new ImageViewTarget<GifDrawable>(animateWow) {
                        @Override
                        protected void setResource(@Nullable GifDrawable resource) {
                            animateWow.setImageDrawable(resource);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_READER)&&
                    PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_DRAW_OTHER)
                            &&PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_LOCATION)&&
                    PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_GPS)&&
                    PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_BLE)){
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                SplashActivity.this.finish();
            }else {
                Intent i = new Intent(SplashActivity.this, PermissionActivity.class);
                startActivity(i);
                SplashActivity.this.finish();
            }

        }, 3000);
    }

    private void setupBLE() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_BLE,false);
        }else {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_BLE,true);
        }
    }

    private void drawOverApps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_DRAW_OTHER,false);
            }else {
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_DRAW_OTHER,true);

            }
        }else {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_DRAW_OTHER,true);

        }
    }


    private void setupLogPermission() {
        boolean isNotificationServiceRunning = isNotificationServiceRunning();
        if (!isNotificationServiceRunning) {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_READER,false);
        }else {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_READER,true);

        }
    }
    private boolean isNotificationServiceRunning() {
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }
    private void setupGPS() {

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_GPS,false);
        } else {

            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_GPS,true);

        }
    }
    public void setupLocation(){
        if (hasPermissions(SplashActivity.this, PERMISSIONS)) {

            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,true);
        } else {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,false);

        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}