package com.demo.musicvideoplayer.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_ID_1 = "channel1";
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        createNotificationChannel();
    }

    public static Context getContext() {
        return context;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID_1, "Channel(1)", 4);
            notificationChannel.setDescription("Channel 1 Desc..");
            NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_ID_1, "Channel(2)", 4);
            notificationChannel2.setDescription("Channel 2 Desc..");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.createNotificationChannel(notificationChannel2);
        }
    }
}
