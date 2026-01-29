# 📋 数据库 SQL 快速执行指南

## 🎯 场景选择

### 场景 1：全新安装（推荐）

直接执行完整初始化脚本：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS guitar_tab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE guitar_tab;

-- 删除已存在的表
DROP TABLE IF EXISTS guitar_tab_tag;
DROP TABLE IF EXISTS crawler_task;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS guitar_tab;

-- 创建吉他谱表（包含 tab_data_model 字段）
CREATE TABLE guitar_tab (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    song_name VARCHAR(200) NOT NULL COMMENT '歌曲名称',
    artist VARCHAR(100) NOT NULL COMMENT '歌手/艺术家',
    original_url VARCHAR(500) COMMENT '原始链接',
    lyrics TEXT COMMENT '歌词',
    tab_content TEXT COMMENT '吉他谱内容(header后的完整HTML)',
    tab_image_url VARCHAR(500) COMMENT '吉他谱图片链接',
    tab_data_model TEXT COMMENT '吉他谱原始数据模型(data-model属性)',
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

-- 创建标签表
CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tag_name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    tag_type VARCHAR(20) COMMENT '标签类型：style-风格, era-年代, mood-情绪',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tag_type (tag_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 创建关联表
CREATE TABLE guitar_tab_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tab_id BIGINT NOT NULL COMMENT '吉他谱ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tab_id (tab_id),
    INDEX idx_tag_id (tag_id),
    UNIQUE KEY uk_tab_tag (tab_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='吉他谱标签关联表';

-- 创建爬虫任务表
CREATE TABLE crawler_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    url VARCHAR(500) NOT NULL COMMENT '目标URL',
    task_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    error_message TEXT COMMENT '错误信息',
    tab_id BIGINT COMMENT '关联的吉他谱ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (task_status),
    INDEX idx_url (url(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫任务表';

-- 插入示例标签
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
```

---

### 场景 2：已有数据库，只添加新字段

**⚠️ 不会删除数据**

```sql
USE guitar_tab;

-- 添加 tab_data_model 字段
ALTER TABLE guitar_tab 
ADD COLUMN tab_data_model TEXT COMMENT '吉他谱原始数据模型(data-model属性)' 
AFTER tab_image_url;

-- 验证字段是否添加成功
DESC guitar_tab;
```

---

### 场景 3：检查字段是否存在

```sql
USE guitar_tab;

-- 查看 guitar_tab 表结构
DESC guitar_tab;

-- 或者
SHOW COLUMNS FROM guitar_tab LIKE 'tab_data_model';
```

---

## 🔧 命令行执行方式

### 方式 1：使用 mysql 命令

```bash
# 场景1：全新安装
mysql -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 < backend/database/schema.sql

# 场景2：仅添加字段
mysql -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 guitar_tab < backend/database/add_tab_data_model.sql
```

### 方式 2：交互式执行

```bash
# 登录数据库
mysql -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 guitar_tab

# 然后复制粘贴 SQL 语句
mysql> ALTER TABLE guitar_tab ADD COLUMN tab_data_model TEXT COMMENT '吉他谱原始数据模型' AFTER tab_image_url;
mysql> DESC guitar_tab;
mysql> exit;
```

---

## ✅ 验证执行结果

执行后查看表结构：

```sql
DESC guitar_tab;
```

应该看到：

```
+------------------+--------------+------+-----+-------------------+
| Field            | Type         | Null | Key | Default           |
+------------------+--------------+------+-----+-------------------+
| id               | bigint       | NO   | PRI | NULL              |
| song_name        | varchar(200) | NO   |     | NULL              |
| artist           | varchar(100) | NO   |     | NULL              |
| original_url     | varchar(500) | YES  |     | NULL              |
| lyrics           | text         | YES  |     | NULL              |
| tab_content      | text         | YES  |     | NULL              |
| tab_image_url    | varchar(500) | YES  |     | NULL              |
| tab_data_model   | text         | YES  |     | NULL              | ⭐ 新增
| difficulty       | varchar(20)  | YES  |     | NULL              |
...
```

---

## 🗂️ 文件说明

| 文件 | 用途 | 是否删除数据 |
|------|------|--------------|
| `schema.sql` | 完整初始化 | ✅ 是（DROP TABLE） |
| `add_tab_data_model.sql` | 仅添加字段 | ❌ 否 |

---

## ⚠️ 注意事项

1. **场景1 会删除所有数据**，请先备份
2. **场景2 不会删除数据**，安全执行
3. 如果字段已存在，场景2 会报错（可忽略）
4. 执行前建议先备份数据库

---

## 📞 遇到问题？

常见错误：

### 错误1：字段已存在
```
ERROR 1060: Duplicate column name 'tab_data_model'
```
**解决**：字段已存在，无需再添加。

### 错误2：连接失败
```
ERROR 2003: Can't connect to MySQL server
```
**解决**：检查数据库地址、用户名、密码。

### 错误3：权限不足
```
ERROR 1142: ALTER command denied
```
**解决**：确认用户有 ALTER 权限。

---

**选择适合您的场景，复制 SQL 直接执行即可！** 🎯
