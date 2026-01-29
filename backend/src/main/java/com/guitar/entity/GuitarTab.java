package com.guitar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 吉他谱实体类
 */
@Data
@TableName("guitar_tab")
public class GuitarTab {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String songName;

    private String artist;

    private String originalUrl;

    private String lyrics;

    private String tabContent;

    private String tabImageUrl;

    private String difficulty;

    private String tuning;

    private Integer capo;

    private String playKey;

    private String originalKey;

    private String beat;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
