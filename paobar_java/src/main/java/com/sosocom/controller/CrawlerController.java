package com.sosocom.controller;

import com.sosocom.common.Result;
import com.sosocom.dto.SongDTO;
import com.sosocom.entity.CrawlerTask;
import com.sosocom.entity.Song;
import com.sosocom.service.CrawlerService;
import com.sosocom.service.impl.SongServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 爬虫Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;
    
    @Autowired
    private SongServiceImpl songService;
    
    @Autowired
    private com.sosocom.service.AccountCrawlTaskManager taskManager;

    /**
     * 创建爬虫任务
     */
    @PostMapping("/task")
    public Result<CrawlerTask> createTask(@RequestBody TaskRequest request) {
        log.info("创建爬虫任务: url={}", request.getUrl());
        
        try {
            CrawlerTask task = crawlerService.createTask(request.getUrl());
            return Result.success("任务创建成功", task);
        } catch (Exception e) {
            log.error("创建任务失败", e);
            return Result.error("创建任务失败: " + e.getMessage());
        }
    }

    /**
     * 执行爬虫任务
     */
    @PostMapping("/task/{taskId}/execute")
    public Result<SongDTO> executeTask(@PathVariable Long taskId) {
        log.info("执行爬虫任务: taskId={}", taskId);
        
        try {
            Song song = crawlerService.executeTask(taskId);
            SongDTO dto = songService.convertToDetailDTO(song);
            return Result.success("爬取成功", dto);
        } catch (Exception e) {
            log.error("执行任务失败", e);
            String message = normalizeCrawlErrorMessage(e.getMessage());
            return Result.error(message);
        }
    }

    /**
     * 直接爬取并保存
     */
    @PostMapping("/crawl")
    public Result<SongDTO> crawl(@RequestBody TaskRequest request) {
        log.info("直接爬取: url={}", request.getUrl());
        
        try {
            Song song = crawlerService.crawlAndSave(request.getUrl());
            SongDTO dto = songService.convertToDetailDTO(song);
            return Result.success("爬取成功", dto);
        } catch (Exception e) {
            log.error("爬取失败", e);
            String message = normalizeCrawlErrorMessage(e.getMessage());
            return Result.error(message);
        }
    }
    
    /**
     * 爬取用户账号的所有歌曲
     */
    @PostMapping("/crawl-account")
    public Result<com.sosocom.dto.AccountCrawlResultDTO> crawlAccount(@RequestBody TaskRequest request) {
        log.info("爬取账号: url={}", request.getUrl());
        
        try {
            com.sosocom.dto.AccountCrawlResultDTO result = crawlerService.crawlAccount(request.getUrl());
            return Result.success("爬取完成", result);
        } catch (Exception e) {
            log.error("爬取账号失败", e);
            String message = normalizeCrawlErrorMessage(e.getMessage());
            return Result.error(message);
        }
    }
    
    /**
     * 异步爬取用户账号的所有歌曲（队列化流程）：
     * <ol>
     *   <li>同步阶段：拉齐账号下所有谱链接 → 过滤已在库 / 已在队列 → 剩余写入 crawler_task</li>
     *   <li>异步阶段：唤醒 queue worker，由它逐条消费 PENDING 任务</li>
     * </ol>
     * 接口本身的耗时由同步阶段决定（一般 1-5s），实际抓取在后台执行；
     * 客户端拿到响应后用 userCode 轮询 {@code /crawl-account-status/{userCode}} 看进度。
     */
    @PostMapping("/crawl-account-async")
    public Result<com.sosocom.dto.AccountCrawlResultDTO> crawlAccountAsync(@RequestBody TaskRequest request) {
        log.info("异步爬取账号: url={}", request.getUrl());

        try {
            String userCode = extractUserCodeFromUrl(request.getUrl());
            if (userCode == null || userCode.isEmpty()) {
                return Result.error("无效的用户链接");
            }

            // 防重复提交：同一账号若仍在 RUNNING 状态，先让它跑完再说
            if (taskManager.isTaskRunning(userCode)) {
                return Result.error("该账号正在爬取中，请稍后再试");
            }

            // 1) 同步入队（拉链接 + 过滤 + 落库）
            com.sosocom.dto.AccountCrawlResultDTO stats = crawlerService.enqueueAccount(request.getUrl());

            // 2) 异步唤醒 worker（fire-and-forget）。需要走 Spring 代理，
            //   所以这里调用注入进来的 CrawlerService 而不是某个内部 helper
            crawlerService.triggerQueueWorker();

            log.info("【账号入队】返回前端: total={} alreadyInLib={} alreadyPending={} enqueued={}",
                    stats.getTotalFound(), stats.getAlreadyInLibrary(),
                    stats.getAlreadyPending(), stats.getEnqueued());
            return Result.success("入队完成，已开始后台爬取", stats);
        } catch (Exception e) {
            log.error("提交爬取任务失败", e);
            return Result.error("提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询账号爬取任务状态
     */
    @GetMapping("/crawl-account-status/{userCode}")
    public Result<com.sosocom.service.AccountCrawlTaskManager.AccountCrawlTask> getCrawlStatus(
            @PathVariable String userCode) {
        log.info("查询爬取状态: {}", userCode);
        
        com.sosocom.service.AccountCrawlTaskManager.AccountCrawlTask task = taskManager.getTask(userCode);
        if (task == null) {
            return Result.error("未找到该任务");
        }
        
        return Result.success(task);
    }
    
    /**
     * 从URL提取用户代码
     */
    private String extractUserCodeFromUrl(String accountUrl) {
        if (accountUrl.contains("code=")) {
            int codeIndex = accountUrl.indexOf("code=");
            String code = accountUrl.substring(codeIndex + 5);
            int ampIndex = code.indexOf("&");
            if (ampIndex > 0) {
                code = code.substring(0, ampIndex);
            }
            return code.trim();
        }
        return null;
    }

    private String normalizeCrawlErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "爬取失败";
        }
        // 去重前缀，例如 "爬取失败: 爬取失败: xxx"
        String normalized = message.replaceAll("^(爬取失败[:：]\\s*)+", "爬取失败: ");
        if (!normalized.startsWith("爬取失败")) {
            normalized = "爬取失败: " + normalized;
        }
        return normalized;
    }

    /**
     * 请求参数类
     */
    @lombok.Data
    public static class TaskRequest {
        private String url;
    }
}
