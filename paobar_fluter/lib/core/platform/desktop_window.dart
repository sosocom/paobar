import 'package:flutter/widgets.dart';
import 'package:paobar/core/platform/platform_info.dart';

/// 桌面端窗口最小尺寸配置。
///
/// 说明：为了保持零额外依赖，这里使用 Flutter 官方 channel
/// (`flutter/platform_views` 的 `Window.setMinSize` 在当前 embedder
/// 并未正式暴露)。因此当前实现仅做「行为占位」：
/// 最终产品接入 `window_manager` / `bitsdojo_window` 后，在这里替换
/// 为真正的 `WindowManager.instance.setMinimumSize(...)`。
///
/// 目的：
/// - 保留统一的调用入口 `DesktopWindow.configure()`，不污染业务代码；
/// - 明确在 Bootstrap 里声明桌面端窗口契约，便于后续接入。
class DesktopWindow {
  DesktopWindow._();

  /// 建议最小尺寸：对齐 Web breakpoint `md` (768) 以上，
  /// 避免被压到移动布局挡位。
  static const Size minSize = Size(860, 640);

  /// 默认启动尺寸
  static const Size defaultSize = Size(1280, 800);

  static Future<void> configure() async {
    if (!PlatformInfo.isDesktop) return;
    // TODO(platform): 接入 window_manager 后放开：
    //   await windowManager.ensureInitialized();
    //   await windowManager.setMinimumSize(minSize);
    //   await windowManager.setSize(defaultSize);
    //   await windowManager.setTitle('泡吧吉他谱');
    //   await windowManager.center();
  }
}
