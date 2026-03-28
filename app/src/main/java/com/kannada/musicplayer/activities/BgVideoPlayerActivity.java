package com.kannada.musicplayer.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.adapter.BGVideoPlayListAdapter;
import com.kannada.musicplayer.adapter.PlayerIconAdapter;
import com.kannada.musicplayer.ads.MyApplication;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.databinding.ActivityBgVideoPlayerBinding;
import com.kannada.musicplayer.databinding.BottomsheetBgVideoPlaylistBinding;
import com.kannada.musicplayer.databinding.BottomsheetBrightnessBinding;
import com.kannada.musicplayer.databinding.BottomsheetSpeedBinding;
import com.kannada.musicplayer.databinding.BottomsheetVolumeBinding;
import com.kannada.musicplayer.model.IconModel;
import com.kannada.musicplayer.scalable.ScalableType;
import com.kannada.musicplayer.scalable.ScaleManager;
import com.kannada.musicplayer.scalable.Size;
import com.kannada.musicplayer.service.AudioService;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;
import com.kannada.musicplayer.utils.BetterActivityResult;
import com.kannada.musicplayer.utils.PlayerUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BgVideoPlayerActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, TextureView.SurfaceTextureListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private static final int SWIPE_THRESHOLD = 50;
    int MAX_VIDEO_STEP_SIZE = 60000;
    String Orientation = AppConstants.AUTO_ROTATE;
    int action = 2;
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    BGVideoPlayListAdapter adapter;
    AudioManager audioManager;
    AudioService audioService;
    AudioVideoModal audioVideoModal;
    float baseX;
    float baseY;
    ActivityBgVideoPlayerBinding binding;
    BottomSheetDialog bottomSheetDialog;
    BGVideoPlayListAdapter bottomVideoListAdapter;
    int device_height;
    int device_width;
    long diffX;
    long diffY;
    GestureDetector gestureDetector;
    Handler handler = new Handler();
    List<IconModel> iconModelList = new ArrayList();
    boolean isDoubleTap = false;
    boolean isExpanded = false;
    boolean isFlip = false;
    boolean isLeft = false;
    boolean isLock = false;
    boolean isMute = false;
    boolean isPlaying = true;
    boolean isScaling = false;
    boolean isViewsVisible = false;
    boolean left = false;
    protected float mBrightness;
    GestureDetector mGestureDetector;
    BgPlayerReceiver mReceiver;
    protected ScalableType mScalableType = ScalableType.FIT_CENTER;
    protected int mStreamVolume;
    MainActivityReceiver mainActivityReceiver;
    MediaController mediaController;
    MediaPlayer mediaPlayer;
    ObjectAnimator moveAnim;
    PlayerIconAdapter playerIconAdapter;
    PlaybackParams playerSpeed;
    CountDownTimer playerTimer;
    float playerVolume = 0.0f;
    BottomsheetBgVideoPlaylistBinding playlistBinding;
    int position = 0;
    boolean right = false;
    long runningDuration = 0;
    ScaleGestureDetector scaleGestureDetector;
    String scaleType = AppConstants.FULL;
    float scale_factor = 1.0f;
    long seekTo = 0;
    
    public MotionEvent startEvent;
    int startVideoTime = -1;
    
    public SwipeEventType swipeEventType = SwipeEventType.NONE;
    long totalDuration = 0;
    String userAgent;
    double videoSpeed = 25.0d;
    CountDownTimer waitTimer;

    private enum SwipeEventType {
        NONE,
        BRIGHTNESS,
        VOLUME,
        SEEK,
        COMMENTS
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
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }


    private void applyDisplayCutouts() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_home), (v, insets) -> {
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
        this.binding = (ActivityBgVideoPlayerBinding) DataBindingUtil.setContentView(this, R.layout.activity_bg_video_player);

        applyDisplayCutouts();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        this.audioManager = (AudioManager) getSystemService("audio");
        this.audioVideoModal = (AudioVideoModal) getIntent().getParcelableExtra("modal");
        this.seekTo = (long) getIntent().getIntExtra("seek", 0);
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            bindService();
        } else {
            Intent intent = new Intent(MyApplication.getContext(), AudioService.class);
            intent.putExtra("model", AppPref.getBgAudioList().get(this.position));
            intent.putExtra("seek", 0);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            bindService(intent, this, 1);
        }
        setPlayerIconList();
        setVideo(this.audioVideoModal);
        Clicks();
        registerReceiver();
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
        float f = getWindow().getAttributes().screenBrightness;
        this.mBrightness = f;
        if (((double) f) > 0.5d) {
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

    private void registerReceiver() {
        this.mReceiver = new BgPlayerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.STOP_BG_PLAYER);
        registerReceiver(this.mReceiver, intentFilter);
        if (this.mainActivityReceiver == null) {
            MainActivityReceiver mainActivityReceiver2 = new MainActivityReceiver();
            this.mainActivityReceiver = mainActivityReceiver2;
            registerReceiver(mainActivityReceiver2, new IntentFilter(AppConstants.MAIN_ACTIVITY_RECEIVER));
        }
    }

    public void bindService() {
        bindService(new Intent(MyApplication.getContext(), AudioService.class), this, 1);
    }

    private void Clicks() {
        this.binding.back.setOnClickListener(this);
        this.binding.rlPlayPause.setOnClickListener(this);
        this.binding.btnBackward.setOnClickListener(this);
        this.binding.btnForward.setOnClickListener(this);
        this.binding.screenshot.setOnClickListener(this);
        this.binding.scale.setOnClickListener(this);
        this.binding.screenshot.setOnClickListener(this);
        this.binding.lock.setOnClickListener(this);
        this.binding.unlock.setOnClickListener(this);
    }

    private void setPlayerIconList() {
        this.iconModelList.add(new IconModel("right_back.png", ""));
        this.iconModelList.add(new IconModel("day.png", "Day"));
        this.iconModelList.add(new IconModel("volume_on.png", "Mute"));
        this.iconModelList.add(new IconModel("rotation.png", "Rotate"));
        this.playerIconAdapter = new PlayerIconAdapter(this, this.iconModelList, new PlayerIconAdapter.OnClick() {
            @Override
            public void onIconClick(int i) {
                BgVideoPlayerActivity.this.stopTimer();
                BgVideoPlayerActivity.this.setWaitTimer(2000);
                IconModel iconModel = BgVideoPlayerActivity.this.iconModelList.get(i);
                if (i == 0) {
                    if (BgVideoPlayerActivity.this.isExpanded) {
                        BgVideoPlayerActivity.this.iconModelList.clear();
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("right_back.png", ""));
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("day.png", "Day"));
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("volume_on.png", "Mute"));
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("rotation.png", "Rotate"));
                        BgVideoPlayerActivity.this.playerIconAdapter.notifyDataSetChanged();
                        BgVideoPlayerActivity.this.isExpanded = false;
                        return;
                    }
                    if (BgVideoPlayerActivity.this.iconModelList.size() == 4) {
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("volume_on.png", "Volume"));
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("brightness.png", "Brightness"));
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("speed.png", "Speed"));
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("mirror.png", "Mirror"));
                        BgVideoPlayerActivity.this.iconModelList.add(new IconModel("playlist.png", "Playlist"));
                    }
                    BgVideoPlayerActivity.this.iconModelList.set(i, new IconModel("left_back.png", ""));
                    BgVideoPlayerActivity.this.playerIconAdapter.notifyDataSetChanged();
                    BgVideoPlayerActivity.this.isExpanded = true;
                } else if (i == 1) {
                    if (BgVideoPlayerActivity.this.binding.nightView.getVisibility() == View.VISIBLE) {
                        BgVideoPlayerActivity.this.binding.nightView.setVisibility(View.GONE);
                        BgVideoPlayerActivity.this.iconModelList.set(i, new IconModel("day.png", "Night"));
                    } else {
                        BgVideoPlayerActivity.this.binding.nightView.setVisibility(View.VISIBLE);
                        BgVideoPlayerActivity.this.iconModelList.set(i, new IconModel("day.png", "Day"));
                    }
                    BgVideoPlayerActivity.this.playerIconAdapter.notifyDataSetChanged();
                } else if (i == 2) {
                    if (BgVideoPlayerActivity.this.isMute) {
                        BgVideoPlayerActivity.this.isMute = false;
                        BgVideoPlayerActivity.this.iconModelList.set(i, new IconModel("volume_on.png", "Mute"));
                        BgVideoPlayerActivity.this.audioManager.setStreamVolume(3, BgVideoPlayerActivity.this.mStreamVolume, 0);
                    } else {
                        BgVideoPlayerActivity.this.iconModelList.set(i, new IconModel("volume_off.png", "UnMute"));
                        BgVideoPlayerActivity.this.isMute = true;
                        BgVideoPlayerActivity.this.audioManager.setStreamVolume(3, 0, 0);
                    }
                    BgVideoPlayerActivity.this.playerIconAdapter.notifyDataSetChanged();
                } else if (i == 3) {
                    if (BgVideoPlayerActivity.this.Orientation.equals(AppConstants.PORTRAIT)) {
                        BgVideoPlayerActivity.this.Orientation = AppConstants.LANDSCAPE;
                        BgVideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        Toast.makeText(BgVideoPlayerActivity.this, "LandScape", Toast.LENGTH_SHORT).show();
                    } else if (BgVideoPlayerActivity.this.Orientation.equals(AppConstants.LANDSCAPE)) {
                        BgVideoPlayerActivity.this.Orientation = AppConstants.AUTO_ROTATE;
                        BgVideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        Toast.makeText(BgVideoPlayerActivity.this, AppConstants.AUTO_ROTATE, Toast.LENGTH_SHORT).show();
                    } else {
                        BgVideoPlayerActivity.this.Orientation = AppConstants.PORTRAIT;
                        BgVideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        Toast.makeText(BgVideoPlayerActivity.this, AppConstants.PORTRAIT, Toast.LENGTH_SHORT).show();
                    }
                } else if (i == 4) {
                    BgVideoPlayerActivity.this.OpenVolumeBottomSheet();
                } else if (i == 5) {
                    BgVideoPlayerActivity.this.OpenBrightnessBottomSheet();
                } else if (i == 6) {
                    BgVideoPlayerActivity.this.OpenSpeedBottomSheet();
                } else if (i == 7) {
                    if (BgVideoPlayerActivity.this.isFlip) {
                        BgVideoPlayerActivity.this.isFlip = false;
                        BgVideoPlayerActivity.this.binding.surfaceView.setScaleX(1.0f);
                        return;
                    }
                    BgVideoPlayerActivity.this.isFlip = true;
                    BgVideoPlayerActivity.this.binding.surfaceView.setScaleX(-1.0f);
                } else if (i == 8) {
                    BgVideoPlayerActivity.this.OpenPlayListBottomSheet();
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
                BgVideoPlayerActivity.this.stopTimer();
                BgVideoPlayerActivity.this.setWaitTimer(2000);
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
                BgVideoPlayerActivity.this.audioManager.setStreamVolume(3, i, 0);
                int streamVolume = BgVideoPlayerActivity.this.audioManager.getStreamVolume(3);
                TextView textView = bottomsheetVolumeBinding.txtVolume;
                textView.setText("" + ((streamVolume * 100) / 16));
                if (i == 0) {
                    BgVideoPlayerActivity.this.isMute = true;
                    RequestManager with = Glide.with((FragmentActivity) BgVideoPlayerActivity.this);
                    with.load(Uri.parse(AppConstants.AssetsPath() + "volume_off.png")).into(bottomsheetVolumeBinding.imgVolume);
                    return;
                }
                BgVideoPlayerActivity.this.isMute = false;
                RequestManager with2 = Glide.with((FragmentActivity) BgVideoPlayerActivity.this);
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
                BgVideoPlayerActivity.this.videoSpeed = 25.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 1.0d);
                if (BgVideoPlayerActivity.this.mediaPlayer != null) {
                    BgVideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) BgVideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btn1x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BgVideoPlayerActivity.this.videoSpeed = 50.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 2.0d);
                if (BgVideoPlayerActivity.this.mediaPlayer != null) {
                    BgVideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) BgVideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btn2x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BgVideoPlayerActivity.this.videoSpeed = 100.0d;
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed((float) 4.0d);
                if (BgVideoPlayerActivity.this.mediaPlayer != null) {
                    BgVideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                }
                bottomsheetSpeedBinding.seekBar.setProgress((int) BgVideoPlayerActivity.this.videoSpeed);
            }
        });
        bottomsheetSpeedBinding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaybackParams playbackParams = new PlaybackParams();
                playbackParams.setSpeed(BgVideoPlayerActivity.this.playerSpeed.getSpeed());
                if (BgVideoPlayerActivity.this.mediaPlayer != null) {
                    BgVideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
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
                BgVideoPlayerActivity.this.videoSpeed = (double) i;
                if (d != 0.0d) {
                    try {
                        PlaybackParams playbackParams = new PlaybackParams();
                        playbackParams.setSpeed((float) d);
                        if (BgVideoPlayerActivity.this.mediaPlayer != null) {
                            BgVideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    d = 0.1d;
                    PlaybackParams playbackParams2 = new PlaybackParams();
                    playbackParams2.setSpeed((float) 0.1d);
                    if (BgVideoPlayerActivity.this.mediaPlayer != null) {
                        BgVideoPlayerActivity.this.mediaPlayer.setPlaybackParams(playbackParams2);
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
                WindowManager.LayoutParams attributes = BgVideoPlayerActivity.this.getWindow().getAttributes();
                attributes.screenBrightness = ((float) i) / 100.0f;
                BgVideoPlayerActivity.this.getWindow().setAttributes(attributes);
                float f = BgVideoPlayerActivity.this.getWindow().getAttributes().screenBrightness;
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


    public void OpenPlayListBottomSheet() {
        this.bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetBgVideoPlaylistBinding bottomsheetBgVideoPlaylistBinding = (BottomsheetBgVideoPlaylistBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_bg_video_playlist, (ViewGroup) null, false);
        this.playlistBinding = bottomsheetBgVideoPlaylistBinding;
        this.bottomSheetDialog.setContentView(bottomsheetBgVideoPlaylistBinding.getRoot());
        this.bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        this.bottomSheetDialog.setCancelable(true);
        this.bottomSheetDialog.show();
        final ArrayList arrayList = new ArrayList();
        if (AppPref.getBgAudioList() != null) {
            arrayList.addAll(AppPref.getBgAudioList());
        }
        this.bottomVideoListAdapter = new BGVideoPlayListAdapter(this, arrayList, this.position, true, new BGVideoPlayListAdapter.AudioVideoClick() {
            @Override
            public void Click(int i, int i2) {
                BgVideoPlayerActivity.this.position = i;
                if (i2 == 1) {
                    BgVideoPlayerActivity.this.bottomSheetDialog.dismiss();
                    BgVideoPlayerActivity.this.audioService.updatePosition(AppPref.getBgAudioList().get(BgVideoPlayerActivity.this.position));
                    if (Build.VERSION.SDK_INT > 30) {
                        BgVideoPlayerActivity.this.audioService.UpdateMediaCompact(BgVideoPlayerActivity.this.position);
                    } else {
                        BgVideoPlayerActivity.this.audioService.updateNotification(BgVideoPlayerActivity.this.position);
                    }
                    AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(BgVideoPlayerActivity.this.position);
                    if (TextUtils.isEmpty(audioVideoModal.getArtist()) || TextUtils.isEmpty(audioVideoModal.getAlbum())) {
                        try {
                            BgVideoPlayerActivity bgVideoPlayerActivity = BgVideoPlayerActivity.this;
                            bgVideoPlayerActivity.totalDuration = AppConstants.getDuration(bgVideoPlayerActivity, AppPref.getBgAudioList().get(BgVideoPlayerActivity.this.position).getUri());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        BgVideoPlayerActivity.this.binding.txtTotalTime.setText(AppConstants.timeFormat(BgVideoPlayerActivity.this.totalDuration));
                        BgVideoPlayerActivity.this.binding.seekBar.setMax((int) BgVideoPlayerActivity.this.totalDuration);
                        BgVideoPlayerActivity.this.setPlayer(Uri.parse(AppPref.getBgAudioList().get(BgVideoPlayerActivity.this.position).getUri()));
                        BgVideoPlayerActivity.this.setPlayerSeekBar();
                        BgVideoPlayerActivity.this.binding.imgPlayPause.setImageResource(R.drawable.pause);
                        return;
                    }
                    BgVideoPlayerActivity.this.handler.removeCallbacksAndMessages((Object) null);
                    BgVideoPlayerActivity.this.releasePlayer();
                    Intent intent = new Intent(BgVideoPlayerActivity.this, AudioPlayerActivity.class);
                    intent.putExtra("modal", audioVideoModal);
                    intent.putExtra("isFromBgVideo", true);
                    BgVideoPlayerActivity.this.startActivity(intent);
                    BgVideoPlayerActivity.this.finish();
                    return;
                }
                arrayList.remove(i);
                ArrayList<AudioVideoModal> bgAudioList = AppPref.getBgAudioList();
                if (AppPref.getBgAudioList().contains(AppPref.getBgAudioList().get(BgVideoPlayerActivity.this.position))) {
                    bgAudioList.remove(AppPref.getPopupVideoList().indexOf(AppPref.getBgAudioList().get(BgVideoPlayerActivity.this.position)));
                }
                AppPref.getBgAudioList().clear();
                AppPref.setBgAudioList(bgAudioList);
                BgVideoPlayerActivity.this.bottomVideoListAdapter.notifyDataSetChanged();
            }
        });
        this.playlistBinding.playListRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        this.playlistBinding.playListRecycle.setAdapter(this.bottomVideoListAdapter);
        this.playlistBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BgVideoPlayerActivity.this.bottomSheetDialog.dismiss();
            }
        });
    }

    private void setVideo(AudioVideoModal audioVideoModal2) {
        Uri.parse(audioVideoModal2.getUri());
        try {
            this.totalDuration = AppConstants.getDuration(this, audioVideoModal2.getUri());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.binding.txtTotalTime.setText(AppConstants.timeFormat(this.totalDuration));
        this.binding.seekBar.setMax((int) this.totalDuration);
        this.position = AppPref.getBgAudioList().indexOf(audioVideoModal2);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.screenBrightness = AppPref.getScreenBrightness();
        getWindow().setAttributes(attributes);
        MediaPlayer mediaPlayer2 = new MediaPlayer();
        this.mediaPlayer = mediaPlayer2;
        mediaPlayer2.setOnVideoSizeChangedListener(this);
        this.binding.surfaceView.setSurfaceTextureListener(this);
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnCompletionListener(this);
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
        setPlayer(Uri.parse(this.audioVideoModal.getUri()));
        setPlayerSeekBar();
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
            this.mediaPlayer.setScreenOnWhilePlaying(true);
            this.mediaPlayer.setDataSource(this, uri);
            this.mediaPlayer.setScreenOnWhilePlaying(true);
            this.mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.binding.imgPlayPause.setImageResource(R.drawable.pause);
        this.binding.txtTitle.setText(AppPref.getBgAudioList().get(this.position).getName());
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

    
    public void setPlayerSeekBar() {
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BgVideoPlayerActivity.this.mediaPlayer != null) {
                    BgVideoPlayerActivity.this.binding.txtRunningTime.setText(AppConstants.timeFormat((long) BgVideoPlayerActivity.this.mediaPlayer.getCurrentPosition()));
                    BgVideoPlayerActivity bgVideoPlayerActivity = BgVideoPlayerActivity.this;
                    bgVideoPlayerActivity.runningDuration = (long) bgVideoPlayerActivity.mediaPlayer.getCurrentPosition();
                    BgVideoPlayerActivity.this.binding.seekBar.setProgress(BgVideoPlayerActivity.this.mediaPlayer.getCurrentPosition());
                    if (((long) BgVideoPlayerActivity.this.mediaPlayer.getCurrentPosition()) == BgVideoPlayerActivity.this.totalDuration) {
                        BgVideoPlayerActivity.this.mediaPlayer.pause();
                    }
                }
                BgVideoPlayerActivity.this.handler.postDelayed(this, 10);
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
                    if (BgVideoPlayerActivity.this.mediaPlayer != null) {
                        BgVideoPlayerActivity.this.mediaPlayer.seekTo(i);
                    }
                    BgVideoPlayerActivity.this.stopTimer();
                    BgVideoPlayerActivity.this.setWaitTimer(2000);
                }
                BgVideoPlayerActivity.this.binding.seekBar.setProgress(i);
            }
        });
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

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        Log.d("TAG", "onBackPressed: Video");
        AppPref.setFullScreenPlay(false);
        Intent intent = new Intent();
        intent.setAction(AppConstants.CREATE);
        intent.setPackage(getPackageName());
        intent.putExtra("pos", this.position);
        intent.putExtra("action", this.action);
        intent.putExtra("seek", this.runningDuration);
        sendBroadcast(intent);
        this.handler.removeCallbacksAndMessages((Object) null);
        releasePlayer();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                return;
            case R.id.btnBackward:
                PreviousClick();
                return;
            case R.id.btnForward:
                nextBtnClick();
                return;
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
                    }
                    this.action = 1;
                } else {
                    Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.pause)).into(this.binding.imgPlayPause);
                    this.isPlaying = true;
                    MediaPlayer mediaPlayer3 = this.mediaPlayer;
                    if (mediaPlayer3 != null) {
                        mediaPlayer3.start();
                    }
                    this.action = 2;
                }
                Intent intent = new Intent(AppConstants.MAIN_ACTIVITY_RECEIVER);
                intent.setPackage(getPackageName());
                intent.putExtra(NotificationCompat.CATEGORY_STATUS, AppConstants.PLAY_PAUSE);
                intent.putExtra("action", this.action);
                sendBroadcast(intent);
                if (this.audioService == null) {
                    return;
                }
                if (Build.VERSION.SDK_INT > 30) {
                    this.audioService.UpdateMediaPlayPause(this.action);
                    return;
                } else {
                    this.audioService.updatePlayPauseNotification(this.action);
                    return;
                }
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
                        BgVideoPlayerActivity.this.binding.frame.setVisibility(View.GONE);
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

    private void PreviousClick() {
        this.seekTo = 0;
        this.position = this.audioService.setPreviousRepetition();
        if (Build.VERSION.SDK_INT > 30) {
            this.audioService.UpdateMediaCompact(this.position);
        } else {
            this.audioService.updateNotification(this.position);
        }
        AudioVideoModal audioVideoModal2 = AppPref.getBgAudioList().get(this.position);
        this.audioService.updatePosition(audioVideoModal2);
        if (TextUtils.isEmpty(audioVideoModal2.getArtist()) || TextUtils.isEmpty(audioVideoModal2.getAlbum())) {
            try {
                this.totalDuration = AppConstants.getDuration(this, AppPref.getBgAudioList().get(this.position).getUri());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.binding.txtTotalTime.setText(AppConstants.timeFormat(this.totalDuration));
            this.binding.seekBar.setMax((int) this.totalDuration);
            setPlayer(Uri.parse(AppPref.getBgAudioList().get(this.position).getUri()));
            setPlayerSeekBar();
            this.binding.imgPlayPause.setImageResource(R.drawable.pause);
            return;
        }
        this.handler.removeCallbacksAndMessages((Object) null);
        releasePlayer();
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("modal", audioVideoModal2);
        intent.putExtra("isFromBgVideo", true);
        startActivity(intent);
        finish();
    }

    private void nextBtnClick() {
        this.seekTo = 0;
        this.position = this.audioService.setNextRepetition();
        if (Build.VERSION.SDK_INT > 30) {
            this.audioService.UpdateMediaCompact(this.position);
        } else {
            this.audioService.updateNotification(this.position);
        }
        AudioVideoModal audioVideoModal2 = AppPref.getBgAudioList().get(this.position);
        this.audioService.updatePosition(audioVideoModal2);
        if (TextUtils.isEmpty(audioVideoModal2.getArtist()) || TextUtils.isEmpty(audioVideoModal2.getAlbum())) {
            try {
                this.totalDuration = AppConstants.getDuration(this, AppPref.getBgAudioList().get(this.position).getUri());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.binding.txtTotalTime.setText(AppConstants.timeFormat(this.totalDuration));
            this.binding.seekBar.setMax((int) this.totalDuration);
            setPlayer(Uri.parse(AppPref.getBgAudioList().get(this.position).getUri()));
            setPlayerSeekBar();
            this.binding.imgPlayPause.setImageResource(R.drawable.pause);
            return;
        }
        this.handler.removeCallbacksAndMessages((Object) null);
        releasePlayer();
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("modal", audioVideoModal2);
        intent.putExtra("isFromBgVideo", true);
        startActivity(intent);
        finish();
    }

    private Bitmap getBitmap(long j) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        if (Build.VERSION.SDK_INT > 29) {
            mediaMetadataRetriever.setDataSource(this, Uri.parse(AppPref.getBgAudioList().get(this.position).getUri()));
        } else {
            mediaMetadataRetriever.setDataSource(this, FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(AppPref.getBgAudioList().get(this.position).getUri())));
        }
        return mediaMetadataRetriever.getFrameAtTime(j * 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        MediaPlayer mediaPlayer2 = this.mediaPlayer;
        if (mediaPlayer2 != null) {
            mediaPlayer2.pause();
        }
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            try {
                unbindService(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        this.audioService = ((AudioService.MyBinder) iBinder).getService();
        Log.d("TAG", "onServiceConnected: ");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        this.audioService = null;
    }

    public class BgPlayerReceiver extends BroadcastReceiver {
        public BgPlayerReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AppConstants.STOP_BG_PLAYER.equals(intent.getAction())) {
                BgVideoPlayerActivity.this.handler.removeCallbacksAndMessages((Object) null);
                BgVideoPlayerActivity.this.releasePlayer();
                BgVideoPlayerActivity.this.finish();
            }
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
    public void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(128);
        BgPlayerReceiver bgPlayerReceiver = this.mReceiver;
        if (bgPlayerReceiver != null) {
            try {
                unregisterReceiver(bgPlayerReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        MainActivityReceiver mainActivityReceiver2 = this.mainActivityReceiver;
        if (mainActivityReceiver2 != null) {
            try {
                unregisterReceiver(mainActivityReceiver2);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void setWaitTimer(long j) {
        this.waitTimer = new CountDownTimer(j, 1000) {
            @Override
            public void onTick(long j) {
            }

            @Override
            public void onFinish() {
                Log.d("TAG", "onFinish: ");
                BgVideoPlayerActivity.this.binding.rlMain.setVisibility(View.GONE);
                BgVideoPlayerActivity.this.binding.rlProgress.setVisibility(View.GONE);
                BgVideoPlayerActivity.this.binding.llForwarding.setVisibility(View.GONE);
                BgVideoPlayerActivity.this.hideSystemUI();
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
            public boolean onTouch(View view, MotionEvent motionEvent) {
                BgVideoPlayerActivity.this.scaleGestureDetector.onTouchEvent(motionEvent);
                BgVideoPlayerActivity.this.gestureDetector.onTouchEvent(motionEvent);
                BgVideoPlayerActivity.this.mGestureDetector.onTouchEvent(motionEvent);
                int action = motionEvent.getAction();
                if (action == 0) {
                    if (motionEvent.getX() < ((float) (BgVideoPlayerActivity.this.device_width / 2))) {
                        BgVideoPlayerActivity.this.left = true;
                        BgVideoPlayerActivity.this.right = false;
                    } else if (motionEvent.getX() > ((float) (BgVideoPlayerActivity.this.device_width / 2))) {
                        BgVideoPlayerActivity.this.left = false;
                        BgVideoPlayerActivity.this.right = true;
                    }
                    BgVideoPlayerActivity.this.baseX = motionEvent.getX();
                    BgVideoPlayerActivity.this.baseY = motionEvent.getY();
                    MotionEvent unused = BgVideoPlayerActivity.this.startEvent = MotionEvent.obtain(motionEvent);
                    BgVideoPlayerActivity.this.binding.rlProgress.setVisibility(View.GONE);
                    BgVideoPlayerActivity.this.binding.llForwarding.setVisibility(View.GONE);
                } else if (action == 1) {
                    Log.d("TAG", "onTouch: Up");
                    BgVideoPlayerActivity.this.binding.rlProgress.setVisibility(View.GONE);
                    BgVideoPlayerActivity.this.binding.llForwarding.setVisibility(View.GONE);
                    SwipeEventType unused2 = BgVideoPlayerActivity.this.swipeEventType = SwipeEventType.NONE;
                    MotionEvent unused3 = BgVideoPlayerActivity.this.startEvent = null;
                    BgVideoPlayerActivity.this.startVideoTime = -1;
                } else if (action == 2 && BgVideoPlayerActivity.this.swipeEventType == SwipeEventType.NONE && BgVideoPlayerActivity.this.startEvent != null && !BgVideoPlayerActivity.this.isLock && !BgVideoPlayerActivity.this.isScaling) {
                    BgVideoPlayerActivity bgVideoPlayerActivity = BgVideoPlayerActivity.this;
                    SwipeEventType unused4 = bgVideoPlayerActivity.swipeEventType = bgVideoPlayerActivity.whatTypeIsItExo(bgVideoPlayerActivity.startEvent, motionEvent);
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
            AppPref.setScreenBrightness(getWindow().getAttributes().screenBrightness);
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
        int action2 = motionEvent.getAction();
        if (action2 == 0) {
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
        } else if (action2 == 1) {
            this.binding.rlProgress.setVisibility(View.GONE);
            this.binding.llForwarding.setVisibility(View.GONE);
            this.swipeEventType = SwipeEventType.NONE;
            this.startEvent = null;
            this.startVideoTime = -1;
            if (this.binding.rlMain.getVisibility() == View.VISIBLE) {
                stopTimer();
                setWaitTimer(2000);
            }
        } else if (action2 == 2 && this.swipeEventType == SwipeEventType.NONE && (motionEvent2 = this.startEvent) != null && !this.isLock && !this.isScaling) {
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
                        this.mediaPlayer.seekTo(0);
                    }
                }
                this.binding.backward.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BgVideoPlayerActivity.this.binding.backward.setVisibility(View.GONE);
                    }
                }, 500);
                Log.d("TAG", "onDoubleTap: Backward");
            } else {
                if (!this.isLock) {
                    long j2 = currentPosition + WorkRequest.MIN_BACKOFF_MILLIS;
                    if (j2 <= ((long) this.mediaPlayer.getDuration())) {
                        this.mediaPlayer.seekTo((int) j2);
                    } else {
                        this.mediaPlayer.seekTo(0);
                    }
                }
                Log.d("TAG", "onDoubleTap: Forward");
                this.binding.forward.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BgVideoPlayerActivity.this.binding.forward.setVisibility(View.GONE);
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
            if (!BgVideoPlayerActivity.this.isLock) {
                BgVideoPlayerActivity.this.scale_factor *= scaleGestureDetector.getScaleFactor();
                BgVideoPlayerActivity bgVideoPlayerActivity = BgVideoPlayerActivity.this;
                bgVideoPlayerActivity.scale_factor = Math.max(0.5f, Math.min(bgVideoPlayerActivity.scale_factor, 6.0f));
                BgVideoPlayerActivity.this.binding.rlZoom.setScaleX(BgVideoPlayerActivity.this.scale_factor);
                BgVideoPlayerActivity.this.binding.rlZoom.setScaleY(BgVideoPlayerActivity.this.scale_factor);
                BgVideoPlayerActivity.this.isScaling = true;
                Log.d("TAG", "onScaleEnd ScaleDetector: Start " + BgVideoPlayerActivity.this.scale_factor + " Per " + ((int) (BgVideoPlayerActivity.this.scale_factor * 100.0f)));
            }
            return super.onScale(scaleGestureDetector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            super.onScaleEnd(scaleGestureDetector);
            BgVideoPlayerActivity.this.isScaling = false;
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
            BgVideoPlayerActivity bgVideoPlayerActivity = BgVideoPlayerActivity.this;
            bgVideoPlayerActivity.mStreamVolume = bgVideoPlayerActivity.audioManager.getStreamVolume(3);
            BgVideoPlayerActivity bgVideoPlayerActivity2 = BgVideoPlayerActivity.this;
            bgVideoPlayerActivity2.mBrightness = bgVideoPlayerActivity2.getWindow().getAttributes().screenBrightness;
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
            if (!BgVideoPlayerActivity.this.isLock && !BgVideoPlayerActivity.this.isScaling) {
                if (BgVideoPlayerActivity.this.swipeEventType == SwipeEventType.BRIGHTNESS) {
                    this.mChangeBrightness = true;
                    BgVideoPlayerActivity.this.slideToChangeBrightness(y);
                    Log.d("TAG", "onScroll: Brightness");
                } else if (BgVideoPlayerActivity.this.swipeEventType == SwipeEventType.VOLUME) {
                    BgVideoPlayerActivity.this.slideToChangeVolume(y);
                    Log.d("TAG", "onScroll: Volume");
                } else if (BgVideoPlayerActivity.this.swipeEventType == SwipeEventType.SEEK) {
                    double width = x / ((double) BgVideoPlayerActivity.this.ExoViewRect().width());
                    BgVideoPlayerActivity bgVideoPlayerActivity = BgVideoPlayerActivity.this;
                    if (f < 0.0f) {
                        z = true;
                    }
                    bgVideoPlayerActivity.SetSeeking(width, z);
                    Log.d("TAG", "onScroll: Seek");
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            long ceil = (long) Math.ceil((double) (motionEvent.getX() - BgVideoPlayerActivity.this.baseX));
            long ceil2 = (long) Math.ceil((double) (motionEvent.getX() - BgVideoPlayerActivity.this.baseY));
            Log.d("TAG", "onDoubleTap: " + BgVideoPlayerActivity.this.left + " || " + BgVideoPlayerActivity.this.right);
            if (BgVideoPlayerActivity.this.mediaPlayer != null && Math.abs(ceil2) > 100 && Math.abs(ceil2) > Math.abs(ceil)) {
                long currentPosition = (long) BgVideoPlayerActivity.this.mediaPlayer.getCurrentPosition();
                if (BgVideoPlayerActivity.this.left) {
                    if (!BgVideoPlayerActivity.this.isLock) {
                        long j = currentPosition - WorkRequest.MIN_BACKOFF_MILLIS;
                        if (j >= 0) {
                            BgVideoPlayerActivity.this.mediaPlayer.seekTo((int) j);
                        } else {
                            BgVideoPlayerActivity.this.mediaPlayer.seekTo(0);
                        }
                        BgVideoPlayerActivity.this.binding.backward.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BgVideoPlayerActivity.this.binding.backward.setVisibility(View.GONE);
                            }
                        }, 500);
                    }
                } else if (!BgVideoPlayerActivity.this.isLock) {
                    long j2 = currentPosition + WorkRequest.MIN_BACKOFF_MILLIS;
                    if (j2 <= ((long) BgVideoPlayerActivity.this.mediaPlayer.getDuration())) {
                        BgVideoPlayerActivity.this.mediaPlayer.seekTo((int) j2);
                    } else {
                        BgVideoPlayerActivity.this.mediaPlayer.seekTo(0);
                    }
                    BgVideoPlayerActivity.this.binding.forward.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BgVideoPlayerActivity.this.binding.forward.setVisibility(View.GONE);
                        }
                    }, 500);
                }
            }
            Log.d("TAG", "onDoubleTap: event1");
            return true;
        }
    }

    
    public void hideSystemUI() {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    private void showSystemUI() {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        insetsController.show(WindowInsetsCompat.Type.systemBars());
    }

    public class MainActivityReceiver extends BroadcastReceiver {
        public MainActivityReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.MAIN_ACTIVITY_RECEIVER)) {
                String stringExtra = intent.getStringExtra(NotificationCompat.CATEGORY_STATUS);
                stringExtra.hashCode();
                if (stringExtra.equals(AppConstants.STOP)) {
                    BgVideoPlayerActivity.this.releasePlayer();
                    BgVideoPlayerActivity.this.finish();
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("orientation", " after delay onConfigurationChanged: viewheight:" + BgVideoPlayerActivity.this.binding.frameTemp.getHeight() + " width: " + BgVideoPlayerActivity.this.binding.frameTemp.getWidth());
                Log.d("orientation", " after delay onConfigurationChanged: surfaceview viewheight:" + BgVideoPlayerActivity.this.binding.surfaceView.getHeight() + " width: " + BgVideoPlayerActivity.this.binding.surfaceView.getWidth());
                BgVideoPlayerActivity.this.binding.surfaceView.invalidate();
                BgVideoPlayerActivity bgVideoPlayerActivity = BgVideoPlayerActivity.this;
                bgVideoPlayerActivity.setScalableType(bgVideoPlayerActivity.mScalableType);
            }
        }, 200);
    }



}
