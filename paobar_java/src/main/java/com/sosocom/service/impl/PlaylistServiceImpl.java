package com.sosocom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sosocom.common.RequestContext;
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
        Long currentUserId = RequestContext.getUserId();
        LambdaQueryWrapper<Playlist> wrapper = new LambdaQueryWrapper<>();
        if (currentUserId != null) {
            wrapper.and(w -> w.eq(Playlist::getUserId, currentUserId).or().eq(Playlist::getType, "ai"));
        } else {
            wrapper.eq(Playlist::getType, "ai");
        }
        wrapper.orderByAsc(Playlist::getCreateTime);
        List<Playlist> playlists = playlistMapper.selectList(wrapper);
        return playlists.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<PlaylistDTO> getPlaylistsByType(String type) {
        Long currentUserId = RequestContext.getUserId();
        LambdaQueryWrapper<Playlist> wrapper = new LambdaQueryWrapper<>();
        if ("user".equals(type)) {
            if (currentUserId == null) return new ArrayList<>();
            wrapper.eq(Playlist::getUserId, currentUserId).eq(Playlist::getType, "user");
        } else {
            wrapper.eq(Playlist::getType, type);
        }
        wrapper.orderByAsc(Playlist::getCreateTime);
        List<Playlist> playlists = playlistMapper.selectList(wrapper);
        return playlists.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PlaylistDTO getPlaylistById(String id) {
        Playlist playlist = playlistMapper.selectById(Long.parseLong(id));
        if (playlist == null) return null;
        Long currentUserId = RequestContext.getUserId();
        boolean canAccess = "ai".equals(playlist.getType())
                || (currentUserId != null && currentUserId.equals(playlist.getUserId()));
        return canAccess ? convertToDTO(playlist) : null;
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
        Long currentUserId = RequestContext.getUserId();
        if (currentUserId == null) return null;
        Playlist playlist = new Playlist();
        playlist.setUserId(currentUserId);
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
        if (!StringUtils.hasText(name) || name.trim().isEmpty()) return null;
        Playlist playlist = playlistMapper.selectById(Long.parseLong(id));
        if (playlist == null || !isOwnPlaylist(playlist)) return null;
        playlist.setName(name.trim());
        playlistMapper.updateById(playlist);
        return convertToDTO(playlist);
    }

    @Override
    public boolean addSongToPlaylist(String playlistId, String songId) {
        if (!isOwnPlaylist(playlistMapper.selectById(Long.parseLong(playlistId)))) return false;
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
        if (!isOwnPlaylist(playlistMapper.selectById(Long.parseLong(playlistId)))) return false;
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

    @Override
    public boolean moveSongToTop(String playlistId, String songId) {
        Playlist p = playlistMapper.selectById(Long.parseLong(playlistId));
        if (p == null || !isOwnPlaylist(p)) return false;
        Long pid = Long.parseLong(playlistId);
        Long sid = Long.parseLong(songId);
        PlaylistSong target = getPlaylistSong(pid, sid);
        if (target == null) return false;
        int currentPos = target.getPosition();
        if (currentPos == 0) return true;
        // 将 position < currentPos 的记录 position 都 +1（从大到小更新避免冲突）
        List<PlaylistSong> toShift = playlistSongMapper.selectList(
            new LambdaQueryWrapper<PlaylistSong>()
                .eq(PlaylistSong::getPlaylistId, pid)
                .lt(PlaylistSong::getPosition, currentPos)
                .orderByDesc(PlaylistSong::getPosition)
        );
        for (PlaylistSong ps : toShift) {
            ps.setPosition(ps.getPosition() + 1);
            playlistSongMapper.updateById(ps);
        }
        target.setPosition(0);
        playlistSongMapper.updateById(target);
        return true;
    }

    @Override
    public boolean moveSongToBottom(String playlistId, String songId) {
        Playlist p = playlistMapper.selectById(Long.parseLong(playlistId));
        if (p == null || !isOwnPlaylist(p)) return false;
        Long pid = Long.parseLong(playlistId);
        Long sid = Long.parseLong(songId);
        PlaylistSong target = getPlaylistSong(pid, sid);
        if (target == null) return false;
        LambdaQueryWrapper<PlaylistSong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistSong::getPlaylistId, pid);
        wrapper.orderByDesc(PlaylistSong::getPosition);
        wrapper.last("LIMIT 1");
        PlaylistSong last = playlistSongMapper.selectOne(wrapper);
        int maxPos = last != null ? last.getPosition() : 0;
        if (target.getPosition() >= maxPos) return true;
        target.setPosition(maxPos + 1);
        playlistSongMapper.updateById(target);
        return true;
    }

    @Override
    public boolean reorderSongs(String playlistId, List<String> songIds) {
        if (songIds == null || songIds.isEmpty()) return true;
        Playlist p = playlistMapper.selectById(Long.parseLong(playlistId));
        if (p == null || !isOwnPlaylist(p)) return false;
        Long pid = Long.parseLong(playlistId);
        // 先全部置为临时位置，避免唯一约束或覆盖
        List<PlaylistSong> all = playlistSongMapper.selectList(
            new LambdaQueryWrapper<PlaylistSong>().eq(PlaylistSong::getPlaylistId, pid).orderByAsc(PlaylistSong::getPosition)
        );
        int offset = 10000;
        for (int i = 0; i < all.size(); i++) {
            PlaylistSong ps = all.get(i);
            ps.setPosition(offset + i);
            playlistSongMapper.updateById(ps);
        }
        for (int i = 0; i < songIds.size(); i++) {
            Long sid = Long.parseLong(songIds.get(i));
            PlaylistSong ps = getPlaylistSong(pid, sid);
            if (ps != null) {
                ps.setPosition(i);
                playlistSongMapper.updateById(ps);
            }
        }
        return true;
    }

    private boolean isOwnPlaylist(Playlist playlist) {
        if (playlist == null || !"user".equals(playlist.getType())) return false;
        Long current = RequestContext.getUserId();
        return current != null && current.equals(playlist.getUserId());
    }

    private PlaylistSong getPlaylistSong(Long playlistId, Long songId) {
        return playlistSongMapper.selectOne(
            new LambdaQueryWrapper<PlaylistSong>()
                .eq(PlaylistSong::getPlaylistId, playlistId)
                .eq(PlaylistSong::getSongId, songId)
        );
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
