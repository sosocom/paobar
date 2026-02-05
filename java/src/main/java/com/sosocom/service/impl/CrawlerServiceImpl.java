package com.sosocom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosocom.crawler.SeleniumCrawler;
import com.sosocom.crawler.YopuCrawler;
import com.sosocom.dto.AccountCrawlResultDTO;
import com.sosocom.entity.CrawlerTask;
import com.sosocom.entity.Song;
import com.sosocom.mapper.CrawlerTaskMapper;
import com.sosocom.mapper.SongMapper;
import com.sosocom.service.AccountCrawlTaskManager;
import com.sosocom.service.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private SongMapper songMapper;

    @Autowired
    private CrawlerTaskMapper crawlerTaskMapper;
    
    @Autowired
    private AccountCrawlTaskManager taskManager;

    @Value("${crawler.retry-times:3}")
    private int retryTimes;

    @Value("${crawler.use-selenium:true}")
    private boolean useSelenium;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

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
    public Song executeTask(Long taskId) {
        CrawlerTask task = crawlerTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        task.setTaskStatus("PROCESSING");
        task.setUpdateTime(LocalDateTime.now());
        crawlerTaskMapper.updateById(task);

        try {
            // 根据配置选择爬虫
            Song song;
            if (useSelenium) {
                log.info("使用 Selenium 爬虫（支持动态渲染）");
                song = seleniumCrawler.crawlTabDetail(task.getUrl());
            } else {
                log.info("使用 Jsoup 爬虫（仅支持静态内容）");
                song = yopuCrawler.crawlTabDetail(task.getUrl());
            }
            
            // 删除相同 URL 的旧记录（如果存在）
            QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("original_url", task.getUrl());
            songMapper.delete(queryWrapper);
            
            // 保存新的歌曲
            try {
                songMapper.insert(song);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 唯一键冲突（歌名+歌手已存在）
                log.warn("歌曲已存在: {} - {}", song.getSongName(), song.getArtist());
                throw new RuntimeException("歌曲已存在，无需重复爬取");
            }
            
            // 更新任务状态
            task.setTaskStatus("SUCCESS");
            task.setTabId(song.getId());
            task.setErrorMessage(null);
            crawlerTaskMapper.updateById(task);
            
            log.info("任务执行成功: {}", taskId);
            return song;
            
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("歌曲已存在: {}", task.getUrl());
            
            // 更新任务状态为已存在
            task.setTaskStatus("SKIPPED");
            task.setErrorMessage("歌曲已存在");
            crawlerTaskMapper.updateById(task);
            
            throw new RuntimeException("歌曲已存在，无需重复爬取");
            
        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            
            // 检查是否是唯一键冲突
            if (e.getMessage() != null && 
                (e.getMessage().contains("歌曲已存在") || 
                 e.getMessage().contains("Duplicate entry"))) {
                
                task.setTaskStatus("SKIPPED");
                task.setErrorMessage("歌曲已存在");
                crawlerTaskMapper.updateById(task);
                
                throw new RuntimeException("歌曲已存在，无需重复爬取");
            }
            
            // 更新失败状态
            task.setTaskStatus("FAILED");
            task.setRetryCount(task.getRetryCount() + 1);
            
            // 隐藏详细错误信息
            String errorMsg = "爬取失败";
            if (e.getMessage() != null) {
                if (e.getMessage().contains("超时") || e.getMessage().contains("timeout")) {
                    errorMsg = "爬取超时，请稍后重试";
                } else if (e.getMessage().contains("连接") || e.getMessage().contains("connection")) {
                    errorMsg = "网络连接失败，请检查网络";
                } else if (e.getMessage().contains("403") || e.getMessage().contains("404")) {
                    errorMsg = "目标页面不可访问";
                }
            }
            
            task.setErrorMessage(errorMsg);
            crawlerTaskMapper.updateById(task);
            
            throw new RuntimeException(errorMsg);
        }
    }

    @Override
    @Transactional
    public Song crawlAndSave(String url) {
        try {
            log.info("开始爬取: {}", url);
            
            // 创建任务
            CrawlerTask task = createTask(url);
            
            // 执行任务
            return executeTask(task.getId());
            
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 唯一键冲突，歌曲已存在
            log.warn("歌曲已存在: {}", url);
            throw new RuntimeException("歌曲已存在，无需重复爬取");
            
        } catch (Exception e) {
            log.error("爬取并保存失败: {}", url, e);
            
            // 检查是否是数据库唯一键冲突
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new RuntimeException("歌曲已存在，无需重复爬取");
            }
            
            // 隐藏详细的数据库错误信息
            String errorMsg = "爬取失败";
            if (e.getMessage() != null) {
                if (e.getMessage().contains("超时") || e.getMessage().contains("timeout")) {
                    errorMsg = "爬取超时，请稍后重试";
                } else if (e.getMessage().contains("连接") || e.getMessage().contains("connection")) {
                    errorMsg = "网络连接失败，请检查网络";
                } else if (e.getMessage().contains("403") || e.getMessage().contains("404")) {
                    errorMsg = "目标页面不可访问";
                }
            }
            
            throw new RuntimeException(errorMsg);
        }
    }
    
    @Override
    @Transactional
    public AccountCrawlResultDTO crawlAccount(String accountUrl) {
        log.info("开始爬取账号: {}", accountUrl);
        
        AccountCrawlResultDTO result = new AccountCrawlResultDTO();
        result.setTotalFound(0);
        result.setSuccessCount(0);
        result.setFailedCount(0);
        result.setSkippedCount(0);
        
        try {
            // 从 URL 中提取 code
            String userCode = extractUserCode(accountUrl);
            if (userCode == null || userCode.isEmpty()) {
                throw new RuntimeException("无效的用户链接，无法提取用户代码");
            }
            
            result.setUserCode(userCode);
            log.info("提取到用户代码: {}", userCode);
            
            // 获取所有歌曲信息（包含标题和艺术家）
            List<SongInfo> songInfos = fetchAllSongInfos(userCode);
            result.setTotalFound(songInfos.size());
            log.info("发现 {} 首歌曲", songInfos.size());
            
            // 逐个爬取歌曲
            for (int i = 0; i < songInfos.size(); i++) {
                SongInfo songInfo = songInfos.get(i);
                String songUrl = "https://yopu.co/view/" + songInfo.getId();
                
                log.info("正在爬取第 {}/{} 首: {} - {}", i + 1, songInfos.size(), 
                    songInfo.getTitle(), songInfo.getArtist());
                
                try {
                    // 直接爬取并保存（即时入库）
                    crawlAndSave(songUrl);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    log.info("成功爬取并入库: {} - {}", songInfo.getTitle(), songInfo.getArtist());
                    
                    // 延迟，避免请求过快
                    Thread.sleep(1000);
                    
                } catch (org.springframework.dao.DuplicateKeyException e) {
                    // 歌曲已存在（唯一键冲突），跳过
                    log.info("歌曲已存在（{}+{}），跳过", songInfo.getTitle(), songInfo.getArtist());
                    result.setSkippedCount(result.getSkippedCount() + 1);
                    
                } catch (Exception e) {
                    // 检查是否是歌曲已存在的错误
                    if (e.getMessage() != null && 
                        (e.getMessage().contains("歌曲已存在") || 
                         e.getMessage().contains("Duplicate entry"))) {
                        log.info("歌曲已存在（{}+{}），跳过", songInfo.getTitle(), songInfo.getArtist());
                        result.setSkippedCount(result.getSkippedCount() + 1);
                    } else {
                        log.error("爬取歌曲失败: {} - {}", songInfo.getTitle(), songInfo.getArtist(), e);
                        result.setFailedCount(result.getFailedCount() + 1);
                    }
                }
            }
            
            // 尝试获取用户名（从第一个歌曲的返回数据中）
            if (!songInfos.isEmpty()) {
                try {
                    String apiUrl = String.format(
                        "https://yopu.co/api/user/sheets?code=%s&sortBy=views&form=all&page=0&instrument=guitar",
                        userCode
                    );
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .GET()
                        .build();
                    
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    JsonNode jsonArray = objectMapper.readTree(response.body());
                    
                    if (jsonArray.isArray() && jsonArray.size() > 0) {
                        JsonNode firstSong = jsonArray.get(0);
                        JsonNode owner = firstSong.get("owner");
                        if (owner != null && owner.has("displayName")) {
                            result.setUserName(owner.get("displayName").asText());
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取用户名失败", e);
                }
            }
            
            log.info("账号爬取完成: 总计={}, 成功={}, 失败={}, 跳过={}", 
                result.getTotalFound(), result.getSuccessCount(), 
                result.getFailedCount(), result.getSkippedCount());
            
            return result;
            
        } catch (Exception e) {
            log.error("爬取账号失败", e);
            throw new RuntimeException("爬取账号失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    public void crawlAccountAsync(String accountUrl) {
        String userCode = null;
        try {
            // 从 URL 中提取 code
            userCode = extractUserCode(accountUrl);
            if (userCode == null || userCode.isEmpty()) {
                throw new RuntimeException("无效的用户链接，无法提取用户代码");
            }
            
            log.info("【异步任务】开始爬取账号: {}", userCode);
            
            // 获取所有歌曲信息
            List<SongInfo> songInfos = fetchAllSongInfos(userCode);
            
            // 获取用户名
            String userName = getUserName(userCode);
            
            // 开始任务
            taskManager.startTask(userCode, userName);
            
            // 初始化结果
            AccountCrawlResultDTO result = new AccountCrawlResultDTO();
            result.setUserCode(userCode);
            result.setUserName(userName);
            result.setTotalFound(songInfos.size());
            result.setSuccessCount(0);
            result.setFailedCount(0);
            result.setSkippedCount(0);
            
            log.info("发现 {} 首歌曲", songInfos.size());
            taskManager.updateProgress(userCode, 0, songInfos.size());
            
            // 逐个爬取歌曲
            for (int i = 0; i < songInfos.size(); i++) {
                SongInfo songInfo = songInfos.get(i);
                String songUrl = "https://yopu.co/view/" + songInfo.getId();
                
                log.info("正在爬取第 {}/{} 首: {} - {}", i + 1, songInfos.size(), 
                    songInfo.getTitle(), songInfo.getArtist());
                
                try {
                    // 直接爬取并保存（即时入库）
                    crawlAndSave(songUrl);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    log.info("成功爬取并入库: {} - {}", songInfo.getTitle(), songInfo.getArtist());
                    
                } catch (org.springframework.dao.DuplicateKeyException e) {
                    // 歌曲已存在（唯一键冲突），跳过
                    log.info("歌曲已存在（{}+{}），跳过", songInfo.getTitle(), songInfo.getArtist());
                    result.setSkippedCount(result.getSkippedCount() + 1);
                    
                } catch (Exception e) {
                    // 检查是否是歌曲已存在的错误
                    if (e.getMessage() != null && 
                        (e.getMessage().contains("歌曲已存在") || 
                         e.getMessage().contains("Duplicate entry"))) {
                        log.info("歌曲已存在（{}+{}），跳过", songInfo.getTitle(), songInfo.getArtist());
                        result.setSkippedCount(result.getSkippedCount() + 1);
                    } else {
                        log.error("爬取歌曲失败: {} - {}", songInfo.getTitle(), songInfo.getArtist(), e);
                        result.setFailedCount(result.getFailedCount() + 1);
                    }
                }
                
                // 更新进度
                taskManager.updateProgress(userCode, i + 1, songInfos.size());
                
                // 延迟，避免请求过快
                Thread.sleep(1000);
            }
            
            log.info("【异步任务】账号爬取完成: 总计={}, 成功={}, 失败={}, 跳过={}", 
                result.getTotalFound(), result.getSuccessCount(), 
                result.getFailedCount(), result.getSkippedCount());
            
            // 完成任务
            taskManager.completeTask(userCode, result);
            
        } catch (Exception e) {
            log.error("【异步任务】爬取账号失败", e);
            if (userCode != null) {
                taskManager.failTask(userCode, e.getMessage());
            }
        }
    }
    
    /**
     * 获取用户名
     */
    private String getUserName(String userCode) {
        try {
            String apiUrl = String.format(
                "https://yopu.co/api/user/sheets?code=%s&sortBy=views&form=all&page=0&instrument=guitar",
                userCode
            );
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonArray = objectMapper.readTree(response.body());
            
            if (jsonArray.isArray() && jsonArray.size() > 0) {
                JsonNode firstSong = jsonArray.get(0);
                JsonNode owner = firstSong.get("owner");
                if (owner != null && owner.has("displayName")) {
                    return owner.get("displayName").asText();
                }
            }
        } catch (Exception e) {
            log.warn("获取用户名失败", e);
        }
        return null;
    }
    
    /**
     * 从URL中提取用户代码
     */
    private String extractUserCode(String accountUrl) {
        // 支持格式: https://yopu.co/user#code=b19JMeQX
        if (accountUrl.contains("code=")) {
            int codeIndex = accountUrl.indexOf("code=");
            String code = accountUrl.substring(codeIndex + 5);
            // 移除可能的查询参数
            int ampIndex = code.indexOf("&");
            if (ampIndex > 0) {
                code = code.substring(0, ampIndex);
            }
            return code.trim();
        }
        return null;
    }
    
    /**
     * 获取用户的所有歌曲信息（包含ID、标题、艺术家）
     */
    private List<SongInfo> fetchAllSongInfos(String userCode) throws IOException, InterruptedException {
        List<SongInfo> songInfos = new ArrayList<>();
        int page = 0;
        
        while (true) {
            String apiUrl = String.format(
                "https://yopu.co/api/user/sheets?code=%s&sortBy=views&form=all&page=%d&instrument=guitar",
                userCode, page
            );
            
            log.info("请求第 {} 页: {}", page, apiUrl);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 解析 JSON
            JsonNode jsonArray = objectMapper.readTree(response.body());
            
            // 如果返回为空数组，说明没有更多数据
            if (!jsonArray.isArray() || jsonArray.size() == 0) {
                log.info("第 {} 页无数据，停止获取", page);
                break;
            }
            
            // 提取所有歌曲信息
            for (JsonNode song : jsonArray) {
                if (song.has("id") && song.has("title") && song.has("artist")) {
                    SongInfo info = new SongInfo();
                    info.setId(song.get("id").asText());
                    info.setTitle(song.get("title").asText());
                    info.setArtist(song.get("artist").asText());
                    songInfos.add(info);
                }
            }
            
            log.info("第 {} 页获取到 {} 首歌曲", page, jsonArray.size());
            
            // 下一页
            page++;
            
            // 延迟，避免请求过快
            Thread.sleep(500);
        }
        
        return songInfos;
    }
    
    /**
     * 歌曲信息内部类
     */
    @lombok.Data
    private static class SongInfo {
        private String id;
        private String title;
        private String artist;
    }
}
