package com.app.biketracker.activity.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.biketracker.R;
import com.app.biketracker.activity.utils.ConstantMethod;
import com.app.biketracker.activity.utils.SettingObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.preference.PowerPreference;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnSave;
    ImageView imgBack;
    String bikeName, phoneNo, bluetoothPin,trackerNo;
    EditText editTextBikeName, editTextPhoneNo, editTextBluetoothPin,editTextTrackerNo;
    ArrayList<SettingObject> settingObjectArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        imgBack = findViewById(R.id.imgBack);
        btnSave = findViewById(R.id.btnSave);
        editTextBikeName = findViewById(R.id.edittextBikeName);
        editTextPhoneNo = findViewById(R.id.editTextPhoneNo);
        editTextBluetoothPin = findViewById(R.id.editTextBluetoothPin);
        editTextTrackerNo = findViewById(R.id.editTextTrackerNo);

        imgBack.setOnClickListener(SettingActivity.this);
        btnSave.setOnClickListener(SettingActivity.this);
        fillData();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.imgBack: {
                onBackPressed();
            }
            break;

            case R.id.btnSave: {
                PowerPreference.getDefaultFile().putString(ConstantMethod.TRACKER_NO,editTextTrackerNo.getText().toString());
                setupData();
            }
            break;
        }
    }

    /*
    method for setup save data
     */
    private void setupData() {
        try {
            bikeName = editTextBikeName.getText().toString().trim();
            phoneNo = editTextPhoneNo.getText().toString().trim();
            bluetoothPin = editTextBluetoothPin.getText().toString().trim();
            trackerNo = editTextTrackerNo.getText().toString().trim();

            settingObjectArrayList.clear();
            settingObjectArrayList.add(new SettingObject(bikeName, phoneNo, bluetoothPin,trackerNo));
            Gson gson = new Gson();
            String json = gson.toJson(settingObjectArrayList);
            PowerPreference.getDefaultFile().putString(ConstantMethod.PREF_SETTING, json);
            Toast.makeText(this, "Data Saved Successfully!", Toast.LENGTH_SHORT).show();
            SettingActivity.this.finish();
        } catch (Exception e) {
            Toast.makeText(this, "Try again! Something goes wrong", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }



    }

    /*
    method for save persist data
     */
    private void fillData() {
        Gson gson = new Gson();
        String json = PowerPreference.getDefaultFile().getString(ConstantMethod.PREF_SETTING);
        Type type = new TypeToken<List<SettingObject>>() {
        }.getType();
        List<SettingObject> settingObjects = gson.fromJson(json, type);
        if (settingObjects != null) {
            for (int i = 0; i < settingObjects.size(); i++) {
                editTextBluetoothPin.setText(ConstantMethod.validateString(settingObjects.get(i).getBluetoothPin().trim()));
                editTextPhoneNo.setText(ConstantMethod.validateString(settingObjects.get(i).getPh_no().trim()));
                editTextBikeName.setText(ConstantMethod.validateString(settingObjects.get(i).getBikeName().trim()));
                editTextTrackerNo.setText(ConstantMethod.validateString(settingObjects.get(i).getTrackerNo().trim()));

            }
        }
    }
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

}