package com.app.biketracker.activity.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.biketracker.R;
import com.app.biketracker.activity.utils.ConstantMethod;
import com.app.biketracker.activity.utils.UserObject;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.preference.PowerPreference;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegisterProfile extends AppCompatActivity implements View.OnClickListener {
    EditText editBirthDate, editTextGender, editTextName, editTextAddress, editTextCountry,
            editTextCity, editTextState, editTextKeyNumber, editTextRetailer, editTextBike, editTextEmail;
    ImageView imgBack;
    int mYear, mMonth, mDay;
    BottomSheetDialog bottomSheetDialogGender;
    Button btnSave;
    String name, address, country, city, state, keyNumber, retailer, bike, gender, birthDate, email;
    ArrayList<UserObject> userObjectArrayList = new ArrayList<>();
    private RadioButton radioButtonMale,radioButtonFemale;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile);

        initView();
    }


    /*
    method for initialise components
     */
    private void initView() {
        editBirthDate = findViewById(R.id.editBirthDate);
        editTextGender = findViewById(R.id.editGender);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextBike = findViewById(R.id.editBike);
        editTextCountry = findViewById(R.id.editTextCountry);
        editTextCity = findViewById(R.id.editTextCity);
        editTextRetailer = findViewById(R.id.editRetailer);
        editTextKeyNumber = findViewById(R.id.editTextKeyNumber);
        editTextState = findViewById(R.id.editTextState);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);


        btnSave = findViewById(R.id.btnSave);
        imgBack = findViewById(R.id.imgBack);
        editBirthDate.setOnClickListener(this);
        editTextGender.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        setupGender();
        fillData();
    }

    /*
    method for setup persistence data
     */
    private void fillData() {
        Gson gson = new Gson();
        String json = PowerPreference.getDefaultFile().getString(ConstantMethod.PREF_REG);
        Type type = new TypeToken<List<UserObject>>() {
        }.getType();
        List<UserObject> userObject1 = gson.fromJson(json, type);
        if (userObject1 != null) {
            for (int i = 0; i < userObject1.size(); i++) {
                editTextEmail.setText(ConstantMethod.validateString(userObject1.get(i).getEmail().trim()));
                editTextState.setText(ConstantMethod.validateString(userObject1.get(i).getState().trim()));
                editTextCountry.setText(ConstantMethod.validateString(userObject1.get(i).getCountry().trim()));
                editTextCity.setText(ConstantMethod.validateString(userObject1.get(i).getCity().trim()));
                editTextState.setText(ConstantMethod.validateString(userObject1.get(i).getState().trim()));
                editTextGender.setText(ConstantMethod.validateString(userObject1.get(i).getGender().trim()));
                editBirthDate.setText(ConstantMethod.validateString(userObject1.get(i).getBirthDate().trim()));
                editTextKeyNumber.setText(ConstantMethod.validateString(userObject1.get(i).getKey_number().trim()));
                editTextRetailer.setText(ConstantMethod.validateString(userObject1.get(i).getRetailer().trim()));
                editTextBike.setText(ConstantMethod.validateString(userObject1.get(i).getBike().trim()));
                editTextName.setText(ConstantMethod.validateString(userObject1.get(i).getName().trim()));
                editTextAddress.setText(ConstantMethod.validateString(userObject1.get(i).getAddress().trim()));
                gender=editTextGender.getText().toString();
                if(gender.equals("Male")){
                    radioButtonMale.setChecked(true);
                }else {
                    radioButtonFemale.setChecked(true);
                }
            }
        }
    }

    /*
    method for save register data values in shared preference
     */
    private void saveData() {
        try {
            name = editTextName.getText().toString().trim();
            keyNumber = editTextKeyNumber.getText().toString().trim();
            retailer = editTextRetailer.getText().toString().trim();
            city = editTextCity.getText().toString().trim();
            birthDate = editBirthDate.getText().toString().trim();
            state = editTextState.getText().toString().trim();
            gender = editTextGender.getText().toString().trim();
            country = editTextCountry.getText().toString().trim();
            bike = editTextBike.getText().toString().trim();
            address = editTextAddress.getText().toString().trim();
            email = editTextEmail.getText().toString().trim();
            userObjectArrayList.clear();
            userObjectArrayList.add(new UserObject(name, address, country, state, city, email, keyNumber, retailer, bike, gender, birthDate));
            Gson gson = new Gson();
            String json = gson.toJson(userObjectArrayList);
            PowerPreference.getDefaultFile().putString(ConstantMethod.PREF_REG, json);

            Toast.makeText(this, "Data Saved Successfully!", Toast.LENGTH_SHORT).show();
            RegisterProfile.this.finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Try again! Something goes wrong", Toast.LENGTH_SHORT).show();
        }

    }

    private void setupGender() {
        bottomSheetDialogGender = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        @SuppressLint("InflateParams") View sheetView = getLayoutInflater().inflate(R.layout.item_gender, null);
        bottomSheetDialogGender.setContentView(sheetView);
        bottomSheetDialogGender.setCancelable(true);
        RadioGroup radioSexGroup = sheetView.findViewById(R.id.radioGroup);
        TextView textViewCancel = sheetView.findViewById(R.id.textViewCancel);
        TextView textViewDone = sheetView.findViewById(R.id.textViewDone);
        radioButtonMale=sheetView.findViewById(R.id.radioMale);
        radioButtonFemale=sheetView.findViewById(R.id.radioFemale);
        textViewCancel.setOnClickListener(view -> bottomSheetDialogGender.dismiss());
        textViewDone.setOnClickListener(v -> {
            RadioButton radioButton;
            int selectedId = radioSexGroup.getCheckedRadioButtonId();
            radioButton = radioSexGroup.findViewById(selectedId);
            editTextGender.setText(radioButton.getText().toString().trim());
            bottomSheetDialogGender.cancel();
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.editBirthDate: {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> editBirthDate.setText(String.format("%d-%d-%d", dayOfMonth, monthOfYear + 1, year)), mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
                datePickerDialog.show();
            }
            break;
            case R.id.editGender: {
                bottomSheetDialogGender.show();

            }
            break;
            case R.id.imgBack: {
                onBackPressed();

            }
            break;
            case R.id.btnSave: {
                Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show();
                saveData();
            }
            break;
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