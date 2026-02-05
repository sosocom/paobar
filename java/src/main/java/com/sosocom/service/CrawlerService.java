package com.sosocom.service;

import com.sosocom.dto.AccountCrawlResultDTO;
import com.sosocom.entity.CrawlerTask;
import com.sosocom.entity.Song;

/**
 * 爬虫服务接口
 */
public interface CrawlerService {

    /**
     * 创建爬虫任务
     */
    CrawlerTask createTask(String url);

    /**
     * 执行爬虫任务
     */
    Song executeTask(Long taskId);

    /**
     * 直接爬取并保存
     */
    Song crawlAndSave(String url);
    
    /**
     * 爬取用户账号的所有歌曲（同步）
     */
    AccountCrawlResultDTO crawlAccount(String accountUrl);
    
    /**
     * 异步爬取用户账号的所有歌曲
     */
    void crawlAccountAsync(String accountUrl);
}
