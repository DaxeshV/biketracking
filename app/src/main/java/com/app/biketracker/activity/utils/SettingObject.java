package com.app.biketracker.activity.utils;

public class SettingObject {
    private String bikeName;
    private String ph_no;
    private String bluetoothPin;
    private String TrackerNo;

    public SettingObject(String bikeName, String ph_no, String bluetoothPin,String TrackerNo) {
        this.bikeName = bikeName;
        this.ph_no = ph_no;
        this.bluetoothPin = bluetoothPin;
        this.TrackerNo = TrackerNo;
    }

    public String getBikeName() {
        return bikeName;
    }

    public void setBikeName(String bikeName) {
        this.bikeName = bikeName;
    }

    public String getPh_no() {
        return ph_no;
    }

    public void setPh_no(String ph_no) {
        this.ph_no = ph_no;
    }

    public String getBluetoothPin() {
        return bluetoothPin;
    }

    public String getTrackerNo() {
        return TrackerNo;
    }

    public void setTrackerNo(String trackerNo) {
        TrackerNo = trackerNo;
    }

    public void setBluetoothPin(String bluetoothPin) {
        this.bluetoothPin = bluetoothPin;
    }
}
