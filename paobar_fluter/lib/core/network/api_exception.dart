/// 响应包络层面的异常：`{code: !=200, message}` 会被转成这个类型，
/// 最终在 UI 或 Repository 层统一 handle。
class ApiException implements Exception {
  ApiException({
    required this.statusCode,
    required this.apiCode,
    required this.message,
    this.data,
  });

  /// HTTP 状态码。
  final int statusCode;

  /// 后端包络里的业务码（和 web 对齐：200 成功、401 登录过期、其他失败）。
  final int apiCode;
  final String message;
  final Object? data;

  @override
  String toString() =>
      'ApiException(status=$statusCode, apiCode=$apiCode, message=$message)';
}
