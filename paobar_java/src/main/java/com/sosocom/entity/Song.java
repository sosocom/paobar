package com.sosocom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 歌曲实体类
 */
@Data
@TableName("song")
public class Song {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String songName;

    /** 歌名拼音排序键（全拼小写），列表分页按此字段排序。 */
    private String sortKey;

    private String artist;

    private String originalUrl;

    private String lyrics;

    /** 规范化吉他谱 JSON 字符串（对应表字段 tab_content_json）。 */
    private String tabContentJson;

    private String tabImageUrl;

    private String meta;

    private String difficulty;

    private String tuning;

    private Integer capo;

    private String playKey;

    private String originalKey;

    private String beat;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
