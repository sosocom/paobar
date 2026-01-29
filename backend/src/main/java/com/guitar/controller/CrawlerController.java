package com.guitar.controller;

import com.guitar.common.Result;
import com.guitar.entity.CrawlerTask;
import com.guitar.entity.GuitarTab;
import com.guitar.service.CrawlerService;
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
    public Result<GuitarTab> executeTask(@PathVariable Long taskId) {
        log.info("执行爬虫任务: taskId={}", taskId);
        
        try {
            GuitarTab tab = crawlerService.executeTask(taskId);
            return Result.success("爬取成功", tab);
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
    public Result<GuitarTab> crawl(@RequestBody TaskRequest request) {
        log.info("直接爬取: url={}", request.getUrl());
        
        try {
            GuitarTab tab = crawlerService.crawlAndSave(request.getUrl());
            return Result.success("爬取成功", tab);
        } catch (Exception e) {
            log.error("爬取失败", e);
            String message = normalizeCrawlErrorMessage(e.getMessage());
            return Result.error(message);
        }
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
