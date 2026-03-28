package com.kannada.musicplayer.database.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class AudioVideoModal implements Parcelable {
    public static final Creator<AudioVideoModal> CREATOR = new Creator<AudioVideoModal>() {
        @Override
        public AudioVideoModal createFromParcel(Parcel parcel) {
            return new AudioVideoModal(parcel);
        }

        @Override
        public AudioVideoModal[] newArray(int i) {
            return new AudioVideoModal[i];
        }
    };
    public String Album;
    public String Artist;
    public int AudioVideoOrder;
    public long Duration;
    public String Id;
    public String Name;
    public String RefId;
    public String Uri;

    public int describeContents() {
        return 0;
    }

    public AudioVideoModal() {
    }

    public AudioVideoModal(String str, String str2, String str3, long j, String str4, String str5, int i, String str6) {
        this.Id = str;
        this.Uri = str2;
        this.Name = str3;
        this.Duration = j;
        this.Artist = str4;
        this.Album = str5;
        this.AudioVideoOrder = i;
        this.RefId = str6;
    }

    public AudioVideoModal(String str, String str2, long j, String str3, String str4, int i) {
        this.Uri = str;
        this.Name = str2;
        this.Duration = j;
        this.Artist = str3;
        this.Album = str4;
        this.AudioVideoOrder = i;
    }

    public AudioVideoModal(AudioVideoModal audioVideoModal) {
        this.Id = audioVideoModal.getId();
        this.Uri = audioVideoModal.getUri();
        this.Name = audioVideoModal.getName();
        this.Duration = audioVideoModal.getDuration();
        this.Artist = audioVideoModal.getArtist();
        this.Album = audioVideoModal.getAlbum();
        this.AudioVideoOrder = audioVideoModal.getAudioVideoOrder();
        this.RefId = audioVideoModal.getRefId();
    }

    public AudioVideoModal(String str) {
        this.Uri = str;
    }

    protected AudioVideoModal(Parcel parcel) {
        this.Id = parcel.readString();
        this.Uri = parcel.readString();
        this.Name = parcel.readString();
        this.Duration = parcel.readLong();
        this.Artist = parcel.readString();
        this.Album = parcel.readString();
        this.AudioVideoOrder = parcel.readInt();
        this.RefId = parcel.readString();
    }

    public String getId() {
        return this.Id;
    }

    public void setId(String str) {
        this.Id = str;
    }

    public String getUri() {
        return this.Uri;
    }

    public void setUri(String str) {
        this.Uri = str;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String str) {
        this.Name = str;
    }

    public long getDuration() {
        return this.Duration;
    }

    public void setDuration(long j) {
        this.Duration = j;
    }

    public String getArtist() {
        return this.Artist;
    }

    public void setArtist(String str) {
        this.Artist = str;
    }

    public String getAlbum() {
        return this.Album;
    }

    public void setAlbum(String str) {
        this.Album = str;
    }

    public int getAudioVideoOrder() {
        return this.AudioVideoOrder;
    }

    public void setAudioVideoOrder(int i) {
        this.AudioVideoOrder = i;
    }

    public String getRefId() {
        return this.RefId;
    }

    public void setRefId(String str) {
        this.RefId = str;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.Uri, ((AudioVideoModal) obj).Uri);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.Uri});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.Id);
        parcel.writeString(this.Uri);
        parcel.writeString(this.Name);
        parcel.writeLong(this.Duration);
        parcel.writeString(this.Artist);
        parcel.writeString(this.Album);
        parcel.writeInt(this.AudioVideoOrder);
        parcel.writeString(this.RefId);
    }
}
