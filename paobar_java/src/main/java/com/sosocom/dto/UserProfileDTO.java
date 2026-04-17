package com.sosocom.dto;

import lombok.Data;

/**
 * 用户信息响应DTO
 */
@Data
public class UserProfileDTO {
    private String id;
    private String username;
    private String avatar;
    private Integer points;
    private UserStatsDTO stats;
}
