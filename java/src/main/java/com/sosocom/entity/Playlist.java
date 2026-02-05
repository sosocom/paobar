package com.sosocom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 歌单实体类
 */
@Data
@TableName("playlist")
public class Playlist {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String coverUrl;

    private String gradientStart;

    private String gradientEnd;

    private Integer songCount;

    private String type; // user, ai

    private String chordProgression;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
