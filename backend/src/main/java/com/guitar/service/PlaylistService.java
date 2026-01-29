package com.guitar.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guitar.entity.GuitarTab;
import com.guitar.entity.Playlist;

import java.util.List;

/**
 * 歌单服务接口
 */
public interface PlaylistService extends IService<Playlist> {

    List<Playlist> listWithCounts();

    List<GuitarTab> listTabs(Long playlistId);

    boolean addTab(Long playlistId, Long tabId);

    boolean removeTab(Long playlistId, Long tabId);
}
