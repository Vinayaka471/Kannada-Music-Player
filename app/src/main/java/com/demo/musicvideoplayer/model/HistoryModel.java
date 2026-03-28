package com.demo.musicvideoplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import java.util.Objects;

public class HistoryModel implements Parcelable {
    public static final Creator<HistoryModel> CREATOR = new Creator<HistoryModel>() {
        @Override
        public HistoryModel createFromParcel(Parcel parcel) {
            return new HistoryModel(parcel);
        }

        @Override
        public HistoryModel[] newArray(int i) {
            return new HistoryModel[i];
        }
    };
    long Date;
    AudioVideoModal audioVideoModal;

    public int describeContents() {
        return 0;
    }

    public HistoryModel(AudioVideoModal audioVideoModal2, long j) {
        this.audioVideoModal = audioVideoModal2;
        this.Date = j;
    }

    public HistoryModel(AudioVideoModal audioVideoModal2) {
        this.audioVideoModal = audioVideoModal2;
    }

    protected HistoryModel(Parcel parcel) {
        this.audioVideoModal = (AudioVideoModal) parcel.readParcelable(AudioVideoModal.class.getClassLoader());
        this.Date = parcel.readLong();
    }

    public AudioVideoModal getAudioVideoModal() {
        return this.audioVideoModal;
    }

    public void setAudioVideoModal(AudioVideoModal audioVideoModal2) {
        this.audioVideoModal = audioVideoModal2;
    }

    public long getDate() {
        return this.Date;
    }

    public void setDate(long j) {
        this.Date = j;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.audioVideoModal, ((HistoryModel) obj).audioVideoModal);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.audioVideoModal});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.audioVideoModal, i);
        parcel.writeLong(this.Date);
    }
}
