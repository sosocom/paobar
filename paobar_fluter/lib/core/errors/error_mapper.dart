import 'package:dio/dio.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/core/network/api_exception.dart';

/// 将 Dio/平台 异常统一转换为领域层 [Failure]。
Failure mapErrorToFailure(Object error, [StackTrace? stackTrace]) {
  if (error is Failure) return error;

  if (error is ApiException) {
    switch (error.statusCode) {
      case 401:
        return Failure.unauthorized(message: error.message);
      case 403:
        return Failure.forbidden(message: error.message);
      case 404:
        return Failure.notFound(message: error.message);
      default:
        return Failure.server(
          statusCode: error.statusCode,
          apiCode: error.apiCode,
          message: error.message,
        );
    }
  }

  if (error is DioException) {
    switch (error.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return const Failure.timeout();
      case DioExceptionType.badCertificate:
      case DioExceptionType.connectionError:
        return const Failure.network();
      case DioExceptionType.cancel:
        return Failure.unknown(message: 'Request cancelled', cause: error);
      case DioExceptionType.badResponse:
        final code = error.response?.statusCode;
        if (code == 401) return const Failure.unauthorized();
        if (code == 403) return const Failure.forbidden();
        if (code == 404) return const Failure.notFound();
        return Failure.server(statusCode: code, message: error.message);
      case DioExceptionType.unknown:
        return Failure.unknown(message: error.message, cause: error);
    }
  }

  return Failure.unknown(message: error.toString(), cause: error);
}
