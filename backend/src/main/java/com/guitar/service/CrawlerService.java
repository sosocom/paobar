package com.guitar.service;

import com.guitar.entity.CrawlerTask;
import com.guitar.entity.GuitarTab;

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
    GuitarTab executeTask(Long taskId);

    /**
     * 直接爬取并保存
     */
    GuitarTab crawlAndSave(String url);
}
