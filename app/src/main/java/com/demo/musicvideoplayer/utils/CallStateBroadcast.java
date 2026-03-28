package com.demo.musicvideoplayer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.demo.musicvideoplayer.service.AudioService;

public class CallStateBroadcast extends BroadcastReceiver {
    boolean isCalling = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("CallStateBroadcast", "received");
        if (AppConstants.isMyServiceRunning(context, AudioService.class)) {
            String stringExtra = intent.getStringExtra("state");
            Log.d("TAG", "onReceive: State " + stringExtra);
            if (stringExtra.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                this.isCalling = true;
                Intent intent2 = new Intent();
                intent2.setAction(AppConstants.PLAY_PAUSE);
                intent2.putExtra("phone_state", AppConstants.CALLING);
                context.sendBroadcast(intent2);
                Log.e("Zed", "ringing");
            }
            if (stringExtra.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.e("Zed", "offhook");
            }
            if (stringExtra.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Intent intent3 = new Intent();
                intent3.setAction(AppConstants.PLAY_PAUSE);
                intent3.putExtra("phone_state", AppConstants.CALLING);
                context.sendBroadcast(intent3);
                this.isCalling = false;
                Log.e("Zed", "idle");
            }
        }
    }
}
