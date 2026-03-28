package com.kannada.musicplayer.database.dao;

import com.kannada.musicplayer.database.model.AudioVideoModal;
import java.util.List;

public interface AudioVideoDao {
    void DeleteAudioVideo(AudioVideoModal audioVideoModal);

    void DeleteAudioVideoByFolder(String str);

    void DeleteAudioVideoByUri(String str);

    List<AudioVideoModal> GetAudioVideoListByFolderID(String str);

    int GetAudioVideoListSize(String str);

    List<String> GetAudioVideoUriListByFolderID(String str);

    void InsertAudioVideo(AudioVideoModal audioVideoModal);

    void UpdateAudioVideo(AudioVideoModal audioVideoModal);

    void UpdateNameModel(String str, String str2);

    void UpdateUriModel(String str, String str2);
}
