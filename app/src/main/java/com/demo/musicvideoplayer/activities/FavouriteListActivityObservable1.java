package com.demo.musicvideoplayer.activities;

import java.util.concurrent.Callable;

public final class FavouriteListActivityObservable1 implements Callable {
    public final FavouriteListActivity favouriteListActivity1;

    public FavouriteListActivityObservable1(FavouriteListActivity favouriteListActivity) {
        this.favouriteListActivity1 = favouriteListActivity;
    }

    @Override
    public Object call() throws Exception {
        return this.favouriteListActivity1.FavouriteListActivityObservable1call();
    }
}
