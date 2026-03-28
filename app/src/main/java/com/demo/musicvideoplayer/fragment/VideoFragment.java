package com.demo.musicvideoplayer.fragment;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.activities.MainActivity;
import com.demo.musicvideoplayer.activities.VideoPlayerActivity;
import com.demo.musicvideoplayer.adapter.BottomPlaylistAdapter;
import com.demo.musicvideoplayer.adapter.VideoFolderAdapter;
import com.demo.musicvideoplayer.adapter.VideoGridAdapter;
import com.demo.musicvideoplayer.adapter.VideoListAdapter;
import com.demo.musicvideoplayer.database.AppDatabase;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import com.demo.musicvideoplayer.database.model.FolderModal;
import com.demo.musicvideoplayer.databinding.BottomsheetAddToPlaylistBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetMultiVideosPropertiesBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetPropertiesBinding;
import com.demo.musicvideoplayer.databinding.BottomsheetVideoMenuBinding;
import com.demo.musicvideoplayer.databinding.DialogDeleteBinding;
import com.demo.musicvideoplayer.databinding.DialogRenameBinding;
import com.demo.musicvideoplayer.databinding.FragmentVideoBinding;
import com.demo.musicvideoplayer.model.CombineFolderModel;
import com.demo.musicvideoplayer.model.HistoryModel;
import com.demo.musicvideoplayer.model.VideoFolderModal;
import com.demo.musicvideoplayer.model.VideoModal;
import com.demo.musicvideoplayer.service.AudioService;
import com.demo.musicvideoplayer.utils.AppConstants;
import com.demo.musicvideoplayer.utils.AppPref;
import com.demo.musicvideoplayer.utils.BetterActivityResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VideoFragment extends Fragment {
    public List<VideoModal> MultiVideoList = new ArrayList();
    public List<VideoModal> VideosList = new ArrayList();
    
    public final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    AppDatabase appDatabase;
    FragmentVideoBinding binding;
    BottomPlaylistAdapter bottomPlaylistAdapter;
    int deleteAllPos;
    ActivityResultLauncher<IntentSenderRequest> deleteLauncher;
    VideoModal deleteModel;
    int deleteModelPos;
    int deleteVideoFolderPos;
    String displayName = (System.currentTimeMillis() + ".mp4");
    public VideoFolderAdapter folderAdapter;
    public List<CombineFolderModel> folderModalList = new ArrayList();
    public boolean isMultiSelect = false;
    String oldPath = null;
    int renameAllPos;
    int renameFavPos = -1;
    int renameFolderPos;
    ActivityResultLauncher<IntentSenderRequest> renameLauncher;
    VideoModal renameModel;
    int renameModelPos;
    int renamePos;
    int renameRecent = -1;
    Uri renameUri;
    int renameVideoFolderPos;
    public VideoFolderModal videoFolderModal;
    public VideoGridAdapter videoGridAdapter;
    public VideoListAdapter videoListAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        FragmentVideoBinding fragmentVideoBinding = (FragmentVideoBinding) DataBindingUtil.inflate(layoutInflater, R.layout.fragment_video, viewGroup, false);
        this.binding = fragmentVideoBinding;
        View root = fragmentVideoBinding.getRoot();
        this.appDatabase = AppDatabase.getAppDatabase(getActivity());
        setFolderAdapter();
        setVideoListAdapter();
        setVideoGridAdapter();
        this.deleteLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
                if (activityResult.getResultCode() == -1) {
                    if (VideoFragment.this.deleteModel != null) {
                        VideoFragment videoFragment = VideoFragment.this;
                        videoFragment.DeleteVideoFromList(videoFragment.deleteModel);
                    }
                    VideoFragment.this.CheckNoData();
                    VideoFragment.this.RemoveFolder();
                    VideoFragment videoFragment2 = VideoFragment.this;
                    videoFragment2.RemoveFromAllList(videoFragment2.deleteModel);
                    Toast.makeText(VideoFragment.this.getActivity(), "Delete File Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
        this.renameLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult activityResult) {
                if (activityResult.getResultCode() == -1) {
                    ContentResolver contentResolver = VideoFragment.this.getActivity().getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("is_pending", 1);
                    contentResolver.update(VideoFragment.this.renameUri, contentValues, (String) null, (String[]) null);
                    Log.d("TAG", "OpenRenameDialog: oldUri launcher1" + VideoFragment.this.renameUri);
                    contentValues.clear();
                    contentValues.put("_display_name", VideoFragment.this.displayName);
                    contentValues.put("is_pending", 0);
                    contentResolver.update(VideoFragment.this.renameUri, contentValues, (String) null, (String[]) null);
                    Log.d("TAG", "OpenRenameDialog: oldUri launcher2" + VideoFragment.this.renameUri);
                    VideoFragment.this.renameModel.setaPath(String.valueOf(VideoFragment.this.renameUri));
                    VideoFragment.this.VideosList.set(VideoFragment.this.renamePos, VideoFragment.this.renameModel);
                    ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.set(VideoFragment.this.renameFolderPos, VideoFragment.this.videoFolderModal);
                    int indexOf = VideoFragment.this.videoListAdapter.getFilterList().indexOf(VideoFragment.this.renameModel);
                    VideoFragment.this.videoListAdapter.getFilterList().set(indexOf, VideoFragment.this.renameModel);
                    VideoFragment.this.videoGridAdapter.getFilterList().set(indexOf, VideoFragment.this.renameModel);
                    VideoFragment.this.folderAdapter.notifyItemChanged(VideoFragment.this.renameFolderPos);
                    VideoFragment.this.videoListAdapter.notifyDataSetChanged();
                    VideoFragment.this.videoGridAdapter.notifyDataSetChanged();
                    if (VideoFragment.this.videoFolderModal.getaName().equals("All")) {
                        if (!(VideoFragment.this.renameVideoFolderPos == -1 || VideoFragment.this.renameModelPos == -1)) {
                            ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.get(VideoFragment.this.renameVideoFolderPos).getVideoList().set(VideoFragment.this.renameModelPos, VideoFragment.this.renameModel);
                        }
                    } else if (VideoFragment.this.renameAllPos != -1) {
                        ((MainActivity) VideoFragment.this.getActivity()).AllVideosList.set(VideoFragment.this.renameAllPos, VideoFragment.this.renameModel);
                    }
                    if (VideoFragment.this.renameFavPos != -1) {
                        ArrayList<AudioVideoModal> favouriteList = AppPref.getFavouriteList();
                        favouriteList.set(VideoFragment.this.renameFavPos, new AudioVideoModal(VideoFragment.this.renameModel.getaPath(), VideoFragment.this.renameModel.getaName(), VideoFragment.this.renameModel.getDuration(), "", "", AppPref.getFavouriteList().size() + 1));
                        AppPref.setFavouriteList(favouriteList);
                    }
                    VideoFragment.this.appDatabase.audioVideoDao().UpdateUriModel(VideoFragment.this.renameModel.getaPath(), VideoFragment.this.oldPath);
                    VideoFragment.this.appDatabase.audioVideoDao().UpdateNameModel(VideoFragment.this.renameModel.getaName(), VideoFragment.this.renameModel.getaPath());
                    Log.d("TAG", "onActivityResult: " + VideoFragment.this.renameModel.getaPath() + " || " + VideoFragment.this.renameModel.getaName() + " || " + VideoFragment.this.oldPath);
                    if (VideoFragment.this.renameRecent != -1) {
                        ArrayList<HistoryModel> recentList = AppPref.getRecentList();
                        HistoryModel historyModel = recentList.get(VideoFragment.this.renameRecent);
                        AudioVideoModal audioVideoModal = historyModel.getAudioVideoModal();
                        audioVideoModal.setUri(VideoFragment.this.renameModel.getaPath());
                        audioVideoModal.setName(VideoFragment.this.renameModel.getaName());
                        historyModel.setAudioVideoModal(audioVideoModal);
                        recentList.set(VideoFragment.this.renameRecent, historyModel);
                        return;
                    }
                    return;
                }
                Toast.makeText(VideoFragment.this.getActivity(), "Not Granted Permission", Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

    public void DeleteVideoFromList(VideoModal videoModal) {
        int indexOf = this.videoListAdapter.getFilterList().indexOf(videoModal);
        this.videoListAdapter.getFilterList().remove(videoModal);
        this.videoListAdapter.getFilterList().remove(videoModal);
        this.videoGridAdapter.getFilterList().remove(videoModal);
        int indexOf2 = this.videoFolderModal.getVideoList().indexOf(videoModal);
        if (indexOf2 != -1) {
            this.videoFolderModal.getVideoList().remove(indexOf2);
        }
        int indexOf3 = ((MainActivity) getActivity()).videoFolderList.indexOf(this.videoFolderModal);
        ((MainActivity) getActivity()).videoFolderList.set(indexOf3, this.videoFolderModal);
        this.folderAdapter.notifyItemChanged(indexOf3);
        this.videoGridAdapter.notifyItemRemoved(indexOf);
        this.videoListAdapter.notifyItemRemoved(indexOf);
        if (this.videoFolderModal.getaName().equals("All")) {
            if (this.deleteVideoFolderPos != -1 && this.deleteModelPos != -1) {
                ((MainActivity) getActivity()).videoFolderList.get(this.deleteVideoFolderPos).getVideoList().remove(this.deleteModelPos);
            }
        } else if (this.deleteAllPos != -1) {
            ((MainActivity) getActivity()).AllVideosList.remove(this.deleteAllPos);
        }
    }

    public void setFolderAdapter() {
        this.folderAdapter = new VideoFolderAdapter(getActivity(), ((MainActivity) getActivity()).videoFolderList, new VideoFolderAdapter.FolderClick() {
            @Override
            public void OnVideoClick(VideoFolderModal videoFolderModal) {
                VideoFragment.this.VideosList.clear();
                VideoFragment.this.videoFolderModal = videoFolderModal;
                VideoFragment.this.VideosList.addAll(VideoFragment.this.videoFolderModal.getVideoList());
                VideoFragment.this.videoListAdapter.setVideoFolderList(VideoFragment.this.VideosList);
                VideoFragment.this.videoGridAdapter.setVideoFolderList(VideoFragment.this.VideosList);
                VideoFragment.this.ClearMultiSelection();
                VideoFragment.this.CheckNoData();
            }
        });
        this.binding.TitleRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        this.binding.TitleRecycle.setAdapter(this.folderAdapter);
    }

    private void setVideoListAdapter() {
        this.videoListAdapter = new VideoListAdapter(getActivity(), this.VideosList, this.MultiVideoList, new VideoListAdapter.VideoClick() {
            @Override
            public void Click(VideoModal videoModal, int i, View view) {
                if (VideoFragment.this.isMultiSelect) {
                    VideoFragment.this.NotifyForLongClick(true);
                    VideoFragment.this.SetMultiSelectList(videoModal);
                    ActionMode actionMode = ((MainActivity) VideoFragment.this.getActivity()).actionMode;
                    actionMode.setTitle(VideoFragment.this.MultiVideoList.size() + " Selected");
                } else if (i == 1) {
                    if (AppConstants.isMyServiceRunning(VideoFragment.this.getActivity(), AudioService.class)) {
                        Intent intent = new Intent();
                        intent.setAction(AppConstants.CLOSE);
                        VideoFragment.this.getActivity().sendBroadcast(intent);
                    }
                    Intent intent2 = new Intent(VideoFragment.this.getActivity(), VideoPlayerActivity.class);
                    intent2.putExtra("VideoModel", videoModal);
                    intent2.putExtra("FolderModel", VideoFragment.this.videoFolderModal);
                    VideoFragment.this.activityLauncher.launch(intent2);
                } else {
                    VideoFragment.this.openBottomSheet(videoModal);
                }
            }

            @Override
            public void LongClick(VideoModal videoModal, int i, View view) {
                VideoFragment.this.isMultiSelect = true;
                VideoFragment.this.getActivity().startActionMode(((MainActivity) VideoFragment.this.getActivity()).callback);
                VideoFragment.this.NotifyForLongClick(true);
                VideoFragment.this.SetMultiSelectList(videoModal);
                ActionMode actionMode = ((MainActivity) VideoFragment.this.getActivity()).actionMode;
                actionMode.setTitle(VideoFragment.this.MultiVideoList.size() + " Selected");
                Log.d("TAG", "LongClick: " + VideoFragment.this.MultiVideoList.size());
            }
        });
        this.binding.VideoListRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        this.binding.VideoListRecycle.setAdapter(this.videoListAdapter);
    }

    private void setVideoGridAdapter() {
        this.videoGridAdapter = new VideoGridAdapter(getActivity(), this.VideosList, this.MultiVideoList, new VideoGridAdapter.VideoClick() {
            @Override
            public void Click(VideoModal videoModal, int i, View view) {
                if (VideoFragment.this.isMultiSelect) {
                    VideoFragment.this.NotifyForLongClick(true);
                    VideoFragment.this.SetMultiSelectList(videoModal);
                    ActionMode actionMode = ((MainActivity) VideoFragment.this.getActivity()).actionMode;
                    actionMode.setTitle(VideoFragment.this.MultiVideoList.size() + " Selected");
                } else if (i == 1) {
                    if (AppConstants.isMyServiceRunning(VideoFragment.this.getActivity(), AudioService.class)) {
                        Intent intent = new Intent();
                        intent.setAction(AppConstants.CLOSE);
                        VideoFragment.this.getActivity().sendBroadcast(intent);
                    }
                    Intent intent2 = new Intent(VideoFragment.this.getActivity(), VideoPlayerActivity.class);
                    intent2.putExtra("VideoModel", videoModal);
                    intent2.putExtra("FolderModel", VideoFragment.this.videoFolderModal);
                    VideoFragment.this.activityLauncher.launch(intent2);
                } else {
                    VideoFragment.this.openBottomSheet(videoModal);
                }
            }

            @Override
            public void LongClick(VideoModal videoModal, int i, View view) {
                VideoFragment.this.isMultiSelect = true;
                VideoFragment.this.getActivity().startActionMode(((MainActivity) VideoFragment.this.getActivity()).callback);
                VideoFragment.this.NotifyForLongClick(true);
                VideoFragment.this.SetMultiSelectList(videoModal);
                ActionMode actionMode = ((MainActivity) VideoFragment.this.getActivity()).actionMode;
                actionMode.setTitle(VideoFragment.this.MultiVideoList.size() + " Selected");
                Log.d("TAG", "LongClick: " + VideoFragment.this.MultiVideoList.size());
            }
        });
        this.binding.VideoGridRecycle.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        this.binding.VideoGridRecycle.setAdapter(this.videoGridAdapter);
    }

    public void ClearMultiSelection() {
        if (this.isMultiSelect) {
            ((MainActivity) getActivity()).actionMode.finish();
        }
    }

    public void NotifyForLongClick(boolean z) {
        this.videoListAdapter.NotifyLongClick(z);
        this.videoGridAdapter.NotifyLongClick(z);
    }

    public void SetMultiSelectList(VideoModal videoModal) {
        if (this.MultiVideoList.contains(videoModal)) {
            this.videoListAdapter.SetSelected(false);
            this.videoGridAdapter.SetSelected(false);
            this.MultiVideoList.remove(videoModal);
        } else {
            this.videoListAdapter.SetSelected(true);
            this.videoGridAdapter.SetSelected(true);
            this.MultiVideoList.add(videoModal);
        }
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void AddToPlayListVideos() {
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
                FolderModal folderModal = VideoFragment.this.folderModalList.get(i).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = VideoFragment.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                int size = GetAudioVideoListByFolderID.size() + 1;
                int i2 = 0;
                while (i2 < VideoFragment.this.MultiVideoList.size()) {
                    VideoModal videoModal = VideoFragment.this.MultiVideoList.get(i2);
                    int i3 = size + 1;
                    AudioVideoModal audioVideoModal = new AudioVideoModal(videoModal.getaPath(), videoModal.getaName(), videoModal.getDuration(), "", "", size);
                    if (!GetAudioVideoListByFolderID.contains(audioVideoModal)) {
                        audioVideoModal.setId(AppConstants.getUniqueId());
                        audioVideoModal.setRefId(folderModal.getId());
                        VideoFragment.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal);
                    }
                    i2++;
                    size = i3;
                }
                Toast.makeText(VideoFragment.this.getActivity(), "Music Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                VideoFragment.this.OpenCreateMultiPlaylistDialog();
            }
        });
    }

    public void OpenCreateMultiPlaylistDialog() {
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
                VideoFragment.this.appDatabase.folderDao().InsertFolder(folderModal);
                for (int i = 0; i < VideoFragment.this.MultiVideoList.size(); i++) {
                    VideoModal videoModal = VideoFragment.this.MultiVideoList.get(i);
                    VideoFragment.this.appDatabase.audioVideoDao().InsertAudioVideo(new AudioVideoModal(AppConstants.getUniqueId(), videoModal.getaPath(), videoModal.getaName(), videoModal.getDuration(), "", "", i, folderModal.getId()));
                }
                Toast.makeText(VideoFragment.this.getActivity(), "Music Added to playlist", Toast.LENGTH_SHORT).show();
                VideoFragment.this.folderModalList.add(VideoFragment.this.appDatabase.folderDao().GetFolderById(folderModal.getId()));
                VideoFragment.this.bottomPlaylistAdapter.notifyDataSetChanged();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void DeleteVideos() {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialogTheme);
        DialogDeleteBinding dialogDeleteBinding = (DialogDeleteBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_delete, (ViewGroup) null, false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(dialogDeleteBinding.getRoot());
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(-1, -2);
        dialog.show();
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                for (int i = 0; i < VideoFragment.this.MultiVideoList.size(); i++) {
                    VideoModal videoModal = VideoFragment.this.MultiVideoList.get(i);
                    VideoFragment.this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(videoModal.getaPath());
                    try {
                        if (Build.VERSION.SDK_INT > 29) {
                            ContentResolver contentResolver = VideoFragment.this.getActivity().getContentResolver();
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(Uri.parse(videoModal.getaPath()));
                            Collections.addAll(arrayList, new Uri[0]);
                            VideoFragment.this.deleteLauncher.launch(new IntentSenderRequest.Builder(MediaStore.createDeleteRequest(contentResolver, arrayList).getIntentSender()).setFillInIntent((Intent) null).setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0).build());
                        } else {
                            File file = new File(videoModal.getaPath());
                            String[] strArr = {file.getAbsolutePath()};
                            ContentResolver contentResolver2 = VideoFragment.this.getActivity().getContentResolver();
                            Uri contentUri = MediaStore.Files.getContentUri("external");
                            contentResolver2.delete(contentUri, "_data=?", strArr);
                            if (file.exists()) {
                                contentResolver2.delete(contentUri, "_data=?", strArr);
                            }
                            VideoFragment.this.VideosList.remove(VideoFragment.this.VideosList.indexOf(videoModal));
                            VideoFragment.this.videoListAdapter.getFilterList().remove(videoModal);
                            VideoFragment.this.videoGridAdapter.getFilterList().remove(videoModal);
                            int indexOf = ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.indexOf(VideoFragment.this.videoFolderModal);
                            ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.set(indexOf, VideoFragment.this.videoFolderModal);
                            VideoFragment.this.folderAdapter.notifyItemChanged(indexOf);
                            VideoFragment.this.videoListAdapter.notifyDataSetChanged();
                            VideoFragment.this.videoGridAdapter.notifyDataSetChanged();
                            VideoFragment.this.CheckNoData();
                            VideoFragment.this.RemoveFolder();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                VideoFragment.this.MultiVideoList.clear();
                ((MainActivity) VideoFragment.this.getActivity()).actionMode.finish();
            }
        });
        dialogDeleteBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void ShareVideo() {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < this.MultiVideoList.size(); i++) {
            VideoModal videoModal = this.MultiVideoList.get(i);
            if (Build.VERSION.SDK_INT > 29) {
                arrayList.add(Uri.parse(videoModal.getaPath()));
            } else {
                arrayList2.add(FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", new File(videoModal.getaPath())));
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

    public void VideosProperties() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        BottomsheetMultiVideosPropertiesBinding bottomsheetMultiVideosPropertiesBinding = (BottomsheetMultiVideosPropertiesBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_multi_videos_properties, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetMultiVideosPropertiesBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        TextView textView = bottomsheetMultiVideosPropertiesBinding.txtCount;
        textView.setText("" + this.MultiVideoList.size() + " videos");
        long j = 0;
        for (int i = 0; i < this.MultiVideoList.size(); i++) {
            j += this.MultiVideoList.get(i).getSize();
        }
        bottomsheetMultiVideosPropertiesBinding.txtSize.setText(AppConstants.getSize(j));
        ((MainActivity) getActivity()).actionMode.finish();
    }

    public void openBottomSheet(final VideoModal videoModal) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        final BottomsheetVideoMenuBinding bottomsheetVideoMenuBinding = (BottomsheetVideoMenuBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_video_menu, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetVideoMenuBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        if (AppPref.getFavouriteList() == null) {
            bottomsheetVideoMenuBinding.llUnFavourite.setVisibility(View.GONE);
            bottomsheetVideoMenuBinding.llFavourite.setVisibility(View.VISIBLE);
        } else if (AppPref.getFavouriteList().contains(new AudioVideoModal(videoModal.getaPath()))) {
            bottomsheetVideoMenuBinding.llUnFavourite.setVisibility(View.VISIBLE);
            bottomsheetVideoMenuBinding.llFavourite.setVisibility(View.GONE);
        } else {
            bottomsheetVideoMenuBinding.llUnFavourite.setVisibility(View.GONE);
            bottomsheetVideoMenuBinding.llFavourite.setVisibility(View.VISIBLE);
        }
        bottomsheetVideoMenuBinding.txtTitle.setText(videoModal.getaName());
        bottomsheetVideoMenuBinding.llFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                if (AppPref.getFavouriteList() != null) {
                    ArrayList<AudioVideoModal> favouriteList = AppPref.getFavouriteList();
                    favouriteList.add(new AudioVideoModal(videoModal.getaPath(), videoModal.getaName(), videoModal.getDuration(), "", "", AppPref.getFavouriteList().size() + 1));
                    AppPref.setFavouriteList(favouriteList);
                } else {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(new AudioVideoModal(videoModal.getaPath(), videoModal.getaName(), videoModal.getDuration(), "", "", 0));
                    AppPref.setFavouriteList(arrayList);
                }
                bottomsheetVideoMenuBinding.llUnFavourite.setVisibility(View.VISIBLE);
                bottomsheetVideoMenuBinding.llFavourite.setVisibility(View.GONE);
            }
        });
        bottomsheetVideoMenuBinding.llUnFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                if (AppPref.getFavouriteList() != null && AppPref.getFavouriteList().contains(new AudioVideoModal(videoModal.getaPath()))) {
                    ArrayList<AudioVideoModal> favouriteList = AppPref.getFavouriteList();
                    favouriteList.remove(AppPref.getFavouriteList().indexOf(new AudioVideoModal(videoModal.getaPath())));
                    AppPref.getFavouriteList().clear();
                    AppPref.setFavouriteList(favouriteList);
                }
                bottomsheetVideoMenuBinding.llUnFavourite.setVisibility(View.GONE);
                bottomsheetVideoMenuBinding.llFavourite.setVisibility(View.VISIBLE);
            }
        });
        bottomsheetVideoMenuBinding.llPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                VideoFragment.this.OpenAddToPlaylistBottomSheet(videoModal);
            }
        });
        bottomsheetVideoMenuBinding.llRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                VideoFragment.this.OpenRenameDialog(videoModal);
            }
        });
        bottomsheetVideoMenuBinding.llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("video/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT > 29) {
                    intent.putExtra("android.intent.extra.STREAM", Uri.parse(videoModal.getaPath()));
                } else {
                    intent.putExtra("android.intent.extra.STREAM", FileProvider.getUriForFile(VideoFragment.this.getActivity(), getActivity().getPackageName() + ".provider", new File(videoModal.getaPath())));
                }
                try {
                    VideoFragment.this.startActivity(Intent.createChooser(intent, "Share File "));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        bottomsheetVideoMenuBinding.llProperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                VideoFragment.this.OpenPropertiesDialog(videoModal);
            }
        });
        bottomsheetVideoMenuBinding.llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                VideoFragment.this.OpenDeleteDialog(videoModal);
            }
        });
    }

    public void OpenAddToPlaylistBottomSheet(final VideoModal videoModal) {
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
                FolderModal folderModal = VideoFragment.this.folderModalList.get(i).getFolderModal();
                List<AudioVideoModal> GetAudioVideoListByFolderID = VideoFragment.this.appDatabase.audioVideoDao().GetAudioVideoListByFolderID(folderModal.getId());
                AudioVideoModal audioVideoModal = new AudioVideoModal(videoModal.getaPath(), videoModal.getaName(), videoModal.getDuration(), "", "", GetAudioVideoListByFolderID.size() + 1);
                if (!GetAudioVideoListByFolderID.contains(audioVideoModal)) {
                    audioVideoModal.setId(AppConstants.getUniqueId());
                    audioVideoModal.setRefId(folderModal.getId());
                    VideoFragment.this.appDatabase.audioVideoDao().InsertAudioVideo(audioVideoModal);
                }
                Toast.makeText(VideoFragment.this.getActivity(), "Video Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        bottomsheetAddToPlaylistBinding.playlistRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        bottomsheetAddToPlaylistBinding.playlistRecycle.setAdapter(this.bottomPlaylistAdapter);
        bottomsheetAddToPlaylistBinding.llCreatePlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                VideoFragment.this.OpenCreatePlaylistDialog(videoModal);
            }
        });
    }

    public void OpenCreatePlaylistDialog(final VideoModal videoModal) {
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
        dialogRenameBinding.txtTitle.setText("Create Playlist");
        dialogRenameBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(dialogRenameBinding.edtName.getText())) {
                    dialogRenameBinding.edtName.setError("Enter PlayList Name");
                    return;
                }
                dialog.dismiss();
                FolderModal folderModal = new FolderModal(AppConstants.getUniqueId(), dialogRenameBinding.edtName.getText().toString());
                VideoFragment.this.appDatabase.folderDao().InsertFolder(folderModal);
                CombineFolderModel combineFolderModel = new CombineFolderModel();
                combineFolderModel.setFolderModal(folderModal);
                combineFolderModel.setRefId(folderModal.getId());
                VideoFragment.this.folderModalList.add(combineFolderModel);
                VideoFragment.this.bottomPlaylistAdapter.notifyDataSetChanged();
                VideoFragment.this.appDatabase.audioVideoDao().InsertAudioVideo(new AudioVideoModal(AppConstants.getUniqueId(), videoModal.getaPath(), videoModal.getaName(), videoModal.getDuration(), "", "", 0, folderModal.getId()));
                Toast.makeText(VideoFragment.this.getActivity(), "Video Added to playlist", Toast.LENGTH_SHORT).show();
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void OpenDeleteDialog(final VideoModal videoModal) {
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
        this.deleteAllPos = ((MainActivity) getActivity()).AllVideosList.indexOf(new VideoModal(videoModal.getaPath(), videoModal.getBucketId()));
        this.deleteVideoFolderPos = -1;
        this.deleteModelPos = -1;
        if (this.videoFolderModal.getaName().equals("All")) {
            this.deleteVideoFolderPos = ((MainActivity) getActivity()).videoFolderList.indexOf(new VideoFolderModal(videoModal.getBucketName(), videoModal.getBucketId()));
            this.deleteModelPos = ((MainActivity) getActivity()).videoFolderList.get(this.deleteVideoFolderPos).getVideoList().indexOf(new VideoModal(videoModal.getaPath(), videoModal.getBucketId()));
        }
        dialogDeleteBinding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (Build.VERSION.SDK_INT > 29) {
                        VideoFragment.this.deleteModel = videoModal;
                        ContentResolver contentResolver = VideoFragment.this.getActivity().getContentResolver();
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(Uri.parse(videoModal.getaPath()));
                        Collections.addAll(arrayList, new Uri[0]);
                        VideoFragment.this.deleteLauncher.launch(new IntentSenderRequest.Builder(MediaStore.createDeleteRequest(contentResolver, arrayList).getIntentSender()).setFillInIntent((Intent) null).setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0).build());
                    } else {
                        File file = new File(videoModal.getaPath());
                        String[] strArr = {file.getAbsolutePath()};
                        ContentResolver contentResolver2 = VideoFragment.this.getActivity().getContentResolver();
                        Uri contentUri = MediaStore.Files.getContentUri("external");
                        contentResolver2.delete(contentUri, "_data=?", strArr);
                        if (file.exists()) {
                            contentResolver2.delete(contentUri, "_data=?", strArr);
                        }
                        int indexOf = VideoFragment.this.VideosList.indexOf(videoModal);
                        VideoFragment.this.VideosList.remove(indexOf);
                        VideoFragment.this.videoListAdapter.getFilterList().remove(videoModal);
                        VideoFragment.this.videoGridAdapter.getFilterList().remove(videoModal);
                        VideoFragment.this.videoFolderModal.getVideoList().remove(VideoFragment.this.videoFolderModal.getVideoList().indexOf(videoModal));
                        int indexOf2 = ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.indexOf(VideoFragment.this.videoFolderModal);
                        ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.set(indexOf2, VideoFragment.this.videoFolderModal);
                        VideoFragment.this.folderAdapter.notifyItemChanged(indexOf2);
                        VideoFragment.this.videoListAdapter.notifyItemRemoved(indexOf);
                        VideoFragment.this.videoGridAdapter.notifyItemRemoved(indexOf);
                        if (VideoFragment.this.videoFolderModal.getaName().equals("All")) {
                            if (!(VideoFragment.this.deleteVideoFolderPos == -1 || VideoFragment.this.deleteModelPos == -1)) {
                                ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.get(VideoFragment.this.deleteVideoFolderPos).getVideoList().remove(VideoFragment.this.deleteModelPos);
                            }
                        } else if (VideoFragment.this.deleteAllPos != -1) {
                            ((MainActivity) VideoFragment.this.getActivity()).AllVideosList.remove(VideoFragment.this.deleteAllPos);
                        }
                        VideoFragment.this.CheckNoData();
                        VideoFragment.this.RemoveFolder();
                        VideoFragment.this.RemoveFromAllList(videoModal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    public void RemoveFromAllList(VideoModal videoModal) {
        if (AppPref.getRecentList() != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.addAll(AppPref.getRecentList());
            if (arrayList.contains(new HistoryModel(new AudioVideoModal(videoModal.getaPath())))) {
                arrayList.remove(arrayList.indexOf(new HistoryModel(new AudioVideoModal(videoModal.getaPath()))));
                AppPref.setRecentList(arrayList);
            }
        }
        if (AppPref.getFavouriteList() != null) {
            ArrayList arrayList2 = new ArrayList();
            arrayList2.addAll(AppPref.getFavouriteList());
            if (arrayList2.contains(new AudioVideoModal(videoModal.getaPath()))) {
                arrayList2.remove(arrayList2.indexOf(new AudioVideoModal(videoModal.getaPath())));
                AppPref.setFavouriteList(arrayList2);
            }
        }
        this.appDatabase.audioVideoDao().DeleteAudioVideoByUri(videoModal.getaPath());
    }

    public void OpenPropertiesDialog(VideoModal videoModal) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.MyBottomSheetDialogTheme);
        BottomsheetPropertiesBinding bottomsheetPropertiesBinding = (BottomsheetPropertiesBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottomsheet_properties, (ViewGroup) null, false);
        bottomSheetDialog.setContentView(bottomsheetPropertiesBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
        bottomsheetPropertiesBinding.txtFileName.setText(videoModal.getaName());
        if (Build.VERSION.SDK_INT > 29) {
            bottomsheetPropertiesBinding.txtLocation.setText(AppConstants.GetPathFromUri(getActivity(), videoModal.getaPath()));
        } else {
            bottomsheetPropertiesBinding.txtLocation.setText(videoModal.getaPath());
        }
        bottomsheetPropertiesBinding.txtSize.setText(AppConstants.getSize(videoModal.getSize()));
        String str = videoModal.getaName();
        bottomsheetPropertiesBinding.txtFormat.setText(str.substring(str.lastIndexOf(".")));
        bottomsheetPropertiesBinding.txtLength.setText(AppConstants.formatTime(videoModal.getDuration()));
        bottomsheetPropertiesBinding.llArtist.setVisibility(View.GONE);
        bottomsheetPropertiesBinding.llAlbum.setVisibility(View.GONE);
    }

    public void OpenRenameDialog(VideoModal videoModal) {
        final Dialog dialog = new Dialog(getActivity(), R.style.dialogTheme);
        DialogRenameBinding dialogRenameBinding = (DialogRenameBinding) DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_rename, (ViewGroup) null, false);
        dialog.setContentView(dialogRenameBinding.getRoot());
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            dialog.getWindow().setGravity(17);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        final File file = new File(videoModal.getaPath());
        String str = videoModal.getaName();
        final String substring = str.substring(str.lastIndexOf("."));
        String substring2 = str.substring(0, str.lastIndexOf("."));
        this.oldPath = videoModal.getaPath();
        final int indexOf = this.VideosList.indexOf(videoModal);
        this.renamePos = indexOf;
        final int indexOf2 = ((MainActivity) getActivity()).videoFolderList.indexOf(this.videoFolderModal);
        this.renameFolderPos = indexOf2;
        this.renameModel = videoModal;
        Log.d("TAG", "OpenRenameDialog: oldUri" + videoModal.getaPath());
        this.renameAllPos = ((MainActivity) getActivity()).AllVideosList.indexOf(new VideoModal(videoModal.getaPath(), videoModal.getBucketId()));
        this.renameVideoFolderPos = -1;
        this.renameModelPos = -1;
        this.renameFavPos = -1;
        if (this.videoFolderModal.getaName().equals("All")) {
            this.renameVideoFolderPos = ((MainActivity) getActivity()).videoFolderList.indexOf(new VideoFolderModal(videoModal.getBucketName(), videoModal.getBucketId()));
            this.renameModelPos = ((MainActivity) getActivity()).videoFolderList.get(this.renameVideoFolderPos).getVideoList().indexOf(new VideoModal(videoModal.getaPath(), videoModal.getBucketId()));
        }
        if (AppPref.getFavouriteList().contains(new AudioVideoModal(videoModal.getaPath()))) {
            this.renameFavPos = AppPref.getFavouriteList().indexOf(new AudioVideoModal(videoModal.getaPath()));
        }
        if (AppPref.getRecentList().contains(new HistoryModel(new AudioVideoModal(videoModal.getaPath())))) {
            this.renameRecent = AppPref.getRecentList().indexOf(new HistoryModel(new AudioVideoModal(videoModal.getaPath())));
        }
        dialogRenameBinding.edtName.setText(substring2);
        final DialogRenameBinding dialogRenameBinding2 = dialogRenameBinding;
        final Dialog dialog2 = dialog;
        final VideoModal videoModal2 = videoModal;
        dialogRenameBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(dialogRenameBinding2.edtName.getText())) {
                    dialogRenameBinding2.edtName.setError("Enter Video Name");
                    return;
                }
                dialog2.dismiss();
                if (Build.VERSION.SDK_INT > 29) {
                    try {
                        ArrayList arrayList = new ArrayList();
                        VideoFragment.this.renameUri = Uri.parse(videoModal2.getaPath());
                        arrayList.add(Uri.parse(videoModal2.getaPath()));
                        Collections.addAll(arrayList, new Uri[0]);
                        VideoFragment.this.displayName = dialogRenameBinding2.edtName.getText().toString() + substring;
                        VideoFragment.this.renameLauncher.launch(new IntentSenderRequest.Builder(MediaStore.createWriteRequest(VideoFragment.this.getActivity().getContentResolver(), arrayList).getIntentSender()).setFillInIntent((Intent) null).setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0).build());
                        VideoFragment.this.renameModel.setaName(VideoFragment.this.displayName);
                        videoModal2.setaName(VideoFragment.this.displayName);
                        Log.d("TAG", "OpenRenameDialog: oldUri Dialog" + VideoFragment.this.renameUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String absolutePath = file.getParentFile().getAbsolutePath();
                        String absolutePath2 = file.getAbsolutePath();
                        String substring = absolutePath2.substring(absolutePath2.lastIndexOf("."));
                        String str = absolutePath + "/" + dialogRenameBinding2.edtName.getText().toString() + substring;
                        File file = new File(str);
                        if (file.renameTo(file)) {
                            videoModal2.setaName(dialogRenameBinding2.edtName.getText().toString() + substring);
                            videoModal2.setaPath(str);
                            VideoFragment.this.getActivity().getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_data=?", new String[]{file.getAbsolutePath()});
                            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                            intent.setData(Uri.fromFile(file));
                            VideoFragment.this.getActivity().sendBroadcast(intent);
                            Toast.makeText(VideoFragment.this.getActivity(), "Video Renamed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VideoFragment.this.getActivity(), "Process Failed", Toast.LENGTH_SHORT).show();
                        }
                        VideoFragment.this.VideosList.set(indexOf, videoModal2);
                        ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.set(indexOf2, VideoFragment.this.videoFolderModal);
                        int indexOf = VideoFragment.this.videoListAdapter.getFilterList().indexOf(videoModal2);
                        VideoFragment.this.videoListAdapter.getFilterList().set(indexOf, videoModal2);
                        VideoFragment.this.videoGridAdapter.getFilterList().set(indexOf, videoModal2);
                        VideoFragment.this.folderAdapter.notifyItemChanged(indexOf2);
                        VideoFragment.this.videoListAdapter.notifyDataSetChanged();
                        VideoFragment.this.videoGridAdapter.notifyDataSetChanged();
                        if (VideoFragment.this.videoFolderModal.getaName().equals("All")) {
                            if (!(VideoFragment.this.renameVideoFolderPos == -1 || VideoFragment.this.renameModelPos == -1)) {
                                ((MainActivity) VideoFragment.this.getActivity()).videoFolderList.get(VideoFragment.this.renameVideoFolderPos).getVideoList().set(VideoFragment.this.renameModelPos, videoModal2);
                            }
                        } else if (VideoFragment.this.renameAllPos != -1) {
                            ((MainActivity) VideoFragment.this.getActivity()).AllVideosList.set(VideoFragment.this.renameAllPos, videoModal2);
                        }
                        if (VideoFragment.this.renameFavPos != -1) {
                            ArrayList<AudioVideoModal> favouriteList = AppPref.getFavouriteList();
                            favouriteList.set(VideoFragment.this.renameFavPos, new AudioVideoModal(videoModal2.getaPath(), videoModal2.getaName(), videoModal2.getDuration(), "", "", AppPref.getFavouriteList().size() + 1));
                            AppPref.setFavouriteList(favouriteList);
                        }
                        if (VideoFragment.this.renameRecent != -1) {
                            ArrayList<HistoryModel> recentList = AppPref.getRecentList();
                            HistoryModel historyModel = recentList.get(VideoFragment.this.renameRecent);
                            AudioVideoModal audioVideoModal = historyModel.getAudioVideoModal();
                            audioVideoModal.setUri(videoModal2.getaPath());
                            audioVideoModal.setName(videoModal2.getaName());
                            historyModel.setAudioVideoModal(audioVideoModal);
                            recentList.set(VideoFragment.this.renameRecent, historyModel);
                        }
                        VideoFragment.this.appDatabase.audioVideoDao().UpdateUriModel(videoModal2.getaPath(), VideoFragment.this.oldPath);
                        VideoFragment.this.appDatabase.audioVideoDao().UpdateNameModel(videoModal2.getaName(), videoModal2.getaPath());
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
        dialogRenameBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void SetNoData() {
        if (this.videoListAdapter.getFilterList().size() > 0) {
            this.binding.rlNoData.setVisibility(View.GONE);
        } else {
            this.binding.rlNoData.setVisibility(View.VISIBLE);
        }
    }

    public void SortVideoNameDesc() {
        Collections.sort(this.videoListAdapter.getFilterList(), new Comparator<VideoModal>() {
            @Override
            public int compare(VideoModal videoModal, VideoModal videoModal2) {
                return videoModal2.getaName().compareTo(videoModal.getaName());
            }
        });
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void SortVideoNameAsc() {
        Collections.sort(this.videoListAdapter.getFilterList(), new Comparator<VideoModal>() {
            @Override
            public int compare(VideoModal videoModal, VideoModal videoModal2) {
                return videoModal.getaName().compareTo(videoModal2.getaName());
            }
        });
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void SortVideoFileDesc() {
        Collections.sort(this.videoListAdapter.getFilterList(), new Comparator<VideoModal>() {
            @Override
            public int compare(VideoModal videoModal, VideoModal videoModal2) {
                return Long.compare(videoModal2.getSize(), videoModal.getSize());
            }
        });
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void SortVideoFileAsc() {
        Collections.sort(this.videoListAdapter.getFilterList(), new Comparator<VideoModal>() {
            @Override
            public int compare(VideoModal videoModal, VideoModal videoModal2) {
                return Long.compare(videoModal.getSize(), videoModal2.getSize());
            }
        });
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void SortVideoLengthDesc() {
        Collections.sort(this.videoListAdapter.getFilterList(), new Comparator<VideoModal>() {
            @Override
            public int compare(VideoModal videoModal, VideoModal videoModal2) {
                return Long.compare(videoModal2.getDuration(), videoModal.getDuration());
            }
        });
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void SortVideoLengthAsc() {
        Collections.sort(this.videoListAdapter.getFilterList(), new Comparator<VideoModal>() {
            @Override
            public int compare(VideoModal videoModal, VideoModal videoModal2) {
                return Long.compare(videoModal.getDuration(), videoModal2.getDuration());
            }
        });
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void SortVideoDateDesc() {
        Collections.sort(this.videoListAdapter.getFilterList(), new Comparator<VideoModal>() {
            @Override
            public int compare(VideoModal videoModal, VideoModal videoModal2) {
                return Long.compare(videoModal2.getCreationDate(), videoModal.getCreationDate());
            }
        });
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void SortVideoDateAsc() {
        Collections.sort(this.videoListAdapter.getFilterList(), new Comparator<VideoModal>() {
            @Override
            public int compare(VideoModal videoModal, VideoModal videoModal2) {
                return Long.compare(videoModal.getCreationDate(), videoModal2.getCreationDate());
            }
        });
        this.videoListAdapter.notifyDataSetChanged();
        this.videoGridAdapter.notifyDataSetChanged();
    }

    public void NotifyVideoFolderAdapter() {
        if (((MainActivity) getActivity()) == null) {
            return;
        }
        if (((MainActivity) getActivity()).videoFolderList.size() > 0) {
            this.folderAdapter.setVideoFolderList(((MainActivity) getActivity()).videoFolderList);
        } else {
            CheckNoData();
        }
    }

    public void NotifyVideosListAdapter() {
        if (((MainActivity) getActivity()) == null) {
            return;
        }
        if (((MainActivity) getActivity()).videoFolderList.size() > 0) {
            this.videoListAdapter.setVideoFolderList(((MainActivity) getActivity()).videoFolderList.get(0).getVideoList());
            this.VideosList.clear();
            this.VideosList.addAll(((MainActivity) getActivity()).videoFolderList.get(0).getVideoList());
            return;
        }
        CheckNoData();
    }

    public void NotifyVideosGridAdapter() {
        if (((MainActivity) getActivity()) == null) {
            return;
        }
        if (((MainActivity) getActivity()).videoFolderList.size() > 0) {
            this.videoGridAdapter.setVideoFolderList(((MainActivity) getActivity()).videoFolderList.get(0).getVideoList());
            this.VideosList.clear();
            this.VideosList.addAll(((MainActivity) getActivity()).videoFolderList.get(0).getVideoList());
            return;
        }
        CheckNoData();
    }

    public void SetVideoFolderModal(VideoFolderModal videoFolderModal2) {
        this.videoFolderModal = videoFolderModal2;
    }

    public void ChangeDataList(boolean z) {
        if (z) {
            this.binding.VideoGridRecycle.setVisibility(View.VISIBLE);
            this.binding.VideoListRecycle.setVisibility(View.GONE);
            return;
        }
        this.binding.VideoGridRecycle.setVisibility(View.GONE);
        this.binding.VideoListRecycle.setVisibility(View.VISIBLE);
    }

    public boolean isGridVisible() {
        return this.binding.VideoGridRecycle.getVisibility() == View.VISIBLE;
    }

    public void CheckNoData() {
        if (this.VideosList.size() > 0) {
            this.binding.rlNoData.setVisibility(View.GONE);
        } else {
            this.binding.rlNoData.setVisibility(View.VISIBLE);
        }
    }

    public void RemoveFolder() {
        if (this.VideosList.size() <= 0) {
            int indexOf = ((MainActivity) getActivity()).videoFolderList.indexOf(this.videoFolderModal);
            ((MainActivity) getActivity()).videoFolderList.remove(indexOf);
            this.folderAdapter.notifyItemRemoved(indexOf);
            this.VideosList.clear();
            CheckNoData();
        }
    }
}
