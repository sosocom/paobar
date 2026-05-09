package com.sosocom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sosocom.dto.AdminUserDTO;

/**
 * 管理员后台服务（仅 /api/admin/** 调用，鉴权由 JwtAuthFilter 完成）。
 */
public interface AdminService {

    /**
     * 分页查询用户列表，可按用户名模糊搜索。
     */
    IPage<AdminUserDTO> listUsers(String keyword, long page, long pageSize);

    /**
     * 设置某用户的管理员标记（true=升为管理员，false=取消管理员）。
     * 不允许操作目标 = 当前登录用户自身（避免误操作把自己降级）。
     */
    boolean setAdmin(Long userId, boolean admin);

    /**
     * 设置某用户的启用状态（true=启用，false=禁用）。
     * 不允许操作目标 = 当前登录用户自身。
     */
    boolean setStatus(Long userId, boolean enabled);
}
