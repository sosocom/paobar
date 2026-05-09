package com.sosocom.controller;

import com.sosocom.common.Result;
import com.sosocom.dto.SongDTO;
import com.sosocom.service.PlayHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 播放历史控制器。
 *
 * 约定：
 *   - 所有接口都走 JwtAuthenticationFilter，未登录直接 401 由过滤器处理；
 *   - record 接受 "songId" 字符串参数，与其它接口保持一致（前端统一字符串 ID）。
 */
@RestController
@RequestMapping("/api/history")
public class PlayHistoryController {

    @Autowired
    private PlayHistoryService playHistoryService;

    /** 记录一次播放（打开谱详情）。前端在详情加载完成后调用一次即可。 */
    @PostMapping
    public Result<Boolean> record(@RequestBody Map<String, String> request) {
        String songId = request == null ? null : request.get("songId");
        if (songId == null || songId.isEmpty()) {
            return Result.error("歌曲ID不能为空");
        }
        boolean ok = playHistoryService.recordPlay(songId);
        return ok ? Result.success(true) : Result.error("记录失败");
    }

    /** 最近播放列表，按 last_played_at DESC。 */
    @GetMapping
    public Result<List<SongDTO>> list(@RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        return Result.success(playHistoryService.listRecent(limit));
    }

    /** 删除单条历史记录（按歌曲 ID）。 */
    @DeleteMapping("/{songId}")
    public Result<Boolean> remove(@PathVariable String songId) {
        boolean ok = playHistoryService.removeOne(songId);
        return ok ? Result.success(true) : Result.error("删除失败");
    }

    /** 清空全部播放历史。 */
    @DeleteMapping
    public Result<Integer> clear() {
        return Result.success(playHistoryService.clearAll());
    }
}
