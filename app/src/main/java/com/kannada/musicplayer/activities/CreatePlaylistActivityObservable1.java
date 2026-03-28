package com.demo.musicvideoplayer.activities;

import java.util.concurrent.Callable;

public final class CreatePlaylistActivityObservable1 implements Callable {
    public final CreatePlaylistActivity createPlaylistActivity1;

    public CreatePlaylistActivityObservable1(CreatePlaylistActivity createPlaylistActivity) {
        this.createPlaylistActivity1 = createPlaylistActivity;
    }

    @Override
    public Object call() throws Exception {
        return this.createPlaylistActivity1.CreatePlaylistActivityObservable1call();
    }
}
