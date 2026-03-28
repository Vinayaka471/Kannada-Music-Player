package com.kannada.musicplayer.model;

public class IconModel {
    String IconName;
    String Title;

    public IconModel(String str, String str2) {
        this.IconName = str;
        this.Title = str2;
    }

    public String getIconName() {
        return this.IconName;
    }

    public void setIconName(String str) {
        this.IconName = str;
    }

    public String getTitle() {
        return this.Title;
    }

    public void setTitle(String str) {
        this.Title = str;
    }
}
