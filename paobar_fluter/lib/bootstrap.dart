import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:logger/logger.dart';
import 'package:paobar/app/app.dart';
import 'package:paobar/app/app_bloc_observer.dart';
import 'package:paobar/core/config/env.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/core/platform/desktop_window.dart';
import 'package:paobar/core/platform/platform_info.dart';
import 'package:paobar/core/platform/url_strategy.dart' as url_strategy;

/// 统一入口，负责：
/// - 初始化 Flutter binding
/// - 打日志设定 + BlocObserver
/// - DI 配置
/// - 全局错误捕获
/// - 启动 MaterialApp
Future<void> bootstrap() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Web：使用 path-based URL，不带 #，利于 SEO 和深链分享
  if (PlatformInfo.isWeb) {
    url_strategy.usePathUrlStrategy();
  }

  // 桌面端：最小窗口尺寸，防止布局崩到移动端之下
  if (PlatformInfo.isDesktop) {
    await DesktopWindow.configure();
  }

  // 全局错误捕获（Flutter 框架 + Zone 之外的异步错误）
  FlutterError.onError = (details) {
    FlutterError.presentError(details);
    if (!kDebugMode) {
      // TODO: 接入 Sentry / Firebase Crashlytics
    }
  };

  await runZonedGuarded<Future<void>>(
    () async {
      await configureDependencies();

      Bloc.observer = AppBlocObserver();

      if (Env.I.enableLogging) {
        sl<Logger>().i('Bootstrapping ${Env.I}');
      }

      runApp(const PaoBarApp());
    },
    (error, stackTrace) {
      if (kDebugMode) {
        // ignore: avoid_print
        print('Uncaught zone error: $error\n$stackTrace');
      }
    },
  );
}
