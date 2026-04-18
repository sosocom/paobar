# 泡吧吉他谱 · Flutter 多端

基于 Flutter 的多端（Android / iOS / Web / macOS / Windows）吉他和弦谱应用，复用 `paobar_java` 后端 API 与 `paobar_web` 的设计令牌。

## 技术栈

- Flutter 3.22+ / Dart 3.4+
- 状态管理：`flutter_bloc` + `Cubit` + `freezed`
- 路由：`go_router`
- 网络：`dio` + `retrofit`
- 依赖注入：`get_it`（手动注册，见 `lib/core/di/injector.dart`）
- 存储：`flutter_secure_storage`（JWT）+ `shared_preferences`（UI 偏好）+ `hive`（离线缓存）
- 谱面渲染：`package:html` 解析 → 原生 Flutter Widget 渲染（不依赖 WebView，5 端一致）
- Lint：`very_good_analysis`

## 架构

采用 **Clean Architecture + Feature-first**：

```
lib/
  app/        应用级：MaterialApp、主题、路由
  core/       通用基础设施：DI、网络、存储、错误、响应式、平台、widgets
  features/   业务模块（每个模块下 data / domain / presentation 三层）
    auth/ songs/ now_playing/ playlists/ favorites/ crawler/ profile/ settings/
```

详细架构说明见 [`/Users/jason/.cursor/plans/flutter_多端_app_架构_*.plan.md`](../.cursor/plans/)。

## 首次拉起

> 平台宿主目录（`android/` `ios/` `web/` `macos/` `windows/`）需要在本机用 Flutter SDK 生成，不跟随仓库提交的代码。执行顺序：

```bash
cd paobar_fluter

# 1) 补齐平台 runner（不会覆盖 lib/ 与配置文件）
flutter create . \
  --org com.sosocom \
  --project-name paobar \
  --platforms=android,ios,web,macos,windows

# 2) 拉依赖
flutter pub get

# 3) 生成 freezed / json / retrofit 产物
dart run build_runner build --delete-conflicting-outputs

# 3.5) 生成国际化产物
flutter gen-l10n

# 4) 运行（dev 环境，连本机后端）
flutter run \
  --dart-define=API_BASE_URL=http://localhost:8001 \
  --dart-define=ENV=dev
```

## 日常脚本

```bash
# 重新生成所有 codegen 产物
./scripts/codegen.sh

# 生成本地化（ARB → Dart）
./scripts/l10n.sh
```

## 构建五端

封装在 `scripts/` 下，默认使用 `API_BASE_URL` / `FLAVOR` 环境变量：

```bash
API_BASE_URL=https://api.paobar.com FLAVOR=prod ./scripts/build_android.sh
API_BASE_URL=https://api.paobar.com FLAVOR=prod ./scripts/build_ios.sh
API_BASE_URL=https://api.paobar.com FLAVOR=prod ./scripts/build_web.sh
API_BASE_URL=https://api.paobar.com FLAVOR=prod ./scripts/build_macos.sh
API_BASE_URL=https://api.paobar.com FLAVOR=prod ./scripts/build_windows.sh

# 按当前操作系统批量构建
./scripts/build_all.sh
```

CI 配置见 `.github/workflows/flutter-ci.yml`（analyze + test 强制通过，main 分支自动产出 web 构建产物）。

## 多端注意事项

| 平台 | 导航 | 分享 | 触感 | 窗口 |
| ---- | ---- | ---- | ---- | ---- |
| Android / iOS | Bottom Nav | 系统 Share Sheet | HapticFeedback | — |
| Web | Nav Rail (≥768) | `navigator.share` / 回退剪贴板 | no-op | path URL 深链 |
| macOS / Windows | 侧边栏（≥1200） | 复制到剪贴板 | no-op | 最小 860×640 |

- 桌面最小窗口尺寸在 `lib/core/platform/desktop_window.dart` 维护，接入 `window_manager` 时只改这一个文件。
- Web URL 使用 `usePathUrlStrategy()`，路径形如 `/now-playing/123`，可直接用于深链分享。
