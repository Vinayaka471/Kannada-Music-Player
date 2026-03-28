package com.kannada.musicplayer.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
//import android.support.v4.media.MediaMetadataCompat;
//import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.core.internal.view.SupportMenu;
//import androidx.media.app.NotificationCompat;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.activities.AudioPlayerActivity;
import com.kannada.musicplayer.activities.BgVideoPlayerActivity;

import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.utils.ActionPlaying;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;
import java.io.File;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    public static final String CHANNEL_ID = "CHANNEL_ID";
    ActionPlaying actionPlaying;
    float audioSpeed = 1.0f;
    NotificationCompat.Builder builder;
    PendingIntent closePendingIntent;
    PendingIntent contentPendingIntent;
    HeadSetReceiver headSetReceiver;
    boolean isCalling = false;
    boolean isFromMain = false;
    IBinder mBinder = new MyBinder();
    BroadcastReceiver mReceiver = null;
    MediaPlayer mediaPlayer;
    MediaSession mediaSession;
    AudioVideoModal modal;
    PendingIntent nextPendingIntent;
    NotificationManager notificationManager;
    PendingIntent playPausePendingIntent;
    int pos = -1;
    PendingIntent previousPendingIntent;
    RemoteViews remoteViews;
    NotificationCompat.Style style;
    Uri uri;
    CountDownTimer waitTimer;

    public IBinder onBind(Intent intent) {
        Log.d("AudioService:", "onBind: called");
        return this.mBinder;
    }

    public class MyBinder extends Binder {
        public MyBinder() {
        }

        public AudioService getService() {
            Log.d("AudioService:", "get service from Binder");
            return AudioService.this;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    @Override
    public void onCreate() {
        Log.d("AudioService:", "Service onCreate");
        super.onCreate();
        this.notificationManager = (NotificationManager) getSystemService("notification");
        this.remoteViews = new RemoteViews(getPackageName(), R.layout.audio_notification);
        MediaSession mediaSession2 = new MediaSession(this, "PlayerService");
        this.mediaSession = mediaSession2;
        mediaSession2.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        this.mediaSession.setPlaybackState(new PlaybackState.Builder().setState(PlaybackState.STATE_NONE, 0, 0.0f).setActions(PlaybackState.ACTION_PLAY_PAUSE).build());
        this.mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent intent) {
                Log.d("TAG", "onMediaButtonEvent: ");
                if ("android.intent.action.MEDIA_BUTTON".equals(intent.getAction())) {
                    KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
                    if (keyEvent.getAction() == 1) {
                        int keyCode = keyEvent.getKeyCode();
                        if (keyCode == 87) {
                            if (AppConstants.isMyServiceRunning(AudioService.this, AudioService.class)) {
                                Intent intent2 = new Intent();
                                intent2.setAction(AppConstants.NEXT);
                                AudioService.this.sendBroadcast(intent2);
                            }
                        } else if (keyCode == 85 && AppConstants.isMyServiceRunning(AudioService.this, AudioService.class)) {
                            Intent intent3 = new Intent();
                            intent3.setAction(AppConstants.PLAY_PAUSE);
                            AudioService.this.sendBroadcast(intent3);
                        }
                    }
                }
                return super.onMediaButtonEvent(intent);
            }
        });
        this.mediaSession.setActive(true);
        HeadSetReceiver headSetReceiver2 = new HeadSetReceiver();
        this.headSetReceiver = headSetReceiver2;

        IntentFilter headsetFilter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(headSetReceiver2, headsetFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(headSetReceiver2, headsetFilter);
        }

        if (Build.VERSION.SDK_INT >= 31) {
            this.style = new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(MediaSessionCompat.Token.fromToken(this.mediaSession.getSessionToken()));
            this.builder = new NotificationCompat.Builder((Context) this, "CHANNEL_ID").setStyle(this.style).setSmallIcon((int) R.drawable.notification).setContentTitle(getString(R.string.app_name)).setPriority(2);
        } else {
            this.builder = new NotificationCompat.Builder((Context) this, "CHANNEL_ID").setSmallIcon((int) R.drawable.notification).setContentTitle(getString(R.string.app_name)).setPriority(2).setCustomContentView(this.remoteViews);
        }
        registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.d("AudioService:", "Service onStartCommand");
        handleIntent(this.mediaSession, intent);
        if (intent != null) {
            AudioVideoModal audioVideoModal = (AudioVideoModal) intent.getParcelableExtra("model");
            int intExtra = intent.getIntExtra("seek", 0);
            String stringExtra = intent.getStringExtra("ActionName");
            if (audioVideoModal != null) {
                this.isFromMain = true;
                int indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(audioVideoModal.getUri()));
                this.pos = indexOf;
                if (indexOf == -1) {
                    this.pos = 0;
                }
                CreateNotification(this.pos);
                stopPlaying();
                Log.d("TAG", "onStartCommand: FromService");
                createMediaPlayer(this.pos);
                start();
                MediaPlayer mediaPlayer2 = this.mediaPlayer;
                if (mediaPlayer2 != null) {
                    mediaPlayer2.seekTo(intExtra);
                }
                setWaitTimer();
            }
            if (stringExtra != null) {
                stringExtra.hashCode();
                if (stringExtra.equals(AppConstants.CONTENT)) {
                    AudioVideoModal audioVideoModal2 = AppPref.getBgAudioList().get(this.pos);
                    if (TextUtils.isEmpty(audioVideoModal2.getArtist()) || TextUtils.isEmpty(audioVideoModal2.getAlbum())) {
                        Intent intent2 = new Intent(getApplicationContext(), BgVideoPlayerActivity.class);
                        intent2.putExtra("modal", AppPref.getBgAudioList().get(this.pos));
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        stopPlaying();
                    } else {
                        Intent intent3 = new Intent(getApplicationContext(), AudioPlayerActivity.class);
                        intent3.putExtra("modal", AppPref.getBgAudioList().get(this.pos));
                        intent3.putExtra("seek", getCurrentPosition());
                        intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent3);
                    }
                    AppPref.setFullScreenPlay(true);
                }
            }
        }
        return Service.START_STICKY;
    }

    public int setNextRepetition() {
        String audioState = AppPref.getAudioState();
        audioState.hashCode();
        char c = 65535;
        switch (audioState.hashCode()) {
            case -1974417883:
                if (audioState.equals(AppConstants.LOOP_ALL)) {
                    c = 0;
                    break;
                }
                break;
            case 76453678:
                if (audioState.equals(AppConstants.ORDER)) {
                    c = 1;
                    break;
                }
                break;
            case 2090883002:
                if (audioState.equals(AppConstants.SHUFFLE_ALL)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                this.pos = (this.pos + 1) % AppPref.getBgAudioList().size();
                break;
            case 1:
                if (this.pos + 1 < AppPref.getBgAudioList().size()) {
                    this.pos++;
                    break;
                } else {
                    Intent intent = new Intent();
                    intent.setAction(AppConstants.CLOSE);
                    sendBroadcast(intent);
                    break;
                }
            case 2:
                this.pos = AppConstants.GetRandomPosition(AppPref.getBgAudioList().size() - 1);
                break;
        }
        return this.pos;
    }

    public int setPreviousRepetition() {
        String audioState = AppPref.getAudioState();
        audioState.hashCode();
        char c = 65535;
        switch (audioState.hashCode()) {
            case -1974417883:
                if (audioState.equals(AppConstants.LOOP_ALL)) {
                    c = 0;
                    break;
                }
                break;
            case 76453678:
                if (audioState.equals(AppConstants.ORDER)) {
                    c = 1;
                    break;
                }
                break;
            case 2090883002:
                if (audioState.equals(AppConstants.SHUFFLE_ALL)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                int i = this.pos;
                if (i - 1 < 0) {
                    i = AppPref.getBgAudioList().size();
                }
                this.pos = i - 1;
                break;
            case 1:
                int i2 = this.pos;
                if (i2 - 1 >= 0) {
                    this.pos = i2 - 1;
                    break;
                } else {
                    Intent intent = new Intent();
                    intent.setAction(AppConstants.CLOSE);
                    sendBroadcast(intent);
                    break;
                }
            case 2:
                this.pos = AppConstants.GetRandomPosition(AppPref.getBgAudioList().size() - 1);
                break;
        }
        return this.pos;
    }

    public void CloseService() {
        Intent intent = new Intent(AppConstants.MAIN_ACTIVITY_RECEIVER);
        intent.setPackage(getPackageName());
        intent.putExtra(NotificationCompat.CATEGORY_STATUS, AppConstants.STOP);
        sendBroadcast(intent);
    }

    public void playPauseClick() {
        ActionPlaying actionPlaying2 = this.actionPlaying;
        if (actionPlaying2 != null) {
            int playPauseBtnClick = actionPlaying2.playPauseBtnClick();
            Intent intent = new Intent(AppConstants.MAIN_ACTIVITY_RECEIVER);
            intent.setPackage(getPackageName());
            intent.putExtra(NotificationCompat.CATEGORY_STATUS, AppConstants.PLAY_PAUSE);
            intent.putExtra("action", playPauseBtnClick);
            sendBroadcast(intent);
            if (Build.VERSION.SDK_INT >= 31) {
                UpdateMediaPlayPause(playPauseBtnClick);
            } else {
                updatePlayPauseNotification(playPauseBtnClick);
            }
        }
    }

    public void UpdateMediaPlayPause(int i) {
        this.builder.clearActions();
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("modal", AppPref.getBgAudioList().get(this.pos));
        intent.putExtra("seek", getCurrentPosition());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= 23) {
            this.contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            this.contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        if (i == 1) {
            this.builder.addAction(R.drawable.ic_baseline_previous, "", this.previousPendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_play, "", this.playPausePendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_next, "", this.nextPendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_close, "", this.closePendingIntent);
            this.builder.addAction(R.id.llMain, "", this.contentPendingIntent);
        } else {
            this.builder.addAction(R.drawable.ic_baseline_previous, "", this.previousPendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_pause, "", this.playPausePendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_next, "", this.nextPendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_close, "", this.closePendingIntent);
            this.builder.addAction(R.id.llMain, "", this.contentPendingIntent);
        }
        this.builder.setContentIntent(this.contentPendingIntent);
        notificationManager.notify(1, builder.build());
    }

    public void updatePlayPauseNotification(int i) {
        int i2 = Build.VERSION.SDK_INT;
        if (i == 1) {
            this.remoteViews.setImageViewResource(R.id.playPause, R.drawable.play_white);
        } else {
            this.remoteViews.setImageViewResource(R.id.playPause, R.drawable.pause_white);
        }
        this.builder.build();
        this.notificationManager.notify(1, this.builder.build());
    }

    public void previousButton() {
        ActionPlaying actionPlaying2 = this.actionPlaying;
        if (actionPlaying2 != null) {
            this.pos = actionPlaying2.prevBtnClick();
        }
        Intent intent = new Intent(AppConstants.MAIN_ACTIVITY_RECEIVER);
        intent.setPackage(getPackageName());
        intent.putExtra(NotificationCompat.CATEGORY_STATUS, "RUNNING");
        intent.putExtra("position", this.pos);
        sendBroadcast(intent);
    }

    public void nextButton() {
        ActionPlaying actionPlaying2 = this.actionPlaying;
        if (actionPlaying2 != null) {
            this.pos = actionPlaying2.nextBtnClick();
        }
        Intent intent = new Intent(AppConstants.MAIN_ACTIVITY_RECEIVER);
        intent.setPackage(getPackageName());
        intent.putExtra(NotificationCompat.CATEGORY_STATUS, "RUNNING");
        intent.putExtra("position", this.pos);
        sendBroadcast(intent);
    }

    private void CreateNotification(int i) {
        Log.d("AudioService:", "Service CreateNotification");
        Intent intent = new Intent();
        intent.setAction(AppConstants.PREVIOUS);
        if (Build.VERSION.SDK_INT >= 23) {
            this.previousPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            this.previousPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Intent intent2 = new Intent();
        intent2.setAction(AppConstants.NEXT);
        if (Build.VERSION.SDK_INT >= 23) {
            this.nextPendingIntent = PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            this.nextPendingIntent = PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Intent intent3 = new Intent();
        intent3.setAction(AppConstants.PLAY_PAUSE);
        if (Build.VERSION.SDK_INT >= 23) {
            this.playPausePendingIntent = PendingIntent.getBroadcast(this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            this.playPausePendingIntent = PendingIntent.getBroadcast(this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Intent intent4 = new Intent();
        intent4.setAction(AppConstants.CLOSE);
        if (Build.VERSION.SDK_INT >= 23) {
            this.closePendingIntent = PendingIntent.getBroadcast(this, 0, intent4, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            this.closePendingIntent = PendingIntent.getBroadcast(this, 0, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Intent intent5 = new Intent(this, AudioPlayerActivity.class);
        intent5.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent5.putExtra("modal", AppPref.getBgAudioList().get(i));
        intent5.putExtra("seek", getCurrentPosition());
        if (Build.VERSION.SDK_INT >= 23) {
            this.contentPendingIntent = PendingIntent.getActivity(this, 0, intent5, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            this.contentPendingIntent = PendingIntent.getActivity(this, 0, intent5, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("CHANNEL_ID", "com.heeder23.videoplayer23", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
            notificationChannel.enableVibration(false);
            notificationChannel.setLockscreenVisibility(1);
            notificationChannel.setSound((Uri) null, (AudioAttributes) null);
            this.notificationManager.createNotificationChannel(notificationChannel);
        }
        if (Build.VERSION.SDK_INT >= 31) {
            AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(i);
            Bitmap bitmap = getBitmap(AppPref.getBgAudioList().get(i));
            MediaMetadata.Builder builder2 = new MediaMetadata.Builder();
            builder2.putString("android.media.metadata.TITLE", audioVideoModal.getName());
            builder2.putString("android.media.metadata.ARTIST", audioVideoModal.getArtist());
            builder2.putBitmap("android.media.metadata.ART", bitmap);
            builder2.putLong("android.media.metadata.DURATION", audioVideoModal.getDuration());
            this.mediaSession.setMetadata(builder2.build());
            this.builder.addAction(R.drawable.ic_baseline_previous, "", this.previousPendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_pause, "", this.playPausePendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_next, "", this.nextPendingIntent);
            this.builder.addAction(R.drawable.ic_baseline_close, "", this.closePendingIntent);
            this.builder.setContentIntent(this.contentPendingIntent);
        }
        this.remoteViews.setOnClickPendingIntent(R.id.previous, this.previousPendingIntent);
        this.remoteViews.setOnClickPendingIntent(R.id.next, this.nextPendingIntent);
        this.remoteViews.setOnClickPendingIntent(R.id.playPause, this.playPausePendingIntent);
        this.remoteViews.setOnClickPendingIntent(R.id.close, this.closePendingIntent);
        this.remoteViews.setOnClickPendingIntent(R.id.llMain, this.contentPendingIntent);
        this.remoteViews.setTextViewText(R.id.txtTitle, AppPref.getBgAudioList().get(i).getName());
        if (!TextUtils.isEmpty(AppPref.getBgAudioList().get(i).getArtist())) {
            this.remoteViews.setViewVisibility(R.id.txtArtist, View.VISIBLE);
            this.remoteViews.setTextViewText(R.id.txtArtist, AppPref.getBgAudioList().get(i).getArtist());
        } else {
            this.remoteViews.setViewVisibility(R.id.txtArtist, View.INVISIBLE);
        }
        Bitmap bitmap2 = getBitmap(AppPref.getBgAudioList().get(i));
        if (bitmap2 != null) {
            this.remoteViews.setImageViewBitmap(R.id.imgArt, bitmap2);
        }
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, this.builder.build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, this.builder.build());
        }
    }

    public void VolumeOn(int i) {
        float f = (float) i;
        this.mediaPlayer.setVolume(f, f);
    }

    public void VolumeOff() {
        this.mediaPlayer.setVolume(0.0f, 0.0f);
    }

    public void updatePosition(AudioVideoModal audioVideoModal) {
        if (AppPref.getBgAudioList().contains(audioVideoModal)) {
            this.pos = AppPref.getBgAudioList().indexOf(audioVideoModal);
        } else {
            this.pos = 0;
        }
    }

    public void UpdateMediaCompact(int i) {
        AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(i);
        Bitmap bitmap = getBitmap(AppPref.getBgAudioList().get(i));
        MediaMetadata.Builder builder2 = new MediaMetadata.Builder();
        builder2.putString("android.media.metadata.TITLE", audioVideoModal.getName());
        builder2.putString("android.media.metadata.ARTIST", audioVideoModal.getArtist());
        if (bitmap != null) {
            builder2.putBitmap("android.media.metadata.ART", bitmap);
        }
        builder2.putLong("android.media.metadata.DURATION", audioVideoModal.getDuration());
        this.mediaSession.setMetadata(builder2.build());
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("modal", AppPref.getBgAudioList().get(i));
        intent.putExtra("seek", getCurrentPosition());
        if (Build.VERSION.SDK_INT >= 23) {
            this.contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            this.contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        this.builder.setContentIntent(this.contentPendingIntent);
        notificationManager.notify(1, builder.build());
    }

    public void updateNotification(int i) {
        this.remoteViews.setTextViewText(R.id.txtTitle, AppPref.getBgAudioList().get(i).getName());
        if (!TextUtils.isEmpty(AppPref.getBgAudioList().get(i).getArtist())) {
            this.remoteViews.setViewVisibility(R.id.txtArtist, View.VISIBLE);
            this.remoteViews.setTextViewText(R.id.txtArtist, AppPref.getBgAudioList().get(i).getArtist());
        } else {
            this.remoteViews.setViewVisibility(R.id.txtArtist, View.INVISIBLE);
        }
        Bitmap bitmap = getBitmap(AppPref.getBgAudioList().get(i));
        if (bitmap != null) {
            this.remoteViews.setViewVisibility(R.id.imgArt, View.VISIBLE);
            this.remoteViews.setImageViewBitmap(R.id.imgArt, bitmap);
        } else {
            this.remoteViews.setViewVisibility(R.id.imgArt, View.GONE);
        }
        this.builder.build();
        this.notificationManager.notify(1, this.builder.build());
    }

    public Bitmap getBitmap(AudioVideoModal audioVideoModal) {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT > 29) {
                mediaMetadataRetriever.setDataSource(this, Uri.parse(audioVideoModal.getUri()));
            } else {
                mediaMetadataRetriever.setDataSource(this, FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(audioVideoModal.getUri())));
            }
            if (TextUtils.isEmpty(audioVideoModal.getAlbum()) || TextUtils.isEmpty(audioVideoModal.getArtist())) {
                return mediaMetadataRetriever.getFrameAtTime(1000);
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

    public void start() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.start();
        }
        if (Build.VERSION.SDK_INT >= 31) {
            UpdateMediaPlayPause(2);
        } else {
            updatePlayPauseNotification(2);
        }
    }

    public boolean isPlaying() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            return mediaPlayer2.isPlaying();
        }
        return false;
    }

    public void stop() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.stop();
        }
    }

    public void release() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.reset();
            this.mediaPlayer.release();
        }
    }

    public int getDuartion() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            return mediaPlayer2.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            return mediaPlayer2.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int i) {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.seekTo(i);
        }
    }

    public void createMediaPlayer(int i) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        this.mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(AppPref.getBgAudioList().get(i).getUri()));
        if (Build.VERSION.SDK_INT >= 31) {
            UpdateMediaCompact(i);
        } else {
            updateNotification(i);
        }
        this.modal = AppPref.getBgAudioList().get(i);
        AppConstants.SetAudioHistory(AppPref.getBgAudioList().get(i));
        setAudioSpeed(this.audioSpeed);
    }

    public AudioVideoModal getPlayingModel() {
        return this.modal;
    }

    public void pause() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.pause();
        }
        if (Build.VERSION.SDK_INT >= 31) {
            UpdateMediaPlayPause(1);
        } else {
            updatePlayPauseNotification(1);
        }
    }

    public boolean isPlayerNotNull() {
        return this.mediaPlayer != null;
    }

    public void setAudioSpeed(float f) {
        this.audioSpeed = f;
        PlaybackParams playbackParams = new PlaybackParams();
        playbackParams.setSpeed(f);
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.setPlaybackParams(playbackParams);
        }
    }

    public float getPlayerSpeed() {
        return this.mediaPlayer.getPlaybackParams().getSpeed();
    }

    public void onCompleted() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.setOnCompletionListener(this);
        }
    }

    public void SetPosition(int i) {
        this.pos = i;
    }

    public void setTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppPref.getAudioTimerTime() != 0) {
                    AudioService.this.stopPlaying();
                    AudioService.this.stopForeground(true);
                    AudioService.this.stopSelf();
                    AppPref.setAudioTimerTime(0);
                    Intent intent = new Intent(AppConstants.MAIN_ACTIVITY_RECEIVER);
                    intent.putExtra(NotificationCompat.CATEGORY_STATUS, AppConstants.STOP);
                    AudioService.this.sendBroadcast(intent);
                }
            }
        }, AppPref.getAudioTimerTime());
    }

    public void setWaitTimer() {
        this.waitTimer = new CountDownTimer(AppPref.getAudioTimerTime(), 1000) {
            @Override
            public void onTick(long j) {
            }

            @Override
            public void onFinish() {
                if (AppPref.getAudioTimerTime() != 0) {
                    AudioService.this.stopPlaying();
                    AudioService.this.stopForeground(true);
                    AppPref.setAudioTimerTime(0);
                    Intent intent = new Intent(AppConstants.MAIN_ACTIVITY_RECEIVER);
                    intent.putExtra(NotificationCompat.CATEGORY_STATUS, AppConstants.STOP);
                    AudioService.this.sendBroadcast(intent);
                }
            }
        }.start();
    }

    public void stopTimer() {
        CountDownTimer countDownTimer = this.waitTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            this.waitTimer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer2) {
        Log.d("TAG", "onCompletion: " + AppPref.getAudioState());
        ActionPlaying actionPlaying2 = this.actionPlaying;
        if (actionPlaying2 != null) {
            this.pos = actionPlaying2.nextBtnClick();
        } else {
            this.pos = 0;
            createMediaPlayer(0);
            start();
            onCompleted();
        }
        Intent intent = new Intent(AppConstants.MAIN_ACTIVITY_RECEIVER);
        intent.putExtra(NotificationCompat.CATEGORY_STATUS, "RUNNING");
        intent.putExtra("position", this.pos);
        sendBroadcast(intent);
    }

    public void SetNewListModel(AudioVideoModal audioVideoModal) {
        int indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(audioVideoModal.getUri()));
        this.pos = indexOf;
        if (indexOf == -1) {
            this.pos = 0;
        }
        stopPlaying();
        Log.d("TAG", "onStartCommand: FromService");
        createMediaPlayer(this.pos);
        this.mediaPlayer.start();
    }

    public void stopPlaying() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.stop();
            this.mediaPlayer.reset();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    public void SetNullPlayer() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer = null;
        }
    }


    public void setCallBack(ActionPlaying actionPlaying2) {
        this.actionPlaying = actionPlaying2;
    }

    @Override
    public void onDestroy() {
        Log.d("AudioService", "Service onDestroy");
        super.onDestroy();
        BroadcastReceiver broadcastReceiver = this.mReceiver;
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (headSetReceiver != null) {
            try {
                unregisterReceiver(headSetReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            headSetReceiver = null;
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("AudioService", "Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("AudioService", "Service onRebind");
        super.onRebind(intent);
    }

    private void registerReceiver() {
        this.mReceiver = new MyBroadCastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.ACTION_CHANGE_SONG);
        intentFilter.addAction(AppConstants.PREVIOUS);
        intentFilter.addAction(AppConstants.NEXT);
        intentFilter.addAction(AppConstants.PLAY_PAUSE);
        intentFilter.addAction(AppConstants.CLOSE);
        intentFilter.addAction(AppConstants.CREATE);
        intentFilter.addAction(AppConstants.CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(this.mReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(this.mReceiver, intentFilter);
        }

    }

    public class MyBroadCastReceiver extends BroadcastReceiver {
        public MyBroadCastReceiver() {
        }

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
                case 45095867:
                    if (action.equals(AppConstants.ACTION_CHANGE_SONG)) {
                        c = 3;
                        break;
                    }
                    break;
                case 65203672:
                    if (action.equals(AppConstants.CLOSE)) {
                        c = 4;
                        break;
                    }
                    break;
                case 1669513305:
                    if (action.equals(AppConstants.CONTENT)) {
                        c = 5;
                        break;
                    }
                    break;
                case 1996002556:
                    if (action.equals(AppConstants.CREATE)) {
                        c = 6;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    if (AppPref.getBgAudioList().size() > 0) {
                        AudioService.this.previousButton();
                        return;
                    }
                    Intent intent2 = new Intent();
                    intent2.setAction(AppConstants.CLOSE);
                    AudioService.this.sendBroadcast(intent2);
                    return;
                case 1:
                    if (TextUtils.isEmpty(intent.getStringExtra("phone_state"))) {
                        AudioService.this.playPauseClick();
                        return;
                    } else if (AudioService.this.mediaPlayer.isPlaying()) {
                        AudioService.this.playPauseClick();
                        return;
                    } else {
                        return;
                    }
                case 2:
                    if (AppPref.getBgAudioList().size() > 0) {
                        AudioService.this.nextButton();
                        return;
                    }
                    Intent intent3 = new Intent();
                    intent3.setAction(AppConstants.CLOSE);
                    AudioService.this.sendBroadcast(intent3);
                    return;
                case 3:
                    int intExtra = intent.getIntExtra("playPosition", 0);
                    if (intent.getBooleanExtra("isFromBgVideo", false)) {
                        AudioService.this.pos = intExtra;
                        int intExtra2 = intent.getIntExtra("seek", 0);
                        AudioService.this.stopPlaying();
                        AudioService audioService = AudioService.this;
                        audioService.createMediaPlayer(audioService.pos);
                        AudioService.this.mediaPlayer.start();
                        AudioService.this.mediaPlayer.seekTo(intExtra2);
                        return;
                    }
                    AudioService.this.pos = intExtra;
                    int intExtra3 = intent.getIntExtra("seek", 0);
                    AudioService.this.stopPlaying();
                    AudioService audioService2 = AudioService.this;
                    audioService2.createMediaPlayer(audioService2.pos);
                    AudioService.this.mediaPlayer.start();
                    AudioService.this.mediaPlayer.seekTo(intExtra3);
                    return;
                case 4:
                    AudioService.this.stopPlaying();
                    AudioService.this.stopForeground(true);
                    AudioService.this.stopSelf();
                    AudioService.this.CloseService();
                    return;
                case 5:
                    if (AppPref.getBgAudioList().size() > 0) {
                        AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(AudioService.this.pos);
                        if (TextUtils.isEmpty(audioVideoModal.getArtist()) || TextUtils.isEmpty(audioVideoModal.getAlbum())) {
                            Intent intent4 = new Intent(AudioService.this.getApplicationContext(), BgVideoPlayerActivity.class);
                            intent4.putExtra("modal", AppPref.getBgAudioList().get(AudioService.this.pos));
                            intent4.putExtra("seek", AudioService.this.getCurrentPosition());
                            intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            AudioService.this.startActivity(intent4);
                            AudioService.this.stopPlaying();
                        } else {
                            Intent intent5 = new Intent(AudioService.this.getApplicationContext(), AudioPlayerActivity.class);
                            intent5.putExtra("modal", AppPref.getBgAudioList().get(AudioService.this.pos));
                            intent5.putExtra("seek", AudioService.this.getCurrentPosition());
                            intent5.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            AudioService.this.startActivity(intent5);
                        }
                        AppPref.setFullScreenPlay(true);
                        return;
                    }
                    Intent intent6 = new Intent();
                    intent6.setAction(AppConstants.CLOSE);
                    AudioService.this.sendBroadcast(intent6);
                    return;
                case 6:
                    int intExtra4 = intent.getIntExtra("pos", 0);
                    int intExtra5 = intent.getIntExtra("action", 2);
                    long longExtra = intent.getLongExtra("seek", 0);
                    AudioService.this.stopPlaying();
                    AudioService.this.createMediaPlayer(intExtra4);
                    if (intExtra5 == 1) {
                        AudioService.this.pause();
                    } else {
                        AudioService.this.start();
                    }
                    AudioService.this.seekTo((int) longExtra);
                    return;
                default:
                    return;
            }
        }
    }

    public KeyEvent handleIntent(MediaSession mediaSession2, Intent intent) {
        if (mediaSession2 == null || intent == null || !"android.intent.action.MEDIA_BUTTON".equals(intent.getAction()) || !intent.hasExtra("android.intent.extra.KEY_EVENT")) {
            return null;
        }
        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
        mediaSession2.getController().dispatchMediaButtonEvent(keyEvent);
        return keyEvent;
    }

    public class HeadSetReceiver extends BroadcastReceiver {
        public int state;

        public HeadSetReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.HEADSET_PLUG")) {
                if (!AudioService.this.isFromMain) {
                    int intExtra = intent.getIntExtra("state", -1);
                    this.state = intExtra;
                    if (intExtra == 0) {
                        if (AppConstants.isMyServiceRunning(context, AudioService.class) && AudioService.this.mediaPlayer != null && AudioService.this.mediaPlayer.isPlaying()) {
                            Intent intent2 = new Intent();
                            intent2.setAction(AppConstants.PLAY_PAUSE);
                            context.sendBroadcast(intent2);
                        }
                        Log.d("TAG", "onReceive: Headphone Detached");
                    } else if (intExtra == 1) {
                        Log.d("TAG", "onReceive: Headphone Attached");
                    }
                }
                AudioService.this.isFromMain = false;
            }
        }
    }
}
