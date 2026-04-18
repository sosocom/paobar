import 'package:flutter/services.dart';
import 'package:paobar/core/platform/platform_info.dart';
import 'package:share_plus/share_plus.dart';

/// 跨端分享：
/// - 移动端 (Android/iOS)：调用系统 Share Sheet。
/// - Web：使用 `navigator.share`，share_plus 已兼容，不支持时回退剪贴板。
/// - 桌面 (macOS/Windows/Linux)：share_plus 在桌面会以"回退"方式实现（打开
///   邮件客户端等体验不佳），这里强制改为「复制到剪贴板」以保证一致体验。
class ShareService {
  Future<ShareOutcome> shareText(String text, {String? subject}) async {
    if (PlatformInfo.isDesktop) {
      await Clipboard.setData(ClipboardData(text: text));
      return ShareOutcome.copiedToClipboard;
    }
    try {
      await Share.share(text, subject: subject);
      return ShareOutcome.shared;
    } catch (_) {
      await Clipboard.setData(ClipboardData(text: text));
      return ShareOutcome.copiedToClipboard;
    }
  }
}

enum ShareOutcome { shared, copiedToClipboard }
