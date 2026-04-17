package com.sosocom.service;

import com.sosocom.dto.LoginResultDTO;
import com.sosocom.dto.UserProfileDTO;
import com.sosocom.dto.SongDTO;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 登录，返回 token 与用户信息
     */
    LoginResultDTO login(String username, String password);

    /**
     * 注册，返回 token 与用户信息
     */
    LoginResultDTO register(String username, String password);
    
    /**
     * 获取当前用户信息（从 RequestContext 取当前用户 ID）
     */
    UserProfileDTO getCurrentUser();
    
    /**
     * 收藏歌曲
     */
    boolean favoriteSong(String userId, String songId);
    
    /**
     * 取消收藏歌曲
     */
    boolean unfavoriteSong(String userId, String songId);
    
    /**
     * 获取收藏的歌曲列表
     */
    List<SongDTO> getFavoriteSongs(String userId);

    /**
     * 获取当前用户所有收藏歌曲的 ID 集合
     */
    List<Long> getFavoriteSongIds();

    /**
     * 修改密码
     */
    boolean changePassword(String oldPassword, String newPassword);
}
