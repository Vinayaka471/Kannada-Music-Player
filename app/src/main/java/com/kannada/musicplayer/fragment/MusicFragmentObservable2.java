package com.kannada.musicplayer.fragment;

import io.reactivex.functions.Consumer;

public final class MusicFragmentObservable2 implements Consumer {
    public final MusicFragment musicFragment1;

    public MusicFragmentObservable2(MusicFragment musicFragment) {
        this.musicFragment1 = musicFragment;
    }

    @Override
    public void accept(Object obj) throws Exception {
        this.musicFragment1.MusicFragmentObservable2call((Boolean) obj);
    }
}
