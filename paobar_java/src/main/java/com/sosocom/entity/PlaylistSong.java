package com.sosocom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 歌单-歌曲关联实体类
 */
@Data
@TableName("playlist_song")
public class PlaylistSong {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long playlistId;

    private Long songId;

    private Integer position;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
