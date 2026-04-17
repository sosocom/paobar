package com.sosocom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sosocom.common.RequestContext;
import com.sosocom.config.JwtUtil;
import com.sosocom.dto.LoginResultDTO;
import com.sosocom.dto.SongDTO;
import com.sosocom.dto.UserProfileDTO;
import com.sosocom.dto.UserStatsDTO;
import com.sosocom.entity.Song;
import com.sosocom.entity.User;
import com.sosocom.entity.UserFavorite;
import com.sosocom.mapper.SongMapper;
import com.sosocom.mapper.UserFavoriteMapper;
import com.sosocom.mapper.UserMapper;
import com.sosocom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private SongServiceImpl songService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResultDTO login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username.trim());
        User user = userMapper.selectOne(wrapper);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            return null;
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        UserProfileDTO profile = buildProfile(user);
        return new LoginResultDTO(token, profile);
    }

    @Override
    public LoginResultDTO register(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return null;
        }
        String name = username.trim();
        if (name.length() < 2) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, name);
        if (userMapper.selectCount(wrapper) > 0) {
            return null;
        }
        User user = new User();
        user.setUsername(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(1);
        user.setDeleted(0);
        user.setPoints(0);
        user.setCollected(0);
        user.setPlaylistsCount(0);
        user.setPracticeHours(0);
        userMapper.insert(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        UserProfileDTO profile = buildProfile(user);
        return new LoginResultDTO(token, profile);
    }

    private UserProfileDTO buildProfile(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(String.valueOf(user.getId()));
        dto.setUsername(user.getUsername());
        dto.setAvatar(user.getAvatar());
        dto.setPoints(user.getPoints());
        UserStatsDTO stats = new UserStatsDTO();
        stats.setCollected(user.getCollected());
        stats.setPlaylists(user.getPlaylistsCount());
        stats.setPracticeHours(user.getPracticeHours());
        dto.setStats(stats);
        return dto;
    }

    @Override
    public UserProfileDTO getCurrentUser() {
        Long userId = RequestContext.getUserId();
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            // 兜底：deleted 字段可能为 null（旧数据），用不带 @TableLogic 的原始 SQL
            user = userMapper.selectByIdRaw(userId);
            if (user != null) {
                // 自动修复脏数据
                userMapper.fixDeletedNull(userId);
            } else {
                return null;
            }
        }
        return buildProfile(user);
    }

    @Override
    public boolean favoriteSong(String userId, String songId) {
        if (userId == null) userId = RequestContext.getUserId() != null ? String.valueOf(RequestContext.getUserId()) : null;
        if (userId == null) return false;
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(Long.parseLong(userId));
        favorite.setSongId(Long.parseLong(songId));
        
        int result = userFavoriteMapper.insert(favorite);
        
        // 更新用户收藏数
        if (result > 0) {
            User user = userMapper.selectById(Long.parseLong(userId));
            user.setCollected(user.getCollected() + 1);
            userMapper.updateById(user);
            
            // 更新歌曲收藏数
            Song song = songMapper.selectById(Long.parseLong(songId));
            song.setFavoriteCount(song.getFavoriteCount() + 1);
            songMapper.updateById(song);
        }
        
        return result > 0;
    }

    @Override
    public boolean unfavoriteSong(String userId, String songId) {
        if (userId == null) userId = RequestContext.getUserId() != null ? String.valueOf(RequestContext.getUserId()) : null;
        if (userId == null) return false;
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, Long.parseLong(userId));
        wrapper.eq(UserFavorite::getSongId, Long.parseLong(songId));
        
        int result = userFavoriteMapper.delete(wrapper);
        
        // 更新用户收藏数
        if (result > 0) {
            User user = userMapper.selectById(Long.parseLong(userId));
            user.setCollected(Math.max(0, user.getCollected() - 1));
            userMapper.updateById(user);
            
            // 更新歌曲收藏数
            Song song = songMapper.selectById(Long.parseLong(songId));
            song.setFavoriteCount(Math.max(0, song.getFavoriteCount() - 1));
            songMapper.updateById(song);
        }
        
        return result > 0;
    }

    @Override
    public List<SongDTO> getFavoriteSongs(String userId) {
        if (userId == null) userId = RequestContext.getUserId() != null ? String.valueOf(RequestContext.getUserId()) : null;
        if (userId == null) return new ArrayList<>();
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, Long.parseLong(userId));
        wrapper.orderByDesc(UserFavorite::getCreateTime);
        
        List<UserFavorite> favorites = userFavoriteMapper.selectList(wrapper);
        
        List<SongDTO> result = new ArrayList<>();
        for (UserFavorite favorite : favorites) {
            Song song = songMapper.selectById(favorite.getSongId());
            if (song != null) {
                result.add(songService.convertToDTO(song));
            }
        }
        return result;
    }

    @Override
    public List<Long> getFavoriteSongIds() {
        Long userId = RequestContext.getUserId();
        if (userId == null) return new ArrayList<>();
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        wrapper.select(UserFavorite::getSongId);
        return userFavoriteMapper.selectList(wrapper)
                .stream().map(UserFavorite::getSongId).collect(Collectors.toList());
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        Long userId = RequestContext.getUserId();
        if (userId == null) return false;
        User user = userMapper.selectById(userId);
        if (user == null) return false;
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return true;
    }
}
