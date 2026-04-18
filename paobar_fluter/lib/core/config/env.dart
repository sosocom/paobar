import 'package:paobar/core/config/flavors.dart';

/// 环境配置单例。所有值通过 `--dart-define=xxx=yyy` 在编译期注入，
/// 保证产物里不会打包任何硬编码的私有地址/密钥。
class Env {
  Env._({
    required this.apiBaseUrl,
    required this.flavor,
    required this.enableLogging,
    required this.connectTimeoutMs,
    required this.receiveTimeoutMs,
  });

  static const _apiBaseUrlDefine = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://localhost:8001',
  );
  static const _envDefine = String.fromEnvironment('ENV', defaultValue: 'dev');

  final String apiBaseUrl;
  final Flavor flavor;
  final bool enableLogging;
  final int connectTimeoutMs;
  final int receiveTimeoutMs;

  static Env? _instance;

  static Env get I {
    return _instance ??= _build();
  }

  static Env _build() {
    final flavor = Flavor.fromString(_envDefine);
    return Env._(
      apiBaseUrl: _apiBaseUrlDefine,
      flavor: flavor,
      enableLogging: !flavor.isProd,
      connectTimeoutMs: 10000,
      receiveTimeoutMs: 15000,
    );
  }

  @override
  String toString() =>
      'Env(flavor=${flavor.name}, apiBaseUrl=$apiBaseUrl, logging=$enableLogging)';
}
