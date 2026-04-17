package com.sosocom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sosocom.dto.SongDTO;
import com.sosocom.entity.Song;
import com.sosocom.mapper.SongMapper;
import com.sosocom.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 歌曲服务实现
 */
@Service
public class SongServiceImpl implements SongService {

    @Autowired
    private SongMapper songMapper;

    @Override
    public List<SongDTO> getAllSongs(String search, Integer page, Integer pageSize) {
        int p = (page != null && page > 0) ? page : 1;
        int size = (pageSize != null && pageSize > 0) ? Math.min(pageSize, 100) : 20;

        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);

        if (search != null && !search.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("song_name", search)
                .or()
                .like("artist", search)
            );
        }

        queryWrapper.orderByDesc("create_time");

        Page<Song> pageParam = new Page<>(p, size);
        Page<Song> result = songMapper.selectPage(pageParam, queryWrapper);
        return result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SongDTO getSongById(String id) {
        Song song = songMapper.selectById(Long.parseLong(id));
        return song != null ? convertToDTO(song) : null;
    }

    @Override
    public List<SongDTO> getCurrentPlaylist() {
        // 返回最近的歌曲作为当前播放列表
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 20");
        
        List<Song> songs = songMapper.selectList(queryWrapper);
        return songs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    public SongDTO convertToDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(String.valueOf(song.getId()));
        dto.setTitle(song.getSongName());
        dto.setArtist(song.getArtist());
        dto.setOriginalUrl(song.getOriginalUrl());
        dto.setLyrics(song.getLyrics());
        dto.setTabContent(song.getTabContent());
        dto.setTabImageUrl(song.getTabImageUrl());
        dto.setMeta(song.getMeta());
        dto.setDifficulty(song.getDifficulty());
        dto.setTuning(song.getTuning());
        dto.setCapo(song.getCapo());
        dto.setPlayKey(song.getPlayKey());
        dto.setOriginalKey(song.getOriginalKey());
        dto.setBeat(song.getBeat());
        dto.setViewCount(song.getViewCount());
        dto.setFavoriteCount(song.getFavoriteCount());
        return dto;
    }
}
