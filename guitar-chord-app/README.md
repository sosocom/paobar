# 吉他和弦谱 App

基于 Vue 3 + TypeScript + Vite + Tailwind CSS 构建的吉他和弦谱移动端应用。

## 功能特性

- 📚 **谱库** - 浏览和搜索吉他和弦谱
- 🎵 **歌单** - 创建用户歌单和 AI 智能歌单
- 🎸 **播放页面** - 查看和弦谱，快速切换歌曲
- 👤 **个人中心** - 管理收藏、歌单和设置

## 技术栈

- Vue 3 (Composition API)
- TypeScript
- Vue Router
- Vite
- Tailwind CSS
- Lucide Icons

## 开始使用

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
src/
├── api/            # API 服务层（Mock API）
├── components/     # 公共组件
├── views/          # 页面组件
├── router/         # 路由配置
├── data/           # 模拟数据
├── types/          # TypeScript 类型定义
├── App.vue         # 根组件
├── main.ts         # 入口文件
└── style.css       # 全局样式
```

## 架构说明

### Mock API 层

项目使用前端 Mock API 模拟真实的后端接口调用：

- **位置**: `src/api/index.ts`
- **特性**:
  - 模拟网络延迟（300-500ms）
  - 支持异步操作
  - 统一的 API 接口
  - 便于后期替换为真实 API

**可用的 API 方法**:

```typescript
// 歌曲相关
api.getSongs(params)           // 获取歌曲列表（支持筛选和搜索）
api.getSongById(id)            // 获取单首歌曲

// 歌单相关
api.getPlaylists()             // 获取所有歌单
api.getPlaylistsByType(type)   // 根据类型获取歌单
api.getPlaylistById(id)        // 获取歌单详情
api.getPlaylistSongs(id)       // 获取歌单中的歌曲

// 用户相关
api.getCurrentUser()           // 获取当前用户信息
api.getCurrentPlaylist()       // 获取当前播放列表

// 操作相关
api.createPlaylist(name)       // 创建歌单
api.addSongToPlaylist(...)     // 添加歌曲到歌单
api.favoriteSong(id)           // 收藏歌曲
```

### 数据加载

所有页面组件都使用 API 层加载数据：

- 显示加载状态（loading spinner）
- 异步数据获取
- 错误处理
- 响应式更新

## 设计风格

- 深色模式
- 毛玻璃效果
- 圆角设计
- 红色主题色 (#DC2626)

## 开发说明

### Mock API 使用

当前使用前端 Mock API 进行开发，所有数据请求都会：

1. 模拟网络延迟
2. 返回预设的模拟数据
3. 显示加载状态

### 后端集成

未来接入真实后端时，只需修改 `src/api/index.ts`：

```typescript
// 将 Mock API 替换为真实的 HTTP 请求
async getSongs(params) {
  const response = await fetch('/api/songs', {
    method: 'GET',
    params
  })
  return response.json()
}
```

所有页面组件无需修改，因为它们都通过 API 层访问数据。
