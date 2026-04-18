import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:paobar/app/router/app_router.dart';
import 'package:paobar/app/theme/app_theme.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_cubit.dart';

/// MaterialApp.router 入口。AuthCubit 作为全局 Cubit 挂在根上：
/// - 路由 redirect 监听它
/// - 任何页面可直接 `context.read<AuthCubit>()`
class PaoBarApp extends StatefulWidget {
  const PaoBarApp({super.key});

  @override
  State<PaoBarApp> createState() => _PaoBarAppState();
}

class _PaoBarAppState extends State<PaoBarApp> {
  late final _router = buildAppRouter();

  @override
  void initState() {
    super.initState();
    sl<AuthCubit>().bootstrap();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider<AuthCubit>.value(
      value: sl<AuthCubit>(),
      child: MaterialApp.router(
        title: '泡吧吉他谱',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.dark,
        darkTheme: AppTheme.dark,
        themeMode: ThemeMode.dark,
        routerConfig: _router,
        localizationsDelegates: const [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        supportedLocales: const [
          Locale('zh'),
          Locale('en'),
        ],
      ),
    );
  }
}
