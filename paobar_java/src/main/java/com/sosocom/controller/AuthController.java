package com.sosocom.controller;

import com.sosocom.common.Result;
import com.sosocom.dto.LoginResultDTO;
import com.sosocom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 登录、注册（白名单，无需 token）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginResultDTO> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        LoginResultDTO result = userService.login(username, password);
        if (result == null) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result<LoginResultDTO> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        LoginResultDTO result = userService.register(username, password);
        if (result == null) {
            return Result.error("注册失败，用户名可能已存在或格式不符（至少2位）");
        }
        return Result.success(result);
    }
}
