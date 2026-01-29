package com.guitar.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@TableName("tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;

    private String tagType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
