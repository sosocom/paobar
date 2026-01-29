package com.guitar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guitar.entity.GuitarTab;
import com.guitar.entity.Playlist;
import com.guitar.entity.PlaylistTab;
import com.guitar.mapper.PlaylistMapper;
import com.guitar.mapper.PlaylistTabMapper;
import com.guitar.service.PlaylistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 歌单服务实现类
 */
@Slf4j
@Service
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements PlaylistService {

    private final PlaylistTabMapper playlistTabMapper;

    public PlaylistServiceImpl(PlaylistTabMapper playlistTabMapper) {
        this.playlistTabMapper = playlistTabMapper;
    }

    @Override
    public List<Playlist> listWithCounts() {
        List<Playlist> playlists = this.list();
        for (Playlist playlist : playlists) {
            int count = playlistTabMapper.countByPlaylistId(playlist.getId());
            playlist.setTabCount(count);
        }
        return playlists;
    }

    @Override
    public List<GuitarTab> listTabs(Long playlistId) {
        return playlistTabMapper.selectTabsByPlaylistId(playlistId);
    }

    @Override
    @Transactional
    public boolean addTab(Long playlistId, Long tabId) {
        Long existingId = playlistTabMapper.selectIdByPlaylistAndTab(playlistId, tabId);
        if (existingId != null) {
            return false;
        }
        PlaylistTab relation = new PlaylistTab();
        relation.setPlaylistId(playlistId);
        relation.setTabId(tabId);
        return playlistTabMapper.insert(relation) > 0;
    }

    @Override
    @Transactional
    public boolean removeTab(Long playlistId, Long tabId) {
        LambdaQueryWrapper<PlaylistTab> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlaylistTab::getPlaylistId, playlistId)
               .eq(PlaylistTab::getTabId, tabId);
        return playlistTabMapper.delete(wrapper) > 0;
    }
}
