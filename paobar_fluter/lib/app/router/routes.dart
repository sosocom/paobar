/// 路由常量 + path 构造函数。`paths.*` 保证没有散落的魔法字符串。
abstract final class AppRoutes {
  AppRoutes._();

  static const String index = '/index';
  static const String library = '/library'; // 兼容 web：重定向到 index
  static const String login = '/login';
  static const String nowPlaying = '/now-playing/:id';
  static const String crawler = '/crawler';
  static const String playlists = '/playlists';
  static const String playlistDetail = '/playlist/:id';
  static const String favorites = '/favorites';
  static const String profile = '/profile';
  static const String settings = '/settings';

  static String nowPlayingOf(String id, {String? playlistId}) {
    final base = '/now-playing/$id';
    return playlistId == null ? base : '$base?playlistId=$playlistId';
  }

  static String playlistDetailOf(String id) => '/playlist/$id';

  /// 顶级 tab（底部导航 / NavigationRail 使用）
  static const List<String> topLevelTabs = <String>[
    index,
    playlists,
    favorites,
    profile,
  ];

  static bool requiresAuth(String path) {
    return path.startsWith('/playlists') ||
        path.startsWith('/playlist/') ||
        path.startsWith('/favorites') ||
        path.startsWith('/profile') ||
        path.startsWith('/settings');
  }
}
