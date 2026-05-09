package com.sosocom.controller;

import com.sosocom.common.Result;
import com.sosocom.dto.PlaylistDTO;
import com.sosocom.dto.SongDTO;
import com.sosocom.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 歌单控制器
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    /**
     * 获取所有歌单
     */
    @GetMapping
    public Result<List<PlaylistDTO>> getPlaylists(
            @RequestParam(required = false) String type) {
        List<PlaylistDTO> playlists;
        if (type != null) {
            playlists = playlistService.getPlaylistsByType(type);
        } else {
            playlists = playlistService.getAllPlaylists();
        }
        return Result.success(playlists);
    }

    /**
     * 根据ID获取歌单详情
     */
    @GetMapping("/{id}")
    public Result<PlaylistDTO> getPlaylistById(@PathVariable String id) {
        PlaylistDTO playlist = playlistService.getPlaylistById(id);
        if (playlist == null) {
            return Result.error("歌单不存在");
        }
        return Result.success(playlist);
    }

    /**
     * 获取歌单中的歌曲
     */
    @GetMapping("/{id}/songs")
    public Result<List<SongDTO>> getPlaylistSongs(@PathVariable String id) {
        List<SongDTO> songs = playlistService.getPlaylistSongs(id);
        return Result.success(songs);
    }

    /**
     * 创建歌单
     */
    @PostMapping
    public Result<PlaylistDTO> createPlaylist(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return Result.error("歌单名称不能为空");
        }
        PlaylistDTO playlist = playlistService.createPlaylist(name);
        return Result.success(playlist);
    }

    /**
     * 更新歌单名称（仅用户歌单可改）
     */
    @PutMapping("/{id}")
    public Result<PlaylistDTO> updatePlaylistName(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return Result.error("歌单名称不能为空");
        }
        PlaylistDTO playlist = playlistService.updatePlaylistName(id, name);
        if (playlist == null) {
            return Result.error("歌单不存在或不可修改");
        }
        return Result.success(playlist);
    }

    /**
     * 删除歌单（仅用户歌单可删）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePlaylist(@PathVariable String id) {
        boolean success = playlistService.deletePlaylist(id);
        return success ? Result.success(true) : Result.error("歌单不存在或不可删除");
    }

    /**
     * 添加歌曲到歌单
     */
    @PostMapping("/{id}/songs")
    public Result<Boolean> addSongToPlaylist(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String songId = request.get("songId");
        if (songId == null) {
            return Result.error("歌曲ID不能为空");
        }
        boolean success = playlistService.addSongToPlaylist(id, songId);
        return success ? Result.success(true) : Result.error("添加失败");
    }

    /**
     * 从歌单移除歌曲
     */
    @DeleteMapping("/{id}/songs/{songId}")
    public Result<Boolean> removeSongFromPlaylist(
            @PathVariable String id,
            @PathVariable String songId) {
        boolean success = playlistService.removeSongFromPlaylist(id, songId);
        return success ? Result.success(true) : Result.error("移除失败");
    }

    /**
     * 歌单内歌曲置顶
     */
    @PutMapping("/{id}/songs/{songId}/move-to-top")
    public Result<Boolean> moveSongToTop(
            @PathVariable String id,
            @PathVariable String songId) {
        boolean success = playlistService.moveSongToTop(id, songId);
        return success ? Result.success(true) : Result.error("操作失败");
    }

    /**
     * 歌单内歌曲置底
     */
    @PutMapping("/{id}/songs/{songId}/move-to-bottom")
    public Result<Boolean> moveSongToBottom(
            @PathVariable String id,
            @PathVariable String songId) {
        boolean success = playlistService.moveSongToBottom(id, songId);
        return success ? Result.success(true) : Result.error("操作失败");
    }

    /**
     * 歌单内歌曲拖动排序，body 为新的 songId 顺序列表
     */
    @PutMapping("/{id}/songs/reorder")
    public Result<Boolean> reorderSongs(
            @PathVariable String id,
            @RequestBody List<String> songIds) {
        if (songIds == null) {
            return Result.error("顺序不能为空");
        }
        boolean success = playlistService.reorderSongs(id, songIds);
        return success ? Result.success(true) : Result.error("排序失败");
    }
}
