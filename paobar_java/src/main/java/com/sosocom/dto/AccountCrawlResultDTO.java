package com.sosocom.dto;

import lombok.Data;

/**
 * 账号爬取结果DTO
 */
@Data
public class AccountCrawlResultDTO {
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 用户代码
     */
    private String userCode;
    
    /**
     * 总共发现的歌曲数
     */
    private Integer totalFound;
    
    /**
     * 成功爬取的歌曲数
     */
    private Integer successCount;
    
    /**
     * 失败的歌曲数
     */
    private Integer failedCount;
    
    /**
     * 跳过的歌曲数（已存在）。
     * 新流程下 = alreadyInLibrary（爬取前就在 song 表里）+ alreadyPending
     * （已经在 crawler_task 队列里 PENDING/PROCESSING）。保留单字段以兼容前端老 UI。
     */
    private Integer skippedCount;

    /** 入队前发现 已在 song 表里 的链接数。 */
    private Integer alreadyInLibrary;

    /** 入队前发现 已在 crawler_task 队列里待消费 的链接数。 */
    private Integer alreadyPending;

    /** 实际新加入待爬队列的链接数（= totalFound - alreadyInLibrary - alreadyPending）。 */
    private Integer enqueued;
}
