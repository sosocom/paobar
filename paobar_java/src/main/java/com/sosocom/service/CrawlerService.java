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
     * 账号批量爬取的"入队"阶段（同步执行，预期 1~5 秒）：
     * <ol>
     *   <li>请求 yopu API 拉齐账号下所有歌曲链接</li>
     *   <li>过滤掉 song 表里已有的</li>
     *   <li>过滤掉 crawler_task 队列里已 PENDING/PROCESSING 的</li>
     *   <li>把剩余 URL 一次性写入 crawler_task（task_status=PENDING, account_code=userCode）</li>
     *   <li>把统计写入 {@link AccountCrawlTaskManager}，等候 worker 消费</li>
     * </ol>
     * 调用方拿到结果后仍可调 {@link #triggerQueueWorker()} 尽快处理【一批】；
     * 日常由 {@code @Scheduled} 按配置间隔持续分批消费。
     *
     * @param accountUrl 形如 {@code https://yopu.co/user#code=xxx}
     * @return 完整统计：发现总数 / 已在库 / 已在队列 / 新入队 等
     */
    AccountCrawlResultDTO enqueueAccount(String accountUrl);

    /**
     * 异步执行【一批】队列任务（与定时任务共享同一互斥与批量上限）。
     * <ul>
     *   <li>入队后立即调用可缩短首包延迟</li>
     *   <li>其余任务由定时调度按间隔继续拉取</li>
     * </ul>
     */
    void triggerQueueWorker();
}
