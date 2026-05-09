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
import com.sosocom.util.SongSortKeyUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * 自身代理引用：用于在内部方法里走 Spring AOP 代理调用 @Async 的方法
     * （直接 this.triggerQueueWorker() 会绕开代理，@Async 失效）。
     */
    @Autowired
    @Lazy
    private CrawlerService self;

    @Value("${crawler.retry-times:3}")
    private int retryTimes;

    @Value("${crawler.use-selenium:true}")
    private boolean useSelenium;

    /** >0：在 user/sheets 请求上附带 limit=N&pageSize=N（易被服务端忽略，见配置文件说明）。 */
    @Value("${crawler.account-sheets-page-size:0}")
    private int accountSheetsPageSize;

    /** 附加到 user/sheets 的自定义查询片段，不要前缀 &（可为空）。 */
    @Value("${crawler.yopu-user-sheets-extra-query:}")
    private String yopuUserSheetsExtraQueryRaw;

    /** 拼装后的片段，形如 limit=100&foo=bar。 */
    private String yopuUserSheetsQuerySuffix = "";

    /** 全局互斥：定时任务与 triggerQueueWorker 同一时刻只跑一批。 */
    private final AtomicBoolean crawlBatchRunning = new AtomicBoolean(false);

    /** 每一批最多领取并执行几条（批与批之间由定时器间隔拉出）。 */
    @Value("${crawler.queue-batch-size:5}")
    private int queueBatchSize;

    /** 同一批内两条任务之间的间隔（ms）。 */
    @Value("${crawler.queue-task-interval-ms:800}")
    private long queueTaskIntervalMs;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @PostConstruct
    public void composeYopuUserSheetsQuerySuffix() {
        StringBuilder b = new StringBuilder();
        if (yopuUserSheetsExtraQueryRaw != null && !yopuUserSheetsExtraQueryRaw.isBlank()) {
            String segment = yopuUserSheetsExtraQueryRaw.trim();
            if (segment.startsWith("&")) {
                segment = segment.substring(1);
            }
            if (!segment.isBlank()) {
                b.append(segment);
            }
        }
        if (accountSheetsPageSize > 0) {
            if (!b.isEmpty()) {
                b.append('&');
            }
            b.append("limit=").append(accountSheetsPageSize);
            b.append("&pageSize=").append(accountSheetsPageSize);
        }
        yopuUserSheetsQuerySuffix = b.toString();
        if (!yopuUserSheetsQuerySuffix.isEmpty()) {
            log.info(
                "有谱账号谱列表附加查询片段: `{}` （若仍为每页10条则说明服务端暂未开放分页大小）",
                yopuUserSheetsQuerySuffix
            );
        }
    }

    /**
     * 构造 /api/user/sheets 清单 URL（与 Yopu 前台一致多加 form=all）。
     *
     * @param userCode 用户 code
     * @param page     基于 0 的页码
     */
    private String buildUserSheetsListUrl(String userCode, int page) {
        String baseUrl = String.format(
            "https://yopu.co/api/user/sheets?"
                + "code=%s&sortBy=views&form=all&page=%d&instrument=guitar",
            userCode, page);
        return yopuUserSheetsQuerySuffix.isEmpty()
            ? baseUrl
            : baseUrl + "&" + yopuUserSheetsQuerySuffix;
    }

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

            // 唯一性来源 = original_url，确保字段值与 task.url 对齐
            song.setOriginalUrl(task.getUrl());
            song.setSortKey(SongSortKeyUtil.fromSongName(song.getSongName()));

            // 把 TabDocument JSON 中的 meter 同步到结构化 beat 字段（如有真实值）
            syncBeatColumnFromJson(song);

            // 按 URL 做 upsert：已有则覆盖（保留 id / view_count / favorite_count / create_time）
            Song existing = songMapper.selectOne(
                new QueryWrapper<Song>().eq("original_url", task.getUrl())
            );
            if (existing != null) {
                song.setId(existing.getId());
                song.setViewCount(existing.getViewCount());
                song.setFavoriteCount(existing.getFavoriteCount());
                song.setCreateTime(existing.getCreateTime());
                songMapper.updateById(song);
                log.info("覆盖更新已存在歌曲: id={}, {} - {}",
                    song.getId(), song.getSongName(), song.getArtist());
            } else {
                songMapper.insert(song);
            }

            // 真实拍号回填到同(歌名, 歌手)的旧记录，让历史数据也一起摆脱"未知"
            backfillRealMeterToSiblings(song);

            // 更新任务状态
            task.setTaskStatus("SUCCESS");
            task.setTabId(song.getId());
            task.setErrorMessage(null);
            crawlerTaskMapper.updateById(task);

            // 通知 in-memory 进度管理器：归属账号 +1（accountCode==null 则跳过，
            // 单曲爬取不挂账号，没有进度可以推进）
            if (task.getAccountCode() != null) {
                taskManager.bumpProgress(task.getAccountCode(), true);
            }

            log.info("任务执行成功: {}", taskId);
            return song;

        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);

            String errorMsg = crawlFailureUserMessage(e);
            int maxAttempts = Math.max(1, retryTimes);
            int newRetry = task.getRetryCount() + 1;

            task.setRetryCount(newRetry);
            task.setErrorMessage(errorMsg);
            task.setUpdateTime(LocalDateTime.now());

            if (newRetry >= maxAttempts) {
                log.warn(
                    "任务 {} 已连续失败 {} 次（上限 {}），从待爬队列删除: {}",
                    taskId,
                    newRetry,
                    maxAttempts,
                    task.getUrl()
                );
                crawlerTaskMapper.deleteById(task.getId());
                if (task.getAccountCode() != null) {
                    taskManager.bumpProgress(task.getAccountCode(), false);
                }
            } else {
                task.setTaskStatus("PENDING");
                crawlerTaskMapper.updateById(task);
                log.warn(
                    "任务 {} 第 {} / {} 次失败，回队列待重试: {}",
                    taskId,
                    newRetry,
                    maxAttempts,
                    errorMsg
                );
            }

            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * 面向调用方的简短错误文案（不含堆栈细节）。
     */
    private static String crawlFailureUserMessage(Throwable e) {
        String raw = e.getMessage();
        if (raw != null) {
            if (raw.contains("超时") || raw.contains("timeout")) {
                return "爬取超时，请稍后重试";
            }
            if (raw.contains("连接") || raw.contains("connection")) {
                return "网络连接失败，请检查网络";
            }
            if (raw.contains("403") || raw.contains("404")) {
                return "目标页面不可访问";
            }
        }
        return "爬取失败";
    }

    @Override
    @Transactional
    public Song crawlAndSave(String url) {
        try {
            log.info("开始爬取: {}", url);

            // 创建任务
            CrawlerTask task = createTask(url);

            // 执行任务（按 URL upsert，重复爬同一 URL 视为覆盖更新，不再抛"已存在"）
            return executeTask(task.getId());

        } catch (Exception e) {
            log.error("爬取并保存失败: {}", url, e);

            throw new RuntimeException(crawlFailureUserMessage(e));
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
                    // 直接爬取并保存（按 URL upsert：新增或覆盖）
                    crawlAndSave(songUrl);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    log.info("成功爬取并入库: {} - {}", songInfo.getTitle(), songInfo.getArtist());

                    // 延迟，避免请求过快
                    Thread.sleep(1000);

                } catch (Exception e) {
                    log.error("爬取歌曲失败: {} - {}", songInfo.getTitle(), songInfo.getArtist(), e);
                    result.setFailedCount(result.getFailedCount() + 1);
                }
            }

            // 尝试获取用户名（从第一个歌曲的返回数据中）
            if (!songInfos.isEmpty()) {
                try {
                    String apiUrl = buildUserSheetsListUrl(userCode, 0);
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
    
    // ==========================================================================
    // 账号批量爬取 —— 新流程（队列化）
    // --------------------------------------------------------------------------
    // 1) enqueueAccount(同步)
    //      ├─ 拉齐账号下所有歌曲链接（fetchAllSongInfos）
    //      ├─ 批量过滤掉 song 表里已有的
    //      ├─ 批量过滤掉 crawler_task 队列里已 PENDING/PROCESSING 的
    //      └─ 把剩余 URL 一次性写入 crawler_task（PENDING + account_code = userCode）
    //         同时把统计推给 AccountCrawlTaskManager 让前端能查
    //
    // 2) triggerQueueWorker(@Async)：立刻异步跑【一批】（与定时器互斥）。
    //
    // 3) @Scheduled fixedDelay：
    //      └─ 周期性检查 crawler_task（PENDING/PROCESSING）；有则 consumeCrawlQueueBatch，
    //         每轮最多 queue-batch-size 条，轮与轮之间相隔 queue-fixed-delay-ms。
    //
    // 4) executeTask 完成时通过 taskManager.bumpProgress 把账号进度推进
    //    （单曲爬取 account_code 为 null，自动跳过这一步）
    //
    // 5) 应用启动事件里复位 PROCESSING → PENDING（上次进程崩了的残留）+ 异步跑一批唤醒
    // ==========================================================================

    @Override
    public AccountCrawlResultDTO enqueueAccount(String accountUrl) {
        String userCode = extractUserCode(accountUrl);
        if (userCode == null || userCode.isEmpty()) {
            throw new RuntimeException("无效的用户链接，无法提取用户代码");
        }
        log.info("【账号入队】开始: {}", userCode);

        try {
            // 1) 拉齐账号下所有歌曲信息
            List<SongInfo> songInfos = fetchAllSongInfos(userCode);
            String userName = getUserName(userCode);

            // 同账号内的源数据可能有重复（极少见但保险起见），先按 URL 去重保留首次出现顺序
            LinkedHashMap<String, SongInfo> uniqueByUrl = new LinkedHashMap<>();
            for (SongInfo info : songInfos) {
                String url = "https://yopu.co/view/" + info.getId();
                uniqueByUrl.putIfAbsent(url, info);
            }
            int totalFound = uniqueByUrl.size();
            log.info("【账号入队】发现 {} 个独立链接 (原始 {} 条)",
                    totalFound, songInfos.size());

            // 2) 过滤已在 song 表的链接
            Set<String> alreadyInLib = totalFound == 0
                    ? new HashSet<>()
                    : new HashSet<>(songMapper.findExistingOriginalUrls(uniqueByUrl.keySet()));
            log.info("【账号入队】已在 song 表中: {} 条", alreadyInLib.size());

            // 3) 过滤已在 crawler_task 队列里 PENDING/PROCESSING 的链接
            //   （上次还没爬完就再次提交 / 多账号有重叠链接 都会落到这里）
            LinkedHashSet<String> candidates = new LinkedHashSet<>();
            for (String u : uniqueByUrl.keySet()) {
                if (!alreadyInLib.contains(u)) {
                    candidates.add(u);
                }
            }
            Set<String> alreadyPendingSet = candidates.isEmpty()
                    ? new HashSet<>()
                    : new HashSet<>(crawlerTaskMapper.findOpenQueueUrls(candidates));
            log.info("【账号入队】已在队列中: {} 条", alreadyPendingSet.size());

            // 4) 把剩余 URL 一次性写入 crawler_task（PENDING）
            int enqueued = 0;
            LocalDateTime now = LocalDateTime.now();
            for (String url : candidates) {
                if (alreadyPendingSet.contains(url)) {
                    continue;
                }
                CrawlerTask task = new CrawlerTask();
                task.setUrl(url);
                task.setTaskStatus("PENDING");
                task.setRetryCount(0);
                task.setAccountCode(userCode);
                task.setCreateTime(now);
                task.setUpdateTime(now);
                crawlerTaskMapper.insert(task);
                enqueued++;
            }
            log.info("【账号入队】新入队: {} 条", enqueued);

            // 5) 推一份初始进度给 in-memory manager
            taskManager.startTask(userCode, userName,
                    totalFound, alreadyInLib.size(), alreadyPendingSet.size(), enqueued);

            // 6) 组装一次性返回值
            AccountCrawlResultDTO result = new AccountCrawlResultDTO();
            result.setUserCode(userCode);
            result.setUserName(userName);
            result.setTotalFound(totalFound);
            result.setSuccessCount(0);
            result.setFailedCount(0);
            result.setSkippedCount(alreadyInLib.size() + alreadyPendingSet.size());
            result.setAlreadyInLibrary(alreadyInLib.size());
            result.setAlreadyPending(alreadyPendingSet.size());
            result.setEnqueued(enqueued);
            return result;

        } catch (Exception e) {
            log.error("【账号入队】失败", e);
            taskManager.failTask(userCode, e.getMessage());
            throw new RuntimeException("入队失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void triggerQueueWorker() {
        consumeCrawlQueueBatch();
    }

    /**
     * 定时分批拉队列：上一轮跑完后相隔 {@code queue-fixed-delay-ms} 再触发；
     * 无待处理（PENDING/PROCESSING）则快速返回。
     */
    @Scheduled(
        fixedDelayString = "${crawler.queue-fixed-delay-ms:60000}",
        initialDelayString = "${crawler.queue-initial-delay-ms:15000}"
    )
    public void scheduledDrainCrawlQueue() {
        if (crawlerTaskMapper.countOpenTasks() <= 0L) {
            return;
        }
        consumeCrawlQueueBatch();
    }

    /**
     * 领取并执行至多 {@link #queueBatchSize} 条 PENDING。
     *
     * @return 实际执行到的条数（含失败并已落 FAILED 的行）
     */
    private int consumeCrawlQueueBatch() {
        if (!crawlBatchRunning.compareAndSet(false, true)) {
            log.trace("爬虫队列本批已由其他线程执行，跳过本次触发");
            return 0;
        }
        int processed = 0;
        try {
            int cap = Math.max(1, queueBatchSize);
            for (int i = 0; i < cap; i++) {
                Long taskId = claimNextPendingTaskId();
                if (taskId == null) {
                    break;
                }
                try {
                    executeTask(taskId);
                } catch (Exception e) {
                    log.warn("队列任务 {} 执行失败: {}", taskId, e.getMessage());
                }
                processed++;
                if (queueTaskIntervalMs > 0 && i < cap - 1) {
                    try {
                        Thread.sleep(queueTaskIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            if (processed > 0) {
                log.info("爬虫队列本批结束，处理 {} 条（单批上限 {}）", processed, cap);
            }
            return processed;
        } finally {
            crawlBatchRunning.set(false);
        }
    }

    /**
     * FIFO 领取一条 PENDING：先 SELECT 最小 id，再 UPDATE 把它翻 PROCESSING。
     * 中间被别的 worker 抢到 → markProcessing 影响 0 行，循环再选下一条。
     * 队列彻底为空 → 返回 null。
     */
    private Long claimNextPendingTaskId() {
        for (int attempt = 0; attempt < 8; attempt++) {
            Long id = crawlerTaskMapper.selectNextPendingId();
            if (id == null) {
                return null;
            }
            int affected = crawlerTaskMapper.markProcessing(id);
            if (affected > 0) {
                return id;
            }
            // 这里说明并发抢占，循环重试再 select 下一条
        }
        return null;
    }

    /**
     * 应用启动后做两件事：
     * <ol>
     *   <li>把上次进程崩在路上的 PROCESSING 行复位到 PENDING（重启自愈）</li>
     *   <li>立刻 kick 一次 worker 把队列里的 PENDING 跑掉</li>
     * </ol>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            int reset = crawlerTaskMapper.resetStaleProcessing();
            if (reset > 0) {
                log.info("启动自愈：把 {} 条遗留的 PROCESSING 复位为 PENDING", reset);
            }
            long open = crawlerTaskMapper.countOpenTasks();
            if (open > 0) {
                log.info("启动时发现 {} 条 PENDING 任务，唤醒 worker", open);
                self.triggerQueueWorker();
            }
        } catch (Exception e) {
            log.warn("启动自愈/唤醒 worker 失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户名
     */
    private String getUserName(String userCode) {
        try {
            String apiUrl = buildUserSheetsListUrl(userCode, 0);
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
            String apiUrl = buildUserSheetsListUrl(userCode, page);

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

            if (page == 0
                && accountSheetsPageSize > 0
                && jsonArray.size() < accountSheetsPageSize) {
                log.info(
                    "账号谱列表第 {} 页仅返回 {} 条（配置了期待每页约 {}）；"
                        + "Yopu 常为固定每页 10，URL 仍可翻页汇总完整清单。",
                    page, jsonArray.size(), accountSheetsPageSize
                );
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

    // ==========================================================================
    // 拍号回填相关
    // --------------------------------------------------------------------------
    // 现状：yopu 源页面里 拍号(meter) 没填的歌会被渲染成字面 "未知"，TabHtmlNormalizer
    //      原样写入 tab_content_json.$.meter。前端鼓机的默认节奏型来自 meter
    //      （drumBus → pickDefaultStyle(meter)），meter="未知" 时只能退到 4/4 默认。
    // 策略：每次新爬到一首歌，若 JSON.meter 是真实拍号（非 null/未知/空），就：
    //      1) 同步到本歌的结构化 beat 字段（数据库内两份字段保持一致）；
    //      2) 把同 (song_name, artist) 的旧记录里仍是 未知/null/空 的 meter
    //         一并升级为新值（JSON_SET 单次 UPDATE），让历史数据自动受益、
    //         前端鼓机推荐节奏也跟着变。
    // ==========================================================================

    /** 把 TabDocument JSON 里的 meter 写到 song.beat 字段（仅在是真实拍号时）。 */
    private void syncBeatColumnFromJson(Song song) {
        String meter = extractMeter(song.getTabContentJson());
        if (isRealMeter(meter)) {
            song.setBeat(meter);
        }
    }

    /** 真实拍号回填：同(歌名, 歌手) 的其它老记录里 meter 缺失/未知的，都升级到新值。 */
    private void backfillRealMeterToSiblings(Song song) {
        String meter = extractMeter(song.getTabContentJson());
        if (!isRealMeter(meter)) {
            return;
        }
        if (song.getId() == null
            || song.getSongName() == null || song.getSongName().isBlank()
            || song.getArtist() == null || song.getArtist().isBlank()) {
            return;
        }

        try {
            int affected = songMapper.backfillMeterByName(
                meter, song.getSongName(), song.getArtist(), song.getId()
            );
            if (affected > 0) {
                log.info("拍号回填成功：把 '{}' 同步到 [{} - {}] 的 {} 条历史记录",
                    meter, song.getSongName(), song.getArtist(), affected);
            }
        } catch (Exception e) {
            // 回填失败不应阻断爬取主流程，仅记录日志
            log.warn("拍号回填失败 [{} - {}]: {}",
                song.getSongName(), song.getArtist(), e.getMessage());
        }
    }

    private String extractMeter(String tabContentJson) {
        if (tabContentJson == null || tabContentJson.isEmpty()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(tabContentJson);
            JsonNode meterNode = root.get("meter");
            return (meterNode != null && !meterNode.isNull()) ? meterNode.asText() : null;
        } catch (Exception e) {
            log.warn("解析 tab_content_json.meter 失败: {}", e.getMessage());
            return null;
        }
    }

    private boolean isRealMeter(String meter) {
        if (meter == null) return false;
        String trimmed = meter.trim();
        return !trimmed.isEmpty()
            && !"未知".equals(trimmed)
            && !"null".equalsIgnoreCase(trimmed);
    }
}
