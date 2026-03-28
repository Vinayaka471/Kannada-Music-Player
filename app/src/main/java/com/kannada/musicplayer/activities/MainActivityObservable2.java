package com.demo.musicvideoplayer.activities;

import io.reactivex.functions.Consumer;

public final class MainActivityObservable2 implements Consumer {
    public final MainActivity mainActivity1;

    public MainActivityObservable2(MainActivity mainActivity) {
        this.mainActivity1 = mainActivity;
    }

    @Override
    public void accept(Object obj) throws Exception {
        this.mainActivity1.MainActivityObservable2call((Boolean) obj);
    }
}
