package com.demo.musicvideoplayer.activities;

import io.reactivex.functions.Consumer;

public final class AudioVideoListActivityObservable2 implements Consumer {
    public final AudioVideoListActivity audioVideoListActivity1;

    public AudioVideoListActivityObservable2(AudioVideoListActivity audioVideoListActivity) {
        this.audioVideoListActivity1 = audioVideoListActivity;
    }

    @Override
    public void accept(Object obj) throws Exception {
        this.audioVideoListActivity1.AudioVideoListActivityObservable2call((Boolean) obj);
    }
}
