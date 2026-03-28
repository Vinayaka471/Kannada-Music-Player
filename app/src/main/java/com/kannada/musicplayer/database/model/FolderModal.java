package com.kannada.musicplayer.database.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class FolderModal implements Parcelable {
    public static final Creator<FolderModal> CREATOR = new Creator<FolderModal>() {
        @Override
        public FolderModal createFromParcel(Parcel parcel) {
            return new FolderModal(parcel);
        }

        @Override
        public FolderModal[] newArray(int i) {
            return new FolderModal[i];
        }
    };
    public String FolderName;
    public String Id;

    public int describeContents() {
        return 0;
    }

    public FolderModal(String str, String str2) {
        this.Id = str;
        this.FolderName = str2;
    }

    public FolderModal() {
    }

    protected FolderModal(Parcel parcel) {
        this.Id = parcel.readString();
        this.FolderName = parcel.readString();
    }

    public String getId() {
        return this.Id;
    }

    public void setId(String str) {
        this.Id = str;
    }

    public String getFolderName() {
        return this.FolderName;
    }

    public void setFolderName(String str) {
        this.FolderName = str;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.Id, ((FolderModal) obj).Id);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.Id});
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.Id);
        parcel.writeString(this.FolderName);
    }
}
