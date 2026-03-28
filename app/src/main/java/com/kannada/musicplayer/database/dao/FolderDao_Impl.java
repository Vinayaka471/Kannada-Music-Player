package com.kannada.musicplayer.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.exifinterface.media.ExifInterface;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.kannada.musicplayer.database.model.FolderModal;
import com.kannada.musicplayer.model.CombineFolderModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FolderDao_Impl implements FolderDao {
    private final RoomDatabase __db;
    private final EntityDeletionOrUpdateAdapter<FolderModal> __deletionAdapterOfFolderModal;
    private final EntityInsertionAdapter<FolderModal> __insertionAdapterOfFolderModal;
    private final EntityDeletionOrUpdateAdapter<FolderModal> __updateAdapterOfFolderModal;

    public FolderDao_Impl(RoomDatabase roomDatabase) {
        this.__db = roomDatabase;
        this.__insertionAdapterOfFolderModal = new EntityInsertionAdapter<FolderModal>(roomDatabase) {
            public String createQuery() {
                return "INSERT OR ABORT INTO `FolderModal` (`Id`,`FolderName`) VALUES (?,?)";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, FolderModal folderModal) {
                if (folderModal.Id == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, folderModal.Id);
                }
                if (folderModal.FolderName == null) {
                    supportSQLiteStatement.bindNull(2);
                } else {
                    supportSQLiteStatement.bindString(2, folderModal.FolderName);
                }
            }
        };
        this.__deletionAdapterOfFolderModal = new EntityDeletionOrUpdateAdapter<FolderModal>(roomDatabase) {
            public String createQuery() {
                return "DELETE FROM `FolderModal` WHERE `Id` = ?";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, FolderModal folderModal) {
                if (folderModal.Id == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, folderModal.Id);
                }
            }
        };
        this.__updateAdapterOfFolderModal = new EntityDeletionOrUpdateAdapter<FolderModal>(roomDatabase) {
            public String createQuery() {
                return "UPDATE OR ABORT `FolderModal` SET `Id` = ?,`FolderName` = ? WHERE `Id` = ?";
            }

            public void bind(SupportSQLiteStatement supportSQLiteStatement, FolderModal folderModal) {
                if (folderModal.Id == null) {
                    supportSQLiteStatement.bindNull(1);
                } else {
                    supportSQLiteStatement.bindString(1, folderModal.Id);
                }
                if (folderModal.FolderName == null) {
                    supportSQLiteStatement.bindNull(2);
                } else {
                    supportSQLiteStatement.bindString(2, folderModal.FolderName);
                }
                if (folderModal.Id == null) {
                    supportSQLiteStatement.bindNull(3);
                } else {
                    supportSQLiteStatement.bindString(3, folderModal.Id);
                }
            }
        };
    }

    public void InsertFolder(FolderModal folderModal) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__insertionAdapterOfFolderModal.insert(folderModal);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    public void DeleteFolder(FolderModal folderModal) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__deletionAdapterOfFolderModal.handle(folderModal);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    public void UpdateFolder(FolderModal folderModal) {
        this.__db.assertNotSuspendingTransaction();
        this.__db.beginTransaction();
        try {
            this.__updateAdapterOfFolderModal.handle(folderModal);
            this.__db.setTransactionSuccessful();
        } finally {
            this.__db.endTransaction();
        }
    }

    public List<FolderModal> GetAllFolderList() {
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("Select * From FolderModal Order by FolderName Asc", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, (CancellationSignal) null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "Id");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "FolderName");
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                FolderModal folderModal = new FolderModal();
                if (query.isNull(columnIndexOrThrow)) {
                    folderModal.Id = null;
                } else {
                    folderModal.Id = query.getString(columnIndexOrThrow);
                }
                if (query.isNull(columnIndexOrThrow2)) {
                    folderModal.FolderName = null;
                } else {
                    folderModal.FolderName = query.getString(columnIndexOrThrow2);
                }
                arrayList.add(folderModal);
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    public List<CombineFolderModel> GetCombineFolderList() {
        FolderModal folderModal;
        String str = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM FolderModal\nLEFT JOIN (\nSELECT AudioVideoModal.RefId,SUM(case when AudioVideoModal.Artist = '' AND AudioVideoModal.Album = '' THEN 1 ELSE 0 END) totalVideos,\nSUM(case when AudioVideoModal.Artist <> '' AND AudioVideoModal.Album <> '' THEN 1 ELSE 0 END) totalSongs,\nA.Uri,A.Album,A.Artist\nFROM AudioVideoModal\nLEFT JOIN (\n\tSELECT Uri,Album,Artist,RefId FROM AudioVideoModal where AudioVideoOrder = (SELECT min(AudioVideoOrder) FRom AudioVideoModal)\n) A on A.RefId=AudioVideoModal.RefId\nGROUP BY AudioVideoModal.RefId\n) As A on A.RefId = FolderModal.Id", 0);
        this.__db.assertNotSuspendingTransaction();
        Cursor query = DBUtil.query(this.__db, acquire, false, (CancellationSignal) null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "Id");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "FolderName");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "RefId");
            int columnIndexOrThrow4 = CursorUtil.getColumnIndexOrThrow(query, "totalVideos");
            int columnIndexOrThrow5 = CursorUtil.getColumnIndexOrThrow(query, "totalSongs");
            int columnIndexOrThrow6 = CursorUtil.getColumnIndexOrThrow(query, "Uri");
            int columnIndexOrThrow7 = CursorUtil.getColumnIndexOrThrow(query, "Album");
            int columnIndexOrThrow8 = CursorUtil.getColumnIndexOrThrow(query, ExifInterface.TAG_ARTIST);
            ArrayList arrayList = new ArrayList(query.getCount());
            while (query.moveToNext()) {
                if (query.isNull(columnIndexOrThrow)) {
                    if (query.isNull(columnIndexOrThrow2)) {
                        folderModal = null;
                        CombineFolderModel combineFolderModel = new CombineFolderModel();
                        if (!query.isNull(columnIndexOrThrow3)) {
                            str = null;
                        } else {
                            str = query.getString(columnIndexOrThrow3);
                        }
                        combineFolderModel.setRefId(str);
                        combineFolderModel.setTotalVideos(query.getInt(columnIndexOrThrow4));
                        combineFolderModel.setTotalSongs(query.getInt(columnIndexOrThrow5));
                        if (!query.isNull(columnIndexOrThrow6)) {
                            str2 = null;
                        } else {
                            str2 = query.getString(columnIndexOrThrow6);
                        }
                        combineFolderModel.setUri(str2);
                        if (!query.isNull(columnIndexOrThrow7)) {
                            str3 = null;
                        } else {
                            str3 = query.getString(columnIndexOrThrow7);
                        }
                        combineFolderModel.setAlbum(str3);
                        if (!query.isNull(columnIndexOrThrow8)) {
                            str4 = null;
                        } else {
                            str4 = query.getString(columnIndexOrThrow8);
                        }
                        combineFolderModel.setArtist(str4);
                        combineFolderModel.setFolderModal(folderModal);
                        arrayList.add(combineFolderModel);
                    }
                }
                folderModal = new FolderModal();
                if (query.isNull(columnIndexOrThrow)) {
                    folderModal.Id = null;
                } else {
                    folderModal.Id = query.getString(columnIndexOrThrow);
                }
                if (query.isNull(columnIndexOrThrow2)) {
                    folderModal.FolderName = null;
                } else {
                    folderModal.FolderName = query.getString(columnIndexOrThrow2);
                }
                CombineFolderModel combineFolderModel2 = new CombineFolderModel();
                if (!query.isNull(columnIndexOrThrow3)) {
                }
                combineFolderModel2.setRefId(str);
                combineFolderModel2.setTotalVideos(query.getInt(columnIndexOrThrow4));
                combineFolderModel2.setTotalSongs(query.getInt(columnIndexOrThrow5));
                if (!query.isNull(columnIndexOrThrow6)) {
                }
                combineFolderModel2.setUri(str2);
                if (!query.isNull(columnIndexOrThrow7)) {
                }
                combineFolderModel2.setAlbum(str3);
                if (!query.isNull(columnIndexOrThrow8)) {
                }
                combineFolderModel2.setArtist(str4);
                combineFolderModel2.setFolderModal(folderModal);
                arrayList.add(combineFolderModel2);
            }
            return arrayList;
        } finally {
            query.close();
            acquire.release();
        }
    }

    public CombineFolderModel GetFolderById(String str) {
        FolderModal folderModal;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        RoomSQLiteQuery acquire = RoomSQLiteQuery.acquire("SELECT * FROM FolderModal\nLEFT JOIN (\nSELECT AudioVideoModal.RefId,SUM(case when AudioVideoModal.Artist = '' AND AudioVideoModal.Album = '' THEN 1 ELSE 0 END) totalVideos,\nSUM(case when AudioVideoModal.Artist <> '' AND AudioVideoModal.Album <> '' THEN 1 ELSE 0 END) totalSongs,\nA.Uri,A.Album,A.Artist\nFROM AudioVideoModal\nLEFT JOIN (\n\tSELECT Uri,Album,Artist,RefId FROM AudioVideoModal where AudioVideoOrder = (SELECT min(AudioVideoOrder) FRom AudioVideoModal)\n) A on A.RefId=AudioVideoModal.RefId\nGROUP BY AudioVideoModal.RefId\n) As A on A.RefId = FolderModal.Id  where Id=?", 1);
        if (str == null) {
            acquire.bindNull(1);
        } else {
            acquire.bindString(1, str);
        }
        this.__db.assertNotSuspendingTransaction();
        String r2 = null;
        CombineFolderModel cc11 = new CombineFolderModel();
        Cursor query = DBUtil.query(this.__db, acquire, false, (CancellationSignal) null);
        try {
            int columnIndexOrThrow = CursorUtil.getColumnIndexOrThrow(query, "Id");
            int columnIndexOrThrow2 = CursorUtil.getColumnIndexOrThrow(query, "FolderName");
            int columnIndexOrThrow3 = CursorUtil.getColumnIndexOrThrow(query, "RefId");
            int columnIndexOrThrow4 = CursorUtil.getColumnIndexOrThrow(query, "totalVideos");
            int columnIndexOrThrow5 = CursorUtil.getColumnIndexOrThrow(query, "totalSongs");
            int columnIndexOrThrow6 = CursorUtil.getColumnIndexOrThrow(query, "Uri");
            int columnIndexOrThrow7 = CursorUtil.getColumnIndexOrThrow(query, "Album");
            int columnIndexOrThrow8 = CursorUtil.getColumnIndexOrThrow(query, ExifInterface.TAG_ARTIST);
            if (query.moveToFirst()) {
                if (query.isNull(columnIndexOrThrow)) {
                    if (query.isNull(columnIndexOrThrow2)) {
                        folderModal = null;
                        CombineFolderModel combineFolderModel = new CombineFolderModel();
                        if (!query.isNull(columnIndexOrThrow3)) {
                            str2 = null;
                        } else {
                            str2 = query.getString(columnIndexOrThrow3);
                        }
                        combineFolderModel.setRefId(str2);
                        combineFolderModel.setTotalVideos(query.getInt(columnIndexOrThrow4));
                        combineFolderModel.setTotalSongs(query.getInt(columnIndexOrThrow5));
                        if (!query.isNull(columnIndexOrThrow6)) {
                            str3 = null;
                        } else {
                            str3 = query.getString(columnIndexOrThrow6);
                        }
                        combineFolderModel.setUri(str3);
                        if (!query.isNull(columnIndexOrThrow7)) {
                            str4 = null;
                        } else {
                            str4 = query.getString(columnIndexOrThrow7);
                        }
                        combineFolderModel.setAlbum(str4);
                        if (query.isNull(columnIndexOrThrow8)) {
                            r2 = query.getString(columnIndexOrThrow8);
                        }
                        combineFolderModel.setArtist(r2);
                        combineFolderModel.setFolderModal(folderModal);
                        cc11 = combineFolderModel;
                    }
                }
                folderModal = new FolderModal();
                if (query.isNull(columnIndexOrThrow)) {
                    folderModal.Id = null;
                } else {
                    folderModal.Id = query.getString(columnIndexOrThrow);
                }
                if (query.isNull(columnIndexOrThrow2)) {
                    folderModal.FolderName = null;
                } else {
                    folderModal.FolderName = query.getString(columnIndexOrThrow2);
                }
                CombineFolderModel combineFolderModel2 = new CombineFolderModel();
                if (!query.isNull(columnIndexOrThrow3)) {
                }
                combineFolderModel2.setRefId(str2);
                combineFolderModel2.setTotalVideos(query.getInt(columnIndexOrThrow4));
                combineFolderModel2.setTotalSongs(query.getInt(columnIndexOrThrow5));
                if (!query.isNull(columnIndexOrThrow6)) {
                }
                combineFolderModel2.setUri(str3);
                if (!query.isNull(columnIndexOrThrow7)) {
                }
                combineFolderModel2.setAlbum(str4);
                if (query.isNull(columnIndexOrThrow8)) {
                }
                combineFolderModel2.setArtist(r2);
                combineFolderModel2.setFolderModal(folderModal);
                cc11 = combineFolderModel2;
            }
            return cc11;
        } finally {
            query.close();
            acquire.release();
        }
    }

    public static List<Class<?>> getRequiredConverters() {
        return Collections.emptyList();
    }
}
