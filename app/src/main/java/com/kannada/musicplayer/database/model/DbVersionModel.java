package com.demo.musicvideoplayer.database.model;

public class DbVersionModel {
    private int versionNumber = 0;

    public int getVersionNumber() {
        return this.versionNumber;
    }

    public void setVersionNumber(int i) {
        this.versionNumber = i;
    }

    public DbVersionModel(int i) {
        this.versionNumber = i;
    }
}
