import 'package:flutter/services.dart';
import 'package:paobar/core/platform/platform_info.dart';

/// 桌面 / Web 上 HapticFeedback 是 no-op，这里直接做一层无害的封装。
class HapticsService {
  Future<void> selection() async {
    if (!PlatformInfo.isMobile) return;
    await HapticFeedback.selectionClick();
  }

  Future<void> lightImpact() async {
    if (!PlatformInfo.isMobile) return;
    await HapticFeedback.lightImpact();
  }

  Future<void> mediumImpact() async {
    if (!PlatformInfo.isMobile) return;
    await HapticFeedback.mediumImpact();
  }
}
