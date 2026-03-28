package com.demo.musicvideoplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.demo.musicvideoplayer.utils.AppConstants;
import java.util.Objects;

public class AudioModel implements Parcelable {
    public static final Creator<AudioModel> CREATOR = new Creator<AudioModel>() {
        @Override
        public AudioModel createFromParcel(Parcel parcel) {
            return new AudioModel(parcel);
        }

        @Override
        public AudioModel[] newArray(int i) {
            return new AudioModel[i];
        }
    };
    long CreationDate;
    String albumId;
    String albumName;
    String artist;
    long duration;
    boolean isPlayed;
    long longSize;
    String name;
    int order;
    String path;
    String size;
    String time;
    String type;
    String uri;

    public int describeContents() {
        return 0;
    }

    public AudioModel() {
    }

    public AudioModel(String str) {
        this.path = str;
    }

    public AudioModel(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, boolean z, long j, int i) {
        this.name = str;
        this.path = str2;
        this.size = str3;
        this.time = str4;
        this.type = str5;
        this.uri = str6;
        this.artist = str7;
        this.albumId = str8;
        this.albumName = str9;
        this.isPlayed = z;
        this.duration = j;
        this.order = i;
    }

    public AudioModel(String str, String str2, String str3, long j, String str4, String str5, String str6, String str7, String str8, String str9, boolean z, long j2, long j3) {
        this.name = str;
        this.path = str2;
        this.size = str3;
        this.longSize = j;
        this.time = str4;
        this.type = str5;
        this.uri = str6;
        this.artist = str7;
        this.albumId = str8;
        this.albumName = str9;
        this.isPlayed = z;
        this.duration = j2;
        this.CreationDate = j3;
    }

    protected AudioModel(Parcel parcel) {
        this.name = parcel.readString();
        this.path = parcel.readString();
        this.size = parcel.readString();
        this.longSize = parcel.readLong();
        this.time = parcel.readString();
        this.type = parcel.readString();
        this.uri = parcel.readString();
        this.artist = parcel.readString();
        this.albumId = parcel.readString();
        this.albumName = parcel.readString();
        this.isPlayed = parcel.readByte() != 0;
        this.duration = parcel.readLong();
        this.order = parcel.readInt();
        this.CreationDate = parcel.readLong();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String str) {
        this.path = str;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String str) {
        this.size = str;
    }

    public long getLongSize() {
        return this.longSize;
    }

    public void setLongSize(long j) {
        this.longSize = j;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String str) {
        this.time = str;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String str) {
        this.type = str;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String str) {
        this.uri = str;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(String str) {
        this.artist = str;
    }

    public String getAlbumId() {
        return this.albumId;
    }

    public void setAlbumId(String str) {
        this.albumId = str;
    }

    public boolean isPlayed() {
        return this.isPlayed;
    }

    public void setPlayed(boolean z) {
        this.isPlayed = z;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long j) {
        this.duration = j;
    }

    public String getAlbumName() {
        return this.albumName;
    }

    public void setAlbumName(String str) {
        this.albumName = str;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int i) {
        this.order = i;
    }

    public long getCreationDate() {
        return this.CreationDate;
    }

    public void setCreationDate(long j) {
        this.CreationDate = j;
    }

    public String timeSet() {
        return AppConstants.formatTime(this.duration);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.path, ((AudioModel) obj).path);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.path});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeString(this.path);
        parcel.writeString(this.size);
        parcel.writeLong(this.longSize);
        parcel.writeString(this.time);
        parcel.writeString(this.type);
        parcel.writeString(this.uri);
        parcel.writeString(this.artist);
        parcel.writeString(this.albumId);
        parcel.writeString(this.albumName);
        parcel.writeByte(this.isPlayed ? (byte) 1 : 0);
        parcel.writeLong(this.duration);
        parcel.writeInt(this.order);
        parcel.writeLong(this.CreationDate);
    }
}
