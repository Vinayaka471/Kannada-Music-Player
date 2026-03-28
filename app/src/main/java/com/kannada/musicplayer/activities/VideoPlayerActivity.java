package com.kannada.musicplayer.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.adapter.BottomVideoListAdapter;
import com.kannada.musicplayer.adapter.PlayerIconAdapter;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.databinding.ActivityVideoPlayerBinding;
import com.kannada.musicplayer.databinding.BottomsheetBrightnessBinding;
import com.kannada.musicplayer.databinding.BottomsheetSpeedBinding;
import com.kannada.musicplayer.databinding.BottomsheetTimerBinding;
import com.kannada.musicplayer.databinding.BottomsheetVideoPlaylistBinding;
import com.kannada.musicplayer.databinding.BottomsheetVolumeBinding;
import com.kannada.musicplayer.model.IconModel;
import com.kannada.musicplayer.model.VideoFolderModal;
import com.kannada.musicplayer.model.VideoModal;
import com.kannada.musicplayer.scalable.ScalableType;
import com.kannada.musicplayer.scalable.ScaleManager;
import com.kannada.musicplayer.scalable.Size;
import com.kannada.musicplayer.service.VideoService;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;
import com.kannada.musicplayer.utils.BetterActivityResult;
import com.kannada.musicplayer.utils.PlayerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, TextureView.SurfaceTextureListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final int SWIPE_THRESHOLD = 50;
    int MAX_VIDEO_STEP_SIZE = 60000;
    String Orientation = AppConstants.AUTO_ROTATE;
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    AudioManager audioManager;
    float baseX;
    float baseY;
    ActivityVideoPlayerBinding binding;
    BottomSheetDialog bottomSheetDialog;
    BottomVideoListAdapter bottomVideoListAdapter;
    int device_height;
    int device_width;
    long diffX;
    long diffY;
    String displayName;
    GestureDetector gestureDetector;
    Handler handler = new Handler();
    SurfaceHolder holder;
    List<IconModel> iconModelList = new ArrayList();
    String id;
    boolean isDoubleTap = false;
    boolean isExpanded = false;
    boolean isFlip = false;
    boolean isFromCopyFile = false;
    boolean isLeft = false;
    boolean isLock = false;
    boolean isMute = false;
    boolean isPlaying = true;
    boolean isScaling = false;
    boolean isViewsVisible = false;
    boolean left = false;
    protected float mBrightness;
    GestureDetector mGestureDetector;
    protected ScalableType mScalableType = ScalableType.FIT_CENTER;
    protected int mStreamVolume;
    MediaController mediaController;
    MediaPlayer mediaPlayer;
    ObjectAnimator moveAnim;
    PlayerIconAdapter playerIconAdapter;
    PlaybackParams playerSpeed;
    CountDownTimer playerTimer;
    float playerVolume = 0.0f;
    BottomsheetVideoPlaylistBinding playlistBinding;
    int position = 0;
    boolean right = false;
    long runningDuration = 0;
    ScaleGestureDetector scaleGestureDetector;
    String scaleType = AppConstants.FIT;
    float scale_factor = 1.0f;
    long seekTo = 0;
    Animation slide_down;
    
    public MotionEvent startEvent;
    int startVideoTime = -1;
    
    public SwipeEventType swipeEventType = SwipeEventType.NONE;
    int tempPos = -1;
    long totalDuration = 0;
    String userAgent;
    public List<VideoFolderModal> videoFolderList = new ArrayList();
    VideoFolderModal videoFolderModal;
    VideoModal videoModal;
    List<VideoModal> videoModalList = new ArrayList();
    double videoSpeed = 25.0d;
    CountDownTimer waitTimer;

    private enum SwipeEventType {
        NONE,
        BRIGHTNESS,
        VOLUME,
        SEEK,
        COMMENTS
    }


    public long getMilli(int i) {
        return (long) (i * 60000);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    private void applyDisplayCutouts() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rl), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());

            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    public void onCreate(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        Window window = getWindow();
        window.setFlags(134217728, 134217728);
        window.setFlags(67108864, 67108864);
        window.addFlags(128);
        super.onCreate(bundle);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
        this.binding = (ActivityVideoPlayerBinding) DataBindingUtil.setContentView(this, R.layout.activity_video_player);

        applyDisplayCutouts();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        this.audioManager = (AudioManager) getSystemService("audio");
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        this.binding.progressBar.setVisibility(View.GONE);
        if (("android.intent.action.SEND".equals(action) || "android.intent.action.VIEW".equals(action)) && type != null) {
            Uri data = intent.getData();
            this.videoModal = new VideoModal();
            if ("video".equals(type.substring(0, type.lastIndexOf("/")))) {
                if ("android.intent.action.VIEW".equals(action)) {
                    data = intent.getData();
                }
                Log.d("GURI", "onCreate: " + data);
                Log.d("URII", "onCreate: " + intent.getData().getEncodedPath() + "  || " + intent.getData().getPath());
                intent.getData().getPath();
                StringBuilder sb = new StringBuilder();
                sb.append("onCreate: ");
                sb.append(getFileName(data));
                Log.d("TAG", sb.toString());
                if (data.getScheme() != null && data.getScheme().equals("content")) {
                    Cursor query = getContentResolver().query(data, (String[]) null, (String) null, (String[]) null, (String) null);
                    if (query != null) {
                        try {
                            if (query.moveToFirst()) {
                                String string = query.getString(query.getColumnIndex("_display_name"));
                                this.displayName = string;
                                this.videoModal.setaName(string);
                                this.videoModal.setaPath(data.toString());
                            }
                        } catch (Throwable th) {
                            query.close();
                            throw th;
                        }
                    }
                    query.close();
                }
                if (this.displayName == null) {
                    String path = data.getPath();
                    this.displayName = path;
                    int lastIndexOf = path.lastIndexOf(47);
                    if (lastIndexOf != -1) {
                        String substring = this.displayName.substring(lastIndexOf + 1);
                        this.displayName = substring;
                        this.videoModal.setaName(substring);
                        this.videoModal.setaPath(data.getPath());
                    }
                }
                this.videoModalList.add(this.videoModal);
                AppPref.setPlayedFolder((VideoFolderModal) null);
            }
        } else {
            this.videoModal = (VideoModal) getIntent().getParcelableExtra("VideoModel");
            VideoFolderModal videoFolderModal2 = (VideoFolderModal) getIntent().getParcelableExtra("FolderModel");
            if (videoFolderModal2 != null) {
                this.videoModalList = videoFolderModal2.getVideoList();
                AppPref.setPlayedFolder(videoFolderModal2);
            } else {
                this.videoModalList.add(this.videoModal);
                AppPref.setPlayedFolder((VideoFolderModal) null);
            }
            this.seekTo = getIntent().getLongExtra("SeekTo", 0);
        }
        AppPref.setPopupVideoList(this.videoModalList);
        setPlayerIconList();
        setVideo();
        setExoplayerTouchListener();
        this.gestureDetector = new GestureDetector(this, this);
        this.scaleGestureDetector = new ScaleGestureDetector(this, new ScaleDetector());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.device_width = displayMetrics.widthPixels;
        this.device_height = displayMetrics.heightPixels;
        this.mBrightness = PlayerUtils.scanForActivity(this).getWindow().getAttributes().screenBrightness;
        this.mGestureDetector = new GestureDetector(this, new MyGestureListener());
        this.mStreamVolume = this.audioManager.getStreamVolume(3);
        this.mBrightness = getWindow().getAttributes().screenBrightness;
        Log.d("TAG", "slideToChangeBrightness: " + this.mBrightness);
        if (((double) this.mBrightness) > 0.5d) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.screenBrightness = 0.5f;
            getWindow().setAttributes(attributes);
        }
        slideToChangeBrightness(0.0f);
        slideToChangeVolume(0.0f);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.binding.frame, "Y", new float[]{2000.0f});
        this.moveAnim = ofFloat;
        ofFloat.setDuration(4000);
        this.moveAnim.setInterpolator(new BounceInterpolator());
        setWaitTimer(2000);
    }

    public String getFileName(Uri uri) {
        String str = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor query = getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (query != null) {
                try {
                    if (query.moveToFirst()) {
                        str = query.getString(query.getColumnIndex("_display_name"));
                    }
                } catch (Throwable th) {
                    query.close();
                    throw th;
                }
            }
            query.close();
        }
        if (str != null) {
            return str;
        }
        String path = uri.getPath();
        int lastIndexOf = path.lastIndexOf(47);
        return lastIndexOf != -1 ? path.substring(lastIndexOf + 1) : path;
    }

    private void setVideo() {
        Uri.parse(this.videoModal.getaPath());
        try {
            this.totalDuration = AppConstants.getDuration(this, this.videoModal.getaPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.binding.txtTotalTime.setText(AppConstants.timeFormat(this.totalDuration));
        this.binding.seekBar.setMax((int) this.totalDuration);
        this.position = this.videoModalList.indexOf(this.videoModal);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.screenBrightness = AppPref.getScreenBrightness();
        getWindow().setAttributes(attributes);
        MediaPlayer mediaPlayer2 = new MediaPlayer();
        this.mediaPlayer = mediaPlayer2;
        mediaPlayer2.setOnVideoSizeChangedListener(this);
        this.binding.surfaceView.setSurfaceTextureListener(this);
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnCompletionListener(this);
        Clicks();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer2, int i, int i2) {
        scaleVideoSize(i, i2);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.reset();
        } else {
            this.mediaPlayer = new MediaPlayer();
        }
        this.mediaPlayer.setSurface(surface);
        setPlayer(Uri.parse(this.videoModal.getaPath()));
        setPlayerSeekBar();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d("TAG", "checkOverlayPermission: onSurfaceTextureDestroyed");
        releasePlayer();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer2) {
        MediaPlayer mediaPlayer3 = this.mediaPlayer;
        if (mediaPlayer3 != null) {
            mediaPlayer3.start();
            this.mediaPlayer.seekTo((int) this.seekTo);
            this.playerSpeed = this.mediaPlayer.getPlaybackParams();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer2) {
        nextBtnClick();
    }

    public void setPlayer(Uri uri) {
        resetPlayer();
        try {
            MediaPlayer mediaPlayer2 = this.mediaPlayer;
            if (mediaPlayer2 != null) {
                mediaPlayer2.setScreenOnWhilePlaying(true);
                this.mediaPlayer.setDataSource(this, uri);
                this.mediaPlayer.setScreenOnWhilePlaying(true);
                this.mediaPlayer.prepare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.binding.imgPlayPause.setImageResource(R.drawable.pause);
        this.binding.txtTitle.setText(this.videoModalList.get(this.position).getaName());
        AppConstants.SetVideoHistory(this.videoModalList.get(this.position));
    }

    private void updateTextureViewSize(String str) {
        float f;
        char c;
        int i;
        float width = (float) this.binding.surfaceView.getWidth();
        float height = (float) this.binding.surfaceView.getHeight();
        int videoWidth = this.mediaPlayer.getVideoWidth();
        int videoHeight = this.mediaPlayer.getVideoHeight();
        float f2 = (float) videoWidth;
        float f3 = 1.0f;
        if (f2 > width) {
            float f4 = (float) videoHeight;
            if (f4 > height) {
                f3 = f2 / width;
                f = f4 / height;
                str.hashCode();
                c = 65535;
                int i2 = 0;
                switch (str.hashCode()) {
                    case -440887238:
                        if (str.equals("CENTER_CROP")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 83253:
                        if (str.equals("TOP")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1965067819:
                        if (str.equals("BOTTOM")) {
                            c = 2;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        i2 = (int) (width / 2.0f);
                        height /= 2.0f;
                        break;
                    case 2:
                        i2 = (int) width;
                        break;
                    default:
                        i = 0;
                        break;
                }
                i = (int) height;
                Matrix matrix = new Matrix();
                matrix.setScale(f3, f, (float) i2, (float) i);
                this.binding.surfaceView.setTransform(matrix);
            }
        }
        if (f2 < width) {
            float f5 = (float) videoHeight;
            if (f5 < height) {
                f3 = height / f5;
                f = width / f2;
                str.hashCode();
                c = 65535;
                int i22 = 0;
                switch (str.hashCode()) {
                    case -440887238:
                        break;
                    case 83253:
                        break;
                    case 1965067819:
                        break;
                }
                switch (c) {
                    case 0:
                        break;
                    case 2:
                        break;
                }
                i = (int) height;
                Matrix matrix2 = new Matrix();
                matrix2.setScale(f3, f, (float) i22, (float) i);
                this.binding.surfaceView.setTransform(matrix2);
            }
        }
        if (width > f2) {
            f = (width / f2) / (height / ((float) videoHeight));
        } else {
            float f6 = (float) videoHeight;
            if (height > f6) {
                float f7 = (height / f6) / (width / f2);
                f = 1.0f;
                f3 = f7;
            } else {
                f = 1.0f;
            }
        }
        str.hashCode();
        c = 65535;
        int i222 = 0;
        switch (str.hashCode()) {
            case -440887238:
                break;
            case 83253:
                break;
            case 1965067819:
                break;
        }
        switch (c) {
            case 0:
                break;
            case 2:
                break;
        }
        i = (int) height;
        Matrix matrix22 = new Matrix();
        matrix22.setScale(f3, f, (float) i222, (float) i);
        this.binding.surfaceView.setTransform(matrix22);
    }

    private void scaleVideoSize(int i, int i2) {
        Matrix scaleMatrix;
        if (i != 0 && i2 != 0 && (scaleMatrix = new ScaleManager(new Size(this.binding.surfaceView.getWidth(), this.binding.surfaceView.getHeight()), new Size(i, i2)).getScaleMatrix(this.mScalableType)) != null) {
            this.binding.surfaceView.setTransform(scaleMatrix);
        }
    }

    public void setScalableType(ScalableType scalableType) {
        this.mScalableType = scalableType;
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            scaleVideoSize(mediaPlayer2.getVideoWidth(), this.mediaPlayer.getVideoHeight());
        }
    }

    private void setTextureViewSize() {
        double d;
        getWindowManager().getDefaultDisplay();
        int videoWidth = this.mediaPlayer.getVideoWidth();
        int videoHeight = this.mediaPlayer.getVideoHeight();
        int i = this.device_width;
        if (videoWidth > i || videoHeight > this.device_height) {
            float f = (float) videoHeight;
            float f2 = f / ((float) this.device_height);
            float f3 = (float) videoWidth;
            float f4 = f3 / ((float) i);
            if (f2 > 1.0f || f4 > 1.0f) {
                if (f2 > f4) {
                    videoHeight = (int) Math.ceil((double) (f / f2));
                    d = Math.ceil((double) (f3 / f2));
                } else {
                    videoHeight = (int) Math.ceil((double) (f / f4));
                    d = Math.ceil((double) (f3 / f4));
                }
                videoWidth = (int) d;
            }
        }
        Log.d("TAG", "onPrepared: " + videoHeight + " " + videoWidth);
        ViewGroup.LayoutParams layoutParams = this.binding.surfaceView.getLayoutParams();
        layoutParams.width = videoWidth;
        layoutParams.height = videoHeight;
        this.binding.surfaceView.requestLayout();
        this.binding.surfaceView.setLayoutParams(layoutParams);
    }

    
    public void setPlayerSeekBar() {
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    VideoPlayerActivity.this.binding.txtRunningTime.setText(AppConstants.timeFormat((long) VideoPlayerActivity.this.mediaPlayer.getCurrentPosition()));
                    VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                    videoPlayerActivity.runningDuration = (long) videoPlayerActivity.mediaPlayer.getCurrentPosition();
                    VideoPlayerActivity.this.binding.seekBar.setProgress(VideoPlayerActivity.this.mediaPlayer.getCurrentPosition());
                    if (((long) VideoPlayerActivity.this.mediaPlayer.getCurrentPosition()) == VideoPlayerActivity.this.totalDuration) {
                        VideoPlayerActivity.this.mediaPlayer.pause();
                    }
                }
                VideoPlayerActivity.this.handler.postDelayed(this, 10);
            }
        }, 10);
        this.binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (z) {
                    if (VideoPlayerActivity.this.mediaPlayer != null) {
                        VideoPlayerActivity.this.mediaPlayer.seekTo(i);
                    }
                    VideoPlayerActivity.this.stopTimer();
                    VideoPlayerActivity.this.setWaitTimer(2000);
                }
                VideoPlayerActivity.this.binding.seekBar.setProgress(i);
            }
        });
    }

    
    public void setRepeatImage() {
        this.playlistBinding.txtType.setText(AppPref.getVideoState());
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
            case -267444236:
                if (videoState.equals(AppConstants.REPEAT_CURRENT)) {
                    c = 1;
                    break;
                }
                break;
            case 76453678:
                if (videoState.equals(AppConstants.ORDER)) {
                    c = 2;
                    break;
                }
                break;
            case 2090883002:
                if (videoState.equals(AppConstants.SHUFFLE_ALL)) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                this.playlistBinding.imgState.setImageResource(R.drawable.loop);
                return;
            case 1:
                this.playlistBinding.imgState.setImageResource(R.drawable.repeat_current);
                return;
            case 2:
                this.playlistBinding.imgState.setImageResource(R.drawable.add_to_queue);
                return;
            case 3:
                this.playlistBinding.imgState.setImageResource(R.drawable.shuffle);
                return;
            default:
                return;
        }
    }

    public void nextBtnClick() {
        this.binding.surfaceView.destroyDrawingCache();
        this.seekTo = 0;
        notifyBottomAdapter(this.position, false);
        int nextRepetition = setNextRepetition();
        this.position = nextRepetition;
        if (nextRepetition == -1 || nextRepetition >= this.videoFolderList.size()) {
            releasePlayer();
            finish();
            return;
        }
        notifyBottomAdapter(this.position, true);
        try {
            this.totalDuration = AppConstants.getDuration(this, this.videoModalList.get(this.position).getaPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.binding.txtTotalTime.setText(AppConstants.timeFormat(this.totalDuration));
        this.binding.seekBar.setMax((int) this.totalDuration);
        setPlayer(Uri.parse(this.videoModalList.get(this.position).getaPath()));
        setPlayerSeekBar();
        this.binding.imgPlayPause.setImageResource(R.drawable.pause);
    }

    public void prevBtnClick() {
        this.seekTo = 0;
        notifyBottomAdapter(this.position, false);
        int previousRepetition = setPreviousRepetition();
        this.position = previousRepetition;
        if (previousRepetition == -1 || previousRepetition >= this.videoFolderList.size()) {
            releasePlayer();
            finish();
            return;
        }
        notifyBottomAdapter(this.position, true);
        try {
            this.totalDuration = AppConstants.getDuration(this, this.videoModalList.get(this.position).getaPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.binding.txtTotalTime.setText(AppConstants.timeFormat(this.totalDuration));
        this.binding.seekBar.setMax((int) this.totalDuration);
        setPlayer(Uri.parse(this.videoModalList.get(this.position).getaPath()));
        setPlayerSeekBar();
        this.binding.imgPlayPause.setImageResource(R.drawable.pause);
    }

    public void notifyBottomAdapter(int i, boolean z) {
        if (this.bottomVideoListAdapter != null && AppPref.getPopupVideoList() != null && AppPref.getPopupVideoList().contains(this.videoModalList.get(i))) {
            this.bottomVideoListAdapter.setPlayingPos(AppPref.getPopupVideoList().indexOf(this.videoModalList.get(i)), z);
        }
    }

    private int setNextRepetition() {
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
                this.position = (this.position + 1) % this.videoModalList.size();
                break;
            case 1:
                if (this.position + 1 > this.videoModalList.size()) {
                    if (this.mediaPlayer.isPlaying()) {
                        releasePlayer();
                        finish();
                        break;
                    }
                } else {
                    this.position++;
                    break;
                }
                break;
            case 2:
                this.position = AppConstants.GetRandomPosition(this.videoModalList.size() - 1);
                break;
        }
        return this.position;
    }

    private int setPreviousRepetition() {
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
                    i = this.videoModalList.size();
                }
                this.position = i - 1;
                break;
            case 1:
                int i2 = this.position;
                if (i2 - 1 >= 0) {
                    this.position = i2 - 1;
                    break;
                } else {
                    releasePlayer();
                    finish();
                    break;
                }
            case 2:
                this.position = AppConstants.GetRandomPosition(this.videoModalList.size() - 1);
                break;
        }
        return this.position;
    }

    private void setPlayerIconList() {
        this.iconModelList.add(new IconModel("right_back.png", ""));
        this.iconModelList.add(new IconModel("day.png", "Day"));
        this.iconModelList.add(new IconModel("volume_on.png", "Mute"));
        this.iconModelList.add(new IconModel("rotation.png", "Rotate"));
        this.playerIconAdapter = new PlayerIconAdapter(this, this.iconModelList, new PlayerIconAdapter.OnClick() {
            @Override
            public void onIconClick(int i) {
                int i2 = i;
                VideoPlayerActivity.this.stopTimer();
                VideoPlayerActivity.this.setWaitTimer(2000);
                IconModel iconModel = VideoPlayerActivity.this.iconModelList.get(i2);
                if (i2 == 0) {
                    if (VideoPlayerActivity.this.isExpanded) {
                        VideoPlayerActivity.this.iconModelList.clear();
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("right_back.png", ""));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("day.png", "Day"));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("volume_on.png", "Mute"));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("rotation.png", "Rotate"));
                        VideoPlayerActivity.this.playerIconAdapter.notifyDataSetChanged();
                        VideoPlayerActivity.this.isExpanded = false;
                        return;
                    }
                    if (VideoPlayerActivity.this.iconModelList.size() == 4) {
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("volume_on.png", "Volume"));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("brightness.png", "Brightness"));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("speed.png", "Speed"));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("mirror.png", "Mirror"));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("pop_up.png", "Pop-up Play"));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("bg_play.png", "Background Play"));
                        VideoPlayerActivity.this.iconModelList.add(new IconModel("playlist.png", "Playlist"));
                    }
                    VideoPlayerActivity.this.iconModelList.set(i2, new IconModel("left_back.png", ""));
                    VideoPlayerActivity.this.playerIconAdapter.notifyDataSetChanged();
                    VideoPlayerActivity.this.isExpanded = true;
                } else if (i2 == 1) {
                    if (VideoPlayerActivity.this.binding.nightView.getVisibility() == View.VISIBLE) {
                        VideoPlayerActivity.this.binding.nightView.setVisibility(View.GONE);
                        VideoPlayerActivity.this.iconModelList.set(i2, new IconModel("day.png", "Night"));
                    } else {
                        VideoPlayerActivity.this.binding.nightView.setVisibility(View.VISIBLE);
                        VideoPlayerActivity.this.iconModelList.set(i2, new IconModel("day.png", "Day"));
                    }
                    VideoPlayerActivity.this.playerIconAdapter.notifyDataSetChanged();
                } else if (i2 == 2) {
                    if (VideoPlayerActivity.this.isMute) {
                        VideoPlayerActivity.this.isMute = false;
                        VideoPlayerActivity.this.iconModelList.set(i2, new IconModel("volume_on.png", "Mute"));
                        VideoPlayerActivity.this.audioManager.setStreamVolume(3, VideoPlayerActivity.this.mStreamVolume, 0);
                    } else {
                        VideoPlayerActivity.this.iconModelList.set(i2, new IconModel("volume_off.png", "UnMute"));
                        VideoPlayerActivity.this.isMute = true;
                        VideoPlayerActivity.this.audioManager.setStreamVolume(3, 0, 0);
                    }
                    VideoPlayerActivity.this.playerIconAdapter.notifyDataSetChanged();
                } else if (i2 == 3) {
                    if (VideoPlayerActivity.this.Orientation.equals(AppConstants.PORTRAIT)) {
                        VideoPlayerActivity.this.Orientation = AppConstants.LANDSCAPE;
                        VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        Toast.makeText(VideoPlayerActivity.this, "LandScape", Toast.LENGTH_SHORT).show();
                    } else if (VideoPlayerActivity.this.Orientation.equals(AppConstants.LANDSCAPE)) {
                        VideoPlayerActivity.this.Orientation = AppConstants.AUTO_ROTATE;
                        VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        Toast.makeText(VideoPlayerActivity.this, AppConstants.AUTO_ROTATE, Toast.LENGTH_SHORT).show();
                    } else {
                        VideoPlayerActivity.this.Orientation = AppConstants.PORTRAIT;
                        VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        Toast.makeText(VideoPlayerActivity.this, AppConstants.PORTRAIT, Toast.LENGTH_SHORT).show();
                    }
                } else if (i2 == 4) {
                    VideoPlayerActivity.this.OpenVolumeBottomSheet();
                } else if (i2 == 5) {
                    VideoPlayerActivity.this.OpenBrightnessBottomSheet();
                } else if (i2 == 6) {
                    VideoPlayerActivity.this.OpenSpeedBottomSheet();
                } else if (i2 == 7) {
                    if (VideoPlayerActivity.this.isFlip) {
                        VideoPlayerActivity.this.isFlip = false;
                        VideoPlayerActivity.this.binding.surfaceView.setScaleX(1.0f);
                        return;
                    }
                    VideoPlayerActivity.this.isFlip = true;
                    VideoPlayerActivity.this.binding.surfaceView.setScaleX(-1.0f);
                } else if (i2 == 8) {
                    VideoPlayerActivity.this.checkOverlayPermission();
                } else if (i2 == 9) {
                    if (AppPref.getPopupVideoList() != null) {
                        ArrayList arrayList = new ArrayList();
                        for (int i3 = 0; i3 < AppPref.getPopupVideoList().size(); i3++) {
                            VideoModal videoModal = AppPref.getPopupVideoList().get(i3);
                            arrayList.add(new AudioVideoModal(videoModal.getaPath(), videoModal.getaName(), videoModal.getDuration(), "", "", i3));
                        }
                        if (AppPref.getBgAudioList() != null) {
                            AppPref.getBgAudioList().clear();
                        }
                        AppPref.setBgAudioList(arrayList);
                        Intent intent = new Intent(VideoPlayerActivity.this, AudioPlayerActivity.class);
                        int indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(VideoPlayerActivity.this.videoModalList.get(VideoPlayerActivity.this.position).getaPath()));
                        if (indexOf != -1) {
                            intent.putExtra("modal", AppPref.getBgAudioList().get(indexOf));
                        } else {
                            intent.putExtra("modal", AppPref.getBgAudioList().get(0));
                        }
                        intent.putExtra("seek", (int) VideoPlayerActivity.this.runningDuration);
                        VideoPlayerActivity.this.startActivity(intent);
                        VideoPlayerActivity.this.releasePlayer();
                        VideoPlayerActivity.this.handler.removeCallbacksAndMessages((Object) null);
                        VideoPlayerActivity.this.finish();
                    }
                } else if (i2 == 10) {
                    VideoPlayerActivity.this.OpenPlayListBottomSheet();
                } else if (i2 == 11) {
                    VideoPlayerActivity.this.OpenTimerBottomSheet();
                }
            }
        });
        this.binding.iconRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true));
        this.binding.iconRecycle.setAdapter(this.playerIconAdapter);
        this.binding.iconRecycle.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                Log.d("TAG", "onScrolled onScrollStateChanged: ");
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                super.onScrolled(recyclerView, i, i2);
                VideoPlayerActivity.this.stopTimer();
                VideoPlayerActivity.this.setWaitTimer(2000);
            }
        });
    }

    
    public void OpenTimerBottomSheet() {
        final BottomSheetDialog bottomSheetDialog2 = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetTimerBinding bottomsheetTimerBinding = (BottomsheetTimerBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_timer, (ViewGroup) null, false);
        bottomSheetDialog2.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog2.setContentView(bottomsheetTimerBinding.getRoot());
        bottomSheetDialog2.setCancelable(true);
        bottomSheetDialog2.show();
        bottomsheetTimerBinding.llOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
                AppPref.setAudioTimerTime(0);
                VideoPlayerActivity.this.stopPlayerTimer();
            }
        });
        bottomsheetTimerBinding.ll15Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
                AppPref.setAudioTimerTime(VideoPlayerActivity.this.getMilli(15));
                VideoPlayerActivity.this.stopPlayerTimer();
                VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                videoPlayerActivity.setPlayerTimer(videoPlayerActivity.getMilli(15));
            }
        });
        bottomsheetTimerBinding.ll30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
                AppPref.setAudioTimerTime(VideoPlayerActivity.this.getMilli(30));
                VideoPlayerActivity.this.stopPlayerTimer();
                VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                videoPlayerActivity.setPlayerTimer(videoPlayerActivity.getMilli(30));
            }
        });
        bottomsheetTimerBinding.ll45Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
                AppPref.setAudioTimerTime(VideoPlayerActivity.this.getMilli(45));
                VideoPlayerActivity.this.stopPlayerTimer();
                VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                videoPlayerActivity.setPlayerTimer(videoPlayerActivity.getMilli(45));
            }
        });
        bottomsheetTimerBinding.ll60Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
                AppPref.setAudioTimerTime(VideoPlayerActivity.this.getMilli(60));
                VideoPlayerActivity.this.stopPlayerTimer();
                VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                videoPlayerActivity.setPlayerTimer(videoPlayerActivity.getMilli(60));
            }
        });
        bottomsheetTimerBinding.llStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    long duration = ((long) VideoPlayerActivity.this.mediaPlayer.getDuration()) - VideoPlayerActivity.this.runningDuration;
                    AppPref.setAudioTimerTime(duration);
                    VideoPlayerActivity.this.stopPlayerTimer();
                    VideoPlayerActivity.this.setPlayerTimer(duration);
                }
            }
        });
    }

    private Bitmap getBitmap(long j) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        if (Build.VERSION.SDK_INT > 29) {
            mediaMetadataRetriever.setDataSource(this, Uri.parse(this.videoModalList.get(this.position).getaPath()));
        } else {
            mediaMetadataRetriever.setDataSource(this, FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(this.videoModalList.get(this.position).getaPath())));
        }
        return mediaMetadataRetriever.getFrameAtTime(j * 1000);
    }

    public void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {

            this.activityLauncher.launch(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + getPackageName())), new BetterActivityResult.OnActivityResult<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (Settings.canDrawOverlays(VideoPlayerActivity.this)) {
                        releasePlayer();
                        handler.removeCallbacksAndMessages((Object) null);
                        startServices();
                        finish();
                    }
                }
            });
            return;
        }
        releasePlayer();
        this.handler.removeCallbacksAndMessages((Object) null);
        if (this.videoModalList.size() > 0) {
            startServices();
        }
        Log.d("TAG", "checkOverlayPermission: finish");
        finish();
    }



    public void startServices() {
        Intent intent = new Intent(this, VideoService.class);
        intent.putExtra("modal", this.videoModalList.get(this.position));
        intent.putExtra("SeekTo", this.runningDuration);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    
    public void OpenPlayListBottomSheet() {
        this.bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetVideoPlaylistBinding bottomsheetVideoPlaylistBinding = (BottomsheetVideoPlaylistBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_video_playlist, (ViewGroup) null, false);
        this.playlistBinding = bottomsheetVideoPlaylistBinding;
        this.bottomSheetDialog.setContentView(bottomsheetVideoPlaylistBinding.getRoot());
        this.bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        this.bottomSheetDialog.setCancelable(true);
        this.bottomSheetDialog.show();
        setRepeatImage();
        final ArrayList arrayList = new ArrayList();
        if (AppPref.getPopupVideoList() != null) {
            arrayList.addAll(AppPref.getPopupVideoList());
        }
        this.bottomVideoListAdapter = new BottomVideoListAdapter(this, arrayList, this.position, true, new BottomVideoListAdapter.VideoClick() {
            @Override
            public void Click(VideoModal videoModal, int i) {
                VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                videoPlayerActivity.position = videoPlayerActivity.videoModalList.indexOf(videoModal);
                if (i == 1) {
                    try {
                        VideoPlayerActivity videoPlayerActivity2 = VideoPlayerActivity.this;
                        videoPlayerActivity2.totalDuration = AppConstants.getDuration(videoPlayerActivity2, videoPlayerActivity2.videoModalList.get(VideoPlayerActivity.this.position).getaPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    VideoPlayerActivity.this.binding.txtTotalTime.setText(AppConstants.timeFormat(VideoPlayerActivity.this.totalDuration));
                    VideoPlayerActivity.this.binding.seekBar.setMax((int) VideoPlayerActivity.this.totalDuration);
                    VideoPlayerActivity videoPlayerActivity3 = VideoPlayerActivity.this;
                    videoPlayerActivity3.setPlayer(Uri.parse(videoPlayerActivity3.videoModalList.get(VideoPlayerActivity.this.position).getaPath()));
                    VideoPlayerActivity.this.setPlayerSeekBar();
                    VideoPlayerActivity.this.binding.imgPlayPause.setImageResource(R.drawable.pause);
                    return;
                }
                arrayList.remove(arrayList.indexOf(videoModal));
                VideoPlayerActivity.this.videoModalList.remove(VideoPlayerActivity.this.position);
                ArrayList<VideoModal> popupVideoList = AppPref.getPopupVideoList();
                if (AppPref.getPopupVideoList().contains(videoModal)) {
                    popupVideoList.remove(AppPref.getPopupVideoList().indexOf(videoModal));
                }
                AppPref.getPopupVideoList().clear();
                AppPref.setPopupVideoList(popupVideoList);
                VideoPlayerActivity.this.bottomVideoListAdapter.notifyDataSetChanged();
            }
        });
        this.playlistBinding.playListRecycle.setLayoutManager(new GridLayoutManager(this, 3));
        this.playlistBinding.playListRecycle.setAdapter(this.bottomVideoListAdapter);
        this.playlistBinding.llChnagePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    case -267444236:
                        if (videoState.equals(AppConstants.REPEAT_CURRENT)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 76453678:
                        if (videoState.equals(AppConstants.ORDER)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 2090883002:
                        if (videoState.equals(AppConstants.SHUFFLE_ALL)) {
                            c = 3;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        AppPref.setVideoState(AppConstants.ORDER);
                        VideoPlayerActivity.this.setRepeatImage();
                        return;
                    case 1:
                        AppPref.setVideoState(AppConstants.LOOP_ALL);
                        VideoPlayerActivity.this.setRepeatImage();
                        return;
                    case 2:
                        AppPref.setVideoState(AppConstants.SHUFFLE_ALL);
                        VideoPlayerActivity.this.setRepeatImage();
                        return;
                    case 3:
                        AppPref.setVideoState(AppConstants.REPEAT_CURRENT);
                        VideoPlayerActivity.this.setRepeatImage();
                        return;
                    default:
                        return;
                }
            }
        });
        this.playlistBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.bottomSheetDialog.dismiss();
            }
        });
    }

    
    public void OpenVolumeBottomSheet() {
        final BottomSheetDialog bottomSheetDialog2 = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        final BottomsheetVolumeBinding bottomsheetVolumeBinding = (BottomsheetVolumeBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_volume, (ViewGroup) null, false);
        bottomSheetDialog2.setContentView(bottomsheetVolumeBinding.getRoot());
        bottomSheetDialog2.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog2.setCancelable(true);
        bottomSheetDialog2.show();
        bottomsheetVolumeBinding.seekBar.setMax(this.audioManager.getStreamMaxVolume(3));
        int streamVolume = this.audioManager.getStreamVolume(3);
        TextView textView = bottomsheetVolumeBinding.txtVolume;
        textView.setText("" + ((streamVolume * 100) / 16));
        bottomsheetVolumeBinding.seekBar.setProgress(this.audioManager.getStreamVolume(3));
        if (this.audioManager.getStreamVolume(3) == 0) {
            RequestManager with = Glide.with((FragmentActivity) this);
            with.load(Uri.parse(AppConstants.AssetsPath() + "volume_off.png")).into(bottomsheetVolumeBinding.imgVolume);
        } else {
            RequestManager with2 = Glide.with((FragmentActivity) this);
            with2.load(Uri.parse(AppConstants.AssetsPath() + "volume_on.png")).into(bottomsheetVolumeBinding.imgVolume);
        }
        bottomsheetVolumeBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                VideoPlayerActivity.this.audioManager.setStreamVolume(3, i, 0);
                int streamVolume = VideoPlayerActivity.this.audioManager.getStreamVolume(3);
                TextView textView = bottomsheetVolumeBinding.txtVolume;
                textView.setText("" + ((streamVolume * 100) / 16));
                if (i == 0) {
                    VideoPlayerActivity.this.isMute = true;
                    RequestManager with = Glide.with((FragmentActivity) VideoPlayerActivity.this);
                    with.load(Uri.parse(AppConstants.AssetsPath() + "volume_off.png")).into(bottomsheetVolumeBinding.imgVolume);
                    return;
                }
                VideoPlayerActivity.this.isMute = false;
                RequestManager with2 = Glide.with((FragmentActivity) VideoPlayerActivity.this);
                with2.load(Uri.parse(AppConstants.AssetsPath() + "volume_on.png")).into(bottomsheetVolumeBinding.imgVolume);
            }
        });
        bottomsheetVolumeBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
            }
        });
    }

    
    public void OpenSpeedBottomSheet() {
        final BottomSheetDialog bottomSheetDialog2 = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        final BottomsheetSpeedBinding bottomsheetSpeedBinding = (BottomsheetSpeedBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_speed, (ViewGroup) null, false);
        bottomSheetDialog2.setContentView(bottomsheetSpeedBinding.getRoot());
        bottomSheetDialog2.setCancelable(true);
        bottomSheetDialog2.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog2.show();
        bottomsheetSpeedBinding.seekBar.setProgress((int) this.videoSpeed);
        bottomsheetSpeedBinding.btn05x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.videoSpeed = 14.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 0.5d);
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) VideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btn1x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.videoSpeed = 28.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 0.2800000011920929d);
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) VideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btn15x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.videoSpeed = 42.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 0.41999998688697815d);
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) VideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btn2x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.videoSpeed = 56.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 0.5600000023841858d);
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) VideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btn25x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.videoSpeed = 70.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 0.699999988079071d);
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) VideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btn3x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.videoSpeed = 84.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 0.8399999737739563d);
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) VideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed(VideoPlayerActivity.this.playerSpeed.getSpeed());
                if (VideoPlayerActivity.this.mediaPlayer != null) {
                    VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress(25);
            }
        });
        bottomsheetSpeedBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                double d = ((double) ((float) i)) / 25.0d;
                VideoPlayerActivity.this.videoSpeed = (double) i;
                if (d != 0.0d) {
                    try {
                        PlaybackParams playbackParams = new PlaybackParams();
                        playbackParams.setSpeed((float) d);
                        if (VideoPlayerActivity.this.mediaPlayer != null) {
                            VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    d = 0.1d;
                    PlaybackParams playbackParams2 = new PlaybackParams();
                    playbackParams2.setSpeed((float) 0.1d);
                    if (VideoPlayerActivity.this.mediaPlayer != null) {
                        VideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams2);
                    }
                }
                String.valueOf(d);
            }
        });
        bottomsheetSpeedBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
            }
        });
    }

    
    public void OpenBrightnessBottomSheet() {
        final BottomSheetDialog bottomSheetDialog2 = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        final BottomsheetBrightnessBinding bottomsheetBrightnessBinding = (BottomsheetBrightnessBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_brightness, (ViewGroup) null, false);
        bottomSheetDialog2.setContentView(bottomsheetBrightnessBinding.getRoot());
        bottomSheetDialog2.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog2.setCancelable(true);
        bottomSheetDialog2.show();
        float f = getWindow().getAttributes().screenBrightness;
        Log.d("TAG", "slideToChangeBrightness: " + f);
        bottomsheetBrightnessBinding.seekBar.setProgress((int) (100.0f * f));
        bottomsheetBrightnessBinding.txtBrightness.setText(String.format(Locale.US, "%.2f", new Object[]{Float.valueOf(f)}));
        bottomsheetBrightnessBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                WindowManager.LayoutParams attributes = VideoPlayerActivity.this.getWindow().getAttributes();
                attributes.screenBrightness = ((float) i) / 100.0f;
                VideoPlayerActivity.this.getWindow().setAttributes(attributes);
                float f = VideoPlayerActivity.this.getWindow().getAttributes().screenBrightness;
                Log.d("TAG", "slideToChangeBrightness: " + f);
                AppPref.setScreenBrightness(f);
                bottomsheetBrightnessBinding.txtBrightness.setText(String.format(Locale.US, "%.2f", new Object[]{Float.valueOf(f)}));
            }
        });
        bottomsheetBrightnessBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog2.dismiss();
            }
        });
    }

    public void lockDeviceRotation(boolean z) {
        if (!z) {
            getWindow().clearFlags(16);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        } else if (getResources().getConfiguration().orientation == 2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    private void setExoplayerTouchListener() {
        this.binding.surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                VideoPlayerActivity.this.scaleGestureDetector.onTouchEvent(motionEvent);
                VideoPlayerActivity.this.gestureDetector.onTouchEvent(motionEvent);
                VideoPlayerActivity.this.mGestureDetector.onTouchEvent(motionEvent);
                int action = motionEvent.getAction();
                if (action == 0) {
                    if (motionEvent.getX() < ((float) (VideoPlayerActivity.this.device_width / 2))) {
                        VideoPlayerActivity.this.left = true;
                        VideoPlayerActivity.this.right = false;
                    } else if (motionEvent.getX() > ((float) (VideoPlayerActivity.this.device_width / 2))) {
                        VideoPlayerActivity.this.left = false;
                        VideoPlayerActivity.this.right = true;
                    }
                    VideoPlayerActivity.this.baseX = motionEvent.getX();
                    VideoPlayerActivity.this.baseY = motionEvent.getY();
                    MotionEvent unused = VideoPlayerActivity.this.startEvent = MotionEvent.obtain(motionEvent);
                    VideoPlayerActivity.this.binding.rlProgress.setVisibility(View.GONE);
                    VideoPlayerActivity.this.binding.llForwarding.setVisibility(View.GONE);
                } else if (action == 1) {
                    Log.d("TAG", "onTouch: Up");
                    VideoPlayerActivity.this.binding.rlProgress.setVisibility(View.GONE);
                    VideoPlayerActivity.this.binding.llForwarding.setVisibility(View.GONE);
                    SwipeEventType unused2 = VideoPlayerActivity.this.swipeEventType = SwipeEventType.NONE;
                    MotionEvent unused3 = VideoPlayerActivity.this.startEvent = null;
                    VideoPlayerActivity.this.startVideoTime = -1;
                } else if (action == 2 && VideoPlayerActivity.this.swipeEventType == SwipeEventType.NONE && VideoPlayerActivity.this.startEvent != null && !VideoPlayerActivity.this.isLock && !VideoPlayerActivity.this.isScaling) {
                    VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                    SwipeEventType unused4 = videoPlayerActivity.swipeEventType = videoPlayerActivity.whatTypeIsItExo(videoPlayerActivity.startEvent, motionEvent);
                }
                return true;
            }
        });
    }

    public SwipeEventType whatTypeIsItExo(MotionEvent motionEvent, MotionEvent motionEvent2) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        float x2 = motionEvent2.getX();
        float y2 = motionEvent2.getY();
        float f = x - x2;
        float f2 = x2 - x;
        if (Math.abs(f2) < 50.0f || Math.abs(f2) <= Math.abs(y2 - y)) {
            if (Math.abs(y2 - y) >= 50.0f) {
                this.binding.rlProgress.setVisibility(View.VISIBLE);
                int i = (int) x;
                int i2 = (int) y;
                if (new Rect(ExoViewRect().right / 2, 0, ExoViewRect().right, ExoViewRect().bottom).contains(i, i2)) {
                    this.binding.mVolumeProgressView.setVisibility(View.GONE);
                    this.binding.mLightPeogressView.setVisibility(View.VISIBLE);
                    return SwipeEventType.BRIGHTNESS;
                } else if (new Rect(0, 0, ExoViewRect().right / 2, ExoViewRect().bottom).contains(i, i2)) {
                    this.binding.mVolumeProgressView.setVisibility(View.VISIBLE);
                    this.binding.mLightPeogressView.setVisibility(View.GONE);
                    return SwipeEventType.VOLUME;
                }
            }
            return SwipeEventType.NONE;
        } else if (new Rect((int) (((double) ExoViewRect().right) - (((double) Math.min(ExoViewRect().bottom, ExoViewRect().right)) * 0.2d)), 0, ExoViewRect().right, ExoViewRect().bottom).contains((int) x, (int) y) && f > 0.0f) {
            return SwipeEventType.COMMENTS;
        } else {
            this.binding.llForwarding.setVisibility(View.VISIBLE);
            this.binding.rlMain.setVisibility(View.VISIBLE);
            return SwipeEventType.SEEK;
        }
    }

    private SwipeEventType whatTypeIsItScreen(MotionEvent motionEvent, MotionEvent motionEvent2) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        float x2 = motionEvent2.getX();
        float y2 = motionEvent2.getY();
        float f = x - x2;
        float f2 = x2 - x;
        if (Math.abs(f2) < 50.0f || Math.abs(f2) <= Math.abs(y2 - y)) {
            if (Math.abs(y2 - y) >= 50.0f) {
                this.binding.rlProgress.setVisibility(View.VISIBLE);
                int i = (int) x;
                int i2 = (int) y;
                if (new Rect(ScreenViewRect().right / 2, 0, ScreenViewRect().right, ScreenViewRect().bottom).contains(i, i2)) {
                    this.binding.mVolumeProgressView.setVisibility(View.GONE);
                    this.binding.mLightPeogressView.setVisibility(View.VISIBLE);
                    return SwipeEventType.BRIGHTNESS;
                } else if (new Rect(0, 0, ScreenViewRect().right / 2, ScreenViewRect().bottom).contains(i, i2)) {
                    this.binding.mVolumeProgressView.setVisibility(View.VISIBLE);
                    this.binding.mLightPeogressView.setVisibility(View.GONE);
                    return SwipeEventType.VOLUME;
                }
            }
            return SwipeEventType.NONE;
        } else if (new Rect((int) (((double) ScreenViewRect().right) - (((double) Math.min(ScreenViewRect().bottom, ScreenViewRect().right)) * 0.2d)), 0, ScreenViewRect().right, ScreenViewRect().bottom).contains((int) x, (int) y) && f > 0.0f) {
            return SwipeEventType.COMMENTS;
        } else {
            this.binding.llForwarding.setVisibility(View.VISIBLE);
            this.binding.rlMain.setVisibility(View.VISIBLE);
            return SwipeEventType.SEEK;
        }
    }

    
    public void slideToChangeBrightness(float f) {
        Window window = PlayerUtils.scanForActivity(this).getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        int screenHeight = PlayerUtils.getScreenHeight(getApplicationContext(), false);
        if (!this.isLock) {
            if (this.mBrightness == -1.0f) {
                this.mBrightness = 0.5f;
            }
            float f2 = (f * 2.0f) / ((float) screenHeight);
            float f3 = 1.0f;
            float f4 = (f2 * 1.0f) + this.mBrightness;
            if (f4 < 0.0f) {
                f4 = 0.0f;
            }
            if (f4 <= 1.0f) {
                f3 = f4;
            }
            this.binding.mLightPeogressView.setProgress(f3);
            attributes.screenBrightness = f3;
            window.setAttributes(attributes);
            float f5 = getWindow().getAttributes().screenBrightness;
            Log.d("TAG", "slideToChangeBrightness: " + f5);
            AppPref.setScreenBrightness(f5);
        }
    }

    
    public void slideToChangeVolume(float f) {
        if (!this.isLock) {
            float streamMaxVolume = (float) this.audioManager.getStreamMaxVolume(3);
            float screenHeight = ((float) this.mStreamVolume) + (((f * 2.0f) / ((float) PlayerUtils.getScreenHeight(getApplicationContext(), false))) * streamMaxVolume);
            if (screenHeight > streamMaxVolume) {
                screenHeight = streamMaxVolume;
            }
            if (screenHeight < 0.0f) {
                screenHeight = 0.0f;
            }
            if (this.isMute) {
                this.isMute = false;
            }
            this.binding.mVolumeProgressView.setProgress(screenHeight / streamMaxVolume);
            this.audioManager.setStreamVolume(3, (int) screenHeight, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        MotionEvent motionEvent2;
        this.gestureDetector.onTouchEvent(motionEvent);
        this.mGestureDetector.onTouchEvent(motionEvent);
        Log.d("TAG", "onTouchEvent: (" + motionEvent.getX());
        int action = motionEvent.getAction();
        if (action == 0) {
            if (motionEvent.getX() < ((float) (this.device_width / 2))) {
                this.left = true;
                this.right = false;
            } else if (motionEvent.getX() > ((float) (this.device_width / 2))) {
                this.left = false;
                this.right = true;
            }
            this.baseX = motionEvent.getX();
            this.baseY = motionEvent.getY();
            this.startEvent = MotionEvent.obtain(motionEvent);
            this.binding.rlProgress.setVisibility(View.GONE);
            this.binding.llForwarding.setVisibility(View.GONE);
        } else if (action == 1) {
            this.binding.rlProgress.setVisibility(View.GONE);
            this.binding.llForwarding.setVisibility(View.GONE);
            this.swipeEventType = SwipeEventType.NONE;
            this.startEvent = null;
            this.startVideoTime = -1;
            if (this.binding.rlMain.getVisibility() == View.VISIBLE) {
                stopTimer();
                setWaitTimer(2000);
            }
        } else if (action == 2 && this.swipeEventType == SwipeEventType.NONE && (motionEvent2 = this.startEvent) != null && !this.isLock && !this.isScaling) {
            this.swipeEventType = whatTypeIsItScreen(motionEvent2, motionEvent);
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.d("TAG", "onSingleTapUp:  exoplayer");
        if (!this.isLock) {
            if (this.binding.rlMain.getVisibility() == View.VISIBLE) {
                this.binding.rlMain.setVisibility(View.GONE);
                hideSystemUI();
            } else {
                this.binding.rlMain.setVisibility(View.VISIBLE);
                stopTimer();
                setWaitTimer(2000);
                showSystemUI();
            }
        }
        return false;
    }

    public Rect ExoViewRect() {
        return new Rect(this.binding.surfaceView.getLeft(), this.binding.surfaceView.getTop(), this.binding.surfaceView.getRight(), this.binding.surfaceView.getBottom());
    }

    public Rect ScreenViewRect() {
        return new Rect(this.binding.rlMain.getLeft(), this.binding.rlMain.getTop(), this.binding.rlMain.getRight(), this.binding.rlMain.getBottom());
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        motionEvent2.getY();
        motionEvent.getY();
        double x = (double) (motionEvent2.getX() - motionEvent.getX());
        float y = motionEvent.getY() - motionEvent2.getY();
        if (!this.isLock && !this.isScaling) {
            if (this.swipeEventType == SwipeEventType.BRIGHTNESS) {
                slideToChangeBrightness(y);
                Log.d("TAG", "onScroll: Brightness");
            } else if (this.swipeEventType == SwipeEventType.VOLUME) {
                slideToChangeVolume(y);
                Log.d("TAG", "onScroll: Volume");
            } else if (this.swipeEventType == SwipeEventType.SEEK) {
                SetSeeking(x / ((double) ExoViewRect().width()), f < 0.0f);
            }
        }
        return true;
    }

    public void SetSeeking(double d, boolean z) {
        if (d < -1.0d) {
            d = -1.0d;
        } else if (d > 1.0d) {
            d = 1.0d;
        }
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            int duration = mediaPlayer2.getDuration();
            if (this.startVideoTime < 0) {
                this.startVideoTime = this.mediaPlayer.getCurrentPosition();
            }
            int max = this.startVideoTime + ((int) (((double) this.MAX_VIDEO_STEP_SIZE) * d * (Math.max(d, -d) / 0.1d)));
            int i = 0;
            if (max >= duration) {
                max = 0;
            }
            if (max >= 0) {
                i = max;
            }
            String formattingHours = AppConstants.formattingHours(i / 1000);
            if (z) {
                Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.forward_forwarding)).into(this.binding.imgForwarding);
            } else {
                Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.backward_rewinding)).into(this.binding.imgForwarding);
            }
            this.mediaPlayer.seekTo(i);
            this.binding.txtForwardingTime.setText(formattingHours);
        }
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        Log.d("TAG", "onFling: ");
        try {
            if (Math.abs(this.diffY) > 100 && Math.abs(f2) > 100.0f && !this.isLock) {
                if (this.isScaling) {
                    this.binding.mLightPeogressView.setVisibility(View.GONE);
                    this.binding.mVolumeProgressView.setVisibility(View.GONE);
                } else if (this.isLeft) {
                    this.binding.mLightPeogressView.setVisibility(View.VISIBLE);
                    this.binding.mVolumeProgressView.setVisibility(View.GONE);
                } else {
                    this.binding.mLightPeogressView.setVisibility(View.GONE);
                    this.binding.mVolumeProgressView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        Log.d("TAG", "onDoubleTap: " + this.left + " || " + this.right);
        long ceil = (long) Math.ceil((double) (motionEvent.getX() - this.baseX));
        long ceil2 = (long) Math.ceil((double) (motionEvent.getX() - this.baseY));
        this.isDoubleTap = true;
        if (this.mediaPlayer != null && Math.abs(ceil2) > 100 && Math.abs(ceil2) > Math.abs(ceil)) {
            long currentPosition = (long) this.mediaPlayer.getCurrentPosition();
            if (this.left) {
                if (!this.isLock) {
                    long j = currentPosition - WorkRequest.MIN_BACKOFF_MILLIS;
                    if (j >= 0) {
                        this.mediaPlayer.seekTo((int) j);
                    } else {
                        prevBtnClick();
                    }
                }
                this.binding.backward.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        VideoPlayerActivity.this.binding.backward.setVisibility(View.GONE);
                    }
                }, 500);
                Log.d("TAG", "onDoubleTap: Backward");
            } else {
                if (!this.isLock) {
                    long j2 = currentPosition + WorkRequest.MIN_BACKOFF_MILLIS;
                    if (j2 <= ((long) this.mediaPlayer.getDuration())) {
                        this.mediaPlayer.seekTo((int) j2);
                    } else {
                        nextBtnClick();
                    }
                }
                Log.d("TAG", "onDoubleTap: Forward");
                this.binding.forward.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        VideoPlayerActivity.this.binding.forward.setVisibility(View.GONE);
                    }
                }, 500);
            }
        }
        Log.d("TAG", "onDoubleTap: ");
        return true;
    }

    public class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        public ScaleDetector() {
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            if (!VideoPlayerActivity.this.isLock) {
                VideoPlayerActivity.this.scale_factor *= scaleGestureDetector.getScaleFactor();
                VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                videoPlayerActivity.scale_factor = Math.max(0.5f, Math.min(videoPlayerActivity.scale_factor, 6.0f));
                VideoPlayerActivity.this.binding.rlZoom.setScaleX(VideoPlayerActivity.this.scale_factor);
                VideoPlayerActivity.this.binding.rlZoom.setScaleY(VideoPlayerActivity.this.scale_factor);
                VideoPlayerActivity.this.isScaling = true;
                Log.d("TAG", "onScaleEnd ScaleDetector: Start " + VideoPlayerActivity.this.scale_factor + " Per " + ((int) (VideoPlayerActivity.this.scale_factor * 100.0f)));
            }
            return super.onScale(scaleGestureDetector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            super.onScaleEnd(scaleGestureDetector);
            VideoPlayerActivity.this.isScaling = false;
            Log.d("TAG", "onScaleEnd: ");
        }
    }

    protected class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean mChangeBrightness;
        private boolean mChangeVolume;
        private boolean mFirstTouch;

        protected MyGestureListener() {
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            Log.d("TAG", "onDown: ");
            VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
            videoPlayerActivity.mStreamVolume = videoPlayerActivity.audioManager.getStreamVolume(3);
            VideoPlayerActivity videoPlayerActivity2 = VideoPlayerActivity.this;
            videoPlayerActivity2.mBrightness = videoPlayerActivity2.getWindow().getAttributes().screenBrightness;
            Log.d("TAG", "slideToChangeBrightness: " + VideoPlayerActivity.this.mBrightness);
            this.mFirstTouch = true;
            this.mChangeBrightness = false;
            this.mChangeVolume = false;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            boolean z = false;
            if (motionEvent == null || motionEvent2 == null) {
                return false;
            }
            motionEvent2.getY();
            motionEvent.getY();
            double x = (double) (motionEvent2.getX() - motionEvent.getX());
            float y = motionEvent.getY() - motionEvent2.getY();
            if (!VideoPlayerActivity.this.isLock && !VideoPlayerActivity.this.isScaling) {
                if (VideoPlayerActivity.this.swipeEventType == SwipeEventType.BRIGHTNESS) {
                    this.mChangeBrightness = true;
                    VideoPlayerActivity.this.slideToChangeBrightness(y);
                    Log.d("TAG", "onScroll: Brightness");
                } else if (VideoPlayerActivity.this.swipeEventType == SwipeEventType.VOLUME) {
                    VideoPlayerActivity.this.slideToChangeVolume(y);
                    Log.d("TAG", "onScroll: Volume");
                } else if (VideoPlayerActivity.this.swipeEventType == SwipeEventType.SEEK) {
                    double width = x / ((double) VideoPlayerActivity.this.ExoViewRect().width());
                    VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                    if (f < 0.0f) {
                        z = true;
                    }
                    videoPlayerActivity.SetSeeking(width, z);
                    Log.d("TAG", "onScroll: Seek");
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            long ceil = (long) Math.ceil((double) (motionEvent.getX() - VideoPlayerActivity.this.baseX));
            long ceil2 = (long) Math.ceil((double) (motionEvent.getX() - VideoPlayerActivity.this.baseY));
            Log.d("TAG", "onDoubleTap: " + VideoPlayerActivity.this.left + " || " + VideoPlayerActivity.this.right);
            if (VideoPlayerActivity.this.mediaPlayer != null && Math.abs(ceil2) > 100 && Math.abs(ceil2) > Math.abs(ceil)) {
                long currentPosition = (long) VideoPlayerActivity.this.mediaPlayer.getCurrentPosition();
                if (VideoPlayerActivity.this.left) {
                    if (!VideoPlayerActivity.this.isLock) {
                        long j = currentPosition - WorkRequest.MIN_BACKOFF_MILLIS;
                        if (j >= 0) {
                            VideoPlayerActivity.this.mediaPlayer.seekTo((int) j);
                        } else {
                            VideoPlayerActivity.this.prevBtnClick();
                        }
                        VideoPlayerActivity.this.binding.backward.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                VideoPlayerActivity.this.binding.backward.setVisibility(View.GONE);
                            }
                        }, 500);
                    }
                } else if (!VideoPlayerActivity.this.isLock) {
                    long j2 = currentPosition + WorkRequest.MIN_BACKOFF_MILLIS;
                    if (j2 <= ((long) VideoPlayerActivity.this.mediaPlayer.getDuration())) {
                        VideoPlayerActivity.this.mediaPlayer.seekTo((int) j2);
                    } else {
                        VideoPlayerActivity.this.nextBtnClick();
                    }
                    VideoPlayerActivity.this.binding.forward.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            VideoPlayerActivity.this.binding.forward.setVisibility(View.GONE);
                        }
                    }, 500);
                }
            }
            Log.d("TAG", "onDoubleTap: event1");
            return true;
        }
    }

    private void Clicks() {
        this.binding.back.setOnClickListener(this);
        this.binding.rlPlayPause.setOnClickListener(this);
        this.binding.btnForward.setOnClickListener(this);
        this.binding.btnBackward.setOnClickListener(this);
        this.binding.scale.setOnClickListener(this);
        this.binding.screenshot.setOnClickListener(this);
        this.binding.lock.setOnClickListener(this);
        this.binding.unlock.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                return;
            case R.id.btnBackward:
                stopTimer();
                setWaitTimer(2000);
                if (this.videoModalList.size() > 0) {
                    prevBtnClick();
                    return;
                } else {
                    onBackPressed();
                    return;
                }
            case R.id.btnForward:
                stopTimer();
                setWaitTimer(2000);
                if (this.videoModalList.size() > 0) {
                    nextBtnClick();
                    return;
                } else {
                    onBackPressed();
                    return;
                }
            case R.id.lock:
                stopTimer();
                setWaitTimer(2000);
                this.isLock = false;
                this.binding.rlMain.setVisibility(View.VISIBLE);
                this.binding.lock.setVisibility(View.GONE);
                lockDeviceRotation(false);
                return;
            case R.id.rlPlayPause:
                stopTimer();
                setWaitTimer(2000);
                if (this.isPlaying) {
                    Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play)).into(this.binding.imgPlayPause);
                    this.isPlaying = false;
                    MediaPlayer mediaPlayer2 = this.mediaPlayer;
                    if (mediaPlayer2 != null) {
                        mediaPlayer2.pause();
                        return;
                    }
                    return;
                }
                Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.pause)).into(this.binding.imgPlayPause);
                this.isPlaying = true;
                MediaPlayer mediaPlayer3 = this.mediaPlayer;
                if (mediaPlayer3 != null) {
                    mediaPlayer3.start();
                    return;
                }
                return;
            case R.id.scale:
                stopTimer();
                setWaitTimer(2000);
                if (this.scaleType.equals(AppConstants.FIT)) {
                    ScalableType scalableType = ScalableType.FIT_XY;
                    this.mScalableType = scalableType;
                    setScalableType(scalableType);
                    this.binding.surfaceView.invalidate();
                    this.binding.scale.setImageResource(R.drawable.full_screen);
                    this.scaleType = AppConstants.FULL;
                    return;
                } else if (this.scaleType.equals(AppConstants.FULL)) {
                    ScalableType scalableType2 = ScalableType.CENTER_CROP;
                    this.mScalableType = scalableType2;
                    setScalableType(scalableType2);
                    this.binding.surfaceView.invalidate();
                    this.binding.scale.setImageResource(R.drawable.fit);
                    this.scaleType = AppConstants.ZOOM;
                    return;
                } else {
                    ScalableType scalableType3 = ScalableType.FIT_CENTER;
                    this.mScalableType = scalableType3;
                    setScalableType(scalableType3);
                    this.binding.surfaceView.invalidate();
                    this.binding.scale.setImageResource(R.drawable.full);
                    this.scaleType = AppConstants.FIT;
                    return;
                }
            case R.id.screenshot:
                MediaPlayer mediaPlayer4 = this.mediaPlayer;
                if (mediaPlayer4 != null) {
                    Bitmap bitmap = getBitmap((long) mediaPlayer4.getCurrentPosition());
                    saveBitmap(bitmap);
                    Glide.with((FragmentActivity) this).load(bitmap).into(this.binding.imgSS);
                }
                this.binding.frame.setVisibility(View.VISIBLE);
                this.moveAnim.start();
                this.moveAnim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }

                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        VideoPlayerActivity.this.binding.frame.setVisibility(View.GONE);
                    }
                });
                stopTimer();
                setWaitTimer(2000);
                return;
            case R.id.unlock:
                this.isLock = true;
                this.binding.rlMain.setVisibility(View.GONE);
                this.binding.lock.setVisibility(View.VISIBLE);
                lockDeviceRotation(true);
                return;
            default:
                return;
        }
    }

    private void saveBitmap(Bitmap bitmap) {
        String str = "Heeder_" + System.currentTimeMillis() + ".png";
        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                ContentResolver contentResolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS + "/" + AppConstants.IMAGE_FOLDER + "/");
                contentValues.put("_display_name", str);
                contentValues.put("date_modified", Long.valueOf(System.currentTimeMillis()));
                contentValues.put("date_added", Long.valueOf(System.currentTimeMillis()));
                contentValues.put("mime_type", "image/" + "png");
                contentValues.put("is_pending", 0);
                contentValues.put("bucket_display_name", "/" + AppConstants.IMAGE_FOLDER);
                Uri insert = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
                Log.i("saveImageAndGetURI", "saveImageAndGetURI: " + insert);
                OutputStream openOutputStream = contentResolver.openOutputStream(insert);
                bitmap.compress(compressFormat, 90, openOutputStream);
                openOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + AppConstants.IMAGE_FOLDER);
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file, str);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file2);
                bitmap.compress(compressFormat, 90, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            AppConstants.refreshGallery(String.valueOf(file2), this);
        }
    }

    public void releasePlayer() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    private void resetPlayer() {
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.reset();
        } else {
            this.mediaPlayer = new MediaPlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 == null) {
            return;
        }
        if (this.isPlaying) {
            mediaPlayer2.start();
        } else {
            mediaPlayer2.pause();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("TAG", "checkOverlayPermission: pause");
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.pause();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("TAG", "checkOverlayPermission: Destroy");
        getWindow().clearFlags(128);
        releasePlayer();
        this.handler.removeCallbacksAndMessages((Object) null);
        stopTimer();
        stopPlayerTimer();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setWaitTimer(long j) {
        this.waitTimer = new CountDownTimer(j, 1000) {
            @Override
            public void onTick(long j) {
            }

            @Override
            public void onFinish() {
                Log.d("TAG", "onFinish: ");
                VideoPlayerActivity.this.binding.rlMain.setVisibility(View.GONE);
                VideoPlayerActivity.this.binding.rlProgress.setVisibility(View.GONE);
                VideoPlayerActivity.this.binding.llForwarding.setVisibility(View.GONE);
                VideoPlayerActivity.this.hideSystemUI();
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

    public void stopPlayerTimer() {
        CountDownTimer countDownTimer = this.playerTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            this.playerTimer = null;
        }
    }

    public void setPlayerTimer(long j) {
        this.playerTimer = new CountDownTimer(j, 1000) {
            @Override
            public void onTick(long j) {
            }

            @Override
            public void onFinish() {
                if (AppPref.getAudioTimerTime() != 0) {
                    VideoPlayerActivity.this.releasePlayer();
                    VideoPlayerActivity.this.handler.removeCallbacksAndMessages((Object) null);
                    VideoPlayerActivity.this.finish();
                    AppPref.setAudioTimerTime(0);
                }
            }
        }.start();
    }

    public void copyFile(String str, String str2) throws IOException {
        try {
            FileInputStream fileInputStream = new FileInputStream(getContentResolver().openFileDescriptor(Uri.parse(str), "rw").getFileDescriptor());
            FileOutputStream fileOutputStream = new FileOutputStream(str2);
            FileChannel channel = fileInputStream.getChannel();
            channel.transferTo(0, channel.size(), fileOutputStream.getChannel());
            fileInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Log.d("orientation", "onConfigurationChanged: viewheight:" + this.binding.frameTemp.getHeight() + " width: " + this.binding.frameTemp.getWidth());
        Log.d("orientation", "onConfigurationChanged: surfaceview viewheight:" + this.binding.surfaceView.getHeight() + " width: " + this.binding.surfaceView.getWidth());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("orientation", " after delay onConfigurationChanged: viewheight:" + VideoPlayerActivity.this.binding.frameTemp.getHeight() + " width: " + VideoPlayerActivity.this.binding.frameTemp.getWidth());
                Log.d("orientation", " after delay onConfigurationChanged: surfaceview viewheight:" + VideoPlayerActivity.this.binding.surfaceView.getHeight() + " width: " + VideoPlayerActivity.this.binding.surfaceView.getWidth());
                VideoPlayerActivity.this.binding.surfaceView.invalidate();
                VideoPlayerActivity videoPlayerActivity = VideoPlayerActivity.this;
                videoPlayerActivity.setScalableType(videoPlayerActivity.mScalableType);
            }
        }, 200);
    }

    
    public void hideSystemUI() {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    private void showSystemUI() {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        insetsController.show(WindowInsetsCompat.Type.systemBars());
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
