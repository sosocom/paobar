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

    /**
     * 触发该任务的账号 code（仅账号批量爬取时填）。单曲爬取保持为 null。
     * 用作队列检索 / 进度归属，不参与唯一性。
     */
    private String accountCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
