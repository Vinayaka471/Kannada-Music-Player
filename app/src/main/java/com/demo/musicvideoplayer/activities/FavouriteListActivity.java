package com.demo.musicvideoplayer.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.adapter.BottomPlaylistAdapter;
import com.demo.musicvideoplayer.adapter.PlaylistItemAdapter;
import com.demo.musicvideoplayer.ads.AdsCommon;
import com.demo.musicvideoplayer.ads.MyApplication;
import com.demo.musicvideoplayer.database.AppDatabase;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import com.demo.musicvideoplayer.database.model.FolderModal;
import com.demo.musicvideoplayer.databinding.ActivityFavouriteListBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetAddItemBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetAddToPlaylistBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetFolderMenuBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetPropertiesBinding;
import com.demo.musicvideoplayer.databinding.DialogDeleteBinding;
import com.demo.musicvideoplayer.databinding.DialogRenameBinding;
import com.demo.musicvideoplayer.model.AudioModel;
import com.demo.musicvideoplayer.model.CombineFolderModel;
import com.demo.musicvideoplayer.service.AudioService;
import com.demo.musicvideoplayer.utils.AppConstants;
import com.demo.musicvideoplayer.utils.AppPref;
import com.demo.musicvideoplayer.utils.BetterActivityResult;
import com.demo.musicvideoplayer.utils.SwipeAndDragHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FavouriteListActivity extends AppCompatActivity implements ServiceConnection, View.OnClickListener {
    List<AudioVideoModal> MultiList = new ArrayList();
    ActionMode actionMode;
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    PlaylistItemAdapter adapter;
    MenuItem addItem;
    AppDatabase appDatabase;
    AudioService audioService;
    ActivityFavouriteListBinding binding;
    BottomPlaylistAdapter bottomPlaylistAdapter;
    ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            FavouriteListActivity.this.isMultiSelect = true;
            actionMode.getMenuInflater().inflate(R.menu.play_list_multi_select, menu);
            FavouriteListActivity.this.actionMode = actionMode;
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
                for (int i = 0; i < FavouriteListActivity.this.MultiList.size(); i++) {
                    AudioVideoModal audioVideoModal = FavouriteListActivity.this.MultiList.get(i);
                    audioVideoModal.setAudioVideoOrder(i);
                    arrayList.add(audioVideoModal);
                }
                AppPref.setBgAudioList(arrayList);
                if (AppPref.getBgAudioList().size() > 0) {
                    Intent intent = new Intent(FavouriteListActivity.this, AudioPlayerActivity.class);
                    intent.putExtra("modal", AppPref.getBgAudioList().get(0));
                    FavouriteListActivity.this.activityLauncher.launch(intent);
                }
                FavouriteListActivity.this.actionMode.finish();
            } else if (menuItem.getItemId() == R.id.playNext) {
                if (AppConstants.isMyServiceRunning(FavouriteListActivity.this, AudioService.class)) {
                    ArrayList arrayList2 = new ArrayList();
                    AudioVideoModal playingModel = FavouriteListActivity.this.audioService.getPlayingModel();
                    if (!(playingModel == null || (indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(playingModel.getUri()))) == -1)) {
                        int i2 = indexOf + 1;
                        for (int i3 = 0; i3 < i2; i3++) {
                            AudioVideoModal audioVideoModal2 = AppPref.getBgAudioList().get(i3);
                            audioVideoModal2.setAudioVideoOrder(i3);
                            arrayList2.add(audioVideoModal2);
                        }
                        int i4 = i2 + 1;
                        for (int i5 = 0; i5 < FavouriteListActivity.this.MultiList.size(); i5++) {
                            AudioVideoModal audioVideoModal3 = FavouriteListActivity.this.MultiList.get(i5);
                            arrayList2.add(new AudioVideoModal(audioVideoModal3.getUri(), audioVideoModal3.getName(), audioVideoModal3.getDuration(), audioVideoModal3.getArtist(), audioVideoModal3.getAlbum(), i4));
                            i4++;
                        }
                        while (indexOf < AppPref.getBgAudioList().size()) {
                            AudioVideoModal audioVideoModal4 = AppPref.getBgAudioList().get(indexOf);
                            audioVideoModal4.setAudioVideoOrder(i4);
                            arrayList2.add(audioVideoModal4);
                            i4++;
                            indexOf++;
                        }
                        AppPref.getBgAudioList().clear();
                        AppPref.setBgAudioList(arrayList2);
                    }
                }
                FavouriteListActivity.this.actionMode.finish();
            } else if (menuItem.getItemId() == R.id.addToQueue) {
                if (AppConstants.isMyServiceRunning(FavouriteListActivity.this, AudioService.class)) {
                    ArrayList<AudioVideoModal> bgAudioList = AppPref.getBgAudioList();
                    int size = AppPref.getBgAudioList().size();
                    int i6 = 0;
                    while (i6 < FavouriteListActivity.this.MultiList.size()) {
                        AudioVideoModal audioVideoModal5 = FavouriteListActivity.this.MultiList.get(i6);
                        int i7 = size + 1;
                        audioVideoModal5.setAudioVideoOrder(size);
                        if (!AppPref.getBgAudioList().contains(audioVideoModal5)) {
                            bgAudioList.add(audioVideoModal5);
                        }
                        i6++;
                        size = i7;
                    }
                    AppPref.getBgAudioList().clear();
                    AppPref.setBgAudioList(bgAudioList);
                }
                FavouriteListActivity.this.actionMode.finish();
            } else if (menuItem.getItemId() == R.id.addToPlaylist) {
                FavouriteListActivity.this.MultipleAddToPlayListItem();
                FavouriteListActivity.this.actionMode.finish();
            } else if (menuItem.getItemId() == R.id.delete) {
                FavouriteListActivity.this.MultiDeletePlayListItem();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            FavouriteListActivity.this.isMultiSelect = false;
            FavouriteListActivity.this.NotifyForLongClick(false);
            FavouriteListActivity.this.adapter.notifyDataSetChanged();
            FavouriteListActivity.this.MultiList.clear();
        }
    };
    public CompositeDisposable disposable = new CompositeDisposable();
    MenuItem draggable_down;
    MenuItem draggable_up;
    List<AudioVideoModal> favList = new ArrayList();
    List<CombineFolderModel> folderModalList = new ArrayList();
    boolean isDraggable = false;
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
        this.binding = (ActivityFavouriteListBinding) DataBindingUtil.setContentView(this, R.layout.activity_favourite_list);

        applyDisplayCutouts();

        AdsCommon.InterstitialAdsOnly(this);


        //Reguler Banner Ads
        RelativeLayout admob_banner = (RelativeLayout) findViewById(R.id.Admob_Banner_Frame);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        FrameLayout qureka = (FrameLayout) findViewById(R.id.qureka);
        AdsCommon.RegulerBanner(this, admob_banner, adContainer, qureka);


        this.appDatabase = AppDatabase.getAppDatabase(this);
        setToolbar();
        LoadFavList();
        Clicks();
        registerReceiver();
        this.binding.appBarLayout.addOnOffsetChangedListener((AppBarLayout.BaseOnOffsetChangedListener) new AppBarLayout.BaseOnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if (Math.abs(i) > 200) {
                    FavouriteListActivity.this.binding.playCard.hide();
                } else {
                    FavouriteListActivity.this.binding.playCard.show();
                }
            }
        });
    }

    private void setToolbar() {
        setSupportActionBar(this.binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        this.binding.toolbarLayout.toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
        this.binding.toolbarLayout.txtSubTitle.setText("Favourite");
    }

    private void LoadFavList() {
        this.binding.progressBar.setVisibility(View.VISIBLE);
        this.disposable.add(Observable.fromCallable(new FavouriteListActivityObservable1(this)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new FavouriteListActivityObservable2(this)));
    }

    public Boolean FavouriteListActivityObservable1call() throws Exception {
        if (AppPref.getFavouriteList() != null) {
            this.favList.addAll(AppPref.getFavouriteList());
        }
        return false;
    }

    public void FavouriteListActivityObservable2call(Boolean bool) throws Exception {
        this.binding.progressBar.setVisibility(View.GONE);
        setFolderArt();
        setAdapter();
        CheckNoData();
        SortListAsc();
    }

    private void setAdapter() {
        this.adapter = new PlaylistItemAdapter(this, this.favList, this.MultiList, new PlaylistItemAdapter.OnPlayList() {
            @Override
            public void onPlayListClick(int i, int i2, View view) {
                if (i2 == 2) {
                    if (FavouriteListActivity.this.isMultiSelect) {
                        FavouriteListActivity.this.NotifyForLongClick(true);
                        FavouriteListActivity favouriteListActivity = FavouriteListActivity.this;
                        favouriteListActivity.SetMultiSelectList(favouriteListActivity.adapter.getFilterList().get(i));
                        FavouriteListActivity.this.actionMode.setTitle(FavouriteListActivity.this.MultiList.size() + " Selected");
                        return;
                    }
                    if (AppPref.getBgAudioList() != null) {
                        AppPref.getBgAudioList().clear();
                    }
                    ArrayList arrayList = new ArrayList();
                    for (int i3 = 0; i3 < FavouriteListActivity.this.adapter.getFilterList().size(); i3++) {
                        AudioVideoModal audioVideoModal = FavouriteListActivity.this.adapter.getFilterList().get(i3);
                        arrayList.add(new AudioVideoModal(audioVideoModal.getUri(), audioVideoModal.getName(), audioVideoModal.getDuration(), audioVideoModal.getArtist(), audioVideoModal.getAlbum(), i3));
                    }
                    AppPref.setBgAudioList(arrayList);
                    Intent intent = new Intent(FavouriteListActivity.this, AudioPlayerActivity.class);
                    int indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(FavouriteListActivity.this.adapter.getFilterList().get(i).getUri()));
                    if (indexOf != -1) {
                        intent.putExtra("modal", AppPref.getBgAudioList().get(indexOf));
                    } else {
                        intent.putExtra("modal", AppPref.getBgAudioList().get(0));
                    }
                    FavouriteListActivity.this.activityLauncher.launch(intent);
                } else if (i2 == 3) {
                    FavouriteListActivity.this.OpenBottomSheet(i);
                }
            }

            @Override
            public void onPlayListLongClick(int i, int i2, View view) {
                FavouriteListActivity.this.isMultiSelect = true;
                FavouriteListActivity favouriteListActivity = FavouriteListActivity.this;
                favouriteListActivity.startActionMode(favouriteListActivity.callback);
                FavouriteListActivity.this.NotifyForLongClick(true);
                FavouriteListActivity favouriteListActivity2 = FavouriteListActivity.this;
                favouriteListActivity2.SetMultiSelectList(favouriteListActivity2.adapter.getFilterList().get(i));
                ActionMode actionMode = FavouriteListActivity.this.actionMode;
                actionMode.setTitle(FavouriteListActivity.this.MultiList.size() + " Selected");
                Log.d("TAG", "LongClick: " + FavouriteListActivity.this.MultiList.size());
            }
        }, true);
        this.binding.recycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeAndDragHelper(this.adapter));
        this.adapter.setTouchHelper(itemTouchHelper);
        this.binding.recycle.setAdapter(this.adapter);
        itemTouchHelper.attachToRecyclerView(this.binding.recycle);
    }

    public void SortListAsc() {
        Collections.sort(this.favList, new Comparator<AudioVideoModal>() {
            @Override
            public int compare(AudioVideoModal audioVideoModal, AudioVideoModal audioVideoModal2) {
                return audioVideoModal.getAudioVideoOrder() - audioVideoModal2.getAudioVideoOrder();
            }
        });
        this.adapter.notifyDataSetChanged();
    }

    public void NotifyForLongClick(boolean z) {
        this.adapter.NotifyLongClick(z);
    }

    public void SetMultiSelectList(AudioVideoModal audioVideoModal) {
        if (this.MultiList.contains(audioVideoModal)) {
            this.adapter.SetSelected(false);
            this.MultiList.remove(audioVideoModal);
        } else {
            this.adapter.SetSelected(true);
            this.MultiList.add(audioVideoModal);
        }
        this.adapter.notifyDataSetChanged();
    }

    
    public void MultipleAddToPlayListItem() {
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
                bottomSheetDialog.dismiss();
                FolderModal folderModal = FavouriteListActivity.this.folderModalList.get(i).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = FavouriteListActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                int size = GetAudioVideoListByFolderID.size();
                int i2 = 0;
                while (i2 < FavouriteListActivity.this.MultiList.size()) {
                    AudioVideoModal audioVideoModal = FavouriteListActivity.this.MultiList.get(i2);
                    int i3 = size + 1;
                    AudioVideoModal audioVideoModal2 = new AudioVideoModal(audioVideoModal.getUri(), audioVideoModal.getName(), audioVideoModal.getDuration(), audioVideoModal.getArtist(), audioVideoModal.getAlbum(), size);
                    if (!GetAudioVideoListByFolderID.contains(audioVideoModal2)) {
                        audioVideoModal2.setId(AppConstants.getUniqueId());
                        audioVideoModal2.setRefId(folderModal.getId());
                        FavouriteListActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal2);
                    }
                    i2++;
                    size = i3;
                }
                Toast.makeText(FavouriteListActivity.this, "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                FavouriteListActivity.this.OpenCreateMultiPlaylistDialog();
            }
        });
    }

    
    public void OpenCreateMultiPlaylistDialog() {
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
                FavouriteListActivity.this.appDatabase.folderDao().InsertFolder(folderModal);
                for (int i = 0; i < FavouriteListActivity.this.MultiList.size(); i++) {
                    AudioVideoModal audioVideoModal = FavouriteListActivity.this.MultiList.get(i);
                    FavouriteListActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(new AudioVideoModal(AppConstants.getUniqueId(), audioVideoModal.getUri(), audioVideoModal.getName(), audioVideoModal.getDuration(), audioVideoModal.getArtist(), audioVideoModal.getAlbum(), i, folderModal.getId()));
                }
                FavouriteListActivity.this.folderModalList.add(FavouriteListActivity.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                FavouriteListActivity.this.bottomPlaylistAdapter.notifyDataSetChanged();
                Toast.makeText(FavouriteListActivity.this, "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    
    public void MultiDeletePlayListItem() {
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
                ArrayList<AudioVideoModal> favouriteList = AppPref.getFavouriteList();
                for (int i = 0; i < FavouriteListActivity.this.MultiList.size(); i++) {
                    AudioVideoModal audioVideoModal = FavouriteListActivity.this.MultiList.get(i);
                    if (favouriteList.contains(audioVideoModal)) {
                        favouriteList.remove(favouriteList.indexOf(audioVideoModal));
                    }
                    int indexOf = FavouriteListActivity.this.favList.indexOf(audioVideoModal);
                    FavouriteListActivity.this.favList.remove(indexOf);
                    FavouriteListActivity.this.adapter.notifyItemRemoved(indexOf);
                    int indexOf2 = FavouriteListActivity.this.adapter.getFilterList().indexOf(audioVideoModal);
                    if (indexOf2 != -1) {
                        FavouriteListActivity.this.adapter.getFilterList().remove(indexOf2);
                        FavouriteListActivity.this.adapter.notifyItemRemoved(indexOf2);
                    }
                }
                if (AppPref.getFavouriteList() != null) {
                    AppPref.getFavouriteList().clear();
                }
                AppPref.setFavouriteList(favouriteList);
                FavouriteListActivity.this.CheckNoData();
                dialog.dismiss();
                FavouriteListActivity.this.actionMode.finish();
            }
        });
        dialogDeleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                FavouriteListActivity.this.actionMode.finish();
            }
        });
    }

    
    public void OpenBottomSheet(final int i) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetFolderMenuBinding bottomsheetFolderMenuBinding = (BottomsheetFolderMenuBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_folder_menu, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetFolderMenuBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetFolderMenuBinding.llRename.setVisibility(View.GONE);
        bottomsheetFolderMenuBinding.llBgPlay.setVisibility(View.GONE);
        bottomsheetFolderMenuBinding.txtTitle.setText(this.adapter.getFilterList().get(i).getName());
        bottomsheetFolderMenuBinding.llPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int indexOf;
                bottomSheetDialog.dismiss();
                if (AppConstants.isMyServiceRunning(FavouriteListActivity.this, AudioService.class)) {
                    ArrayList arrayList = new ArrayList();
                    AudioVideoModal playingModel = FavouriteListActivity.this.audioService.getPlayingModel();
                    if (playingModel != null && (indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(playingModel.getUri()))) != -1) {
                        int i = indexOf + 1;
                        for (int i2 = 0; i2 < i; i2++) {
                            AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(i2);
                            audioVideoModal.setAudioVideoOrder(i2);
                            arrayList.add(audioVideoModal);
                        }
                        int i3 = i + 1;
                        AudioVideoModal audioVideoModal2 = FavouriteListActivity.this.adapter.getFilterList().get(i);
                        arrayList.add(new AudioVideoModal(audioVideoModal2.getUri(), audioVideoModal2.getName(), audioVideoModal2.getDuration(), audioVideoModal2.getArtist(), audioVideoModal2.getAlbum(), i3));
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
                if (AppConstants.isMyServiceRunning(FavouriteListActivity.this, AudioService.class)) {
                    AudioVideoModal audioVideoModal = FavouriteListActivity.this.adapter.getFilterList().get(i);
                    ArrayList<AudioVideoModal> bgAudioList = AppPref.getBgAudioList();
                    audioVideoModal.setAudioVideoOrder(AppPref.getBgAudioList().size() + 1);
                    if (!AppPref.getBgAudioList().contains(audioVideoModal)) {
                        bgAudioList.add(audioVideoModal);
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
                FavouriteListActivity favouriteListActivity = FavouriteListActivity.this;
                favouriteListActivity.OpenPlaylistBottomSheet(favouriteListActivity.adapter.getFilterList().get(i));
            }
        });
        bottomsheetFolderMenuBinding.llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                FavouriteListActivity favouriteListActivity = FavouriteListActivity.this;
                favouriteListActivity.SharePlayListItem(favouriteListActivity.adapter.getFilterList().get(i).getUri());
            }
        });
        bottomsheetFolderMenuBinding.llProperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                FavouriteListActivity.this.OpenPropertiesBottomSheet(i);
            }
        });
        bottomsheetFolderMenuBinding.llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                FavouriteListActivity.this.OpenDeleteDialog(i);
            }
        });
    }

    
    public void OpenPlaylistBottomSheet(final AudioVideoModal audioVideoModal1) {
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
                bottomSheetDialog.dismiss();
                FolderModal folderModal = FavouriteListActivity.this.folderModalList.get(i).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = FavouriteListActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                AudioVideoModal audioVideoModal = new AudioVideoModal(audioVideoModal1.getUri(), audioVideoModal1.getName(), audioVideoModal1.getDuration(), audioVideoModal1.getArtist(), audioVideoModal1.getAlbum(), GetAudioVideoListByFolderID.size() + 1);
                if (!GetAudioVideoListByFolderID.contains(audioVideoModal)) {
                    audioVideoModal.setId(AppConstants.getUniqueId());
                    audioVideoModal.setRefId(folderModal.getId());
                    FavouriteListActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal);
                }
                Toast.makeText(FavouriteListActivity.this, "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                FavouriteListActivity.this.OpenCreatePlaylistDialog(audioVideoModal1);
            }
        });
    }

    
    public void OpenCreatePlaylistDialog(final AudioVideoModal audioVideoModal) {
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
                FavouriteListActivity.this.appDatabase.folderDao().InsertFolder(folderModal);
                FavouriteListActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(new AudioVideoModal(AppConstants.getUniqueId(), audioVideoModal.getUri(), audioVideoModal.getName(), audioVideoModal.getDuration(), audioVideoModal.getArtist(), audioVideoModal.getAlbum(), 0, folderModal.getId()));
                FavouriteListActivity.this.folderModalList.add(FavouriteListActivity.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                FavouriteListActivity.this.bottomPlaylistAdapter.notifyDataSetChanged();
                Toast.makeText(FavouriteListActivity.this, "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    
    public void OpenPropertiesBottomSheet(int i) {
        AudioVideoModal audioVideoModal = this.adapter.getFilterList().get(i);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetPropertiesBinding bottomsheetPropertiesBinding = (BottomsheetPropertiesBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_properties, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetPropertiesBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetPropertiesBinding.llformat.setVisibility(View.GONE);
        bottomsheetPropertiesBinding.llsize.setVisibility(View.GONE);
        bottomsheetPropertiesBinding.txtFileName.setText(audioVideoModal.getName());
        if (Build.VERSION.SDK_INT > 29) {
            bottomsheetPropertiesBinding.txtLocation.setText(AppConstants.GetPathFromUri(this, audioVideoModal.getUri()));
        } else {
            bottomsheetPropertiesBinding.txtLocation.setText(audioVideoModal.getUri());
        }
        bottomsheetPropertiesBinding.txtLength.setText(AppConstants.formatTime(audioVideoModal.getDuration()));
        if (!TextUtils.isEmpty(audioVideoModal.getAlbum()) || !TextUtils.isEmpty(audioVideoModal.getArtist())) {
            bottomsheetPropertiesBinding.txtAlbumName.setText(audioVideoModal.getAlbum());
            bottomsheetPropertiesBinding.txtArtistName.setText(audioVideoModal.getArtist());
            return;
        }
        bottomsheetPropertiesBinding.llArtist.setVisibility(View.GONE);
        bottomsheetPropertiesBinding.llAlbum.setVisibility(View.GONE);
    }

    
    public void SharePlayListItem(String str) {
        Uri uri;
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction("android.intent.action.SEND");
        if (Build.VERSION.SDK_INT > 29) {
            uri = Uri.parse(str);
        } else {
            uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(str));
        }
        intent.putExtra("android.intent.extra.STREAM", uri);
        try {
            startActivity(Intent.createChooser(intent, "Share File "));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    
    public void OpenDeleteDialog(final int i) {
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
        final AudioVideoModal audioVideoModal = this.adapter.getFilterList().get(i);
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FavouriteListActivity.this.appDatabase.audioVideoDao().DeleteAudioVideo(audioVideoModal);
                FavouriteListActivity.this.favList.remove(audioVideoModal);
                FavouriteListActivity.this.adapter.notifyItemRemoved(i);
                int indexOf = FavouriteListActivity.this.adapter.getFilterList().indexOf(audioVideoModal);
                if (indexOf != -1) {
                    FavouriteListActivity.this.adapter.getFilterList().remove(indexOf);
                    FavouriteListActivity.this.adapter.notifyItemRemoved(indexOf);
                }
                FavouriteListActivity.this.CheckNoData();
                dialog.dismiss();
                if (AppPref.getFavouriteList() != null) {
                    AppPref.getFavouriteList().clear();
                }
                AppPref.setFavouriteList(FavouriteListActivity.this.favList);
                FavouriteListActivity.this.setFolderArt();
            }
        });
        dialogDeleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    
    public void setFolderArt() {
        if (this.favList.size() > 0) {
            this.binding.imgArt.setVisibility(View.VISIBLE);
            AudioVideoModal audioVideoModal = this.favList.get(0);
            if (TextUtils.isEmpty(audioVideoModal.getArtist()) && TextUtils.isEmpty(audioVideoModal.getAlbum())) {
                Glide.with((FragmentActivity) this).load(audioVideoModal.getUri()).into(this.binding.imgArt);
            } else if (audioVideoModal.getUri() != null) {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                if (Build.VERSION.SDK_INT > 29) {
                    mediaMetadataRetriever.setDataSource(this, Uri.parse(audioVideoModal.getUri()));
                } else {
                    mediaMetadataRetriever.setDataSource(this, FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(audioVideoModal.getUri())));
                }
                byte[] embeddedPicture = mediaMetadataRetriever.getEmbeddedPicture();
                if (embeddedPicture != null) {
                    Bitmap decodeByteArray = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
                    this.binding.imgArt.setVisibility(View.VISIBLE);
                    Glide.with((FragmentActivity) this).load(decodeByteArray).into(this.binding.imgArt);
                }
            }
        } else {
            this.binding.imgArt.setVisibility(View.GONE);
        }
    }

    
    public void CheckNoData() {
        if (this.favList.size() > 0) {
            this.binding.recycle.setVisibility(View.VISIBLE);
            this.binding.rlNoData.setVisibility(View.GONE);
            return;
        }
        this.binding.recycle.setVisibility(View.GONE);
        this.binding.rlNoData.setVisibility(View.VISIBLE);
    }

    private void OpenAddItemBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetAddItemBinding bottomsheetAddItemBinding = (BottomsheetAddItemBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_add_item, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetAddItemBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetAddItemBinding.cardVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(FavouriteListActivity.this, AudioVideoListActivity.class);
                intent.putExtra("isForVideo", true);
                intent.putExtra("isFormFav", true);

                FavouriteListActivity.this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult activityResult) {
                        if (activityResult.getResultCode() == -1 && activityResult.getData() != null) {
                            ArrayList parcelableArrayListExtra = activityResult.getData().getParcelableArrayListExtra("AudioVideoList");
                            FavouriteListActivity.this.favList.addAll(parcelableArrayListExtra);
                            if (AppPref.getFavouriteList() != null) {
                                AppPref.getFavouriteList().clear();
                                AppPref.setFavouriteList(FavouriteListActivity.this.favList);
                            } else {
                                AppPref.setFavouriteList(parcelableArrayListExtra);
                            }
                            FavouriteListActivity.this.adapter.notifyDataSetChanged();
                            FavouriteListActivity.this.CheckNoData();
                            FavouriteListActivity.this.setFolderArt();
                        }
                    }
                });

            }

        });
        bottomsheetAddItemBinding.cardMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(FavouriteListActivity.this, AudioVideoListActivity.class);
                intent.putExtra("isForVideo", false);
                intent.putExtra("isFormFav", true);
                FavouriteListActivity.this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult activityResult) {
                        if (activityResult.getResultCode() == -1 && activityResult.getData() != null) {
                            ArrayList parcelableArrayListExtra = activityResult.getData().getParcelableArrayListExtra("AudioVideoList");
                            FavouriteListActivity.this.favList.addAll(parcelableArrayListExtra);
                            if (AppPref.getFavouriteList() != null) {
                                AppPref.getFavouriteList().clear();
                                AppPref.setFavouriteList(FavouriteListActivity.this.favList);
                            } else {
                                AppPref.setFavouriteList(parcelableArrayListExtra);
                            }
                            FavouriteListActivity.this.adapter.notifyDataSetChanged();
                            FavouriteListActivity.this.CheckNoData();
                            FavouriteListActivity.this.setFolderArt();
                        }
                    }
                });
            }

        });
    }

    private void Clicks() {
        this.binding.playCard.setOnClickListener(this);
        this.binding.btnNext.setOnClickListener(this);
        this.binding.btnPrevious.setOnClickListener(this);
        this.binding.framePlayPause.setOnClickListener(this);
        this.binding.llContentPlayer.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.playlist_menu, menu);
        this.search = menu.findItem(R.id.search);
        this.draggable_up = menu.findItem(R.id.draggable_up);
        this.draggable_down = menu.findItem(R.id.draggable_down);
        this.addItem = menu.findItem(R.id.addItem);
        this.draggable_down.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            onBackPressed();
        } else if (menuItem.getItemId() == R.id.search) {
            SearchView searchView = (SearchView) menuItem.getActionView();
            ((ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn)).setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            EditText editText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            editText.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            editText.setHint("Search here");
            editText.setHintTextColor(getResources().getColor(R.color.white));
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean z) {
                    if (z) {
                        FavouriteListActivity.this.draggable_up.setVisible(false);
                        FavouriteListActivity.this.addItem.setVisible(false);
                        FavouriteListActivity.this.binding.playCard.hide();
                    } else {
                        FavouriteListActivity.this.draggable_up.setVisible(true);
                        FavouriteListActivity.this.addItem.setVisible(true);
                        FavouriteListActivity.this.binding.playCard.show();
                        FavouriteListActivity.this.CheckNoData();
                    }
                    FavouriteListActivity.this.setAdapterPlayingSong();
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String str) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String str) {
                    if (FavouriteListActivity.this.adapter == null) {
                        return false;
                    }
                    FavouriteListActivity.this.adapter.getFilter().filter(str);
                    if (FavouriteListActivity.this.adapter.getFilterList().size() > 0) {
                        FavouriteListActivity.this.binding.rlNoData.setVisibility(View.GONE);
                    } else {
                        FavouriteListActivity.this.binding.rlNoData.setVisibility(View.VISIBLE);
                    }
                    FavouriteListActivity.this.setAdapterPlayingSong();
                    return false;
                }
            });
        } else if (menuItem.getItemId() == R.id.draggable_up) {
            this.draggable_up.setVisible(false);
            this.draggable_down.setVisible(true);
            this.adapter.setDraggable(true);
        } else if (menuItem.getItemId() == R.id.addItem) {
            OpenAddItemBottomSheet();
        } else if (menuItem.getItemId() == R.id.draggable_down) {
            this.draggable_up.setVisible(true);
            this.draggable_down.setVisible(false);
            this.adapter.setDraggable(false);
        }
        return true;
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
                    if (FavouriteListActivity.this.audioService != null && FavouriteListActivity.this.audioService.isPlayerNotNull()) {
                        int currentPosition = FavouriteListActivity.this.audioService.getCurrentPosition();
                        FavouriteListActivity.this.binding.progress.setProgress(currentPosition);
                        FavouriteListActivity.this.binding.txtRunningTime.setText(AppConstants.formatTime((long) currentPosition));
                    }
                    FavouriteListActivity.this.myHandler.postDelayed(this, 1000);
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
            case R.id.playCard:
                if (AppPref.getBgAudioList() != null) {
                    AppPref.getBgAudioList().clear();
                }
                ArrayList arrayList = new ArrayList();
                for (int i = 0; i < this.favList.size(); i++) {
                    AudioVideoModal audioVideoModal = this.favList.get(i);
                    audioVideoModal.setAudioVideoOrder(i);
                    arrayList.add(audioVideoModal);
                }
                AppPref.setBgAudioList(arrayList);
                if (AppPref.getBgAudioList().size() > 0) {
                    Intent intent2 = new Intent(this, AudioPlayerActivity.class);
                    intent2.putExtra("modal", AppPref.getBgAudioList().get(0));
                    this.activityLauncher.launch(intent2);
                    return;
                }
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
                        int indexOf = FavouriteListActivity.this.adapter.getFilterList().indexOf(new AudioModel(AppPref.getBgAudioList().get(intent.getIntExtra("position", 0)).getUri()));
                        if (indexOf != -1) {
                            FavouriteListActivity.this.adapter.setPlayingPos(indexOf);
                            FavouriteListActivity.this.adapter.setIsPlaying(true);
                        } else {
                            FavouriteListActivity.this.adapter.setIsPlaying(false);
                        }
                        FavouriteListActivity.this.getPlayingModel();
                        return;
                    case 1:
                        if (intent.getIntExtra("action", 0) == 1) {
                            FavouriteListActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_play);
                        } else {
                            FavouriteListActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_pause);
                        }
                        FavouriteListActivity.this.setAdapterPlayingSong();
                        return;
                    case 2:
                        FavouriteListActivity.this.binding.llPlayerBottom.setVisibility(View.GONE);
                        if (AppConstants.isMyServiceRunning(FavouriteListActivity.this, AudioService.class)) {
                            try {
                                FavouriteListActivity favouriteListActivity = FavouriteListActivity.this;
                                favouriteListActivity.unbindService(favouriteListActivity);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        FavouriteListActivity.this.adapter.setIsPlaying(false);
                        FavouriteListActivity.this.myHandler.removeCallbacksAndMessages((Object) null);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public void setAdapterPlayingSong() {
        PlaylistItemAdapter playlistItemAdapter;
        AudioService audioService2 = this.audioService;
        if (audioService2 != null) {
            int indexOf = this.adapter.getFilterList().indexOf(new AudioVideoModal(audioService2.getPlayingModel().getUri()));
            if (indexOf != -1 && (playlistItemAdapter = this.adapter) != null) {
                playlistItemAdapter.setPlayingPos(indexOf);
                this.adapter.setIsPlaying(true);
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

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
