package com.sosocom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sosocom.common.RequestContext;
import com.sosocom.dto.AdminUserDTO;
import com.sosocom.entity.User;
import com.sosocom.mapper.UserMapper;
import com.sosocom.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 管理员后台服务实现。
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<AdminUserDTO> listUsers(String keyword, long page, long pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        if (pageSize > 100) pageSize = 100;

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword.trim());
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> mpPage = new Page<>(page, pageSize);
        IPage<User> userPage = userMapper.selectPage(mpPage, wrapper);

        return userPage.convert(this::toDTO);
    }

    @Override
    public boolean setAdmin(Long userId, boolean admin) {
        if (userId == null) return false;
        Long current = RequestContext.getUserId();
        // 不允许给自己改管理员标记，避免误操作把自己降级后无法恢复
        if (current != null && current.equals(userId)) return false;
        User user = userMapper.selectById(userId);
        if (user == null) return false;
        user.setIsAdmin(admin ? 1 : 0);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean setStatus(Long userId, boolean enabled) {
        if (userId == null) return false;
        Long current = RequestContext.getUserId();
        // 不允许把自己禁用
        if (current != null && current.equals(userId)) return false;
        User user = userMapper.selectById(userId);
        if (user == null) return false;
        user.setStatus(enabled ? 1 : 0);
        return userMapper.updateById(user) > 0;
    }

    private AdminUserDTO toDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(String.valueOf(user.getId()));
        dto.setUsername(user.getUsername());
        dto.setAvatar(user.getAvatar());
        dto.setPoints(user.getPoints());
        dto.setIsAdmin(user.getIsAdmin() != null && user.getIsAdmin() == 1);
        dto.setStatus(user.getStatus());
        dto.setCollected(user.getCollected());
        dto.setPlaylistsCount(user.getPlaylistsCount());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}
