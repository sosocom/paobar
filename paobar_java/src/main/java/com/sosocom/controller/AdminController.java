package com.sosocom.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sosocom.common.Result;
import com.sosocom.dto.AdminUserDTO;
import com.sosocom.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员后台控制器。
 * 路径前缀 /api/admin/** 已被 JwtAuthFilter 强制要求 is_admin = 1。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        IPage<AdminUserDTO> result = adminService.listUsers(keyword, page, pageSize);
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        return Result.success(data);
    }

    /**
     * 设置某用户的管理员标记
     */
    @PutMapping("/users/{id}/admin")
    public Result<Boolean> setAdmin(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Boolean admin = body.get("isAdmin");
        if (admin == null) {
            return Result.error("缺少 isAdmin 参数");
        }
        boolean ok = adminService.setAdmin(id, admin);
        return ok ? Result.success(true) : Result.error("操作失败（不能修改自己，或用户不存在）");
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/users/{id}/status")
    public Result<Boolean> setStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return Result.error("缺少 enabled 参数");
        }
        boolean ok = adminService.setStatus(id, enabled);
        return ok ? Result.success(true) : Result.error("操作失败（不能禁用自己，或用户不存在）");
    }
}
