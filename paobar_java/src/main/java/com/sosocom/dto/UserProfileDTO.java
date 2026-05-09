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
    /** 是否管理员（前端用以决定管理员功能可见性）。 */
    private Boolean isAdmin;
    private UserStatsDTO stats;
}
