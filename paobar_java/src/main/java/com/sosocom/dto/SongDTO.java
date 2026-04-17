package com.sosocom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 歌曲DTO
 */
@Data
public class SongDTO {
    
    private String id;
    
    private String title;
    
    private String artist;
    
    @JsonProperty("originalUrl")
    private String originalUrl;
    
    private String lyrics;
    
    @JsonProperty("tabContent")
    private String tabContent; // HTML内容
    
    @JsonProperty("tabImageUrl")
    private String tabImageUrl;
    
    private String meta;
    
    private String difficulty;
    
    private String tuning;
    
    private Integer capo;
    
    @JsonProperty("playKey")
    private String playKey;
    
    @JsonProperty("originalKey")
    private String originalKey;
    
    private String beat;
    
    @JsonProperty("viewCount")
    private Integer viewCount;
    
    @JsonProperty("favoriteCount")
    private Integer favoriteCount;
}
