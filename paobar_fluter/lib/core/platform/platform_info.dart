import 'dart:io' show Platform;

import 'package:flutter/foundation.dart';

/// `dart:io` 在 Web 上会抛，所以统一包一层，调用方不用关心平台检测分支。
class PlatformInfo {
  PlatformInfo._();

  static bool get isWeb => kIsWeb;
  static bool get isAndroid => !kIsWeb && Platform.isAndroid;
  static bool get isIOS => !kIsWeb && Platform.isIOS;
  static bool get isMacOS => !kIsWeb && Platform.isMacOS;
  static bool get isWindows => !kIsWeb && Platform.isWindows;
  static bool get isLinux => !kIsWeb && Platform.isLinux;

  static bool get isMobile => isAndroid || isIOS;
  static bool get isDesktop => isMacOS || isWindows || isLinux;

  /// 需要 SafeArea 处理的刘海屏型设备
  static bool get hasNotch => isIOS || isAndroid;
}
