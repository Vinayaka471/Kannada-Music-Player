package com.kannada.musicplayer.ads;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {


    public static SharedPreferences sharedPreferencesInApp;
    public static SharedPreferences.Editor editorInApp;

    // Ad-related fields maintained for compatibility but with no-op values
    public static String AdMob_Banner1 = "ca-app-pub-3940256099942544/6300978111";
    public static String AdMob_Int1 = "";
    public static String AdMob_Int2 = "";
    public static String AdMob_NativeAdvance1 = "";
    public static String AdMob_NativeAdvance2 = "";
    public static String App_Open = "";

    public static String FbBanner = "";
    public static String FbInter = "";
    public static String Fbnative = "";
    public static String FbNativeB = "";
    public static String AdMob_Adaptive_Banner = "ca-app-pub-3940256099942544/9214589741";

    public static int click = 9999;
    public static int backclick = 9999;
    public static int AdsClickCount = 0;
    public static int backAdsClickCount = 0;

    public static String Type1 = "none";
    public static String Type2 = "none";
    public static String Type3 = "";
    public static String Type4 = "";

    public static String MoreApps = "More+Apps";
    public static String PrivacyPolicy = "https://www.google.com";

    public static Context context1;

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);

        sharedPreferencesInApp = getSharedPreferences("my", MODE_PRIVATE);
        editorInApp = sharedPreferencesInApp.edit();

        context1 = getApplicationContext();

        /*app code*/
        createNotificationChannel();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }


    public static void setuser_balance(Integer user_balance) {
        if (editorInApp != null) {
            editorInApp.putInt("user_balance", user_balance).commit();
        }
    }
    public static Integer getuser_balance() {
        if (sharedPreferencesInApp != null) {
            return sharedPreferencesInApp.getInt("user_balance", 0);
        }
        return 0;
    }

    public static void setuser_onetime(Integer user_onetime) {
        if (editorInApp != null) {
            editorInApp.putInt("user_onetime", user_onetime).commit();
        }
    }
    public static Integer getuser_onetime() {
        if (sharedPreferencesInApp != null) {
            return sharedPreferencesInApp.getInt("user_onetime", 0);
        }
        return 0;
    }

    public static void setuser_permission(Integer user_permission) {
        if (editorInApp != null) {
            editorInApp.putInt("user_permission", user_permission).commit();
        }
    }
    public static Integer getuser_permission() {
        if (sharedPreferencesInApp != null) {
            return sharedPreferencesInApp.getInt("user_permission", 0);
        }
        return 0;
    }



    /*app code*/

    public static final String CHANNEL_ID_1 = "channel1";

    public static Context getContext() {
        return context1;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID_1, "Channel(1)", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Channel 1 Desc..");
            NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_ID_1, "Channel(2)", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel2.setDescription("Channel 2 Desc..");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.createNotificationChannel(notificationChannel2);
        }
    }


}