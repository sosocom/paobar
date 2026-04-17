package com.sosocom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 歌单响应DTO
 */
@Data
public class PlaylistDTO {
    private String id;
    private String name;
    
    @JsonProperty("coverUrl")
    private String coverUrl;
    
    private List<String> gradient;
    
    @JsonProperty("songCount")
    private Integer songCount;
    
    private List<String> songs; // song ids
    
    private String type; // user, ai
    
    @JsonProperty("chordProgression")
    private String chordProgression;
}
