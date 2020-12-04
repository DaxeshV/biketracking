package com.app.biketracker.activity.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Button;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ConstantMethod {

    public static String PREF_REG="PREF_REG";
    public static String PREF_SETTING="PREF_SETTING";
    public static String PREF_BUSY="PREF_BUSY";
    public static String PREF_REMOTE="PREF_REMOTE";
    public static String PREF_CUSTOM="PREF_CUSTOM";
    public static String PREF_FIRST_LAUNCH="PREF_FIRST_LAUNCH";
    public static String PREF_DEFAULT_LOCATION ="PREF_DEFAULT_LOC";
    public static String PREF_DELAY="PREF_DELAY";
    public static String PREF_WARN="PREF_WARN";
    public static String PREF_ALARM="PREF_ALARM";
    public static String IS_ARM_ENABLE="IS_ARM_ENABLE";
    public static String PREF_LOCATION_INTERVAL="PREF_LOCATION_INTERVAL";
    public static String TRACKER_NO="TRACKER_NO";
    public static String BLE_ADDRESS="BLE_ADDRESS";
    public static String IS_BLE="IS_BLE";
    public static String IS_LOCATION="IS_LOCATION";
    public static String IS_READER="IS_READER";
    public static String IS_DRAW_OTHER="IS_DRAW_OTHER";
    public static String IS_GPS="IS_GPS";
    public static String GetData="GetData";

    public static String BLE_ENABLE="BLE_ENABLE";
    public static String LOG_FLAG="LOG_FLAG";
    public static String LOG_FLAG_MESSAGE="LOG_FLAG_MESSAGE";



    // BLE Name Define here
    public static String BLE_NAME="MyTracker";

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setImageViewBackground(final Context mContext, int picture, final Button imageView)
    {
        if (mContext != null && imageView != null)
        {
            try
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    imageView.setBackground(mContext.getResources().getDrawable(picture, mContext.getApplicationContext().getTheme()));

                } else
                {
                    imageView.setBackground(mContext.getResources().getDrawable(picture));

                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String validateString(String value) {
        if (value == null)
            return "";
        else if (value.equalsIgnoreCase("null"))
            return "";
        else if (value.equalsIgnoreCase(""))
            return "";
        else
            return value;
    }






    public static String getAppNameFromPackage(Context context, String packageName, boolean returnNull) {
        final PackageManager pm = context.getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        if (returnNull) {
            return ai == null ? null : pm.getApplicationLabel(ai).toString();
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : packageName);
    }


    public static String nullToEmptyString(CharSequence charsequence) {
        if (charsequence == null) {
            return "";
        } else {
            return charsequence.toString();
        }
    }

    public static String getTime(Long currentTimeMillis){
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date resultdate = new Date(currentTimeMillis);
        return df.format(resultdate);
    }

    public static String getDate(Long currentTimeMillis){
        DateFormat df = new SimpleDateFormat("d MMM yyyy");
        Date resultdate = new Date(currentTimeMillis);
        return df.format(resultdate);
    }



}
