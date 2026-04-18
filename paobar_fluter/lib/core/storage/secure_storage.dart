import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// 负责存 JWT 等敏感数据：iOS Keychain / Android EncryptedSharedPreferences /
/// macOS Keychain / Windows Credentials Locker / Web IndexedDB 加密。
class SecureStorage {
  SecureStorage([FlutterSecureStorage? storage])
      : _storage = storage ??
            const FlutterSecureStorage(
              aOptions: AndroidOptions(encryptedSharedPreferences: true),
              iOptions: IOSOptions(
                accessibility: KeychainAccessibility.first_unlock,
              ),
            );

  static const _kToken = 'paobar.jwt';
  static const _kRefresh = 'paobar.refresh';

  final FlutterSecureStorage _storage;

  Future<void> writeToken(String value) => _storage.write(key: _kToken, value: value);
  Future<String?> readToken() => _storage.read(key: _kToken);
  Future<void> deleteToken() => _storage.delete(key: _kToken);

  Future<void> writeRefreshToken(String value) => _storage.write(key: _kRefresh, value: value);
  Future<String?> readRefreshToken() => _storage.read(key: _kRefresh);
  Future<void> deleteRefreshToken() => _storage.delete(key: _kRefresh);

  Future<void> clear() => _storage.deleteAll();
}
