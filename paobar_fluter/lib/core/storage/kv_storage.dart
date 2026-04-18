import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

/// 轻量 KV：UI 偏好、最近访问路径、用户选择的主题等。
/// 对 SharedPreferences 做了 JSON 编解码的薄封装。
class KvStorage {
  KvStorage(this._prefs);

  static Future<KvStorage> create() async =>
      KvStorage(await SharedPreferences.getInstance());

  final SharedPreferences _prefs;

  Future<bool> setString(String key, String value) => _prefs.setString(key, value);
  String? getString(String key) => _prefs.getString(key);

  Future<bool> setInt(String key, int value) => _prefs.setInt(key, value);
  int? getInt(String key) => _prefs.getInt(key);

  Future<bool> setBool(String key, {required bool value}) => _prefs.setBool(key, value);
  bool? getBool(String key) => _prefs.getBool(key);

  Future<bool> setJson(String key, Map<String, dynamic> value) =>
      _prefs.setString(key, jsonEncode(value));

  Map<String, dynamic>? getJson(String key) {
    final raw = _prefs.getString(key);
    if (raw == null || raw.isEmpty) return null;
    return jsonDecode(raw) as Map<String, dynamic>?;
  }

  Future<bool> remove(String key) => _prefs.remove(key);
  Future<bool> clear() => _prefs.clear();
}
