package com.kannada.musicplayer.activities;

import io.reactivex.functions.Consumer;

public final class PlayListItemsActivityObservable2 implements Consumer {
    public final PlayListItemsActivity playListItemsActivity1;

    public PlayListItemsActivityObservable2(PlayListItemsActivity playListItemsActivity) {
        this.playListItemsActivity1 = playListItemsActivity;
    }

    @Override
    public void accept(Object obj) throws Exception {
        this.playListItemsActivity1.PlayListItemsActivityObservable2call((Boolean) obj);
    }
}
