package com.sosocom.service;

import com.sosocom.dto.AccountCrawlResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 账号批量爬取任务的进度管理器（in-memory）。
 *
 * <p>新流程语义：</p>
 * <ul>
 *   <li>{@code total} = 本次提交"实际入队"的链接数（已入库 / 已在队列的不计）</li>
 *   <li>{@code current} = 这批入队任务里已经被 worker 处理完（成功 + 失败）的数量</li>
 *   <li>{@code progress} = current / total 百分比</li>
 *   <li>当 current == total 时自动转为 COMPLETED 并写入 result，5 秒后从 map 移除</li>
 * </ul>
 *
 * <p>"已在库 / 已在队列" 这类未真实入队的统计在 {@link #startTask} 时一次性写入
 * task.{@code skippedCount}/{@code alreadyInLibrary}/{@code alreadyPending}，
 * 不再随每条任务变化。</p>
 *
 * <p>数据只活在内存里；服务重启后这里会清空，但 crawler_task 表里 account_code
 * 仍可让 {@link com.sosocom.mapper.CrawlerTaskMapper#countByAccountAndStatuses}
 * 重建出统计。</p>
 */
@Slf4j
@Component
public class AccountCrawlTaskManager {

    private final Map<String, AccountCrawlTask> runningTasks = new ConcurrentHashMap<>();

    public boolean isTaskRunning(String userCode) {
        AccountCrawlTask task = runningTasks.get(userCode);
        return task != null && "RUNNING".equals(task.getStatus());
    }

    public AccountCrawlTask getTask(String userCode) {
        return runningTasks.get(userCode);
    }

    /**
     * 启动一个新的账号批量爬取任务（已经入队完毕、即将开始消费时调用）。
     *
     * @param userCode         账号 code
     * @param userName         账号显示名（可空）
     * @param totalFound       账号下发现的链接总数
     * @param alreadyInLibrary 入队前发现已在 song 表里的数量
     * @param alreadyPending   入队前发现已在 crawler_task 队列里的数量
     * @param enqueued         实际新入队的数量（= totalFound - alreadyInLibrary - alreadyPending）
     */
    public void startTask(String userCode,
                          String userName,
                          int totalFound,
                          int alreadyInLibrary,
                          int alreadyPending,
                          int enqueued) {
        AccountCrawlTask task = new AccountCrawlTask();
        task.setUserCode(userCode);
        task.setUserName(userName);
        task.setStatus(enqueued > 0 ? "RUNNING" : "COMPLETED");
        task.setTotal(enqueued);
        task.setCurrent(0);
        task.setProgress(enqueued > 0 ? 0 : 100);
        task.setTotalFound(totalFound);
        task.setAlreadyInLibrary(alreadyInLibrary);
        task.setAlreadyPending(alreadyPending);
        task.setSuccessCount(0);
        task.setFailedCount(0);
        task.setSkippedCount(alreadyInLibrary + alreadyPending);
        task.setStartTime(System.currentTimeMillis());

        if (enqueued <= 0) {
            // 全部已在库或已在队列里，没有新工作，立刻给 result 让前端能渲染
            task.setEndTime(System.currentTimeMillis());
            task.setResult(buildSnapshotResult(task));
        }
        runningTasks.put(userCode, task);
        log.info("账号爬取启动: {} totalFound={} alreadyInLib={} alreadyPending={} enqueued={}",
                userCode, totalFound, alreadyInLibrary, alreadyPending, enqueued);

        if (enqueued <= 0) {
            scheduleRemoval(userCode);
        }
    }

    /**
     * worker 处理完一条任务后回调：根据 {@code accountCode} 把该账号的进度 +1，
     * 如果累计到 {@code total} 就自动结束。
     *
     * @param accountCode 任务行的 account_code（NULL 表示单曲爬取，非账号批量来源）
     * @param success     该任务是否成功
     */
    public void bumpProgress(String accountCode, boolean success) {
        if (accountCode == null) {
            return;
        }
        AccountCrawlTask task = runningTasks.get(accountCode);
        if (task == null || !"RUNNING".equals(task.getStatus())) {
            return;
        }

        synchronized (task) {
            task.setCurrent(task.getCurrent() + 1);
            if (success) {
                task.setSuccessCount(task.getSuccessCount() + 1);
            } else {
                task.setFailedCount(task.getFailedCount() + 1);
            }
            int total = task.getTotal();
            task.setProgress(total > 0 ? (int) ((task.getCurrent() * 100.0) / total) : 100);

            if (task.getCurrent() >= total) {
                task.setStatus("COMPLETED");
                task.setEndTime(System.currentTimeMillis());
                task.setResult(buildSnapshotResult(task));
                log.info("账号爬取完成: {} success={} failed={} skipped={} 耗时={}ms",
                        accountCode, task.getSuccessCount(), task.getFailedCount(),
                        task.getSkippedCount(),
                        task.getEndTime() - task.getStartTime());
                scheduleRemoval(accountCode);
            }
        }
    }

    /** 任务整体失败（一般是 enqueue 阶段就抛了，例如账号链接非法 / 网络抖动）。 */
    public void failTask(String userCode, String error) {
        AccountCrawlTask task = runningTasks.get(userCode);
        if (task == null) {
            // 没启动就失败，造一条让前端能查
            task = new AccountCrawlTask();
            task.setUserCode(userCode);
            task.setStartTime(System.currentTimeMillis());
            runningTasks.put(userCode, task);
        }
        task.setStatus("FAILED");
        task.setError(error);
        task.setEndTime(System.currentTimeMillis());
        log.error("账号爬取失败: {}, 错误: {}", userCode, error);
        scheduleRemoval(userCode);
    }

    private AccountCrawlResultDTO buildSnapshotResult(AccountCrawlTask task) {
        AccountCrawlResultDTO dto = new AccountCrawlResultDTO();
        dto.setUserCode(task.getUserCode());
        dto.setUserName(task.getUserName());
        dto.setTotalFound(task.getTotalFound());
        dto.setSuccessCount(task.getSuccessCount());
        dto.setFailedCount(task.getFailedCount());
        dto.setSkippedCount(task.getSkippedCount());
        dto.setAlreadyInLibrary(task.getAlreadyInLibrary());
        dto.setAlreadyPending(task.getAlreadyPending());
        dto.setEnqueued(task.getTotal());
        return dto;
    }

    private void scheduleRemoval(String userCode) {
        // 5 秒后从 map 移除，留出时间让前端最后再 poll 一次拿到结果
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                runningTasks.remove(userCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "acct-task-cleanup-" + userCode).start();
    }

    /**
     * 账号爬取任务信息
     */
    @lombok.Data
    public static class AccountCrawlTask {
        private String userCode;
        private String userName;
        private String status;          // RUNNING / COMPLETED / FAILED
        private int current;            // 已处理（success+failed）数
        private int total;              // 入队任务数（即 enqueued）
        private int progress;           // 进度百分比 (0-100)
        private int totalFound;         // 账号下发现的总数
        private int alreadyInLibrary;   // 入队前已在 song 表的数
        private int alreadyPending;     // 入队前已在 crawler_task 队列的数
        private int successCount;
        private int failedCount;
        private int skippedCount;       // = alreadyInLibrary + alreadyPending
        private long startTime;
        private long endTime;
        private String error;
        private AccountCrawlResultDTO result;
    }
}
