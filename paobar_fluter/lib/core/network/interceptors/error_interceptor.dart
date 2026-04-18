import 'dart:async';

import 'package:dio/dio.dart';
import 'package:paobar/core/network/api_exception.dart';

typedef OnUnauthorized = FutureOr<void> Function();

/// 解包后端的 `{code, data, message}` 包络：
/// - 200 → 直接让响应体变成 data 本身，下游 `retrofit` 或手写的 `fromJson`
///   只需要关心业务数据
/// - 401 → 触发 [onUnauthorized]（通常是 AuthCubit 登出 + 弹登录）
/// - 其他非 200 → 抛 [ApiException]
class ErrorInterceptor extends Interceptor {
  ErrorInterceptor({required this.onUnauthorized});

  final OnUnauthorized onUnauthorized;

  @override
  Future<void> onResponse(
    Response<dynamic> response,
    ResponseInterceptorHandler handler,
  ) async {
    final data = response.data;

    if (data is Map<String, dynamic> && data.containsKey('code')) {
      final code = (data['code'] as num?)?.toInt() ?? 500;
      final message = data['message'] as String? ?? 'Unknown error';

      if (code == 200) {
        response.data = data['data'];
        return handler.next(response);
      }

      if (code == 401 || response.statusCode == 401) {
        await onUnauthorized();
        return handler.reject(
          DioException(
            requestOptions: response.requestOptions,
            response: response,
            type: DioExceptionType.badResponse,
            error: ApiException(
              statusCode: response.statusCode ?? 401,
              apiCode: code,
              message: message,
            ),
          ),
        );
      }

      return handler.reject(
        DioException(
          requestOptions: response.requestOptions,
          response: response,
          type: DioExceptionType.badResponse,
          error: ApiException(
            statusCode: response.statusCode ?? 500,
            apiCode: code,
            message: message,
            data: data['data'],
          ),
        ),
      );
    }

    handler.next(response);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    if (err.response?.statusCode == 401) {
      await onUnauthorized();
    }
    handler.next(err);
  }
}
