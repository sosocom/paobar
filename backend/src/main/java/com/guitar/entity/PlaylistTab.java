package com.guitar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 歌单-吉他谱关联实体
 */
@Data
@TableName("playlist_tab")
public class PlaylistTab {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long playlistId;

    private Long tabId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
