package com.demo.musicvideoplayer.activities;

import io.reactivex.functions.Consumer;

public final class FavouriteListActivityObservable2 implements Consumer {
    public final FavouriteListActivity favouriteListActivity1;

    public FavouriteListActivityObservable2(FavouriteListActivity favouriteListActivity) {
        this.favouriteListActivity1 = favouriteListActivity;
    }

    @Override
    public void accept(Object obj) throws Exception {
        this.favouriteListActivity1.FavouriteListActivityObservable2call((Boolean) obj);
    }
}
