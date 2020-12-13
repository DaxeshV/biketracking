package com.app.biketracker.activity.smslogHelper;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.service.notification.StatusBarNotification;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.biketracker.activity.activity.RedAlertActivity;
import com.app.biketracker.activity.soundHelper.BackgroundSoundService;
import com.app.biketracker.activity.utils.ConstantMethod;
import com.preference.PowerPreference;

import static android.content.Context.ACTIVITY_SERVICE;

public class NotificationHandler {

    public static final String LOCK = "lock";

    Context context;
    Class<BackgroundSoundService> BACKGROUND_SOUND_SERVICE_CLASS = BackgroundSoundService.class;

    NotificationHandler(Context context) {
        this.context = context;
    }

    void handlePosted(StatusBarNotification sbn) {
        try {
            NotificationObject no = new NotificationObject(sbn, context);
            if (no.getText().length() == 0)
                return;
            String appName = no.getAppName();
            String title = no.getTitle();
            String text = no.getText();
            if (PowerPreference.getDefaultFile().getBoolean(ConstantMethod.IS_ARM_ENABLE)) {

                if (!ConstantMethod.isEmpty(PowerPreference.getDefaultFile().getString(ConstantMethod.TRACKER_NO))) {
                    if (title.contains(PowerPreference.getDefaultFile().getString(ConstantMethod.TRACKER_NO))) {

                        if (text.contains("messages |")) {
                            String[] separated = text.split("\\|");
                            PowerPreference.getDefaultFile().putString(ConstantMethod.LOG_FLAG, separated[1].trim());
                            Intent mintent = new Intent(ConstantMethod.GetData);
                            mintent.putExtra(ConstantMethod.LOG_FLAG, ConstantMethod.LOG_FLAG_MESSAGE);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(mintent);
                        } else {
                            PowerPreference.getDefaultFile().putString(ConstantMethod.LOG_FLAG, text);
                            Intent mintent = new Intent(ConstantMethod.GetData);
                            mintent.putExtra(ConstantMethod.LOG_FLAG, ConstantMethod.LOG_FLAG_MESSAGE);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(mintent);
                        }

                        String time = ConstantMethod.getTime(no.getSystemTime());
                        String date = ConstantMethod.getDate(no.getSystemTime());
                        String packageName = no.getPackageName();
                        long timeInMillis = no.getSystemTime();
                        String[] selectionArgs = new String[]{appName, text, title, time, date, packageName};
                        String selection = "name = ? AND text = ? AND title = ? AND time = ? AND date = ? AND packagename = ?";
                        String[] projection = {
                                NotificationsContract.NotifEntry._ID,
                                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_NAME,
                                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TIME,
                                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_DATE,
                                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TITLE,
                                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TEXT,
                                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_PACKAGE_NAME};
                        Cursor cursor = context.getContentResolver().query(NotificationsContract.NotifEntry.CONTENT_URI, projection, selection, selectionArgs, null);
                        if (cursor != null && cursor.getCount() > 0)
                            return;
                        synchronized (LOCK) {
                            ContentValues values = new ContentValues();
                            values.put(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_NAME, appName);
                            values.put(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TITLE, title);
                            values.put(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TEXT, text);
                            values.put(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TIME, time);
                            values.put(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_DATE, date);
                            values.put(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_PACKAGE_NAME, packageName);
                            values.put(NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_TIME_IN_MILLI, timeInMillis);
                            context.getContentResolver().insert(NotificationsContract.NotifEntry.CONTENT_URI, values);
                        }
                        if (text.contains("Bike Tracker Alarm")) {
                            if (!isMyServiceRunning(BACKGROUND_SOUND_SERVICE_CLASS)) {
                                Intent myService = new Intent(context, BackgroundSoundService.class);
                                context.startService(myService);
                            }
                            Intent dialogIntent = new Intent(context, RedAlertActivity.class);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(dialogIntent);
                        }
                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
