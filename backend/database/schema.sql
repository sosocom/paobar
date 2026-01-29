-- 创建数据库
CREATE DATABASE IF NOT EXISTS guitar_tab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE guitar_tab;

-- ============================================
-- 吉他谱表
-- ============================================
DROP TABLE IF EXISTS guitar_tab;
CREATE TABLE guitar_tab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    song_name VARCHAR(200) NOT NULL COMMENT '歌曲名称',
    artist VARCHAR(100) NOT NULL COMMENT '歌手/艺术家',
    original_url VARCHAR(500) COMMENT '原始链接',
    lyrics TEXT COMMENT '歌词',
    tab_content MEDIUMTEXT COMMENT '吉他谱内容(header后的完整HTML)',
    tab_image_url VARCHAR(500) COMMENT '吉他谱图片链接',
    difficulty VARCHAR(20) COMMENT '难度等级',
    tuning VARCHAR(50) COMMENT '调弦方式',
    capo INT COMMENT '变调夹位置',
    play_key VARCHAR(10) COMMENT '演奏调',
    original_key VARCHAR(10) COMMENT '原调',
    beat VARCHAR(20) COMMENT '节拍',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    favorite_count INT DEFAULT 0 COMMENT '收藏次数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_song_name (song_name),
    INDEX idx_artist (artist),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='吉他谱信息表';
-- ============================================
-- 标签表
-- ============================================
DROP TABLE IF EXISTS tag;
CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tag_name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    tag_type VARCHAR(20) COMMENT '标签类型：style-风格, era-年代, mood-情绪',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tag_type (tag_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================
-- 吉他谱标签关联表
-- ============================================
DROP TABLE IF EXISTS guitar_tab_tag;
CREATE TABLE guitar_tab_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tab_id BIGINT NOT NULL COMMENT '吉他谱ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tab_id (tab_id),
    INDEX idx_tag_id (tag_id),
    UNIQUE KEY uk_tab_tag (tab_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='吉他谱标签关联表';

-- ============================================
-- 爬虫任务表
-- ============================================
DROP TABLE IF EXISTS crawler_task;
CREATE TABLE crawler_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    url VARCHAR(500) NOT NULL COMMENT '目标URL',
    task_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待处理，PROCESSING-处理中，SUCCESS-成功，FAILED-失败',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    error_message TEXT COMMENT '错误信息',
    tab_id BIGINT COMMENT '关联的吉他谱ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (task_status),
    INDEX idx_url (url(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫任务表';

-- ============================================
-- 歌单表
-- ============================================
DROP TABLE IF EXISTS playlist_tab;
DROP TABLE IF EXISTS playlist;
CREATE TABLE playlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '歌单名称',
    description VARCHAR(500) COMMENT '歌单描述',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌单表';

-- ============================================
-- 歌单-吉他谱关联表
-- ============================================
CREATE TABLE playlist_tab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    playlist_id BIGINT NOT NULL COMMENT '歌单ID',
    tab_id BIGINT NOT NULL COMMENT '吉他谱ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_playlist_id (playlist_id),
    INDEX idx_tab_id (tab_id),
    UNIQUE KEY uk_playlist_tab (playlist_id, tab_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌单-吉他谱关联表';

-- ============================================
-- 插入示例标签数据
-- ============================================
DELETE FROM tag WHERE id > 0;  -- 清空已有标签
INSERT INTO tag (tag_name, tag_type) VALUES 
('民谣', 'style'),
('摇滚', 'style'),
('流行', 'style'),
('古典', 'style'),
('指弹', 'style'),
('弹唱', 'style'),
('90年代', 'era'),
('2000年代', 'era'),
('2010年代', 'era'),
('抒情', 'mood'),
('欢快', 'mood'),
('忧伤', 'mood');
