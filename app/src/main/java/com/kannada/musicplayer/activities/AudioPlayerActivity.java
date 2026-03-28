package com.demo.musicvideoplayer.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.adapter.BGPlaylistAdapter;
import com.demo.musicvideoplayer.ads.AdsCommon;
import com.demo.musicvideoplayer.ads.MyApplication;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import com.demo.musicvideoplayer.databinding.ActivityAudioPlayerBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetAudioPlaylistBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetSpeedBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetTimerBinding;
import com.demo.musicvideoplayer.model.AudioFolderModal;
import com.demo.musicvideoplayer.service.AudioService;
import com.demo.musicvideoplayer.utils.ActionPlaying;
import com.demo.musicvideoplayer.utils.AppConstants;
import com.demo.musicvideoplayer.utils.AppPref;
import com.demo.musicvideoplayer.utils.SwipeAndDragHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AudioPlayerActivity extends AppCompatActivity implements View.OnClickListener, ActionPlaying, ServiceConnection {
    public static AudioPlayerActivity audioPlayerActivity;
    BGPlaylistAdapter adapter;
    AudioFolderModal audioFolderModal;
    AudioManager audioManager;
    AudioService audioService;
    double audioSpeed = 25.0d;
    ActivityAudioPlayerBinding binding;
    String displayName;
    MenuItem fav;
    public List<AudioFolderModal> folderList = new ArrayList();
    String id;
    boolean isFromBgVideo = false;
    boolean isFromCopyFile = false;
    MainActivityReceiver mReceiver;
    int mStreamVolume;
    
    public Handler myHandler = new Handler();
    public int position;
    public int seek;
    int tempPos;
    MenuItem unFav;
    MenuItem volumeOff;
    MenuItem volumeOn;

    
    public long getMilli(int i) {
        return (long) (i * 60000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivityReceiver mainActivityReceiver = this.mReceiver;
        if (mainActivityReceiver != null) {
            try {
                unregisterReceiver(mainActivityReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        super.onCreate(bundle);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
        this.binding = (ActivityAudioPlayerBinding) DataBindingUtil.setContentView(this, R.layout.activity_audio_player);

        applyDisplayCutouts();

        AdsCommon.InterstitialAdsOnly(this);


        //Reguler Banner Ads
        RelativeLayout admob_banner = (RelativeLayout) findViewById(R.id.Admob_Banner_Frame);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        FrameLayout qureka = (FrameLayout) findViewById(R.id.qureka);
        AdsCommon.RegulerBanner(this, admob_banner, adContainer, qureka);

        //Adaptive Banner Ads
        RelativeLayout adaptive_banner_container = (RelativeLayout) findViewById(R.id.adaptive_banner_container);
        FrameLayout adaptive_ad_frame = (FrameLayout) findViewById(R.id.adaptive_ad_frame);
        ImageView btn_close_ad = (ImageView) findViewById(R.id.btn_close_ad);

        if (adaptive_banner_container != null && adaptive_ad_frame != null) {
            adaptive_banner_container.setVisibility(View.VISIBLE);
            AdsCommon.LoadAdaptiveBanner(this, adaptive_ad_frame);
            if (btn_close_ad != null) {
                btn_close_ad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adaptive_banner_container.setVisibility(View.GONE);
                    }
                });
            }
        }


        SetToolbar();
        AudioManager audioManager2 = (AudioManager) getSystemService("audio");
        this.audioManager = audioManager2;
        this.mStreamVolume = audioManager2.getStreamVolume(3);
        setPlayerRepetitionImg();
        Clicks();
        registerReceiver();
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ((!"android.intent.action.SEND".equals(action) && !"android.intent.action.VIEW".equals(action)) || type == null) {
            this.seek = getIntent().getIntExtra("seek", 0);
            this.isFromBgVideo = getIntent().getBooleanExtra("isFromBgVideo", false);
            this.position = AppPref.getBgAudioList().indexOf(new AudioVideoModal(((AudioVideoModal) getIntent().getParcelableExtra("modal")).getUri()));
        } else if ("audio".equals(type.substring(0, type.lastIndexOf("/")))) {
            Uri data = "android.intent.action.VIEW".equals(action) ? intent.getData() : null;
            if (data.getScheme() != null && data.getScheme().equals("content")) {
                Cursor query = getContentResolver().query(data, (String[]) null, (String) null, (String[]) null, (String) null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            this.displayName = query.getString(query.getColumnIndex("_display_name"));
                            AudioVideoModal audioVideoModal = new AudioVideoModal();
                            audioVideoModal.setName(this.displayName);
                            audioVideoModal.setUri(data.toString());
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(audioVideoModal);
                            AppPref.setBgAudioList(arrayList);
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
                    this.displayName = this.displayName.substring(lastIndexOf + 1);
                    AudioVideoModal audioVideoModal2 = new AudioVideoModal();
                    audioVideoModal2.setName(this.displayName);
                    audioVideoModal2.setUri(data.toString());
                    ArrayList arrayList2 = new ArrayList();
                    arrayList2.add(audioVideoModal2);
                    AppPref.setBgAudioList(arrayList2);
                }
            }
        }
        if (this.position == -1) {
            this.position = 0;
        }
        playAudio(this.position);
        this.binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (AudioPlayerActivity.this.audioService != null && z) {
                    AudioPlayerActivity.this.audioService.seekTo(i);
                    if (AppPref.getAudioTimerTime() != 0) {
                        AppPref.setAudioTimerTime((long) (AudioPlayerActivity.this.audioService.getDuartion() - AudioPlayerActivity.this.audioService.getCurrentPosition()));
                    }
                }
            }
        });
    }

    private void SetFavOrUnFav(AudioVideoModal audioVideoModal) {
        if (AppPref.getFavouriteList() == null) {
            return;
        }
        if (AppPref.getFavouriteList().contains(new AudioVideoModal(audioVideoModal.getUri()))) {
            this.fav.setVisible(true);
            this.unFav.setVisible(false);
            return;
        }
        this.fav.setVisible(false);
        this.unFav.setVisible(true);
    }


    private void setPlayerRepetitionImg() {
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
            case -267444236:
                if (audioState.equals(AppConstants.REPEAT_CURRENT)) {
                    c = 1;
                    break;
                }
                break;
            case 76453678:
                if (audioState.equals(AppConstants.ORDER)) {
                    c = 2;
                    break;
                }
                break;
            case 2090883002:
                if (audioState.equals(AppConstants.SHUFFLE_ALL)) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.loop)).into(this.binding.repetition);
                return;
            case 1:
                Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.repeat_current)).into(this.binding.repetition);
                return;
            case 2:
                Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.add_to_queue)).into(this.binding.repetition);
                return;
            case 3:
                Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.shuffle)).into(this.binding.repetition);
                return;
            default:
                return;
        }
    }

    private void SetToolbar() {
        setSupportActionBar(this.binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        this.binding.toolbarLayout.toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
    }

    private void playAudio(int i) {
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            Intent intent = new Intent(AppConstants.ACTION_CHANGE_SONG);
            intent.putExtra("Action", 0);
            intent.putExtra("seek", this.seek);
            intent.putExtra("playPosition", i);
            intent.putExtra("isFromBgVideo", this.isFromBgVideo);
            sendBroadcast(intent);
            return;
        }
        Intent intent2 = new Intent(MyApplication.getContext(), AudioService.class);
        intent2.putExtra("model", AppPref.getBgAudioList().get(i));
        intent2.putExtra("seek", this.seek);
        if (Build.VERSION.SDK_INT >= 26) {
            Log.d("AudioService:", "AudioPlayerActivity startservice called");
            startForegroundService(intent2);
        } else {
            Log.d("AudioService:", "AudioPlayerActivity startservice called");
            startService(intent2);
        }
        Log.d("AudioService:", "AudioPlayerActivity bindService called");
        bindService(intent2, this, 1);
    }

    private void stopPlaying() {
        AudioService audioService2 = this.audioService;
        if (audioService2 != null) {
            audioService2.stop();
            this.audioService.release();
        }
    }

    private void OpenPlayListBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetAudioPlaylistBinding bottomsheetAudioPlaylistBinding = (BottomsheetAudioPlaylistBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_audio_playlist, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetAudioPlaylistBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        this.adapter = new BGPlaylistAdapter(this, getSortedList(), new BGPlaylistAdapter.ItemClick() {
            @Override
            public void onItemClick(int i, int i2) {
                if (i2 != 1 && i2 != 2) {
                    ArrayList<AudioVideoModal> bgAudioList = AppPref.getBgAudioList();
                    bgAudioList.remove(i);
                    AppPref.getBgAudioList().clear();
                    AppPref.setBgAudioList(bgAudioList);
                    AudioPlayerActivity.this.adapter.setList(AppPref.getBgAudioList());
                }
            }
        });
        bottomsheetAudioPlaylistBinding.recycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeAndDragHelper(this.adapter));
        this.adapter.setTouchHelper(itemTouchHelper);
        bottomsheetAudioPlaylistBinding.recycle.setAdapter(this.adapter);
        itemTouchHelper.attachToRecyclerView(bottomsheetAudioPlaylistBinding.recycle);
        bottomsheetAudioPlaylistBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    public List<AudioVideoModal> getSortedList() {
        Collections.sort(AppPref.getBgAudioList(), new Comparator<AudioVideoModal>() {
            @Override
            public int compare(AudioVideoModal audioVideoModal, AudioVideoModal audioVideoModal2) {
                return Integer.compare(audioVideoModal.getAudioVideoOrder(), audioVideoModal2.getAudioVideoOrder());
            }
        });
        return AppPref.getBgAudioList();
    }

    public void nullHandler() {
        this.myHandler.removeCallbacksAndMessages((Object) null);
    }

    public void FinishActivity() {
        finish();
    }

    public int nextBtnClick() {
        Intent intent = new Intent();
        intent.setAction(AppConstants.STOP_BG_PLAYER);
        sendBroadcast(intent);
        this.audioService.stop();
        this.audioService.release();
        this.position = this.audioService.setNextRepetition();
        AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(this.position);
        if (!AppPref.IsFullScreenPlay()) {
            NextPlaying();
        } else if (TextUtils.isEmpty(audioVideoModal.getArtist()) || TextUtils.isEmpty(audioVideoModal.getAlbum())) {
            this.audioService.SetNullPlayer();
            this.myHandler.removeCallbacksAndMessages((Object) null);
            if (Build.VERSION.SDK_INT > 30) {
                this.audioService.UpdateMediaCompact(this.position);
            } else {
                this.audioService.updateNotification(this.position);
            }
            Intent intent2 = new Intent(this, BgVideoPlayerActivity.class);
            intent2.putExtra("modal", audioVideoModal);
            startActivity(intent2);
            finish();
        } else {
            NextPlaying();
        }
        return this.position;
    }

    private void NextPlaying() {
        AppConstants.SetAudioHistory(AppPref.getBgAudioList().get(this.position));
        SetFavOrUnFav(AppPref.getBgAudioList().get(this.position));
        this.audioService.createMediaPlayer(this.position);
        this.binding.txtTitle.setText(AppPref.getBgAudioList().get(this.position).getName());
        this.binding.seekBar.setMax(this.audioService.getDuartion());
        this.binding.txtTotalTime.setText(AppConstants.formatTime((long) this.audioService.getDuartion()));
        setPlayingImage();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (AudioPlayerActivity.this.audioService != null) {
                    AudioPlayerActivity.this.binding.seekBar.setProgress(AudioPlayerActivity.this.audioService.getCurrentPosition());
                }
                AudioPlayerActivity.this.myHandler.postDelayed(this, 1000);
            }
        });
        this.audioService.onCompleted();
        this.binding.playPause.setImageResource(R.drawable.main_pause);
        this.audioService.start();
        if (isDestroyed()) {
            return;
        }
        if (this.audioService.isPlaying()) {
            Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play_gif)).into(this.binding.imgPlayer);
        } else {
            Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play_pause_round)).into(this.binding.imgPlayer);
        }
    }

    public int prevBtnClick() {
        Intent intent = new Intent();
        intent.setAction(AppConstants.STOP_BG_PLAYER);
        sendBroadcast(intent);
        boolean isPlaying = this.audioService.isPlaying();
        Integer valueOf = Integer.valueOf(R.drawable.play_gif);
        Integer valueOf2 = Integer.valueOf(R.drawable.play_pause_round);
        if (isPlaying) {
            this.audioService.stop();
            this.audioService.release();
            this.position = this.audioService.setPreviousRepetition();
            AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(this.position);
            if (!AppPref.IsFullScreenPlay()) {
                PrevPlaying();
            } else if (TextUtils.isEmpty(audioVideoModal.getArtist()) || TextUtils.isEmpty(audioVideoModal.getAlbum())) {
                this.audioService.SetNullPlayer();
                this.myHandler.removeCallbacksAndMessages((Object) null);
                if (Build.VERSION.SDK_INT > 30) {
                    this.audioService.UpdateMediaCompact(this.position);
                } else {
                    this.audioService.updateNotification(this.position);
                }
                Intent intent2 = new Intent(this, BgVideoPlayerActivity.class);
                intent2.putExtra("modal", audioVideoModal);
                startActivity(intent2);
                finish();
            } else {
                PrevPlaying();
                this.binding.playPause.setImageResource(R.drawable.main_pause);
                this.audioService.start();
                if (!isDestroyed()) {
                    if (this.audioService.isPlaying()) {
                        Glide.with((FragmentActivity) this).load(valueOf).into(this.binding.imgPlayer);
                    } else {
                        Glide.with((FragmentActivity) this).load(valueOf2).into(this.binding.imgPlayer);
                    }
                }
            }
        } else {
            this.audioService.stop();
            this.audioService.release();
            this.position = this.audioService.setPreviousRepetition();
            AudioVideoModal audioVideoModal2 = AppPref.getBgAudioList().get(this.position);
            if (!AppPref.IsFullScreenPlay()) {
                PrevPlaying();
            } else if (TextUtils.isEmpty(audioVideoModal2.getArtist()) || TextUtils.isEmpty(audioVideoModal2.getAlbum())) {
                this.audioService.SetNullPlayer();
                this.myHandler.removeCallbacksAndMessages((Object) null);
                if (Build.VERSION.SDK_INT > 30) {
                    this.audioService.UpdateMediaCompact(this.position);
                } else {
                    this.audioService.updateNotification(this.position);
                }
                Intent intent3 = new Intent(this, BgVideoPlayerActivity.class);
                intent3.putExtra("modal", audioVideoModal2);
                startActivity(intent3);
                finish();
            } else {
                PrevPlaying();
                this.binding.playPause.setImageResource(R.drawable.main_play);
                if (!isDestroyed()) {
                    if (this.audioService.isPlaying()) {
                        Glide.with((FragmentActivity) this).load(valueOf).into(this.binding.imgPlayer);
                    } else {
                        Glide.with((FragmentActivity) this).load(valueOf2).into(this.binding.imgPlayer);
                    }
                }
            }
        }
        return this.position;
    }

    private void PrevPlaying() {
        SetFavOrUnFav(AppPref.getBgAudioList().get(this.position));
        this.audioService.createMediaPlayer(this.position);
        this.binding.txtTitle.setText(AppPref.getBgAudioList().get(this.position).getName());
        this.binding.seekBar.setMax(this.audioService.getDuartion());
        this.binding.txtTotalTime.setText(AppConstants.formatTime((long) this.audioService.getDuartion()));
        setPlayingImage();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (AudioPlayerActivity.this.audioService != null) {
                    AudioPlayerActivity.this.binding.seekBar.setProgress(AudioPlayerActivity.this.audioService.getCurrentPosition());
                }
                AudioPlayerActivity.this.myHandler.postDelayed(this, 1000);
            }
        });
        this.audioService.onCompleted();
    }

    public int playPauseBtnClick() {
        boolean isPlaying = this.audioService.isPlaying();
        Integer valueOf = Integer.valueOf(R.drawable.play_gif);
        Integer valueOf2 = Integer.valueOf(R.drawable.play_pause_round);
        if (isPlaying) {
            this.binding.playPause.setImageResource(R.drawable.main_play);
            this.audioService.pause();
            this.binding.seekBar.setMax(this.audioService.getDuartion());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (AudioPlayerActivity.this.audioService != null) {
                        AudioPlayerActivity.this.binding.seekBar.setProgress(AudioPlayerActivity.this.audioService.getCurrentPosition());
                    }
                    AudioPlayerActivity.this.myHandler.postDelayed(this, 1000);
                }
            });
            if (Build.VERSION.SDK_INT > 30) {
                this.audioService.UpdateMediaPlayPause(1);
            } else {
                this.audioService.updatePlayPauseNotification(1);
            }
            if (!isDestroyed() && this.binding.imgPlayer.getVisibility() == View.VISIBLE) {
                if (this.audioService.isPlaying()) {
                    Glide.with((FragmentActivity) this).load(valueOf).into(this.binding.imgPlayer);
                } else {
                    Glide.with((FragmentActivity) this).load(valueOf2).into(this.binding.imgPlayer);
                }
            }
            return 1;
        }
        this.binding.playPause.setImageResource(R.drawable.main_pause);
        this.audioService.start();
        this.binding.seekBar.setMax(this.audioService.getDuartion());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (AudioPlayerActivity.this.audioService != null) {
                    AudioPlayerActivity.this.binding.seekBar.setProgress(AudioPlayerActivity.this.audioService.getCurrentPosition());
                }
                AudioPlayerActivity.this.myHandler.postDelayed(this, 1000);
            }
        });
        if (Build.VERSION.SDK_INT > 30) {
            this.audioService.UpdateMediaPlayPause(2);
        } else {
            this.audioService.updatePlayPauseNotification(2);
        }
        if (!isDestroyed() && this.binding.imgPlayer.getVisibility() == View.VISIBLE) {
            if (this.audioService.isPlaying()) {
                Glide.with((FragmentActivity) this).load(valueOf).into(this.binding.imgPlayer);
            } else {
                Glide.with((FragmentActivity) this).load(valueOf2).into(this.binding.imgPlayer);
            }
        }
        return 2;
    }

    private void Clicks() {
        this.binding.llAddToPlaylist.setOnClickListener(this);
        this.binding.playPause.setOnClickListener(this);
        this.binding.btnPrevious.setOnClickListener(this);
        this.binding.btnForward.setOnClickListener(this);
        this.binding.btnFastForward.setOnClickListener(this);
        this.binding.btnFastBackward.setOnClickListener(this);
        this.binding.llRepeating.setOnClickListener(this);
        this.binding.llSpeed.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFastBackward:
                int currentPosition = this.audioService.getCurrentPosition() - 10000;
                if (currentPosition >= 0) {
                    this.audioService.seekTo(currentPosition);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (AudioPlayerActivity.this.audioService != null) {
                                int currentPosition = AudioPlayerActivity.this.audioService.getCurrentPosition();
                                AudioPlayerActivity.this.binding.seekBar.setProgress(currentPosition);
                                AudioPlayerActivity.this.binding.txtRunningTime.setText(AppConstants.formatTime((long) currentPosition));
                            }
                            AudioPlayerActivity.this.myHandler.postDelayed(this, 1000);
                        }
                    });
                    return;
                }
                prevBtnClick();
                return;
            case R.id.btnFastForward:
                int currentPosition2 = this.audioService.getCurrentPosition() + 10000;
                if (currentPosition2 <= this.audioService.getDuartion()) {
                    this.audioService.seekTo(currentPosition2);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (AudioPlayerActivity.this.audioService != null) {
                                int currentPosition = AudioPlayerActivity.this.audioService.getCurrentPosition();
                                AudioPlayerActivity.this.binding.seekBar.setProgress(currentPosition);
                                AudioPlayerActivity.this.binding.txtRunningTime.setText(AppConstants.formatTime((long) currentPosition));
                            }
                            AudioPlayerActivity.this.myHandler.postDelayed(this, 1000);
                        }
                    });
                    return;
                }
                nextBtnClick();
                return;
            case R.id.btnForward:
                if (AppPref.getBgAudioList().size() > 0) {
                    nextBtnClick();
                    return;
                }
                Intent intent = new Intent();
                intent.setAction(AppConstants.CLOSE);
                sendBroadcast(intent);
                return;
            case R.id.btnPrevious:
                if (AppPref.getBgAudioList().size() > 0) {
                    prevBtnClick();
                    return;
                }
                Intent intent2 = new Intent();
                intent2.setAction(AppConstants.CLOSE);
                sendBroadcast(intent2);
                return;
            case R.id.llAddToPlaylist:
                OpenPlayListBottomSheet();
                return;
            case R.id.llRepeating:
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
                    case -267444236:
                        if (audioState.equals(AppConstants.REPEAT_CURRENT)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 76453678:
                        if (audioState.equals(AppConstants.ORDER)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 2090883002:
                        if (audioState.equals(AppConstants.SHUFFLE_ALL)) {
                            c = 3;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        AppPref.setAudioState(AppConstants.ORDER);
                        break;
                    case 1:
                        AppPref.setAudioState(AppConstants.LOOP_ALL);
                        break;
                    case 2:
                        AppPref.setAudioState(AppConstants.SHUFFLE_ALL);
                        break;
                    case 3:
                        AppPref.setAudioState(AppConstants.REPEAT_CURRENT);
                        break;
                }
                setPlayerRepetitionImg();
                return;
            case R.id.llSpeed:
                OpenSpeedBottomSheet();
                return;
            case R.id.playPause:
                playPauseBtnClick();
                return;
            default:
                return;
        }
    }

    private void OpenSpeedBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetSpeedBinding bottomsheetSpeedBinding = (BottomsheetSpeedBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_speed, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetSpeedBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetSpeedBinding.seekBar.setProgress((int) this.audioSpeed);
        Log.d("TAG", "OpenSpeedBottomSheet: " + this.audioService.getPlayerSpeed());
        bottomsheetSpeedBinding.btn05x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerActivity.this.audioSpeed = 14.0d;
                AudioPlayerActivity.this.audioService.setAudioSpeed(0.5f);
            }
        });
        bottomsheetSpeedBinding.btn1x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerActivity.this.audioSpeed = 28.0d;
                AudioPlayerActivity.this.audioService.setAudioSpeed(1.0f);
            }
        });
        bottomsheetSpeedBinding.btn15x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerActivity.this.audioSpeed = 42.0d;
                AudioPlayerActivity.this.audioService.setAudioSpeed(1.5f);
            }
        });
        bottomsheetSpeedBinding.btn2x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerActivity.this.audioSpeed = 56.0d;
                AudioPlayerActivity.this.audioService.setAudioSpeed(2.0f);
            }
        });
        bottomsheetSpeedBinding.btn25x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerActivity.this.audioSpeed = 70.0d;
                AudioPlayerActivity.this.audioService.setAudioSpeed(2.5f);
            }
        });
        bottomsheetSpeedBinding.btn3x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerActivity.this.audioSpeed = 84.0d;
                AudioPlayerActivity.this.audioService.setAudioSpeed(2.5f);
            }
        });
        bottomsheetSpeedBinding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerActivity.this.audioService.setAudioSpeed(1.0f);
            }
        });
        bottomsheetSpeedBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu_player, menu);
        this.fav = menu.findItem(R.id.fav);
        this.unFav = menu.findItem(R.id.unFav);
        this.volumeOn = menu.findItem(R.id.volumeOn);
        this.volumeOff = menu.findItem(R.id.volumeOff);
        this.fav.setVisible(false);
        this.volumeOff.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            onBackPressed();
        } else if (menuItem.getItemId() == R.id.timer) {
            OpenTimerBottomSheet();
        } else if (menuItem.getItemId() == R.id.fav) {
            if (AppPref.getFavouriteList() != null && AppPref.getFavouriteList().contains(this.audioService.getPlayingModel())) {
                ArrayList<AudioVideoModal> favouriteList = AppPref.getFavouriteList();
                int indexOf = favouriteList.indexOf(this.audioService.getPlayingModel());
                if (indexOf != -1) {
                    favouriteList.remove(indexOf);
                }
                AppPref.getFavouriteList().clear();
                AppPref.setFavouriteList(favouriteList);
            }
            this.fav.setVisible(false);
            this.unFav.setVisible(true);
        } else if (menuItem.getItemId() == R.id.unFav) {
            if (AppPref.getFavouriteList() == null) {
                ArrayList arrayList = new ArrayList();
                AudioVideoModal playingModel = this.audioService.getPlayingModel();
                playingModel.setAudioVideoOrder(0);
                arrayList.add(playingModel);
                AppPref.setFavouriteList(arrayList);
            } else if (!AppPref.getFavouriteList().contains(this.audioService.getPlayingModel())) {
                ArrayList<AudioVideoModal> favouriteList2 = AppPref.getFavouriteList();
                AudioVideoModal playingModel2 = this.audioService.getPlayingModel();
                playingModel2.setAudioVideoOrder(AppPref.getFavouriteList().size() + 1);
                favouriteList2.add(playingModel2);
                AppPref.getFavouriteList().clear();
                AppPref.setFavouriteList(favouriteList2);
            }
            this.fav.setVisible(true);
            this.unFav.setVisible(false);
        } else if (menuItem.getItemId() == R.id.volumeOn) {
            Log.d("TAG", "onOptionsItemSelected: on");
            this.volumeOff.setVisible(true);
            this.volumeOn.setVisible(false);
            this.audioManager.setStreamVolume(3, 0, 0);
        } else if (menuItem.getItemId() == R.id.volumeOff) {
            Log.d("TAG", "onOptionsItemSelected: off");
            this.volumeOff.setVisible(false);
            this.volumeOn.setVisible(true);
            this.audioManager.setStreamVolume(3, this.mStreamVolume, AudioManager.FLAG_SHOW_UI);
        }
        return true;
    }

    private void OpenTimerBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetTimerBinding bottomsheetTimerBinding = (BottomsheetTimerBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_timer, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetTimerBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetTimerBinding.llOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AppPref.setAudioTimerTime(0);
                AudioPlayerActivity.this.audioService.stopTimer();
            }
        });
        bottomsheetTimerBinding.ll15Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AppPref.setAudioTimerTime(AudioPlayerActivity.this.getMilli(15));
                AudioPlayerActivity.this.audioService.stopTimer();
                AudioPlayerActivity.this.audioService.setWaitTimer();
            }
        });
        bottomsheetTimerBinding.ll30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AppPref.setAudioTimerTime(AudioPlayerActivity.this.getMilli(30));
                AudioPlayerActivity.this.audioService.stopTimer();
                AudioPlayerActivity.this.audioService.setWaitTimer();
            }
        });
        bottomsheetTimerBinding.ll45Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AppPref.setAudioTimerTime(AudioPlayerActivity.this.getMilli(45));
                AudioPlayerActivity.this.audioService.stopTimer();
                AudioPlayerActivity.this.audioService.setWaitTimer();
            }
        });
        bottomsheetTimerBinding.ll60Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AppPref.setAudioTimerTime(AudioPlayerActivity.this.getMilli(60));
                AudioPlayerActivity.this.audioService.stopTimer();
                AudioPlayerActivity.this.audioService.setWaitTimer();
            }
        });
        bottomsheetTimerBinding.llStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                AppPref.setAudioTimerTime((long) (AudioPlayerActivity.this.audioService.getDuartion() - AudioPlayerActivity.this.audioService.getCurrentPosition()));
                AudioPlayerActivity.this.audioService.stopTimer();
                AudioPlayerActivity.this.audioService.setWaitTimer();
            }
        });
    }

    @Override
    public void onResume() {
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            Intent intent = new Intent(MyApplication.getContext(), AudioService.class);
            intent.putExtra("model", AppPref.getBgAudioList().get(this.position));
            Log.d("AudioService:", "AudioPlayerActivity on resume bindService called");
            bindService(intent, this, 1);
        }
        Log.d("TAG", "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService(this);
        Log.d("TAG", "onPause: ");
    }

    private void registerReceiver() {
        MainActivityReceiver mainActivityReceiver = new MainActivityReceiver();
        this.mReceiver = mainActivityReceiver;

        IntentFilter intentFilter = new IntentFilter(AppConstants.MAIN_ACTIVITY_RECEIVER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mainActivityReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mainActivityReceiver, intentFilter);
        }
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
                    AudioPlayerActivity.this.myHandler.removeCallbacksAndMessages((Object) null);
                    AudioPlayerActivity.this.finish();
                }
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d("TAG", "playAudio: Connected");
        Log.d("AudioService:", "AudioPlayerActivity onServiceConnected called");
        AudioService service = ((AudioService.MyBinder) iBinder).getService();
        this.audioService = service;
        service.setCallBack(this);
        Log.d("TAG", "onStartCommand: FromActivity");
        this.binding.seekBar.setMax(this.audioService.getDuartion());
        this.binding.txtTotalTime.setText(AppConstants.formatTime((long) this.audioService.getDuartion()));
        this.binding.txtTitle.setText(AppPref.getBgAudioList().get(this.position).getName());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (AudioPlayerActivity.this.audioService != null && AudioPlayerActivity.this.audioService.isPlayerNotNull()) {
                    int currentPosition = AudioPlayerActivity.this.audioService.getCurrentPosition();
                    AudioPlayerActivity.this.binding.seekBar.setProgress(currentPosition);
                    AudioPlayerActivity.this.binding.txtRunningTime.setText(AppConstants.formatTime((long) currentPosition));
                }
                AudioPlayerActivity.this.myHandler.postDelayed(this, 1000);
            }
        });
        this.audioService.onCompleted();
        if (this.audioService.isPlaying()) {
            this.binding.playPause.setImageResource(R.drawable.main_pause);
        } else {
            this.binding.playPause.setImageResource(R.drawable.main_play);
        }
        setPlayingImage();
    }

    private void setPlayingImage() {
        if (!isDestroyed()) {
            AudioVideoModal playingModel = this.audioService.getPlayingModel();
            if (playingModel == null) {
                this.binding.cardArt.setVisibility(View.GONE);
                this.binding.imgPlayer.setVisibility(View.VISIBLE);
                if (this.audioService.isPlaying()) {
                    Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play_gif)).into(this.binding.imgPlayer);
                } else {
                    Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play_pause_round)).into(this.binding.imgPlayer);
                }
            } else if (!TextUtils.isEmpty(playingModel.getUri())) {
                Bitmap folderArt = AppConstants.setFolderArt(playingModel.getUri(), this);
                if (folderArt != null) {
                    this.binding.cardArt.setVisibility(View.VISIBLE);
                    Glide.with((FragmentActivity) this).load(folderArt).into(this.binding.imgArt);
                    this.binding.imgPlayer.setVisibility(View.GONE);
                    return;
                }
                this.binding.cardArt.setVisibility(View.GONE);
                this.binding.imgPlayer.setVisibility(View.VISIBLE);
                if (this.audioService.isPlaying()) {
                    Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play_gif)).into(this.binding.imgPlayer);
                } else {
                    Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play_pause_round)).into(this.binding.imgPlayer);
                }
            } else {
                this.binding.cardArt.setVisibility(View.GONE);
                this.binding.imgPlayer.setVisibility(View.VISIBLE);
                if (this.audioService.isPlaying()) {
                    Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play_gif)).into(this.binding.imgPlayer);
                } else {
                    Glide.with((FragmentActivity) this).load(Integer.valueOf(R.drawable.play_pause_round)).into(this.binding.imgPlayer);
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d("AudioService:", "AudioPlayerActivity onServiceDisconnected called");
        stopPlaying();
        this.audioService = null;
        Log.d("TAG", "onServiceDisconnected: ");
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        Log.d("TAG", "onBackPressed: Audio");
        AppPref.setFullScreenPlay(false);
        finish();
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
}
