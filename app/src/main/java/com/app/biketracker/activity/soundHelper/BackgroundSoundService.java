package com.app.biketracker.activity.soundHelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.app.biketracker.R;




public class BackgroundSoundService extends Service {
    private static final int FOREGROUND_SERVICE_ID = 111;
    private static final String TAG = "BackgroundSoundService";
    MediaPlayer player;

    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBind()" );
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        try{
            player = MediaPlayer.create(this, R.raw.warning);
            player.setLooping(true);
            player.setVolume(100,100);
            Toast.makeText(this, "Bike Tracker Alarm...", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.app.biketracker";
            String channelName = "My BackgroundSound Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            Notification.Builder builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("BackgroundSoundService Running")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true);
            Notification notification = builder.build();
            startForeground(FOREGROUND_SERVICE_ID, notification);
            Log.e(TAG,"startForeground >= Build.VERSION_CODES.O");
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("BackgroundSoundService Running")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true);
            Notification notification = builder.build();
            startForeground(FOREGROUND_SERVICE_ID, notification);
            Log.e(TAG,"startForeground < Build.VERSION_CODES.O");
        }

        if(Preferences.getMediaPosition(getApplicationContext())>0){
            Log.i(TAG, "onStartCommand(), position stored, continue from position : " + Preferences.getMediaPosition(getApplicationContext()));
            player.start();
            player.seekTo(Preferences.getMediaPosition(getApplicationContext()));
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent myService = new Intent(this, BackgroundSoundService.class);
                stopService(myService);
            }, 6000);
        }else {
            Log.i(TAG, "onStartCommand() Start!...");
            player.start();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent myService = new Intent(this, BackgroundSoundService.class);
                stopService(myService);
            }, 6000);
        }
        //re-create the service if it is killed.
        return Service.START_STICKY;
    }

    public IBinder onUnBind(Intent arg0) {
        Log.i(TAG, "onUnBind()");
        return null;
    }

    public void onStop() {
        Log.i(TAG, "onStop()");
        Preferences.setMediaPosition(getApplicationContext(), player.getCurrentPosition());
    }

    public void onPause() {
        Log.i(TAG, "onPause()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() , service stopped! Media position: " + player.getCurrentPosition());
        //Save current position before destruction.
        Preferences.setMediaPosition(getApplicationContext(), player.getCurrentPosition());
        player.pause();
        player.release();
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory()");
        Preferences.setMediaPosition(getApplicationContext(), player.getCurrentPosition());
    }

    //Inside AndroidManifest.xml add android:stopWithTask="false" to the Service definition.
    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Preferences.setMediaPosition(getApplicationContext(), player.getCurrentPosition());
    }

}
