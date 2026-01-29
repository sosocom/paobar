# 吉他和弦谱应用 - 产品需求文档 (PRD)

## 1. 产品概述

### 1.1 产品定位
一款专注于吉他弹唱的和弦谱查看与管理应用，参考 Apple Music 的设计理念，为吉他爱好者提供优雅、高效的和弦谱学习体验。

### 1.2 目标用户
- 吉他初学者：需要简单易读的和弦谱
- 吉他爱好者：需要管理和练习大量曲目
- 专业吉他手：需要快速查找和切换曲谱

### 1.3 核心价值
- 清晰的和弦谱显示体验
- 智能化的歌单管理
- AI 驱动的和弦走向分析
- 类 Apple Music 的优雅交互

---

## 2. 功能架构

### 2.1 应用结构

```
├── 谱库 (Library)
│   ├── 全部曲谱
│   ├── 搜索与筛选
│   └── 分类浏览
│
├── 歌单 (Playlists)
│   ├── 我的歌单
│   ├── AI 歌单
│   └── 播放模式
│
├── 正在播放 (Now Playing)
│   ├── 和弦谱显示
│   ├── 自动滚动
│   └── 播放控制
│
└── 我的 (Profile)
    ├── 账户信息
    ├── 点数系统
    ├── 充值中心
    └── 设置
```

---

## 3. 详细功能设计

### 3.1 谱库模块

#### 3.1.1 曲谱列表
**功能描述**
- 展示所有可用的吉他和弦谱
- 支持多种视图模式（列表/网格）

**界面元素**（参考 Apple Music）
- 顶部导航栏：标题 + 搜索按钮
- 筛选栏：分类标签（流行、民谣、摇滚、经典等）
- 曲谱卡片包含：
  - 封面图（专辑封面或默认图）
  - 歌曲名称
  - 艺人名称
  - 难度标识（简单/中等/困难）
  - 和弦数量提示

**交互设计**
- 点击卡片：进入曲谱详情/播放页面
- 长按卡片：快捷菜单（添加到歌单、收藏、分享）
- 下拉刷新：更新曲谱库
- 无限滚动加载

#### 3.1.2 搜索功能
- 支持歌曲名、艺人名、和弦搜索
- 智能搜索建议
- 搜索历史记录
- 热门搜索推荐

#### 3.1.3 分类筛选
- 按音乐风格：流行、民谣、摇滚、爵士等
- 按难度等级：简单、中等、困难
- 按和弦数量：1-3个和弦、4-6个和弦、7+个和弦
- 按更新时间：最新添加、最近更新

---

### 3.2 歌单模块

#### 3.2.1 我的歌单

**创建歌单**
- 点击 "+" 创建新歌单
- 输入歌单名称和描述
- 选择封面图（从相册或默认）
- 设置歌单隐私（公开/私密）

**歌单管理**
- 歌单列表展示（类似 Apple Music 播放列表）
- 每个歌单显示：
  - 封面拼图（4首歌的封面组合）
  - 歌单名称
  - 曲目数量
  - 最后更新时间

**添加曲目**
- 从谱库页面添加
- 从搜索结果添加
- 批量添加功能
- 拖拽排序

**播放控制**
- 点击歌单进入播放界面
- 支持顺序播放
- 上下切歌（滑动或按钮）
- 当前播放进度指示

#### 3.2.2 AI 歌单

**核心算法**
```
1. 和弦走向识别
   - 提取每首歌的和弦序列
   - 分析和弦在调内的级数（如：C大调中 Am-F-C-G = 6-4-1-5）
   
2. 和弦走向聚类
   - 将相同级数走向的歌曲分组
   - 识别常见走向模式（如：1-5-6-4、1-6-4-5等）
   
3. 自动生成歌单
   - 为每个走向模式创建歌单
   - 歌单命名格式："1-5-6-4 走向" 或 "卡农走向"
```

**AI 歌单展示**
- 标识 AI 自动生成标签
- 显示和弦走向图示
- 显示该走向的歌曲数量
- 提供"刷新推荐"功能

**走向说明**
- 点击歌单显示和弦走向详解
- 展示代表性歌曲示例
- 提供练习建议

**常见走向示例**
- **1-5-6-4 走向**（C-G-Am-F）
  - 《Someone Like You》
  - 《童年》
  - 《小情歌》
  
- **1-6-4-5 走向**（C-Am-F-G）
  - 《You Are My Sunshine》
  - 《月亮代表我的心》
  
- **4-1-5-6 走向**（F-C-G-Am）
  - 《Let It Be》
  - 《成都》

---

### 3.3 正在播放模块

#### 3.3.1 和弦谱显示

**显示格式**
```
标题：《歌曲名》
艺人：艺人名
调性：C大调    难度：★★☆☆☆    BPM：120

--------------------------------

前奏：C  G  Am  F

[主歌]
    C              G
我站在这里看着你
    Am             F
想要对你说声谢谢你
    C              G
感谢你陪我走过
        Am      F
这一段美好时光

[副歌]
    F              C
让我们一起唱一首歌
    G              Am
记住这快乐的时刻
    F              C
无论走到哪里
        G       C
都不会忘记你

[间奏]
C  G  Am  F  x2

--------------------------------
```

**显示特性**
- 大字体清晰显示
- 和弦与歌词精确对齐
- 不同段落用标签区分 [主歌] [副歌] [间奏]
- 和弦名称高亮显示（不同颜色）
- 支持横竖屏切换

#### 3.3.2 自动滚动
- 可调节滚动速度（慢/中/快）
- 点击屏幕暂停/继续滚动
- 手动滚动时自动暂停
- 进度条显示当前位置

#### 3.3.3 播放控制栏

**控制按钮**（参考 Apple Music）
- 上一曲 |◀
- 播放/暂停 ▶/⏸
- 下一曲 ▶|
- 滚动速度调节 🎚️
- 更多选项 ⋯

**附加功能**
- 歌单队列显示
- 添加到歌单
- 收藏/取消收藏
- 分享功能
- 转调功能（升/降调）

#### 3.3.4 和弦指法参考
- 点击和弦名称弹出指法图
- 显示常用按法
- 支持多种按法切换
- 可播放和弦音效（需要权限）

---

### 3.4 我的模块

#### 3.4.1 账户信息

**个人资料**
- 头像（可点击更换）
- 用户名
- 用户 ID
- 注册时间
- 练习天数统计

**统计数据**
- 已收藏曲目数
- 创建的歌单数
- 累计练习时长
- 掌握的和弦数

#### 3.4.2 点数系统

**点数用途**
- 解锁高级曲谱
- 使用 AI 歌单功能
- 下载离线曲谱
- 访问独家内容

**获取点数**
- 每日签到奖励
- 分享应用获得
- 完成练习任务
- 充值购买

**点数展示**
- 当前点数余额（显著位置）
- 点数历史记录
- 即将过期提醒

#### 3.4.3 充值中心

**充值套餐**（参考 Apple Music 订阅）
```
┌─────────────────────┐
│   月度会员 ¥15      │
│   • 300 点数        │
│   • 无限 AI 歌单    │
│   • 无广告          │
└─────────────────────┘

┌─────────────────────┐
│   季度会员 ¥40      │
│   • 1000 点数       │
│   • 所有月度权益    │
│   • 独家曲谱       │
└─────────────────────┘

┌─────────────────────┐
│   年度会员 ¥128     │
│   • 5000 点数       │
│   • 所有季度权益    │
│   • 优先客服支持    │
└─────────────────────┘
```

**支付方式**
- Apple Pay
- 微信支付
- 支付宝
- 信用卡

#### 3.4.4 设置功能

**显示设置**
- 主题模式（浅色/深色/自动）
- 字体大小调节
- 和弦颜色方案
- 界面布局偏好

**播放设置**
- 默认滚动速度
- 自动播放下一首
- 播放完成后行为
- 节拍器开关

**账户设置**
- 修改密码
- 绑定邮箱/手机
- 隐私设置
- 数据同步

**其他**
- 关于我们
- 用户协议
- 隐私政策
- 反馈与建议
- 版本信息

---

## 4. 用户界面设计

### 4.1 设计原则（参考 Apple Music）

#### 4.1.1 视觉风格
- **现代简约**：大量留白，重点突出
- **层次分明**：使用阴影和模糊效果
- **流畅动画**：过渡自然，响应迅速
- **品质感**：高清图片，精致图标

#### 4.1.2 色彩方案

**浅色模式**
```
主色调：#007AFF（Apple 蓝）
辅助色：#5856D6（紫色）
背景色：#FFFFFF
次要背景：#F2F2F7
文本色：#000000
次要文本：#8E8E93
分割线：#C6C6C8
```

**深色模式**
```
主色调：#0A84FF
辅助色：#5E5CE6
背景色：#000000
次要背景：#1C1C1E
文本色：#FFFFFF
次要文本：#8E8E93
分割线：#38383A
```

#### 4.1.3 字体规范
- 标题：SF Pro Display (iOS) / Roboto (Android)
- 正文：SF Pro Text (iOS) / Roboto (Android)
- 和弦：SF Mono（等宽字体，确保对齐）
- 中文：苹方 (iOS) / 思源黑体 (Android)

### 4.2 页面布局

#### 4.2.1 底部导航栏
```
┌─────────────────────────────────┐
│  谱库    歌单    我的            │
│  📚      📋      👤             │
└─────────────────────────────────┘
```

#### 4.2.2 谱库页面
```
┌─────────────────────────────────┐
│  [返回]  谱库          [搜索]    │
├─────────────────────────────────┤
│  流行 民谣 摇滚 爵士 经典 ...    │
├─────────────────────────────────┤
│  ┌───────┐ ┌───────┐            │
│  │ 封面  │ │ 封面  │            │
│  │       │ │       │            │
│  └───────┘ └───────┘            │
│  歌曲名     歌曲名               │
│  艺人名     艺人名               │
│  难度★★☆   难度★☆☆             │
│                                 │
│  ┌───────┐ ┌───────┐            │
│  │ 封面  │ │ 封面  │            │
│  ...                            │
└─────────────────────────────────┘
```

#### 4.2.3 歌单页面
```
┌─────────────────────────────────┐
│  [返回]  歌单          [+]       │
├─────────────────────────────────┤
│  📱 我的歌单                     │
│  ┌─────────────────────────┐   │
│  │ 🎸 新学的歌    12 首    │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │ 💙 最爱民谣    8 首     │   │
│  └─────────────────────────┘   │
│                                 │
│  🤖 AI 智能歌单                 │
│  ┌─────────────────────────┐   │
│  │ 1-5-6-4 走向   25 首    │   │
│  └─────────────────────────┘   │
│  ┌─────────────────────────┐   │
│  │ 1-6-4-5 走向   18 首    │   │
│  └─────────────────────────┘   │
└─────────────────────────────────┘
```

#### 4.2.4 播放页面（核心）
```
┌─────────────────────────────────┐
│  [< 返回]       [⋯ 更多]         │
│                                 │
│         [专辑封面]               │
│                                 │
│     《歌曲名》                   │
│      艺人名                      │
│                                 │
│  C大调  难度★★☆  BPM:120       │
├─────────────────────────────────┤
│                                 │
│  [前奏]                         │
│  C    G    Am   F               │
│                                 │
│  [主歌]                         │
│      C              G           │
│  我站在这里看着你                │
│      Am             F           │
│  想要对你说声谢谢你              │
│                                 │
│  ... (滚动显示)                 │
│                                 │
├─────────────────────────────────┤
│  ━━━━━━●━━━━━━━━━  2:30 / 4:15 │
│                                 │
│    |◀    ▶    ▶|    🎚️        │
│   上一曲  播放  下一曲  速度     │
└─────────────────────────────────┘
```

#### 4.2.5 我的页面
```
┌─────────────────────────────────┐
│              我的                │
├─────────────────────────────────┤
│     [头像]                      │
│     用户名                       │
│     点数余额：1,280              │
│     [立即充值]                   │
├─────────────────────────────────┤
│  📊 我的统计                     │
│     已收藏 45 | 歌单 8 | 练习 120h│
├─────────────────────────────────┤
│  🎵 收藏的曲目          >        │
│  📋 创建的歌单          >        │
│  📥 下载的曲谱          >        │
│  ⏱️ 练习历史            >        │
├─────────────────────────────────┤
│  ⚙️ 设置                >        │
│  💬 反馈与建议          >        │
│  ℹ️ 关于我们            >        │
└─────────────────────────────────┘
```

### 4.3 交互动画

#### 4.3.1 页面切换
- 类似 iOS 推入推出动画
- 导航栏标题淡入淡出
- 保持流畅的60fps

#### 4.3.2 列表滚动
- 下拉刷新动画
- 惯性滚动
- 滚动到底部提示

#### 4.3.3 播放动画
- 专辑封面旋转（播放时）
- 波形律动效果
- 和弦高亮渐变

#### 4.3.4 点击反馈
- 按钮缩放动画
- 触觉反馈（Haptic）
- 选中状态动画

---

## 5. 技术架构

### 5.1 技术栈建议

#### 5.1.1 前端
**移动端**
- iOS: Swift + SwiftUI
- Android: Kotlin + Jetpack Compose
- 跨平台: React Native / Flutter

**设计工具**
- Figma（UI设计）
- Principle（动画原型）

#### 5.1.2 后端
```
技术栈：
- 框架: Node.js (Express) 或 Python (FastAPI)
- 数据库: PostgreSQL (主数据) + Redis (缓存)
- 存储: AWS S3 / 阿里云 OSS (曲谱文件)
- 搜索: Elasticsearch
- 队列: RabbitMQ / Redis Queue
```

#### 5.1.3 AI 服务
```
和弦分析引擎：
- 语言: Python
- 库: music21 (音乐理论分析)
- 算法: 聚类算法 (K-means / DBSCAN)
- 部署: Docker + Kubernetes
```

### 5.2 数据模型

#### 5.2.1 数据库表结构

**用户表 (users)**
```sql
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255),
    avatar_url VARCHAR(255),
    points INT DEFAULT 0,
    membership_type VARCHAR(20), -- free, monthly, quarterly, annual
    membership_expire_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**曲谱表 (songs)**
```sql
CREATE TABLE songs (
    song_id BIGINT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    artist VARCHAR(100),
    album VARCHAR(200),
    cover_image_url VARCHAR(255),
    genre VARCHAR(50), -- pop, folk, rock, jazz, etc.
    difficulty INT, -- 1-5
    original_key VARCHAR(10), -- C, G, Am, etc.
    bpm INT,
    chord_content TEXT, -- 和弦谱内容
    lyrics_content TEXT, -- 歌词内容
    chord_sequence JSON, -- ['C', 'G', 'Am', 'F']
    chord_progression JSON, -- ['1', '5', '6', '4']
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    is_premium BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**歌单表 (playlists)**
```sql
CREATE TABLE playlists (
    playlist_id BIGINT PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(255),
    is_public BOOLEAN DEFAULT TRUE,
    is_ai_generated BOOLEAN DEFAULT FALSE,
    chord_progression_pattern VARCHAR(50), -- AI歌单的和弦走向标识
    song_count INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**歌单-曲目关联表 (playlist_songs)**
```sql
CREATE TABLE playlist_songs (
    id BIGINT PRIMARY KEY,
    playlist_id BIGINT REFERENCES playlists(playlist_id),
    song_id BIGINT REFERENCES songs(song_id),
    position INT, -- 在歌单中的位置
    added_at TIMESTAMP
);
```

**用户收藏表 (favorites)**
```sql
CREATE TABLE favorites (
    user_id BIGINT REFERENCES users(user_id),
    song_id BIGINT REFERENCES songs(song_id),
    created_at TIMESTAMP,
    PRIMARY KEY (user_id, song_id)
);
```

**点数记录表 (point_transactions)**
```sql
CREATE TABLE point_transactions (
    transaction_id BIGINT PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id),
    amount INT, -- 正数为获得，负数为消费
    type VARCHAR(50), -- sign_in, share, purchase, unlock_song, etc.
    description TEXT,
    created_at TIMESTAMP
);
```

**订单表 (orders)**
```sql
CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id),
    product_type VARCHAR(50), -- monthly, quarterly, annual, points
    amount DECIMAL(10, 2),
    payment_method VARCHAR(50),
    payment_status VARCHAR(20), -- pending, completed, failed
    created_at TIMESTAMP,
    paid_at TIMESTAMP
);
```

### 5.3 API 设计

#### 5.3.1 谱库 API
```
GET    /api/songs              # 获取曲谱列表
GET    /api/songs/:id          # 获取曲谱详情
GET    /api/songs/search       # 搜索曲谱
GET    /api/songs/categories   # 获取分类
POST   /api/songs/:id/favorite # 收藏曲谱
DELETE /api/songs/:id/favorite # 取消收藏
```

#### 5.3.2 歌单 API
```
GET    /api/playlists          # 获取用户歌单列表
POST   /api/playlists          # 创建歌单
GET    /api/playlists/:id      # 获取歌单详情
PUT    /api/playlists/:id      # 更新歌单信息
DELETE /api/playlists/:id      # 删除歌单
POST   /api/playlists/:id/songs # 添加歌曲到歌单
DELETE /api/playlists/:id/songs/:songId # 从歌单移除歌曲
PUT    /api/playlists/:id/songs/order # 调整歌曲顺序
```

#### 5.3.3 AI 歌单 API
```
GET    /api/ai-playlists       # 获取 AI 生成的歌单
POST   /api/ai-playlists/generate # 触发 AI 生成歌单
GET    /api/ai-playlists/progressions # 获取所有和弦走向类型
```

#### 5.3.4 用户 API
```
POST   /api/auth/register      # 用户注册
POST   /api/auth/login         # 用户登录
POST   /api/auth/logout        # 用户登出
GET    /api/user/profile       # 获取用户信息
PUT    /api/user/profile       # 更新用户信息
GET    /api/user/points        # 获取点数余额
GET    /api/user/points/history # 获取点数记录
POST   /api/user/sign-in       # 每日签到
```

#### 5.3.5 支付 API
```
GET    /api/products           # 获取商品列表
POST   /api/orders             # 创建订单
GET    /api/orders/:id         # 获取订单详情
POST   /api/payments/wechat    # 微信支付
POST   /api/payments/alipay    # 支付宝支付
POST   /api/payments/apple     # Apple Pay
```

### 5.4 和弦分析算法

#### 5.4.1 和弦识别
```python
def analyze_chord_progression(song):
    """
    分析歌曲的和弦走向
    """
    # 1. 提取和弦序列
    chords = extract_chords(song.chord_content)
    
    # 2. 识别调性
    key = detect_key(chords)
    
    # 3. 转换为级数
    progression = []
    for chord in chords:
        degree = chord_to_degree(chord, key)
        progression.append(degree)
    
    # 4. 提取主要走向模式
    pattern = extract_main_pattern(progression)
    
    return {
        'key': key,
        'chords': chords,
        'progression': progression,
        'pattern': pattern
    }
```

#### 5.4.2 和弦聚类
```python
from sklearn.cluster import DBSCAN
import numpy as np

def generate_ai_playlists(user_id):
    """
    为用户生成 AI 歌单
    """
    # 1. 获取用户所有歌单中的歌曲
    songs = get_user_songs(user_id)
    
    # 2. 分析每首歌的和弦走向
    progressions = []
    for song in songs:
        pattern = analyze_chord_progression(song)
        progressions.append({
            'song_id': song.id,
            'pattern': pattern['pattern']
        })
    
    # 3. 将走向模式向量化
    vectors = vectorize_progressions(progressions)
    
    # 4. 使用聚类算法分组
    clustering = DBSCAN(eps=0.3, min_samples=2)
    labels = clustering.fit_predict(vectors)
    
    # 5. 为每个聚类创建歌单
    playlists = []
    for label in set(labels):
        if label == -1:  # 跳过噪声点
            continue
        
        # 获取该聚类的歌曲
        cluster_songs = [
            p['song_id'] for i, p in enumerate(progressions)
            if labels[i] == label
        ]
        
        # 识别该聚类的代表性走向
        representative_pattern = get_representative_pattern(
            [p for i, p in enumerate(progressions) if labels[i] == label]
        )
        
        # 创建歌单
        playlist = create_ai_playlist(
            user_id=user_id,
            songs=cluster_songs,
            pattern=representative_pattern,
            name=f"{representative_pattern} 走向"
        )
        playlists.append(playlist)
    
    return playlists
```

#### 5.4.3 常见和弦走向库
```python
COMMON_PROGRESSIONS = {
    '1-5-6-4': {
        'name': '卡农走向',
        'examples': ['Someone Like You', '童年', '小情歌'],
        'difficulty': '简单',
        'description': '最常见的流行歌曲走向，易学易弹'
    },
    '1-6-4-5': {
        'name': '经典50s走向',
        'examples': ['月亮代表我的心', 'Stand By Me'],
        'difficulty': '简单',
        'description': '经典的50年代流行走向，温暖动听'
    },
    '6-4-1-5': {
        'name': '悲伤走向',
        'examples': ['演员', '说散就散'],
        'difficulty': '中等',
        'description': '从小调开始，营造忧伤氛围'
    },
    '1-3-6-4': {
        'name': '明亮走向',
        'examples': ['晴天', '告白气球'],
        'difficulty': '中等',
        'description': '充满阳光感的走向'
    },
    '2-5-1': {
        'name': '爵士走向',
        'examples': ['Fly Me To The Moon', 'Autumn Leaves'],
        'difficulty': '困难',
        'description': '爵士乐中的经典进行'
    }
}
```

---

## 6. 开发计划

### 6.1 MVP (最小可行产品) - Phase 1

**目标：验证核心功能**

✅ 必须功能
- [ ] 用户注册/登录
- [ ] 谱库浏览
- [ ] 基础搜索
- [ ] 曲谱播放/显示
- [ ] 创建歌单
- [ ] 添加歌曲到歌单
- [ ] 基础播放控制（上一曲/下一曲）

📱 技术选型
- 跨平台：Flutter 或 React Native
- 后端：Node.js + Express + PostgreSQL
- 部署：Heroku / DigitalOcean

⏱ 预计时间：6-8 周

### 6.2 Phase 2 - 完善体验

✅ 增强功能
- [ ] 自动滚动
- [ ] 转调功能
- [ ] 和弦指法参考
- [ ] 收藏功能
- [ ] 高级搜索/筛选
- [ ] 用户统计
- [ ] 点数系统

🎨 设计优化
- [ ] 深色模式
- [ ] 流畅动画
- [ ] 响应式布局

### 6.3 Phase 3 - AI 功能

✅ AI 歌单
- [ ] 和弦分析引擎
- [ ] 和弦走向识别
- [ ] 自动聚类分组
- [ ] AI 歌单生成
- [ ] 走向说明页面

🤖 技术实现
- [ ] 音乐理论库集成
- [ ] 机器学习模型训练
- [ ] 批量分析任务队列

### 6.4 Phase 4 - 商业化

✅ 付费功能
- [ ] 会员系统
- [ ] 充值中心
- [ ] 支付集成（微信/支付宝/Apple Pay）
- [ ] 高级曲谱解锁
- [ ] 离线下载

📊 运营功能
- [ ] 数据统计后台
- [ ] 用户行为分析
- [ ] 曲谱管理后台
- [ ] 推送通知

---

## 7. 用户体验亮点

### 7.1 类 Apple Music 体验

✨ **精致的视觉设计**
- 大专辑封面展示
- 毛玻璃背景效果
- 流畅的过渡动画
- 一致的设计语言

✨ **直观的交互**
- 手势操作（滑动切歌）
- 长按快捷菜单
- 拖拽排序
- 触觉反馈

✨ **智能功能**
- AI 自动生成歌单
- 个性化推荐
- 练习进度追踪
- 学习曲线统计

### 7.2 吉他手专属功能

🎸 **和弦谱优化**
- 清晰的对齐显示
- 可调节字体大小
- 和弦高亮
- 段落标识

🎸 **练习辅助**
- 自动滚动
- 速度可调
- 循环播放段落
- 节拍器（可选）

🎸 **学习工具**
- 和弦指法图
- 转调功能
- 难度标识
- 练习统计

### 7.3 社区功能（未来扩展）

👥 **分享与互动**
- 分享歌单到社交媒体
- 查看热门曲谱
- 用户评论和评分
- 演奏视频上传
- 跟弹挑战

---

## 8. 商业模式

### 8.1 收入来源

**1. 会员订阅**
- 月费：¥15
- 季费：¥40
- 年费：¥128

**2. 单次购买**
- 点数充值
- 高级曲谱解锁
- 独家内容

**3. 广告收入**（免费用户）
- 横幅广告
- 激励视频广告

**4. 合作分成**
- 吉他品牌合作
- 在线课程推广
- 乐器销售佣金

### 8.2 成本估算

**开发成本**
- 前端开发：2人 × 3个月
- 后端开发：1人 × 3个月
- UI/UX设计：1人 × 2个月
- 测试：1人 × 1个月

**运营成本**（月度）
- 服务器：¥2,000-5,000
- CDN/存储：¥1,000-3,000
- 支付手续费：营收的2-3%
- 运营人员：1-2人

### 8.3 盈利预测

**保守估算**（上线1年后）
- 日活用户：10,000
- 付费转化率：3%
- 月付费用户：300
- 月订阅收入：300 × ¥15 = ¥4,500
- 加上点数购买和广告：月收入 ¥8,000-12,000

**乐观估算**（上线2年后）
- 日活用户：50,000
- 付费转化率：5%
- 月付费用户：2,500
- 月订阅收入：2,500 × ¥15 = ¥37,500
- 总月收入：¥60,000-80,000

---

## 9. 风险与挑战

### 9.1 技术挑战

⚠️ **和弦识别准确性**
- 解决方案：建立和弦规则库，人工审核

⚠️ **大规模曲谱管理**
- 解决方案：使用CDN，分级缓存策略

⚠️ **离线功能同步**
- 解决方案：增量同步，冲突解决机制

### 9.2 内容挑战

⚠️ **曲谱版权问题**
- 解决方案：
  - UGC模式（用户上传，平台审核）
  - 与出版社合作获得授权
  - 只显示和弦，不包含完整歌词

⚠️ **曲谱质量控制**
- 解决方案：
  - 用户评分系统
  - 专业审核团队
  - 错误报告功能

### 9.3 市场挑战

⚠️ **竞品分析**
- 现有产品：《吉他谱》《弹琴吧》《虫虫吉他》
- 差异化：
  - 更优雅的界面（Apple Music 风格）
  - AI 智能歌单（独特功能）
  - 更好的播放体验

⚠️ **用户获取成本**
- 解决方案：
  - 社交媒体营销
  - 吉他社区推广
  - 口碑传播激励

---

## 10. 成功指标 (KPI)

### 10.1 用户指标
- **DAU (日活用户)**：目标 10,000+
- **MAU (月活用户)**：目标 50,000+
- **留存率**：
  - 次日留存：>40%
  - 7日留存：>25%
  - 30日留存：>15%

### 10.2 业务指标
- **付费转化率**：>3%
- **ARPU (每用户平均收入)**：>¥15/月
- **用户生命周期价值 (LTV)**：>¥300

### 10.3 使用指标
- **平均会话时长**：>10分钟
- **每用户平均查看曲谱数**：>5首/天
- **歌单创建率**：>30%
- **AI歌单使用率**：>20%

---

## 11. 总结

这款吉他和弦谱应用将结合 Apple Music 的优雅设计和专业的吉他学习功能，为用户提供：

✅ **核心价值**
1. 清晰易读的和弦谱显示
2. 智能化的歌单管理体验
3. 独特的 AI 和弦走向分析
4. 优雅现代的用户界面

✅ **竞争优势**
1. 类 Apple Music 的精致设计
2. AI 驱动的智能歌单
3. 流畅的播放和切换体验
4. 完善的学习辅助工具

✅ **发展潜力**
1. 庞大的吉他学习市场
2. 清晰的商业模式
3. 可扩展的功能空间
4. 社区化的发展方向

通过分阶段的开发计划，我们可以快速验证 MVP，然后逐步完善功能，最终打造一款受吉他爱好者喜爱的优质应用。

---

## 附录

### A. 参考资料
- Apple Music 设计规范
- iOS Human Interface Guidelines
- Material Design Guidelines
- 音乐理论基础
- 和弦走向分析方法

### B. 竞品分析
- 吉他谱 App
- 弹琴吧
- 虫虫吉他
- Ultimate Guitar

### C. 用户调研
- 目标用户画像
- 使用场景分析
- 痛点需求收集
- 功能优先级排序

---

**文档版本**: v1.0  
**创建日期**: 2026-01-29  
**最后更新**: 2026-01-29  
**负责人**: Product Team
