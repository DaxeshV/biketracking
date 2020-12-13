package com.app.biketracker.activity.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.biketracker.R;
import com.app.biketracker.activity.smslogHelper.NotificationListener;
import com.app.biketracker.activity.utils.ConstantMethod;
import com.app.biketracker.activity.utils.SettingObject;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
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
import java.util.List;
import java.util.Set;


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
        BleManager.getInstance().init(getApplication());

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


        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(2)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setAutoConnect(false)

                .setDeviceName(true, ConstantMethod.BLE_NAME)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
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
                if (BleManager.getInstance().isSupportBle()) {
                    boolean connected = BleManager.getInstance().isConnected(PowerPreference.getDefaultFile().getString(ConstantMethod.BLE_ADDRESS));
                    if (connected) {
                        disconnectBLE();
                    } else {
                        connectBLE();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.lbl_notsupport), Toast.LENGTH_SHORT).show();
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
                .setTitle(R.string.lbl_discoonect_ble)
                .setMessage(R.string.alert_disconnect)
                .setPositiveButton(R.string.lbl_yes, (dialogInterface, i) -> {
                    BleManager.getInstance().disconnectAllDevice();
                })

                .setNegativeButton(R.string.lbl_cancel, (dialogInterface, i) -> {

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

                                        } else if (!BleManager.getInstance().isBlueEnable()) {
                                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                                        } else {
                                            //BLE Scanning process
                                            startScan();
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
                } else if (!BleManager.getInstance().isBlueEnable()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                } else {
                    startScan();
                }
            }
        }
    }

    private void enableREDAlert() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.lbl_alert_permission))
                .setMessage(R.string.lbl_window_permission)
                .setNegativeButton(getString(R.string.lbl_cancel),
                        (dialog, which) -> MainActivity.this.finish())
                .setPositiveButton(R.string.lbl_allow,
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
        builder.setMessage(getString(R.string.lbl_Notification)).setTitle(R.string.lbl_enable_permission);
        builder.setMessage(getString(R.string.lbl_noti_msg))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.enable), (dialog, id) -> {
                    dialog.cancel();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                    Toast.makeText(MainActivity.this,getString(R.string.lbl_enable_bike_log), Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(getString(R.string.lbl_cancel), (dialog, id) -> {

                });
        AlertDialog alert = builder.create();
        alert.setTitle(getString(R.string.enable_noti_access));
        alert.show();
    }

    private void gpsAlertDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.lbl_gpson)
                .setMessage(R.string.lbl_gps_alrt)
                .setNegativeButton(R.string.lbl_cancel,
                        (dialog, which) -> finish())
                .setPositiveButton(R.string.lbl_setting,
                        (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
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
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().disconnectAllDevice();

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
        builder.setTitle(R.string.lbl_alert_permission);
        builder.setMessage(R.string.lbl_location_permission);
        builder.setPositiveButton(R.string.lbl_go_setting, (dialog, which) -> {
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
        BluetoothAdapter adapter = BleManager.getInstance().getBluetoothAdapter();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice d : pairedDevices) {
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
            builder.setMessage(getString(R.string.enable_noti_access)).setTitle(getString(R.string.lbl_enable_permission));
            builder.setMessage(R.string.lbl_sms_permission)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.enable), (dialog, id) -> {
                        dialog.cancel();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                        }

                    })
                    .setNegativeButton(getString(R.string.lbl_cancel), (dialog, id) -> {

                    });
            AlertDialog alert = builder.create();
            alert.setTitle(getString(R.string.enable_noti_access));
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

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                final String deviceName = bleDevice.getDevice().getName();
                if (deviceName != null && deviceName.length() > 0) {
                    // Here we again verify BLE Name
                    if (deviceName.equals(ConstantMethod.BLE_NAME)) {

                        imgBluetooth.setImageResource(R.drawable.bluetooth_symbol_grey_3);
                        imgBle.setImageResource(R.drawable.bluetooth_symbol_grey_3);

                        // Here BLE  Found and start connection process

                        Toast.makeText(MainActivity.this, R.string.lbl_found, Toast.LENGTH_SHORT).show();


                        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
                            @Override
                            public void onStartConnect() {
                                Toast.makeText(MainActivity.this, R.string.lbl_connecting, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                                updateConnectionStateUi(2);
                            }

                            @Override
                            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                                PowerPreference.getDefaultFile().putString(ConstantMethod.BLE_ADDRESS, bleDevice.getDevice().getAddress());

                                updateConnectionStateUi(1);
                            }

                            @Override
                            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                                updateConnectionStateUi(0);
                            }

                        });

                    } else {
                        // Here BLE Not Found
                        Toast.makeText(MainActivity.this, R.string.lbl_notfound, Toast.LENGTH_SHORT).show();
                        BleManager.getInstance().cancelScan();
                    }
                } else {
                    // Here BLE Not Found
                    Toast.makeText(MainActivity.this, R.string.lbl_notfound, Toast.LENGTH_SHORT).show();
                    BleManager.getInstance().cancelScan();
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {

            }

        });
    }


    private void updateConnectionStateUi(int flag) {

        if (flag == 1) {

            imgBluetooth.setImageResource(R.drawable.bluetooth_symbol);
            imgBle.setImageResource(R.drawable.bluetooth_symbol);
            Toast.makeText(MainActivity.this, getString(R.string.lbl_connected), Toast.LENGTH_LONG).show();
            BleManager.getInstance().cancelScan();


        } else if (flag == 0) {

            imgBluetooth.setImageResource(R.drawable.bluetooth_symbol_grey_1);
            imgBle.setImageResource(R.drawable.bluetooth_symbol_grey_1);

            Toast.makeText(MainActivity.this, getString(R.string.lbl_disconnect), Toast.LENGTH_LONG).show();

        } else if (flag == 2) {

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
                if (!BleManager.getInstance().isBlueEnable()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    setImageBackground(MainActivity.this, R.drawable.green_lock_with_border_closed, imgArm);
                    PowerPreference.getDefaultFile().setBoolean(ConstantMethod.IS_ARM_ENABLE, true);
                    startScan();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.lbl_enable_gps, Toast.LENGTH_SHORT).show();

            }

        } else if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (isGpsOn()) {
                if (!BleManager.getInstance().isBlueEnable()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    setImageBackground(MainActivity.this, R.drawable.green_lock_with_border_closed, imgArm);
                    startScan();
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
            r = () -> {
                indicatorSeekBarDelay.setEnabled(false);
                indicatorSeekBarWarning.setEnabled(false);
                indicatorSeekBarAlarm.setEnabled(false);
                PowerPreference.getDefaultFile().putBoolean(ConstantMethod.PREF_CUSTOM, false);
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