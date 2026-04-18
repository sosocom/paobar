import 'package:hive_flutter/hive_flutter.dart';

/// Hive 轻量离线缓存：最近查看的谱、收藏 ID 列表等。
/// 业务层直接 [openBox] 拿类型化的 [Box]。
class CacheBox {
  CacheBox._();

  static const String tabsBox = 'paobar.cache.tabs';
  static const String favoritesBox = 'paobar.cache.favorites';
  static const String lastViewedBox = 'paobar.cache.lastViewed';

  static Future<void> init() async {
    await Hive.initFlutter('paobar');
  }

  static Future<Box<String>> openString(String name) => Hive.openBox<String>(name);
  static Future<Box<dynamic>> openDynamic(String name) => Hive.openBox<dynamic>(name);
}
