package com.kannada.musicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.kannada.musicplayer.utils.AppConstants;
import java.util.Objects;

public class VideoModal implements Parcelable {
    public static final Creator<VideoModal> CREATOR = new Creator<VideoModal>() {
        @Override
        public VideoModal createFromParcel(Parcel parcel) {
            return new VideoModal(parcel);
        }

        @Override
        public VideoModal[] newArray(int i) {
            return new VideoModal[i];
        }
    };
    String BucketId;
    String BucketName;
    String aName;
    String aPath;
    long creationDate;
    long duration;
    long size;
    long time;

    public int describeContents() {
        return 0;
    }

    public VideoModal(String str, String str2, long j, long j2, long j3, long j4) {
        this.aPath = str;
        this.aName = str2;
        this.size = j;
        this.time = j2;
        this.duration = j3;
        this.creationDate = j4;
    }

    public VideoModal(String str, String str2, long j, long j2, long j3, long j4, String str3, String str4) {
        this.aPath = str;
        this.aName = str2;
        this.size = j;
        this.time = j2;
        this.duration = j3;
        this.creationDate = j4;
        this.BucketId = str3;
        this.BucketName = str4;
    }

    public VideoModal() {
    }

    public VideoModal(String str) {
        this.aPath = str;
    }

    public VideoModal(String str, String str2) {
        this.aPath = str;
        this.BucketId = str2;
    }

    protected VideoModal(Parcel parcel) {
        this.aPath = parcel.readString();
        this.aName = parcel.readString();
        this.size = parcel.readLong();
        this.time = parcel.readLong();
        this.duration = parcel.readLong();
        this.creationDate = parcel.readLong();
        this.BucketId = parcel.readString();
        this.BucketName = parcel.readString();
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

    public long getSize() {
        return this.size;
    }

    public void setSize(long j) {
        this.size = j;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long j) {
        this.time = j;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long j) {
        this.duration = j;
    }

    public long getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(long j) {
        this.creationDate = j;
    }

    public String getBucketId() {
        return this.BucketId;
    }

    public void setBucketId(String str) {
        this.BucketId = str;
    }

    public String getBucketName() {
        return this.BucketName;
    }

    public void setBucketName(String str) {
        this.BucketName = str;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VideoModal videoModal = (VideoModal) obj;
        if (!Objects.equals(this.aPath, videoModal.aPath) || !Objects.equals(this.BucketId, videoModal.BucketId)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.aPath, this.BucketId});
    }

    public String sizeSet() {
        return AppConstants.formatSize(this.size);
    }

    public String timeSet() {
        return AppConstants.formatTime(this.duration);
    }

    public String dateSet() {
        return AppConstants.formattedDate(this.time, AppConstants.DATE_FORMAT);
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.aPath);
        parcel.writeString(this.aName);
        parcel.writeLong(this.size);
        parcel.writeLong(this.time);
        parcel.writeLong(this.duration);
        parcel.writeLong(this.creationDate);
        parcel.writeString(this.BucketId);
        parcel.writeString(this.BucketName);
    }
}
