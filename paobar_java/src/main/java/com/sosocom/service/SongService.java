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
     * @param indexLetter 歌名拼音首字母筛选：A–Z 或 #（非 a–z 开头），null 表示不限
     */
    List<SongDTO> getAllSongs(String search, Integer page, Integer pageSize,
            String indexLetter);

    /**
     * 根据ID获取歌曲
     */
    SongDTO getSongById(String id);

    /**
     * 获取当前播放列表
     */
    List<SongDTO> getCurrentPlaylist();
}
