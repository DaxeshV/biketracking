package com.app.biketracker.activity.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.biketracker.R;
import com.app.biketracker.activity.smslogHelper.NotificationListener;
import com.app.biketracker.activity.utils.ConstantMethod;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;
import com.google.android.gms.location.LocationRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.preference.PowerPreference;

import java.util.List;

import static com.example.easywaylocation.EasyWayLocation.LOCATION_SETTING_REQUEST_CODE;

public class PermissionActivity extends AppCompatActivity implements Listener, View.OnClickListener {
    Button btnGps, btnBLE, btnLocation, btnReader, btnDrawOverApps;
    ImageView imgLocationEnable, imgEnableGps, imgEnableBle, imgEnableNoti, imgDisplayEnable;
    int REQUEST_ENABLE_BT = 12;
    int REQUEST_LOCATION = 121;
    int DRAWAPP = 151;
    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    LocationRequest request;
    EasyWayLocation easyWayLocation;
    Button btnStart;
     LocationManager manager;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter  = bluetoothManager.getAdapter();
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        request  = new LocationRequest();
        request.setInterval(50000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        easyWayLocation = new EasyWayLocation(PermissionActivity.this, request, false, PermissionActivity.this);
        btnGps = findViewById(R.id.btnGPS);
        btnBLE = findViewById(R.id.imgBLE);
        btnLocation = findViewById(R.id.btnLocation);
        btnReader = findViewById(R.id.btnReader);
        btnDrawOverApps = findViewById(R.id.btnEnableDisplayOver);
        imgLocationEnable = findViewById(R.id.imgLocationEnable);
        imgEnableGps = findViewById(R.id.imgEnableGps);
        imgEnableBle = findViewById(R.id.imgEnableBle);
        imgEnableNoti = findViewById(R.id.imgEnableNoti);
        btnStart = findViewById(R.id.btnStart);
        imgDisplayEnable = findViewById(R.id.imgDisplayEnable);
        btnDrawOverApps.setOnClickListener(this);
        btnReader.setOnClickListener(this);
        btnBLE.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        btnGps.setOnClickListener(this);
        setupBluetoothButtonColor();
        setupLocationPermission();
        setupGPS();
        setupLogPermission();
        doNotKillService();
        drawOverApps();
        enableButton();
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this,MainActivity.class);
                startActivity(intent);
                PermissionActivity.this.finish();
            }
        });
    }

    private void enableButton() {

        if(PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_READER)&&
                PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_DRAW_OTHER)
                &&PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_LOCATION)&&
                PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_GPS)&&
                PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_BLE)){
             btnStart.setVisibility(View.VISIBLE);

        }else {
            btnStart.setVisibility(View.GONE);
        }
    }

    private void drawOverApps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                imgDisplayEnable.setVisibility(View.GONE);
                btnDrawOverApps.setVisibility(View.VISIBLE);

                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_DRAW_OTHER,false);
            }else {
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_DRAW_OTHER,true);
                imgDisplayEnable.setVisibility(View.VISIBLE);
                btnDrawOverApps.setVisibility(View.GONE);
                enableButton();
            }
        }else {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_DRAW_OTHER,true);
            imgDisplayEnable.setVisibility(View.VISIBLE);
            btnDrawOverApps.setVisibility(View.GONE);
            enableButton();
        }
    }


    private void setupLogPermission() {
        boolean isNotificationServiceRunning = isNotificationServiceRunning();
        if (!isNotificationServiceRunning) {
            imgEnableNoti.setVisibility(View.GONE);
            btnReader.setVisibility(View.VISIBLE);

            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_READER,false);
        }else {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_READER,true);
            imgEnableNoti.setVisibility(View.VISIBLE);
            btnReader.setVisibility(View.GONE);
            enableButton();
        }
    }
    private void doNotKillService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
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
            if (hasPermissions(PermissionActivity.this, PERMISSIONS)) {
                btnGps.setVisibility(View.VISIBLE);
                imgEnableGps.setVisibility(View.GONE);
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,true);
            } else {
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,false);
                Toast.makeText(this, "Please enable location permission", Toast.LENGTH_SHORT).show();
            }
        } else {
            btnGps.setVisibility(View.GONE);
            imgEnableGps.setVisibility(View.VISIBLE);
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_GPS,true);
            enableButton();
        }
    }
    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }


    private void setupLocationPermission() {
        if (hasPermissions(PermissionActivity.this, PERMISSIONS)) {
            btnLocation.setVisibility(View.GONE);
            imgLocationEnable.setVisibility(View.VISIBLE);
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,true);
            enableButton();
        } else {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,false);
            btnLocation.setVisibility(View.VISIBLE);
            imgLocationEnable.setVisibility(View.GONE);

        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PermissionActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use MainActivity.this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_LOCATION);
    }

    private void setupBluetoothButtonColor() {

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device Not Support BLE", Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_BLE,false);
            btnBLE.setVisibility(View.VISIBLE);
            imgEnableBle.setVisibility(View.GONE);
        } else {
            btnBLE.setVisibility(View.GONE);
            imgEnableBle.setVisibility(View.VISIBLE);
            PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_BLE,true);
            enableButton();
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                btnBLE.setVisibility(View.GONE);
                imgEnableBle.setVisibility(View.VISIBLE);
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_BLE,true);
                enableButton();
            } else {
                btnBLE.setVisibility(View.VISIBLE);
                imgEnableBle.setVisibility(View.GONE);
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_BLE,false);
            }
        } else if (requestCode == REQUEST_LOCATION) {
            if (resultCode == RESULT_OK) {
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,true);
                btnLocation.setVisibility(View.GONE);
                imgLocationEnable.setVisibility(View.VISIBLE);
                enableButton();
            } else {
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,false);
                btnLocation.setVisibility(View.VISIBLE);
                imgLocationEnable.setVisibility(View.GONE);
            }
        }else if(requestCode ==LOCATION_SETTING_REQUEST_CODE){
            easyWayLocation.onActivityResult(resultCode);
        }else if(requestCode==555){
            if(isNotificationServiceRunning()){
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_READER,true);
                btnReader.setVisibility(View.GONE);
                imgEnableNoti.setVisibility(View.VISIBLE);
                enableButton();
            }else {
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_READER,false);
                btnReader.setVisibility(View.VISIBLE);
                imgEnableNoti.setVisibility(View.GONE);
            }

        }else if(requestCode==DRAWAPP){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_DRAW_OTHER,false);
                    imgDisplayEnable.setVisibility(View.GONE);
                    btnDrawOverApps.setVisibility(View.VISIBLE);

                }else {
                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_DRAW_OTHER,true);
                    imgDisplayEnable.setVisibility(View.VISIBLE);
                    btnDrawOverApps.setVisibility(View.GONE);
                    enableButton();

                }
            }
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

    @Override
    public void locationOn() {
        PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_GPS,true);
        enableButton();
        btnGps.setVisibility(View.GONE);
        imgEnableGps.setVisibility(View.VISIBLE);
        easyWayLocation.endUpdates();
    }

    @Override
    public void currentLocation(Location location) {

    }

    @Override
    public void locationCancelled() {
        btnGps.setVisibility(View.VISIBLE);
         imgEnableGps.setVisibility(View.GONE);
        easyWayLocation.endUpdates();
        PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_GPS,false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (easyWayLocation != null) {
            easyWayLocation.endUpdates();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.imgBLE: {

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            break;
            case R.id.btnEnableDisplayOver: {
                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                }
                startActivityForResult(intent, DRAWAPP);
            }
            break;
            case R.id.btnReader: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),555);
                }
            }
            break;
            case R.id.btnLocation: {
                Dexter.withContext(PermissionActivity.this)
                        .withPermissions(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    btnLocation.setVisibility(View.GONE);
                                    imgLocationEnable.setVisibility(View.VISIBLE);
                                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,true);
                                    enableButton();
                                } else {
                                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_LOCATION,false);
                                    showSettingsDialog();
                                }

                                if (report.isAnyPermissionPermanentlyDenied()) {
                                    showSettingsDialog();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        })
                        .onSameThread()
                        .check();
            }
            break;
            case R.id.btnGPS: {
                if(!hasGPSDevice(PermissionActivity.this)){
                    Toast.makeText(PermissionActivity.this,"Gps not Supported",Toast.LENGTH_SHORT).show();
                }

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(PermissionActivity.this)) {
                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_GPS,false);
                    easyWayLocation.startLocation();
                }else{
                    btnGps.setVisibility(View.GONE);
                    imgEnableGps.setVisibility(View.VISIBLE);
                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_GPS,true);
                    enableButton();
                }
            }
            break;
        }
    }
}