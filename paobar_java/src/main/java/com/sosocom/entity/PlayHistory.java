package com.sosocom.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户播放历史（upsert 语义：一个 (userId, songId) 只一行，重复打开累加 play_count + 刷新 last_played_at）。
 */
@Data
@TableName("play_history")
public class PlayHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long songId;

    /** 累计打开次数 */
    private Integer playCount;

    /** 最近一次打开时间（列表按这个字段倒序） */
    private LocalDateTime lastPlayedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
