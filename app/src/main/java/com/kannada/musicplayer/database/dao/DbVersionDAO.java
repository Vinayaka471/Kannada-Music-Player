package com.kannada.musicplayer.database.dao;

import com.kannada.musicplayer.database.model.DbVersionModel;

public interface DbVersionDAO {
    int delete(DbVersionModel dbVersionModel);

    int deleteAll();

    long insert(DbVersionModel dbVersionModel);

    int update(DbVersionModel dbVersionModel);
}
