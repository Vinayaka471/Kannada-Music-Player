package com.demo.musicvideoplayer.activities;

import java.util.concurrent.Callable;

public final class PlayListItemsActivityObservable1 implements Callable {
    public final PlayListItemsActivity playListItemsActivity1;

    public PlayListItemsActivityObservable1(PlayListItemsActivity playListItemsActivity) {
        this.playListItemsActivity1 = playListItemsActivity;
    }

    @Override
    public Object call() throws Exception {
        return this.playListItemsActivity1.PlayListItemsActivityObservable1call();
    }
}
