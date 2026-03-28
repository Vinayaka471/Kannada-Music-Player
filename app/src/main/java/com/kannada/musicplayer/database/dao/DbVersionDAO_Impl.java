package com.kannada.musicplayer.database.dao;

import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.SharedSQLiteStatement;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.kannada.musicplayer.database.model.DbVersionModel;
import java.util.Collections;
import java.util.List;

public final class DbVersionDAO_Impl implements DbVersionDAO {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter<DbVersionModel> __deletionAdapterOfDbVersionModel;
    private final EntityInsertionAdapter<DbVersionModel> __insertionAdapterOfDbVersionModel;
    private final SharedSQLiteStatement __preparedStmtOfDeleteAll;
    private final EntityDeletionOrUpdateAdapter<DbVersionModel> __updateAdapterOfDbVersionModel;

    public DbVersionDAO_Impl(RoomDatabase roomDatabase) {
        this.__db = roomDatabase;
        this.__insertionAdapterOfDbVersionModel = new EntityInsertionAdapter<DbVersionModel>(roomDatabase) {
            public String createQuery() {
                return "INSERT OR ABORT INTO `DbVersionModel` (`versionNumber`) VALUES (?)";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, DbVersionModel dbVersionModel) {
                supportSQLiteStatement.bindLong(1, (long) dbVersionModel.getVersionNumber());
            }
        };
        this.__deletionAdapterOfDbVersionModel = new EntityDeletionOrUpdateAdapter<DbVersionModel>(roomDatabase) {
            public String createQuery() {
                return "DELETE FROM `DbVersionModel` WHERE `versionNumber` = ?";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, DbVersionModel dbVersionModel) {
                supportSQLiteStatement.bindLong(1, (long) dbVersionModel.getVersionNumber());
            }
        };
        this.__updateAdapterOfDbVersionModel = new EntityDeletionOrUpdateAdapter<DbVersionModel>(roomDatabase) {
            public String createQuery() {
                return "UPDATE OR ABORT `DbVersionModel` SET `versionNumber` = ? WHERE `versionNumber` = ?";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, DbVersionModel dbVersionModel) {
                supportSQLiteStatement.bindLong(1, (long) dbVersionModel.getVersionNumber());
                supportSQLiteStatement.bindLong(2, (long) dbVersionModel.getVersionNumber());
            }
        };
        this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(roomDatabase) {
            public String createQuery() {
                return "DELETE FROM DbVersionModel";
            }
        };
    }

    public long insert(DbVersionModel dbVersionModel) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            long insertAndReturnId = this.__insertionAdapterOfDbVersionModel.insertAndReturnId(dbVersionModel);
            this.__db.setTransactionSuccessful();
            return insertAndReturnId;
        } finally {
            this.__db.endTransaction();
        }
    }

    public int delete(DbVersionModel dbVersionModel) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            int handle = this.__deletionAdapterOfDbVersionModel.handle(dbVersionModel) + 0;
            this.__db.setTransactionSuccessful();
            return handle;
        } finally {
            this.__db.endTransaction();
        }
    }

    public int update(DbVersionModel dbVersionModel) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            int handle = this.__updateAdapterOfDbVersionModel.handle(dbVersionModel) + 0;
            this.__db.setTransactionSuccessful();
            return handle;
        } finally {
            this.__db.endTransaction();
        }
    }

    public int deleteAll() {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfDeleteAll.acquire();
        this.__db.beginTransaction();
        try {
            int executeUpdateDelete = acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
            return executeUpdateDelete;
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDeleteAll.release(acquire);
        }
    }

    public static List<Class<?>> getRequiredConverters() {
        return Collections.emptyList();
    }
}
