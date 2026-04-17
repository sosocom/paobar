package com.sosocom.dto;

import lombok.Data;

/**
 * 用户统计信息DTO
 */
@Data
public class UserStatsDTO {
    private Integer collected;
    private Integer playlists;
    
    @com.fasterxml.jackson.annotation.JsonProperty("practiceHours")
    private Integer practiceHours;
}
