package com.demo.musicvideoplayer.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import com.demo.musicvideoplayer.model.HistoryModel;
import com.demo.musicvideoplayer.model.VideoModal;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;

public class AppConstants {
    public static final String ACTION_CHANGE_SONG = "ACTION_CHANGE_SONG";
    public static final int ALBUM = 2;
    public static final String APP_DB_NAME = "xPlayer.db";
    public static final int APP_DB_VERSION = 1;
    public static final String APP_EMAIL_SUBJECT = "Your Suggestion - SHIFT CALENDAR:Scheduler & Planner";
    public static final int ARTIST = 3;
    public static final String AUTO_ROTATE = "Auto";
    public static final String CALLING = "CALLING";
    public static final String CALL_END = "CALL_END";
    public static final int CANCEL = 2;
    public static final String CHANGE_SONG = "CHANGE_SONG";
    public static final String CLOSE = "Close";
    public static final String CLOSE_BG_VIDEO = "CLOSE_BG_VIDEO";
    public static final String CONTENT = "CONTENT";
    public static final String CREATE = "CREATE";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");
    public static final String DATE_WISE = "DATE_WISE";
    public static String DISCLOSURE_DIALOG_DESC = "We would like to inform you regarding the 'Consent to Collection and Use Of Data'\n\nTo list all audio and video for Heeader player, allow access to Storage Permission.\n\nWe store your data on your device only, we don’t store them on our server.";
    public static final String FILE_SIZE_WISE = "FILE_SIZE_WISE";
    public static final String FIT = "FIT";
    public static final int FOLDER = 1;
    public static final String FULL = "FULL";
    public static String IMAGE_FOLDER = "HEEDER";
    public static final int INFO = 2;
    public static final String LANDSCAPE = "Landscape";
    public static final String LENGTH_WISE = "LENGTH_WISE";
    public static final String LOOP_ALL = "Loop All";
    public static final String MAIN_ACTIVITY_RECEIVER = "MAIN_ACTIVITY_RECEIVER";
    public static final int MENU_VIDEO = 2;
    public static final String NAME_WISE = "NAME_WISE";
    public static final String NEXT = "Next";
    public static final int OPEN = 1;
    public static final String ORDER = "Order";
    public static final int PAUSE = 2;
    public static final int PLAY = 1;
    public static final String PLAY_PAUSE = "PLAY_PAUSE";
    public static final int PLAY_VIDEO = 1;
    public static final String PORTRAIT = "Portrait";
    public static final String PREVIOUS = "Previous";
    public static final String PREVIOUS_BG_VIDEO = "PREVIOUS_BG_VIDEO";
    public static final String RECEIVER = "receiverKEy";
    public static final String RECEIVER_ACTIVITY = "RECEIVER_ACTIVITY";
    public static final String REPEAT_CURRENT = "Repeat Current";
    public static final int REQUEST_CODE = 1;
    public static final String RUNNING = "RUNNING";
    public static final String SHUFFLE_ALL = "Shuffle All";
    public static final int SONG = 4;
    public static final String STOP = "STOP";
    public static final String STOP_BG_PLAYER = "STOP_BG_PLAYER";
    public static final String ZOOM = "ZOOM";

    public static String AssetsPath() {
        return "file:///android_asset/";
    }

    public static void shareApp(Context context) {
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.TEXT", "Video Music Player\n- Support all video and audio file formats\n- Best Video Popup Player\n- Gesture control, easy to control volume, brightness, and playing progress with \n- Operate any video/audio from notification pane\n- Scans and displays all your videos from your phone\n- HD Video player with Swipe controls \n- Best media player with an attractive user Interface, with playlist feature\n\nhttps://play.google.com/store/apps/details?id=" + context.getPackageName());
            context.startActivity(Intent.createChooser(intent, "Share via"));
        } catch (Exception e) {
            Log.d("TAG", "shareApp: " + e.toString());
        }
    }

    public static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static int GetRandomPosition(int i) {
        return new Random().nextInt(i);
    }

    public static boolean isNightModeActive(Context context) {
        int defaultNightMode = AppCompatDelegate.getDefaultNightMode();
        if (defaultNightMode == 2) {
            return true;
        }
        return defaultNightMode != 1 && (context.getResources().getConfiguration().uiMode & 48) == 32;
    }

    public static String formatSize(long j) {
        float f = (((float) j) / 1024.0f) / 1024.0f;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (f > 1000.0f) {
            return decimalFormat.format((double) (f / 1024.0f)) + " GB";
        }
        return decimalFormat.format((double) f) + " MB";
    }

    public static String formatTime(long j) {
        long hours = TimeUnit.MILLISECONDS.toHours(j);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(j) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(j) % 60;
        if (hours == 0) {
            return String.format(Locale.US, "%02d:%02d", new Object[]{Long.valueOf(minutes), Long.valueOf(seconds)});
        }
        return String.format(Locale.US, "%02d:%02d:%02d", new Object[]{Long.valueOf(hours), Long.valueOf(minutes), Long.valueOf(seconds)});
    }

    public static String formattedDate(long j, DateFormat dateFormat) {
        return dateFormat.format(new Date(j));
    }

    public static long getDuration(Context context, String str) throws IOException {
        long j;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(context, Uri.parse(str));
            j = Long.parseLong(mediaMetadataRetriever.extractMetadata(9));
            mediaMetadataRetriever.release();
        } catch (Exception e) {
            mediaMetadataRetriever.release();
            e.printStackTrace();
            j = 0;
        }
        if (j > 0) {
            return j;
        }
        return 0;
    }

    public static String timeFormat(long j) {
        return String.format("%02d:%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toHours(j) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(j))), Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(j) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(j))), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j)))});
    }

    public static String GetTimeNew(long j) {
        return String.format("%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(j)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j)))});
    }

    public static String formattingHours(int i) {
        int i2 = i / 3600;
        int i3 = i2 * 3600;
        int i4 = (i - i3) / 60;
        int i5 = i - (i3 + (i4 * 60));
        if (i2 == 0) {
            return String.format(Locale.getDefault(), "%1$02d:%2$02d", new Object[]{Integer.valueOf(i4), Integer.valueOf(i5)});
        }
        return String.format(Locale.getDefault(), "%1$d:%2$02d:%3$02d", new Object[]{Integer.valueOf(i2), Integer.valueOf(i4), Integer.valueOf(i5)});
    }

    public static String getSize(long j) {
        double d = (double) (j / 1024);
        double d2 = d / 1024.0d;
        double d3 = d2 / 1024.0d;
        double d4 = d3 / 1024.0d;
        int i = (j > 1024 ? 1 : (j == 1024 ? 0 : -1));
        if (i < 0) {
            return j + " Bytes";
        } else if (i >= 0 && j < 1048576) {
            return String.format("%.2f", new Object[]{Double.valueOf(d)}) + " KB";
        } else if (j >= 1048576 && j < FileUtils.ONE_GB) {
            return String.format("%.2f", new Object[]{Double.valueOf(d2)}) + " MB";
        } else if (j >= FileUtils.ONE_GB && j < FileUtils.ONE_TB) {
            return String.format("%.2f", new Object[]{Double.valueOf(d3)}) + " GB";
        } else if (j < FileUtils.ONE_TB) {
            return "";
        } else {
            return String.format("%.2f", new Object[]{Double.valueOf(d4)}) + " TB";
        }
    }

    public static void refreshGallery(String str, Context context) {
        File file = new File(str);
        try {
            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            Uri fromFile = Uri.fromFile(file);
            Log.i("refreshGallery", "refreshGallery: " + fromFile);
            intent.setData(fromFile);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void refreshFiles(Context context, File file) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(Uri.fromFile(file));
        context.sendBroadcast(intent);
    }

    public static void refreshUri(Context context, Uri uri) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    public static long GetOnlyDate(long j) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j);
        instance.set(11, 0);
        instance.set(12, 0);
        instance.set(13, 0);
        instance.set(14, 0);
        return instance.getTimeInMillis();
    }

    public static long GetYesterday() {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(System.currentTimeMillis());
        instance.set(5, instance.get(5) - 1);
        instance.set(11, 0);
        instance.set(12, 0);
        instance.set(13, 0);
        instance.set(14, 0);
        return instance.getTimeInMillis();
    }

    public static String GetRecentDate(long j) {
        if (j == GetOnlyDate(System.currentTimeMillis())) {
            return "Today";
        }
        if (j == GetYesterday()) {
            return "Yesterday";
        }
        return DATE_FORMAT.format(Long.valueOf(j));
    }

    public static void SetVideoHistory(VideoModal videoModal) {
        ArrayList<HistoryModel> arrayList = new ArrayList<>();
        if (AppPref.getRecentList() != null) {
            arrayList = AppPref.getRecentList();
            AppPref.getRecentList().clear();
        }
        ArrayList arrayList2 = new ArrayList();
        AudioVideoModal audioVideoModal = new AudioVideoModal(videoModal.getaPath(), videoModal.getaName(), videoModal.getDuration(), "", "", 0);
        if (!arrayList.contains(new HistoryModel(audioVideoModal))) {
            arrayList2.add(new HistoryModel(audioVideoModal, GetOnlyDate(System.currentTimeMillis())));
            int i = 0;
            while (i < arrayList.size()) {
                if (arrayList2.size() < 20) {
                    arrayList2.add(arrayList.get(i));
                    i++;
                } else {
                    return;
                }
            }
            AppPref.setRecentList(arrayList2);
        }
    }

    public static void SetAudioHistory(AudioVideoModal audioVideoModal) {
        ArrayList<HistoryModel> arrayList = new ArrayList<>();
        if (AppPref.getRecentList() != null) {
            arrayList = AppPref.getRecentList();
            AppPref.getRecentList().clear();
        }
        if (!arrayList.contains(new HistoryModel(new AudioVideoModal(audioVideoModal.getUri())))) {
            ArrayList arrayList2 = new ArrayList();
            arrayList2.add(new HistoryModel(audioVideoModal, GetOnlyDate(System.currentTimeMillis())));
            int i = 0;
            while (i < arrayList.size()) {
                if (arrayList2.size() < 20) {
                    arrayList2.add(arrayList.get(i));
                    i++;
                } else {
                    return;
                }
            }
            AppPref.setRecentList(arrayList2);
        }
    }

    public static Bitmap setFolderArt(String str, Context context) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            if (Build.VERSION.SDK_INT > 29) {
                mediaMetadataRetriever.setDataSource(context, Uri.parse(str));
            } else {
                mediaMetadataRetriever.setDataSource(context, FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(str)));
            }
            byte[] embeddedPicture = mediaMetadataRetriever.getEmbeddedPicture();
            if (embeddedPicture != null) {
                return BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
            }
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (SecurityException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static boolean isMyServiceRunning(Context context, Class<?> cls) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            if (cls.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String GetPathFromUri(Context context, String str) {
        Cursor query = context.getContentResolver().query(Uri.parse(str), new String[]{"_data"}, (String) null, (String[]) null, (String) null);
        String string = query.moveToFirst() ? query.getString(query.getColumnIndexOrThrow("_data")) : null;
        query.close();
        return string;
    }

    public static String getRootPath(Context context) {
        return context.getDatabasePath(APP_DB_NAME).getParent();
    }

    public static String getMediaDir(Context context) {
        File file = new File(getRootPath(context), context.getResources().getString(R.string.app_name));
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath();
    }

    public static long getVideoDuration(String str) throws IOException {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(str);
        long parseLong = Long.parseLong(mediaMetadataRetriever.extractMetadata(9));
        mediaMetadataRetriever.release();
        return parseLong;
    }

    public static void deleteTempFile(Context context) {
        try {
            deleteFolder(new File(getMediaDir(context)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFolder(File file) {
        for (File file2 : file.listFiles()) {
            if (file2.isDirectory()) {
                deleteFolder(file2);
            } else {
                file2.delete();
            }
        }
        file.delete();
    }
}
