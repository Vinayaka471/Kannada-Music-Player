package com.demo.musicvideoplayer.database;

import androidx.exifinterface.media.ExifInterface;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomMasterTable;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.demo.musicvideoplayer.database.dao.AudioVideoDao;
import com.demo.musicvideoplayer.database.dao.AudioVideoDao_Impl;
import com.demo.musicvideoplayer.database.dao.DbVersionDAO;
import com.demo.musicvideoplayer.database.dao.DbVersionDAO_Impl;
import com.demo.musicvideoplayer.database.dao.FolderDao;
import com.demo.musicvideoplayer.database.dao.FolderDao_Impl;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AppDatabase_Impl extends AppDatabase {
    private volatile AudioVideoDao _audioVideoDao;
    private volatile DbVersionDAO _dbVersionDAO;
    private volatile FolderDao _folderDao;

    
    public SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration databaseConfiguration) {
        return databaseConfiguration.sqliteOpenHelperFactory.create(SupportSQLiteOpenHelper.Configuration.builder(databaseConfiguration.context).name(databaseConfiguration.name).callback(new RoomOpenHelper(databaseConfiguration, new RoomOpenHelper.Delegate(1) {
            @Override
            public void onPostMigrate(SupportSQLiteDatabase supportSQLiteDatabase) {
            }

            @Override
            public void createAllTables(SupportSQLiteDatabase supportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `DbVersionModel` (`versionNumber` INTEGER NOT NULL, PRIMARY KEY(`versionNumber`))");
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `FolderModal` (`Id` TEXT NOT NULL, `FolderName` TEXT, PRIMARY KEY(`Id`))");
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `AudioVideoModal` (`Id` TEXT NOT NULL, `Uri` TEXT, `Name` TEXT, `Duration` INTEGER NOT NULL, `Artist` TEXT, `Album` TEXT, `AudioVideoOrder` INTEGER NOT NULL, `RefId` TEXT, PRIMARY KEY(`Id`))");
                supportSQLiteDatabase.execSQL(RoomMasterTable.CREATE_QUERY);
                supportSQLiteDatabase.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '01b30cb05949ec95f820c9f4bc711fcd')");
            }

            @Override
            public void dropAllTables(SupportSQLiteDatabase supportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `DbVersionModel`");
                supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `FolderModal`");
                supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS `AudioVideoModal`");
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onDestructiveMigration(supportSQLiteDatabase);
                    }
                }
            }

            
            @Override
            public void onCreate(SupportSQLiteDatabase supportSQLiteDatabase) {
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onCreate(supportSQLiteDatabase);
                    }
                }
            }

            @Override
            public void onOpen(SupportSQLiteDatabase supportSQLiteDatabase) {
                SupportSQLiteDatabase unused = AppDatabase_Impl.this.mDatabase = supportSQLiteDatabase;
                AppDatabase_Impl.this.internalInitInvalidationTracker(supportSQLiteDatabase);
                if (AppDatabase_Impl.this.mCallbacks != null) {
                    int size = AppDatabase_Impl.this.mCallbacks.size();
                    for (int i = 0; i < size; i++) {
                        ((RoomDatabase.Callback) AppDatabase_Impl.this.mCallbacks.get(i)).onOpen(supportSQLiteDatabase);
                    }
                }
            }

            @Override
            public void onPreMigrate(SupportSQLiteDatabase supportSQLiteDatabase) {
                DBUtil.dropFtsSyncTriggers(supportSQLiteDatabase);
            }

            @Override
            public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase supportSQLiteDatabase) {
                SupportSQLiteDatabase supportSQLiteDatabase2 = supportSQLiteDatabase;
                HashMap hashMap = new HashMap(1);
                hashMap.put("versionNumber", new TableInfo.Column("versionNumber", "INTEGER", true, 1, (String) null, 1));
                TableInfo tableInfo = new TableInfo("DbVersionModel", hashMap, new HashSet(0), new HashSet(0));
                TableInfo read = TableInfo.read(supportSQLiteDatabase2, "DbVersionModel");
                if (!tableInfo.equals(read)) {
                    return new RoomOpenHelper.ValidationResult(false, "DbVersionModel(com.demo.musicvideoplayer.database.model.DbVersionModel).\n Expected:\n" + tableInfo + "\n Found:\n" + read);
                }
                HashMap hashMap2 = new HashMap(2);
                hashMap2.put("Id", new TableInfo.Column("Id", "TEXT", true, 1, (String) null, 1));
                hashMap2.put("FolderName", new TableInfo.Column("FolderName", "TEXT", false, 0, (String) null, 1));
                TableInfo tableInfo2 = new TableInfo("FolderModal", hashMap2, new HashSet(0), new HashSet(0));
                TableInfo read2 = TableInfo.read(supportSQLiteDatabase2, "FolderModal");
                if (!tableInfo2.equals(read2)) {
                    return new RoomOpenHelper.ValidationResult(false, "FolderModal(com.demo.musicvideoplayer.database.model.FolderModal).\n Expected:\n" + tableInfo2 + "\n Found:\n" + read2);
                }
                HashMap hashMap3 = new HashMap(8);
                hashMap3.put("Id", new TableInfo.Column("Id", "TEXT", true, 1, (String) null, 1));
                hashMap3.put("Uri", new TableInfo.Column("Uri", "TEXT", false, 0, (String) null, 1));
                hashMap3.put("Name", new TableInfo.Column("Name", "TEXT", false, 0, (String) null, 1));
                hashMap3.put("Duration", new TableInfo.Column("Duration", "INTEGER", true, 0, (String) null, 1));
                hashMap3.put(ExifInterface.TAG_ARTIST, new TableInfo.Column(ExifInterface.TAG_ARTIST, "TEXT", false, 0, (String) null, 1));
                hashMap3.put("Album", new TableInfo.Column("Album", "TEXT", false, 0, (String) null, 1));
                hashMap3.put("AudioVideoOrder", new TableInfo.Column("AudioVideoOrder", "INTEGER", true, 0, (String) null, 1));
                hashMap3.put("RefId", new TableInfo.Column("RefId", "TEXT", false, 0, (String) null, 1));
                TableInfo tableInfo3 = new TableInfo("AudioVideoModal", hashMap3, new HashSet(0), new HashSet(0));
                TableInfo read3 = TableInfo.read(supportSQLiteDatabase2, "AudioVideoModal");
                if (tableInfo3.equals(read3)) {
                    return new RoomOpenHelper.ValidationResult(true, (String) null);
                }
                return new RoomOpenHelper.ValidationResult(false, "AudioVideoModal(com.demo.musicvideoplayer.database.model.AudioVideoModal).\n Expected:\n" + tableInfo3 + "\n Found:\n" + read3);
            }
        }, "01b30cb05949ec95f820c9f4bc711fcd", "809af08c3b70f611bb3a834eec397e97")).build());
    }

    
    public InvalidationTracker createInvalidationTracker() {
        return new InvalidationTracker(this, new HashMap(0), new HashMap(0), "DbVersionModel", "FolderModal", "AudioVideoModal");
    }

    public void clearAllTables() {
        super.assertNotMainThread();
        SupportSQLiteDatabase writableDatabase = super.getOpenHelper().getWritableDatabase();
        try {
            super.beginTransaction();
            writableDatabase.execSQL("DELETE FROM `DbVersionModel`");
            writableDatabase.execSQL("DELETE FROM `FolderModal`");
            writableDatabase.execSQL("DELETE FROM `AudioVideoModal`");
            super.setTransactionSuccessful();
        } finally {
            super.endTransaction();
            writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close();
            if (!writableDatabase.inTransaction()) {
                writableDatabase.execSQL("VACUUM");
            }
        }
    }

    
    public Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
        HashMap hashMap = new HashMap();
        hashMap.put(DbVersionDAO.class, DbVersionDAO_Impl.getRequiredConverters());
        hashMap.put(FolderDao.class, FolderDao_Impl.getRequiredConverters());
        hashMap.put(AudioVideoDao.class, AudioVideoDao_Impl.getRequiredConverters());
        return hashMap;
    }

    public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
        return new HashSet();
    }

    public List<Migration> getAutoMigrations(Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> map) {
        return Arrays.asList(new Migration[0]);
    }

    public DbVersionDAO dbVersionDAO() {
        DbVersionDAO dbVersionDAO;
        if (this._dbVersionDAO != null) {
            return this._dbVersionDAO;
        }
        synchronized (this) {
            if (this._dbVersionDAO == null) {
                this._dbVersionDAO = new DbVersionDAO_Impl(this);
            }
            dbVersionDAO = this._dbVersionDAO;
        }
        return dbVersionDAO;
    }

    public FolderDao folderDao() {
        FolderDao folderDao;
        if (this._folderDao != null) {
            return this._folderDao;
        }
        synchronized (this) {
            if (this._folderDao == null) {
                this._folderDao = new FolderDao_Impl(this);
            }
            folderDao = this._folderDao;
        }
        return folderDao;
    }

    public AudioVideoDao audioVideoDao() {
        AudioVideoDao audioVideoDao;
        if (this._audioVideoDao != null) {
            return this._audioVideoDao;
        }
        synchronized (this) {
            if (this._audioVideoDao == null) {
                this._audioVideoDao = new AudioVideoDao_Impl(this);
            }
            audioVideoDao = this._audioVideoDao;
        }
        return audioVideoDao;
    }
}
