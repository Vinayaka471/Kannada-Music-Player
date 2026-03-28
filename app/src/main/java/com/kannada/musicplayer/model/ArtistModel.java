package com.kannada.musicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArtistModel implements Parcelable {
    public static final Creator<ArtistModel> CREATOR = new Creator<ArtistModel>() {
        @Override
        public ArtistModel createFromParcel(Parcel parcel) {
            return new ArtistModel(parcel);
        }

        @Override
        public ArtistModel[] newArray(int i) {
            return new ArtistModel[i];
        }
    };
    String Path;
    String artist;
    List<AudioModel> audioModelList = new ArrayList();
    int count = 0;
    String id;
    String numOfAlbum;

    public int describeContents() {
        return 0;
    }

    public ArtistModel(String str, String str2) {
        this.id = str;
        this.artist = str2;
    }

    public ArtistModel(String str) {
        this.artist = str;
    }

    protected ArtistModel(Parcel parcel) {
        this.id = parcel.readString();
        this.artist = parcel.readString();
        this.numOfAlbum = parcel.readString();
        this.Path = parcel.readString();
        this.count = parcel.readInt();
        this.audioModelList = parcel.createTypedArrayList(AudioModel.CREATOR);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String str) {
        this.id = str;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(String str) {
        this.artist = str;
    }

    public String getNumOfAlbum() {
        return this.numOfAlbum;
    }

    public void setNumOfAlbum(String str) {
        this.numOfAlbum = str;
    }

    public String getPath() {
        return this.Path;
    }

    public void setPath(String str) {
        this.Path = str;
    }

    public void setCount(int i) {
        this.count = i;
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
        ArtistModel artistModel = (ArtistModel) obj;
        if (!Objects.equals(this.id, artistModel.id) || !Objects.equals(this.artist, artistModel.artist)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.artist});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.artist);
        parcel.writeString(this.numOfAlbum);
        parcel.writeString(this.Path);
        parcel.writeInt(this.count);
        parcel.writeTypedList(this.audioModelList);
    }
}
