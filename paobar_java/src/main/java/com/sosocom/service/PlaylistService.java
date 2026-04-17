package com.sosocom.service;

import com.sosocom.dto.PlaylistDTO;
import com.sosocom.dto.SongDTO;
import java.util.List;

/**
 * 歌单服务接口
 */
public interface PlaylistService {
    
    /**
     * 获取所有歌单
     */
    List<PlaylistDTO> getAllPlaylists();
    
    /**
     * 根据类型获取歌单
     */
    List<PlaylistDTO> getPlaylistsByType(String type);
    
    /**
     * 根据ID获取歌单详情
     */
    PlaylistDTO getPlaylistById(String id);
    
    /**
     * 获取歌单中的歌曲
     */
    List<SongDTO> getPlaylistSongs(String playlistId);
    
    /**
     * 创建歌单
     */
    PlaylistDTO createPlaylist(String name);

    /**
     * 更新歌单名称（仅 user 类型歌单可改）
     */
    PlaylistDTO updatePlaylistName(String id, String name);
    
    /**
     * 添加歌曲到歌单
     */
    boolean addSongToPlaylist(String playlistId, String songId);
    
    /**
     * 从歌单移除歌曲
     */
    boolean removeSongFromPlaylist(String playlistId, String songId);

    /**
     * 将歌单内某首歌曲置顶
     */
    boolean moveSongToTop(String playlistId, String songId);

    /**
     * 将歌单内某首歌曲置底
     */
    boolean moveSongToBottom(String playlistId, String songId);

    /**
     * 按给定 songIds 顺序重排歌单内歌曲
     */
    boolean reorderSongs(String playlistId, List<String> songIds);
}
