package com.sosocom.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 爬虫任务实体类
 */
@Data
@TableName("crawler_task")
public class CrawlerTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String url;

    private String taskStatus;

    private Integer retryCount;

    private String errorMessage;

    private Long tabId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
