package com.kannada.musicplayer.fragment;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.activities.AudioPlayerActivity;
import com.kannada.musicplayer.activities.MainActivity;
import com.kannada.musicplayer.activities.SongsListActivity;
import com.kannada.musicplayer.adapter.AlbumAdapter;
import com.kannada.musicplayer.adapter.ArtistAdapter;
import com.kannada.musicplayer.adapter.AudioFolderAdapter;
import com.kannada.musicplayer.adapter.BottomPlaylistAdapter;
import com.kannada.musicplayer.adapter.SongsAdapter;
import com.kannada.musicplayer.database.AppDatabase;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.database.model.FolderModal;
import com.kannada.musicplayer.databinding.BottomsheetAddToPlaylistBinding;
import com.kannada.musicplayer.databinding.BottomsheetFolderMenuBinding;
import com.kannada.musicplayer.databinding.BottomsheetMultiVideosPropertiesBinding;
import com.kannada.musicplayer.databinding.BottomsheetPropertiesBinding;
import com.kannada.musicplayer.databinding.DialogDeleteBinding;
import com.kannada.musicplayer.databinding.DialogRenameBinding;
import com.kannada.musicplayer.databinding.FragmentMusicBinding;
import com.kannada.musicplayer.model.AlbumModel;
import com.kannada.musicplayer.model.ArtistModel;
import com.kannada.musicplayer.model.AudioFolderModal;
import com.kannada.musicplayer.model.AudioModel;
import com.kannada.musicplayer.model.CombineFolderModel;
import com.kannada.musicplayer.model.HistoryModel;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MusicFragment extends Fragment implements View.OnClickListener {
    public List<AlbumModel> MultiAlbumList = new ArrayList();
    public List<ArtistModel> MultiArtistList = new ArrayList();
    public List<AudioModel> MultiAudioList = new ArrayList();
    public List<AudioFolderModal> MultiFolderList = new ArrayList();
    
    public final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    public AlbumAdapter albumAdapter;
    AppDatabase appDatabase;
    public ArtistAdapter artistAdapter;
    FragmentMusicBinding binding;
    BottomPlaylistAdapter bottomPlaylistAdapter;
    ActivityResultLauncher<IntentSenderRequest> deleteLauncher;
    AudioModel deleteModel;
    int deletePos;
    int deleteType;
    public CompositeDisposable disposable = new CompositeDisposable();
    public AudioFolderAdapter folderAdapter;
    List<CombineFolderModel> folderModalList = new ArrayList();
    public boolean isMultiSelect = false;
    public SongsAdapter songsAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        FragmentMusicBinding fragmentMusicBinding = (FragmentMusicBinding) DataBindingUtil.inflate(layoutInflater, R.layout.fragment_music, viewGroup, false);
        this.binding = fragmentMusicBinding;
        View root = fragmentMusicBinding.getRoot();
        this.appDatabase = AppDatabase.getAppDatabase(getActivity());
        Clicks();
        setSongsAdapter();
        setFolderAdapter();
        setAlbumAdapter();
        setArtistAdapter();
        this.deleteLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
                if (activityResult.getResultCode() == -1) {
                    if (MusicFragment.this.deleteModel != null) {
                        if (AppConstants.isMyServiceRunning(MusicFragment.this.getActivity(), AudioService.class) && ((MainActivity) MusicFragment.this.getActivity()).getServiceModel().getUri().equals(MusicFragment.this.deleteModel.getUri())) {
                            Intent intent = new Intent();
                            intent.setAction(AppConstants.CLOSE);
                            MusicFragment.this.getActivity().sendBroadcast(intent);
                        }
                        MusicFragment musicFragment = MusicFragment.this;
                        musicFragment.DeleteFromAllList(musicFragment.deleteModel);
                        MusicFragment.this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(MusicFragment.this.deleteModel.getPath());
                    }
                    Toast.makeText(MusicFragment.this.getActivity(), "Delete File Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    }

    private void setSongsAdapter() {
        this.songsAdapter = new SongsAdapter(getActivity(), ((MainActivity) getActivity()).songsList, this.MultiAudioList, new SongsAdapter.OnItemClick() {
            @Override
            public void AudioClick(AudioModel audioModel, int i, View view) {
                AudioVideoModal audioVideoModal;
                if (MusicFragment.this.isMultiSelect) {
                    MusicFragment.this.NotifyForLongClick(true);
                    MusicFragment.this.SetMultiSelectList(audioModel);
                    ((MainActivity) MusicFragment.this.getActivity()).actionMode.setTitle(MusicFragment.this.MultiAudioList.size() + " Selected");
                } else if (i == 1) {
                    ArrayList arrayList = new ArrayList();
                    if (AppPref.getBgAudioList() != null) {
                        AppPref.getBgAudioList().clear();
                    }
                    for (int i2 = 0; i2 < ((MainActivity) MusicFragment.this.getActivity()).songsList.size(); i2++) {
                        AudioModel audioModel2 = ((MainActivity) MusicFragment.this.getActivity()).songsList.get(i2);
                        arrayList.add(new AudioVideoModal(audioModel2.getPath(), audioModel2.getName(), audioModel2.getDuration(), audioModel2.getArtist(), audioModel2.getAlbumName(), i2));
                    }
                    AppPref.setBgAudioList(arrayList);
                    int indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(audioModel.getPath()));
                    Intent intent = new Intent(MusicFragment.this.getActivity(), AudioPlayerActivity.class);
                    if (indexOf != -1) {
                        audioVideoModal = AppPref.getBgAudioList().get(indexOf);
                    } else {
                        audioVideoModal = AppPref.getBgAudioList().get(0);
                    }
                    intent.putExtra("modal", audioVideoModal);
                    MusicFragment.this.activityLauncher.launch(intent);
                } else {
                    MusicFragment.this.openSongsBottomSheet(audioModel);
                }
            }

            @Override
            public void AudioLongClick(AudioModel audioModel, int i, View view) {
                MusicFragment.this.isMultiSelect = true;
                MusicFragment.this.getActivity().startActionMode(((MainActivity) MusicFragment.this.getActivity()).callback);
                MusicFragment.this.NotifyForLongClick(true);
                MusicFragment.this.SetMultiSelectList(audioModel);
                ActionMode actionMode = ((MainActivity) MusicFragment.this.getActivity()).actionMode;
                actionMode.setTitle(MusicFragment.this.MultiAudioList.size() + " Selected");
            }
        });
        this.binding.songRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        this.binding.songRecycle.setAdapter(this.songsAdapter);
    }

    public void DeleteAudios() {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialogTheme);
        DialogDeleteBinding dialogDeleteBinding = (DialogDeleteBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_delete, (ViewGroup) null, false);
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
                try {
                    if (Build.VERSION.SDK_INT > 29) {
                        MusicFragment.this.deleteType = 4;
                        for (int i = 0; i < MusicFragment.this.MultiAudioList.size(); i++) {
                            AudioModel audioModel = MusicFragment.this.MultiAudioList.get(i);
                            MusicFragment.this.deleteModel = audioModel;
                            ContentResolver contentResolver = MusicFragment.this.getActivity().getContentResolver();
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(Uri.parse(audioModel.getPath()));
                            Collections.addAll(arrayList, new Uri[0]);
                            MusicFragment.this.deleteLauncher.launch(new IntentSenderRequest.Builder(MediaStore.createDeleteRequest(contentResolver, arrayList).getIntentSender()).setFillInIntent((Intent) null).setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0).build());
                        }
                        return;
                    }
                    for (int i2 = 0; i2 < MusicFragment.this.MultiAudioList.size(); i2++) {
                        AudioModel audioModel2 = MusicFragment.this.MultiAudioList.get(i2);
                        if (AppConstants.isMyServiceRunning(MusicFragment.this.getActivity(), AudioService.class) && ((MainActivity) MusicFragment.this.getActivity()).getServiceModel().getUri().equals(audioModel2.getUri())) {
                            Intent intent = new Intent();
                            intent.setAction(AppConstants.CLOSE);
                            MusicFragment.this.getActivity().sendBroadcast(intent);
                        }
                        MusicFragment.this.getActivity().getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_data=?", new String[]{new File(audioModel2.getPath()).getAbsolutePath()});
                        MusicFragment.this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(audioModel2.getPath());
                        MusicFragment.this.DeleteFromAllList(audioModel2);
                    }
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

    public void MultiDeleteAudios(int i) {
        Log.d("TAG", "From Dialog MultiDeleteAudios: ");
        final Dialog dialog = new Dialog(getActivity(), R.style.dialogTheme);
        int i2 = 0;
        DialogDeleteBinding dialogDeleteBinding = (DialogDeleteBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_delete, (ViewGroup) null, false);
        dialog.setContentView(dialogDeleteBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        this.deleteType = i;
        final ArrayList arrayList = new ArrayList();
        if (i == 1) {
            while (i2 < this.MultiFolderList.size()) {
                arrayList.addAll(this.MultiFolderList.get(i2).getAudioList());
                i2++;
            }
        } else if (i == 2) {
            while (i2 < this.MultiAlbumList.size()) {
                arrayList.addAll(this.MultiAlbumList.get(i2).getAudioModelList());
                i2++;
            }
        } else if (i == 3) {
            while (i2 < this.MultiArtistList.size()) {
                arrayList.addAll(this.MultiArtistList.get(i2).getAudioModelList());
                i2++;
            }
        }
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                try {
                    if (Build.VERSION.SDK_INT > 29) {
                        for (int i = 0; i < arrayList.size(); i++) {
                            AudioModel audioModel = (AudioModel) arrayList.get(i);
                            MusicFragment.this.deleteModel = audioModel;
                            ContentResolver contentResolver = MusicFragment.this.getActivity().getContentResolver();
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(Uri.parse(audioModel.getPath()));
                            Collections.addAll(arrayList, new Uri[0]);
                            MusicFragment.this.deleteLauncher.launch(new IntentSenderRequest.Builder(MediaStore.createDeleteRequest(contentResolver, arrayList).getIntentSender()).setFillInIntent((Intent) null).setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0).build());
                        }
                        return;
                    }
                    for (int i2 = 0; i2 < arrayList.size(); i2++) {
                        AudioModel audioModel2 = (AudioModel) arrayList.get(i2);
                        if (AppConstants.isMyServiceRunning(MusicFragment.this.getActivity(), AudioService.class) && ((MainActivity) MusicFragment.this.getActivity()).getServiceModel().getUri().equals(audioModel2.getUri())) {
                            Intent intent = new Intent();
                            intent.setAction(AppConstants.CLOSE);
                            MusicFragment.this.getActivity().sendBroadcast(intent);
                        }
                        MusicFragment.this.getActivity().getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_data=?", new String[]{new File(audioModel2.getPath()).getAbsolutePath()});
                        MusicFragment.this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(audioModel2.getPath());
                        MusicFragment.this.DeleteFromAllList(audioModel2);
                    }
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

    public void AudiosProperties(List<AudioModel> list) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        BottomsheetMultiVideosPropertiesBinding bottomsheetMultiVideosPropertiesBinding = (BottomsheetMultiVideosPropertiesBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_multi_videos_properties, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetMultiVideosPropertiesBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        TextView textView = bottomsheetMultiVideosPropertiesBinding.txtCount;
        textView.setText("" + list.size() + " Audios");
        long j = 0;
        for (int i = 0; i < list.size(); i++) {
            j += list.get(i).getLongSize();
            Log.d("TAG", "AudiosProperties: " + list.get(i).getLongSize());
        }
        bottomsheetMultiVideosPropertiesBinding.txtSize.setText(AppConstants.getSize(j));
        ((MainActivity) getActivity()).actionMode.finish();
    }

    public void NotifyForLongClick(boolean z) {
        this.songsAdapter.NotifyLongClick(z);
    }

    public void SetMultiSelectList(AudioModel audioModel) {
        if (this.MultiAudioList.contains(audioModel)) {
            this.songsAdapter.SetSelected(false);
            this.MultiAudioList.remove(audioModel);
        } else {
            this.songsAdapter.SetSelected(true);
            this.MultiAudioList.add(audioModel);
        }
        this.songsAdapter.notifyDataSetChanged();
    }

    public void NotifyForLongClickFolder(boolean z) {
        this.folderAdapter.NotifyLongClick(z);
    }

    public void SetMultiSelectFolderList(AudioFolderModal audioFolderModal) {
        if (this.MultiFolderList.contains(audioFolderModal)) {
            this.folderAdapter.SetSelected(false);
            this.MultiFolderList.remove(audioFolderModal);
        } else {
            this.folderAdapter.SetSelected(true);
            this.MultiFolderList.add(audioFolderModal);
        }
        this.folderAdapter.notifyDataSetChanged();
    }

    public void NotifyForLongClickAlbum(boolean z) {
        this.albumAdapter.NotifyLongClick(z);
    }

    public void SetMultiSelectAlbumList(AlbumModel albumModel) {
        if (this.MultiAlbumList.contains(albumModel)) {
            this.albumAdapter.SetSelected(false);
            this.MultiAlbumList.remove(albumModel);
        } else {
            this.albumAdapter.SetSelected(true);
            this.MultiAlbumList.add(albumModel);
        }
        this.albumAdapter.notifyDataSetChanged();
    }

    public void NotifyForLongClickArtist(boolean z) {
        this.artistAdapter.NotifyLongClick(z);
    }

    public void SetMultiSelectArtistList(ArtistModel artistModel) {
        if (this.MultiArtistList.contains(artistModel)) {
            this.artistAdapter.SetSelected(false);
            this.MultiArtistList.remove(artistModel);
        } else {
            this.artistAdapter.SetSelected(true);
            this.MultiArtistList.add(artistModel);
        }
        this.artistAdapter.notifyDataSetChanged();
    }

    public void openSongsBottomSheet(final AudioModel audioModel) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        BottomsheetFolderMenuBinding bottomsheetFolderMenuBinding = (BottomsheetFolderMenuBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_folder_menu, (ViewGroup) null, false);
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
                if (AppConstants.isMyServiceRunning(MusicFragment.this.getActivity(), AudioService.class)) {
                    ArrayList arrayList = new ArrayList();
                    AudioVideoModal serviceModel = ((MainActivity) MusicFragment.this.getActivity()).getServiceModel();
                    if (serviceModel != null && (indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(serviceModel.getUri()))) != -1) {
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
                if (AppConstants.isMyServiceRunning(MusicFragment.this.getActivity(), AudioService.class)) {
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
                MusicFragment.this.OpenPlaylistBottomSheet(audioModel);
            }
        });
        bottomsheetFolderMenuBinding.llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                MusicFragment.this.ShareSong(audioModel);
            }
        });
        bottomsheetFolderMenuBinding.llProperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                MusicFragment.this.OpenPropertiesBottomSheet(audioModel);
            }
        });
        bottomsheetFolderMenuBinding.llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                MusicFragment.this.OpenSongsDeleteDialog(audioModel);
            }
        });
    }

    public void OpenPlaylistBottomSheet(final AudioModel audioModel) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        BottomsheetAddToPlaylistBinding bottomsheetAddToPlaylistBinding = (BottomsheetAddToPlaylistBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_add_to_playlist, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetAddToPlaylistBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        this.folderModalList.clear();
        this.folderModalList.addAll(this.appDatabase.folderDao().GetCombineFolderList());
        this.bottomPlaylistAdapter = new BottomPlaylistAdapter(getActivity(), this.folderModalList, new BottomPlaylistAdapter.FolderClick() {
            @Override
            public void onFolderClick(int i) {
                bottomSheetDialog.dismiss();
                FolderModal folderModal = MusicFragment.this.folderModalList.get(i).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = MusicFragment.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                AudioVideoModal audioVideoModal = new AudioVideoModal(audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), GetAudioVideoListByFolderID.size() + 1);
                if (!GetAudioVideoListByFolderID.contains(audioVideoModal)) {
                    audioVideoModal.setId(AppConstants.getUniqueId());
                    audioVideoModal.setRefId(folderModal.getId());
                    MusicFragment.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal);
                }
                Toast.makeText(MusicFragment.this.getActivity(), "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                MusicFragment.this.OpenCreatePlaylistDialog(audioModel);
            }
        });
    }

    public void OpenCreatePlaylistDialog(final AudioModel audioModel) {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialogTheme);
        final DialogRenameBinding dialogRenameBinding = (DialogRenameBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_rename, (ViewGroup) null, false);
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
                MusicFragment.this.appDatabase.folderDao().InsertFolder(folderModal);
                MusicFragment.this.appDatabase.audioVideoDao().InsertAudioVideo(new AudioVideoModal(AppConstants.getUniqueId(), audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), 0, folderModal.getId()));
                Toast.makeText(MusicFragment.this.getActivity(), "Music Added to playlist", Toast.LENGTH_SHORT).show();
                MusicFragment.this.folderModalList.add(MusicFragment.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                MusicFragment.this.bottomPlaylistAdapter.notifyDataSetChanged();
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
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction("android.intent.action.SEND");
        if (Build.VERSION.SDK_INT > 29) {
            uri = Uri.parse(audioModel.getPath());
        } else {
            uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", new File(audioModel.getPath()));
        }
        intent.putExtra("android.intent.extra.STREAM", uri);
        try {
            startActivity(Intent.createChooser(intent, "Share File "));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void OpenPropertiesBottomSheet(AudioModel audioModel) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        BottomsheetPropertiesBinding bottomsheetPropertiesBinding = (BottomsheetPropertiesBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_properties, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetPropertiesBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetPropertiesBinding.txtFileName.setText(audioModel.getName());
        bottomsheetPropertiesBinding.txtAlbumName.setText(audioModel.getAlbumName());
        bottomsheetPropertiesBinding.txtArtistName.setText(audioModel.getArtist());
        if (Build.VERSION.SDK_INT > 29) {
            bottomsheetPropertiesBinding.txtLocation.setText(AppConstants.GetPathFromUri(getActivity(), audioModel.getUri()));
        } else {
            bottomsheetPropertiesBinding.txtLocation.setText(audioModel.getUri());
        }
        bottomsheetPropertiesBinding.txtSize.setText(audioModel.getSize());
        bottomsheetPropertiesBinding.txtFormat.setText(audioModel.getType());
        bottomsheetPropertiesBinding.txtLength.setText(AppConstants.formatTime(audioModel.getDuration()));
    }

    
    public void OpenSongsDeleteDialog(final AudioModel audioModel) {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialogTheme);
        DialogDeleteBinding dialogDeleteBinding = (DialogDeleteBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_delete, (ViewGroup) null, false);
        dialog.setContentView(dialogDeleteBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        this.deleteModel = audioModel;
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                try {
                    if (Build.VERSION.SDK_INT > 29) {
                        MusicFragment.this.deleteType = 4;
                        ContentResolver contentResolver = MusicFragment.this.getActivity().getContentResolver();
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(Uri.parse(audioModel.getPath()));
                        Collections.addAll(arrayList, new Uri[0]);
                        MusicFragment.this.deleteLauncher.launch(new IntentSenderRequest.Builder(MediaStore.createDeleteRequest(contentResolver, arrayList).getIntentSender()).setFillInIntent((Intent) null).setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0).build());
                        return;
                    }
                    if (AppConstants.isMyServiceRunning(MusicFragment.this.getActivity(), AudioService.class) && ((MainActivity) MusicFragment.this.getActivity()).getServiceModel().getUri().equals(audioModel.getUri())) {
                        Intent intent = new Intent();
                        intent.setAction(AppConstants.CLOSE);
                        MusicFragment.this.getActivity().sendBroadcast(intent);
                    }
                    File file = new File(audioModel.getPath());
                    String[] strArr = {file.getAbsolutePath()};
                    ContentResolver contentResolver2 = MusicFragment.this.getActivity().getContentResolver();
                    Uri contentUri = MediaStore.Files.getContentUri("external");
                    contentResolver2.delete(contentUri, "_data=?", strArr);
                    if (file.exists()) {
                        contentResolver2.delete(contentUri, "_data=?", strArr);
                    } else {
                        contentResolver2.delete(contentUri, "_data=?", strArr);
                    }
                    MusicFragment.this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(audioModel.getPath());
                    MusicFragment.this.DeleteFromAllList(audioModel);
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

    private void setFolderAdapter() {
        this.folderAdapter = new AudioFolderAdapter(getActivity(), ((MainActivity) getActivity()).folderList, this.MultiFolderList, new AudioFolderAdapter.OnFolderClick() {
            @Override
            public void OnFolderClick(int i, int i2, View view) {
                if (MusicFragment.this.isMultiSelect) {
                    MusicFragment.this.NotifyForLongClickFolder(true);
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.SetMultiSelectFolderList(musicFragment.folderAdapter.getFilterList().get(i));
                    ActionMode actionMode = ((MainActivity) MusicFragment.this.getActivity()).actionMode;
                    actionMode.setTitle(MusicFragment.this.MultiFolderList.size() + " Selected");
                } else if (i2 == 1) {
                    Intent intent = new Intent(MusicFragment.this.getActivity(), SongsListActivity.class);
                    intent.putExtra("FolderModal", MusicFragment.this.folderAdapter.getFilterList().get(i));
                    intent.putExtra("audioListType", 1);
                    MusicFragment.this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult activityResult) {
                            if (activityResult.getData() != null) {
                                ArrayList parcelableArrayListExtra = activityResult.getData().getParcelableArrayListExtra("DeletedList");
                                for (int i = 0; i < parcelableArrayListExtra.size(); i++) {
                                    MusicFragment.this.DeleteFromAllList((AudioModel) parcelableArrayListExtra.get(i));
                                }
                            }
                        }
                    });

                } else {
                    MusicFragment.this.openFolderBottomSheet(i, 1);
                }
            }

            @Override
            public void OnFolderLongClick(int i, int i2, View view) {
                MusicFragment.this.isMultiSelect = true;
                MusicFragment.this.getActivity().startActionMode(((MainActivity) MusicFragment.this.getActivity()).callback);
                MusicFragment.this.NotifyForLongClickFolder(true);
                MusicFragment musicFragment = MusicFragment.this;
                musicFragment.SetMultiSelectFolderList(musicFragment.folderAdapter.getFilterList().get(i));
                ActionMode actionMode = ((MainActivity) MusicFragment.this.getActivity()).actionMode;
                actionMode.setTitle(MusicFragment.this.MultiFolderList.size() + " Selected");
            }
        });
        this.binding.folderRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        this.binding.folderRecycle.setAdapter(this.folderAdapter);
    }

    
    public void DeleteFromAllList(AudioModel audioModel) {
        this.disposable.add(Observable.fromCallable(new MusicFragmentObservable1(this, audioModel)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new MusicFragmentObservable2(this)));
    }

    public Boolean MusicFragmentObservable1call(AudioModel audioModel) throws Exception {
        if (((MainActivity) getActivity()).songsList.contains(audioModel)) {
            ((MainActivity) getActivity()).songsList.remove(audioModel);
        }
        for (int i = 0; i < ((MainActivity) getActivity()).folderList.size(); i++) {
            AudioFolderModal audioFolderModal = ((MainActivity) getActivity()).folderList.get(i);
            List<AudioModel> audioList = audioFolderModal.getAudioList();
            if (audioList.contains(audioModel)) {
                audioList.remove(audioModel);
                audioFolderModal.setAudioList(audioList);
                audioFolderModal.setSongCount(audioList.size());
                if (audioList.size() == 0) {
                    ((MainActivity) getActivity()).folderList.remove(i);
                }
            }
        }
        for (int i2 = 0; i2 < ((MainActivity) getActivity()).artistList.size(); i2++) {
            ArtistModel artistModel = ((MainActivity) getActivity()).artistList.get(i2);
            List<AudioModel> audioModelList = artistModel.getAudioModelList();
            if (audioModelList.contains(audioModel)) {
                audioModelList.remove(audioModel);
                artistModel.setAudioModelList(audioModelList);
                artistModel.setCount(audioModelList.size());
                if (audioModelList.size() == 0) {
                    ((MainActivity) getActivity()).artistList.remove(i2);
                }
            }
        }
        for (int i3 = 0; i3 < ((MainActivity) getActivity()).albumList.size(); i3++) {
            AlbumModel albumModel = ((MainActivity) getActivity()).albumList.get(i3);
            List<AudioModel> audioModelList2 = albumModel.getAudioModelList();
            if (audioModelList2.contains(audioModel)) {
                audioModelList2.remove(audioModel);
                albumModel.setAudioModelList(audioModelList2);
                albumModel.setAudioCount(audioModelList2.size());
                if (audioModelList2.size() == 0) {
                    ((MainActivity) getActivity()).albumList.remove(i3);
                }
            }
        }
        if (AppPref.getFavouriteList() != null && AppPref.getFavouriteList().contains(new AudioVideoModal(audioModel.getPath()))) {
            ArrayList<AudioVideoModal> favouriteList = AppPref.getFavouriteList();
            favouriteList.remove(new AudioVideoModal(audioModel.getPath()));
            AppPref.getFavouriteList().clear();
            AppPref.setFavouriteList(favouriteList);
        }
        if (AppPref.getRecentList() != null) {
            AudioVideoModal audioVideoModal = new AudioVideoModal(audioModel.getPath());
            if (AppPref.getRecentList().contains(new HistoryModel(audioVideoModal))) {
                ArrayList<HistoryModel> recentList = AppPref.getRecentList();
                recentList.remove(new HistoryModel(audioVideoModal));
                AppPref.getRecentList().clear();
                AppPref.setRecentList(recentList);
            }
        }
        return false;
    }

    public void MusicFragmentObservable2call(Boolean bool) throws Exception {
        this.songsAdapter.setSongsList(((MainActivity) getActivity()).songsList);
        this.folderAdapter.setFolderModelList(((MainActivity) getActivity()).folderList);
        this.artistAdapter.setArtistList(((MainActivity) getActivity()).artistList);
        this.albumAdapter.setAlbumList(((MainActivity) getActivity()).albumList);
    }

    private void setAlbumAdapter() {
        this.albumAdapter = new AlbumAdapter(getActivity(), ((MainActivity) getActivity()).albumList, this.MultiAlbumList, new AlbumAdapter.OnFolderClick() {
            @Override
            public void OnClick(int i, int i2) {
                if (MusicFragment.this.isMultiSelect) {
                    MusicFragment.this.NotifyForLongClickAlbum(true);
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.SetMultiSelectAlbumList(musicFragment.albumAdapter.getFilterList().get(i));
                    ActionMode actionMode = ((MainActivity) MusicFragment.this.getActivity()).actionMode;
                    actionMode.setTitle(MusicFragment.this.MultiAlbumList.size() + " Selected");
                } else if (i2 == 1) {
                    Intent intent = new Intent(MusicFragment.this.getActivity(), SongsListActivity.class);
                    intent.putExtra("AlbumModel", MusicFragment.this.albumAdapter.getFilterList().get(i));
                    intent.putExtra("audioListType", 2);


                    MusicFragment.this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult activityResult) {
                            if (activityResult.getData() != null) {
                                ArrayList parcelableArrayListExtra = activityResult.getData().getParcelableArrayListExtra("DeletedList");
                                for (int i = 0; i < parcelableArrayListExtra.size(); i++) {
                                    MusicFragment.this.DeleteFromAllList((AudioModel) parcelableArrayListExtra.get(i));
                                }
                            }
                        }
                    });

                } else {
                    MusicFragment.this.openFolderBottomSheet(i, 2);
                }
            }

            @Override
            public void OnLongClick(int i, int i2) {
                MusicFragment.this.isMultiSelect = true;
                MusicFragment.this.getActivity().startActionMode(((MainActivity) MusicFragment.this.getActivity()).callback);
                MusicFragment.this.NotifyForLongClickAlbum(true);
                MusicFragment musicFragment = MusicFragment.this;
                musicFragment.SetMultiSelectAlbumList(musicFragment.albumAdapter.getFilterList().get(i));
                ActionMode actionMode = ((MainActivity) MusicFragment.this.getActivity()).actionMode;
                actionMode.setTitle(MusicFragment.this.MultiAlbumList.size() + " Selected");
            }
        });
        this.binding.albumRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        this.binding.albumRecycle.setAdapter(this.albumAdapter);
    }

    private void setArtistAdapter() {
        this.artistAdapter = new ArtistAdapter(getActivity(), ((MainActivity) getActivity()).artistList, this.MultiArtistList, new ArtistAdapter.OnFolderClick() {
            @Override
            public void OnFolderClick(int i, int i2) {
                if (MusicFragment.this.isMultiSelect) {
                    MusicFragment.this.NotifyForLongClickArtist(true);
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.SetMultiSelectArtistList(musicFragment.artistAdapter.getFilterList().get(i));
                    ActionMode actionMode = ((MainActivity) MusicFragment.this.getActivity()).actionMode;
                    actionMode.setTitle(MusicFragment.this.MultiArtistList.size() + " Selected");
                } else if (i2 == 1) {
                    Intent intent = new Intent(MusicFragment.this.getActivity(), SongsListActivity.class);
                    intent.putExtra("ArtistModel", MusicFragment.this.artistAdapter.getFilterList().get(i));
                    intent.putExtra("audioListType", 3);
                    MusicFragment.this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult activityResult) {
                            if (activityResult.getData() != null) {
                                ArrayList parcelableArrayListExtra = activityResult.getData().getParcelableArrayListExtra("DeletedList");
                                for (int i = 0; i < parcelableArrayListExtra.size(); i++) {
                                    MusicFragment.this.DeleteFromAllList((AudioModel) parcelableArrayListExtra.get(i));
                                }
                            }
                        }
                    });

                } else {
                    MusicFragment.this.openFolderBottomSheet(i, 3);
                }
            }

            @Override
            public void OnFolderLongClick(int i, int i2) {
                MusicFragment.this.isMultiSelect = true;
                MusicFragment.this.getActivity().startActionMode(((MainActivity) MusicFragment.this.getActivity()).callback);
                MusicFragment.this.NotifyForLongClickArtist(true);
                MusicFragment musicFragment = MusicFragment.this;
                musicFragment.SetMultiSelectArtistList(musicFragment.artistAdapter.getFilterList().get(i));
                ActionMode actionMode = ((MainActivity) MusicFragment.this.getActivity()).actionMode;
                actionMode.setTitle(MusicFragment.this.MultiArtistList.size() + " Selected");
            }
        });
        this.binding.artistRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        this.binding.artistRecycle.setAdapter(this.artistAdapter);
    }

    
    public void openFolderBottomSheet(final int i, final int i2) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        BottomsheetFolderMenuBinding bottomsheetFolderMenuBinding = (BottomsheetFolderMenuBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_folder_menu, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetFolderMenuBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetFolderMenuBinding.llRename.setVisibility(View.GONE);
        bottomsheetFolderMenuBinding.llProperties.setVisibility(View.GONE);
        if (i2 == 1) {
            bottomsheetFolderMenuBinding.txtTitle.setText(this.folderAdapter.getFilterList().get(i).getBucketName());
        } else if (i2 == 2) {
            bottomsheetFolderMenuBinding.txtTitle.setText(this.albumAdapter.getFilterList().get(i).getAlbum());
        } else if (i2 == 3) {
            bottomsheetFolderMenuBinding.txtTitle.setText(this.artistAdapter.getFilterList().get(i).getArtist());
        }
        if (Build.VERSION.SDK_INT > 29) {
            bottomsheetFolderMenuBinding.llDelete.setVisibility(View.GONE);
        } else {
            bottomsheetFolderMenuBinding.llDelete.setVisibility(View.VISIBLE);
        }
        bottomsheetFolderMenuBinding.llBgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                int i = i2;
                if (i == 1) {
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.AddBackgroundMusicList(musicFragment.folderAdapter.getFilterList().get(i).getAudioList());
                } else if (i == 2) {
                    MusicFragment musicFragment2 = MusicFragment.this;
                    musicFragment2.AddBackgroundMusicList(musicFragment2.albumAdapter.getFilterList().get(i).getAudioModelList());
                } else if (i == 3) {
                    MusicFragment musicFragment3 = MusicFragment.this;
                    musicFragment3.AddBackgroundMusicList(musicFragment3.artistAdapter.getFilterList().get(i).getAudioModelList());
                }
            }
        });
        bottomsheetFolderMenuBinding.llPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                int i = i2;
                if (i == 1) {
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.AddToPlayNext(musicFragment.folderAdapter.getFilterList().get(i).getAudioList());
                } else if (i == 2) {
                    MusicFragment musicFragment2 = MusicFragment.this;
                    musicFragment2.AddToPlayNext(musicFragment2.albumAdapter.getFilterList().get(i).getAudioModelList());
                } else if (i == 3) {
                    MusicFragment musicFragment3 = MusicFragment.this;
                    musicFragment3.AddToPlayNext(musicFragment3.artistAdapter.getFilterList().get(i).getAudioModelList());
                }
            }
        });
        bottomsheetFolderMenuBinding.llAddToQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                int i = i2;
                if (i == 1) {
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.AddAudioInQueue(musicFragment.folderAdapter.getFilterList().get(i).getAudioList());
                } else if (i == 2) {
                    MusicFragment musicFragment2 = MusicFragment.this;
                    musicFragment2.AddAudioInQueue(musicFragment2.albumAdapter.getFilterList().get(i).getAudioModelList());
                } else if (i == 3) {
                    MusicFragment musicFragment3 = MusicFragment.this;
                    musicFragment3.AddAudioInQueue(musicFragment3.artistAdapter.getFilterList().get(i).getAudioModelList());
                }
            }
        });
        bottomsheetFolderMenuBinding.llPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                int i = i2;
                if (i == 1) {
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.AddAudiosInPlayList(musicFragment.folderAdapter.getFilterList().get(i).getAudioList());
                } else if (i == 2) {
                    MusicFragment musicFragment2 = MusicFragment.this;
                    musicFragment2.AddAudiosInPlayList(musicFragment2.albumAdapter.getFilterList().get(i).getAudioModelList());
                } else if (i == 3) {
                    MusicFragment musicFragment3 = MusicFragment.this;
                    musicFragment3.AddAudiosInPlayList(musicFragment3.artistAdapter.getFilterList().get(i).getAudioModelList());
                }
            }
        });
        bottomsheetFolderMenuBinding.llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                int i = i2;
                if (i == 1) {
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.ShareAudios(musicFragment.folderAdapter.getFilterList().get(i).getAudioList());
                } else if (i == 2) {
                    MusicFragment musicFragment2 = MusicFragment.this;
                    musicFragment2.ShareAudios(musicFragment2.albumAdapter.getFilterList().get(i).getAudioModelList());
                } else if (i == 3) {
                    MusicFragment musicFragment3 = MusicFragment.this;
                    musicFragment3.ShareAudios(musicFragment3.artistAdapter.getFilterList().get(i).getAudioModelList());
                }
            }
        });
        bottomsheetFolderMenuBinding.llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                int i = i2;
                if (i == 1) {
                    MusicFragment musicFragment = MusicFragment.this;
                    musicFragment.OpenDeleteAudiosDialog(musicFragment.folderAdapter.getFilterList().get(i).getAudioList(), i, 1);
                } else if (i == 2) {
                    MusicFragment musicFragment2 = MusicFragment.this;
                    musicFragment2.OpenDeleteAudiosDialog(musicFragment2.albumAdapter.getFilterList().get(i).getAudioModelList(), i, 2);
                } else if (i == 3) {
                    MusicFragment musicFragment3 = MusicFragment.this;
                    musicFragment3.OpenDeleteAudiosDialog(musicFragment3.artistAdapter.getFilterList().get(i).getAudioModelList(), i, 3);
                }
            }
        });
    }

    
    public void AddBackgroundMusicList(List<AudioModel> list) {
        if (AppPref.getBgAudioList() != null) {
            AppPref.getBgAudioList().clear();
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            AudioModel audioModel = list.get(i);
            arrayList.add(new AudioVideoModal(audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), i));
        }
        AppPref.setBgAudioList(arrayList);
        if (AppPref.getBgAudioList().size() > 0) {
            Intent intent = new Intent(getActivity(), AudioPlayerActivity.class);
            intent.putExtra("modal", AppPref.getBgAudioList().get(0));
            this.activityLauncher.launch(intent);
        }
    }

    
    public void AddToPlayNext(List<AudioModel> list) {
        int indexOf;
        if (AppConstants.isMyServiceRunning(getActivity(), AudioService.class)) {
            ArrayList arrayList = new ArrayList();
            AudioVideoModal serviceModel = ((MainActivity) getActivity()).getServiceModel();
            if (serviceModel != null && (indexOf = AppPref.getBgAudioList().indexOf(new AudioVideoModal(serviceModel.getUri()))) != -1) {
                int i = indexOf + 1;
                int i2 = 0;
                for (int i3 = 0; i3 < i; i3++) {
                    AudioVideoModal audioVideoModal = AppPref.getBgAudioList().get(i3);
                    audioVideoModal.setAudioVideoOrder(i3);
                    arrayList.add(audioVideoModal);
                }
                while (true) {
                    int i4 = i;
                    if (i2 >= list.size()) {
                        break;
                    }
                    AudioModel audioModel = list.get(i2);
                    String path = audioModel.getPath();
                    String name = audioModel.getName();
                    long duration = audioModel.getDuration();
                    String artist = audioModel.getArtist();
                    String albumName = audioModel.getAlbumName();
                    i = i4 + 1;
                    arrayList.add(new AudioVideoModal(path, name, duration, artist, albumName, i4));
                    i2++;
                }
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

    
    public void AddAudioInQueue(List<AudioModel> list) {
        if (AppConstants.isMyServiceRunning(getActivity(), AudioService.class)) {
            ArrayList<AudioVideoModal> bgAudioList = AppPref.getBgAudioList();
            for (int i = 0; i < list.size(); i++) {
                AudioModel audioModel = list.get(i);
                AudioVideoModal audioVideoModal = new AudioVideoModal(audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), AppPref.getBgAudioList().size() + 1);
                if (!AppPref.getBgAudioList().contains(audioVideoModal)) {
                    bgAudioList.add(audioVideoModal);
                }
            }
            AppPref.getBgAudioList().clear();
            AppPref.setBgAudioList(bgAudioList);
        }
    }

    public void AddAudiosInPlayList(final List<AudioModel> list) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        BottomsheetAddToPlaylistBinding bottomsheetAddToPlaylistBinding = (BottomsheetAddToPlaylistBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_add_to_playlist, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetAddToPlaylistBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        this.folderModalList.clear();
        this.folderModalList.addAll(this.appDatabase.folderDao().GetCombineFolderList());
        this.bottomPlaylistAdapter = new BottomPlaylistAdapter(getActivity(), this.folderModalList, new BottomPlaylistAdapter.FolderClick() {
            @Override
            public void onFolderClick(int i) {
                bottomSheetDialog.dismiss();
                FolderModal folderModal = MusicFragment.this.folderModalList.get(i).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = MusicFragment.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                int size = GetAudioVideoListByFolderID.size() + 1;
                for (int i2 = 0; i2 < list.size(); i2++) {
                    AudioModel audioModel = (AudioModel) list.get(i2);
                    AudioVideoModal audioVideoModal = new AudioVideoModal(audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), size);
                    if (!GetAudioVideoListByFolderID.contains(audioVideoModal)) {
                        audioVideoModal.setId(AppConstants.getUniqueId());
                        audioVideoModal.setRefId(folderModal.getId());
                        MusicFragment.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal);
                        size++;
                    }
                }
                Toast.makeText(MusicFragment.this.getActivity(), "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                MusicFragment.this.PlaylistDialog(list);
            }
        });
    }

    
    public void PlaylistDialog(final List<AudioModel> list) {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialogTheme);
        final DialogRenameBinding dialogRenameBinding = (DialogRenameBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_rename, (ViewGroup) null, false);
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
                MusicFragment.this.appDatabase.folderDao().InsertFolder(folderModal);
                for (int i = 0; i < list.size(); i++) {
                    AudioModel audioModel = (AudioModel) list.get(i);
                    MusicFragment.this.appDatabase.audioVideoDao().InsertAudioVideo(new AudioVideoModal(AppConstants.getUniqueId(), audioModel.getPath(), audioModel.getName(), audioModel.getDuration(), audioModel.getArtist(), audioModel.getAlbumName(), i, folderModal.getId()));
                }
                MusicFragment.this.folderModalList.add(MusicFragment.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                MusicFragment.this.bottomPlaylistAdapter.notifyDataSetChanged();
                Toast.makeText(MusicFragment.this.getActivity(), "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    
    public void OpenDeleteAudiosDialog(List<AudioModel> list, int i, int i2) {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialogTheme);
        DialogDeleteBinding dialogDeleteBinding = (DialogDeleteBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_delete, (ViewGroup) null, false);
        dialog.setContentView(dialogDeleteBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        final Dialog dialog2 = dialog;
        final int i3 = i2;
        final List<AudioModel> list2 = list;
        final int i4 = i;
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();
                try {
                    if (Build.VERSION.SDK_INT > 29) {
                        MusicFragment.this.deleteType = i3;
                        for (int i = 0; i < list2.size(); i++) {
                            AudioModel audioModel = (AudioModel) list2.get(i);
                            MusicFragment musicFragment = MusicFragment.this;
                            musicFragment.deletePos = ((MainActivity) musicFragment.getActivity()).songsList.indexOf(audioModel);
                            MusicFragment.this.deleteModel = audioModel;
                            ContentResolver contentResolver = MusicFragment.this.getActivity().getContentResolver();
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(Uri.parse(audioModel.getPath()));
                            Collections.addAll(arrayList, new Uri[0]);
                            MusicFragment.this.deleteLauncher.launch(new IntentSenderRequest.Builder(MediaStore.createDeleteRequest(contentResolver, arrayList).getIntentSender()).setFillInIntent((Intent) null).setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0).build());
                        }
                    } else {
                        for (int i2 = 0; i2 < list2.size(); i2++) {
                            AudioModel audioModel2 = (AudioModel) list2.get(i2);
                            if (AppConstants.isMyServiceRunning(MusicFragment.this.getActivity(), AudioService.class) && ((MainActivity) MusicFragment.this.getActivity()).getServiceModel().getUri().equals(audioModel2.getUri())) {
                                Intent intent = new Intent();
                                intent.setAction(AppConstants.CLOSE);
                                MusicFragment.this.getActivity().sendBroadcast(intent);
                            }
                            MusicFragment.this.getActivity().getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_data=?", new String[]{new File(audioModel2.getPath()).getAbsolutePath()});
                            MusicFragment.this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(audioModel2.getPath());
                            MusicFragment.this.DeleteFromAllList(audioModel2);
                        }
                    }
                    //int i3 = i3;
                    if (i3 == 1) {
                        ((MainActivity) MusicFragment.this.getActivity()).folderList.remove(MusicFragment.this.folderAdapter.getFilterList().get(i4));
                        MusicFragment.this.folderAdapter.setFolderModelList(((MainActivity) MusicFragment.this.getActivity()).folderList);
                    } else if (i3 == 2) {
                        ((MainActivity) MusicFragment.this.getActivity()).albumList.remove(MusicFragment.this.albumAdapter.getFilterList().get(i4));
                        MusicFragment.this.albumAdapter.setAlbumList(((MainActivity) MusicFragment.this.getActivity()).albumList);
                    } else if (i3 == 3) {
                        ((MainActivity) MusicFragment.this.getActivity()).artistList.remove(MusicFragment.this.artistAdapter.getFilterList().get(i4));
                        MusicFragment.this.artistAdapter.setArtistList(((MainActivity) MusicFragment.this.getActivity()).artistList);
                    }
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

    public void ShareAudios(List<AudioModel> list) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            AudioModel audioModel = list.get(i);
            if (Build.VERSION.SDK_INT > 29) {
                arrayList.add(Uri.parse(audioModel.getPath()));
            } else {
                arrayList2.add(FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", new File(audioModel.getPath())));
            }
        }
        Intent intent = new Intent("android.intent.action.SEND_MULTIPLE");
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (Build.VERSION.SDK_INT > 29) {
            intent.putParcelableArrayListExtra("android.intent.extra.STREAM", arrayList);
        } else {
            intent.putParcelableArrayListExtra("android.intent.extra.STREAM", arrayList2);
        }
        try {
            startActivity(Intent.createChooser(intent, "Share File "));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void Clicks() {
        this.binding.llSong.setOnClickListener(this);
        this.binding.llFolder.setOnClickListener(this);
        this.binding.llAlbum.setOnClickListener(this);
        this.binding.llArtist.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        boolean z = true;
        switch (view.getId()) {
            case R.id.llAlbum:
                if (((MainActivity) getActivity()).isExpanded()) {
                    ((MainActivity) getActivity()).setSearchCollapse();
                }
                ChangeSelection(this.binding.albumDivider, this.binding.albumRecycle, this.binding.folderDivider, this.binding.folderRecycle, this.binding.songsDivider, this.binding.songRecycle, this.binding.artistDivider, this.binding.artistRecycle, true);
                if (((MainActivity) getActivity()).orderType == 1) {
                    if (((MainActivity) getActivity()).sortType.equals(AppConstants.NAME_WISE)) {
                        SortAlbumNameAsc();
                        return;
                    }
                    return;
                } else if (((MainActivity) getActivity()).sortType.equals(AppConstants.NAME_WISE)) {
                    SortAlbumNameDesc();
                    return;
                } else {
                    return;
                }
            case R.id.llArtist:
                if (((MainActivity) getActivity()).isExpanded()) {
                    ((MainActivity) getActivity()).setSearchCollapse();
                }
                ChangeSelection(this.binding.artistDivider, this.binding.artistRecycle, this.binding.folderDivider, this.binding.folderRecycle, this.binding.albumDivider, this.binding.albumRecycle, this.binding.songsDivider, this.binding.songRecycle, true);
                if (((MainActivity) getActivity()).orderType == 1) {
                    if (((MainActivity) getActivity()).sortType.equals(AppConstants.NAME_WISE)) {
                        SortArtistNameAsc();
                        return;
                    }
                    return;
                } else if (((MainActivity) getActivity()).sortType.equals(AppConstants.NAME_WISE)) {
                    SortArtistNameDesc();
                    return;
                } else {
                    return;
                }
            case R.id.llFolder:
                if (((MainActivity) getActivity()).isExpanded()) {
                    ((MainActivity) getActivity()).setSearchCollapse();
                }
                ChangeSelection(this.binding.folderDivider, this.binding.folderRecycle, this.binding.songsDivider, this.binding.songRecycle, this.binding.albumDivider, this.binding.albumRecycle, this.binding.artistDivider, this.binding.artistRecycle, true);
                if (((MainActivity) getActivity()).orderType == 1) {
                    if (((MainActivity) getActivity()).sortType.equals(AppConstants.NAME_WISE)) {
                        SortFolderNameAsc();
                        return;
                    }
                    return;
                } else if (((MainActivity) getActivity()).sortType.equals(AppConstants.NAME_WISE)) {
                    SortFolderNameDesc();
                    return;
                } else {
                    return;
                }
            case R.id.llSong:
                if (((MainActivity) getActivity()).isExpanded()) {
                    ((MainActivity) getActivity()).setSearchCollapse();
                }
                ChangeSelection(this.binding.songsDivider, this.binding.songRecycle, this.binding.folderDivider, this.binding.folderRecycle, this.binding.albumDivider, this.binding.albumRecycle, this.binding.artistDivider, this.binding.artistRecycle, true);
                if (((MainActivity) getActivity()).orderType == 1) {
                    char c1 = 0;
                    String str = ((MainActivity) getActivity()).sortType;
                    str.hashCode();
                    switch (str.hashCode()) {
                        case -1727588715:
                            if (str.equals(AppConstants.DATE_WISE)) {
                                z = false;
                                c1 = 1;
                                break;
                            }
                        case -1659068641:
                            c1 = 0;
                            break;
                        case -473329059:
                            if (str.equals(AppConstants.LENGTH_WISE)) {
                                z = true;
                                c1 = 2;
                                break;
                            }
                        case -244855336:
                            if (str.equals(AppConstants.NAME_WISE)) {
                                z = true;
                                c1 = 3;
                                break;
                            }
                        default:
                            z = true;
                            break;
                    }

                    if(c1 == 0){
                        SortSongFileAsc();
                    } else if(c1 == 1){
                        SortSongDateAsc();
                    } else if(c1 == 2){
                        SortSongLengthAsc();
                    } else if(c1 == 3){
                        SortSongNameAsc();
                    }

                    /*switch (z) {
                        case false:
                            SortSongDateAsc();
                            return;
                        case true:
                            SortSongFileAsc();
                            return;
                        case true:
                            SortSongLengthAsc();
                            return;
                        case true:
                            SortSongNameAsc();
                            return;
                        default:
                            return;
                    }*/
                } else {
                    char c1 = 0;
                    String str2 = ((MainActivity) getActivity()).sortType;
                    str2.hashCode();
                    switch (str2.hashCode()) {
                        case -1727588715:
                            if (str2.equals(AppConstants.DATE_WISE)) {
                                z = false;
                                c1 = 1;
                                break;
                            }
                        case -1659068641:
                            c1 = 0;
                            break;
                        case -473329059:
                            if (str2.equals(AppConstants.LENGTH_WISE)) {
                                z = true;
                                c1 = 2;
                                break;
                            }
                        case -244855336:
                            if (str2.equals(AppConstants.NAME_WISE)) {
                                z = true;
                                c1 = 3;
                                break;
                            }
                        default:
                            z = true;
                            break;
                    }

                    if(c1 == 0){
                        SortSongFileDesc();
                    } else if(c1 == 1){
                        SortSongDateDesc();
                    } else if(c1 == 2){
                        SortSongLengthDesc();
                    } else if(c1 == 3){
                        SortSongNameDesc();
                    }

                    /*switch (z) {
                        case false:
                            SortSongDateDesc();
                            return;
                        case true:
                            SortSongFileDesc();
                            return;
                        case true:
                            SortSongLengthDesc();
                            return;
                        case true:
                            SortSongNameDesc();
                            return;
                        default:
                            return;
                    }*/
                }
            default:
                return;
        }
    }

    private void ChangeSelection(View view, RecyclerView recyclerView, View view2, RecyclerView recyclerView2, View view3, RecyclerView recyclerView3, View view4, RecyclerView recyclerView4, boolean z) {
        if (z) {
            view.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        view2.setVisibility(View.INVISIBLE);
        recyclerView2.setVisibility(View.GONE);
        view3.setVisibility(View.INVISIBLE);
        recyclerView3.setVisibility(View.GONE);
        view4.setVisibility(View.INVISIBLE);
        recyclerView4.setVisibility(View.GONE);
        FragmentActivity activity = getActivity();
        Objects.requireNonNull(activity);
        ((MainActivity) activity).setAdapterPlayingSong();
        CheckNoData();
        if (((MainActivity) getActivity()).actionMode != null) {
            ((MainActivity) getActivity()).actionMode.finish();
        }
    }

    public int GetVisibleData() {
        if (this.binding.songRecycle.getVisibility() == View.VISIBLE) {
            return 4;
        }
        if (this.binding.folderRecycle.getVisibility() == android.view.View.VISIBLE) {
            return 1;
        }
        if (this.binding.albumRecycle.getVisibility() == android.view.View.VISIBLE) {
            return 2;
        }
        if (this.binding.artistRecycle.getVisibility() == android.view.View.VISIBLE) {
            return 3;
        }
        return 4;
    }

    public void SortSongNameDesc() {
        Collections.sort(this.songsAdapter.getFilterList(), new Comparator<AudioModel>() {
            @Override
            public int compare(AudioModel audioModel, AudioModel audioModel2) {
                return audioModel2.getName().compareTo(audioModel.getName());
            }
        });
        this.songsAdapter.notifyDataSetChanged();
    }

    public void SortSongNameAsc() {
        Collections.sort(this.songsAdapter.getFilterList(), new Comparator<AudioModel>() {
            @Override
            public int compare(AudioModel audioModel, AudioModel audioModel2) {
                return audioModel.getName().compareTo(audioModel2.getName());
            }
        });
        this.songsAdapter.notifyDataSetChanged();
    }

    public void SortFolderNameDesc() {
        Collections.sort(this.folderAdapter.getFilterList(), new Comparator<AudioFolderModal>() {
            @Override
            public int compare(AudioFolderModal audioFolderModal, AudioFolderModal audioFolderModal2) {
                return audioFolderModal2.getBucketName().compareTo(audioFolderModal.getBucketName());
            }
        });
        this.folderAdapter.notifyDataSetChanged();
    }

    public void SortFolderNameAsc() {
        Collections.sort(this.folderAdapter.getFilterList(), new Comparator<AudioFolderModal>() {
            @Override
            public int compare(AudioFolderModal audioFolderModal, AudioFolderModal audioFolderModal2) {
                return audioFolderModal.getBucketName().compareTo(audioFolderModal2.getBucketName());
            }
        });
        this.folderAdapter.notifyDataSetChanged();
    }

    public void SortAlbumNameDesc() {
        Collections.sort(this.albumAdapter.getFilterList(), new Comparator<AlbumModel>() {
            @Override
            public int compare(AlbumModel albumModel, AlbumModel albumModel2) {
                return albumModel2.getAlbum().compareTo(albumModel.getAlbum());
            }
        });
        this.albumAdapter.notifyDataSetChanged();
    }

    public void SortAlbumNameAsc() {
        Collections.sort(this.albumAdapter.getFilterList(), new Comparator<AlbumModel>() {
            @Override
            public int compare(AlbumModel albumModel, AlbumModel albumModel2) {
                return albumModel.getAlbum().compareTo(albumModel2.getAlbum());
            }
        });
        this.albumAdapter.notifyDataSetChanged();
    }

    public void SortArtistNameDesc() {
        Collections.sort(this.artistAdapter.getFilterList(), new Comparator<ArtistModel>() {
            @Override
            public int compare(ArtistModel artistModel, ArtistModel artistModel2) {
                return artistModel2.getArtist().compareTo(artistModel.getArtist());
            }
        });
        this.artistAdapter.notifyDataSetChanged();
    }

    public void SortArtistNameAsc() {
        Collections.sort(this.artistAdapter.getFilterList(), new Comparator<ArtistModel>() {
            @Override
            public int compare(ArtistModel artistModel, ArtistModel artistModel2) {
                return artistModel.getArtist().compareTo(artistModel2.getArtist());
            }
        });
        this.artistAdapter.notifyDataSetChanged();
    }

    public void SortSongFileDesc() {
        Collections.sort(this.songsAdapter.getFilterList(), new Comparator<AudioModel>() {
            @Override
            public int compare(AudioModel audioModel, AudioModel audioModel2) {
                return Long.compare(audioModel2.getLongSize(), audioModel.getLongSize());
            }
        });
        this.songsAdapter.notifyDataSetChanged();
    }

    public void SortSongFileAsc() {
        Collections.sort(this.songsAdapter.getFilterList(), new Comparator<AudioModel>() {
            @Override
            public int compare(AudioModel audioModel, AudioModel audioModel2) {
                return Long.compare(audioModel.getLongSize(), audioModel2.getLongSize());
            }
        });
        this.songsAdapter.notifyDataSetChanged();
    }

    public void SortSongLengthDesc() {
        Collections.sort(this.songsAdapter.getFilterList(), new Comparator<AudioModel>() {
            @Override
            public int compare(AudioModel audioModel, AudioModel audioModel2) {
                return Long.compare(audioModel2.getDuration(), audioModel.getDuration());
            }
        });
        this.songsAdapter.notifyDataSetChanged();
    }

    public void SortSongLengthAsc() {
        Collections.sort(this.songsAdapter.getFilterList(), new Comparator<AudioModel>() {
            @Override
            public int compare(AudioModel audioModel, AudioModel audioModel2) {
                return Long.compare(audioModel.getDuration(), audioModel2.getDuration());
            }
        });
        this.songsAdapter.notifyDataSetChanged();
    }

    public void SortSongDateDesc() {
        Collections.sort(this.songsAdapter.getFilterList(), new Comparator<AudioModel>() {
            @Override
            public int compare(AudioModel audioModel, AudioModel audioModel2) {
                return Long.compare(audioModel2.getCreationDate(), audioModel.getCreationDate());
            }
        });
        this.songsAdapter.notifyDataSetChanged();
    }

    public void SortSongDateAsc() {
        Collections.sort(this.songsAdapter.getFilterList(), new Comparator<AudioModel>() {
            @Override
            public int compare(AudioModel audioModel, AudioModel audioModel2) {
                return Long.compare(audioModel.getCreationDate(), audioModel2.getCreationDate());
            }
        });
        this.songsAdapter.notifyDataSetChanged();
    }

    public void CheckNoData() {
        int GetVisibleData = GetVisibleData();
        if (GetVisibleData == 4) {
            if (this.songsAdapter.getFilterList().size() > 0) {
                this.binding.rlNoData.setVisibility(View.GONE);
            } else {
                this.binding.rlNoData.setVisibility(View.VISIBLE);
            }
        } else if (GetVisibleData == 1) {
            if (this.folderAdapter.getFilterList().size() > 0) {
                this.binding.rlNoData.setVisibility(View.GONE);
            } else {
                this.binding.rlNoData.setVisibility(View.VISIBLE);
            }
        } else if (GetVisibleData == 2) {
            if (this.albumAdapter.getFilterList().size() > 0) {
                this.binding.rlNoData.setVisibility(View.GONE);
            } else {
                this.binding.rlNoData.setVisibility(View.VISIBLE);
            }
        } else if (GetVisibleData != 3) {
        } else {
            if (this.artistAdapter.getFilterList().size() > 0) {
                this.binding.rlNoData.setVisibility(View.GONE);
            } else {
                this.binding.rlNoData.setVisibility(View.VISIBLE);
            }
        }
    }

    public void ClearMultiSelection() {
        if (((MainActivity) getActivity()).actionMode != null) {
            ((MainActivity) getActivity()).actionMode.finish();
        }
    }
}
