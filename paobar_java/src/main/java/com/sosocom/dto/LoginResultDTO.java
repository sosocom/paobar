package com.sosocom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/注册成功返回：token + 用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultDTO {
    private String token;
    private UserProfileDTO user;
}
