package com.app.biketracker.activity.activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.biketracker.R;
import com.app.biketracker.activity.smslogHelper.NotifCursorAdaptor;
import com.app.biketracker.activity.smslogHelper.NotifDbHelper;
import com.app.biketracker.activity.smslogHelper.NotificationListener;
import com.app.biketracker.activity.smslogHelper.NotificationsContract;

public class SmsLogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    NotifDbHelper notifDbHelper;
    private NotifCursorAdaptor notifCursorAdaptor;
    private static final int NOTIF_LOADER = 1;
    ListView listView;
    ImageView imgBack;
    Button btnDeleteAll;
    TextView txtLogNoFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_log);
        checkNotifPermission();
        doNotKillService();
        listView = findViewById(R.id.list_view_notif);
        imgBack = findViewById(R.id.imgBack);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);

        txtLogNoFound = findViewById(R.id.txtLogNoFound);
        notifCursorAdaptor = new NotifCursorAdaptor(this, null);

        notifDbHelper = new NotifDbHelper(this);
        listView.setAdapter(notifCursorAdaptor);

        registerForContextMenu(listView);

        getLoaderManager().initLoader(NOTIF_LOADER, null, this);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertForDeletingAllNotifications();
            }
        });

    }

    private void doNotKillService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationListener.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void checkNotifPermission() {
        boolean isNotificationServiceRunning = isNotificationServiceRunning();
        if (!isNotificationServiceRunning) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enable Notification Access").setTitle("Enable permissions");
            builder.setMessage("For checking current log of your bike tracker please enable notification access of bike tracker log")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            }
                        }
                    })
                    .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                           onBackPressed();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setTitle("Enable Notification Access");
            alert.show();
        }
    }

    private boolean isNotificationServiceRunning() {
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                NotificationsContract.NotifEntry._ID,
                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_NAME,
                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TIME,
                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_DATE,
                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TITLE,
                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_TEXT,
                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_DATA_PACKAGE_NAME,
                NotificationsContract.NotifEntry.COLUMN_NOTIF_APP_TIME_IN_MILLI};

        return new CursorLoader(this, NotificationsContract.NotifEntry.CONTENT_URI, projection, null, null, NotificationsContract.NotifEntry._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        notifCursorAdaptor.swapCursor(data);
        if(data.getCount()==0)
        {
            txtLogNoFound.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            btnDeleteAll.setVisibility(View.GONE);
        }
        else
        {
            txtLogNoFound.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            btnDeleteAll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        notifCursorAdaptor.swapCursor(null);
    }

    private void alertForDeletingAllNotifications() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to delete all logged sms?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int rowsDeleted = getContentResolver().delete(NotificationsContract.NotifEntry.CONTENT_URI, null, null);
                        dialog.cancel();
                        if (rowsDeleted > 0)
                            Toast.makeText(getApplicationContext(), R.string.delted_all_successfully, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button

                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Delete All Notifications");
        alert.show();
    }

    private void deleteCurrentNotif(final long notifId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to delete this notification ?").setTitle("Delete current Notification")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri currentNotifUri = ContentUris.withAppendedId(NotificationsContract.NotifEntry.CONTENT_URI, notifId);
                        int rowDeleted = getContentResolver().delete(currentNotifUri, null, null);
                        if (rowDeleted == 0) {
                            Toast.makeText(getApplicationContext(), "Notification Delete failed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Notification Deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Delete selected notification");
        alert.show();
    }
}