package com.kannada.musicplayer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import com.kannada.musicplayer.service.AudioService;

public class HardButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "onReceive: From Media button");
        if ("android.intent.action.MEDIA_BUTTON".equals(intent.getAction())) {
            KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
            if (keyEvent.getAction() == 1) {
                int keyCode = keyEvent.getKeyCode();
                if (keyCode == 87) {
                    if (AppConstants.isMyServiceRunning(context, AudioService.class)) {
                        Intent intent2 = new Intent();
                        intent2.setAction(AppConstants.NEXT);
                        context.sendBroadcast(intent2);
                    }
                } else if (keyCode == 85 && AppConstants.isMyServiceRunning(context, AudioService.class)) {
                    Intent intent3 = new Intent();
                    intent3.setAction(AppConstants.PLAY_PAUSE);
                    context.sendBroadcast(intent3);
                }
            }
        }
    }
}
