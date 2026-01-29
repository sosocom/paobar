package com.guitar.service.impl;

import com.guitar.crawler.SeleniumCrawler;
import com.guitar.crawler.YopuCrawler;
import com.guitar.entity.CrawlerTask;
import com.guitar.entity.GuitarTab;
import com.guitar.mapper.CrawlerTaskMapper;
import com.guitar.service.CrawlerService;
import com.guitar.service.GuitarTabService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 爬虫服务实现类
 */
@Slf4j
@Service
public class CrawlerServiceImpl implements CrawlerService {

    @Autowired
    private SeleniumCrawler seleniumCrawler;

    @Autowired
    private YopuCrawler yopuCrawler;

    @Autowired
    private GuitarTabService guitarTabService;

    @Autowired
    private CrawlerTaskMapper crawlerTaskMapper;

    @Value("${crawler.retry-times}")
    private int retryTimes;

    @Value("${crawler.use-selenium:true}")
    private boolean useSelenium;

    @Override
    @Transactional
    public CrawlerTask createTask(String url) {
        CrawlerTask task = new CrawlerTask();
        task.setUrl(url);
        task.setTaskStatus("PENDING");
        task.setRetryCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        crawlerTaskMapper.insert(task);
        log.info("创建爬虫任务: {}", task.getId());
        
        return task;
    }

    @Override
    @Transactional
    public GuitarTab executeTask(Long taskId) {
        CrawlerTask task = crawlerTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        task.setTaskStatus("PROCESSING");
        task.setUpdateTime(LocalDateTime.now());
        crawlerTaskMapper.updateById(task);

        try {
            // 根据配置选择爬虫
            GuitarTab tab;
            if (useSelenium) {
                log.info("使用 Selenium 爬虫（支持动态渲染）");
                tab = seleniumCrawler.crawlTabDetail(task.getUrl());
            } else {
                log.info("使用 Jsoup 爬虫（仅支持静态内容）");
                tab = yopuCrawler.crawlTabDetail(task.getUrl());
            }
            
            // ============================================
            // 删除相同 URL 的旧记录（如果存在）
            // ============================================
            guitarTabService.deleteByUrl(task.getUrl());
            
            // 保存新的吉他谱
            guitarTabService.save(tab);
            
            // 更新任务状态
            task.setTaskStatus("SUCCESS");
            task.setTabId(tab.getId());
            task.setErrorMessage(null);
            crawlerTaskMapper.updateById(task);
            
            log.info("任务执行成功: {}", taskId);
            return tab;
            
        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            
            // 更新失败状态
            task.setTaskStatus("FAILED");
            task.setRetryCount(task.getRetryCount() + 1);
            task.setErrorMessage(e.getMessage());
            crawlerTaskMapper.updateById(task);
            
            throw new RuntimeException("爬取失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public GuitarTab crawlAndSave(String url) {
        try {
            log.info("开始爬取: {}", url);
            
            // 创建任务
            CrawlerTask task = createTask(url);
            
            // 执行任务
            return executeTask(task.getId());
            
        } catch (Exception e) {
            log.error("爬取并保存失败: {}", url, e);
            throw new RuntimeException("爬取失败: " + e.getMessage(), e);
        }
    }
}
