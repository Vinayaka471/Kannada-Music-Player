package com.demo.musicvideoplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoFolderModal implements Parcelable {
    public static final Creator<VideoFolderModal> CREATOR = new Creator<VideoFolderModal>() {
        @Override
        public VideoFolderModal createFromParcel(Parcel parcel) {
            return new VideoFolderModal(parcel);
        }

        @Override
        public VideoFolderModal[] newArray(int i) {
            return new VideoFolderModal[i];
        }
    };
    String aName;
    String aPath;
    String bucketId;
    int count = 0;
    String folderPath;
    boolean isSelected;
    List<VideoModal> videoList = new ArrayList();

    public int describeContents() {
        return 0;
    }

    public VideoFolderModal(String str) {
        this.aName = str;
    }

    public VideoFolderModal(String str, String str2) {
        this.aName = str;
        this.bucketId = str2;
    }

    protected VideoFolderModal(Parcel parcel) {
        boolean z = false;
        this.aPath = parcel.readString();
        this.aName = parcel.readString();
        this.count = parcel.readInt();
        this.bucketId = parcel.readString();
        this.folderPath = parcel.readString();
        this.isSelected = parcel.readByte() != 0 ? true : z;
        this.videoList = parcel.createTypedArrayList(VideoModal.CREATOR);
    }

    public String getFolderPath() {
        return this.folderPath;
    }

    public void setFolderPath(String str) {
        this.folderPath = str;
    }

    public String getaPath() {
        return this.aPath;
    }

    public void setaPath(String str) {
        this.aPath = str;
    }

    public String getaName() {
        return this.aName;
    }

    public void setaName(String str) {
        this.aName = str;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount() {
        this.count++;
    }

    public String getBucketId() {
        return this.bucketId;
    }

    public void setBucketId(String str) {
        this.bucketId = str;
    }

    public List<VideoModal> getVideoList() {
        return this.videoList;
    }

    public void setVideoList(List<VideoModal> list) {
        this.videoList = list;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean z) {
        this.isSelected = z;
    }

    public void addVideo(VideoModal videoModal) {
        this.videoList.add(videoModal);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VideoFolderModal videoFolderModal = (VideoFolderModal) obj;
        if (!Objects.equals(this.aName, videoFolderModal.aName) || !Objects.equals(this.bucketId, videoFolderModal.bucketId)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.aName, this.bucketId});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.aPath);
        parcel.writeString(this.aName);
        parcel.writeInt(this.count);
        parcel.writeString(this.bucketId);
        parcel.writeString(this.folderPath);
        parcel.writeByte(this.isSelected ? (byte) 1 : 0);
        parcel.writeTypedList(this.videoList);
    }
}
