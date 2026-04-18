import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/app/theme/app_radius.dart';
import 'package:paobar/app/theme/app_typography.dart';
import 'package:paobar/app/theme/theme_extensions.dart';

abstract final class AppTheme {
  AppTheme._();

  static ThemeData get dark {
    const scheme = ColorScheme.dark(
      primary: AppColors.primary,
      onPrimary: Colors.white,
      secondary: AppColors.chordAccent,
      onSecondary: Colors.white,
      surface: AppColors.backgroundCard,
      onSurface: AppColors.textPrimary,
      error: AppColors.error,
      onError: Colors.white,
    );

    final base = ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: scheme,
      scaffoldBackgroundColor: AppColors.backgroundPage,
      canvasColor: AppColors.backgroundPage,
      dividerColor: AppColors.divider,
      splashFactory: InkSparkle.splashFactory,
      textTheme: AppTypography.build(),
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.backgroundCard,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: true,
        systemOverlayStyle: SystemUiOverlayStyle.light,
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: AppColors.backgroundCard,
        selectedItemColor: AppColors.primary,
        unselectedItemColor: AppColors.textSecondary,
        showUnselectedLabels: true,
        type: BottomNavigationBarType.fixed,
      ),
      navigationBarTheme: const NavigationBarThemeData(
        backgroundColor: AppColors.backgroundCard,
        indicatorColor: Color(0x33DC2626),
      ),
      navigationRailTheme: const NavigationRailThemeData(
        backgroundColor: AppColors.backgroundCard,
        selectedIconTheme: IconThemeData(color: AppColors.primary),
        unselectedIconTheme: IconThemeData(color: AppColors.textSecondary),
        selectedLabelTextStyle: TextStyle(color: AppColors.primary),
        unselectedLabelTextStyle: TextStyle(color: AppColors.textSecondary),
        indicatorColor: Color(0x33DC2626),
      ),
      cardTheme: const CardTheme(
        color: AppColors.backgroundCard,
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: AppRadius.br12),
        margin: EdgeInsets.zero,
      ),
      dialogTheme: const DialogTheme(
        backgroundColor: AppColors.backgroundCard,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(borderRadius: AppRadius.br16),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.backgroundOverlay,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        hintStyle: const TextStyle(color: AppColors.textSecondary),
        border: OutlineInputBorder(
          borderRadius: AppRadius.br12,
          borderSide: BorderSide(color: AppColors.divider.withOpacity(0.5)),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: AppRadius.br12,
          borderSide: BorderSide(color: AppColors.divider.withOpacity(0.5)),
        ),
        focusedBorder: const OutlineInputBorder(
          borderRadius: AppRadius.br12,
          borderSide: BorderSide(color: AppColors.primary),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(48),
          shape: const RoundedRectangleBorder(borderRadius: AppRadius.br12),
          textStyle: const TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.textPrimary,
          side: BorderSide(color: AppColors.divider.withOpacity(0.6)),
          minimumSize: const Size.fromHeight(48),
          shape: const RoundedRectangleBorder(borderRadius: AppRadius.br12),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(foregroundColor: AppColors.primary),
      ),
      iconTheme: const IconThemeData(color: AppColors.textPrimary, size: 22),
      listTileTheme: const ListTileThemeData(
        iconColor: AppColors.textSecondary,
        textColor: AppColors.textPrimary,
      ),
      bottomSheetTheme: const BottomSheetThemeData(
        backgroundColor: AppColors.backgroundCard,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(AppRadius.xxl)),
        ),
        showDragHandle: true,
      ),
      snackBarTheme: const SnackBarThemeData(
        backgroundColor: AppColors.backgroundCard,
        contentTextStyle: TextStyle(color: AppColors.textPrimary),
        behavior: SnackBarBehavior.floating,
      ),
      extensions: const <ThemeExtension<dynamic>>[
        TabPalette.dark,
        SemanticColors.dark,
      ],
    );

    return base;
  }

  /// 占位亮色主题，短期默认不启用；保留以便将来响应系统主题。
  static ThemeData get light => dark;
}
