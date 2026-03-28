package com.kannada.musicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumModel implements Parcelable {
    public static final Creator<AlbumModel> CREATOR = new Creator<AlbumModel>() {
        @Override
        public AlbumModel createFromParcel(Parcel parcel) {
            return new AlbumModel(parcel);
        }

        @Override
        public AlbumModel[] newArray(int i) {
            return new AlbumModel[i];
        }
    };
    String album;
    String albumArt;
    String artist;
    List<AudioModel> audioModelList = new ArrayList();
    int count = 0;
    String id;
    String noOfSongs;
    String path;

    public int describeContents() {
        return 0;
    }

    public AlbumModel(String str, String str2) {
        this.id = str;
        this.album = str2;
    }

    public AlbumModel(String str) {
        this.album = str;
    }

    protected AlbumModel(Parcel parcel) {
        this.id = parcel.readString();
        this.album = parcel.readString();
        this.artist = parcel.readString();
        this.noOfSongs = parcel.readString();
        this.albumArt = parcel.readString();
        this.audioModelList = parcel.createTypedArrayList(AudioModel.CREATOR);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String str) {
        this.id = str;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(String str) {
        this.album = str;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(String str) {
        this.artist = str;
    }

    public String getNoOfSongs() {
        return this.noOfSongs;
    }

    public void setNoOfSongs(String str) {
        this.noOfSongs = str;
    }

    public String getAlbumArt() {
        return this.albumArt;
    }

    public void setAlbumArt(String str) {
        this.albumArt = str;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String str) {
        this.path = str;
    }

    public List<AudioModel> getAudioModelList() {
        return this.audioModelList;
    }

    public void setAudioModelList(List<AudioModel> list) {
        this.audioModelList = list;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount() {
        this.count++;
    }

    public void setAudioCount(int i) {
        this.count = i;
    }

    public void addAudio(AudioModel audioModel) {
        this.audioModelList.add(audioModel);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AlbumModel albumModel = (AlbumModel) obj;
        if (!Objects.equals(this.id, albumModel.id) || !Objects.equals(this.album, albumModel.album)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.album});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.album);
        parcel.writeString(this.artist);
        parcel.writeString(this.noOfSongs);
        parcel.writeString(this.albumArt);
        parcel.writeTypedList(this.audioModelList);
    }
}
