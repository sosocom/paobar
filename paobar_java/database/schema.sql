/*
 * Paobar 数据库全量结构（仅此文件维护）。
 * 新建库：CREATE DATABASE 后执行本脚本；库名以实际连接为准。
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for crawler_task
-- ----------------------------
DROP TABLE IF EXISTS `crawler_task`;
CREATE TABLE `crawler_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标URL',
  `task_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待处理，PROCESSING-处理中，SUCCESS-成功，FAILED-失败',
  `retry_count` int DEFAULT '0' COMMENT '连续失败次数；未达上限回 PENDING 重试，达上限则删行移出队列',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `tab_id` bigint DEFAULT NULL COMMENT '关联的吉他谱ID',
  `account_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '触发该任务的账号 code（账号批量爬取时填，单曲爬取为 NULL）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_id` (`task_status`,`id`),
  KEY `idx_url` (`url`(255)),
  KEY `idx_account_status` (`account_code`,`task_status`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫任务表（兼作待爬队列：account_code 标记账号批量来源，task_status=PENDING 行即为队列）';

-- ----------------------------
-- Table structure for guitar_tab
-- ----------------------------
DROP TABLE IF EXISTS `song`;
CREATE TABLE `song` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `song_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '歌曲名称',
  `sort_key` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '歌名拼音排序键（全拼小写，爬虫入库时生成）',
  `artist` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '歌手/艺术家',
  `original_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始链接（爬虫唯一性来源）',
  `lyrics` text COLLATE utf8mb4_unicode_ci COMMENT '歌词',
  `tab_content_json` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '规范化吉他谱 JSON（TabDocument schemaVersion=1，爬虫入库时生成）',
  `tab_image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '吉他谱图片链接',
  `meta` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '元信息(xhe-meta内容)',
  `difficulty` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '难度等级',
  `tuning` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '调弦方式',
  `capo` int DEFAULT NULL COMMENT '变调夹位置',
  `play_key` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '演奏调',
  `original_key` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原调',
  `beat` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节拍',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  `favorite_count` int DEFAULT '0' COMMENT '收藏次数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_original_url` (`original_url`),
  KEY `idx_sort_key` (`sort_key`),
  KEY `idx_song_name` (`song_name`),
  KEY `idx_artist` (`artist`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='吉他谱信息表（按 original_url 唯一，物理删除）';

-- ----------------------------
-- Table structure for playlist
-- ----------------------------
DROP TABLE IF EXISTS `playlist`;
CREATE TABLE `playlist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，NULL表示系统生成',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '歌单名称',
  `cover_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片URL',
  `gradient_start` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渐变色起始',
  `gradient_end` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渐变色结束',
  `song_count` int DEFAULT '0' COMMENT '歌曲数量',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'user' COMMENT '类型：user-用户创建, ai-AI生成',
  `chord_progression` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'AI歌单的和弦走向，如 1-5-6-4',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌单表';

-- ----------------------------
-- Table structure for playlist_song
-- ----------------------------
DROP TABLE IF EXISTS `playlist_song`;
CREATE TABLE `playlist_song` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `playlist_id` bigint NOT NULL COMMENT '歌单ID',
  `song_id` bigint NOT NULL COMMENT '歌曲ID',
  `position` int DEFAULT '0' COMMENT '在歌单中的位置',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_playlist_song` (`playlist_id`,`song_id`),
  KEY `idx_playlist_id` (`playlist_id`),
  KEY `idx_song_id` (`song_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌单-歌曲关联表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT 'https://picsum.photos/seed/default/200/200' COMMENT '头像URL',
  `points` int DEFAULT '0' COMMENT '点数余额',
  `collected` int DEFAULT '0' COMMENT '已收藏曲目数',
  `playlists_count` int DEFAULT '0' COMMENT '创建的歌单数',
  `practice_hours` int DEFAULT '0' COMMENT '累计练习时长(小时)',
  `is_admin` tinyint NOT NULL DEFAULT '0' COMMENT '是否管理员：0-普通用户，1-管理员',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `song_id` bigint NOT NULL COMMENT '歌曲ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_song` (`user_id`,`song_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_song_id` (`song_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ----------------------------
-- Table structure for play_history
-- ----------------------------
DROP TABLE IF EXISTS `play_history`;
CREATE TABLE `play_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `song_id` bigint NOT NULL COMMENT '歌曲ID',
  `play_count` int NOT NULL DEFAULT '1' COMMENT '累计打开次数',
  `last_played_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次打开时间（排序用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次打开时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_song` (`user_id`,`song_id`),
  KEY `idx_user_last_played` (`user_id`, `last_played_at` DESC),
  KEY `idx_song_id` (`song_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户播放历史（按用户+歌曲唯一，upsert 累加次数+刷新时间）';

SET FOREIGN_KEY_CHECKS = 1;
