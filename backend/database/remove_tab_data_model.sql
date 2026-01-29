-- ============================================
-- 删除 tab_data_model 字段
-- ============================================
-- 说明: 该字段已废弃，不再使用 AlphaTab 相关功能
-- 执行方式: mysql -u root -p guitar_tab < remove_tab_data_model.sql

USE guitar_tab;

-- 删除 tab_data_model 列（如果存在）
ALTER TABLE guitar_tab
DROP COLUMN IF EXISTS tab_data_model;

-- 查看表结构
DESC guitar_tab;
