package com.demo.musicvideoplayer.database;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.demo.musicvideoplayer.database.dao.AudioVideoDao;
import com.demo.musicvideoplayer.database.dao.DbVersionDAO;
import com.demo.musicvideoplayer.database.dao.FolderDao;
import com.demo.musicvideoplayer.database.model.DbVersionModel;
import com.demo.musicvideoplayer.utils.AppConstants;
import com.demo.musicvideoplayer.utils.AppPref;

public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase appDatabase;

    public abstract AudioVideoDao audioVideoDao();

    public abstract DbVersionDAO dbVersionDAO();

    public abstract FolderDao folderDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (appDatabase == null) {
            appDatabase = buildDatabaseInstance(context);
            if (!AppPref.isDbVersionAdded()) {
                addDbVersion();
                AppPref.setIsDbversionAdded(true);
            }
        }
        return appDatabase;
    }

    private static AppDatabase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, AppConstants.APP_DB_NAME).allowMainThreadQueries().build();
    }

    private static void addDbVersion() {
        appDatabase.dbVersionDAO().deleteAll();
        appDatabase.dbVersionDAO().insert(new DbVersionModel(1));
    }
}
