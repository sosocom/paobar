package com.sosocom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sosocom.common.RequestContext;
import com.sosocom.dto.SongDTO;
import com.sosocom.entity.PlayHistory;
import com.sosocom.entity.Song;
import com.sosocom.mapper.PlayHistoryMapper;
import com.sosocom.mapper.SongMapper;
import com.sosocom.service.PlayHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放历史服务实现。
 * 统一用 {@link RequestContext#getUserId()} 取当前登录用户，未登录调用一律安全失败（不抛异常）。
 */
@Service
public class PlayHistoryServiceImpl implements PlayHistoryService {

    /** 列表兜底上限，防止前端传过大的 limit 导致一次拉完。 */
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    @Autowired
    private PlayHistoryMapper playHistoryMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private SongServiceImpl songService;

    @Override
    public boolean recordPlay(String songId) {
        Long userId = RequestContext.getUserId();
        if (userId == null || songId == null) {
            return false;
        }
        long songIdLong;
        try {
            songIdLong = Long.parseLong(songId);
        } catch (NumberFormatException e) {
            return false;
        }
        // 轻量防御：songId 必须真实存在，否则脏数据会一直挂在历史里
        if (songMapper.selectById(songIdLong) == null) {
            return false;
        }
        return playHistoryMapper.upsertPlay(userId, songIdLong) > 0;
    }

    @Override
    public List<SongDTO> listRecent(int limit) {
        Long userId = RequestContext.getUserId();
        if (userId == null) {
            return new ArrayList<>();
        }
        int safeLimit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        LambdaQueryWrapper<PlayHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlayHistory::getUserId, userId);
        wrapper.orderByDesc(PlayHistory::getLastPlayedAt);
        wrapper.last("LIMIT " + safeLimit);
        List<PlayHistory> histories = playHistoryMapper.selectList(wrapper);
        List<SongDTO> result = new ArrayList<>(histories.size());
        for (PlayHistory h : histories) {
            Song song = songMapper.selectById(h.getSongId());
            if (song != null) {
                // 列表页只用列表版 DTO，避免把 tabDocument 这种大字段也塞过去
                result.add(songService.convertToDTO(song));
            }
        }
        return result;
    }

    @Override
    public boolean removeOne(String songId) {
        Long userId = RequestContext.getUserId();
        if (userId == null || songId == null) return false;
        long songIdLong;
        try {
            songIdLong = Long.parseLong(songId);
        } catch (NumberFormatException e) {
            return false;
        }
        LambdaQueryWrapper<PlayHistory> w = new LambdaQueryWrapper<>();
        w.eq(PlayHistory::getUserId, userId);
        w.eq(PlayHistory::getSongId, songIdLong);
        return playHistoryMapper.delete(w) > 0;
    }

    @Override
    public int clearAll() {
        Long userId = RequestContext.getUserId();
        if (userId == null) return 0;
        LambdaQueryWrapper<PlayHistory> w = new LambdaQueryWrapper<>();
        w.eq(PlayHistory::getUserId, userId);
        return playHistoryMapper.delete(w);
    }
}
