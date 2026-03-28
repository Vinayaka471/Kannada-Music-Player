package com.kannada.musicplayer.activities;

import java.util.concurrent.Callable;

public final class SongsListActivityObservable1 implements Callable {
    public final SongsListActivity songsListActivity1;

    public SongsListActivityObservable1(SongsListActivity songsListActivity) {
        this.songsListActivity1 = songsListActivity;
    }

    @Override
    public Object call() throws Exception {
        return this.songsListActivity1.SongsListActivityObservable1call();
    }
}
