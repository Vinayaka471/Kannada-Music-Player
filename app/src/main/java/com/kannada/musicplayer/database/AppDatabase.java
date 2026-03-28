package com.kannada.musicplayer.database;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.kannada.musicplayer.database.dao.AudioVideoDao;
import com.kannada.musicplayer.database.dao.DbVersionDAO;
import com.kannada.musicplayer.database.dao.FolderDao;
import com.kannada.musicplayer.database.model.DbVersionModel;
import com.kannada.musicplayer.utils.AppConstants;
import com.kannada.musicplayer.utils.AppPref;

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
