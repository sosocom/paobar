package com.sosocom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sosocom.dto.PlaylistDTO;
import com.sosocom.dto.SongDTO;
import com.sosocom.entity.Playlist;
import com.sosocom.entity.PlaylistSong;
import com.sosocom.entity.Song;
import com.sosocom.mapper.PlaylistMapper;
import com.sosocom.mapper.PlaylistSongMapper;
import com.sosocom.mapper.SongMapper;
import com.sosocom.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 歌单服务实现类
 */
@Service
public class PlaylistServiceImpl implements PlaylistService {

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private PlaylistSongMapper playlistSongMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private SongServiceImpl songService;

    @Override
    public List<PlaylistDTO> getAllPlaylists() {
        List<Playlist> playlists = playlistMapper.selectList(null);
        return playlists.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<PlaylistDTO> getPlaylistsByType(String type) {
        LambdaQueryWrapper<Playlist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Playlist::getType, type);
        List<Playlist> playlists = playlistMapper.selectList(wrapper);
        return playlists.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PlaylistDTO getPlaylistById(String id) {
        Playlist playlist = playlistMapper.selectById(Long.parseLong(id));
        return playlist != null ? convertToDTO(playlist) : null;
    }

    @Override
    public List<SongDTO> getPlaylistSongs(String playlistId) {
        LambdaQueryWrapper<PlaylistSong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistSong::getPlaylistId, Long.parseLong(playlistId));
        wrapper.orderByAsc(PlaylistSong::getPosition);
        
        List<PlaylistSong> playlistSongs = playlistSongMapper.selectList(wrapper);
        
        List<SongDTO> result = new ArrayList<>();
        for (PlaylistSong ps : playlistSongs) {
            Song song = songMapper.selectById(ps.getSongId());
            if (song != null) {
                result.add(songService.convertToDTO(song));
            }
        }
        return result;
    }

    @Override
    public PlaylistDTO createPlaylist(String name) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setGradientStart("#DC2626");
        playlist.setGradientEnd("#7C2D12");
        playlist.setSongCount(0);
        playlist.setType("user");
        
        playlistMapper.insert(playlist);
        return convertToDTO(playlist);
    }

    @Override
    public PlaylistDTO updatePlaylistName(String id, String name) {
        if (!StringUtils.hasText(name) || name.trim().isEmpty()) {
            return null;
        }
        Playlist playlist = playlistMapper.selectById(Long.parseLong(id));
        if (playlist == null || !"user".equals(playlist.getType())) {
            return null;
        }
        playlist.setName(name.trim());
        playlistMapper.updateById(playlist);
        return convertToDTO(playlist);
    }

    @Override
    public boolean addSongToPlaylist(String playlistId, String songId) {
        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylistId(Long.parseLong(playlistId));
        playlistSong.setSongId(Long.parseLong(songId));
        
        // 获取当前歌单的最大位置
        LambdaQueryWrapper<PlaylistSong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistSong::getPlaylistId, Long.parseLong(playlistId));
        wrapper.orderByDesc(PlaylistSong::getPosition);
        wrapper.last("LIMIT 1");
        PlaylistSong last = playlistSongMapper.selectOne(wrapper);
        int position = last != null ? last.getPosition() + 1 : 0;
        playlistSong.setPosition(position);
        
        int result = playlistSongMapper.insert(playlistSong);
        
        // 更新歌单歌曲数量
        if (result > 0) {
            Playlist playlist = playlistMapper.selectById(Long.parseLong(playlistId));
            playlist.setSongCount(playlist.getSongCount() + 1);
            playlistMapper.updateById(playlist);
        }
        
        return result > 0;
    }

    @Override
    public boolean removeSongFromPlaylist(String playlistId, String songId) {
        LambdaQueryWrapper<PlaylistSong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistSong::getPlaylistId, Long.parseLong(playlistId));
        wrapper.eq(PlaylistSong::getSongId, Long.parseLong(songId));
        
        int result = playlistSongMapper.delete(wrapper);
        
        // 更新歌单歌曲数量
        if (result > 0) {
            Playlist playlist = playlistMapper.selectById(Long.parseLong(playlistId));
            playlist.setSongCount(Math.max(0, playlist.getSongCount() - 1));
            playlistMapper.updateById(playlist);
        }
        
        return result > 0;
    }

    /**
     * 转换为DTO
     */
    private PlaylistDTO convertToDTO(Playlist playlist) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(String.valueOf(playlist.getId()));
        dto.setName(playlist.getName());
        dto.setCoverUrl(playlist.getCoverUrl());
        
        // 设置渐变色
        if (StringUtils.hasText(playlist.getGradientStart()) && 
            StringUtils.hasText(playlist.getGradientEnd())) {
            dto.setGradient(Arrays.asList(playlist.getGradientStart(), playlist.getGradientEnd()));
        }
        
        dto.setSongCount(playlist.getSongCount());
        dto.setType(playlist.getType());
        dto.setChordProgression(playlist.getChordProgression());
        
        // 获取歌单中的歌曲ID列表
        LambdaQueryWrapper<PlaylistSong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistSong::getPlaylistId, playlist.getId());
        wrapper.orderByAsc(PlaylistSong::getPosition);
        List<PlaylistSong> playlistSongs = playlistSongMapper.selectList(wrapper);
        List<String> songIds = playlistSongs.stream()
            .map(ps -> String.valueOf(ps.getSongId()))
            .collect(Collectors.toList());
        dto.setSongs(songIds);
        
        return dto;
    }
}
