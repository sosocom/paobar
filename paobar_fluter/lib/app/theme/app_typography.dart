import 'package:flutter/material.dart';
import 'package:paobar/app/theme/app_colors.dart';

/// 字体族回退链：和 NowPlaying.vue 的 font-family 一致，保证中英文混排时
/// 谱面字形和 Web 端视觉等价。
const List<String> kChineseFontFallback = <String>[
  'Hiragino Sans GB',
  'PingFang SC',
  'Microsoft YaHei',
  'Noto Sans CJK SC',
  'Arial',
];

const List<String> kChordFontFamily = <String>[
  'Helvetica Neue',
  'Arial',
  'sans-serif',
];

abstract final class AppTypography {
  AppTypography._();

  static const String primaryFontFamily = 'HiraginoSansGB';

  static TextTheme build({Color primary = AppColors.textPrimary, Color secondary = AppColors.textSecondary}) {
    return TextTheme(
      displayLarge: TextStyle(fontSize: 32, height: 1.2, fontWeight: FontWeight.w600, color: primary, fontFamilyFallback: kChineseFontFallback),
      displayMedium: TextStyle(fontSize: 28, height: 1.25, fontWeight: FontWeight.w600, color: primary, fontFamilyFallback: kChineseFontFallback),
      displaySmall: TextStyle(fontSize: 24, height: 1.3, fontWeight: FontWeight.w600, color: primary, fontFamilyFallback: kChineseFontFallback),
      headlineLarge: TextStyle(fontSize: 22, height: 1.3, fontWeight: FontWeight.w600, color: primary, fontFamilyFallback: kChineseFontFallback),
      headlineMedium: TextStyle(fontSize: 20, height: 1.35, fontWeight: FontWeight.w600, color: primary, fontFamilyFallback: kChineseFontFallback),
      headlineSmall: TextStyle(fontSize: 18, height: 1.4, fontWeight: FontWeight.w600, color: primary, fontFamilyFallback: kChineseFontFallback),
      titleLarge: TextStyle(fontSize: 17, height: 1.45, fontWeight: FontWeight.w600, color: primary, fontFamilyFallback: kChineseFontFallback),
      titleMedium: TextStyle(fontSize: 15, height: 1.45, fontWeight: FontWeight.w500, color: primary, fontFamilyFallback: kChineseFontFallback),
      titleSmall: TextStyle(fontSize: 13, height: 1.45, fontWeight: FontWeight.w500, color: primary, fontFamilyFallback: kChineseFontFallback),
      bodyLarge: TextStyle(fontSize: 17, height: 1.5, color: primary, fontFamilyFallback: kChineseFontFallback),
      bodyMedium: TextStyle(fontSize: 15, height: 1.5, color: primary, fontFamilyFallback: kChineseFontFallback),
      bodySmall: TextStyle(fontSize: 13, height: 1.5, color: secondary, fontFamilyFallback: kChineseFontFallback),
      labelLarge: TextStyle(fontSize: 14, height: 1.4, fontWeight: FontWeight.w500, color: primary, fontFamilyFallback: kChineseFontFallback),
      labelMedium: TextStyle(fontSize: 12, height: 1.4, color: secondary, fontFamilyFallback: kChineseFontFallback),
      labelSmall: TextStyle(fontSize: 11, height: 1.4, color: secondary, fontFamilyFallback: kChineseFontFallback),
    );
  }
}
