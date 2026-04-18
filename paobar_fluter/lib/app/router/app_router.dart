import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/router/route_refresh_notifier.dart';
import 'package:paobar/app/router/routes.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/core/responsive/adaptive_layout.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_state.dart';
import 'package:paobar/features/auth/presentation/pages/login_page.dart';
import 'package:paobar/features/crawler/presentation/pages/crawler_page.dart';
import 'package:paobar/features/favorites/presentation/pages/favorites_page.dart';
import 'package:paobar/features/now_playing/presentation/pages/now_playing_page.dart';
import 'package:paobar/features/playlists/presentation/pages/playlist_detail_page.dart';
import 'package:paobar/features/playlists/presentation/pages/playlists_page.dart';
import 'package:paobar/features/profile/presentation/pages/profile_page.dart';
import 'package:paobar/features/settings/presentation/pages/settings_page.dart';
import 'package:paobar/features/songs/presentation/pages/index_page.dart';

class AppRouter {
  AppRouter({required AuthCubit authCubit}) : _authCubit = authCubit;

  final AuthCubit _authCubit;

  late final GoRouter router = GoRouter(
    initialLocation: AppRoutes.index,
    debugLogDiagnostics: false,
    refreshListenable: BlocRefreshNotifier(_authCubit),
    redirect: _redirect,
    routes: <RouteBase>[
      GoRoute(
        path: '/',
        redirect: (_, __) => AppRoutes.index,
      ),
      GoRoute(
        path: AppRoutes.library,
        redirect: (_, __) => AppRoutes.index,
      ),
      GoRoute(
        path: AppRoutes.login,
        name: 'login',
        pageBuilder: (context, state) => const NoTransitionPage(child: LoginPage()),
      ),
      GoRoute(
        path: AppRoutes.nowPlaying,
        name: 'nowPlaying',
        pageBuilder: (context, state) {
          final id = state.pathParameters['id']!;
          final playlistId = state.uri.queryParameters['playlistId'];
          return MaterialPage(
            child: NowPlayingPage(songId: id, playlistId: playlistId),
          );
        },
      ),
      GoRoute(
        path: AppRoutes.crawler,
        name: 'crawler',
        pageBuilder: (_, __) => const MaterialPage(child: CrawlerPage()),
      ),
      GoRoute(
        path: AppRoutes.playlistDetail,
        name: 'playlistDetail',
        pageBuilder: (context, state) {
          final id = state.pathParameters['id']!;
          return MaterialPage(child: PlaylistDetailPage(playlistId: id));
        },
      ),
      GoRoute(
        path: AppRoutes.settings,
        name: 'settings',
        pageBuilder: (_, __) => const MaterialPage(child: SettingsPage()),
      ),
      // Shell：包含底部导航 / NavigationRail 的顶级 tab
      ShellRoute(
        builder: (context, state, child) {
          return _RootShell(
            location: state.matchedLocation,
            child: child,
          );
        },
        routes: <RouteBase>[
          GoRoute(
            path: AppRoutes.index,
            name: 'index',
            pageBuilder: (_, __) => const NoTransitionPage(child: IndexPage()),
          ),
          GoRoute(
            path: AppRoutes.playlists,
            name: 'playlists',
            pageBuilder: (_, __) => const NoTransitionPage(child: PlaylistsPage()),
          ),
          GoRoute(
            path: AppRoutes.favorites,
            name: 'favorites',
            pageBuilder: (_, __) => const NoTransitionPage(child: FavoritesPage()),
          ),
          GoRoute(
            path: AppRoutes.profile,
            name: 'profile',
            pageBuilder: (_, __) => const NoTransitionPage(child: ProfilePage()),
          ),
        ],
      ),
    ],
  );

  String? _redirect(BuildContext context, GoRouterState state) {
    final loggedIn = _authCubit.state is Authenticated;
    final goingToLogin = state.matchedLocation == AppRoutes.login;
    final pendingAuth =
        AppRoutes.requiresAuth(state.matchedLocation) && !loggedIn;

    if (pendingAuth && !goingToLogin) {
      return Uri(
        path: AppRoutes.login,
        queryParameters: {'redirect': state.matchedLocation},
      ).toString();
    }

    if (loggedIn && goingToLogin) {
      final redirect = state.uri.queryParameters['redirect'];
      return redirect != null && redirect.isNotEmpty ? redirect : AppRoutes.index;
    }

    return null;
  }
}

class _RootShell extends StatelessWidget {
  const _RootShell({required this.child, required this.location});

  final Widget child;
  final String location;

  static const _destinations = <AdaptiveDestination>[
    AdaptiveDestination(
      label: '谱库',
      icon: LucideIcons.library,
      route: AppRoutes.index,
    ),
    AdaptiveDestination(
      label: '歌单',
      icon: LucideIcons.listMusic,
      route: AppRoutes.playlists,
    ),
    AdaptiveDestination(
      label: '收藏',
      icon: LucideIcons.heart,
      route: AppRoutes.favorites,
    ),
    AdaptiveDestination(
      label: '我的',
      icon: LucideIcons.user,
      route: AppRoutes.profile,
    ),
  ];

  int _indexOf(String loc) {
    final idx = _destinations.indexWhere((d) => loc.startsWith(d.route));
    return idx < 0 ? 0 : idx;
  }

  @override
  Widget build(BuildContext context) {
    return AdaptiveNavScaffold(
      destinations: _destinations,
      currentIndex: _indexOf(location),
      onDestinationSelected: (i) => context.go(_destinations[i].route),
      body: child,
    );
  }
}

/// 全局便捷方法：任意层级触发"回到登录页并保留 redirect"。
void goToLoginFromAnywhere(BuildContext context) {
  final current = GoRouterState.of(context).matchedLocation;
  context.go(
    Uri(
      path: AppRoutes.login,
      queryParameters: {'redirect': current},
    ).toString(),
  );
}

/// 方便从 DI 直接取 router。
GoRouter buildAppRouter() {
  return AppRouter(authCubit: sl<AuthCubit>()).router;
}
