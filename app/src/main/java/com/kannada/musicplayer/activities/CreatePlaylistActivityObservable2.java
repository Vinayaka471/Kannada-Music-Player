package com.kannada.musicplayer.activities;

import io.reactivex.functions.Consumer;

public final class CreatePlaylistActivityObservable2 implements Consumer {
    public final CreatePlaylistActivity createPlaylistActivity1;

    public CreatePlaylistActivityObservable2(CreatePlaylistActivity createPlaylistActivity) {
        this.createPlaylistActivity1 = createPlaylistActivity;
    }

    @Override
    public void accept(Object obj) throws Exception {
        this.createPlaylistActivity1.CreatePlaylistActivityObservable2call((Boolean) obj);
    }
}
