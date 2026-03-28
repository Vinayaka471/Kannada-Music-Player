package com.kannada.musicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AudioFolderModal implements Parcelable {
    public static final Creator<AudioFolderModal> CREATOR = new Creator<AudioFolderModal>() {
        @Override
        public AudioFolderModal createFromParcel(Parcel parcel) {
            return new AudioFolderModal(parcel);
        }

        @Override
        public AudioFolderModal[] newArray(int i) {
            return new AudioFolderModal[i];
        }
    };
    String aPath;
    List<AudioModel> audioList = new ArrayList();
    String bucketId;
    String bucketName;
    int count = 0;

    public int describeContents() {
        return 0;
    }

    public AudioFolderModal(String str, String str2) {
        this.bucketName = str;
        this.bucketId = str2;
    }

    public AudioFolderModal(String str) {
        this.bucketName = str;
    }

    protected AudioFolderModal(Parcel parcel) {
        this.aPath = parcel.readString();
        this.bucketName = parcel.readString();
        this.count = parcel.readInt();
        this.bucketId = parcel.readString();
        this.audioList = parcel.createTypedArrayList(AudioModel.CREATOR);
    }

    public String getaPath() {
        return this.aPath;
    }

    public void setaPath(String str) {
        this.aPath = str;
    }

    public String getBucketName() {
        String str = this.bucketName;
        return str == null ? "" : str;
    }

    public void setBucketName(String str) {
        this.bucketName = str;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount() {
        this.count++;
    }

    public void setSongCount(int i) {
        this.count = i;
    }

    public String getBucketId() {
        return this.bucketId;
    }

    public void setBucketId(String str) {
        this.bucketId = str;
    }

    public List<AudioModel> getAudioList() {
        return this.audioList;
    }

    public void setAudioList(List<AudioModel> list) {
        this.audioList = list;
    }

    public void addAudio(AudioModel audioModel) {
        this.audioList.add(audioModel);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AudioFolderModal audioFolderModal = (AudioFolderModal) obj;
        if (!Objects.equals(this.bucketName, audioFolderModal.bucketName) || !Objects.equals(this.bucketId, audioFolderModal.bucketId)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.bucketName, this.bucketId});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.aPath);
        parcel.writeString(this.bucketName);
        parcel.writeInt(this.count);
        parcel.writeString(this.bucketId);
        parcel.writeTypedList(this.audioList);
    }
}
