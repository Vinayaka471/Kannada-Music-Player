package com.demo.musicvideoplayer.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.adapter.BottomPlaylistAdapter;
import com.demo.musicvideoplayer.adapter.PlaylistFolderAdapter;
import com.demo.musicvideoplayer.ads.AdsCommon;
import com.demo.musicvideoplayer.ads.MyApplication;
import com.demo.musicvideoplayer.database.AppDatabase;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import com.demo.musicvideoplayer.database.model.FolderModal;
import com.demo.musicvideoplayer.databinding.ActivityCreatePlaylistBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetAddToPlaylistBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetFolderMenuBinding;
import com.demo.musicvideoplayer.databinding.DialogDeleteBinding;
import com.demo.musicvideoplayer.databinding.DialogRenameBinding;
import com.demo.musicvideoplayer.model.CombineFolderModel;
import com.demo.musicvideoplayer.service.AudioService;
import com.demo.musicvideoplayer.utils.AppConstants;
import com.demo.musicvideoplayer.utils.AppPref;
import com.demo.musicvideoplayer.utils.BetterActivityResult;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class CreatePlaylistActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {
    List<CombineFolderModel> MultiFolderList = new ArrayList();
    ActionMode actionMode;
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    PlaylistFolderAdapter adapter;
    MenuItem addItem;
    AppDatabase appDatabase;
    AudioService audioService;
    ActivityCreatePlaylistBinding binding;
    BottomPlaylistAdapter bottomPlaylistAdapter;
    ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            CreatePlaylistActivity.this.isMultiSelect = true;
            actionMode.getMenuInflater().inflate(R.menu.play_list_multi_select, menu);
            CreatePlaylistActivity.this.actionMode = actionMode;
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            int indexOf;
            if (menuItem.getItemId() == R.id.bgPlay) {
                if (AppPref.getBgAudioList() != null) {
                    AppPref.getBgAudioList().clear();
                }
                ArrayList arrayList = new ArrayList();
                int i = 0;
                for (int i2 = 0; i2 < CreatePlaylistActivity.this.MultiFolderList.size(); i2++) {
                    List<AudioVideoModal> GetAudioVideoListByFolderID = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(CreatePlaylistActivity.this.MultiFolderList.get(i2).getFolderModal().getId());
                    int i3 = 0;
                    while (i3 < GetAudioVideoListByFolderID.size()) {
                        AudioVideoModal audioVideoModal = GetAudioVideoListByFolderID.get(i3);
                        audioVideoModal.setAudioVideoOrder(i);
                        arrayList.add(audioVideoModal);
                        i3++;
                        i++;
                    }
                }
                AppPref.setBgAudioList(arrayList);
                if (AppPref.getBgAudioList().size() > 0) {
                    Intent intent = new Intent(CreatePlaylistActivity.this, AudioPlayerActivity.class);
                    intent.putExtra("modal", AppPref.getBgAudioList().get(0));
                    CreatePlaylistActivity.this.activityLauncher.launch(intent);
                }
                CreatePlaylistActivity.this.actionMode.finish();
            } else if (menuItem.getItemId() == R.id.playNext) {
                if (AppConstants.isMyServiceRunning(CreatePlaylistActivity.this, AudioService.class)) {
                    ArrayList arrayList2 = new ArrayList();
                    AudioVideoModal playingModel = CreatePlaylistActivity.this.audioService.getPlayingModel();
                    if (!(playingModel == null || (indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(playingModel.getUri()))) == -1)) {
                        int i4 = indexOf + 1;
                        for (int i5 = 0; i5 < i4; i5++) {
                            AudioVideoModal audioVideoModal2 = AppPref.getBgAudioList().get(i5);
                            audioVideoModal2.setAudioVideoOrder(i5);
                            arrayList2.add(audioVideoModal2);
                        }
                        for (int i6 = 0; i6 < CreatePlaylistActivity.this.MultiFolderList.size(); i6++) {
                            List<AudioVideoModal> GetAudioVideoListByFolderID2 = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(CreatePlaylistActivity.this.MultiFolderList.get(i6).getFolderModal().getId());
                            int i7 = 0;
                            while (i7 < GetAudioVideoListByFolderID2.size()) {
                                AudioVideoModal audioVideoModal3 = GetAudioVideoListByFolderID2.get(i7);
                                audioVideoModal3.setAudioVideoOrder(i4);
                                arrayList2.add(audioVideoModal3);
                                i7++;
                                i4++;
                            }
                        }
                        while (indexOf < AppPref.getBgAudioList().size()) {
                            AudioVideoModal audioVideoModal4 = AppPref.getBgAudioList().get(indexOf);
                            audioVideoModal4.setAudioVideoOrder(indexOf);
                            arrayList2.add(audioVideoModal4);
                            indexOf++;
                        }
                        AppPref.getBgAudioList().clear();
                        AppPref.setBgAudioList(arrayList2);
                    }
                }
                CreatePlaylistActivity.this.actionMode.finish();
            } else if (menuItem.getItemId() == R.id.addToQueue) {
                if (AppConstants.isMyServiceRunning(CreatePlaylistActivity.this, AudioService.class)) {
                    ArrayList<AudioVideoModal> bgAudioList = AppPref.getBgAudioList();
                    int size = AppPref.getBgAudioList().size();
                    for (int i8 = 0; i8 < CreatePlaylistActivity.this.MultiFolderList.size(); i8++) {
                        List<AudioVideoModal> GetAudioVideoListByFolderID3 = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(CreatePlaylistActivity.this.MultiFolderList.get(i8).getFolderModal().getId());
                        for (int i9 = 0; i9 < GetAudioVideoListByFolderID3.size(); i9++) {
                            AudioVideoModal audioVideoModal5 = GetAudioVideoListByFolderID3.get(i9);
                            if (!AppPref.getBgAudioList().contains(audioVideoModal5)) {
                                audioVideoModal5.setAudioVideoOrder(size);
                                bgAudioList.add(audioVideoModal5);
                                size++;
                            }
                        }
                    }
                    AppPref.getBgAudioList().clear();
                    AppPref.setBgAudioList(bgAudioList);
                    CreatePlaylistActivity.this.actionMode.finish();
                }
            } else if (menuItem.getItemId() == R.id.addToPlaylist) {
                CreatePlaylistActivity.this.MultipleAddToPlayList();
            } else if (menuItem.getItemId() == R.id.delete) {
                CreatePlaylistActivity.this.MultiDeletePlayList();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            CreatePlaylistActivity.this.isMultiSelect = false;
            CreatePlaylistActivity.this.NotifyForLongClick(false);
            CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
            CreatePlaylistActivity.this.MultiFolderList.clear();
        }
    };
    public CompositeDisposable disposable = new CompositeDisposable();
    MenuItem draggable_down;
    MenuItem draggable_up;
    List<CombineFolderModel> folderModalList = new ArrayList();
    public boolean isMultiSelect = false;
    MainActivityReceiver mReceiver;
    
    public Handler myHandler = new Handler();
    MenuItem search;

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
        this.binding = (ActivityCreatePlaylistBinding) DataBindingUtil.setContentView(this, R.layout.activity_create_playlist);

        applyDisplayCutouts();

        AdsCommon.InterstitialAdsOnly(this);

        //Reguler Banner Ads
        RelativeLayout admob_banner = (RelativeLayout) findViewById(R.id.Admob_Banner_Frame);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        FrameLayout qureka = (FrameLayout) findViewById(R.id.qureka);
        AdsCommon.RegulerBanner(this, admob_banner, adContainer, qureka);


        this.appDatabase = AppDatabase.getAppDatabase(this);
        SetToolbar();
        LoadFolderList();
        Clicks();
        registerReceiver();
    }

    private void LoadFolderList() {
        this.binding.progressBar.setVisibility(View.VISIBLE);
        this.disposable.add(Observable.fromCallable(new CreatePlaylistActivityObservable1(this)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CreatePlaylistActivityObservable2(this)));
    }

    public Boolean CreatePlaylistActivityObservable1call() throws Exception {
        this.folderModalList = this.appDatabase.folderDao().GetCombineFolderList();
        return false;
    }

    public void CreatePlaylistActivityObservable2call(Boolean bool) throws Exception {
        this.binding.progressBar.setVisibility(View.GONE);
        setAdapter();
        CheckNoData();
    }

    private void SetToolbar() {
        setSupportActionBar(this.binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        this.binding.toolbarLayout.toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
    }

    private void setAdapter() {
        this.adapter = new PlaylistFolderAdapter(this, this.folderModalList, this.MultiFolderList, new PlaylistFolderAdapter.OnFolder() {
            @Override
            public void onFolderClick(int i, int i2) {
                if (CreatePlaylistActivity.this.isMultiSelect) {
                    CreatePlaylistActivity.this.NotifyForLongClick(true);
                    CreatePlaylistActivity createPlaylistActivity = CreatePlaylistActivity.this;
                    createPlaylistActivity.SetMultiSelectList(createPlaylistActivity.adapter.getFilterList().get(i));
                    ActionMode actionMode = CreatePlaylistActivity.this.actionMode;
                    actionMode.setTitle(CreatePlaylistActivity.this.MultiFolderList.size() + " Selected");
                } else if (i2 == 1) {
                    Intent intent = new Intent(CreatePlaylistActivity.this, PlayListItemsActivity.class);
                    intent.putExtra("FolderModal", CreatePlaylistActivity.this.adapter.getFilterList().get(i).getFolderModal());

                    CreatePlaylistActivity.this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult activityResult) {
                            if (activityResult.getData() != null && activityResult.getData().getBooleanExtra("isCreated", false)) {
                                CreatePlaylistActivity.this.folderModalList.clear();
                                CreatePlaylistActivity.this.folderModalList.addAll(CreatePlaylistActivity.this.appDatabase.folderDao().GetCombineFolderList());
                                CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                            }
                            CreatePlaylistActivity.this.adapter.getFilterList().set(i, CreatePlaylistActivity.this.appDatabase.folderDao().GetFolderById(CreatePlaylistActivity.this.adapter.getFilterList().get(i).getFolderModal().getId()));
                            CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                        }
                    });

                } else {
                    CreatePlaylistActivity.this.OpenBottomSheet(i);
                }
            }

            @Override
            public void onFolderLongClick(int i, int i2) {
                CreatePlaylistActivity.this.isMultiSelect = true;
                CreatePlaylistActivity createPlaylistActivity = CreatePlaylistActivity.this;
                createPlaylistActivity.startActionMode(createPlaylistActivity.callback);
                CreatePlaylistActivity.this.NotifyForLongClick(true);
                CreatePlaylistActivity createPlaylistActivity2 = CreatePlaylistActivity.this;
                createPlaylistActivity2.SetMultiSelectList(createPlaylistActivity2.adapter.getFilterList().get(i));
                ActionMode actionMode = CreatePlaylistActivity.this.actionMode;
                actionMode.setTitle(CreatePlaylistActivity.this.MultiFolderList.size() + " Selected");
                Log.d("TAG", "LongClick: " + CreatePlaylistActivity.this.MultiFolderList.size());
            }
        });
        this.binding.recycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        this.binding.recycle.setAdapter(this.adapter);
    }

    public void NotifyForLongClick(boolean z) {
        this.adapter.NotifyLongClick(z);
    }

    public void SetMultiSelectList(CombineFolderModel combineFolderModel) {
        if (this.MultiFolderList.contains(combineFolderModel)) {
            this.adapter.SetSelected(false);
            this.MultiFolderList.remove(combineFolderModel);
        } else {
            this.adapter.SetSelected(true);
            this.MultiFolderList.add(combineFolderModel);
        }
        this.adapter.notifyDataSetChanged();
    }

    
    public void MultiDeletePlayList() {
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
                int indexOf;
                for (int i = 0; i < CreatePlaylistActivity.this.MultiFolderList.size(); i++) {
                    FolderModal folderModal = CreatePlaylistActivity.this.MultiFolderList.get(i).getFolderModal();
                    CreatePlaylistActivity.this.appDatabase.audioVideoDao().DeleteAudioVideoByFolder(folderModal.getId());
                    int indexOf2 = CreatePlaylistActivity.this.folderModalList.indexOf(new CombineFolderModel(folderModal));
                    CreatePlaylistActivity.this.appDatabase.folderDao().DeleteFolder(folderModal);
                    CreatePlaylistActivity.this.folderModalList.remove(indexOf2);
                    if (CreatePlaylistActivity.this.adapter.getFilterList().contains(new CombineFolderModel(folderModal)) && (indexOf = CreatePlaylistActivity.this.adapter.getFilterList().indexOf(new CombineFolderModel(folderModal))) != -1) {
                        CreatePlaylistActivity.this.adapter.getFilterList().remove(indexOf);
                    }
                    CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                }
                CreatePlaylistActivity.this.CheckNoData();
                dialog.dismiss();
                CreatePlaylistActivity.this.actionMode.finish();
            }
        });
        dialogDeleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                CreatePlaylistActivity.this.actionMode.finish();
            }
        });
    }

    
    public void MultipleAddToPlayList() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetAddToPlaylistBinding bottomsheetAddToPlaylistBinding = (BottomsheetAddToPlaylistBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_add_to_playlist, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetAddToPlaylistBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        this.folderModalList.clear();
        this.folderModalList.addAll(this.appDatabase.folderDao().GetCombineFolderList());
        this.bottomPlaylistAdapter = new BottomPlaylistAdapter(this, this.folderModalList, new BottomPlaylistAdapter.FolderClick() {
            @Override
            public void onFolderClick(int i) {
                int i2 = i;
                bottomSheetDialog.dismiss();
                FolderModal folderModal = CreatePlaylistActivity.this.folderModalList.get(i2).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                int size = GetAudioVideoListByFolderID.size() + 1;
                for (int i3 = 0; i3 < CreatePlaylistActivity.this.MultiFolderList.size(); i3++) {
                    List<AudioVideoModal> GetAudioVideoListByFolderID2 = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(CreatePlaylistActivity.this.MultiFolderList.get(i3).getFolderModal().getId());
                    int i4 = 0;
                    while (i4 < GetAudioVideoListByFolderID2.size()) {
                        AudioVideoModal audioVideoModal = GetAudioVideoListByFolderID2.get(i4);
                        int i5 = i4;
                        AudioVideoModal audioVideoModal2 = new AudioVideoModal(audioVideoModal.getUri(), audioVideoModal.getName(), audioVideoModal.getDuration(), audioVideoModal.getArtist(), audioVideoModal.getAlbum(), size);
                        if (!GetAudioVideoListByFolderID.contains(audioVideoModal2)) {
                            audioVideoModal2.setId(AppConstants.getUniqueId());
                            audioVideoModal2.setRefId(folderModal.getId());
                            CreatePlaylistActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal2);
                            size++;
                        }
                        i4 = i5 + 1;
                    }
                }
                CreatePlaylistActivity.this.adapter.getFilterList().set(i2, CreatePlaylistActivity.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                Toast.makeText(CreatePlaylistActivity.this, "Music Added to playlist", Toast.LENGTH_SHORT).show();
                CreatePlaylistActivity.this.actionMode.finish();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                CreatePlaylistActivity.this.MultiPlaylistDialog();
            }
        });
    }

    
    public void MultiPlaylistDialog() {
        final Dialog dialog = new Dialog(this, R.style.dialogTheme);
        final DialogRenameBinding dialogRenameBinding = (DialogRenameBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_rename, (ViewGroup) null, false);
        dialog.setContentView(dialogRenameBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        dialogRenameBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(dialogRenameBinding.edtName.getText())) {
                    dialogRenameBinding.edtName.setError("Enter PlayList Name");
                    return;
                }
                dialog.dismiss();
                FolderModal folderModal = new FolderModal(AppConstants.getUniqueId(), dialogRenameBinding.edtName.getText().toString());
                CreatePlaylistActivity.this.appDatabase.folderDao().InsertFolder(folderModal);
                int i = 0;
                for (int i2 = 0; i2 < CreatePlaylistActivity.this.MultiFolderList.size(); i2++) {
                    List<AudioVideoModal> GetAudioVideoListByFolderID = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(CreatePlaylistActivity.this.MultiFolderList.get(i2).getFolderModal().getId());
                    int i3 = 0;
                    while (i3 < GetAudioVideoListByFolderID.size()) {
                        AudioVideoModal audioVideoModal = GetAudioVideoListByFolderID.get(i3);
                        audioVideoModal.setId(AppConstants.getUniqueId());
                        audioVideoModal.setRefId(folderModal.getId());
                        audioVideoModal.setAudioVideoOrder(i);
                        CreatePlaylistActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal);
                        i3++;
                        i++;
                    }
                }
                CreatePlaylistActivity.this.folderModalList.add(CreatePlaylistActivity.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                CreatePlaylistActivity.this.bottomPlaylistAdapter.notifyDataSetChanged();
                CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                CreatePlaylistActivity.this.CheckNoData();
                CreatePlaylistActivity.this.actionMode.finish();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                CreatePlaylistActivity.this.actionMode.finish();
            }
        });
    }

    
    public void OpenBottomSheet(final int i) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetFolderMenuBinding bottomsheetFolderMenuBinding = (BottomsheetFolderMenuBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_folder_menu, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetFolderMenuBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetFolderMenuBinding.txtTitle.setText(this.adapter.getFilterList().get(i).getFolderModal().getFolderName());
        bottomsheetFolderMenuBinding.llShare.setVisibility(View.GONE);
        bottomsheetFolderMenuBinding.llProperties.setVisibility(View.GONE);
        bottomsheetFolderMenuBinding.llBgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                if (AppPref.getBgAudioList() != null) {
                    AppPref.getBgAudioList().clear();
                }
                ArrayList arrayList = new ArrayList();
                List<AudioVideoModal> GetAudioVideoListByFolderID = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(CreatePlaylistActivity.this.adapter.getFilterList().get(i).getFolderModal().getId());
                for (int i = 0; i < GetAudioVideoListByFolderID.size(); i++) {
                    AudioVideoModal audioVideoModal = GetAudioVideoListByFolderID.get(i);
                    audioVideoModal.setAudioVideoOrder(i);
                    arrayList.add(audioVideoModal);
                }
                AppPref.setBgAudioList(arrayList);
                if (AppPref.getBgAudioList().size() > 0) {
                    Intent intent = new Intent(CreatePlaylistActivity.this, AudioPlayerActivity.class);
                    intent.putExtra("modal", AppPref.getBgAudioList().get(0));
                    CreatePlaylistActivity.this.activityLauncher.launch(intent);
                }
            }
        });
        bottomsheetFolderMenuBinding.llPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int indexOf;
                bottomSheetDialog.dismiss();
                if (AppConstants.isMyServiceRunning(CreatePlaylistActivity.this, AudioService.class)) {
                    ArrayList arrayList = new ArrayList();
                    AudioVideoModal playingModel = CreatePlaylistActivity.this.audioService.getPlayingModel();
                    if (playingModel != null && (indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(playingModel.getUri()))) != -1) {
                        int i = indexOf + 1;
                        int i2 = 0;
                        for (int i3 = 0; i3 < i; i3++) {
                            AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(i3);
                            audioVideoModal.setAudioVideoOrder(i3);
                            arrayList.add(audioVideoModal);
                        }
                        List<AudioVideoModal> GetAudioVideoListByFolderID = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(CreatePlaylistActivity.this.adapter.getFilterList().get(i).getFolderModal().getId());
                        while (i2 < GetAudioVideoListByFolderID.size()) {
                            AudioVideoModal audioVideoModal2 = GetAudioVideoListByFolderID.get(i2);
                            audioVideoModal2.setAudioVideoOrder(i);
                            arrayList.add(audioVideoModal2);
                            i2++;
                            i++;
                        }
                        while (indexOf < AppPref.getBgAudioList().size()) {
                            AudioVideoModal audioVideoModal3 = AppPref.getBgAudioList().get(indexOf);
                            audioVideoModal3.setAudioVideoOrder(indexOf);
                            arrayList.add(audioVideoModal3);
                            indexOf++;
                        }
                        AppPref.getBgAudioList().clear();
                        AppPref.setBgAudioList(arrayList);
                    }
                }
            }
        });
        bottomsheetFolderMenuBinding.llAddToQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                if (AppConstants.isMyServiceRunning(CreatePlaylistActivity.this, AudioService.class)) {
                    int size = AppPref.getBgAudioList().size();
                    List<AudioVideoModal> GetAudioVideoListByFolderID = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(CreatePlaylistActivity.this.adapter.getFilterList().get(i).getFolderModal().getId());
                    ArrayList<AudioVideoModal> bgAudioList = AppPref.getBgAudioList();
                    int i = 0;
                    while (i < GetAudioVideoListByFolderID.size()) {
                        AudioVideoModal audioVideoModal = GetAudioVideoListByFolderID.get(i);
                        int i2 = size + 1;
                        audioVideoModal.setAudioVideoOrder(size);
                        if (!AppPref.getBgAudioList().contains(audioVideoModal)) {
                            bgAudioList.add(audioVideoModal);
                        }
                        i++;
                        size = i2;
                    }
                    AppPref.getBgAudioList().clear();
                    AppPref.setBgAudioList(bgAudioList);
                }
            }
        });
        bottomsheetFolderMenuBinding.llPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                CreatePlaylistActivity createPlaylistActivity = CreatePlaylistActivity.this;
                createPlaylistActivity.AddToPlayList(createPlaylistActivity.adapter.getFilterList().get(i).getFolderModal());
            }
        });
        bottomsheetFolderMenuBinding.llRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                CreatePlaylistActivity.this.OpenRenameDialog(i);
            }
        });
        bottomsheetFolderMenuBinding.llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                CreatePlaylistActivity.this.OpenDeleteDialog(i);
            }
        });
    }

    
    public void AddToPlayList(final FolderModal folderModal) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetAddToPlaylistBinding bottomsheetAddToPlaylistBinding = (BottomsheetAddToPlaylistBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_add_to_playlist, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetAddToPlaylistBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        this.folderModalList.clear();
        this.folderModalList.addAll(this.appDatabase.folderDao().GetCombineFolderList());
        this.bottomPlaylistAdapter = new BottomPlaylistAdapter(this, this.folderModalList, new BottomPlaylistAdapter.FolderClick() {
            @Override
            public void onFolderClick(int i) {
                int i2 = i;
                bottomSheetDialog.dismiss();
                FolderModal folderModal = CreatePlaylistActivity.this.folderModalList.get(i2).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                List<AudioVideoModal> GetAudioVideoListByFolderID2 = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                int size = GetAudioVideoListByFolderID.size() + 1;
                for (int i3 = 0; i3 < GetAudioVideoListByFolderID2.size(); i3++) {
                    AudioVideoModal audioVideoModal = GetAudioVideoListByFolderID2.get(i3);
                    AudioVideoModal audioVideoModal2 = new AudioVideoModal(audioVideoModal.getUri(), audioVideoModal.getName(), audioVideoModal.getDuration(), audioVideoModal.getArtist(), audioVideoModal.getAlbum(), size);
                    if (!GetAudioVideoListByFolderID.contains(audioVideoModal2)) {
                        audioVideoModal2.setId(AppConstants.getUniqueId());
                        audioVideoModal2.setRefId(folderModal.getId());
                        CreatePlaylistActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal2);
                        size++;
                    }
                }
                CreatePlaylistActivity.this.adapter.getFilterList().set(i2, CreatePlaylistActivity.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                Toast.makeText(CreatePlaylistActivity.this, "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                CreatePlaylistActivity.this.PlaylistDialog(folderModal);
            }
        });
    }

    
    public void PlaylistDialog(final FolderModal folderModal) {
        final Dialog dialog = new Dialog(this, R.style.dialogTheme);
        final DialogRenameBinding dialogRenameBinding = (DialogRenameBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_rename, (ViewGroup) null, false);
        dialog.setContentView(dialogRenameBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        dialogRenameBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(dialogRenameBinding.edtName.getText())) {
                    dialogRenameBinding.edtName.setError("Enter PlayList Name");
                    return;
                }
                dialog.dismiss();
                FolderModal folderModal = new FolderModal(AppConstants.getUniqueId(), dialogRenameBinding.edtName.getText().toString());
                CreatePlaylistActivity.this.appDatabase.folderDao().InsertFolder(folderModal);
                List<AudioVideoModal> GetAudioVideoListByFolderID = CreatePlaylistActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                for (int i = 0; i < GetAudioVideoListByFolderID.size(); i++) {
                    AudioVideoModal audioVideoModal = GetAudioVideoListByFolderID.get(i);
                    audioVideoModal.setId(AppConstants.getUniqueId());
                    audioVideoModal.setRefId(folderModal.getId());
                    audioVideoModal.setAudioVideoOrder(i);
                    CreatePlaylistActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal);
                }
                CreatePlaylistActivity.this.folderModalList.add(CreatePlaylistActivity.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                CreatePlaylistActivity.this.bottomPlaylistAdapter.notifyDataSetChanged();
                CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                CreatePlaylistActivity.this.CheckNoData();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    
    public void OpenDeleteDialog(int i) {
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
        final FolderModal folderModal = this.adapter.getFilterList().get(i).getFolderModal();
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int indexOf;
                CreatePlaylistActivity.this.appDatabase.audioVideoDao().DeleteAudioVideoByFolder(folderModal.getId());
                int indexOf2 = CreatePlaylistActivity.this.folderModalList.indexOf(new CombineFolderModel(folderModal));
                CreatePlaylistActivity.this.appDatabase.folderDao().DeleteFolder(folderModal);
                CreatePlaylistActivity.this.folderModalList.remove(indexOf2);
                if (CreatePlaylistActivity.this.adapter.getFilterList().contains(new CombineFolderModel(folderModal)) && (indexOf = CreatePlaylistActivity.this.adapter.getFilterList().indexOf(new CombineFolderModel(folderModal))) != -1) {
                    CreatePlaylistActivity.this.adapter.getFilterList().remove(indexOf);
                }
                CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                CreatePlaylistActivity.this.CheckNoData();
                dialog.dismiss();
            }
        });
        dialogDeleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    
    public void OpenRenameDialog(int i) {
        final Dialog dialog = new Dialog(this, R.style.dialogTheme);
        DialogRenameBinding dialogRenameBinding = (DialogRenameBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_rename, (ViewGroup) null, false);
        dialog.setContentView(dialogRenameBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        final FolderModal folderModal = this.adapter.getFilterList().get(i).getFolderModal();
        dialogRenameBinding.edtName.setText(folderModal.getFolderName());
        final DialogRenameBinding dialogRenameBinding2 = dialogRenameBinding;
        final Dialog dialog2 = dialog;
        final int i2 = i;
        dialogRenameBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(dialogRenameBinding2.edtName.getText())) {
                    dialogRenameBinding2.edtName.setError("Enter PlayList Name");
                    return;
                }
                dialog2.dismiss();
                folderModal.setFolderName(dialogRenameBinding2.edtName.getText().toString());
                CreatePlaylistActivity.this.appDatabase.folderDao().UpdateFolder(folderModal);
                CombineFolderModel combineFolderModel = CreatePlaylistActivity.this.adapter.getFilterList().get(i2);
                combineFolderModel.setFolderModal(folderModal);
                CreatePlaylistActivity.this.adapter.getFilterList().set(i2, combineFolderModel);
                CreatePlaylistActivity.this.adapter.notifyItemChanged(i2);
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void OpenCreatePlaylistDialog() {
        final Dialog dialog = new Dialog(this, R.style.dialogTheme);
        final DialogRenameBinding dialogRenameBinding = (DialogRenameBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_rename, (ViewGroup) null, false);
        dialog.setContentView(dialogRenameBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        dialogRenameBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(dialogRenameBinding.edtName.getText())) {
                    dialogRenameBinding.edtName.setError("Enter PlayList Name");
                    return;
                }
                dialog.dismiss();
                FolderModal folderModal = new FolderModal(AppConstants.getUniqueId(), dialogRenameBinding.edtName.getText().toString());
                CreatePlaylistActivity.this.appDatabase.folderDao().InsertFolder(folderModal);
                CombineFolderModel combineFolderModel = new CombineFolderModel();
                combineFolderModel.setFolderModal(folderModal);
                combineFolderModel.setRefId(folderModal.getId());
                CreatePlaylistActivity.this.folderModalList.add(combineFolderModel);
                CreatePlaylistActivity.this.adapter.notifyDataSetChanged();
                CreatePlaylistActivity.this.CheckNoData();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void CheckNoData() {
        if (this.folderModalList.size() > 0) {
            this.binding.recycle.setVisibility(View.VISIBLE);
            this.binding.rlNoData.setVisibility(View.GONE);
        } else {
            this.binding.recycle.setVisibility(View.GONE);
            this.binding.rlNoData.setVisibility(View.VISIBLE);
        }
        TextView textView = this.binding.toolbarLayout.txtSubTitle;
        textView.setText("" + this.folderModalList.size() + " Playlist");
    }

    private void Clicks() {
        this.binding.llCreatePlayList.setOnClickListener(this);
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
            case R.id.llCreatePlayList:
                OpenCreatePlaylistDialog();
                return;
            default:
                return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.playlist_menu, menu);
        this.search = menu.findItem(R.id.search);
        this.draggable_up = menu.findItem(R.id.draggable_up);
        this.draggable_down = menu.findItem(R.id.draggable_down);
        this.addItem = menu.findItem(R.id.addItem);
        this.draggable_up.setVisible(false);
        this.draggable_down.setVisible(false);
        this.addItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            onBackPressed();
            return true;
        } else if (menuItem.getItemId() != R.id.search) {
            return true;
        } else {
            SearchView searchView = (SearchView) menuItem.getActionView();
            ((ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn)).setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            EditText editText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            editText.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            editText.setHint("Search here");
            editText.setHintTextColor(getResources().getColor(R.color.dialogBg));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String str) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String str) {
                    if (CreatePlaylistActivity.this.adapter == null) {
                        return false;
                    }
                    CreatePlaylistActivity.this.adapter.getFilter().filter(str);
                    return false;
                }
            });
            return true;
        }
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
                    if (CreatePlaylistActivity.this.audioService != null && CreatePlaylistActivity.this.audioService.isPlayerNotNull()) {
                        int currentPosition = CreatePlaylistActivity.this.audioService.getCurrentPosition();
                        CreatePlaylistActivity.this.binding.progress.setProgress(currentPosition);
                        CreatePlaylistActivity.this.binding.txtRunningTime.setText(AppConstants.formatTime((long) currentPosition));
                    }
                    CreatePlaylistActivity.this.myHandler.postDelayed(this, 1000);
                }
            });
        }
    }

    public void bindService() {
        bindService(new Intent(MyApplication.getContext(), AudioService.class), this, 1);
    }

    @Override
    public void onResume() {
        if (AppConstants.isMyServiceRunning(this, AudioService.class)) {
            bindService();
        }
        super.onResume();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mainActivityReceiver,
                    new IntentFilter(AppConstants.MAIN_ACTIVITY_RECEIVER),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mainActivityReceiver,
                    new IntentFilter(AppConstants.MAIN_ACTIVITY_RECEIVER));
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
                char c = 65535;
                switch (stringExtra.hashCode()) {
                    case -2026200673:
                        if (stringExtra.equals("RUNNING")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -971121397:
                        if (stringExtra.equals(AppConstants.PLAY_PAUSE)) {
                            c = 1;
                            break;
                        }
                        break;
                    case 2555906:
                        if (stringExtra.equals(AppConstants.STOP)) {
                            c = 2;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        intent.getIntExtra("position", 0);
                        CreatePlaylistActivity.this.getPlayingModel();
                        return;
                    case 1:
                        if (intent.getIntExtra("action", 0) == 1) {
                            CreatePlaylistActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_play);
                            return;
                        } else {
                            CreatePlaylistActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_pause);
                            return;
                        }
                    case 2:
                        CreatePlaylistActivity.this.binding.llPlayerBottom.setVisibility(View.GONE);
                        if (AppConstants.isMyServiceRunning(CreatePlaylistActivity.this, AudioService.class) && CreatePlaylistActivity.this.audioService != null) {
                            try {
                                CreatePlaylistActivity createPlaylistActivity = CreatePlaylistActivity.this;
                                createPlaylistActivity.unbindService(createPlaylistActivity);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        CreatePlaylistActivity.this.myHandler.removeCallbacksAndMessages((Object) null);
                        return;
                    default:
                        return;
                }
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
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        this.audioService = null;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
