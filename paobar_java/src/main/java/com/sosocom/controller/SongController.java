package com.sosocom.controller;

import com.sosocom.common.Result;
import com.sosocom.dto.SongDTO;
import com.sosocom.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 歌曲控制器
 */
@RestController
@RequestMapping("/api/songs")
public class SongController {

    @Autowired
    private SongService songService;

    /**
     * 获取歌曲列表（分页，支持下拉加载更多）
     */
    @GetMapping
    public Result<List<SongDTO>> getSongs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String indexLetter) {
        List<SongDTO> songs = songService.getAllSongs(search, page, pageSize, indexLetter);
        return Result.success(songs);
    }

    /**
     * 根据ID获取歌曲详情
     */
    @GetMapping("/{id}")
    public Result<SongDTO> getSongById(@PathVariable String id) {
        SongDTO song = songService.getSongById(id);
        if (song == null) {
            return Result.error("歌曲不存在");
        }
        return Result.success(song);
    }

    /**
     * 获取当前播放列表
     */
    @GetMapping("/current")
    public Result<List<SongDTO>> getCurrentPlaylist() {
        List<SongDTO> songs = songService.getCurrentPlaylist();
        return Result.success(songs);
    }
}
