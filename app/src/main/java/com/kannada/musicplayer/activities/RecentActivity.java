package com.kannada.musicplayer.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.adapter.HistoryAdapter;
import com.kannada.musicplayer.ads.AdsCommon;
import com.kannada.musicplayer.ads.MyApplication;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.databinding.ActivityRecentBinding;
import com.kannada.musicplayer.databinding.DialogDeleteBinding;
import com.kannada.musicplayer.model.HistoryModel;
import com.kannada.musicplayer.service.AudioService;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;
import com.kannada.musicplayer.utils.BetterActivityResult;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class RecentActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    HistoryAdapter adapter;
    AudioService audioService;
    ActivityRecentBinding binding;
    public CompositeDisposable disposable = new CompositeDisposable();
    List<HistoryModel> historyModelList = new ArrayList();
    MainActivityReceiver mReceiver;
    
    public Handler myHandler = new Handler();

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
        this.binding = (ActivityRecentBinding) DataBindingUtil.setContentView(this, R.layout.activity_recent);

        applyDisplayCutouts();


        //Reguler Banner Ads


        SetToolbar();
        LoadList();
        registerReceiver();
        Clicks();
    }

    private void Clicks() {
        this.binding.btnPrevious.setOnClickListener(this);
        this.binding.btnNext.setOnClickListener(this);
        this.binding.framePlayPause.setOnClickListener(this);
        this.binding.llContentPlayer.setOnClickListener(this);
    }

    private void SetToolbar() {
        setSupportActionBar(this.binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        this.binding.toolbarLayout.toolbar.setNavigationIcon((int) R.drawable.ic_back);
        this.binding.toolbarLayout.txtSubTitle.setText("History");
    }

    private void LoadList() {
        this.binding.progressBar.setVisibility(View.VISIBLE);
        //this.disposable.add(Observable.fromCallable(new RecentActivityObservable1(this)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new RecentActivityObservable2(this)));
        this.disposable.add(
                Observable.fromCallable(() -> {
                            if (AppPref.getRecentList() != null) {
                                historyModelList.addAll(AppPref.getRecentList());
                            }
                            return true;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aBoolean -> {
                            binding.progressBar.setVisibility(View.GONE);
                            setAdapter();
                            CheckNoData();
                        })
        );

    }


    
    public void CheckNoData() {
        if (this.historyModelList.size() > 0) {
            this.binding.rlNoData.setVisibility(View.GONE);
        } else {
            this.binding.rlNoData.setVisibility(View.VISIBLE);
        }
    }

    private void setAdapter() {
        this.adapter = new HistoryAdapter(this, this.historyModelList, new HistoryAdapter.OnHistoryClick() {
            @Override
            public void onListClick(int i, int i2, View view) {
                HistoryModel historyModel = RecentActivity.this.historyModelList.get(i);
                if (i2 == 1) {
                    if (AppPref.getBgAudioList() != null) {
                        AppPref.getBgAudioList().clear();
                    }
                    ArrayList arrayList = new ArrayList();
                    for (int i3 = 0; i3 < RecentActivity.this.historyModelList.size(); i3++) {
                        AudioVideoModal audioVideoModal = RecentActivity.this.historyModelList.get(i3).getAudioVideoModal();
                        arrayList.add(new AudioVideoModal(audioVideoModal.getUri(), audioVideoModal.getName(), audioVideoModal.getDuration(), audioVideoModal.getArtist(), audioVideoModal.getAlbum(), i3));
                    }
                    AppPref.setBgAudioList(arrayList);
                    Intent intent = new Intent(RecentActivity.this, AudioPlayerActivity.class);
                    int indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(historyModel.getAudioVideoModal().getUri()));
                    if (indexOf != -1) {
                        intent.putExtra("modal", AppPref.getBgAudioList().get(indexOf));
                    } else {
                        intent.putExtra("modal", AppPref.getBgAudioList().get(0));
                    }
                    RecentActivity.this.activityLauncher.launch(intent);
                    return;
                }
                RecentActivity.this.historyModelList.remove(i);
                AppPref.getRecentList().clear();
                AppPref.setRecentList(RecentActivity.this.historyModelList);
                RecentActivity.this.adapter.notifyItemRemoved(i);
                RecentActivity.this.adapter.notifyItemChanged(0);
                RecentActivity.this.CheckNoData();
            }
        });
        this.binding.recycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        this.binding.recycle.setAdapter(this.adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            onBackPressed();
            return true;
        } else if (menuItem.getItemId() != R.id.delete) {
            return true;
        } else {
            OpenDeleteDialog();
            return true;
        }
    }

    private void OpenDeleteDialog() {
        final Dialog dialog = new Dialog(this, R.style.dialogTheme);
        DialogDeleteBinding dialogDeleteBinding = (DialogDeleteBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_delete, (ViewGroup) null, false);
        dialog.setContentView(dialogDeleteBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (RecentActivity.this.historyModelList.size() > 0) {
                    RecentActivity.this.historyModelList.clear();
                    RecentActivity.this.adapter.notifyDataSetChanged();
                }
                if (AppPref.getRecentList() != null) {
                    AppPref.getRecentList().clear();
                    AppPref.setRecentList(RecentActivity.this.historyModelList);
                }
                RecentActivity.this.CheckNoData();
            }
        });
        dialogDeleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void getPlayingModel() {
        AudioService audioService2 = this.audioService;
        if (audioService2 != null) {
            AudioVideoModal playingModel = audioService2.getPlayingModel();
            Bitmap bitmap = this.audioService.getBitmap(playingModel);
            if (bitmap != null) {
                this.binding.playArt.setVisibility(View.VISIBLE);
                Glide.with((FragmentActivity) this).load(bitmap).into(this.binding.playArt);
            } else {
                this.binding.playArt.setVisibility(View.GONE);
            }
            this.binding.txtRunningSong.setText(playingModel.getName());
            this.binding.txtTotalTime.setText(AppConstants.formatTime(playingModel.getDuration()));
            this.binding.progress.setMax((int) playingModel.getDuration());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (RecentActivity.this.audioService != null && RecentActivity.this.audioService.isPlayerNotNull()) {
                        int currentPosition = RecentActivity.this.audioService.getCurrentPosition();
                        RecentActivity.this.binding.progress.setProgress(currentPosition);
                        RecentActivity.this.binding.txtRunningTime.setText(AppConstants.formatTime((long) currentPosition));
                    }
                    RecentActivity.this.myHandler.postDelayed(this, 1000);
                }
            });
        }
    }

    public void bindService() {
        bindService(new Intent(MyApplication.getContext(), AudioService.class), this, 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            bindService();
        }
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (AppConstants.isMyServiceRunning(this, AudioService.class) && this.audioService != null) {
            try {
                unbindService(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mReceiver = null;
        }
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

    private void registerReceiver() {
        MainActivityReceiver mainActivityReceiver = new MainActivityReceiver();
        this.mReceiver = mainActivityReceiver;
        IntentFilter filter = new IntentFilter(AppConstants.MAIN_ACTIVITY_RECEIVER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mainActivityReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mainActivityReceiver, filter);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        this.audioService = ((AudioService.MyBinder) iBinder).getService();
        this.binding.llPlayerBottom.setVisibility(View.VISIBLE);
        getPlayingModel();
        if (this.audioService.isPlaying()) {
            this.binding.playPauseImg.setImageResource(R.drawable.main_pause);
        } else {
            this.binding.playPauseImg.setImageResource(R.drawable.main_play);
        }
        setAdapterPlayingSong();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        this.audioService = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNext:
                AudioService audioService2 = this.audioService;
                if (audioService2 != null) {
                    audioService2.nextButton();
                    this.binding.playPauseImg.setImageResource(R.drawable.main_pause);
                    return;
                }
                return;
            case R.id.btnPrevious:
                AudioService audioService3 = this.audioService;
                if (audioService3 != null) {
                    audioService3.previousButton();
                    this.binding.playPauseImg.setImageResource(R.drawable.main_pause);
                    return;
                }
                return;
            case R.id.framePlayPause:
                AudioService audioService4 = this.audioService;
                if (audioService4 != null) {
                    audioService4.playPauseClick();
                    return;
                }
                return;
            case R.id.llContentPlayer:
                Intent intent = new Intent();
                intent.setAction(AppConstants.CONTENT);
                sendBroadcast(intent);
                return;
            default:
                return;
        }
    }

    public class MainActivityReceiver extends BroadcastReceiver {
        public MainActivityReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            char c;
            if (intent.getAction().equals(AppConstants.MAIN_ACTIVITY_RECEIVER)) {
                String stringExtra = intent.getStringExtra(NotificationCompat.CATEGORY_STATUS);
                stringExtra.hashCode();
                switch (stringExtra.hashCode()) {
                    case -2026200673:
                        if (stringExtra.equals("RUNNING")) {
                            c = 0;
                            break;
                        }
                    case -971121397:
                        if (stringExtra.equals(AppConstants.PLAY_PAUSE)) {
                            c = 1;
                            break;
                        }
                    case 2555906:
                        if (stringExtra.equals(AppConstants.STOP)) {
                            c = 2;
                            break;
                        }
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        int indexOf = RecentActivity.this.historyModelList.indexOf(new HistoryModel(AppPref.getBgAudioList().get(intent.getIntExtra("position", 0))));
                        if (indexOf != -1) {
                            RecentActivity.this.adapter.setPlayingPos(indexOf);
                            RecentActivity.this.adapter.setIsPlaying(true);
                        } else {
                            RecentActivity.this.adapter.setIsPlaying(false);
                        }
                        RecentActivity.this.getPlayingModel();
                        return;
                    case 1:
                        if (intent.getIntExtra("action", 0) == 1) {
                            RecentActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_play);
                        } else {
                            RecentActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_pause);
                        }
                        RecentActivity.this.setAdapterPlayingSong();
                        return;
                    case 2:
                        RecentActivity.this.binding.llPlayerBottom.setVisibility(View.GONE);
                        if (AppConstants.isMyServiceRunning(RecentActivity.this, AudioService.class)) {
                            try {
                                RecentActivity recentActivity = RecentActivity.this;
                                recentActivity.unbindService(recentActivity);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        RecentActivity.this.adapter.setIsPlaying(false);
                        RecentActivity.this.myHandler.removeCallbacksAndMessages((Object) null);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public void setAdapterPlayingSong() {
        HistoryAdapter historyAdapter;
        AudioService audioService2 = this.audioService;
        if (audioService2 != null) {
            AudioVideoModal playingModel = audioService2.getPlayingModel();
            int indexOf = this.historyModelList.indexOf(new HistoryModel(playingModel));
            Log.d("TAG", "setAdapterPlayingSong: " + playingModel.getName() + " || " + indexOf);
            if (indexOf != -1 && (historyAdapter = this.adapter) != null) {
                historyAdapter.setPlayingPos(indexOf);
                this.adapter.setIsPlaying(true);
            }
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
