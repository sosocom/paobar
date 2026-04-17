package com.sosocom.service;

import com.sosocom.dto.AccountCrawlResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 账号爬取任务管理器
 */
@Slf4j
@Component
public class AccountCrawlTaskManager {
    
    // 存储正在进行的任务，key为userCode
    private final Map<String, AccountCrawlTask> runningTasks = new ConcurrentHashMap<>();
    
    /**
     * 检查是否有正在进行的任务
     */
    public boolean isTaskRunning(String userCode) {
        return runningTasks.containsKey(userCode);
    }
    
    /**
     * 获取任务状态
     */
    public AccountCrawlTask getTask(String userCode) {
        return runningTasks.get(userCode);
    }
    
    /**
     * 开始任务
     */
    public void startTask(String userCode, String userName) {
        AccountCrawlTask task = new AccountCrawlTask();
        task.setUserCode(userCode);
        task.setUserName(userName);
        task.setStatus("RUNNING");
        task.setStartTime(System.currentTimeMillis());
        runningTasks.put(userCode, task);
        log.info("开始账号爬取任务: {}", userCode);
    }
    
    /**
     * 更新任务进度
     */
    public void updateProgress(String userCode, int current, int total) {
        AccountCrawlTask task = runningTasks.get(userCode);
        if (task != null) {
            task.setCurrent(current);
            task.setTotal(total);
            task.setProgress(total > 0 ? (int)((current * 100.0) / total) : 0);
        }
    }
    
    /**
     * 完成任务
     */
    public void completeTask(String userCode, AccountCrawlResultDTO result) {
        AccountCrawlTask task = runningTasks.get(userCode);
        if (task != null) {
            task.setStatus("COMPLETED");
            task.setResult(result);
            task.setEndTime(System.currentTimeMillis());
            log.info("完成账号爬取任务: {}, 耗时: {}ms", userCode, 
                task.getEndTime() - task.getStartTime());
            
            // 5秒后移除任务记录
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    runningTasks.remove(userCode);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    /**
     * 任务失败
     */
    public void failTask(String userCode, String error) {
        AccountCrawlTask task = runningTasks.get(userCode);
        if (task != null) {
            task.setStatus("FAILED");
            task.setError(error);
            task.setEndTime(System.currentTimeMillis());
            log.error("账号爬取任务失败: {}, 错误: {}", userCode, error);
            
            // 5秒后移除任务记录
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    runningTasks.remove(userCode);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    /**
     * 账号爬取任务信息
     */
    @lombok.Data
    public static class AccountCrawlTask {
        private String userCode;
        private String userName;
        private String status;  // RUNNING, COMPLETED, FAILED
        private int current;    // 当前进度
        private int total;      // 总数
        private int progress;   // 进度百分比
        private long startTime;
        private long endTime;
        private String error;
        private AccountCrawlResultDTO result;
    }
}
