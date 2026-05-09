package com.sosocom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员后台-用户列表项 DTO。
 * 比 UserProfileDTO 多出 status / createTime 等管理字段，
 * 严格仅供 /api/admin/** 接口返回。
 */
@Data
public class AdminUserDTO {
    private String id;
    private String username;
    private String avatar;
    private Integer points;
    /** 是否管理员：true=管理员，false=普通用户。 */
    private Boolean isAdmin;
    /** 账号状态：1-启用，0-禁用。 */
    private Integer status;
    /** 收藏曲目数（来自 user.collected 冗余字段，仅做展示）。 */
    private Integer collected;
    /** 已建用户歌单数（来自 user.playlists_count 冗余字段，仅做展示）。 */
    private Integer playlistsCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
