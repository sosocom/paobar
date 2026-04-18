import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/auth/domain/entities/user.dart';

part 'auth_state.freezed.dart';

@freezed
sealed class AuthState with _$AuthState {
  /// 应用刚启动，还没确定登录状态（读取 token 中）
  const factory AuthState.unknown() = AuthUnknown;

  /// 已登录
  const factory AuthState.authenticated({required User user, required String token}) = Authenticated;

  /// 未登录 / token 失效
  const factory AuthState.unauthenticated({Failure? failure}) = Unauthenticated;

  /// 登录/注册请求中
  const factory AuthState.authenticating() = Authenticating;
}
