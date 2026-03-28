package com.kannada.musicplayer.activities;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kannada.musicplayer.R;
import com.kannada.musicplayer.adapter.AudioSelectionAdapter;
import com.kannada.musicplayer.adapter.VideoSelectionAdapter;
import com.kannada.musicplayer.ads.AdsCommon;
import com.kannada.musicplayer.database.AppDatabase;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.database.model.FolderModal;
import com.kannada.musicplayer.databinding.ActivityAudioVideoListBinding;
import com.kannada.musicplayer.model.AudioModel;
import com.kannada.musicplayer.model.VideoModal;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;

public class AudioVideoListActivity extends AppCompatActivity {
    List<AudioModel> InsertionAudioList = new ArrayList();
    List<VideoModal> InsertionVideoList = new ArrayList();
    AppDatabase appDatabase;
    AudioSelectionAdapter audioListAdapter;
    List<AudioModel> audioModelList = new ArrayList();
    List<String> audioUriList = new ArrayList();
    ArrayList<AudioVideoModal> audioVideoModals = new ArrayList<>();
    ActivityAudioVideoListBinding binding;
    CompositeDisposable disposable = new CompositeDisposable();
    MenuItem done;
    FolderModal folderModal;
    boolean isAllSelected = false;
    boolean isForVideo = false;
    boolean isFromFav = false;
    MenuItem selected;
    int size = 0;
    MenuItem unSelected;
    VideoSelectionAdapter videoListAdapter;
    List<VideoModal> videoModalList = new ArrayList();

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
        this.binding = (ActivityAudioVideoListBinding) DataBindingUtil.setContentView(this, R.layout.activity_audio_video_list);

        applyDisplayCutouts();



        //Reguler Banner Ads


        this.appDatabase = AppDatabase.getAppDatabase(this);
        setToolbar();
        LoadList();
    }

    private void LoadList() {
        this.binding.progressBar.setVisibility(View.VISIBLE);
        this.disposable.add(Observable.fromCallable(new AudioVideoListActivityObservable1(this)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new AudioVideoListActivityObservable2(this)));
    }

    public Boolean AudioVideoListActivityObservable1call() throws Exception {
        this.folderModal = (FolderModal) getIntent().getParcelableExtra("FolderModal");
        this.isForVideo = getIntent().getBooleanExtra("isForVideo", false);
        boolean booleanExtra = getIntent().getBooleanExtra("isFormFav", false);
        this.isFromFav = booleanExtra;
        if (!booleanExtra) {
            this.audioUriList = this.appDatabase.audioVideoDao().GetAudioVideoUriListByFolderID(this.folderModal.getId());
        } else if (AppPref.getFavouriteList() != null) {
            for (int i = 0; i < AppPref.getFavouriteList().size(); i++) {
                this.audioUriList.add(AppPref.getFavouriteList().get(i).getUri());
            }
        }
        this.size = this.audioUriList.size();
        if (this.isForVideo) {
            if (Build.VERSION.SDK_INT > 29) {
                LoadVideoUriList();
            } else {
                LoadVideoDataList();
            }
        } else if (Build.VERSION.SDK_INT > 29) {
            LoadAudioUriList();
        } else {
            LoadAudioDataList();
        }
        return false;
    }

    public void AudioVideoListActivityObservable2call(Boolean bool) throws Exception {
        this.binding.progressBar.setVisibility(View.GONE);
        if (this.isForVideo) {
            setVideoAdapter();
        } else {
            setAudioAdapter();
        }
        CheckNoData();
    }

    private void setToolbar() {
        setSupportActionBar(this.binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle((CharSequence) "");
        this.binding.toolbarLayout.toolbar.setNavigationIcon(getDrawable(R.drawable.ic_back));
    }

    private void LoadVideoUriList() {
        Cursor query = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "date_modified", "bucket_display_name", "_size", "duration", "_display_name", "bucket_id", "date_added"}, (String) null, (String[]) null, "date_modified DESC");
        if (query != null && query.getCount() > 0) {
            while (query.moveToNext()) {
                String valueOf = String.valueOf(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, query.getLong(query.getColumnIndex("_id"))));
                String string = query.getString(query.getColumnIndexOrThrow("_display_name"));
                long j = query.getLong(query.getColumnIndex("_size"));
                int columnIndex = query.getColumnIndex("bucket_display_name");
                long j2 = query.getLong(query.getColumnIndex("date_modified"));
                long j3 = query.getLong(query.getColumnIndex("date_modified"));
                long j4 = query.getLong(query.getColumnIndex("duration"));
                query.getColumnIndex("bucket_id");
                query.getString(columnIndex);
                if (j4 != 0 && !this.audioUriList.contains(valueOf)) {
                    this.videoModalList.add(new VideoModal(valueOf, string, j, j2 * 1000, j4, j3));
                }
            }
        }
        if (query != null) {
            query.close();
        }
    }

    private void LoadVideoDataList() {
        Cursor query = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_data", "date_modified", "date_added", "bucket_display_name", "_size", "duration", "_display_name", "bucket_id"}, (String) null, (String[]) null, "date_modified DESC");
        if (query != null && query.moveToFirst()) {
            while (query.moveToNext()) {
                String string = query.getString(query.getColumnIndexOrThrow("_data"));
                String string2 = query.getString(query.getColumnIndexOrThrow("_display_name"));
                long j = query.getLong(query.getColumnIndex("_size"));
                int columnIndex = query.getColumnIndex("bucket_display_name");
                long j2 = query.getLong(query.getColumnIndex("date_modified"));
                long j3 = query.getLong(query.getColumnIndex("date_added"));
                long j4 = query.getLong(query.getColumnIndex("duration"));
                query.getString(columnIndex);
                Objects.toString(Environment.getExternalStorageDirectory());
                String str = File.separator;
                if (j4 != 0 && !this.audioUriList.contains(string)) {
                    this.videoModalList.add(new VideoModal(string, string2, j, j2 * 1000, j4, j3));
                }
            }
        }
        if (query != null) {
            query.close();
        }
    }

    public void LoadAudioUriList() {
        String str = null;
        String str2 = "duration";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_display_name", "track", "year", "duration", "_data", "date_modified", "album_id", "album", "artist_id", "artist", "composer", "title", "_size", "bucket_display_name", "bucket_id", "date_added"}, (String) null, (String[]) null, (String) null);
            while (cursor.moveToNext()) {
                if (cursor.getCount() > 0) {
                    int columnIndexOrThrow = cursor.getColumnIndexOrThrow("_size");
                    if (cursor.getString(columnIndexOrThrow) != null && Long.parseLong(cursor.getString(columnIndexOrThrow)) > 0) {
                        Uri withAppendedPath = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getString(cursor.getColumnIndex("_id")));
                        int columnIndexOrThrow2 = cursor.getColumnIndexOrThrow("title");
                        int columnIndexOrThrow3 = cursor.getColumnIndexOrThrow("_display_name");
                        long columnIndex = (long) cursor.getColumnIndex(str2);
                        String string = cursor.getString(cursor.getColumnIndex("artist"));
                        int i = columnIndexOrThrow2;
                        Uri withAppendedId = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), (long) cursor.getInt(cursor.getColumnIndex("album_id")));
                        String extension = FilenameUtils.getExtension(cursor.getString(columnIndexOrThrow3));
                        String string2 = cursor.getString(cursor.getColumnIndex("album"));
                        long j = cursor.getLong(cursor.getColumnIndex(str2));
                        cursor.getString(cursor.getColumnIndex("_display_name"));
                        int columnIndex2 = cursor.getColumnIndex("bucket_display_name");
                        int columnIndex3 = cursor.getColumnIndex("bucket_id");
                        cursor.getString(columnIndex2);
                        cursor.getString(columnIndex3);
                        long j2 = cursor.getLong(cursor.getColumnIndex("date_added"));
                        if (j != 0) {
                            AudioModel audioModel = new AudioModel();
                            audioModel.setUri(withAppendedPath.toString());
                            audioModel.setPath(withAppendedPath.toString());
                            str = str2;
                            int i2 = i;
                            audioModel.setName(cursor.getString(i2));
                            int i3 = (int) columnIndex;
                            audioModel.setTime(AppConstants.GetTimeNew(cursor.getLong(i3)));
                            audioModel.setSize(AppConstants.getSize(Long.parseLong(cursor.getString(columnIndexOrThrow))));
                            audioModel.setLongSize(Long.parseLong(cursor.getString(columnIndexOrThrow)));
                            audioModel.setDuration(cursor.getLong(i3));
                            audioModel.setPlayed(false);
                            audioModel.setAlbumId(withAppendedId.toString());
                            audioModel.setAlbumName(string2);
                            audioModel.setArtist(string);
                            audioModel.setType(extension);
                            audioModel.setCreationDate(j2);
                            if (!TextUtils.isEmpty(audioModel.getAlbumName()) && !TextUtils.isEmpty(audioModel.getArtist()) && !this.audioUriList.contains(withAppendedPath.toString())) {
                                this.audioModelList.add(new AudioModel(cursor.getString(i2), withAppendedPath.toString(), AppConstants.getSize(Long.parseLong(cursor.getString(columnIndexOrThrow))), Long.parseLong(cursor.getString(columnIndexOrThrow)), AppConstants.GetTimeNew(cursor.getLong(i3)), extension, withAppendedPath.toString(), string, withAppendedId.toString(), string2, false, cursor.getLong(i3), j2));
                            }
                        }
                    } else {
                        str = str2;
                    }
                    str2 = str;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void LoadAudioDataList() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"_data", "title", "track", "year", "duration", "_data", "date_modified", "album_id", "album", "artist_id", "artist", "composer", "_id", "_display_name", "_size", "duration", "date_added"}, (String) null, (String[]) null, "album ASC");
            while (cursor.moveToNext()) {
                if (cursor.getCount() > 0) {
                    int columnIndexOrThrow = cursor.getColumnIndexOrThrow("_size");
                    if (Long.parseLong(cursor.getString(columnIndexOrThrow)) > 0) {
                        Uri withAppendedPath = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getString(cursor.getColumnIndex("_id")));
                        String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                        int columnIndexOrThrow2 = cursor.getColumnIndexOrThrow("title");
                        long columnIndex = (long) cursor.getColumnIndex("duration");
                        String string2 = cursor.getString(cursor.getColumnIndex("artist"));
                        Uri withAppendedId = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), (long) cursor.getInt(cursor.getColumnIndex("album_id")));
                        String extension = FilenameUtils.getExtension(cursor.getString(cursor.getColumnIndexOrThrow("_display_name")));
                        String string3 = cursor.getString(cursor.getColumnIndex("album"));
                        long j = cursor.getLong(cursor.getColumnIndex("duration"));
                        cursor.getString(cursor.getColumnIndex("_display_name"));
                        long j2 = cursor.getLong(cursor.getColumnIndex("date_added"));
                        new File(string).getParentFile().getName();
                        if (j != 0) {
                            AudioModel audioModel = new AudioModel();
                            audioModel.setPath(string);
                            audioModel.setName(cursor.getString(columnIndexOrThrow2));
                            audioModel.setUri(withAppendedPath.toString());
                            int i = (int) columnIndex;
                            audioModel.setTime(AppConstants.GetTimeNew(cursor.getLong(i)));
                            audioModel.setSize(AppConstants.getSize(Long.parseLong(cursor.getString(columnIndexOrThrow))));
                            int i2 = columnIndexOrThrow;
                            Uri uri = withAppendedPath;
                            audioModel.setLongSize(Long.parseLong(cursor.getString(columnIndexOrThrow)));
                            audioModel.setPlayed(false);
                            audioModel.setAlbumId(withAppendedId.toString());
                            audioModel.setArtist(string2);
                            audioModel.setDuration(cursor.getLong(i));
                            audioModel.setType(extension);
                            audioModel.setAlbumName(string3);
                            if (!TextUtils.isEmpty(audioModel.getAlbumName()) && !TextUtils.isEmpty(audioModel.getArtist()) && !this.audioUriList.contains(string)) {
                                List<AudioModel> list = this.audioModelList;
                                String string4 = cursor.getString(columnIndexOrThrow2);
                                String size2 = AppConstants.getSize(Long.parseLong(cursor.getString(i2)));
                                long parseLong = Long.parseLong(cursor.getString(i2));
                                String GetTimeNew = AppConstants.GetTimeNew(cursor.getLong(i));
                                String uri2 = uri.toString();
                                String str = string3;
                                String str2 = string2;
                                list.add(new AudioModel(string4, string, size2, parseLong, GetTimeNew, extension, uri2, str2, withAppendedId.toString(), str, false, cursor.getLong(i), j2));
                            }
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void setAudioAdapter() {
        this.audioListAdapter = new AudioSelectionAdapter(this, this.audioModelList, this.InsertionAudioList, new AudioSelectionAdapter.AudioClick() {
            @Override
            public void onAudioClick(int i) {
                AudioModel audioModel = AudioVideoListActivity.this.audioListAdapter.getFilterList().get(i);
                if (AudioVideoListActivity.this.InsertionAudioList.contains(audioModel)) {
                    AudioVideoListActivity.this.InsertionAudioList.remove(audioModel);
                    AudioVideoModal audioVideoModal = new AudioVideoModal(audioModel.getPath());
                    if (AudioVideoListActivity.this.audioVideoModals.contains(new AudioVideoModal(audioModel.getPath()))) {
                        AudioVideoListActivity.this.audioVideoModals.remove(audioVideoModal);
                    }
                } else {
                    AudioVideoListActivity.this.InsertionAudioList.add(audioModel);
                    if (AudioVideoListActivity.this.isFromFav) {
                        ArrayList<AudioVideoModal> arrayList = AudioVideoListActivity.this.audioVideoModals;
                        String path = audioModel.getPath();
                        String name = audioModel.getName();
                        long duration = audioModel.getDuration();
                        String artist = audioModel.getArtist();
                        String albumName = audioModel.getAlbumName();
                        AudioVideoListActivity audioVideoListActivity = AudioVideoListActivity.this;
                        int i2 = audioVideoListActivity.size;
                        audioVideoListActivity.size = i2 + 1;
                        arrayList.add(new AudioVideoModal(path, name, duration, artist, albumName, i2));
                    } else {
                        ArrayList<AudioVideoModal> arrayList2 = AudioVideoListActivity.this.audioVideoModals;
                        String uniqueId = AppConstants.getUniqueId();
                        String path2 = audioModel.getPath();
                        String name2 = audioModel.getName();
                        long duration2 = audioModel.getDuration();
                        String artist2 = audioModel.getArtist();
                        String albumName2 = audioModel.getAlbumName();
                        AudioVideoListActivity audioVideoListActivity2 = AudioVideoListActivity.this;
                        int i3 = audioVideoListActivity2.size;
                        audioVideoListActivity2.size = i3 + 1;
                        arrayList2.add(new AudioVideoModal(uniqueId, path2, name2, duration2, artist2, albumName2, i3, AudioVideoListActivity.this.folderModal.getId()));
                    }
                }
                AudioVideoListActivity.this.audioListAdapter.notifyItemChanged(i);
                AudioVideoListActivity.this.CheckAllSelection();
            }
        });
        this.binding.recycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        this.binding.recycle.setAdapter(this.audioListAdapter);
    }

    private void setVideoAdapter() {
        this.videoListAdapter = new VideoSelectionAdapter(this, this.videoModalList, this.InsertionVideoList, new VideoSelectionAdapter.VideoClick() {
            @Override
            public void onVideoClick(int i) {
                VideoModal videoModal = AudioVideoListActivity.this.videoListAdapter.getFilterList().get(i);
                if (AudioVideoListActivity.this.InsertionVideoList.contains(videoModal)) {
                    AudioVideoListActivity.this.InsertionVideoList.remove(videoModal);
                    AudioVideoListActivity.this.audioVideoModals.remove(new AudioVideoModal(videoModal.getaPath()));
                } else {
                    AudioVideoListActivity.this.InsertionVideoList.add(videoModal);
                    if (AudioVideoListActivity.this.isFromFav) {
                        ArrayList<AudioVideoModal> arrayList = AudioVideoListActivity.this.audioVideoModals;
                        String str = videoModal.getaPath();
                        String str2 = videoModal.getaName();
                        long duration = videoModal.getDuration();
                        AudioVideoListActivity audioVideoListActivity = AudioVideoListActivity.this;
                        int i2 = audioVideoListActivity.size;
                        audioVideoListActivity.size = i2 + 1;
                        arrayList.add(new AudioVideoModal(str, str2, duration, "", "", i2));
                    } else {
                        ArrayList<AudioVideoModal> arrayList2 = AudioVideoListActivity.this.audioVideoModals;
                        String uniqueId = AppConstants.getUniqueId();
                        String str3 = videoModal.getaPath();
                        String str4 = videoModal.getaName();
                        long duration2 = videoModal.getDuration();
                        AudioVideoListActivity audioVideoListActivity2 = AudioVideoListActivity.this;
                        int i3 = audioVideoListActivity2.size;
                        audioVideoListActivity2.size = i3 + 1;
                        arrayList2.add(new AudioVideoModal(uniqueId, str3, str4, duration2, "", "", i3, AudioVideoListActivity.this.folderModal.getId()));
                    }
                }
                AudioVideoListActivity.this.videoListAdapter.notifyItemChanged(i);
                AudioVideoListActivity.this.CheckAllSelection();
            }
        });
        this.binding.recycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        this.binding.recycle.setAdapter(this.videoListAdapter);
    }

    
    public void CheckAllSelection() {
        if (this.isForVideo) {
            if (this.InsertionVideoList.size() == this.videoModalList.size()) {
                this.selected.setVisible(true);
                this.unSelected.setVisible(false);
                return;
            }
            this.selected.setVisible(false);
            this.unSelected.setVisible(true);
        } else if (this.InsertionAudioList.size() == this.audioModelList.size()) {
            this.selected.setVisible(true);
            this.unSelected.setVisible(false);
        } else {
            this.selected.setVisible(false);
            this.unSelected.setVisible(true);
        }
    }

    private void AllSelect(boolean z) {
        this.isAllSelected = z;
        int i = 0;
        if (this.isForVideo) {
            while (i < this.videoModalList.size()) {
                VideoModal videoModal = this.videoModalList.get(i);
                if (z) {
                    if (!this.InsertionVideoList.contains(videoModal)) {
                        this.InsertionVideoList.add(videoModal);
                        if (this.isFromFav) {
                            ArrayList<AudioVideoModal> arrayList = this.audioVideoModals;
                            String str = videoModal.getaPath();
                            String str2 = videoModal.getaName();
                            long duration = videoModal.getDuration();
                            int i2 = this.size;
                            this.size = i2 + 1;
                            arrayList.add(new AudioVideoModal(str, str2, duration, "", "", i2));
                        } else {
                            ArrayList<AudioVideoModal> arrayList2 = this.audioVideoModals;
                            String uniqueId = AppConstants.getUniqueId();
                            String str3 = videoModal.getaPath();
                            String str4 = videoModal.getaName();
                            long duration2 = videoModal.getDuration();
                            int i3 = this.size;
                            this.size = i3 + 1;
                            arrayList2.add(new AudioVideoModal(uniqueId, str3, str4, duration2, "", "", i3, this.folderModal.getId()));
                        }
                    }
                } else if (this.InsertionVideoList.contains(videoModal)) {
                    this.audioVideoModals.remove(new AudioVideoModal(videoModal.getaPath()));
                }
                i++;
            }
            if (!z) {
                this.InsertionVideoList.clear();
            }
            this.videoListAdapter.notifyDataSetChanged();
            return;
        }
        while (i < this.audioModelList.size()) {
            AudioModel audioModel = this.audioModelList.get(i);
            if (z) {
                if (!this.InsertionAudioList.contains(audioModel)) {
                    this.InsertionAudioList.add(audioModel);
                    if (this.isFromFav) {
                        ArrayList<AudioVideoModal> arrayList3 = this.audioVideoModals;
                        String path = audioModel.getPath();
                        String name = audioModel.getName();
                        long duration3 = audioModel.getDuration();
                        String artist = audioModel.getArtist();
                        String albumName = audioModel.getAlbumName();
                        int i4 = this.size;
                        this.size = i4 + 1;
                        arrayList3.add(new AudioVideoModal(path, name, duration3, artist, albumName, i4));
                    } else {
                        ArrayList<AudioVideoModal> arrayList4 = this.audioVideoModals;
                        String uniqueId2 = AppConstants.getUniqueId();
                        String path2 = audioModel.getPath();
                        String name2 = audioModel.getName();
                        long duration4 = audioModel.getDuration();
                        String artist2 = audioModel.getArtist();
                        String albumName2 = audioModel.getAlbumName();
                        int i5 = this.size;
                        this.size = i5 + 1;
                        arrayList4.add(new AudioVideoModal(uniqueId2, path2, name2, duration4, artist2, albumName2, i5, this.folderModal.getId()));
                    }
                }
            } else if (this.InsertionAudioList.contains(audioModel)) {
                this.audioVideoModals.remove(new AudioVideoModal(audioModel.getPath()));
            }
            i++;
        }
        if (!z) {
            this.InsertionAudioList.clear();
        }
        this.audioListAdapter.notifyDataSetChanged();
    }

    public void CheckNoData() {
        if (this.isForVideo) {
            if (this.videoModalList.size() > 0) {
                this.binding.recycle.setVisibility(View.VISIBLE);
                this.binding.rlNoData.setVisibility(View.GONE);
                return;
            }
            this.binding.recycle.setVisibility(View.GONE);
            this.binding.rlNoData.setVisibility(View.VISIBLE);
        } else if (this.audioModelList.size() > 0) {
            this.binding.recycle.setVisibility(View.VISIBLE);
            this.binding.rlNoData.setVisibility(View.GONE);
        } else {
            this.binding.recycle.setVisibility(View.GONE);
            this.binding.rlNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.audio_video_list_menu, menu);
        this.selected = menu.findItem(R.id.select);
        this.unSelected = menu.findItem(R.id.unSelect);
        this.done = menu.findItem(R.id.done);
        this.selected.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            onBackPressed();
        } else {
            if (menuItem.getItemId() == R.id.select) {
                AllSelect(false);
                this.unSelected.setVisible(true);
                this.selected.setVisible(false);
            } else if (menuItem.getItemId() == R.id.unSelect) {
                this.unSelected.setVisible(false);
                this.selected.setVisible(true);
                AllSelect(true);
            } else if (menuItem.getItemId() == R.id.done) {
                if (this.audioVideoModals.size() > 0) {
                    if (!this.isFromFav) {
                        for (int i = 0; i < this.audioVideoModals.size(); i++) {
                            this.appDatabase.audioVideoDao().InsertAudioVideo(this.audioVideoModals.get(i));
                        }
                    }
                    Intent intent = getIntent();
                    intent.putParcelableArrayListExtra("AudioVideoList", this.audioVideoModals);
                    setResult(-1, intent);
                }
                finish();
            } else if (menuItem.getItemId() == R.id.search) {
                this.unSelected.setVisible(false);
                this.selected.setVisible(false);
                this.done.setVisible(false);
                SearchView searchView = (SearchView) menuItem.getActionView();
                ((ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn)).setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                EditText editText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                editText.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                editText.setHint("Search here");
                editText.setHintTextColor(getResources().getColor(R.color.dialogBg));
                searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean z) {
                        if (!z) {
                            if (AudioVideoListActivity.this.isAllSelected) {
                                AudioVideoListActivity.this.selected.setVisible(true);
                                AudioVideoListActivity.this.unSelected.setVisible(false);
                            } else {
                                AudioVideoListActivity.this.selected.setVisible(false);
                                AudioVideoListActivity.this.unSelected.setVisible(true);
                            }
                            AudioVideoListActivity.this.done.setVisible(true);
                        }
                    }
                });
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String str) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String str) {
                        if (AudioVideoListActivity.this.videoListAdapter == null && AudioVideoListActivity.this.audioListAdapter == null) {
                            return false;
                        }
                        if (AudioVideoListActivity.this.isForVideo) {
                            AudioVideoListActivity.this.videoListAdapter.getFilter().filter(str);
                        } else {
                            AudioVideoListActivity.this.audioListAdapter.getFilter().filter(str);
                        }
                        return false;
                    }
                });
            }
        }
        return true;
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
