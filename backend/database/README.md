# 数据库操作指南

## 📁 数据库脚本文件

```
backend/database/
├── schema.sql              # 数据库初始化脚本（包含 DROP）
├── update_20260122.sql     # 数据库更新脚本
└── reset.sql               # 完全重置脚本
```

## 🚀 快速开始

### 1. 初始化数据库（首次使用）

```bash
mysql -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 < backend/database/schema.sql
```

**说明**：
- ✅ 自动创建 `guitar_tab` 数据库
- ✅ 创建所有表（会先 DROP 已存在的表）
- ✅ 插入示例标签数据

### 2. 更新数据库结构

```bash
mysql -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 guitar_tab < backend/database/update_20260122.sql
```

**说明**：
- 添加新字段 `tab_data_model`
- 不会删除现有数据

### 3. 完全重置数据库

```bash
# 步骤1: 删除所有表
mysql -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 guitar_tab < backend/database/reset.sql

# 步骤2: 重建表结构
mysql -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 < backend/database/schema.sql
```

**⚠️ 警告**：这会删除所有数据，请谨慎使用！

## 📋 数据库表结构

### 1. guitar_tab (吉他谱表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| song_name | VARCHAR(200) | 歌曲名称 |
| artist | VARCHAR(100) | 歌手/艺术家 |
| original_url | VARCHAR(500) | 原始链接 |
| lyrics | TEXT | 歌词 |
| tab_content | TEXT | 吉他谱内容(HTML) |
| tab_image_url | VARCHAR(500) | 吉他谱图片链接 |
| tab_data_model | TEXT | 原始数据模型 |
| difficulty | VARCHAR(20) | 难度等级 |
| tuning | VARCHAR(50) | 调弦方式 |
| capo | INT | 变调夹位置 |
| play_key | VARCHAR(10) | 演奏调 |
| original_key | VARCHAR(10) | 原调 |
| beat | VARCHAR(20) | 节拍 |
| view_count | INT | 浏览次数 |
| favorite_count | INT | 收藏次数 |
| status | TINYINT | 状态 |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**索引**：
- `idx_song_name` - 歌曲名称索引
- `idx_artist` - 歌手索引
- `idx_create_time` - 创建时间索引

### 2. tag (标签表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| tag_name | VARCHAR(50) | 标签名称(唯一) |
| tag_type | VARCHAR(20) | 标签类型(style/era/mood) |
| create_time | DATETIME | 创建时间 |

**预置标签**：
- 风格：民谣、摇滚、流行、古典、指弹、弹唱
- 年代：90年代、2000年代、2010年代
- 情绪：抒情、欢快、忧伤

### 3. guitar_tab_tag (关联表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| tab_id | BIGINT | 吉他谱ID |
| tag_id | BIGINT | 标签ID |
| create_time | DATETIME | 创建时间 |

**唯一约束**：`uk_tab_tag(tab_id, tag_id)`

### 4. crawler_task (爬虫任务表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| url | VARCHAR(500) | 目标URL |
| task_status | VARCHAR(20) | 任务状态 |
| retry_count | INT | 重试次数 |
| error_message | TEXT | 错误信息 |
| tab_id | BIGINT | 关联的吉他谱ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**任务状态**：
- `PENDING` - 待处理
- `PROCESSING` - 处理中
- `SUCCESS` - 成功
- `FAILED` - 失败

## 🔧 常用操作

### 查看所有表

```sql
USE guitar_tab;
SHOW TABLES;
```

### 查看表结构

```sql
DESC guitar_tab;
DESC tag;
DESC guitar_tab_tag;
DESC crawler_task;
```

### 查看数据量

```sql
SELECT 'guitar_tab' AS table_name, COUNT(*) AS count FROM guitar_tab
UNION ALL
SELECT 'tag', COUNT(*) FROM tag
UNION ALL
SELECT 'guitar_tab_tag', COUNT(*) FROM guitar_tab_tag
UNION ALL
SELECT 'crawler_task', COUNT(*) FROM crawler_task;
```

### 清空某个表的数据（保留结构）

```sql
TRUNCATE TABLE guitar_tab;
TRUNCATE TABLE crawler_task;
-- 注意：tag 表有预置数据，慎用
```

### 删除某个表

```sql
DROP TABLE IF EXISTS guitar_tab;
```

### 备份数据库

```bash
# 备份整个数据库
mysqldump -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 guitar_tab > backup_$(date +%Y%m%d).sql

# 仅备份结构
mysqldump -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 --no-data guitar_tab > schema_backup.sql

# 仅备份数据
mysqldump -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 --no-create-info guitar_tab > data_backup.sql
```

### 恢复数据库

```bash
mysql -h sosocomdb.mysql.rds.aliyuncs.com -u rooter -pDbRooterPass123 guitar_tab < backup_20260122.sql
```

## 🛠️ 开发环境配置

### 本地开发数据库

如果在本地开发，可以：

1. 修改连接配置（`backend/src/main/resources/application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/guitar_tab
    username: root
    password: your_password
```

2. 初始化本地数据库：

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS guitar_tab"

# 初始化表结构
mysql -u root -p guitar_tab < backend/database/schema.sql
```

## 📊 性能优化建议

### 1. 索引优化

已创建的索引：
- ✅ `guitar_tab.song_name` - 支持歌曲名搜索
- ✅ `guitar_tab.artist` - 支持歌手搜索
- ✅ `guitar_tab.create_time` - 支持时间排序

如需添加其他索引：

```sql
-- 添加全文索引（用于歌词搜索）
ALTER TABLE guitar_tab ADD FULLTEXT INDEX ft_lyrics (lyrics);

-- 复合索引（用于组合查询）
ALTER TABLE guitar_tab ADD INDEX idx_artist_status (artist, status);
```

### 2. 查询优化

```sql
-- 使用覆盖索引
SELECT id, song_name, artist FROM guitar_tab WHERE artist = '赵雷';

-- 避免 SELECT *
SELECT song_name, artist FROM guitar_tab LIMIT 10;

-- 使用分页
SELECT * FROM guitar_tab LIMIT 10 OFFSET 0;
```

### 3. 定期维护

```sql
-- 分析表
ANALYZE TABLE guitar_tab;

-- 优化表
OPTIMIZE TABLE guitar_tab;

-- 查看表状态
SHOW TABLE STATUS LIKE 'guitar_tab';
```

## ⚠️ 注意事项

1. **DROP 语句**：
   - `schema.sql` 包含 `DROP TABLE IF EXISTS`
   - 重新执行会删除所有数据
   - 生产环境请做好备份

2. **字符集**：
   - 统一使用 `utf8mb4`
   - 支持 emoji 和特殊字符

3. **时间字段**：
   - `create_time` - 自动填充创建时间
   - `update_time` - 自动更新修改时间

4. **逻辑删除**：
   - 使用 `deleted` 字段标记
   - 0 = 未删除，1 = 已删除
   - MyBatis Plus 自动处理

## 🔗 相关文档

- [后端开发指南](../backend/README.md)
- [MyBatis Plus 文档](https://baomidou.com/)
- [MySQL 官方文档](https://dev.mysql.com/doc/)
