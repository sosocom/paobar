import 'package:paobar/core/network/api_exception.dart';

/// 后端统一包络：`{ code: 200, data: ..., message?: string }`。
/// 与 `paobar_web/src/api/index.ts` 的约定完全对齐。
class ApiEnvelope<T> {
  const ApiEnvelope({
    required this.code,
    required this.message,
    required this.data,
  });

  final int code;
  final String? message;
  final T? data;

  bool get isSuccess => code == 200;

  factory ApiEnvelope.fromJson(
    Map<String, dynamic> json,
    T Function(Object? data)? fromData,
  ) {
    return ApiEnvelope<T>(
      code: (json['code'] as num?)?.toInt() ?? 500,
      message: json['message'] as String?,
      data: fromData == null ? json['data'] as T? : fromData(json['data']),
    );
  }

  /// 解包拿到 data，失败直接抛 [ApiException] 交给 error_interceptor。
  T unwrap({int httpStatus = 200}) {
    if (!isSuccess) {
      throw ApiException(
        statusCode: httpStatus,
        apiCode: code,
        message: message ?? 'Request failed',
      );
    }
    if (data == null) {
      // data 可能为 null（如 void 响应），此处交给调用方自行处理泛型兜底。
      return data as T;
    }
    return data as T;
  }
}
