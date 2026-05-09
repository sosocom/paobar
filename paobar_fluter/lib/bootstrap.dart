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
/// - 初始化 Flutter binding（必须与 runApp 在同一个 Zone）
/// - 打日志设定 + BlocObserver
/// - DI 配置
/// - 全局错误捕获
/// - 启动 MaterialApp
Future<void> bootstrap() async {
  // 全局错误捕获（Flutter 框架自身错误）
  FlutterError.onError = (details) {
    FlutterError.presentError(details);
    if (!kDebugMode) {
      // ignore: unused_element
      // TODO(observability): 接入 Sentry / Firebase Crashlytics
    }
  };

  await runZonedGuarded<Future<void>>(
    () async {
      // `ensureInitialized` 必须与 `runApp` 处于同一个 Zone，否则 Flutter 3.22+
      // 会报 Zone mismatch 警告。所以放在 runZonedGuarded 内部。
      WidgetsFlutterBinding.ensureInitialized();

      if (PlatformInfo.isWeb) {
        url_strategy.usePathUrlStrategy();
      }

      if (PlatformInfo.isDesktop) {
        await DesktopWindow.configure();
      }

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
