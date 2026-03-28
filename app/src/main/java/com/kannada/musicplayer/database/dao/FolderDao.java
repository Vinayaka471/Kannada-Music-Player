package com.demo.musicvideoplayer.database.dao;

import com.demo.musicvideoplayer.database.model.FolderModal;
import com.demo.musicvideoplayer.model.CombineFolderModel;
import java.util.List;

public interface FolderDao {
    void DeleteFolder(FolderModal folderModal);

    List<FolderModal> GetAllFolderList();

    List<CombineFolderModel> GetCombineFolderList();

    CombineFolderModel GetFolderById(String str);

    void InsertFolder(FolderModal folderModal);

    void UpdateFolder(FolderModal folderModal);
}
