-- ---------------------------------------------------------------------------
-- Paobar：已有库增量升级 —— 爬虫队列表 account_code + 队列索引
--
-- 全量建新库仍推荐直接执行 schema.sql。
-- 本脚本用于已有 paobar.crawler_task 的实例；若某项已做过，对应语句可能报错，
--    报错后跳过即可（MySQL 会提示 Duplicate column / Duplicate key name）。
-- ---------------------------------------------------------------------------

ALTER TABLE crawler_task
  ADD COLUMN account_code VARCHAR(64) NULL
    COMMENT '触发该任务的账号 code（账号批量爬取时填，单曲爬取为 NULL）'
    AFTER tab_id;

ALTER TABLE crawler_task ADD INDEX idx_account_status (account_code, task_status);
ALTER TABLE crawler_task ADD INDEX idx_status_id (task_status, id);

-- 与当前队列逻辑语义一致（失败回 PENDING / 超限删行）
ALTER TABLE crawler_task
  MODIFY COLUMN retry_count int DEFAULT 0
    COMMENT '连续失败次数；未达上限回 PENDING 重试，达上限则删行移出队列';
