package com.demo.musicvideoplayer.database.dao;

import com.demo.musicvideoplayer.database.model.DbVersionModel;

public interface DbVersionDAO {
    int delete(DbVersionModel dbVersionModel);

    int deleteAll();

    long insert(DbVersionModel dbVersionModel);

    int update(DbVersionModel dbVersionModel);
}
