package com.sosocom.service;

import com.sosocom.dto.SongDTO;
import java.util.List;

/**
 * 歌曲服务接口
 */
public interface SongService {

    /**
     * 获取所有歌曲（支持分页）
     * @param search 搜索关键词（歌曲名或艺术家）
     * @param page 页码，从 1 开始
     * @param pageSize 每页条数
     */
    List<SongDTO> getAllSongs(String search, Integer page, Integer pageSize);

    /**
     * 根据ID获取歌曲
     */
    SongDTO getSongById(String id);

    /**
     * 获取当前播放列表
     */
    List<SongDTO> getCurrentPlaylist();
}
