package com.demo.musicvideoplayer.activities;

import java.util.concurrent.Callable;

public final class MainActivityObservable1 implements Callable {
    public final MainActivity mainActivity1;

    public MainActivityObservable1(MainActivity mainActivity) {
        this.mainActivity1 = mainActivity;
    }

    @Override
    public Object call() {
        try {
            return this.mainActivity1.MainActivityObservable1call();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
}
