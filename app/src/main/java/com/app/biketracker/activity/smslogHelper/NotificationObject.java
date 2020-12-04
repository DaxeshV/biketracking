package com.app.biketracker.activity.smslogHelper;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import com.app.biketracker.activity.utils.ConstantMethod;

class NotificationObject {

    Context context;
    Notification notification;

    private String packageName;
    long postTime;
    private long systemTime;
    private boolean isOngoing;

    long when;
    private String appName;
    private String title;
    private String text;
    String extraText;
    String textBig;


    public NotificationObject(StatusBarNotification sbn, Context context) {

        this.context = context;
        notification = sbn.getNotification();
        packageName = sbn.getPackageName();
        postTime = sbn.getPostTime();
        systemTime = System.currentTimeMillis();
        isOngoing = sbn.isOngoing();
        when = notification.when;


        Bundle extras = NotificationCompat.getExtras(notification);
        appName = ConstantMethod.getAppNameFromPackage(context, packageName, false);

        if (extras != null) {
            title = ConstantMethod.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_TITLE));
            text = ConstantMethod.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_TEXT));
            extraText = ConstantMethod.nullToEmptyString(extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
            textBig = ConstantMethod.nullToEmptyString(extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT));


        }

    }


    public long getSystemTime() {
        return systemTime;
    }

    public String getAppName() {
        return appName;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean getisOngoing() {
        return isOngoing;
    }
}
