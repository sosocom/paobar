package com.sosocom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 */
@Data
@TableName("user_favorite")
public class UserFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long songId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
