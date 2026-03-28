package com.kannada.musicplayer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.kannada.musicplayer.service.AudioService;

public class PushNotificationListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        action.hashCode();
        char c = 65535;
        switch (action.hashCode()) {
            case -1209131241:
                if (action.equals(AppConstants.PREVIOUS)) {
                    c = 0;
                    break;
                }
                break;
            case -971121397:
                if (action.equals(AppConstants.PLAY_PAUSE)) {
                    c = 1;
                    break;
                }
                break;
            case 2424595:
                if (action.equals(AppConstants.NEXT)) {
                    c = 2;
                    break;
                }
                break;
            case 65203672:
                if (action.equals(AppConstants.CLOSE)) {
                    c = 3;
                    break;
                }
                break;
            case 1669513305:
                if (action.equals(AppConstants.CONTENT)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                Intent intent2 = new Intent(context, AudioService.class);
                intent2.putExtra("ActionName", AppConstants.PREVIOUS);
                context.startService(intent2);
                return;
            case 1:
                Intent intent3 = new Intent(context, AudioService.class);
                intent3.putExtra("ActionName", AppConstants.PLAY_PAUSE);
                context.startService(intent3);
                return;
            case 2:
                Intent intent4 = new Intent(context, AudioService.class);
                intent4.putExtra("ActionName", AppConstants.NEXT);
                context.startService(intent4);
                return;
            case 3:
                Intent intent5 = new Intent(context, AudioService.class);
                intent5.putExtra("ActionName", AppConstants.CLOSE);
                context.startService(intent5);
                return;
            case 4:
                Intent intent6 = new Intent(context, AudioService.class);
                intent6.putExtra("ActionName", AppConstants.CONTENT);
                context.startService(intent6);
                return;
            default:
                throw new IllegalStateException("Unexpected value: " + intent.getAction());
        }
    }
}
