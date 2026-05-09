package com.sosocom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosocom.dto.SongDTO;
import com.sosocom.entity.Song;
import com.sosocom.mapper.SongMapper;
import com.sosocom.service.SongService;
import com.sosocom.tabdoc.TabDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 歌曲服务实现
 */
@Slf4j
@Service
public class SongServiceImpl implements SongService {

    @Autowired
    private SongMapper songMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<SongDTO> getAllSongs(String search, Integer page, Integer pageSize,
            String indexLetter) {
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

        applyIndexLetterFilter(queryWrapper, indexLetter);

        queryWrapper.orderByAsc("sort_key")
                .orderByAsc("song_name")
                .orderByAsc("id");

        Page<Song> pageParam = new Page<>(p, size);
        Page<Song> result = songMapper.selectPage(pageParam, queryWrapper);
        return result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 按首页字母导航筛选（与前端 songIndexLetter 规则一致：sort_key 首字符为 a–z）。
     */
    private void applyIndexLetterFilter(QueryWrapper<Song> queryWrapper,
            String indexLetter) {
        if (indexLetter == null || indexLetter.isBlank()) {
            return;
        }
        String letter = indexLetter.trim();
        if ("#".equals(letter)) {
            queryWrapper.and(w -> w.apply(
                    "(IFNULL(sort_key,'') = '' OR sort_key NOT REGEXP '^[a-z]')"));
            return;
        }
        if (letter.length() != 1) {
            return;
        }
        char c = Character.toUpperCase(letter.charAt(0));
        if (c >= 'A' && c <= 'Z') {
            queryWrapper.likeRight("sort_key", String.valueOf(Character.toLowerCase(c)));
        }
    }

    @Override
    public SongDTO getSongById(String id) {
        Song song = songMapper.selectById(Long.parseLong(id));
        return song != null ? convertToDetailDTO(song) : null;
    }

    @Override
    public List<SongDTO> getCurrentPlaylist() {
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByAsc("sort_key")
                .orderByAsc("song_name")
                .orderByAsc("id");
        queryWrapper.last("LIMIT 20");

        List<Song> songs = songMapper.selectList(queryWrapper);
        return songs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** 列表场景：不带 tabDocument，节省带宽。 */
    public SongDTO convertToDTO(Song song) {
        return baseDTO(song);
    }

    /** 详情场景：带 tabDocument（从字符串反序列化）。 */
    public SongDTO convertToDetailDTO(Song song) {
        SongDTO dto = baseDTO(song);
        dto.setTabDocument(parseTabDocument(song.getTabContentJson()));
        return dto;
    }

    private SongDTO baseDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(String.valueOf(song.getId()));
        dto.setTitle(song.getSongName());
        dto.setArtist(song.getArtist());
        dto.setOriginalUrl(song.getOriginalUrl());
        dto.setLyrics(song.getLyrics());
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

    private TabDocument parseTabDocument(String json) {
        if (json == null || json.isBlank()) {
            TabDocument empty = new TabDocument();
            empty.setBlocks(new ArrayList<>());
            return empty;
        }
        try {
            return objectMapper.readValue(json, TabDocument.class);
        } catch (Exception e) {
            log.warn("反序列化 tabContentJson 失败，返回空文档: {}", e.getMessage());
            TabDocument fallback = new TabDocument();
            fallback.setBlocks(new ArrayList<>());
            return fallback;
        }
    }
}
