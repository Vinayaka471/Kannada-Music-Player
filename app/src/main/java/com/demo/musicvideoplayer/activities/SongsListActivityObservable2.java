package com.demo.musicvideoplayer.activities;

import io.reactivex.functions.Consumer;

public final class SongsListActivityObservable2 implements Consumer {
    public final SongsListActivity songsListActivity1;

    public SongsListActivityObservable2(SongsListActivity songsListActivity) {
        this.songsListActivity1 = songsListActivity;
    }

    @Override
    public void accept(Object obj) throws Exception {
        this.songsListActivity1.SongsListActivityObservable2call((Boolean) obj);
    }
}
