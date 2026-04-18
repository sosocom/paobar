import 'package:freezed_annotation/freezed_annotation.dart';

part 'failure.freezed.dart';

/// 领域层统一错误类型。所有 Repository/UseCase 只抛或返回 Failure，
/// UI 层据此渲染不同的错误提示。
@freezed
sealed class Failure with _$Failure {
  const factory Failure.network({String? message}) = NetworkFailure;

  const factory Failure.timeout({String? message}) = TimeoutFailure;

  const factory Failure.unauthorized({String? message}) = UnauthorizedFailure;

  const factory Failure.forbidden({String? message}) = ForbiddenFailure;

  const factory Failure.notFound({String? message}) = NotFoundFailure;

  const factory Failure.server({
    int? statusCode,
    int? apiCode,
    String? message,
  }) = ServerFailure;

  const factory Failure.cache({String? message}) = CacheFailure;

  const factory Failure.unknown({String? message, Object? cause}) = UnknownFailure;
}

extension FailureX on Failure {
  String get displayMessage {
    return when(
      network: (m) => m ?? '网络不可用，请检查后重试',
      timeout: (m) => m ?? '请求超时，请重试',
      unauthorized: (m) => m ?? '登录已过期，请重新登录',
      forbidden: (m) => m ?? '无权访问',
      notFound: (m) => m ?? '资源不存在',
      server: (_, __, m) => m ?? '服务暂时不可用',
      cache: (m) => m ?? '本地数据读取失败',
      unknown: (m, _) => m ?? '未知错误',
    );
  }
}
