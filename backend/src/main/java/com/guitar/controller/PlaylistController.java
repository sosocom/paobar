package com.guitar.controller;

import com.guitar.common.Result;
import com.guitar.entity.GuitarTab;
import com.guitar.entity.Playlist;
import com.guitar.service.PlaylistService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 歌单 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public Result<List<Playlist>> list() {
        return Result.success(playlistService.listWithCounts());
    }

    @PostMapping
    public Result<Playlist> create(@RequestBody Playlist playlist) {
        if (playlist.getName() == null || playlist.getName().trim().isEmpty()) {
            return Result.error("歌单名称不能为空");
        }
        playlistService.save(playlist);
        return Result.success("创建成功", playlist);
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Playlist playlist) {
        playlist.setId(id);
        playlistService.updateById(playlist);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        playlistService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}/tabs")
    public Result<List<GuitarTab>> listTabs(@PathVariable Long id) {
        return Result.success(playlistService.listTabs(id));
    }

    @PostMapping("/{id}/tabs")
    public Result<String> addTab(@PathVariable Long id, @RequestBody AddTabRequest request) {
        if (request == null || request.getTabId() == null) {
            return Result.error("tabId不能为空");
        }
        boolean added = playlistService.addTab(id, request.getTabId());
        if (!added) {
            return Result.error("该曲谱已在歌单中");
        }
        return Result.success("添加成功");
    }

    @DeleteMapping("/{id}/tabs/{tabId}")
    public Result<String> removeTab(@PathVariable Long id, @PathVariable Long tabId) {
        boolean removed = playlistService.removeTab(id, tabId);
        if (!removed) {
            return Result.error("移除失败");
        }
        return Result.success("移除成功");
    }

    @Data
    private static class AddTabRequest {
        private Long tabId;
    }
}
