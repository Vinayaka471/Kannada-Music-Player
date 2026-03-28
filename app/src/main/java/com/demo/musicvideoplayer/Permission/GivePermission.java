package com.demo.musicvideoplayer.Permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;

public class GivePermission {

    public static void applyPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            ((TedPermission.Builder) ((TedPermission.Builder) TedPermission.create().setPermissions("android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_AUDIO")).setPermissionListener(new PermissionListener() {
                @Override
                public void onPermissionDenied(List<String> list) {
                    //Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                    //intent.setData(Uri.fromParts("package", SecondActivity.this.getPackageName(), (String) null));
                    //startActivityForResult(intent, 1000);
                }

                @Override
                public void onPermissionGranted() {
                    //after permission

                }
            })).check();
        } else {
            ((TedPermission.Builder) ((TedPermission.Builder) TedPermission.create().setPermissions("android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE")).setPermissionListener(new PermissionListener() {
                @Override
                public void onPermissionDenied(List<String> list) {
                    //Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                    //intent.setData(Uri.fromParts("package", SecondActivity.this.getPackageName(), (String) null));
                    //startActivityForResult(intent, 1000);
                }

                @Override
                public void onPermissionGranted() {
                    //after permission
                }
            })).check();
        }
    }

    public static boolean isNetworkConnected(Context context) {
        @SuppressLint("WrongConstant") ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

}
