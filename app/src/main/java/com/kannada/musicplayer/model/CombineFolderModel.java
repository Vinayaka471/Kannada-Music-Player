package com.demo.musicvideoplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.demo.musicvideoplayer.database.model.FolderModal;
import java.util.Objects;

public class CombineFolderModel implements Parcelable {
    public static final Creator<CombineFolderModel> CREATOR = new Creator<CombineFolderModel>() {
        @Override
        public CombineFolderModel createFromParcel(Parcel parcel) {
            return new CombineFolderModel(parcel);
        }

        @Override
        public CombineFolderModel[] newArray(int i) {
            return new CombineFolderModel[i];
        }
    };
    String Album;
    String Artist;
    String RefId;
    String Uri;
    FolderModal folderModal;
    int totalSongs;
    int totalVideos;

    public int describeContents() {
        return 0;
    }

    public CombineFolderModel() {
    }

    public CombineFolderModel(FolderModal folderModal2, String str, int i, int i2, String str2, String str3, String str4) {
        this.folderModal = folderModal2;
        this.RefId = str;
        this.totalVideos = i;
        this.totalSongs = i2;
        this.Uri = str2;
        this.Album = str3;
        this.Artist = str4;
    }

    public CombineFolderModel(FolderModal folderModal2) {
        this.folderModal = folderModal2;
    }

    protected CombineFolderModel(Parcel parcel) {
        this.folderModal = (FolderModal) parcel.readParcelable(FolderModal.class.getClassLoader());
        this.RefId = parcel.readString();
        this.totalVideos = parcel.readInt();
        this.totalSongs = parcel.readInt();
        this.Uri = parcel.readString();
        this.Album = parcel.readString();
        this.Artist = parcel.readString();
    }

    public FolderModal getFolderModal() {
        return this.folderModal;
    }

    public void setFolderModal(FolderModal folderModal2) {
        this.folderModal = folderModal2;
    }

    public String getRefId() {
        return this.RefId;
    }

    public void setRefId(String str) {
        this.RefId = str;
    }

    public int getTotalVideos() {
        return this.totalVideos;
    }

    public void setTotalVideos(int i) {
        this.totalVideos = i;
    }

    public int getTotalSongs() {
        return this.totalSongs;
    }

    public void setTotalSongs(int i) {
        this.totalSongs = i;
    }

    public String getUri() {
        return this.Uri;
    }

    public void setUri(String str) {
        this.Uri = str;
    }

    public String getAlbum() {
        return this.Album;
    }

    public void setAlbum(String str) {
        this.Album = str;
    }

    public String getArtist() {
        return this.Artist;
    }

    public void setArtist(String str) {
        this.Artist = str;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.folderModal, ((CombineFolderModel) obj).folderModal);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.folderModal});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.folderModal, i);
        parcel.writeString(this.RefId);
        parcel.writeInt(this.totalVideos);
        parcel.writeInt(this.totalSongs);
        parcel.writeString(this.Uri);
        parcel.writeString(this.Album);
        parcel.writeString(this.Artist);
    }
}
