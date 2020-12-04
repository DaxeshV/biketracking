package com.app.biketracker.activity.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.biketracker.R;
import com.app.biketracker.activity.smslogHelper.NotificationListener;
import com.app.biketracker.activity.utils.ConstantMethod;
import com.app.biketracker.activity.utils.SettingObject;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.preference.PowerPreference;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ovh.karewan.knble.KnBle;
import ovh.karewan.knble.interfaces.BleGattCallback;
import ovh.karewan.knble.interfaces.BleScanCallback;
import ovh.karewan.knble.scan.ScanFilters;
import ovh.karewan.knble.scan.ScanSettings;
import ovh.karewan.knble.struct.BleDevice;

import static ovh.karewan.knble.scan.ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
import static ovh.karewan.knble.scan.ScanSettings.MATCH_MODE_AGGRESSIVE;
import static ovh.karewan.knble.scan.ScanSettings.MATCH_NUM_FEW_ADVERTISEMENT;
import static ovh.karewan.knble.scan.ScanSettings.SCAN_MODE_LOW_LATENCY;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView imgOption, imgLocation, imgBluetooth, imgSecure, linearArm, linearDisArm, imgArm, imgBle;
    BottomSheetDialog bottomSheetDialogOption;
    String mobileNumber, TrackerNumber, BluetoothPIN;
    Button buttonBusy, btnRemote, btnCustom;
    TextView txtSound, txtAlarmSound, txtLog, txtTestSMS, txtShow;
    IndicatorSeekBar indicatorSeekBarWarning, indicatorSeekBarAlarm, indicatorSeekBarDelay, indicator_location;
    Handler handler;
    Runnable r;
    long locationInterval = 25;
    public final static int REQUEST_CODE = 65;

    private static final int REQUEST_CODE_OPEN_GPS = 123;
    private static final int REQUEST_ENABLE_BT = 11;
    private static final int REQUEST_ENABLE_BT1 = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter(ConstantMethod.GetData));
        setupSettingOption();
        initView();
        checkNotifPermission();
        doNotKillService();
        initBleManager();
        setupData();

    }

    // here we setup BLE
    private void initBleManager() {

        boolean isInit = KnBle.getInstance().isInit();
        if (!isInit) {
            Toast.makeText(MainActivity.this, R.string.lbl_notsupport, Toast.LENGTH_SHORT).show();

        }
        ScanSettings settings = new ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_LATENCY).
                setCallbackType(CALLBACK_TYPE_ALL_MATCHES).setMatchMode(MATCH_MODE_AGGRESSIVE).setNbMatch(MATCH_NUM_FEW_ADVERTISEMENT).setReportDelay(0L).
                build();


        // Here we define BLE Name
        ScanFilters filters = new ScanFilters.Builder().addDeviceName(ConstantMethod.BLE_NAME).build();

        KnBle.getInstance().setScanFilter(filters);
        KnBle.getInstance().setScanSettings(settings);
        KnBle.DEBUG = true;

    }

    /*
    method for initialise all components
     */
    private void initView() {
        imgOption = findViewById(R.id.imgOption);

        imgLocation = findViewById(R.id.imgLocation);
        txtAlarmSound = findViewById(R.id.txtTestAlarm);
        imgArm = findViewById(R.id.imgArm);
        imgBle = findViewById(R.id.imgBLE);
        linearArm = findViewById(R.id.linearArm);
        linearDisArm = findViewById(R.id.linearDisArm);
        txtTestSMS = findViewById(R.id.txtTestSMS);
        txtSound = findViewById(R.id.txtSound);
        txtShow = findViewById(R.id.txtShow);
        buttonBusy = findViewById(R.id.btnBusy);
        btnRemote = findViewById(R.id.btnRemote);
        btnCustom = findViewById(R.id.btnCustom);
        imgBluetooth = findViewById(R.id.imgBluetooth);
        imgSecure = findViewById(R.id.imgSecurity);
        txtLog = findViewById(R.id.txtLogs);
        indicatorSeekBarAlarm = findViewById(R.id.seekbarAlarm);
        indicatorSeekBarDelay = findViewById(R.id.seekBarDelay);
        indicatorSeekBarWarning = findViewById(R.id.seekBarWarn);
        indicator_location = findViewById(R.id.indicator_location);
        locationInterval = indicator_location.getProgress();

        imgOption.setOnClickListener(MainActivity.this);
        txtSound.setOnClickListener(MainActivity.this);
        txtTestSMS.setOnClickListener(MainActivity.this);
        linearArm.setOnClickListener(MainActivity.this);
        linearDisArm.setOnClickListener(MainActivity.this);
        buttonBusy.setOnClickListener(MainActivity.this);
        btnRemote.setOnClickListener(MainActivity.this);
        btnCustom.setOnClickListener(MainActivity.this);
        imgLocation.setOnClickListener(MainActivity.this);
        imgBluetooth.setOnClickListener(MainActivity.this);
        imgSecure.setOnClickListener(MainActivity.this);
        txtSound.setOnClickListener(MainActivity.this);
        txtAlarmSound.setOnClickListener(MainActivity.this);
        imgBle.setOnClickListener(MainActivity.this);
        txtShow.setOnClickListener(this);
    }


    /*
    method for setup progressbar values base on button click busy,remote,custom
     */
    private void setupData() {

        setupLocation();
        setupLaunchData();
        enableArm();

    }

    private void setupLaunchData() {
        try {
            /*
            here check app is open first time or not.
            according that setup values of progressbar
             */
            if (!PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_FIRST_LAUNCH)) {
                ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, btnCustom);
                ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, btnRemote);
                ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background, buttonBusy);
                indicatorSeekBarWarning.setProgress(100);
                indicatorSeekBarAlarm.setProgress(100);
                indicatorSeekBarDelay.setProgress(20);

                indicatorSeekBarAlarm.setEnabled(false);
                indicatorSeekBarWarning.setEnabled(false);
                indicatorSeekBarDelay.setEnabled(false);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_BUSY, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_REMOTE) ||
                PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_BUSY) ||
                PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_CUSTOM)) {
            disableSlider();
            if (PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_BUSY)) {
                settingBusy();

            } else if (PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_REMOTE)) {
                settingRemote();

            } else if (PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_CUSTOM)) {
                settingCustom();
            }
        }

    }

    private void enableArm() {
        if (PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_ARM_ENABLE)) {
            setImageBackground(MainActivity.this, R.drawable.red_lock_closed, imgArm);
        } else {
            setImageBackground(MainActivity.this, R.drawable.white_lock_open, imgArm);
        }
    }

    private void setupLocation() {
        if (!PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_DEFAULT_LOCATION)) {
            PowerPreference.getDefaultFile().putInt(ConstantMethod.PREF_LOCATION_INTERVAL, 25);
            indicator_location.setProgress(PowerPreference.getDefaultFile().getInt(ConstantMethod.PREF_LOCATION_INTERVAL));
        } else {
            indicator_location.setProgress(PowerPreference.getDefaultFile().getInt(ConstantMethod.PREF_LOCATION_INTERVAL));
        }
        indicator_location.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                PowerPreference.getDefaultFile().putInt(ConstantMethod.PREF_LOCATION_INTERVAL, indicator_location.getProgress());
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_DEFAULT_LOCATION, true);
                indicator_location.setProgress(PowerPreference.getDefaultFile().getInt(ConstantMethod.PREF_LOCATION_INTERVAL));
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
    }

    /*
    method for custom settings
     */
    private void settingCustom() {
        indicatorSeekBarWarning.setProgress(PowerPreference.getDefaultFile().getInt(ConstantMethod.PREF_WARN));
        indicatorSeekBarAlarm.setProgress(PowerPreference.getDefaultFile().getInt(ConstantMethod.PREF_ALARM));
        indicatorSeekBarDelay.setProgress(PowerPreference.getDefaultFile().getInt(ConstantMethod.PREF_DELAY));
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background, btnCustom);
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, buttonBusy);
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, btnRemote);
    }

    /*
    method for remote settings
     */
    private void settingRemote() {
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, btnCustom);
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, buttonBusy);
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background, btnRemote);
        indicatorSeekBarWarning.setProgress(75);
        indicatorSeekBarAlarm.setProgress(50);
        indicatorSeekBarDelay.setProgress(5);
    }

    /*
    method for busy settings
     */
    private void settingBusy() {
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, btnCustom);
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, btnRemote);
        ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background, buttonBusy);
        indicatorSeekBarWarning.setProgress(100);
        indicatorSeekBarAlarm.setProgress(100);
        indicatorSeekBarDelay.setProgress(20);
    }


    /*
    method for disable progress slider
     */
    private void disableSlider() {
        indicatorSeekBarAlarm.setEnabled(false);
        indicatorSeekBarWarning.setEnabled(false);
        indicatorSeekBarDelay.setEnabled(false);
    }

    /*
    method for bottom sheet option menu of register,settings and instructions
     */
    private void setupSettingOption() {
        bottomSheetDialogOption = new BottomSheetDialog(MainActivity.this, R.style.CustomBottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.item_option, null);
        ImageView imgCancel = sheetView.findViewById(R.id.imgCancel);
        TextView txtSetting = sheetView.findViewById(R.id.txtSetting);
        TextView textReg = sheetView.findViewById(R.id.txtReg);
        TextView txtLog = sheetView.findViewById(R.id.txtLog);
        TextView txtInstruction = sheetView.findViewById(R.id.txtInstruction);
        imgCancel.setOnClickListener(v -> bottomSheetDialogOption.cancel());
        txtSetting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
            bottomSheetDialogOption.cancel();
        });
        textReg.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterProfile.class);
            startActivity(intent);
            bottomSheetDialogOption.cancel();
        });
        txtLog.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SmsLogActivity.class);
            startActivity(intent);
            bottomSheetDialogOption.cancel();
        });
        txtInstruction.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Instruction", Toast.LENGTH_SHORT).show();
            bottomSheetDialogOption.cancel();
        });
        bottomSheetDialogOption.setContentView(sheetView);
        bottomSheetDialogOption.setCancelable(true);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.imgOption: {
                bottomSheetDialogOption.show();
            }
            break;
            case R.id.imgLocation: {

            }
            case R.id.txtShow: {
                Toast.makeText(MainActivity.this, "Show Location", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.imgBLE: {
                BleDevice device = KnBle.getInstance().getBleDeviceFromMac(PowerPreference.getDefaultFile().getString(ConstantMethod.BLE_ADDRESS));
                if (device != null) {
                    boolean connected = KnBle.getInstance().isConnected(device);
                    if(connected) {
                        disconnectBLE();
                    }else {
                        boolean isScanning = KnBle.getInstance().isScanning();
                        if(!isScanning){
                            connectBLE();
                        }else {
                            Toast.makeText(MainActivity.this, getString(R.string.lbl_scan_wait), Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    boolean isScanning = KnBle.getInstance().isScanning();
                    if(!isScanning){
                        connectBLE();
                    }else {
                        Toast.makeText(MainActivity.this,  getString(R.string.lbl_scan_wait), Toast.LENGTH_SHORT).show();
                    }
                }


            }
            break;
            case R.id.linearArm: {
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_ARM_ENABLE, true);
                setImageBackground(MainActivity.this, R.drawable.red_lock_closed, imgArm);
                Toast.makeText(MainActivity.this, R.string.lbl_arm_sucess, Toast.LENGTH_SHORT).show();

            }
            break;
            case R.id.linearDisArm: {
                Toast.makeText(MainActivity.this, R.string.lbl_disarm_sucess, Toast.LENGTH_SHORT).show();
                setImageBackground(MainActivity.this, R.drawable.white_lock_open, imgArm);
                imgArm.setRotationY(180f);
                PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_ARM_ENABLE, false);

            }
            break;

            case R.id.btnBusy: {
                Toast.makeText(MainActivity.this, "Busy", Toast.LENGTH_SHORT).show();
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_BUSY, true);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_REMOTE, false);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_CUSTOM, false);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_FIRST_LAUNCH, true);
                setupData();
            }
            break;
            case R.id.btnCustom: {
                Toast.makeText(MainActivity.this, "Custom", Toast.LENGTH_SHORT).show();
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_CUSTOM, true);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_BUSY, false);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_REMOTE, false);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_FIRST_LAUNCH, true);
                freeUpSlider();
            }
            break;
            case R.id.btnRemote: {
                Toast.makeText(MainActivity.this, "Remote", Toast.LENGTH_SHORT).show();
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_REMOTE, true);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_BUSY, false);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_CUSTOM, false);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_FIRST_LAUNCH, true);
                setupData();
            }
            break;
            case R.id.txtSound: {
                Toast.makeText(MainActivity.this, "Test Sound", Toast.LENGTH_SHORT).show();

            }
            break;
            case R.id.txtTestAlarm: {
                Toast.makeText(MainActivity.this, "Test Sound", Toast.LENGTH_SHORT).show();

            }
            break;
            case R.id.txtTestSMS: {

                if (!ConstantMethod.isEmpty(mobileNumber)) {
                    Toast.makeText(MainActivity.this, "Test SMS", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Phone number not found", Toast.LENGTH_SHORT).show();

                }
            }
            break;
            case R.id.imgBluetooth: {
                Toast.makeText(MainActivity.this, "Test Bluetooth", Toast.LENGTH_SHORT).show();
            }
            break;
            case R.id.imgSecurity: {
                Toast.makeText(MainActivity.this, "Policy", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private void disconnectBLE() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Disconnect BLE")
                .setMessage(R.string.alert_disconnect)
                .setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PowerPreference.getDefaultFile().setBoolean(ConstantMethod.BLE_ENABLE, false);
                        KnBle.getInstance().disconnectAll();
                        KnBle.getInstance().destroyAllDevices();

                    }
                })

                .setNegativeButton(R.string.lbl_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);

    }


    private void connectBLE() {
        boolean isNotificationServiceRunning = isNotificationServiceRunning();

        if (!isNotificationServiceRunning) {
            enableNotificationAccessDialog();

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    enableREDAlert();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    Dexter.withContext(MainActivity.this)
                            .withPermissions(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    if (report.areAllPermissionsGranted()) {

                                        if (!isGpsOn()) {
                                            gpsAlertDialog();

                                        } else if (!KnBle.getInstance().isBluetoothEnabled()) {
                                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                        } else {
                                            //BLE Scanning process
                                            if (!KnBle.getInstance().isScanning()) {
                                                Toast.makeText(MainActivity.this, R.string.lbl_scan, Toast.LENGTH_SHORT).show();
                                                startScan();
                                            }
                                        }
                                    } else {
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
            } else {
                if (!isGpsOn()) {
                    gpsAlertDialog();
                } else if (!KnBle.getInstance().isBluetoothEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                } else {
                    if (!KnBle.getInstance().isScanning()) {
                        Toast.makeText(this, R.string.lbl_scan, Toast.LENGTH_LONG).show();
                        startScan();
                    }
                }
            }
        }
    }

    private void enableREDAlert() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Permission Required")
                .setMessage("Allow Bike Tracker to appear on top of other apps.")
                .setNegativeButton(getString(R.string.lbl_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.finish();
                            }
                        })
                .setPositiveButton("Allow",
                        (dialog, which) -> {
                            Intent intent = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + MainActivity.this.getPackageName()));
                            }
                            MainActivity.this.startActivityForResult(intent, REQUEST_CODE);
                        })

                .setCancelable(false)
                .show();
    }

    private void enableNotificationAccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable Notification Access").setTitle("Enable permissions");
        builder.setMessage("For checking log of your bike tracker please enable notification access of bike tracker log")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                        }
                        Toast.makeText(MainActivity.this, "Enable Bike Tracker Log", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Enable Notification Access");
        alert.show();
    }

    private void gpsAlertDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.lbl_gpson)
                .setMessage(R.string.lbl_gps_alrt)
                .setNegativeButton(R.string.lbl_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                .setPositiveButton(R.string.lbl_setting,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                            }
                        })

                .setCancelable(false)
                .show();
    }




    @Override
    public void onUserInteraction() {

        super.onUserInteraction();

        if (PowerPreference.getDefaultFile().getBoolean(ConstantMethod.PREF_CUSTOM)) {
            stopHandler();
            startHandler();
        }

    }

    public void stopHandler() {
        if (handler != null) {
            handler.removeCallbacks(r);
        }

    }

    public void startHandler() {
        if (handler != null) {

            handler.postDelayed(r, 10000);
        }

    }


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        KnBle.getInstance().destroyAllDevices();
        if (handler != null) {
            handler.removeCallbacks(r);
        }
        super.onDestroy();

    }


    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setImageBackground(final Context mContext, int picture, final ImageView imageView) {
        if (mContext != null && imageView != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(mContext.getResources().getDrawable(picture, mContext.getApplicationContext().getTheme()));

                } else {
                    imageView.setImageDrawable(mContext.getResources().getDrawable(picture));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();

            return;
        }

        MainActivity.this.doubleBackToExitPressedOnce = true;
        Toast.makeText(MainActivity.this, "Press again to exit.", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }


    /*
     method for if user denied permission
    */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage(R.string.lbl_location_permission);
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(R.string.lbl_cancel, (dialog, which) -> dialog.cancel());
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /*
  method for save persist data
   */
    private void fillData() {
        try {
            Gson gson = new Gson();
            String json = PowerPreference.getDefaultFile().getString(ConstantMethod.PREF_SETTING);
            Type type = new TypeToken<List<SettingObject>>() {
            }.getType();
            List<SettingObject> settingObjects = gson.fromJson(json, type);
            if (settingObjects != null) {
                for (int i = 0; i < settingObjects.size(); i++) {
                    BluetoothPIN = (ConstantMethod.validateString(settingObjects.get(i).getBluetoothPin().trim()));
                    mobileNumber = (ConstantMethod.validateString(settingObjects.get(i).getPh_no().trim()));
                    TrackerNumber = (ConstantMethod.validateString(settingObjects.get(i).getTrackerNo().trim()));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothAdapter adapter = KnBle.getInstance().getBluetoothAdapter();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        for(BluetoothDevice d: pairedDevices) {
            if (d.getAddress().equals(PowerPreference.getDefaultFile().getString(ConstantMethod.BLE_ADDRESS))) {
                startScan();
            }
        }
        fillData();
    }


    private void checkNotifPermission() {
        boolean isNotificationServiceRunning = isNotificationServiceRunning();
        if (!isNotificationServiceRunning) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enable Notification Access").setTitle("Enable permissions");
            builder.setMessage(R.string.lbl_sms_permission)
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            }
                            Toast.makeText(MainActivity.this, "Enable Bike Tracker Log", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alert = builder.create();
            alert.setTitle("Enable Notification Access");
            alert.show();
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


    // Here BLE Scanning process start
    private void startScan() {

        KnBle.getInstance().startScan(new BleScanCallback() {
            @Override
            public void onScanStarted() {

            }

            @Override
            public void onScanFailed(int error) {
                switch (error) {
                    case BleScanCallback.BT_DISABLED:
                        Toast.makeText(MainActivity.this, R.string.lbl_enable_bluetooth, Toast.LENGTH_SHORT).show();

                        break;
                    case BleScanCallback.LOCATION_DISABLED:


                        Toast.makeText(MainActivity.this, R.string.lbl_gps_alrt, Toast.LENGTH_SHORT).show();
                        break;
                    case BleScanCallback.SCANNER_UNAVAILABLE:


                        Toast.makeText(MainActivity.this, R.string.lbl_ble_not_found, Toast.LENGTH_SHORT).show();
                        break;
                    case BleScanCallback.UNKNOWN_ERROR:


                        Toast.makeText(MainActivity.this, R.string.lbl_something_wrong, Toast.LENGTH_SHORT).show();
                        break;
                    case BleScanCallback.SCAN_FEATURE_UNSUPPORTED:

                        Toast.makeText(MainActivity.this, R.string.lbl_ble_scan_not_support, Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onScanResult(@NonNull BleDevice bleDevice) {
                final String deviceName = bleDevice.getDevice().getName();
                if (deviceName != null && deviceName.length() > 0) {
                    // Here we again verify BLE Name
                    if (deviceName.equals(ConstantMethod.BLE_NAME)) {

                        imgBluetooth.setImageResource(R.drawable.bluetooth_symbol_grey_3);
                        imgBle.setImageResource(R.drawable.bluetooth_symbol_grey_3);

                        // Here BLE  Found and start connection process

                        Toast.makeText(MainActivity.this, R.string.lbl_found, Toast.LENGTH_SHORT).show();
                        BluetoothAdapter adapter = KnBle.getInstance().getBluetoothAdapter();
                        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                        
                        // Here we check device already paired or not. if its paired it auto connect 
                        for(BluetoothDevice d: pairedDevices) {
                            if (d.getAddress().equals(PowerPreference.getDefaultFile().getString(ConstantMethod.BLE_ADDRESS))) {
                                KnBle.getInstance().connect(bleDevice, true, new BleGattCallback() {
                                    @Override
                                    public void onConnecting() {

                                        Toast.makeText(MainActivity.this, R.string.lbl_connecting, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onConnectFailed() {


                                        updateConnectionStateUi(2);
                                    }

                                    @Override
                                    public void onConnectSuccess(List<BluetoothGattService> services) {

                                        PowerPreference.getDefaultFile().putString(ConstantMethod.BLE_ADDRESS, bleDevice.getDevice().getAddress());

                                        updateConnectionStateUi(1);
                                    }

                                    @Override
                                    public void onDisconnected() {


                                        updateConnectionStateUi(0);
                                    }
                                });
                            }else {
                                KnBle.getInstance().connect(bleDevice, false, new BleGattCallback() {
                                    @Override
                                    public void onConnecting() {

                                        Toast.makeText(MainActivity.this, R.string.lbl_connecting, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onConnectFailed() {


                                        updateConnectionStateUi(2);
                                    }

                                    @Override
                                    public void onConnectSuccess(List<BluetoothGattService> services) {
                                        PowerPreference.getDefaultFile().putString(ConstantMethod.BLE_ADDRESS, bleDevice.getDevice().getAddress());

                                        updateConnectionStateUi(1);
                                    }

                                    @Override
                                    public void onDisconnected() {


                                        updateConnectionStateUi(0);
                                    }
                                });
                            }
                        }

                      


                    } else {
                        // Here BLE Not Found
                        Toast.makeText(MainActivity.this, R.string.lbl_notfound, Toast.LENGTH_SHORT).show();
                        KnBle.getInstance().stopScan();
                    }
                } else {
                    // Here BLE Not Found
                    Toast.makeText(MainActivity.this, R.string.lbl_notfound, Toast.LENGTH_SHORT).show();
                    KnBle.getInstance().stopScan();
                }
            }

            @Override
            public void onScanFinished(@NonNull HashMap<String, BleDevice> scanResult) {
                Log.d("dvv", "onScanFinished: "+scanResult);
            }


        });
    }


    private void updateConnectionStateUi(int flag) {

        if (flag == 1) {
            Log.d("dvvvv", "updateConnectionStateUi:1 ");
            imgBluetooth.setImageResource(R.drawable.bluetooth_symbol);
            imgBle.setImageResource(R.drawable.bluetooth_symbol);
            Toast.makeText(MainActivity.this, getString(R.string.lbl_connected), Toast.LENGTH_LONG).show();
            KnBle.getInstance().stopScan();


        } else if (flag == 0) {
            Log.d("dvvvv", "updateConnectionStateUi:0 ");
            imgBluetooth.setImageResource(R.drawable.bluetooth_symbol_grey_1);
            imgBle.setImageResource(R.drawable.bluetooth_symbol_grey_1);

            Toast.makeText(MainActivity.this, getString(R.string.lbl_disconnect), Toast.LENGTH_LONG).show();
            PowerPreference.getDefaultFile().setString(ConstantMethod.BLE_ADDRESS, null);
        } else if (flag == 2) {
            Log.d("dvvvv", "updateConnectionStateUi:2 ");
            imgBluetooth.setImageResource(R.drawable.bluetooth_symbol_grey_1);
            imgBle.setImageResource(R.drawable.bluetooth_symbol_grey_1);
            Toast.makeText(MainActivity.this, R.string.lbl_failed, Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            // ** if so check once again if we have permission */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, R.string.lbl_cancel, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_ENABLE_BT1 && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, R.string.lbl_on_sucess, Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, R.string.lbl_on_sucess, Toast.LENGTH_SHORT).show();
            if (isGpsOn()) {
                if (!KnBle.getInstance().isBluetoothEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    setImageBackground(MainActivity.this, R.drawable.green_lock_with_border_closed, imgArm);
                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_ARM_ENABLE, true);
                    if (!KnBle.getInstance().isScanning()) {
                        Toast.makeText(MainActivity.this, R.string.lbl_scan, Toast.LENGTH_LONG).show();
                        startScan();
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.lbl_enable_gps, Toast.LENGTH_SHORT).show();

            }

        } else if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (isGpsOn()) {
                if (!KnBle.getInstance().isBluetoothEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    setImageBackground(MainActivity.this, R.drawable.green_lock_with_border_closed, imgArm);
                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_ARM_ENABLE, true);
                    if (!KnBle.getInstance().isScanning()) {
                        Toast.makeText(this, R.string.lbl_scan, Toast.LENGTH_LONG).show();
                        startScan();
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.lbl_enable_gps, Toast.LENGTH_SHORT).show();

            }

        }
    }

    private boolean isGpsOn() {
        LocationManager locationManager
                = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String flags = intent.getStringExtra(ConstantMethod.LOG_FLAG);
            if (flags != null) {
                if (flags.equals(ConstantMethod.LOG_FLAG_MESSAGE)) {
                    txtLog.setText(PowerPreference.getDefaultFile().getString(ConstantMethod.LOG_FLAG));
                }
            }
        }
    };
    /*
   method for 10 sec custom setting slider enable
    */
    private void freeUpSlider() {
        try {
            indicatorSeekBarDelay.setEnabled(true);
            indicatorSeekBarWarning.setEnabled(true);
            indicatorSeekBarAlarm.setEnabled(true);
            ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background, btnCustom);
            ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, btnRemote);
            ConstantMethod.setImageViewBackground(MainActivity.this, R.drawable.gradient_background_disable, buttonBusy);
            PowerPreference.getDefaultFile().putInt(ConstantMethod.PREF_WARN, indicatorSeekBarWarning.getProgress());
            PowerPreference.getDefaultFile().putInt(ConstantMethod.PREF_ALARM, indicatorSeekBarAlarm.getProgress());
            PowerPreference.getDefaultFile().putInt(ConstantMethod.PREF_DELAY, indicatorSeekBarDelay.getProgress());

            manageScroll();

            handler = new Handler(Looper.getMainLooper());
            r = new Runnable() {
                @Override
                public void run() {
                    indicatorSeekBarDelay.setEnabled(false);
                    indicatorSeekBarWarning.setEnabled(false);
                    indicatorSeekBarAlarm.setEnabled(false);
                    PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_CUSTOM, false);
                }
            };
            startHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void manageScroll() {
        indicatorSeekBarWarning.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                PowerPreference.getDefaultFile().putInt(ConstantMethod.PREF_WARN, indicatorSeekBarWarning.getProgress());
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
        indicatorSeekBarAlarm.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                PowerPreference.getDefaultFile().putInt(ConstantMethod.PREF_ALARM, indicatorSeekBarAlarm.getProgress());
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
        indicatorSeekBarDelay.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                PowerPreference.getDefaultFile().putInt(ConstantMethod.PREF_DELAY, indicatorSeekBarDelay.getProgress());
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
    }

}