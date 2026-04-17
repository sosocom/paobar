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
     * 跳过的歌曲数（已存在）
     */
    private Integer skippedCount;
}
