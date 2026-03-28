package com.kannada.musicplayer.activities;

import java.util.concurrent.Callable;

public final class AudioVideoListActivityObservable1 implements Callable {
    public final AudioVideoListActivity audioVideoListActivity1;

    public AudioVideoListActivityObservable1(AudioVideoListActivity audioVideoListActivity) {
        this.audioVideoListActivity1 = audioVideoListActivity;
    }

    @Override
    public Object call() throws Exception {
        return this.audioVideoListActivity1.AudioVideoListActivityObservable1call();
    }
}
