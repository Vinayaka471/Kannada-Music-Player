package com.demo.musicvideoplayer.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.exifinterface.media.ExifInterface;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AudioVideoDao_Impl implements AudioVideoDao {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter<AudioVideoModal> __deletionAdapterOfAudioVideoModal;
    private final EntityInsertionAdapter<AudioVideoModal> __insertionAdapterOfAudioVideoModal;
    private final SharedSQLiteStatement __preparedStmtOfDeleteAudioVideoByFolder;
    private final SharedSQLiteStatement __preparedStmtOfDeleteAudioVideoByUri;
    private final SharedSQLiteStatement __preparedStmtOfUpdateNameModel;
    private final SharedSQLiteStatement __preparedStmtOfUpdateUriModel;
    private final EntityDeletionOrUpdateAdapter<AudioVideoModal> __updateAdapterOfAudioVideoModal;

    public AudioVideoDao_Impl(RoomDatabase roomDatabase) {
        this.__db = roomDatabase;
        this.__insertionAdapterOfAudioVideoModal = new EntityInsertionAdapter<AudioVideoModal>(roomDatabase) {
            public String createQuery() {
                return "INSERT OR ABORT INTO `AudioVideoModal` (`Id`,`Uri`,`Name`,`Duration`,`Artist`,`Album`,`AudioVideoOrder`,`RefId`) VALUES (?,?,?,?,?,?,?,?)";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, AudioVideoModal audioVideoModal) {
                if (audioVideoModal.Id == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, audioVideoModal.Id);
                }
                if (audioVideoModal.Uri == null) {
                    supportSQLiteStatement.bindNull(2);
                } else {
                    supportSQLiteStatement.bindString(2, audioVideoModal.Uri);
                }
                if (audioVideoModal.Name == null) {
                    supportSQLiteStatement.bindNull(3);
                } else {
                    supportSQLiteStatement.bindString(3, audioVideoModal.Name);
                }
                supportSQLiteStatement.bindLong(4, audioVideoModal.Duration);
                if (audioVideoModal.Artist == null) {
                    supportSQLiteStatement.bindNull(5);
                } else {
                    supportSQLiteStatement.bindString(5, audioVideoModal.Artist);
                }
                if (audioVideoModal.Album == null) {
                    supportSQLiteStatement.bindNull(6);
                } else {
                    supportSQLiteStatement.bindString(6, audioVideoModal.Album);
                }
                supportSQLiteStatement.bindLong(7, (long) audioVideoModal.AudioVideoOrder);
                if (audioVideoModal.RefId == null) {
                    supportSQLiteStatement.bindNull(8);
                } else {
                    supportSQLiteStatement.bindString(8, audioVideoModal.RefId);
                }
            }
        };
        this.__deletionAdapterOfAudioVideoModal = new EntityDeletionOrUpdateAdapter<AudioVideoModal>(roomDatabase) {
            public String createQuery() {
                return "DELETE FROM `AudioVideoModal` WHERE `Id` = ?";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, AudioVideoModal audioVideoModal) {
                if (audioVideoModal.Id == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, audioVideoModal.Id);
                }
            }
        };
        this.__updateAdapterOfAudioVideoModal = new EntityDeletionOrUpdateAdapter<AudioVideoModal>(roomDatabase) {
            public String createQuery() {
                return "UPDATE OR ABORT `AudioVideoModal` SET `Id` = ?,`Uri` = ?,`Name` = ?,`Duration` = ?,`Artist` = ?,`Album` = ?,`AudioVideoOrder` = ?,`RefId` = ? WHERE `Id` = ?";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, AudioVideoModal audioVideoModal) {
                if (audioVideoModal.Id == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, audioVideoModal.Id);
                }
                if (audioVideoModal.Uri == null) {
                    supportSQLiteStatement.bindNull(2);
                } else {
                    supportSQLiteStatement.bindString(2, audioVideoModal.Uri);
                }
                if (audioVideoModal.Name == null) {
                    supportSQLiteStatement.bindNull(3);
                } else {
                    supportSQLiteStatement.bindString(3, audioVideoModal.Name);
                }
                supportSQLiteStatement.bindLong(4, audioVideoModal.Duration);
                if (audioVideoModal.Artist == null) {
                    supportSQLiteStatement.bindNull(5);
                } else {
                    supportSQLiteStatement.bindString(5, audioVideoModal.Artist);
                }
                if (audioVideoModal.Album == null) {
                    supportSQLiteStatement.bindNull(6);
                } else {
                    supportSQLiteStatement.bindString(6, audioVideoModal.Album);
                }
                supportSQLiteStatement.bindLong(7, (long) audioVideoModal.AudioVideoOrder);
                if (audioVideoModal.RefId == null) {
                    supportSQLiteStatement.bindNull(8);
                } else {
                    supportSQLiteStatement.bindString(8, audioVideoModal.RefId);
                }
                if (audioVideoModal.Id == null) {
                    supportSQLiteStatement.bindNull(9);
                } else {
                    supportSQLiteStatement.bindString(9, audioVideoModal.Id);
                }
            }
        };
        this.__preparedStmtOfDeleteAudioVideoByFolder = new SharedSQLiteStatement(roomDatabase) {
            public String createQuery() {
                return "Delete from AudioVideoModal Where RefId=?";
            }
        };
        this.__preparedStmtOfDeleteAudioVideoByUri = new SharedSQLiteStatement(roomDatabase) {
            public String createQuery() {
                return "Delete from AudioVideoModal Where Uri=?";
            }
        };
        this.__preparedStmtOfUpdateUriModel = new SharedSQLiteStatement(roomDatabase) {
            public String createQuery() {
                return "Update AudioVideoModal SET Uri =? WHERE Uri=?";
            }
        };
        this.__preparedStmtOfUpdateNameModel = new SharedSQLiteStatement(roomDatabase) {
            public String createQuery() {
                return "Update AudioVideoModal SET Name =? WHERE Uri=?";
            }
        };
    }

    public void InsertAudioVideo(AudioVideoModal audioVideoModal) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfAudioVideoModal.insert(audioVideoModal);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    public void DeleteAudioVideo(AudioVideoModal audioVideoModal) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfAudioVideoModal.handle(audioVideoModal);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    public void UpdateAudioVideo(AudioVideoModal audioVideoModal) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfAudioVideoModal.handle(audioVideoModal);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    public void DeleteAudioVideoByFolder(String str) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfDeleteAudioVideoByFolder.acquire();
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDeleteAudioVideoByFolder.release(acquire);
        }
    }

    public void DeleteAudioVideoByUri(String str) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfDeleteAudioVideoByUri.acquire();
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfDeleteAudioVideoByUri.release(acquire);
        }
    }

    public void UpdateUriModel(String str, String str2) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfUpdateUriModel.acquire();
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        if (str2 == null) {
            acquire.bindNull(2);
        } else {
            acquire.bindString(2, str2);
        }
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfUpdateUriModel.release(acquire);
        }
    }

    public void UpdateNameModel(String str, String str2) {
        this.__db.assertNotSuspendingTransaction();
        SupportSQLiteStatement acquire = this.__preparedStmtOfUpdateNameModel.acquire();
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        if (str2 == null) {
            acquire.bindNull(2);
        } else {
            acquire.bindString(2, str2);
        }
        this.__db.beginTransaction();
        try {
            acquire.executeUpdateDelete();
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
            this.__preparedStmtOfUpdateNameModel.release(acquire);
        }
    }

    public List<AudioVideoModal> GetAudioVideoListByFolderID(String str) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("Select * From AudioVideoModal Where RefId=? Order By AudioVideoOrder", 1);
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, (CancellationSignal) null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "Id");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "Uri");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "Name");
            int columnIndexOrThrow4 = CursorUtil.getColumnIndexOrThrow(query, "Duration");
            int columnIndexOrThrow5 = CursorUtil.getColumnIndexOrThrow(query, ExifInterface.TAG_ARTIST);
            int columnIndexOrThrow6 = CursorUtil.getColumnIndexOrThrow(query, "Album");
            int columnIndexOrThrow7 = CursorUtil.getColumnIndexOrThrow(query, "AudioVideoOrder");
            int columnIndexOrThrow8 = CursorUtil.getColumnIndexOrThrow(query, "RefId");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                AudioVideoModal audioVideoModal = new AudioVideoModal();
                if (query.isNull(columnIndexOrThrow)) {
                    audioVideoModal.Id = null;
                } else {
                    audioVideoModal.Id = query.getString(columnIndexOrThrow);
                }
                if (query.isNull(columnIndexOrThrow2)) {
                    audioVideoModal.Uri = null;
                } else {
                    audioVideoModal.Uri = query.getString(columnIndexOrThrow2);
                }
                if (query.isNull(columnIndexOrThrow3)) {
                    audioVideoModal.Name = null;
                } else {
                    audioVideoModal.Name = query.getString(columnIndexOrThrow3);
                }
                audioVideoModal.Duration = query.getLong(columnIndexOrThrow4);
                if (query.isNull(columnIndexOrThrow5)) {
                    audioVideoModal.Artist = null;
                } else {
                    audioVideoModal.Artist = query.getString(columnIndexOrThrow5);
                }
                if (query.isNull(columnIndexOrThrow6)) {
                    audioVideoModal.Album = null;
                } else {
                    audioVideoModal.Album = query.getString(columnIndexOrThrow6);
                }
                audioVideoModal.AudioVideoOrder = query.getInt(columnIndexOrThrow7);
                if (query.isNull(columnIndexOrThrow8)) {
                    audioVideoModal.RefId = null;
                } else {
                    audioVideoModal.RefId = query.getString(columnIndexOrThrow8);
                }
                arrayList.add(audioVideoModal);
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    public List<String> GetAudioVideoUriListByFolderID(String str) {
        String str2;
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("Select Uri From AudioVideoModal Where RefId=?", 1);
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, (CancellationSignal) null);
        try {
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                if (query.isNull(0)) {
                    str2 = null;
                } else {
                    str2 = query.getString(0);
                }
                arrayList.add(str2);
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    public int GetAudioVideoListSize(String str) {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("Select count(Id) from AudioVideoModal where RefId=?", 1);
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        this.__db.assertNotSuspendingTransaction();
        int i = 0;
        Cursor query = DBUtil.query(this.__db, acquire, false, (CancellationSignal) null);
        try {
            if (query.moveToFirst()) {
                i = query.getInt(0);
            }
            return i;
        } finally {
            query.close();
            acquire.release();
        }
    }

    public static List<Class<?>> getRequiredConverters() {
        return Collections.emptyList();
    }
}
