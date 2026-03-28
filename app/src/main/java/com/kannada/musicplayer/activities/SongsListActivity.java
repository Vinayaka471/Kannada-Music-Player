package com.kannada.musicplayer.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
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
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.adapter.BottomPlaylistAdapter;
import com.kannada.musicplayer.adapter.SongsAdapter;
import com.kannada.musicplayer.ads.AdsCommon;
import com.kannada.musicplayer.ads.MyApplication;
import com.kannada.musicplayer.database.AppDatabase;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.database.model.FolderModal;
import com.kannada.musicplayer.databinding.ActivitySongsListBinding;
import com.kannada.musicplayer.databinding.BottomsheetAddToPlaylistBinding;
import com.kannada.musicplayer.databinding.BottomsheetFolderMenuBinding;
import com.kannada.musicplayer.databinding.BottomsheetPropertiesBinding;
import com.kannada.musicplayer.databinding.DialogDeleteBinding;
import com.kannada.musicplayer.databinding.DialogRenameBinding;
import com.kannada.musicplayer.model.AlbumModel;
import com.kannada.musicplayer.model.ArtistModel;
import com.kannada.musicplayer.model.AudioFolderModal;
import com.kannada.musicplayer.model.AudioModel;
import com.kannada.musicplayer.model.CombineFolderModel;
import com.kannada.musicplayer.service.AudioService;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;
import com.kannada.musicplayer.utils.BetterActivityResult;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongsListActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    SongsAdapter adapter;
    AppDatabase appDatabase;
    List<AudioModel> audioModelList = new ArrayList();
    AudioService audioService;
    ActivitySongsListBinding binding;
    BottomPlaylistAdapter bottomPlaylistAdapter;
    ActivityResultLauncher<IntentSenderRequest> deleteLauncher;
    int deletePos;
    ArrayList<AudioModel> deletedAudioList = new ArrayList<>();
    public CompositeDisposable disposable = new CompositeDisposable();
    List<CombineFolderModel> folderModalList = new ArrayList();
    MainActivityReceiver mReceiver;
    
    public Handler myHandler = new Handler();
    int type;

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
        this.binding = (ActivitySongsListBinding) DataBindingUtil.setContentView(this, R.layout.activity_songs_list);

        applyDisplayCutouts();



        //Reguler Banner Ads


        this.appDatabase = AppDatabase.getAppDatabase(this);
        this.type = getIntent().getIntExtra("audioListType", 0);
        setToolbar();
        LoadAudioList();
        registerReceiver();
        Clicks();
        this.deleteLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
                if (activityResult.getResultCode() == -1 && SongsListActivity.this.deletePos != -1) {
                    SongsListActivity.this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(SongsListActivity.this.audioModelList.get(SongsListActivity.this.deletePos).getPath());
                    SongsListActivity.this.deletedAudioList.add(SongsListActivity.this.audioModelList.get(SongsListActivity.this.deletePos));
                    SongsListActivity.this.audioModelList.remove(SongsListActivity.this.deletePos);
                    SongsListActivity.this.adapter.notifyItemRemoved(SongsListActivity.this.deletePos);
                    Toast.makeText(SongsListActivity.this, "Delete File Successfully", Toast.LENGTH_SHORT).show();
                }
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

    private void setToolbar() {
        setSupportActionBar(this.binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        this.binding.toolbarLayout.toolbar.setNavigationIcon((int) R.drawable.ic_back);
    }

    private void LoadAudioList() {
        this.binding.progressBar.setVisibility(View.VISIBLE);
        this.disposable.add(Observable.fromCallable(new SongsListActivityObservable1(this)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SongsListActivityObservable2(this)));
    }

    public Boolean SongsListActivityObservable1call() throws Exception {
        int i = this.type;
        if (i == 1) {
            AudioFolderModal audioFolderModal = (AudioFolderModal) getIntent().getParcelableExtra("FolderModal");
            this.audioModelList.addAll(audioFolderModal.getAudioList());
            this.binding.collapsing.setTitle(audioFolderModal.getBucketName());
        } else if (i == 2) {
            AlbumModel albumModel = (AlbumModel) getIntent().getParcelableExtra("AlbumModel");
            this.audioModelList.addAll(albumModel.getAudioModelList());
            this.binding.collapsing.setTitle(albumModel.getAlbum());
        } else if (i == 3) {
            ArtistModel artistModel = (ArtistModel) getIntent().getParcelableExtra("ArtistModel");
            this.audioModelList.addAll(artistModel.getAudioModelList());
            this.binding.collapsing.setTitle(artistModel.getArtist());
        }
        return false;
    }

    public void SongsListActivityObservable2call(Boolean bool) throws Exception {
        this.binding.progressBar.setVisibility(View.GONE);
        setAdapter();
        setImageBitmap();
    }

    private void setAdapter() {
        this.adapter = new SongsAdapter(this, this.audioModelList, new SongsAdapter.OnItemClick() {
            @Override
            public void AudioLongClick(AudioModel audioModel, int i, View view) {
            }

            @Override
            public void AudioClick(AudioModel audioModel, int i, View view) {
                if (i == 1) {
                    if (AppPref.getBgAudioList() != null) {
                        AppPref.getBgAudioList().clear();
                    }
                    ArrayList arrayList = new ArrayList();
                    for (int i2 = 0; i2 < SongsListActivity.this.adapter.getFilterList().size(); i2++) {
                        AudioModel audioModel2 = SongsListActivity.this.adapter.getFilterList().get(i2);
                        arrayList.add(new AudioVideoModal(audioModel2.getPath(), audioModel2.getName(), audioModel2.getDuration(), audioModel2.getArtist(), audioModel2.getAlbumName(), i2));
                    }
                    AppPref.setBgAudioList(arrayList);
                    Intent intent = new Intent(SongsListActivity.this, AudioPlayerActivity.class);
                    int indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(audioModel.getPath()));
                    if (indexOf != -1) {
                        intent.putExtra("modal", AppPref.getBgAudioList().get(indexOf));
                    } else {
                        intent.putExtra("modal", AppPref.getBgAudioList().get(0));
                    }
                    SongsListActivity.this.activityLauncher.launch(intent);
                    return;
                }
                SongsListActivity.this.OpenBottomSheet(audioModel);
            }
        });
        this.binding.recycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        this.binding.recycle.setAdapter(this.adapter);
    }

    
    public void OpenBottomSheet(final AudioModel audioModel) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetFolderMenuBinding bottomsheetFolderMenuBinding = (BottomsheetFolderMenuBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_folder_menu, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetFolderMenuBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetFolderMenuBinding.llBgPlay.setVisibility(View.GONE);
        bottomsheetFolderMenuBinding.llRename.setVisibility(View.GONE);
        bottomsheetFolderMenuBinding.txtTitle.setText(audioModel.getName());
        bottomsheetFolderMenuBinding.llPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int indexOf;
                bottomSheetDialog.dismiss();
                if (AppConstants.isMyServiceRunning(SongsListActivity.this, AudioService.class)) {
                    ArrayList arrayList = new ArrayList();
                    AudioVideoModal playingModel = SongsListActivity.this.audioService.getPlayingModel();
                    if (playingModel != null && (indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(playingModel.getUri()))) != -1) {
                        int i = indexOf + 1;
                        for (int i2 = 0; i2 < i; i2++) {
                            AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(i2);
                            audioVideoModal.setAudioVideoOrder(i2);
                            arrayList.add(audioVideoModal);
                        }
                        arrayList.add(new AudioVideoModal(audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), i + 1));
                        while (indexOf < AppPref.getBgAudioList().size()) {
                            AudioVideoModal audioVideoModal2 = AppPref.getBgAudioList().get(indexOf);
                            audioVideoModal2.setAudioVideoOrder(indexOf);
                            arrayList.add(audioVideoModal2);
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
                if (AppConstants.isMyServiceRunning(SongsListActivity.this, AudioService.class)) {
                    ArrayList<AudioVideoModal> bgAudioList = AppPref.getBgAudioList();
                    AudioVideoModal audioVideoModal = new AudioVideoModal(audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), AppPref.getBgAudioList().size() + 1);
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
                SongsListActivity.this.OpenPlaylistBottomSheet(audioModel);
            }
        });
        bottomsheetFolderMenuBinding.llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                SongsListActivity.this.ShareSong(audioModel);
            }
        });
        bottomsheetFolderMenuBinding.llProperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                SongsListActivity.this.OpenPropertiesBottomSheet(audioModel);
            }
        });
        bottomsheetFolderMenuBinding.llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                SongsListActivity.this.OpenSongsDeleteDialog(audioModel);
            }
        });
    }

    
    public void OpenPlaylistBottomSheet(final AudioModel audioModel) {
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
                FolderModal folderModal = SongsListActivity.this.folderModalList.get(i).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = SongsListActivity.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                AudioVideoModal audioVideoModal = new AudioVideoModal(audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), GetAudioVideoListByFolderID.size() + 1);
                if (!GetAudioVideoListByFolderID.contains(audioVideoModal)) {
                    audioVideoModal.setId(AppConstants.getUniqueId());
                    audioVideoModal.setRefId(folderModal.getId());
                    SongsListActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal);
                }
                Toast.makeText(SongsListActivity.this, "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                SongsListActivity.this.OpenCreatePlaylistDialog(audioModel);
            }
        });
    }

    
    public void OpenCreatePlaylistDialog(final AudioModel audioModel) {
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
                SongsListActivity.this.appDatabase.folderDao().InsertFolder(folderModal);
                SongsListActivity.this.appDatabase.audioVideoDao().InsertAudioVideo(new AudioVideoModal(AppConstants.getUniqueId(), audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), 0, folderModal.getId()));
                SongsListActivity.this.folderModalList.add(SongsListActivity.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                SongsListActivity.this.bottomPlaylistAdapter.notifyDataSetChanged();
                Toast.makeText(SongsListActivity.this, "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    
    public void ShareSong(AudioModel audioModel) {
        Uri uri;
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction("android.intent.action.SEND");
        if (Build.VERSION.SDK_INT > 29) {
            uri = Uri.parse(audioModel.getPath());
        } else {
            uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(audioModel.getPath()));
        }
        intent.putExtra("android.intent.extra.STREAM", uri);
        try {
            startActivity(Intent.createChooser(intent, "Share File "));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    
    public void OpenPropertiesBottomSheet(AudioModel audioModel) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        BottomsheetPropertiesBinding bottomsheetPropertiesBinding = (BottomsheetPropertiesBinding) DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_properties, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetPropertiesBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetPropertiesBinding.llformat.setVisibility(View.GONE);
        bottomsheetPropertiesBinding.llsize.setVisibility(View.GONE);
        bottomsheetPropertiesBinding.txtFileName.setText(audioModel.getName());
        bottomsheetPropertiesBinding.txtAlbumName.setText(audioModel.getAlbumName());
        bottomsheetPropertiesBinding.txtArtistName.setText(audioModel.getArtist());
        if (Build.VERSION.SDK_INT > 29) {
            bottomsheetPropertiesBinding.txtLocation.setText(AppConstants.GetPathFromUri(this, audioModel.getUri()));
        } else {
            bottomsheetPropertiesBinding.txtLocation.setText(audioModel.getUri());
        }
        bottomsheetPropertiesBinding.txtSize.setText(audioModel.getSize());
        bottomsheetPropertiesBinding.txtFormat.setText(audioModel.getType());
        bottomsheetPropertiesBinding.txtLength.setText(AppConstants.formatTime(audioModel.getDuration()));
    }

    
    public void OpenSongsDeleteDialog(final AudioModel audioModel) {
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
        this.deletePos = this.audioModelList.indexOf(audioModel);
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                try {
                    if (Build.VERSION.SDK_INT > 29) {
                        ContentResolver contentResolver = SongsListActivity.this.getContentResolver();
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(Uri.parse(audioModel.getPath()));
                        Collections.addAll(arrayList, new Uri[0]);
                        SongsListActivity.this.deleteLauncher.launch(new IntentSenderRequest.Builder(MediaStore.createDeleteRequest(contentResolver, arrayList).getIntentSender()).setFillInIntent((Intent) null).setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0).build());
                        return;
                    }
                    SongsListActivity.this.getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_data=?", new String[]{new File(audioModel.getPath()).getAbsolutePath()});
                    SongsListActivity.this.deletedAudioList.add(SongsListActivity.this.audioModelList.get(SongsListActivity.this.deletePos));
                    SongsListActivity.this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(audioModel.getPath());
                    SongsListActivity.this.audioModelList.remove(SongsListActivity.this.deletePos);
                    SongsListActivity.this.adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialogDeleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void setImageBitmap() {
        if (this.audioModelList.size() > 0) {
            AudioModel audioModel = this.audioModelList.get(0);
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT > 29) {
                mediaMetadataRetriever.setDataSource(this, Uri.parse(audioModel.getPath()));
            } else {
                mediaMetadataRetriever.setDataSource(this, FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(audioModel.getPath())));
            }
            byte[] embeddedPicture = mediaMetadataRetriever.getEmbeddedPicture();
            if (embeddedPicture != null) {
                Glide.with((FragmentActivity) this).load(BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length)).into(this.binding.albumArt);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return true;
        }
        onBackPressed();
        return true;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putParcelableArrayListExtra("DeletedList", this.deletedAudioList);
        setResult(-1, intent);
        finish();
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
                for (int i = 0; i < this.audioModelList.size(); i++) {
                    AudioModel audioModel = this.audioModelList.get(i);
                    arrayList.add(new AudioVideoModal(audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), i));
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
                    if (SongsListActivity.this.audioService != null && SongsListActivity.this.audioService.isPlayerNotNull()) {
                        int currentPosition = SongsListActivity.this.audioService.getCurrentPosition();
                        SongsListActivity.this.binding.progress.setProgress(currentPosition);
                        SongsListActivity.this.binding.txtRunningTime.setText(AppConstants.formatTime((long) currentPosition));
                    }
                    SongsListActivity.this.myHandler.postDelayed(this, 1000);
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
                        int indexOf = SongsListActivity.this.adapter.getFilterList().indexOf(new AudioModel(AppPref.getBgAudioList().get(intent.getIntExtra("position", 0)).getUri()));
                        if (indexOf != -1) {
                            SongsListActivity.this.adapter.setPlayingPos(indexOf);
                            if (SongsListActivity.this.audioService != null) {
                                SongsListActivity.this.adapter.setAudioIsPlaying(SongsListActivity.this.audioService.isPlaying());
                            }
                            SongsListActivity.this.adapter.setIsPlaying(true);
                        } else {
                            SongsListActivity.this.adapter.setIsPlaying(false);
                        }
                        SongsListActivity.this.getPlayingModel();
                        return;
                    case 1:
                        if (intent.getIntExtra("action", 0) == 1) {
                            SongsListActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_play);
                        } else {
                            SongsListActivity.this.binding.playPauseImg.setImageResource(R.drawable.main_pause);
                        }
                        SongsListActivity.this.setAdapterPlayingSong();
                        return;
                    case 2:
                        SongsListActivity.this.binding.llPlayerBottom.setVisibility(View.GONE);
                        if (AppConstants.isMyServiceRunning(SongsListActivity.this, AudioService.class)) {
                            try {
                                SongsListActivity songsListActivity = SongsListActivity.this;
                                songsListActivity.unbindService(songsListActivity);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        SongsListActivity.this.myHandler.removeCallbacksAndMessages((Object) null);
                        SongsListActivity.this.adapter.setIsPlaying(false);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public void setAdapterPlayingSong() {
        AudioService audioService2 = this.audioService;
        if (audioService2 != null) {
            int indexOf = this.adapter.getFilterList().indexOf(new AudioModel(audioService2.getPlayingModel().getUri()));
            if (indexOf != -1) {
                this.adapter.setPlayingPos(indexOf);
                this.adapter.setAudioIsPlaying(this.audioService.isPlaying());
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

}
