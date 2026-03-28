package com.kannada.musicplayer.database.dao;

import com.kannada.musicplayer.database.model.FolderModal;
import com.kannada.musicplayer.model.CombineFolderModel;
import java.util.List;

public interface FolderDao {
    void DeleteFolder(FolderModal folderModal);

    List<FolderModal> GetAllFolderList();

    List<CombineFolderModel> GetCombineFolderList();

    CombineFolderModel GetFolderById(String str);

    void InsertFolder(FolderModal folderModal);

    void UpdateFolder(FolderModal folderModal);
}
