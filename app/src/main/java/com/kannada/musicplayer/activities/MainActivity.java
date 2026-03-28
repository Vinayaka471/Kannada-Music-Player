package com.kannada.musicplayer.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.kannada.musicplayer.Permission.GivePermission;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.ads.MyApplication;
import com.kannada.musicplayer.database.AppDatabase;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.databinding.ActivityMainBinding;
import com.kannada.musicplayer.databinding.BottomsheetSortingBinding;
import com.kannada.musicplayer.fragment.MusicFragment;
import com.kannada.musicplayer.fragment.SettingsFragment;
import com.kannada.musicplayer.fragment.VideoFragment;
import com.kannada.musicplayer.model.AlbumModel;
import com.kannada.musicplayer.model.ArtistModel;
import com.kannada.musicplayer.model.AudioFolderModal;
import com.kannada.musicplayer.model.AudioModel;
import com.kannada.musicplayer.model.VideoFolderModal;
import com.kannada.musicplayer.model.VideoModal;
import com.kannada.musicplayer.service.AudioService;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;
import com.kannada.musicplayer.utils.BetterActivityResult;
import com.kannada.musicplayer.utils.CallStateBroadcast;
import com.kannada.musicplayer.utils.HardButtonReceiver;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.NavigationViewUtil;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {


    private static Context context;
    public List<VideoModal> AllVideosList = new ArrayList();
    int READ_PHONE = 200;
    //String[] READ_STATE = {"android.permission.READ_PHONE_STATE"};
    //int READ_WRITE = 100;
    //String[] PERMISSION_FINAL;
    //String[] READ_WRITE_EXTERNAL_STORAGE33 = {"android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
    //String[] READ_WRITE_EXTERNAL_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    public ActionMode actionMode;
    Fragment activeFragment;
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    public List<AlbumModel> albumList = new ArrayList();
    AlbumModel albumModel;
    AppDatabase appDatabase;
    public List<ArtistModel> artistList = new ArrayList();
    ArtistModel artistModel;
    AudioFolderModal audioFolderModal;
    AudioService audioService;
    ActivityMainBinding binding;
    HardButtonReceiver buttonReceiver;
    public ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            if (MainActivity.this.activeFragment.equals(MainActivity.this.musicFragment)) {
                MainActivity.this.musicFragment.isMultiSelect = true;
            } else if (MainActivity.this.activeFragment.equals(MainActivity.this.videoFragment)) {
                MainActivity.this.videoFragment.isMultiSelect = true;
            }
            actionMode.getMenuInflater().inflate(R.menu.video_multi_select, menu);
            MainActivity.this.actionMode = actionMode;
            MainActivity.this.delete = menu.findItem(R.id.delete);
            MainActivity.this.delete.setVisible(Build.VERSION.SDK_INT <= 29);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (MainActivity.this.activeFragment.equals(MainActivity.this.musicFragment)) {
                if (menuItem.getItemId() == R.id.addToPlaylist) {
                    int GetVisibleData = MainActivity.this.musicFragment.GetVisibleData();
                    if (GetVisibleData == 4) {
                        MainActivity.this.musicFragment.AddAudiosInPlayList(MainActivity.this.musicFragment.MultiAudioList);
                    } else if (GetVisibleData == 1) {
                        ArrayList arrayList = new ArrayList();
                        for (int i = 0; i < MainActivity.this.musicFragment.MultiFolderList.size(); i++) {
                            arrayList.addAll(MainActivity.this.musicFragment.MultiFolderList.get(i).getAudioList());
                        }
                        MainActivity.this.musicFragment.AddAudiosInPlayList(arrayList);
                    } else if (GetVisibleData == 2) {
                        ArrayList arrayList2 = new ArrayList();
                        for (int i2 = 0; i2 < MainActivity.this.musicFragment.MultiAlbumList.size(); i2++) {
                            arrayList2.addAll(MainActivity.this.musicFragment.MultiAlbumList.get(i2).getAudioModelList());
                        }
                        MainActivity.this.musicFragment.AddAudiosInPlayList(arrayList2);
                    } else if (GetVisibleData == 3) {
                        ArrayList arrayList3 = new ArrayList();
                        for (int i3 = 0; i3 < MainActivity.this.musicFragment.MultiArtistList.size(); i3++) {
                            arrayList3.addAll(MainActivity.this.musicFragment.MultiArtistList.get(i3).getAudioModelList());
                        }
                        MainActivity.this.musicFragment.AddAudiosInPlayList(arrayList3);
                    }
                } else if (menuItem.getItemId() == R.id.delete) {
                    int GetVisibleData2 = MainActivity.this.musicFragment.GetVisibleData();
                    if (GetVisibleData2 == 4) {
                        MainActivity.this.musicFragment.DeleteAudios();
                    } else {
                        MainActivity.this.musicFragment.MultiDeleteAudios(GetVisibleData2);
                    }
                    MainActivity.this.actionMode.finish();
                } else if (menuItem.getItemId() == R.id.share) {
                    int GetVisibleData3 = MainActivity.this.musicFragment.GetVisibleData();
                    if (GetVisibleData3 == 4) {
                        MainActivity.this.musicFragment.ShareAudios(MainActivity.this.musicFragment.MultiAudioList);
                    } else if (GetVisibleData3 == 1) {
                        ArrayList arrayList4 = new ArrayList();
                        for (int i4 = 0; i4 < MainActivity.this.musicFragment.MultiFolderList.size(); i4++) {
                            arrayList4.addAll(MainActivity.this.musicFragment.MultiFolderList.get(i4).getAudioList());
                        }
                        MainActivity.this.musicFragment.ShareAudios(arrayList4);
                    } else if (GetVisibleData3 == 2) {
                        ArrayList arrayList5 = new ArrayList();
                        for (int i5 = 0; i5 < MainActivity.this.musicFragment.MultiAlbumList.size(); i5++) {
                            arrayList5.addAll(MainActivity.this.musicFragment.MultiAlbumList.get(i5).getAudioModelList());
                        }
                        MainActivity.this.musicFragment.ShareAudios(arrayList5);
                    } else if (GetVisibleData3 == 3) {
                        ArrayList arrayList6 = new ArrayList();
                        for (int i6 = 0; i6 < MainActivity.this.musicFragment.MultiArtistList.size(); i6++) {
                            arrayList6.addAll(MainActivity.this.musicFragment.MultiArtistList.get(i6).getAudioModelList());
                        }
                        MainActivity.this.musicFragment.ShareAudios(arrayList6);
                    }
                } else if (menuItem.getItemId() == R.id.properties) {
                    int GetVisibleData4 = MainActivity.this.musicFragment.GetVisibleData();
                    if (GetVisibleData4 == 4) {
                        MainActivity.this.musicFragment.AudiosProperties(MainActivity.this.musicFragment.MultiAudioList);
                    } else if (GetVisibleData4 == 1) {
                        ArrayList arrayList7 = new ArrayList();
                        for (int i7 = 0; i7 < MainActivity.this.musicFragment.MultiFolderList.size(); i7++) {
                            arrayList7.addAll(MainActivity.this.musicFragment.MultiFolderList.get(i7).getAudioList());
                        }
                        MainActivity.this.musicFragment.AudiosProperties(arrayList7);
                    } else if (GetVisibleData4 == 2) {
                        ArrayList arrayList8 = new ArrayList();
                        for (int i8 = 0; i8 < MainActivity.this.musicFragment.MultiAlbumList.size(); i8++) {
                            arrayList8.addAll(MainActivity.this.musicFragment.MultiAlbumList.get(i8).getAudioModelList());
                        }
                        MainActivity.this.musicFragment.AudiosProperties(arrayList8);
                    } else if (GetVisibleData4 == 3) {
                        ArrayList arrayList9 = new ArrayList();
                        for (int i9 = 0; i9 < MainActivity.this.musicFragment.MultiArtistList.size(); i9++) {
                            arrayList9.addAll(MainActivity.this.musicFragment.MultiArtistList.get(i9).getAudioModelList());
                        }
                        MainActivity.this.musicFragment.AudiosProperties(arrayList9);
                    }
                }
            } else if (MainActivity.this.activeFragment.equals(MainActivity.this.videoFragment)) {
                if (menuItem.getItemId() == R.id.delete) {
                    MainActivity.this.videoFragment.DeleteVideos();
                } else if (menuItem.getItemId() == R.id.addToPlaylist) {
                    MainActivity.this.videoFragment.AddToPlayListVideos();
                } else if (menuItem.getItemId() == R.id.share) {
                    MainActivity.this.videoFragment.ShareVideo();
                } else if (menuItem.getItemId() == R.id.properties) {
                    MainActivity.this.videoFragment.VideosProperties();
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            Log.d("TAG", "onDestroyActionMode: ");
            if (MainActivity.this.activeFragment.equals(MainActivity.this.musicFragment)) {
                MainActivity.this.RefreshMultiSelectMusic();
            } else if (MainActivity.this.activeFragment.equals(MainActivity.this.videoFragment)) {
                MainActivity.this.RefreshMultiSelectVideo();
            }
        }
    };
    public MenuItem delete;
    public CompositeDisposable disposable = new CompositeDisposable();
    public List<AudioFolderModal> folderList = new ArrayList();
    FragmentManager fragmentManager;
    MenuItem grid;
    boolean isChangeSort = false;
    MenuItem list;
    MainActivityReceiver mReceiver;
    MusicFragment musicFragment;
    MenuItem musicItem;
    public Handler myHandler = new Handler();
    int oType = this.orderType;
    public int orderType = 2;
    public String sortType = AppConstants.DATE_WISE;
    String sType = this.sortType;
    MenuItem search;
    SearchView searchView;
    SettingsFragment settingsFragment;
    public List<AudioModel> songsList = new ArrayList();
    MenuItem sort;

    int tempPos = -1;
    public List<VideoFolderModal> videoFolderList = new ArrayList();
    VideoFolderModal videoFolderModal;
    VideoFragment videoFragment;

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
        this.binding = (ActivityMainBinding) DataBindingUtil.setContentView(this, R.layout.activity_main);
        this.appDatabase = AppDatabase.getAppDatabase(this);
        context = this;

        applyDisplayCutouts();

        GivePermission.applyPermission();

        setToolbar();

        this.binding.navView.inflateHeaderView(R.layout.ox_nav_header_main);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle((Activity) this.context, this.binding.drawerLayout, this.binding.toolbarLayout.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        Context context2 = this.context;
        NavigationViewUtil.setItemIconColors(this.binding.navView, ATHUtil.resolveColor(context2, R.attr.iconColor, getResources().getColor(R.color.white)), getResources().getColor(R.color.white));
        NavigationViewUtil.setItemTextColors(this.binding.navView, getResources().getColor(R.color.white), getResources().getColor(R.color.white));
        this.binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.favourite:
                        activityLauncher.launch(new Intent(MainActivity.this, FavouriteListActivity.class));
                        binding.drawerLayout.closeDrawer((int) GravityCompat.START);
                        return true;
                    case R.id.playlist:
                        activityLauncher.launch(new Intent(MainActivity.this, CreatePlaylistActivity.class));
                        binding.drawerLayout.closeDrawer((int) GravityCompat.START);
                        return true;
                    case R.id.history:
                        activityLauncher.launch(new Intent(MainActivity.this, RecentActivity.class));
                        binding.drawerLayout.closeDrawer((int) GravityCompat.START);
                        return true;
                    case R.id.videoplayer:
                        HideTopLL(false);
                        setSearchCollapse();
                        ShowFragment(videoFragment);
                        SortList();
                        if (musicFragment.isMultiSelect) {
                            musicFragment.ClearMultiSelection();
                        }
                        SetRecentVisibility();
                        if (videoFragment.isMultiSelect) {
                            RefreshMultiSelectVideo();
                        }
                        SetSelection(binding.imgVideo, binding.txtVideo, binding.imgMusic, binding.txtMusic, binding.imgSetting, binding.txtSetting, true);
                        binding.drawerLayout.closeDrawer((int) GravityCompat.START);
                        return true;
                    case R.id.musicplayer:
                        HideTopLL(false);
                        setSearchCollapse();
                        ShowFragment(musicFragment);
                        SortList();
                        musicFragment.CheckNoData();
                        if (!AppConstants.isMyServiceRunning(MainActivity.this, AudioService.class)) {
                            NotifyForSongPlaying();
                        }
                        setAdapterPlayingSong();
                        if (videoFragment.isMultiSelect) {
                            videoFragment.ClearMultiSelection();
                        }
                        SetSelection(binding.imgMusic, binding.txtMusic, binding.imgVideo, binding.txtVideo, binding.imgSetting, binding.txtSetting, true);
                        if (musicFragment.isMultiSelect) {
                            RefreshMultiSelectMusic();
                        }
                        binding.drawerLayout.closeDrawer((int) GravityCompat.START);
                        return true;
                    case R.id.nav_setting:
                        binding.drawerLayout.closeDrawer((int) GravityCompat.START);
                        HideTopLL(true);
                        setSearchCollapse();
                        ShowFragment(settingsFragment);
                        if (videoFragment.isMultiSelect) {
                            videoFragment.ClearMultiSelection();
                        }
                        if (musicFragment.isMultiSelect) {
                            musicFragment.ClearMultiSelection();
                        }
                        SetSelection(binding.imgSetting, binding.txtSetting, binding.imgMusic, binding.txtMusic, binding.imgVideo, binding.txtVideo, true);
                        return true;
                    case R.id.nav_rate:
                        final String rateapp = getPackageName();
                        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + rateapp));
                        startActivity(intent1);
                        return true;
                    case R.id.nav_share:
                        String appName = getResources().getString(R.string.app_name);
                        final String appPackageName = getPackageName();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, appName + " : \nhttps://play.google.com/store/apps/details?id=" + appPackageName);
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                        binding.drawerLayout.closeDrawer((int) GravityCompat.START);
                        return true;
                    case R.id.nav_privacy:
                        Intent intentPrivacy = new Intent(Intent.ACTION_VIEW, Uri.parse(MyApplication.PrivacyPolicy));
                        intentPrivacy.setPackage("com.android.chrome");
                        startActivity(intentPrivacy);
                        binding.drawerLayout.closeDrawer((int) GravityCompat.START);
                        return true;
                    default:
                        return false;
                }
            }
        });
        this.binding.drawerLayout.setViewScale(GravityCompat.START, 0.9f);
        this.binding.drawerLayout.setRadius(GravityCompat.START, 35.0f);
        this.binding.drawerLayout.setViewElevation(GravityCompat.START, 20.0f);
        this.binding.drawerLayout.setScrimColor(getResources().getColor(R.color.bottomBg));
        this.binding.drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.bottomBg));
        this.binding.drawerLayout.setBackgroundColor(getResources().getColor(R.color.bottomBg));
        this.binding.navView.setBackgroundColor(getResources().getColor(R.color.bottomBg));

        Menu menu = this.binding.navView.getMenu();

        MenuItem tools = menu.findItem(R.id.tools);
        SpannableString toolspan = new SpannableString(tools.getTitle());
        toolspan.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance44), 0, toolspan.length(), 0);
        tools.setTitle(toolspan);

        MenuItem communi = menu.findItem(R.id.communication);
        SpannableString commuspan = new SpannableString(communi.getTitle());
        commuspan.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance44), 0, commuspan.length(), 0);
        communi.setTitle(commuspan);


        SetSelection(this.binding.imgVideo, this.binding.txtVideo, this.binding.imgMusic, this.binding.txtMusic, this.binding.imgSetting, this.binding.txtSetting, true);
        initFragment();
        getReadWritePermission();
        Clicks();
        registerReceiver();
        CallStateBroadcast callStateBroadcast = new CallStateBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("state");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(callStateBroadcast, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(callStateBroadcast, intentFilter);
        }
        SetRecentVisibility();
        SetMusicVisibility(AppPref.IsShowMusic());
        if (!AppConstants.isMyServiceRunning(this, AudioService.class)) {
            try {
                AppConstants.deleteTempFile(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void RefreshMultiSelectVideo() {
        this.videoFragment.isMultiSelect = false;
        this.videoFragment.NotifyForLongClick(false);
        this.videoFragment.MultiVideoList.clear();
    }


    public void RefreshMultiSelectMusic() {
        this.musicFragment.isMultiSelect = false;
        this.musicFragment.NotifyForLongClick(false);
        this.musicFragment.MultiAudioList.clear();
        this.musicFragment.NotifyForLongClickFolder(false);
        this.musicFragment.MultiFolderList.clear();
        this.musicFragment.NotifyForLongClickAlbum(false);
        this.musicFragment.MultiAlbumList.clear();
        this.musicFragment.NotifyForLongClickArtist(false);
        this.musicFragment.MultiArtistList.clear();
    }

    private void setToolbar() {
        setSupportActionBar(this.binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
        //this.binding.toolbarLayout.toolbar.setNavigationIcon(getDrawable(R.mipmap.ic_launcher));
        this.binding.toolbarLayout.txtTitle.setText("Video Player");
    }

    private void initFragment() {
        this.videoFragment = new VideoFragment();
        this.musicFragment = new MusicFragment();
        this.settingsFragment = new SettingsFragment();
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        this.fragmentManager = supportFragmentManager;
        supportFragmentManager.beginTransaction().add((int) R.id.frame, (Fragment) this.videoFragment, "1").show(this.videoFragment).commit();
        this.activeFragment = this.videoFragment;
        this.fragmentManager.beginTransaction().add((int) R.id.frame, (Fragment) this.musicFragment, "2").hide(this.musicFragment).commit();
        this.fragmentManager.beginTransaction().add((int) R.id.frame, (Fragment) this.settingsFragment, "3").hide(this.settingsFragment).commit();
    }

    public void ShowFragment(Fragment fragment) {
        if (!this.activeFragment.equals(fragment)) {
            if (GetOldFragmentPosition() < GetNewFragmentPosition(fragment)) {
                this.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).show(fragment).hide(this.activeFragment).commit();
            } else {
                this.fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left).show(fragment).hide(this.activeFragment).commit();
            }
            this.activeFragment = fragment;
            VideoFragment videoFragment2 = this.videoFragment;
            if (fragment == videoFragment2) {
                if (videoFragment2.isGridVisible()) {
                    this.grid.setVisible(true);
                    this.list.setVisible(false);
                } else {
                    this.grid.setVisible(false);
                    this.list.setVisible(true);
                }
                this.sort.setVisible(true);
                this.search.setVisible(true);
                this.binding.toolbarLayout.txtTitle.setText("Video Player");
            } else if (fragment == this.settingsFragment) {
                this.sort.setVisible(false);
                this.search.setVisible(false);
                this.grid.setVisible(false);
                this.list.setVisible(false);
                this.binding.toolbarLayout.txtTitle.setText("Settings");
            } else {
                this.grid.setVisible(false);
                this.list.setVisible(false);
                this.sort.setVisible(false);
                this.search.setVisible(true);
                this.binding.toolbarLayout.txtTitle.setText("Music Player");
            }
        }
    }

    private void LoadVideoUriList() {
        Cursor cursor;
        String str;
        String str2;
        String str3;
        Cursor query = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "date_modified", "bucket_display_name", "_size", "duration", "_display_name", "bucket_id", "date_added"}, (String) null, (String[]) null, "date_added DESC");
        if (query != null && query.getCount() > 0) {
            while (query.moveToNext()) {
                String valueOf = String.valueOf(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, query.getLong(query.getColumnIndex("_id"))));
                String string = query.getString(query.getColumnIndexOrThrow("_display_name"));
                long j = query.getLong(query.getColumnIndex("_size"));
                int columnIndex = query.getColumnIndex("bucket_display_name");
                long j2 = query.getLong(query.getColumnIndex("date_modified"));
                long j3 = query.getLong(query.getColumnIndex("date_added"));
                long j4 = query.getLong(query.getColumnIndex("duration"));
                String string2 = query.getString(query.getColumnIndex("bucket_id"));
                String string3 = query.getString(columnIndex);
                if (j4 != 0) {
                    if (string3 != null) {
                        VideoFolderModal videoFolderModal2 = new VideoFolderModal(string3, string2);
                        this.videoFolderModal = videoFolderModal2;
                        if (this.videoFolderList.contains(videoFolderModal2)) {
                            int indexOf = this.videoFolderList.indexOf(this.videoFolderModal);
                            this.tempPos = indexOf;
                            this.videoFolderList.get(indexOf).setCount();
                            this.videoFolderList.get(this.tempPos).setaPath(valueOf);
                            cursor = query;
                            String str4 = valueOf;
                            VideoFolderModal videoFolderModal3 = this.videoFolderList.get(this.tempPos);
                            str2 = string3;
                            str = string2;
                            VideoModal videoModal = new VideoModal(valueOf, string, j, j2 * 1000, j4, j3, string2, str2);
                            videoFolderModal3.addVideo(videoModal);
                            str3 = str4;
                        } else {
                            cursor = query;
                            String str5 = valueOf;
                            str2 = string3;
                            str = string2;
                            this.videoFolderModal.getVideoList().add(new VideoModal(str5, string, j, j2 * 1000, j4, j3, str, str2));
                            str3 = str5;
                            this.videoFolderModal.setaPath(str3);
                            this.videoFolderModal.setCount();
                            this.videoFolderList.add(this.videoFolderModal);
                        }
                        List<VideoModal> list2 = this.AllVideosList;
                        VideoModal videoModal3 = new VideoModal(str3, string, j, j2 * 1000, j4, j3, str, str2);
                        list2.add(videoModal3);
                    } else {
                        cursor = query;
                    }
                    query = cursor;
                }
            }
        }
        Cursor cursor2 = query;
        if (cursor2 != null) {
            cursor2.close();
        }
    }

    private void LoadVideoDataList() {
        String str;
        String str2;
        Cursor cursor;
        String str3;
        Cursor query = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_data", "date_modified", "date_added", "bucket_display_name", "_size", "duration", "_display_name", "bucket_id"}, (String) null, (String[]) null, "date_added DESC");
        if (query != null && query.moveToFirst()) {
            while (query.moveToNext()) {
                String string = query.getString(query.getColumnIndexOrThrow("_data"));
                String string2 = query.getString(query.getColumnIndexOrThrow("_display_name"));
                long j = query.getLong(query.getColumnIndex("_size"));
                int columnIndex = query.getColumnIndex("bucket_display_name");
                long j2 = query.getLong(query.getColumnIndex("date_modified"));
                long j3 = query.getLong(query.getColumnIndex("date_added"));
                long j4 = query.getLong(query.getColumnIndex("duration"));
                String string3 = query.getString(query.getColumnIndex("bucket_id"));
                String string4 = query.getString(columnIndex);
                String str4 = Environment.getExternalStorageDirectory() + File.separator + string4;
                if (j4 != 0) {
                    if (string4 != null) {
                        VideoFolderModal videoFolderModal2 = new VideoFolderModal(string4, string3);
                        this.videoFolderModal = videoFolderModal2;
                        videoFolderModal2.setFolderPath(str4);
                    }
                    if (this.videoFolderList.contains(this.videoFolderModal)) {
                        int indexOf = this.videoFolderList.indexOf(this.videoFolderModal);
                        this.tempPos = indexOf;
                        this.videoFolderList.get(indexOf).setCount();
                        this.videoFolderList.get(this.tempPos).setaPath(string);
                        cursor = query;
                        String str5 = string;
                        VideoFolderModal videoFolderModal3 = this.videoFolderList.get(this.tempPos);
                        str2 = string4;
                        str = string3;
                        VideoModal videoModal = new VideoModal(string, string2, j, j2 * 1000, j4, j3, string3, str2);
                        videoFolderModal3.addVideo(videoModal);
                        str3 = str5;
                    } else {
                        cursor = query;
                        String str6 = string;
                        str2 = string4;
                        str = string3;
                        this.videoFolderModal.getVideoList().add(new VideoModal(str6, string2, j, j2 * 1000, j4, j3, str, str2));
                        str3 = str6;
                        this.videoFolderModal.setaPath(str3);
                        this.videoFolderModal.setCount();
                        this.videoFolderList.add(this.videoFolderModal);
                    }
                    List<VideoModal> list2 = this.AllVideosList;
                    VideoModal videoModal3 = new VideoModal(str3, string2, j, j2 * 1000, j4, j3, str, str2);
                    list2.add(videoModal3);
                    query = cursor;
                }
            }
        }
        Cursor cursor2 = query;
        if (cursor2 != null) {
            cursor2.close();
        }
    }

    private boolean videoFileIsCorrupted(String str) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            if (Build.VERSION.SDK_INT > 29) {
                mediaMetadataRetriever.setDataSource(this, Uri.parse(str));
            } else {
                mediaMetadataRetriever.setDataSource(this, FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(str)));
            }
            return "yes".equals(mediaMetadataRetriever.extractMetadata(17));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean audioFileIsCorrupted(String str) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            if (Build.VERSION.SDK_INT > 29) {
                mediaMetadataRetriever.setDataSource(this, Uri.parse(str));
            } else {
                mediaMetadataRetriever.setDataSource(this, FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(str)));
            }
            return "yes".equals(mediaMetadataRetriever.extractMetadata(16));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void LoadAudioUriList() throws Throwable {
        String str;
        Cursor cursor;
        String str2;
        boolean z;
        AudioModel audioModel;
        String str3;
        String str4 = "duration";
        String str5 = "_display_name";
        Cursor cursor2 = null;
        try {
            cursor2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_display_name", "track", "year", "duration", "_data", "date_modified", "album_id", "album", "artist_id", "artist", "composer", "title", "_size", "bucket_display_name", "bucket_id", "date_added"}, (String) null, (String[]) null, "date_added DESC");
            while (cursor2.moveToNext()) {
                try {
                    if (cursor2.getCount() > 0) {
                        int columnIndexOrThrow = cursor2.getColumnIndexOrThrow("_size");
                        if (!TextUtils.isEmpty(cursor2.getString(columnIndexOrThrow))) {
                            if (cursor2.getString(columnIndexOrThrow) != null) {
                                if (Long.parseLong(cursor2.getString(columnIndexOrThrow)) > 0) {
                                    z = true;
                                    if (z) {
                                        Uri withAppendedPath = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor2.getString(cursor2.getColumnIndex("_id")));
                                        int columnIndexOrThrow2 = cursor2.getColumnIndexOrThrow("title");
                                        int columnIndexOrThrow3 = cursor2.getColumnIndexOrThrow(str5);
                                        long columnIndex = (long) cursor2.getColumnIndex(str4);
                                        String string = cursor2.getString(cursor2.getColumnIndex("artist"));
                                        int i = columnIndexOrThrow2;
                                        Uri withAppendedId = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), (long) cursor2.getInt(cursor2.getColumnIndex("album_id")));
                                        String extension = FilenameUtils.getExtension(cursor2.getString(columnIndexOrThrow3));
                                        String string2 = cursor2.getString(cursor2.getColumnIndex("album"));
                                        long j = cursor2.getLong(cursor2.getColumnIndex(str4));
                                        String string3 = cursor2.getString(cursor2.getColumnIndex(str5));
                                        int columnIndex2 = cursor2.getColumnIndex("bucket_display_name");
                                        int columnIndex3 = cursor2.getColumnIndex("bucket_id");
                                        long j2 = cursor2.getLong(cursor2.getColumnIndex("date_added"));
                                        String string4 = cursor2.getString(columnIndex2);
                                        cursor2.getString(columnIndex3);
                                        if (j != 0) {
                                            AudioModel audioModel2 = new AudioModel();
                                            str2 = str4;
                                            audioModel2.setUri(withAppendedPath.toString());
                                            audioModel2.setPath(withAppendedPath.toString());
                                            audioModel2.setName(cursor2.getString(i));
                                            int i2 = (int) columnIndex;
                                            audioModel2.setTime(AppConstants.GetTimeNew(cursor2.getLong(i2)));
                                            audioModel2.setSize(AppConstants.getSize(Long.parseLong(cursor2.getString(columnIndexOrThrow))));
                                            audioModel2.setLongSize(Long.parseLong(cursor2.getString(columnIndexOrThrow)));
                                            audioModel2.setDuration(cursor2.getLong(i2));
                                            audioModel2.setPlayed(false);
                                            audioModel2.setAlbumId(withAppendedId.toString());
                                            audioModel2.setAlbumName(string2);
                                            audioModel2.setArtist(string);
                                            audioModel2.setType(extension);
                                            audioModel2.setCreationDate(j2);
                                            if (!TextUtils.isEmpty(audioModel2.getAlbumName()) && !TextUtils.isEmpty(audioModel2.getArtist()) && !audioModel2.getTime().equals("00:00")) {
                                                this.songsList.add(audioModel2);
                                                ArtistModel artistModel2 = new ArtistModel(audioModel2.getArtist());
                                                this.artistModel = artistModel2;
                                                if (this.artistList.contains(artistModel2)) {
                                                    int indexOf = this.artistList.indexOf(this.artistModel);
                                                    this.tempPos = indexOf;
                                                    this.artistList.get(indexOf).setCount();
                                                    this.artistList.get(this.tempPos).setNumOfAlbum("");
                                                    ArtistModel artistModel3 = this.artistList.get(this.tempPos);
                                                    String path = audioModel2.getPath();
                                                    String size = AppConstants.getSize(audioModel2.getLongSize());
                                                    String str6 = str5;
                                                    cursor = cursor2;
                                                    try {
                                                        str = str6;
                                                        str3 = string4;
                                                        audioModel = audioModel2;
                                                        artistModel3.addAudio(new AudioModel(string3, path, size, (long) columnIndexOrThrow, AppConstants.GetTimeNew(j), extension, withAppendedPath.toString(), string, withAppendedId.toString(), string2, false, j, j2));
                                                    } catch (Throwable th) {
                                                        th = th;
                                                        cursor2 = cursor;
                                                        if (cursor2 != null) {
                                                            cursor2.close();
                                                        }
                                                        throw th;
                                                    }
                                                } else {
                                                    str = str5;
                                                    cursor = cursor2;
                                                    str3 = string4;
                                                    audioModel = audioModel2;
                                                    String str7 = string3;
                                                    this.artistModel.getAudioModelList().add(new AudioModel(str7, audioModel.getPath(), AppConstants.getSize(audioModel.getLongSize()), (long) columnIndexOrThrow, AppConstants.GetTimeNew(j), extension, withAppendedPath.toString(), string, withAppendedId.toString(), string2, false, j, j2));
                                                    this.artistModel.setCount();
                                                    this.artistModel.setPath(audioModel.getPath());
                                                    this.artistModel.setNumOfAlbum("");
                                                    this.artistList.add(this.artistModel);
                                                }
                                                AlbumModel albumModel2 = new AlbumModel(string2);
                                                this.albumModel = albumModel2;
                                                if (this.albumList.contains(albumModel2)) {
                                                    int indexOf2 = this.albumList.indexOf(this.albumModel);
                                                    this.tempPos = indexOf2;
                                                    this.albumList.get(indexOf2).setCount();
                                                    this.albumList.get(this.tempPos).setAlbum(string2);
                                                    this.albumList.get(this.tempPos).addAudio(new AudioModel(string3, audioModel.getPath(), AppConstants.getSize(audioModel.getLongSize()), audioModel.getLongSize(), AppConstants.GetTimeNew(j), extension, withAppendedPath.toString(), string, withAppendedId.toString(), string2, false, j, j2));
                                                } else {
                                                    this.albumModel.getAudioModelList().add(new AudioModel(string3, audioModel.getPath(), AppConstants.getSize(audioModel.getLongSize()), audioModel.getLongSize(), AppConstants.GetTimeNew(j), extension, withAppendedPath.toString(), string, withAppendedId.toString(), string2, false, j, j2));
                                                    this.albumModel.setCount();
                                                    this.albumModel.setPath(audioModel.getPath());
                                                    this.albumModel.setAlbumArt(withAppendedId.toString());
                                                    this.albumModel.setAlbum(string2);
                                                    this.albumList.add(this.albumModel);
                                                }
                                                AudioFolderModal audioFolderModal2 = new AudioFolderModal(str3);
                                                this.audioFolderModal = audioFolderModal2;
                                                if (this.folderList.contains(audioFolderModal2)) {
                                                    int indexOf3 = this.folderList.indexOf(this.audioFolderModal);
                                                    this.tempPos = indexOf3;
                                                    this.folderList.get(indexOf3).setCount();
                                                    this.folderList.get(this.tempPos).setaPath(audioModel.getPath());
                                                    this.folderList.get(this.tempPos).addAudio(new AudioModel(string3, audioModel.getPath(), AppConstants.getSize(audioModel.getLongSize()), (long) columnIndexOrThrow, AppConstants.GetTimeNew(j), extension, withAppendedPath.toString(), string, withAppendedId.toString(), string2, false, j, j2));
                                                } else {
                                                    this.audioFolderModal.getAudioList().add(new AudioModel(string3, audioModel.getPath(), AppConstants.getSize(audioModel.getLongSize()), (long) columnIndexOrThrow, AppConstants.GetTimeNew(j), extension, withAppendedPath.toString(), string, withAppendedId.toString(), string2, false, j, j2));
                                                    this.audioFolderModal.setaPath(audioModel.getPath());
                                                    this.audioFolderModal.setCount();
                                                    this.folderList.add(this.audioFolderModal);
                                                }
                                                str4 = str2;
                                                cursor2 = cursor;
                                                str5 = str;
                                            }
                                            str = str5;
                                            cursor = cursor2;
                                            str4 = str2;
                                            cursor2 = cursor;
                                            str5 = str;
                                        }
                                    }
                                }
                            }
                            z = false;
                            if (z) {
                            }
                        }
                        str2 = str4;
                        str = str5;
                        cursor = cursor2;
                        str4 = str2;
                        cursor2 = cursor;
                        str5 = str;
                    }
                } catch (Throwable th2) {
                    //th = th2;
                    Cursor cursor3 = cursor2;
                    if (cursor2 != null) {
                    }
                    throw th2;
                }
            }
            Cursor cursor4 = cursor2;
            if (cursor4 != null) {
                cursor4.close();
            }
        } catch (Throwable th3) {
            //th = th3;
            if (cursor2 != null) {
            }
            throw th3;
        }
    }

    public void LoadAudioDataList() throws Throwable {
        Cursor cursor = null;
        String str;
        String str2;
        MainActivity mainActivity = null;
        String str3;
        String str4;
        String str5;
        String str6;
        long j;
        MainActivity mainActivity2 = this;
        String str7 = "TAG";
        String str8 = "_display_name";
        String str9 = "duration";
        Cursor cursor2 = null;
        try {
            cursor2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"_data", "title", "track", "year", "duration", "_data", "date_modified", "album_id", "album", "artist_id", "artist", "composer", "_id", "_display_name", "_size", "duration", "date_added"}, (String) null, (String[]) null, "_display_name ASC");
            while (cursor2.moveToNext()) {
                try {
                    if (cursor2.getCount() > 0) {
                        int columnIndexOrThrow = cursor2.getColumnIndexOrThrow("_size");
                        if (TextUtils.isEmpty(cursor2.getString(columnIndexOrThrow)) || cursor2.getString(columnIndexOrThrow) == null || Long.parseLong(cursor2.getString(columnIndexOrThrow)) <= 0) {
                            str2 = str8;
                            str = str9;
                            cursor = cursor2;
                            mainActivity = mainActivity2;
                        } else {
                            Uri withAppendedPath = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor2.getString(cursor2.getColumnIndex("_id")));
                            String string = cursor2.getString(cursor2.getColumnIndexOrThrow("_data"));
                            int columnIndexOrThrow2 = cursor2.getColumnIndexOrThrow("title");
                            long columnIndex = (long) cursor2.getColumnIndex(str9);
                            String string2 = cursor2.getString(cursor2.getColumnIndex("artist"));
                            String str10 = string2;
                            Uri withAppendedId = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), (long) cursor2.getInt(cursor2.getColumnIndex("album_id")));
                            String extension = FilenameUtils.getExtension(cursor2.getString(cursor2.getColumnIndexOrThrow(str8)));
                            String string3 = cursor2.getString(cursor2.getColumnIndex("album"));
                            try {
                                long j2 = cursor2.getLong(cursor2.getColumnIndex(str9));
                                String string4 = cursor2.getString(cursor2.getColumnIndex(str8));
                                str2 = str8;
                                str = str9;
                                long j3 = cursor2.getLong(cursor2.getColumnIndex("date_added"));
                                String name = new File(string).getParentFile().getName();
                                StringBuilder sb = new StringBuilder();
                                String str11 = name;
                                sb.append("LoadAudioDataList: ");
                                sb.append(columnIndexOrThrow);
                                sb.append(" || ");
                                sb.append(string4);
                                Log.d(str7, sb.toString());
                                if (j2 == 0) {
                                    mainActivity2 = this;
                                    str8 = str2;
                                    str9 = str;
                                } else {
                                    AudioModel audioModel = new AudioModel();
                                    audioModel.setPath(string);
                                    audioModel.setName(cursor2.getString(columnIndexOrThrow2));
                                    audioModel.setUri(withAppendedPath.toString());
                                    int i = (int) columnIndex;
                                    audioModel.setTime(AppConstants.GetTimeNew(cursor2.getLong(i)));
                                    audioModel.setSize(AppConstants.getSize(Long.parseLong(cursor2.getString(columnIndexOrThrow))));
                                    audioModel.setLongSize(Long.parseLong(cursor2.getString(columnIndexOrThrow)));
                                    audioModel.setPlayed(false);
                                    audioModel.setAlbumId(withAppendedId.toString());
                                    audioModel.setArtist(str10);
                                    audioModel.setDuration(cursor2.getLong(i));
                                    audioModel.setType(extension);
                                    audioModel.setAlbumName(string3);
                                    long j4 = j3;
                                    audioModel.setCreationDate(j4);
                                    mainActivity = this;
                                    try {
                                        if (mainActivity.audioFileIsCorrupted(string) && !TextUtils.isEmpty(audioModel.getAlbumName()) && !TextUtils.isEmpty(audioModel.getArtist())) {
                                            String str12 = str10;
                                            if (!audioModel.getTime().equals("00:00")) {
                                                mainActivity.songsList.add(audioModel);
                                                ArtistModel artistModel2 = new ArtistModel(audioModel.getArtist());
                                                mainActivity.artistModel = artistModel2;
                                                if (mainActivity.artistList.contains(artistModel2)) {
                                                    j = j4;
                                                    int indexOf = mainActivity.artistList.indexOf(mainActivity.artistModel);
                                                    mainActivity.tempPos = indexOf;
                                                    mainActivity.artistList.get(indexOf).setCount();
                                                    mainActivity.artistList.get(mainActivity.tempPos).setNumOfAlbum("");
                                                    ArtistModel artistModel3 = mainActivity.artistList.get(mainActivity.tempPos);
                                                    long j5 = (long) columnIndexOrThrow;
                                                    long j6 = j5;
                                                    cursor = cursor2;
                                                    str6 = string3;
                                                    str4 = extension;
                                                    str5 = str12;
                                                    try {
                                                        AudioModel audioModel2 = new AudioModel(string4, string, AppConstants.getSize(audioModel.getLongSize()), j6, AppConstants.GetTimeNew(j2), str4, withAppendedPath.toString(), str5, withAppendedId.toString(), str6, false, j2, j);
                                                        artistModel3.addAudio(audioModel2);
                                                        str3 = str7;
                                                    } catch (Throwable th) {
                                                        th = th;
                                                        cursor2 = cursor;
                                                        if (cursor2 != null) {
                                                        }
                                                        throw th;
                                                    }
                                                } else {
                                                    cursor = cursor2;
                                                    j = j4;
                                                    str6 = string3;
                                                    str4 = extension;
                                                    str5 = str12;
                                                    String str13 = string4;
                                                    String str14 = string;
                                                    str3 = str7;
                                                    mainActivity.artistModel.getAudioModelList().add(new AudioModel(str13, str14, AppConstants.getSize(audioModel.getLongSize()), (long) columnIndexOrThrow, AppConstants.GetTimeNew(j2), str4, withAppendedPath.toString(), str5, withAppendedId.toString(), str6, false, j2, j));
                                                    mainActivity.artistModel.setCount();
                                                    mainActivity.artistModel.setPath(string);
                                                    mainActivity.artistModel.setNumOfAlbum("");
                                                    mainActivity.artistList.add(mainActivity.artistModel);
                                                }
                                                String str15 = str6;
                                                AlbumModel albumModel2 = new AlbumModel(str15);
                                                mainActivity.albumModel = albumModel2;
                                                if (mainActivity.albumList.contains(albumModel2)) {
                                                    int indexOf2 = mainActivity.albumList.indexOf(mainActivity.albumModel);
                                                    mainActivity.tempPos = indexOf2;
                                                    mainActivity.albumList.get(indexOf2).setCount();
                                                    mainActivity.albumList.get(mainActivity.tempPos).setAlbum(str15);
                                                    mainActivity.albumList.get(mainActivity.tempPos).addAudio(new AudioModel(string4, string, AppConstants.getSize(audioModel.getLongSize()), audioModel.getLongSize(), AppConstants.GetTimeNew(j2), str4, withAppendedPath.toString(), str5, withAppendedId.toString(), str15, false, j2, j));
                                                } else {
                                                    mainActivity.albumModel.getAudioModelList().add(new AudioModel(string4, string, AppConstants.getSize(audioModel.getLongSize()), audioModel.getLongSize(), AppConstants.GetTimeNew(j2), str4, withAppendedPath.toString(), str5, withAppendedId.toString(), str15, false, j2, j));
                                                    mainActivity.albumModel.setCount();
                                                    mainActivity.albumModel.setPath(string);
                                                    mainActivity.albumModel.setAlbumArt(withAppendedId.toString());
                                                    mainActivity.albumModel.setAlbum(str15);
                                                    mainActivity.albumList.add(mainActivity.albumModel);
                                                }
                                                AudioFolderModal audioFolderModal2 = new AudioFolderModal(str11);
                                                mainActivity.audioFolderModal = audioFolderModal2;
                                                if (mainActivity.folderList.contains(audioFolderModal2)) {
                                                    int indexOf3 = mainActivity.folderList.indexOf(mainActivity.audioFolderModal);
                                                    mainActivity.tempPos = indexOf3;
                                                    mainActivity.folderList.get(indexOf3).setCount();
                                                    mainActivity.folderList.get(mainActivity.tempPos).setaPath(string);
                                                    mainActivity.folderList.get(mainActivity.tempPos).addAudio(new AudioModel(string4, string, AppConstants.getSize(audioModel.getLongSize()), (long) columnIndexOrThrow, AppConstants.GetTimeNew(j2), str4, withAppendedPath.toString(), str5, withAppendedId.toString(), str15, false, j2, j));
                                                } else {
                                                    mainActivity.audioFolderModal.getAudioList().add(new AudioModel(string4, string, AppConstants.getSize(audioModel.getLongSize()), (long) columnIndexOrThrow, AppConstants.GetTimeNew(j2), str4, withAppendedPath.toString(), str5, withAppendedId.toString(), str15, false, j2, j));
                                                    mainActivity.audioFolderModal.setaPath(string);
                                                    mainActivity.audioFolderModal.setCount();
                                                    mainActivity.folderList.add(mainActivity.audioFolderModal);
                                                }
                                                str7 = str3;
                                                Log.d(str7, "getAudioUriFromMediaProviderBelow28:  " + str15 + " || " + string);
                                            }
                                        }
                                        cursor = cursor2;
                                    } catch (Throwable th2) {
                                        //th = th2;
                                        Cursor cursor3 = cursor2;
                                        if (cursor2 != null) {
                                        }
                                        throw th2;
                                    }
                                }
                            } catch (Throwable th3) {
                                //th = th3;
                                Cursor cursor32 = cursor2;
                                if (cursor2 != null) {
                                }
                                throw th3;
                            }
                        }
                        mainActivity2 = mainActivity;
                        str8 = str2;
                        str9 = str;
                        cursor2 = cursor;
                    }
                } catch (Throwable th4) {
                    //th = th4;
                    MainActivity mainActivity3 = mainActivity2;
                    Cursor cursor322 = cursor2;
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    throw th4;
                }
            }
            MainActivity mainActivity4 = mainActivity2;
            Cursor cursor4 = cursor2;
            if (cursor4 != null) {
                cursor4.close();
            }
        } catch (Throwable th5) {
            //th = th5;
            MainActivity mainActivity5 = mainActivity2;
            if (cursor2 != null) {
            }
            throw th5;
        }
    }

    private void getReadWritePermission() {
        LoadAllList();
        /*if (hasReadWritePermission()) {
            LoadAllList();
        } else {
            if (Build.VERSION.SDK_INT >= 33){
                PERMISSION_FINAL = READ_WRITE_EXTERNAL_STORAGE33;
            }else {
                PERMISSION_FINAL = READ_WRITE_EXTERNAL_STORAGE;
            }
            EasyPermissions.requestPermissions((Activity) this, getString(R.string.rationale_Storage), this.READ_WRITE, this.PERMISSION_FINAL);
        }*/
    }

    private void LoadAllList() {
        this.binding.progressBar.setVisibility(View.VISIBLE);
        this.disposable.add(Observable.fromCallable(new MainActivityObservable1(this)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new MainActivityObservable2(this)));
    }

    public Boolean MainActivityObservable1call() throws Throwable {
        if (Build.VERSION.SDK_INT > 29) {
            LoadVideoUriList();
            LoadAudioUriList();
        } else {
            LoadVideoDataList();
            LoadAudioDataList();
        }
        SortVideoNameAsc();
        if (this.AllVideosList.size() > 0) {
            VideoFolderModal videoFolderModal2 = new VideoFolderModal("All", "HEEDER");
            this.videoFolderModal = videoFolderModal2;
            videoFolderModal2.setVideoList(this.AllVideosList);
            this.videoFolderModal.setCount();
            this.videoFolderList.add(0, this.videoFolderModal);
        }
        if (this.videoFolderList.size() > 0) {
            this.videoFolderList.get(0).setSelected(true);
            this.videoFragment.SetVideoFolderModal(this.videoFolderList.get(0));
        }
        return false;
    }


    public void MainActivityObservable2call(Boolean bool) throws Exception {
        this.binding.progressBar.setVisibility(View.GONE);
        this.videoFragment.NotifyVideoFolderAdapter();
        this.videoFragment.NotifyVideosListAdapter();
        this.videoFragment.SortVideoDateDesc();
        this.videoFragment.NotifyVideosGridAdapter();
        this.videoFragment.NotifyVideosListAdapter();
    }

    private void SetRecentVisibility() {
        if (AppPref.IsShowHistory()) {
            this.binding.llRecent.setVisibility(View.VISIBLE);
        } else {
            this.binding.llRecent.setVisibility(View.GONE);
        }
    }

    public void SetMusicVisibility(boolean z) {
        if (z) {
            this.binding.llPlaylist.setVisibility(View.VISIBLE);
            this.binding.llMusic.setVisibility(View.VISIBLE);
            return;
        }
        this.binding.llPlaylist.setVisibility(View.GONE);
        this.binding.llMusic.setVisibility(View.GONE);
    }

    private void Clicks() {
        this.binding.llVideo.setOnClickListener(this);
        this.binding.llMusic.setOnClickListener(this);
        this.binding.llSetting.setOnClickListener(this);
        this.binding.llFavourite.setOnClickListener(this);
        this.binding.llPlaylist.setOnClickListener(this);
        this.binding.llRecent.setOnClickListener(this);
        this.binding.btnNext.setOnClickListener(this);
        this.binding.btnPrevious.setOnClickListener(this);
        this.binding.framePlayPause.setOnClickListener(this);
        this.binding.llContentPlayer.setOnClickListener(this);
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
            case R.id.llFavourite:
                this.activityLauncher.launch(new Intent(this, FavouriteListActivity.class));
                return;
            case R.id.llMusic:
                HideTopLL(false);
                setSearchCollapse();
                ShowFragment(this.musicFragment);
                SortList();
                this.musicFragment.CheckNoData();
                if (!AppConstants.isMyServiceRunning(this, AudioService.class)) {
                    NotifyForSongPlaying();
                }
                setAdapterPlayingSong();
                if (this.videoFragment.isMultiSelect) {
                    this.videoFragment.ClearMultiSelection();
                }
                SetSelection(this.binding.imgMusic, this.binding.txtMusic, this.binding.imgVideo, this.binding.txtVideo, this.binding.imgSetting, this.binding.txtSetting, true);
                if (this.musicFragment.isMultiSelect) {
                    RefreshMultiSelectMusic();
                    return;
                }
                return;
            case R.id.llPlaylist:
                this.activityLauncher.launch(new Intent(this, CreatePlaylistActivity.class));
                return;
            case R.id.llRecent:
                this.activityLauncher.launch(new Intent(this, RecentActivity.class));
                return;
            case R.id.llSetting:
                HideTopLL(true);
                setSearchCollapse();
                ShowFragment(this.settingsFragment);
                if (this.videoFragment.isMultiSelect) {
                    this.videoFragment.ClearMultiSelection();
                }
                if (this.musicFragment.isMultiSelect) {
                    this.musicFragment.ClearMultiSelection();
                }
                SetSelection(this.binding.imgSetting, this.binding.txtSetting, this.binding.imgMusic, this.binding.txtMusic, this.binding.imgVideo, this.binding.txtVideo, true);
                return;
            case R.id.llVideo:
                HideTopLL(false);
                setSearchCollapse();
                ShowFragment(this.videoFragment);
                SortList();
                if (this.musicFragment.isMultiSelect) {
                    this.musicFragment.ClearMultiSelection();
                }
                SetRecentVisibility();
                if (this.videoFragment.isMultiSelect) {
                    RefreshMultiSelectVideo();
                }
                SetSelection(this.binding.imgVideo, this.binding.txtVideo, this.binding.imgMusic, this.binding.txtMusic, this.binding.imgSetting, this.binding.txtSetting, true);
                return;
            default:
                return;
        }
    }

    public void setSearchCollapse() {
        this.search.collapseActionView();
        Fragment fragment = this.activeFragment;
        VideoFragment videoFragment2 = this.videoFragment;
        if (fragment == videoFragment2) {
            videoFragment2.videoListAdapter.setVideoFolderList(this.videoFragment.VideosList);
            this.videoFragment.videoGridAdapter.setVideoFolderList(this.videoFragment.VideosList);
            this.videoFragment.SetNoData();
            return;
        }
        MusicFragment musicFragment2 = this.musicFragment;
        if (fragment == musicFragment2) {
            int GetVisibleData = musicFragment2.GetVisibleData();
            if (GetVisibleData == 4) {
                this.musicFragment.songsAdapter.setSongsList(this.songsList);
            } else if (GetVisibleData == 1) {
                this.musicFragment.folderAdapter.setFolderModelList(this.folderList);
            } else if (GetVisibleData == 2) {
                this.musicFragment.albumAdapter.setAlbumList(this.albumList);
            } else if (GetVisibleData == 3) {
                this.musicFragment.artistAdapter.setArtistList(this.artistList);
            }
            this.musicFragment.CheckNoData();
        }
    }

    private void SetSelection(ImageView imageView, TextView textView, ImageView imageView2, TextView textView2, ImageView imageView3, TextView textView3, boolean z) {
        if (z) {
            imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#38b6ff")));
            textView.setTextColor(Color.parseColor("#38b6ff"));
        } else {
            imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")));
            textView.setTextColor(Color.parseColor("#ffffff"));
        }
        imageView2.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")));
        textView2.setTextColor(Color.parseColor("#ffffff"));
        imageView3.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")));
        textView3.setTextColor(Color.parseColor("#ffffff"));
    }

    public void HideTopLL(boolean z) {
        if (z) {
            this.binding.llTop.setVisibility(View.GONE);
        } else {
            this.binding.llTop.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        this.grid = menu.findItem(R.id.grid);
        this.list = menu.findItem(R.id.list);
        this.search = menu.findItem(R.id.search);
        this.sort = menu.findItem(R.id.sort);
        this.grid.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.grid) {
            this.videoFragment.ChangeDataList(false);
            this.list.setVisible(true);
            this.grid.setVisible(false);
        } else if (menuItem.getItemId() == R.id.list) {
            this.videoFragment.ChangeDataList(true);
            this.list.setVisible(false);
            this.grid.setVisible(true);
        } else if (menuItem.getItemId() == R.id.search) {
            SearchView searchView2 = (SearchView) menuItem.getActionView();
            this.searchView = searchView2;
            ((ImageView) searchView2.findViewById(androidx.appcompat.R.id.search_close_btn)).setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            EditText editText = (EditText) this.searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            editText.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            editText.setHint("Search here");
            editText.setHintTextColor(getResources().getColor(R.color.white));
            this.searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean z) {
                    if (!z) {
                        Log.d("TAG", "onFocusChange: 1" + z);
                        MainActivity.this.binding.llTop.setVisibility(View.VISIBLE);
                    } else {
                        MainActivity.this.binding.llTop.setVisibility(View.GONE);
                        Log.d("TAG", "onFocusChange: 2" + z);
                    }
                    MainActivity.this.setAdapterPlayingSong();
                }
            });
            this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String str) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String str) {
                    if (MainActivity.this.activeFragment == MainActivity.this.videoFragment) {
                        if (MainActivity.this.videoFragment.videoListAdapter == null || MainActivity.this.videoFragment.videoGridAdapter == null) {
                            return false;
                        }
                        MainActivity.this.videoFragment.videoListAdapter.getFilter().filter(str);
                        MainActivity.this.videoFragment.videoGridAdapter.getFilter().filter(str);
                        MainActivity.this.videoFragment.SetNoData();
                        return false;
                    } else if (MainActivity.this.activeFragment != MainActivity.this.musicFragment) {
                        return false;
                    } else {
                        int GetVisibleData = MainActivity.this.musicFragment.GetVisibleData();
                        if (GetVisibleData == 4) {
                            MainActivity.this.musicFragment.songsAdapter.getFilter().filter(str);
                            MainActivity.this.setAdapterPlayingSong();
                            return false;
                        } else if (GetVisibleData == 1) {
                            MainActivity.this.musicFragment.folderAdapter.getFilter().filter(str);
                            return false;
                        } else if (GetVisibleData == 2) {
                            MainActivity.this.musicFragment.albumAdapter.getFilter().filter(str);
                            return false;
                        } else if (GetVisibleData != 3) {
                            return false;
                        } else {
                            MainActivity.this.musicFragment.artistAdapter.getFilter().filter(str);
                            return false;
                        }
                    }
                }
            });
        } else if (menuItem.getItemId() == R.id.sort) {
            OpenSortDialog();
        }
        return true;
    }

    private void OpenSortDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        final BottomsheetSortingBinding bottomsheetSortingBinding = (BottomsheetSortingBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_sorting, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetSortingBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        SetSortingSelection(bottomsheetSortingBinding.imgFile, bottomsheetSortingBinding.imgName, bottomsheetSortingBinding.imgDate, bottomsheetSortingBinding.imgLength, bottomsheetSortingBinding.imgAsc, bottomsheetSortingBinding.imgDesc, this.oType, this.sType);
        bottomsheetSortingBinding.llFileSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.sType = AppConstants.FILE_SIZE_WISE;
                MainActivity.this.SetSortingSelection(bottomsheetSortingBinding.imgFile, bottomsheetSortingBinding.imgName, bottomsheetSortingBinding.imgDate, bottomsheetSortingBinding.imgLength, bottomsheetSortingBinding.imgAsc, bottomsheetSortingBinding.imgDesc, MainActivity.this.oType, MainActivity.this.sType);
            }
        });
        bottomsheetSortingBinding.llName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.sType = AppConstants.NAME_WISE;
                MainActivity.this.SetSortingSelection(bottomsheetSortingBinding.imgFile, bottomsheetSortingBinding.imgName, bottomsheetSortingBinding.imgDate, bottomsheetSortingBinding.imgLength, bottomsheetSortingBinding.imgAsc, bottomsheetSortingBinding.imgDesc, MainActivity.this.oType, MainActivity.this.sType);
            }
        });
        bottomsheetSortingBinding.llDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.sType = AppConstants.DATE_WISE;
                MainActivity.this.SetSortingSelection(bottomsheetSortingBinding.imgFile, bottomsheetSortingBinding.imgName, bottomsheetSortingBinding.imgDate, bottomsheetSortingBinding.imgLength, bottomsheetSortingBinding.imgAsc, bottomsheetSortingBinding.imgDesc, MainActivity.this.oType, MainActivity.this.sType);
            }
        });
        bottomsheetSortingBinding.llLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.sType = AppConstants.LENGTH_WISE;
                MainActivity.this.SetSortingSelection(bottomsheetSortingBinding.imgFile, bottomsheetSortingBinding.imgName, bottomsheetSortingBinding.imgDate, bottomsheetSortingBinding.imgLength, bottomsheetSortingBinding.imgAsc, bottomsheetSortingBinding.imgDesc, MainActivity.this.oType, MainActivity.this.sType);
            }
        });
        bottomsheetSortingBinding.llAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.oType = 1;
                MainActivity.this.SetSortingSelection(bottomsheetSortingBinding.imgFile, bottomsheetSortingBinding.imgName, bottomsheetSortingBinding.imgDate, bottomsheetSortingBinding.imgLength, bottomsheetSortingBinding.imgAsc, bottomsheetSortingBinding.imgDesc, MainActivity.this.oType, MainActivity.this.sType);
            }
        });
        bottomsheetSortingBinding.llDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.oType = 2;
                MainActivity.this.SetSortingSelection(bottomsheetSortingBinding.imgFile, bottomsheetSortingBinding.imgName, bottomsheetSortingBinding.imgDate, bottomsheetSortingBinding.imgLength, bottomsheetSortingBinding.imgAsc, bottomsheetSortingBinding.imgDesc, MainActivity.this.oType, MainActivity.this.sType);
            }
        });
        bottomsheetSortingBinding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                MainActivity mainActivity = MainActivity.this;
                mainActivity.sortType = mainActivity.sType;
                MainActivity mainActivity2 = MainActivity.this;
                mainActivity2.orderType = mainActivity2.oType;
                MainActivity.this.isChangeSort = true;
                MainActivity.this.SortList();
            }
        });
        bottomsheetSortingBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }


    public void SortList() {
        Fragment fragment = this.activeFragment;
        boolean z = false;
        if (fragment == this.videoFragment) {
            if (this.orderType == 1) {
                char cc12 = 0;
                String str = this.sortType;
                str.hashCode();
                switch (str.hashCode()) {
                    case -1727588715:
                        cc12 = 0;
                        break;
                    case -1659068641:
                        if (str.equals(AppConstants.FILE_SIZE_WISE)) {
                            z = true;
                            cc12 = 1;
                            break;
                        }
                    case -473329059:
                        if (str.equals(AppConstants.LENGTH_WISE)) {
                            z = true;
                            cc12 = 2;
                            break;
                        }
                    case -244855336:
                        if (str.equals(AppConstants.NAME_WISE)) {
                            z = true;
                            cc12 = 3;
                            break;
                        }
                    default:
                        z = true;
                        cc12 = 0;
                        break;
                }

                if (cc12 == 0) {
                    this.videoFragment.SortVideoDateAsc();
                } else if (cc12 == 1) {
                    this.videoFragment.SortVideoFileAsc();
                } else if (cc12 == 2) {
                    this.videoFragment.SortVideoLengthAsc();
                } else if (cc12 == 3) {
                    this.videoFragment.SortVideoNameAsc();
                } else {
                    this.videoFragment.SortVideoDateAsc();
                }

                /*switch (z) {
                    case false:
                        this.videoFragment.SortVideoDateAsc();
                        return;
                    case true:
                        this.videoFragment.SortVideoFileAsc();
                        return;
                    case true:
                        this.videoFragment.SortVideoLengthAsc();
                        return;
                    case true:
                        this.videoFragment.SortVideoNameAsc();
                        return;
                    default:
                        return;
                }*/
            } else {
                char cc13 = 0;
                String str2 = this.sortType;
                str2.hashCode();
                switch (str2.hashCode()) {
                    case -1727588715:
                        cc13 = 0;
                        break;
                    case -1659068641:
                        if (str2.equals(AppConstants.FILE_SIZE_WISE)) {
                            z = true;
                            cc13 = 1;
                            break;
                        }
                    case -473329059:
                        if (str2.equals(AppConstants.LENGTH_WISE)) {
                            z = true;
                            cc13 = 2;
                            break;
                        }
                    case -244855336:
                        if (str2.equals(AppConstants.NAME_WISE)) {
                            z = true;
                            cc13 = 3;
                            break;
                        }
                    default:
                        z = true;
                        break;
                }

                if (cc13 == 0) {
                    this.videoFragment.SortVideoDateDesc();
                } else if (cc13 == 1) {
                    this.videoFragment.SortVideoFileDesc();
                } else if (cc13 == 2) {
                    this.videoFragment.SortVideoLengthDesc();
                } else if (cc13 == 3) {
                    this.videoFragment.SortVideoNameDesc();
                } else {
                    this.videoFragment.SortVideoDateDesc();
                }

                /*switch (z) {
                    case false:
                        this.videoFragment.SortVideoDateDesc();
                        return;
                    case true:
                        this.videoFragment.SortVideoFileDesc();
                        return;
                    case true:
                        this.videoFragment.SortVideoLengthDesc();
                        return;
                    case true:
                        this.videoFragment.SortVideoNameDesc();
                        return;
                    default:
                        return;
                }*/
            }
        } else if (fragment == this.musicFragment) {
            if (this.orderType != 1) {
                char cc44 = 0;
                String str3 = this.sortType;
                str3.hashCode();
                switch (str3.hashCode()) {
                    case -1727588715:
                        cc44 = 1;
                        break;
                    case -1659068641:
                        if (str3.equals(AppConstants.FILE_SIZE_WISE)) {
                            z = true;
                            cc44 = 2;
                            break;
                        }
                    case -473329059:
                        if (str3.equals(AppConstants.LENGTH_WISE)) {
                            z = true;
                            cc44 = 3;
                            break;
                        }
                    case -244855336:
                        if (str3.equals(AppConstants.NAME_WISE)) {
                            z = true;
                            cc44 = 4;
                            break;
                        }
                    default:
                        z = true;
                        cc44 = 1;
                        break;
                }

                if (cc44 == 1) {
                    this.musicFragment.SortSongDateDesc();
                } else if (cc44 == 2) {
                    this.musicFragment.SortSongFileDesc();
                } else if (cc44 == 3) {
                    this.musicFragment.SortSongLengthDesc();
                } else if (cc44 == 4) {
                    this.musicFragment.SortSongNameDesc();
                } else {
                    this.musicFragment.SortSongDateDesc();
                }

                /*switch (z) {
                    case false:
                        if (this.musicFragment.GetVisibleData() == 4) {
                            this.musicFragment.SortSongDateDesc();
                            break;
                        }
                        break;
                    case true:
                        if (this.musicFragment.GetVisibleData() == 4) {
                            this.musicFragment.SortSongFileDesc();
                            break;
                        }
                        break;
                    case true:
                        if (this.musicFragment.GetVisibleData() == 4) {
                            this.musicFragment.SortSongLengthDesc();
                            break;
                        }
                        break;
                    case true:
                        if (this.musicFragment.GetVisibleData() != 4) {
                            if (this.musicFragment.GetVisibleData() != 1) {
                                if (this.musicFragment.GetVisibleData() != 3) {
                                    if (this.musicFragment.GetVisibleData() == 2) {
                                        this.musicFragment.SortAlbumNameDesc();
                                        break;
                                    }
                                } else {
                                    this.musicFragment.SortArtistNameDesc();
                                    break;
                                }
                            } else {
                                this.musicFragment.SortFolderNameDesc();
                                break;
                            }
                        } else {
                            this.musicFragment.SortSongNameDesc();
                            break;
                        }
                        break;
                }*/
            } else {
                char cc55 = 0;
                String str4 = this.sortType;
                str4.hashCode();
                switch (str4.hashCode()) {
                    case -1727588715:
                        cc55 = 1;
                        break;
                    case -1659068641:
                        if (str4.equals(AppConstants.FILE_SIZE_WISE)) {
                            z = true;
                            cc55 = 2;
                            break;
                        }
                    case -473329059:
                        if (str4.equals(AppConstants.LENGTH_WISE)) {
                            z = true;
                            cc55 = 3;
                            break;
                        }
                    case -244855336:
                        if (str4.equals(AppConstants.NAME_WISE)) {
                            z = true;
                            cc55 = 4;
                            break;
                        }
                    default:
                        z = true;
                        cc55 = 1;
                        break;
                }

                if (cc55 == 1) {
                    if (this.musicFragment.GetVisibleData() == 4) {
                        this.musicFragment.SortSongDateAsc();
                    } else if (this.musicFragment.GetVisibleData() != 4) {
                        if (this.musicFragment.GetVisibleData() != 1) {
                            if (this.musicFragment.GetVisibleData() != 3) {
                                if (this.musicFragment.GetVisibleData() == 2) {
                                    this.musicFragment.SortAlbumNameAsc();
                                }
                            } else {
                                this.musicFragment.SortArtistNameAsc();
                            }
                        } else {
                            this.musicFragment.SortFolderNameAsc();
                        }
                    } else {
                        this.musicFragment.SortSongNameAsc();
                    }
                } else if (cc55 == 2) {
                    if (this.musicFragment.GetVisibleData() == 4) {
                        this.musicFragment.SortSongFileAsc();
                    } else if (this.musicFragment.GetVisibleData() != 4) {
                        if (this.musicFragment.GetVisibleData() != 1) {
                            if (this.musicFragment.GetVisibleData() != 3) {
                                if (this.musicFragment.GetVisibleData() == 2) {
                                    this.musicFragment.SortAlbumNameAsc();
                                }
                            } else {
                                this.musicFragment.SortArtistNameAsc();
                            }
                        } else {
                            this.musicFragment.SortFolderNameAsc();
                        }
                    } else {
                        this.musicFragment.SortSongNameAsc();
                    }

                } else if (cc55 == 3) {
                    if (this.musicFragment.GetVisibleData() == 4) {
                        this.musicFragment.SortSongLengthAsc();
                    } else if (this.musicFragment.GetVisibleData() != 4) {
                        if (this.musicFragment.GetVisibleData() != 1) {
                            if (this.musicFragment.GetVisibleData() != 3) {
                                if (this.musicFragment.GetVisibleData() == 2) {
                                    this.musicFragment.SortAlbumNameAsc();
                                }
                            } else {
                                this.musicFragment.SortArtistNameAsc();
                            }
                        } else {
                            this.musicFragment.SortFolderNameAsc();
                        }
                    } else {
                        this.musicFragment.SortSongNameAsc();
                    }

                } else if (cc55 == 4) {
                    //this.musicFragment.SortAlbumNameAsc();
                    if (this.musicFragment.GetVisibleData() != 4) {
                        if (this.musicFragment.GetVisibleData() != 1) {
                            if (this.musicFragment.GetVisibleData() != 3) {
                                if (this.musicFragment.GetVisibleData() == 2) {
                                    this.musicFragment.SortAlbumNameAsc();
                                }
                            } else {
                                this.musicFragment.SortArtistNameAsc();
                            }
                        } else {
                            this.musicFragment.SortFolderNameAsc();
                        }
                    } else {
                        this.musicFragment.SortSongNameAsc();
                    }

                } else {
                    if (this.musicFragment.GetVisibleData() == 4) {
                        this.musicFragment.SortSongDateAsc();
                    } else if (this.musicFragment.GetVisibleData() != 4) {
                        if (this.musicFragment.GetVisibleData() != 1) {
                            if (this.musicFragment.GetVisibleData() != 3) {
                                if (this.musicFragment.GetVisibleData() == 2) {
                                    this.musicFragment.SortAlbumNameAsc();
                                }
                            } else {
                                this.musicFragment.SortArtistNameAsc();
                            }
                        } else {
                            this.musicFragment.SortFolderNameAsc();
                        }
                    } else {
                        this.musicFragment.SortSongNameAsc();
                    }
                }

                /*switch (z) {
                    case false:
                        if (this.musicFragment.GetVisibleData() == 4) {
                            this.musicFragment.SortSongDateAsc();
                            break;
                        }
                        break;
                    case true:
                        if (this.musicFragment.GetVisibleData() == 4) {
                            this.musicFragment.SortSongFileAsc();
                            break;
                        }
                        break;
                    case true:
                        if (this.musicFragment.GetVisibleData() == 4) {
                            this.musicFragment.SortSongLengthAsc();
                            break;
                        }
                        break;
                    case true:
                        if (this.musicFragment.GetVisibleData() != 4) {
                            if (this.musicFragment.GetVisibleData() != 1) {
                                if (this.musicFragment.GetVisibleData() != 3) {
                                    if (this.musicFragment.GetVisibleData() == 2) {
                                        this.musicFragment.SortAlbumNameAsc();
                                        break;
                                    }
                                } else {
                                    this.musicFragment.SortArtistNameAsc();
                                    break;
                                }
                            } else {
                                this.musicFragment.SortFolderNameAsc();
                                break;
                            }
                        } else {
                            this.musicFragment.SortSongNameAsc();
                            break;
                        }
                        break;
                }*/
            }
            setAdapterPlayingSong();
        }
    }

    public void NotifyForSongPlaying() {
        this.musicFragment.songsAdapter.setIsPlaying(false);
    }

    public void SetSortingSelection(ImageView imageView, ImageView imageView2, ImageView imageView3, ImageView imageView4, ImageView imageView5, ImageView imageView6, int i, String str) {
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -1727588715:
                if (str.equals(AppConstants.DATE_WISE)) {
                    c = 0;
                    break;
                }
                break;
            case -1659068641:
                if (str.equals(AppConstants.FILE_SIZE_WISE)) {
                    c = 1;
                    break;
                }
                break;
            case -473329059:
                if (str.equals(AppConstants.LENGTH_WISE)) {
                    c = 2;
                    break;
                }
                break;
            case -244855336:
                if (str.equals(AppConstants.NAME_WISE)) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                imageView.setImageResource(R.drawable.unselected_radio);
                imageView2.setImageResource(R.drawable.unselected_radio);
                imageView3.setImageResource(R.drawable.selected_radio);
                imageView4.setImageResource(R.drawable.unselected_radio);
                break;
            case 1:
                imageView.setImageResource(R.drawable.selected_radio);
                imageView2.setImageResource(R.drawable.unselected_radio);
                imageView3.setImageResource(R.drawable.unselected_radio);
                imageView4.setImageResource(R.drawable.unselected_radio);
                break;
            case 2:
                imageView.setImageResource(R.drawable.unselected_radio);
                imageView2.setImageResource(R.drawable.unselected_radio);
                imageView3.setImageResource(R.drawable.unselected_radio);
                imageView4.setImageResource(R.drawable.selected_radio);
                break;
            case 3:
                imageView.setImageResource(R.drawable.unselected_radio);
                imageView2.setImageResource(R.drawable.selected_radio);
                imageView3.setImageResource(R.drawable.unselected_radio);
                imageView4.setImageResource(R.drawable.unselected_radio);
                break;
        }
        if (i == 1) {
            imageView5.setImageResource(R.drawable.selected_radio);
            imageView6.setImageResource(R.drawable.unselected_radio);
            return;
        }
        imageView5.setImageResource(R.drawable.unselected_radio);
        imageView6.setImageResource(R.drawable.selected_radio);
    }

    public void SortVideoNameAsc() {
        Collections.sort(this.videoFolderList, new Comparator<VideoFolderModal>() {
            @Override
            public int compare(VideoFolderModal videoFolderModal, VideoFolderModal videoFolderModal2) {
                return videoFolderModal.getaName().compareTo(videoFolderModal2.getaName());
            }
        });
    }

    public boolean isExpanded() {
        return this.search.isActionViewExpanded();
    }

    /*private boolean hasReadWritePermission() {
        if (Build.VERSION.SDK_INT >= 33){
            PERMISSION_FINAL = READ_WRITE_EXTERNAL_STORAGE33;
        }else {
            PERMISSION_FINAL = READ_WRITE_EXTERNAL_STORAGE;
        }
        return EasyPermissions.hasPermissions(this, PERMISSION_FINAL);
    }

    private boolean hasPhoneStatePermission() {
        return EasyPermissions.hasPermissions(this, this.READ_STATE);
    }

    public void onPermissionsGranted(int i, List<String> list2) {
        if (!hasPhoneStatePermission()) {
            LoadAllList();
            EasyPermissions.requestPermissions((Activity) this, getString(R.string.rationale_phone_state), this.READ_PHONE, this.READ_STATE);
        }
    }

    public void onPermissionsDenied(int i, List<String> list2) {
        if (!hasReadWritePermission()) {
            new AppSettingsDialog.Builder((Activity) this).build().show();
        }
    }

    public void ResultPermission(int i, String[] strArr, int[] iArr) {
        EasyPermissions.onRequestPermissionsResult(i, strArr, iArr, this);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        ResultPermission(i, strArr, iArr);
    }*/

    @Override
    public void onResume() {
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            bindService();
        }
        registerReceiver();
        super.onResume();
    }

    public void bindService() {
        bindService(new Intent(MyApplication.getContext(), AudioService.class), this, 1);
    }

    private void registerReceiver() {
        if (this.mReceiver == null) {
            MainActivityReceiver mainActivityReceiver = new MainActivityReceiver();
            this.mReceiver = mainActivityReceiver;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(mainActivityReceiver,
                        new IntentFilter(AppConstants.MAIN_ACTIVITY_RECEIVER),
                        Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(mainActivityReceiver,
                        new IntentFilter(AppConstants.MAIN_ACTIVITY_RECEIVER));
            }
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
                        AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(intent.getIntExtra("position", 0));
                        if (MainActivity.this.musicFragment.GetVisibleData() == 4 && audioVideoModal.getUri() != null) {
                            int indexOf = MainActivity.this.musicFragment.songsAdapter.getFilterList().indexOf(new AudioModel(audioVideoModal.getUri()));
                            if (indexOf != -1) {
                                MainActivity.this.musicFragment.songsAdapter.setPlayingPos(indexOf);
                                if (MainActivity.this.audioService != null) {
                                    MainActivity.this.musicFragment.songsAdapter.setAudioIsPlaying(MainActivity.this.audioService.isPlaying());
                                }
                                MainActivity.this.musicFragment.songsAdapter.setIsPlaying(true);
                            } else {
                                MainActivity.this.musicFragment.songsAdapter.setIsPlaying(false);
                            }
                        }
                        MainActivity.this.getPlayingModel();
                        return;
                    case 1:
                        if (intent.getIntExtra("action", 0) == 1) {
                            MainActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_play);
                        } else {
                            MainActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_pause);
                        }
                        MainActivity.this.setAdapterPlayingSong();
                        return;
                    case 2:
                        MainActivity.this.binding.llPlayerBottom.setVisibility(View.GONE);
                        MainActivity.this.CloseService();
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public void CloseService() {
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            try {
                unbindService(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.myHandler.removeCallbacksAndMessages((Object) null);
        this.musicFragment.songsAdapter.setIsPlaying(false);
        this.audioService = null;
    }

    public void setAdapterPlayingSong() {
        int indexOf;
        if (this.audioService != null) {
            AudioVideoModal serviceModel = getServiceModel();
            if (this.musicFragment.GetVisibleData() == 4 && serviceModel != null && serviceModel.getUri() != null && (indexOf = this.musicFragment.songsAdapter.getFilterList().indexOf(new AudioModel(serviceModel.getUri()))) != -1) {
                this.musicFragment.songsAdapter.setPlayingPos(indexOf);
                this.musicFragment.songsAdapter.setAudioIsPlaying(this.audioService.isPlaying());
                this.musicFragment.songsAdapter.setIsPlaying(true);
            }
        }
    }

    public void getPlayingModel() {
        AudioVideoModal serviceModel;
        if (this.audioService != null && (serviceModel = getServiceModel()) != null) {
            Bitmap bitmap = this.audioService.getBitmap(serviceModel);
            if (bitmap != null) {
                this.binding.playArt.setVisibility(View.VISIBLE);
                Glide.with((FragmentActivity) this).load(bitmap).into(this.binding.playArt);
            } else {
                this.binding.playArt.setVisibility(View.GONE);
            }
            this.binding.txtTitle.setText(serviceModel.getName());
            this.binding.txtTotalTime.setText(AppConstants.formatTime(serviceModel.getDuration()));
            this.binding.progress.setMax((int) serviceModel.getDuration());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (MainActivity.this.audioService != null && MainActivity.this.audioService.isPlayerNotNull()) {
                        int currentPosition = MainActivity.this.audioService.getCurrentPosition();
                        MainActivity.this.binding.progress.setProgress(currentPosition);
                        MainActivity.this.binding.txtRunningTime.setText(AppConstants.formatTime((long) currentPosition));
                    }
                    MainActivity.this.myHandler.postDelayed(this, 1000);
                }
            });
        }
    }

    public AudioVideoModal getServiceModel() {
        AudioService audioService2 = this.audioService;
        if (audioService2 != null) {
            return audioService2.getPlayingModel();
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            try {
                unbindService(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.mReceiver != null) {
            this.mReceiver = null;
            try {
                //unregisterReceiver((BroadcastReceiver) null);
                if (this.mReceiver != null) {
                    unregisterReceiver(this.mReceiver);
                    this.mReceiver = null;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
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

    public int GetOldFragmentPosition() {
        if (this.activeFragment.equals(this.videoFragment)) {
            return 1;
        }
        if (this.activeFragment.equals(this.musicFragment)) {
            return 2;
        }
        if (this.activeFragment.equals(this.settingsFragment)) {
            return 3;
        }
        return 1;
    }

    public int GetNewFragmentPosition(Fragment fragment) {
        if (fragment.equals(this.videoFragment)) {
            return 1;
        }
        if (fragment.equals(this.musicFragment)) {
            return 2;
        }
        if (fragment.equals(this.settingsFragment)) {
            return 3;
        }
        return 1;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.uiMode;
        Log.d("TAG", "onConfigurationChanged: ");
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (this.binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            ExitDialog();
        }
    }

    private void ExitDialog() {

        final Dialog dialog = new Dialog(MainActivity.this, R.style.DialogTheme);
        dialog.setContentView(R.layout.popup_exit_dialog);
        dialog.setCancelable(false);

        RelativeLayout no = (RelativeLayout) dialog.findViewById(R.id.no);
        RelativeLayout rate = (RelativeLayout) dialog.findViewById(R.id.rate);
        RelativeLayout yes = (RelativeLayout) dialog.findViewById(R.id.yes);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String rateapp = getPackageName();
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + rateapp));
                startActivity(intent1);
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
                System.exit(0);
                //Intent intent = new Intent(AppMainHomeActivity.this, AppThankYouActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //AdsCommon.InterstitialAd(AppMainHomeActivity.this, intent);
            }
        });

        dialog.show();
    }

}
