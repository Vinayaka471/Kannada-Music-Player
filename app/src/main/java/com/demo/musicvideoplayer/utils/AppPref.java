package com.demo.musicvideoplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.demo.musicvideoplayer.ads.MyApplication;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import com.demo.musicvideoplayer.model.HistoryModel;
import com.demo.musicvideoplayer.model.VideoFolderModal;
import com.demo.musicvideoplayer.model.VideoModal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppPref {

    public static String MyPref = "VideoPref";

    public static String APP_LOCK = "APP_LOCK";
    public static String AUDIO_STATE = "AUDIO_STATE";
    public static String AUDIO_TIMER = "AUDIO_TIMER";
    public static String BG_AUDIO_LIST = "BG_AUDIO_LIST";
    public static String BG_VIDEO_LIST = "BG_VIDEO_LIST";
    public static String BRIGHTNESS = "BRIGHTNESS";
    public static String FAVLIST = "FAVLIST";
    public static String ISPINSET = "ISPINSET";
    public static String IS_DBVERSION_ADDED = "IS_DBVERSION_ADDED";
    public static String PIN = "PIN";
    public static String PLAYEDVIDEO = "PLAYEDVIDEO";
    public static String RECENT_LIST = "RECENT_LIST";
    public static String VIDEO_STATE = "VIDEO_STATE";

    static final String IS_PLAYING_FULL_SCREEN = "IS_PLAYING_FULL_SCREEN";
    static final String IS_RATEUS = "IS_RATEUS_NEW";
    static final String IS_RATE_US_ACTION = "IS_RATE_US_ACTION";
    static final String NEVER_SHOW_RATTING_DIALOG = "isNeverShowRatting";
    static final String PRO_VERSION = "PRO_VERSION";

    public static String IS_SHOW_HISTORY = "IS_SHOW_HISTORY";
    public static String IS_SHOW_MUSIC = "IS_SHOW_MUSIC";
    public static String IS_TERMS_ACCEPT = "IS_TERMS_ACCEPT";

    private static SharedPreferences getPref() {
        return MyApplication.getContext().getSharedPreferences(MyPref, Context.MODE_PRIVATE);
    }

    private static Gson gson() {
        return new Gson();
    }

    // ---------------------------------------------------
    // Generic Save / Load
    // ---------------------------------------------------

    private static <T> void saveList(String key, List<T> list) {
        getPref().edit().putString(key, gson().toJson(list)).apply();
    }

    private static <T> ArrayList<T> getList(String key, Type type) {
        try {
            String json = getPref().getString(key, "");
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }

            ArrayList<T> list = gson().fromJson(json, type);
            return list != null ? list : new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ---------------------------------------------------
    // Terms
    // ---------------------------------------------------

    public static boolean IsTermsAccept() {
        return getPref().getBoolean(IS_TERMS_ACCEPT, false);
    }

    public static void setIsTermsAccept(boolean value) {
        getPref().edit().putBoolean(IS_TERMS_ACCEPT, value).apply();
    }

    // ---------------------------------------------------
    // DB Version
    // ---------------------------------------------------

    public static boolean isDbVersionAdded() {
        return getPref().getBoolean(IS_DBVERSION_ADDED, false);
    }

    public static void setIsDbversionAdded(boolean value) {
        getPref().edit().putBoolean(IS_DBVERSION_ADDED, value).apply();
    }

    // ---------------------------------------------------
    // Video State
    // ---------------------------------------------------

    public static String getVideoState() {
        return getPref().getString(VIDEO_STATE, AppConstants.LOOP_ALL);
    }

    public static void setVideoState(String state) {
        getPref().edit().putString(VIDEO_STATE, state).apply();
    }

    // ---------------------------------------------------
    // Audio State
    // ---------------------------------------------------

    public static String getAudioState() {
        return getPref().getString(AUDIO_STATE, AppConstants.LOOP_ALL);
    }

    public static void setAudioState(String state) {
        getPref().edit().putString(AUDIO_STATE, state).apply();
    }

    // ---------------------------------------------------
    // Brightness
    // ---------------------------------------------------

    public static float getScreenBrightness() {
        return getPref().getFloat(BRIGHTNESS, 4.0f);
    }

    public static void setScreenBrightness(float value) {
        getPref().edit().putFloat(BRIGHTNESS, value).apply();
    }

    // ---------------------------------------------------
    // Pin
    // ---------------------------------------------------

    public static boolean isPinSet() {
        return getPref().getBoolean(ISPINSET, false);
    }

    public static void setIsPinSet(boolean value) {
        getPref().edit().putBoolean(ISPINSET, value).apply();
    }

    public static int getPin() {
        return getPref().getInt(PIN, 0);
    }

    public static void setPin(int pin) {
        getPref().edit().putInt(PIN, pin).apply();
    }

    // ---------------------------------------------------
    // Audio Timer
    // ---------------------------------------------------

    public static long getAudioTimerTime() {
        return getPref().getLong(AUDIO_TIMER, 0);
    }

    public static void setAudioTimerTime(long time) {
        getPref().edit().putLong(AUDIO_TIMER, time).apply();
    }

    // ---------------------------------------------------
    // Played Folder
    // ---------------------------------------------------

    public static void setPlayedFolder(VideoFolderModal folder) {
        getPref().edit().putString(PLAYEDVIDEO, gson().toJson(folder)).apply();
    }

    public static VideoFolderModal getPlayedFolder() {
        try {
            String json = getPref().getString(PLAYEDVIDEO, "");
            return gson().fromJson(json, VideoFolderModal.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------------------------------------------
    // Popup Video List
    // ---------------------------------------------------

    public static void setPopupVideoList(List<VideoModal> list) {
        saveList(BG_VIDEO_LIST, list);
    }

    public static ArrayList<VideoModal> getPopupVideoList() {
        Type type = new TypeToken<ArrayList<VideoModal>>() {}.getType();
        return getList(BG_VIDEO_LIST, type);
    }

    // ---------------------------------------------------
    // Background Audio
    // ---------------------------------------------------

    public static void setBgAudioList(List<AudioVideoModal> list) {
        saveList(BG_AUDIO_LIST, list);
    }

    public static ArrayList<AudioVideoModal> getBgAudioList() {
        Type type = new TypeToken<ArrayList<AudioVideoModal>>() {}.getType();
        return getList(BG_AUDIO_LIST, type);
    }

    // ---------------------------------------------------
    // Favourite
    // ---------------------------------------------------

    public static void setFavouriteList(List<AudioVideoModal> list) {
        saveList(FAVLIST, list);
    }

    public static ArrayList<AudioVideoModal> getFavouriteList() {
        Type type = new TypeToken<ArrayList<AudioVideoModal>>() {}.getType();
        return getList(FAVLIST, type);
    }

    // ---------------------------------------------------
    // Recent List
    // ---------------------------------------------------

    public static void setRecentList(List<HistoryModel> list) {
        saveList(RECENT_LIST, list);
    }

    public static ArrayList<HistoryModel> getRecentList() {
        Type type = new TypeToken<ArrayList<HistoryModel>>() {}.getType();
        return getList(RECENT_LIST, type);
    }

    // ---------------------------------------------------
    // History
    // ---------------------------------------------------

    public static boolean IsShowHistory() {
        return getPref().getBoolean(IS_SHOW_HISTORY, true);
    }

    public static void setShowHistory(boolean value) {
        getPref().edit().putBoolean(IS_SHOW_HISTORY, value).apply();
    }

    public static boolean IsShowMusic() {
        return getPref().getBoolean(IS_SHOW_MUSIC, true);
    }

    public static void setShowMusic(boolean value) {
        getPref().edit().putBoolean(IS_SHOW_MUSIC, value).apply();
    }

    // ---------------------------------------------------
    // App Lock
    // ---------------------------------------------------

    public static boolean isAppLock() {
        return getPref().getBoolean(APP_LOCK, false);
    }

    public static void setAppLock(boolean value) {
        getPref().edit().putBoolean(APP_LOCK, value).apply();
    }

    // ---------------------------------------------------
    // Rate Us
    // ---------------------------------------------------

    public static boolean IsRateUsAction(Context context) {
        return context.getSharedPreferences(MyPref, 0).getBoolean(IS_RATE_US_ACTION, false);
    }

    public static void setRateUsAction(Context context, boolean value) {
        context.getSharedPreferences(MyPref, 0).edit().putBoolean(IS_RATE_US_ACTION, value).apply();
    }

    public static void setNeverShowRatting(boolean value) {
        getPref().edit().putBoolean(NEVER_SHOW_RATTING_DIALOG, value).apply();
    }

    public static boolean isNeverShowRatting() {
        return getPref().getBoolean(NEVER_SHOW_RATTING_DIALOG, false);
    }

    public static boolean IsRateUs() {
        return getPref().getBoolean(IS_RATEUS, false);
    }

    public static void setRateUs(boolean value) {
        getPref().edit().putBoolean(IS_RATEUS, value).apply();
    }

    // ---------------------------------------------------
    // Fullscreen
    // ---------------------------------------------------

    public static boolean IsFullScreenPlay() {
        return getPref().getBoolean(IS_PLAYING_FULL_SCREEN, true);
    }

    public static void setFullScreenPlay(boolean value) {
        getPref().edit().putBoolean(IS_PLAYING_FULL_SCREEN, value).apply();
    }

    // ---------------------------------------------------
    // Pro Version
    // ---------------------------------------------------

    public static boolean IsProVersion() {
        return getPref().getBoolean(PRO_VERSION, false);
    }

    public static void setIsProVersion(boolean value) {
        getPref().edit().putBoolean(PRO_VERSION, value).apply();
    }
}