import 'package:flutter/material.dart';

/// 设计令牌·颜色：直接对齐 `paobar_web/tailwind.config.js` 与
/// `NowPlaying.vue` 里的 xhe-sheet tokens，保证和 Web 视觉一致。
abstract final class AppColors {
  AppColors._();

  // ---------- 品牌主色 ----------
  static const Color primary = Color(0xFFDC2626);
  static const Color primaryHover = Color(0xFFB91C1C);

  /// 谱面和弦专用红：暗底下比 primary 更易读，对齐 NowPlaying 的 --accent
  static const Color chordAccent = Color(0xFFEF4444);
  static const Color chordAccentSoft = Color(0x24EF4444); // rgba(239,68,68,0.14)

  // ---------- 背景层 ----------
  static const Color backgroundPage = Color(0xFF0A0A0A);
  static const Color backgroundCard = Color(0xFF18181B);
  static const Color backgroundOverlay = Color(0xFF09090B);

  // ---------- 文字 ----------
  static const Color textPrimary = Color(0xFFFAFAFA);
  static const Color textSecondary = Color(0xFF71717A);
  static const Color textTertiary = Color(0xFFA1A1AA);

  // ---------- 谱面 / 分隔 ----------
  static const Color metaBorder = Color(0x24FFFFFF); // rgba(255,255,255,0.14)
  static const Color headlineBg = Color(0x0AFFFFFF); // rgba(255,255,255,0.04)
  static const Color chordUnderline = Color(0x38FFFFFF); // rgba(255,255,255,0.22)
  static const Color divider = Color(0x14FFFFFF); // rgba(255,255,255,0.08)

  // ---------- 状态色 ----------
  static const Color success = Color(0xFF22C55E);
  static const Color warning = Color(0xFFF59E0B);
  static const Color error = Color(0xFFEF4444);
  static const Color info = Color(0xFF3B82F6);
}
