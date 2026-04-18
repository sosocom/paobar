import 'package:flutter/material.dart';
import 'package:paobar/app/theme/app_colors.dart';

/// 谱面语义色：从 NowPlaying.vue 的 CSS 变量映射而来，暴露给渲染器使用。
@immutable
class TabPalette extends ThemeExtension<TabPalette> {
  const TabPalette({
    required this.accent,
    required this.accentSoft,
    required this.metaBorder,
    required this.headlineBg,
    required this.underline,
    required this.fontMain,
    required this.fontSecondary,
    required this.fontTertiary,
  });

  /// 对齐 Web 的 `--accent` / `--accent-soft` 等变量。
  final Color accent;
  final Color accentSoft;
  final Color metaBorder;
  final Color headlineBg;
  final Color underline;
  final Color fontMain;
  final Color fontSecondary;
  final Color fontTertiary;

  static const TabPalette dark = TabPalette(
    accent: AppColors.chordAccent,
    accentSoft: AppColors.chordAccentSoft,
    metaBorder: AppColors.metaBorder,
    headlineBg: AppColors.headlineBg,
    underline: AppColors.chordUnderline,
    fontMain: AppColors.textPrimary,
    fontSecondary: Color(0xFFD4D4D8),
    fontTertiary: AppColors.textTertiary,
  );

  @override
  TabPalette copyWith({
    Color? accent,
    Color? accentSoft,
    Color? metaBorder,
    Color? headlineBg,
    Color? underline,
    Color? fontMain,
    Color? fontSecondary,
    Color? fontTertiary,
  }) {
    return TabPalette(
      accent: accent ?? this.accent,
      accentSoft: accentSoft ?? this.accentSoft,
      metaBorder: metaBorder ?? this.metaBorder,
      headlineBg: headlineBg ?? this.headlineBg,
      underline: underline ?? this.underline,
      fontMain: fontMain ?? this.fontMain,
      fontSecondary: fontSecondary ?? this.fontSecondary,
      fontTertiary: fontTertiary ?? this.fontTertiary,
    );
  }

  @override
  TabPalette lerp(ThemeExtension<TabPalette>? other, double t) {
    if (other is! TabPalette) return this;
    return TabPalette(
      accent: Color.lerp(accent, other.accent, t)!,
      accentSoft: Color.lerp(accentSoft, other.accentSoft, t)!,
      metaBorder: Color.lerp(metaBorder, other.metaBorder, t)!,
      headlineBg: Color.lerp(headlineBg, other.headlineBg, t)!,
      underline: Color.lerp(underline, other.underline, t)!,
      fontMain: Color.lerp(fontMain, other.fontMain, t)!,
      fontSecondary: Color.lerp(fontSecondary, other.fontSecondary, t)!,
      fontTertiary: Color.lerp(fontTertiary, other.fontTertiary, t)!,
    );
  }
}

/// 业务语义色（卡片、分隔线、状态等），和 primary 色区分开以便将来增加亮色。
@immutable
class SemanticColors extends ThemeExtension<SemanticColors> {
  const SemanticColors({
    required this.pageBackground,
    required this.cardBackground,
    required this.overlayBackground,
    required this.divider,
    required this.success,
    required this.warning,
    required this.danger,
    required this.info,
  });

  final Color pageBackground;
  final Color cardBackground;
  final Color overlayBackground;
  final Color divider;
  final Color success;
  final Color warning;
  final Color danger;
  final Color info;

  static const SemanticColors dark = SemanticColors(
    pageBackground: AppColors.backgroundPage,
    cardBackground: AppColors.backgroundCard,
    overlayBackground: AppColors.backgroundOverlay,
    divider: AppColors.divider,
    success: AppColors.success,
    warning: AppColors.warning,
    danger: AppColors.error,
    info: AppColors.info,
  );

  @override
  SemanticColors copyWith({
    Color? pageBackground,
    Color? cardBackground,
    Color? overlayBackground,
    Color? divider,
    Color? success,
    Color? warning,
    Color? danger,
    Color? info,
  }) {
    return SemanticColors(
      pageBackground: pageBackground ?? this.pageBackground,
      cardBackground: cardBackground ?? this.cardBackground,
      overlayBackground: overlayBackground ?? this.overlayBackground,
      divider: divider ?? this.divider,
      success: success ?? this.success,
      warning: warning ?? this.warning,
      danger: danger ?? this.danger,
      info: info ?? this.info,
    );
  }

  @override
  SemanticColors lerp(ThemeExtension<SemanticColors>? other, double t) {
    if (other is! SemanticColors) return this;
    return SemanticColors(
      pageBackground: Color.lerp(pageBackground, other.pageBackground, t)!,
      cardBackground: Color.lerp(cardBackground, other.cardBackground, t)!,
      overlayBackground: Color.lerp(overlayBackground, other.overlayBackground, t)!,
      divider: Color.lerp(divider, other.divider, t)!,
      success: Color.lerp(success, other.success, t)!,
      warning: Color.lerp(warning, other.warning, t)!,
      danger: Color.lerp(danger, other.danger, t)!,
      info: Color.lerp(info, other.info, t)!,
    );
  }
}

extension ThemeExtensionReaders on BuildContext {
  TabPalette get tabPalette => Theme.of(this).extension<TabPalette>() ?? TabPalette.dark;
  SemanticColors get semantic => Theme.of(this).extension<SemanticColors>() ?? SemanticColors.dark;
}
