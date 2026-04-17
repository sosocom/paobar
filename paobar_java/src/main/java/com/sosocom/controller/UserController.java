package com.sosocom.controller;

import com.sosocom.common.Result;
import com.sosocom.dto.SongDTO;
import com.sosocom.dto.UserProfileDTO;
import com.sosocom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserProfileDTO> getCurrentUser() {
        UserProfileDTO user = userService.getCurrentUser();
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 收藏歌曲
     */
    @PostMapping("/favorites")
    public Result<Boolean> favoriteSong(@RequestBody Map<String, String> request) {
        String songId = request.get("songId");
        if (songId == null) {
            return Result.error("歌曲ID不能为空");
        }
        boolean success = userService.favoriteSong(null, songId);
        return success ? Result.success(true) : Result.error("收藏失败");
    }

    @DeleteMapping("/favorites/{songId}")
    public Result<Boolean> unfavoriteSong(@PathVariable String songId) {
        boolean success = userService.unfavoriteSong(null, songId);
        return success ? Result.success(true) : Result.error("取消收藏失败");
    }

    @GetMapping("/favorites")
    public Result<List<SongDTO>> getFavoriteSongs() {
        List<SongDTO> songs = userService.getFavoriteSongs(null);
        return Result.success(songs);
    }

    /**
     * 获取收藏歌曲ID列表（轻量接口，用于前端判断收藏状态）
     */
    @GetMapping("/favorite-ids")
    public Result<List<Long>> getFavoriteSongIds() {
        return Result.success(userService.getFavoriteSongIds());
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Boolean> changePassword(@RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.error("请填写完整");
        }
        if (newPassword.length() < 4) {
            return Result.error("新密码长度至少 4 位");
        }
        boolean success = userService.changePassword(oldPassword, newPassword);
        return success ? Result.success(true) : Result.error("当前密码不正确");
    }
}
