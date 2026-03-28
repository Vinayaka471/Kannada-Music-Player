package com.demo.musicvideoplayer.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.internal.view.SupportMenu;
import androidx.work.WorkRequest;
import com.bumptech.glide.Glide;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.activities.VideoPlayerActivity;
import com.demo.musicvideoplayer.model.VideoModal;
import com.demo.musicvideoplayer.utils.AppConstants;
import com.demo.musicvideoplayer.utils.AppPref;
import java.io.IOException;

public class VideoService extends Service implements GestureDetector.OnGestureListener, TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final String CHANNEL_ID = "CHANNEL_ID";
    public WindowManager windowManager;
    FrameLayout aspectRatioFrameLayout;
    NotificationCompat.Builder builder;
    GestureDetector gestureDetector;
    Handler handler = new Handler();
    ImageView imgNext;
    LayoutInflater inflater;
    boolean isPlaying = true;
    LinearLayout llBottom;
    MediaPlayer mediaPlayer;
    VideoModal modal;
    NotificationManager notificationManager;
    TextureView playerView;
    int position;
    RelativeLayout rlMain;
    long runningDuration = 0;
    long seekTo = 0;
    String userAgent;
    View videoView;

    private void setCompletionListener() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.notificationManager = (NotificationManager) getSystemService("notification");
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("CHANNEL_ID", getPackageName(), NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound((Uri) null, (AudioAttributes) null);
            this.notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder autoCancel = new NotificationCompat.Builder((Context) this, "CHANNEL_ID").setSmallIcon((int) R.drawable.notification).setContentText("Playing in pop-up window.").setAutoCancel(true);
        this.builder = autoCancel;
        //startForeground(1, autoCancel.build());
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, builder.build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, builder.build());
        }


        final WindowManager.LayoutParams layoutParams;
        MediaPlayer mediaPlayer2;
        this.gestureDetector = new GestureDetector(this, this);
        this.modal = (VideoModal) intent.getParcelableExtra("modal");
        this.seekTo = intent.getLongExtra("SeekTo", 0);
        Uri.parse(this.modal.getaPath());
        this.position = AppPref.getPopupVideoList().indexOf(this.modal);
        if (!(windowManager == null || (mediaPlayer2 = this.mediaPlayer) == null)) {
            this.videoView = null;
            windowManager = null;
            mediaPlayer2.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
        this.inflater = (LayoutInflater) getSystemService("layout_inflater");
        windowManager = (WindowManager) getSystemService("window");
        this.videoView = this.inflater.inflate(R.layout.popup_video, (ViewGroup) null);
        if (Build.VERSION.SDK_INT < 26) {
            layoutParams = new WindowManager.LayoutParams(-2, -2, 2002, 8, -3);
        } else {
            layoutParams = new WindowManager.LayoutParams(-2, -2, 2038, 8, -3);
        }
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.x = 200;
        layoutParams.y = 200;
        windowManager.addView(this.videoView, layoutParams);
        this.playerView = (TextureView) this.videoView.findViewById(R.id.exoplayerView);
        this.llBottom = (LinearLayout) this.videoView.findViewById(R.id.llBottom);
        final ImageView imageView = (ImageView) this.videoView.findViewById(R.id.imgPlayPause);
        this.imgNext = (ImageView) this.videoView.findViewById(R.id.imgNext);
        this.rlMain = (RelativeLayout) this.videoView.findViewById(R.id.rlMain);
        this.aspectRatioFrameLayout = (FrameLayout) this.videoView.findViewById(R.id.rlPopup);
        ((ImageView) this.videoView.findViewById(R.id.imgViewDismiss)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VideoService.this.videoView != null) {
                    windowManager.removeView(VideoService.this.videoView);
                }
                VideoService.this.releasePlayer();
                VideoService.this.stopForeground(true);
                VideoService.this.stopSelf();
                VideoService.this.handler.removeCallbacksAndMessages((Object) null);
            }
        });
        ((ImageView) this.videoView.findViewById(R.id.imgViewMaximise)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VideoService.this.videoView != null) {
                    windowManager.removeView(VideoService.this.videoView);
                }
                if (windowManager != null) {
                    windowManager = null;
                }
                VideoService.this.stopForeground(true);
                VideoService.this.stopSelf();
                Intent intent = new Intent(VideoService.this, VideoPlayerActivity.class);
                intent.putExtra("VideoModel", AppPref.getPopupVideoList().get(VideoService.this.position));
                if (AppPref.getPlayedFolder() != null) {
                    intent.putExtra("FolderModel", AppPref.getPlayedFolder());
                }
                intent.putExtra("SeekTo", VideoService.this.runningDuration);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                VideoService.this.startActivity(intent);
                if (VideoService.this.mediaPlayer != null) {
                    VideoService.this.mediaPlayer.stop();
                    VideoService.this.mediaPlayer.release();
                    VideoService.this.mediaPlayer = null;
                }
                VideoService.this.handler.removeCallbacksAndMessages((Object) null);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VideoService.this.isPlaying) {
                    VideoService.this.isPlaying = false;
                    Glide.with(VideoService.this.getApplicationContext()).load(Integer.valueOf(R.drawable.play_white)).into(imageView);
                    if (VideoService.this.mediaPlayer != null) {
                        VideoService.this.mediaPlayer.pause();
                        return;
                    }
                    return;
                }
                Glide.with(VideoService.this.getApplicationContext()).load(Integer.valueOf(R.drawable.pause_white)).into(imageView);
                VideoService.this.isPlaying = true;
                if (VideoService.this.mediaPlayer != null) {
                    VideoService.this.mediaPlayer.start();
                }
            }
        });
        this.imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoService videoService = VideoService.this;
                videoService.position = videoService.setNextRepetition();
                VideoService.this.playVideo(Uri.parse(AppPref.getPopupVideoList().get(VideoService.this.position).getaPath()), false);
                AppConstants.SetVideoHistory(AppPref.getPopupVideoList().get(VideoService.this.position));
            }
        });
        ((ImageView) this.videoView.findViewById(R.id.imgPrevious)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoService videoService = VideoService.this;
                videoService.position = videoService.setPreviousRepetition();
                VideoService.this.playVideo(Uri.parse(AppPref.getPopupVideoList().get(VideoService.this.position).getaPath()), false);
                AppConstants.SetVideoHistory(AppPref.getPopupVideoList().get(VideoService.this.position));
            }
        });
        ((ImageView) this.videoView.findViewById(R.id.btn10Backward)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long currentPosition = ((long) VideoService.this.mediaPlayer.getCurrentPosition()) - WorkRequest.MIN_BACKOFF_MILLIS;
                if (currentPosition >= 0) {
                    VideoService.this.mediaPlayer.seekTo((int) currentPosition);
                    return;
                }
                VideoService videoService = VideoService.this;
                videoService.position = videoService.setPreviousRepetition();
                VideoService.this.playVideo(Uri.parse(AppPref.getPopupVideoList().get(VideoService.this.position).getaPath()), false);
            }
        });
        ((ImageView) this.videoView.findViewById(R.id.btn10Forward)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long currentPosition = ((long) VideoService.this.mediaPlayer.getCurrentPosition()) + WorkRequest.MIN_BACKOFF_MILLIS;
                if (currentPosition <= ((long) VideoService.this.mediaPlayer.getDuration())) {
                    VideoService.this.mediaPlayer.seekTo((int) currentPosition);
                    return;
                }
                VideoService videoService = VideoService.this;
                videoService.position = videoService.setNextRepetition();
                VideoService.this.playVideo(Uri.parse(AppPref.getPopupVideoList().get(VideoService.this.position).getaPath()), false);
            }
        });
        this.mediaPlayer = new MediaPlayer();
        this.playerView.setSurfaceTextureListener(this);
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnCompletionListener(this);
        this.videoView.findViewById(R.id.rlPopup).setOnTouchListener(new View.OnTouchListener() {
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                VideoService.this.gestureDetector.onTouchEvent(motionEvent);
                int action = motionEvent.getAction();
                if (action != 0) {
                    if (action != 1) {
                        if (action != 2) {
                            return false;
                        }
                        layoutParams.x = this.initialX + ((int) (motionEvent.getRawX() - this.initialTouchX));
                        layoutParams.y = this.initialY + ((int) (motionEvent.getRawY() - this.initialTouchY));
                        windowManager.updateViewLayout(VideoService.this.videoView, layoutParams);
                    }
                    return true;
                }
                this.initialX = layoutParams.x;
                this.initialY = layoutParams.y;
                this.initialTouchX = motionEvent.getRawX();
                this.initialTouchY = motionEvent.getRawY();
                return true;
            }
        });
        return Service.START_REDELIVER_INTENT;
    }

    private void setSeeking() {
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                VideoService videoService = VideoService.this;
                videoService.runningDuration = (long) videoService.mediaPlayer.getCurrentPosition();
                VideoService.this.handler.postDelayed(this, 10);
            }
        }, 10);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        } else {
            this.mediaPlayer = new MediaPlayer();
        }
        this.mediaPlayer.setSurface(surface);
        playVideo(Uri.parse(this.modal.getaPath()), true);
        setSeeking();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer2) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            this.mediaPlayer.seekTo((int) this.seekTo);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer2) {
        this.position = setNextRepetition();
        playVideo(Uri.parse(AppPref.getPopupVideoList().get(this.position).getaPath()), false);
        setSeeking();
    }

    public void playVideo(Uri uri, boolean z) {
        resetPlayer();
        try {
            this.mediaPlayer.setScreenOnWhilePlaying(true);
            this.mediaPlayer.setDataSource(this, uri);
            this.mediaPlayer.setScreenOnWhilePlaying(true);
            this.mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        rotateScreen(uri);
    }

    
    public int setNextRepetition() {
        String videoState = AppPref.getVideoState();
        videoState.hashCode();
        char c = 65535;
        switch (videoState.hashCode()) {
            case -1974417883:
                if (videoState.equals(AppConstants.LOOP_ALL)) {
                    c = 0;
                    break;
                }
                break;
            case 76453678:
                if (videoState.equals(AppConstants.ORDER)) {
                    c = 1;
                    break;
                }
                break;
            case 2090883002:
                if (videoState.equals(AppConstants.SHUFFLE_ALL)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                this.position = (this.position + 1) % AppPref.getPopupVideoList().size();
                break;
            case 1:
                if (this.position + 1 <= AppPref.getPopupVideoList().size()) {
                    this.position++;
                    break;
                } else {
                    View view = this.videoView;
                    if (view != null) {
                        windowManager.removeView(view);
                    }
                    releasePlayer();
                    stopForeground(true);
                    stopSelf();
                    break;
                }
            case 2:
                this.position = AppConstants.GetRandomPosition(AppPref.getPopupVideoList().size() - 1);
                break;
        }
        return this.position;
    }

    
    public int setPreviousRepetition() {
        String videoState = AppPref.getVideoState();
        videoState.hashCode();
        char c = 65535;
        switch (videoState.hashCode()) {
            case -1974417883:
                if (videoState.equals(AppConstants.LOOP_ALL)) {
                    c = 0;
                    break;
                }
                break;
            case 76453678:
                if (videoState.equals(AppConstants.ORDER)) {
                    c = 1;
                    break;
                }
                break;
            case 2090883002:
                if (videoState.equals(AppConstants.SHUFFLE_ALL)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                int i = this.position;
                if (i - 1 < 0) {
                    i = AppPref.getPopupVideoList().size();
                }
                this.position = i - 1;
                break;
            case 1:
                int i2 = this.position;
                if (i2 - 1 >= 0) {
                    this.position = i2 - 1;
                    break;
                } else {
                    View view = this.videoView;
                    if (view != null) {
                        windowManager.removeView(view);
                    }
                    releasePlayer();
                    stopForeground(true);
                    stopSelf();
                    break;
                }
            case 2:
                this.position = AppConstants.GetRandomPosition(AppPref.getPopupVideoList().size() - 1);
                break;
        }
        return this.position;
    }

    private void rotateScreen(Uri uri) {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(getApplicationContext(), uri);
            Bitmap frameAtTime = mediaMetadataRetriever.getFrameAtTime();
            int width = frameAtTime.getWidth();
            int height = frameAtTime.getHeight();
            if (width > height) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                int i = displayMetrics.widthPixels;
                int i2 = displayMetrics.heightPixels;
                ViewGroup.LayoutParams layoutParams = this.aspectRatioFrameLayout.getLayoutParams();
                layoutParams.width = 700;
                layoutParams.height = 400;
                this.aspectRatioFrameLayout.requestLayout();
                this.aspectRatioFrameLayout.setLayoutParams(layoutParams);
                windowManager.updateViewLayout(this.videoView, layoutParams);
            } else if (width < height) {
                ViewGroup.LayoutParams layoutParams2 = this.aspectRatioFrameLayout.getLayoutParams();
                layoutParams2.width = 400;
                layoutParams2.height = 700;
                this.aspectRatioFrameLayout.requestLayout();
                this.aspectRatioFrameLayout.setLayoutParams(layoutParams2);
                windowManager.updateViewLayout(this.videoView, layoutParams2);
            }
        } catch (RuntimeException unused) {
            Log.e("MediaMetadataRetriever", "- Failed to rotate the video");
        }
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    private void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        } else {
            this.mediaPlayer = new MediaPlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        if (this.rlMain.getVisibility() == View.VISIBLE) {
            this.rlMain.setVisibility(View.GONE);
        } else {
            this.rlMain.setVisibility(View.VISIBLE);
        }
        return false;
    }
}
