import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/auth/domain/repositories/auth_repository.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_state.dart';

class AuthCubit extends Cubit<AuthState> {
  AuthCubit(this._repo) : super(const AuthState.unknown());

  final AuthRepository _repo;

  /// 启动时：读 token → 有就尝试拉 profile → 失败则回到未登录。
  Future<void> bootstrap() async {
    final token = await _repo.readCachedToken();
    if (token == null || token.isEmpty) {
      emit(const AuthState.unauthenticated());
      return;
    }
    final result = await _repo.loadCurrentUser();
    result.fold(
      (user) => emit(AuthState.authenticated(user: user, token: token)),
      (f) => emit(AuthState.unauthenticated(failure: f)),
    );
  }

  Future<bool> login({required String username, required String password}) async {
    emit(const AuthState.authenticating());
    final result = await _repo.login(username, password);
    return result.fold(
      (ok) {
        emit(AuthState.authenticated(user: ok.user, token: ok.token));
        return true;
      },
      (f) {
        emit(AuthState.unauthenticated(failure: f));
        return false;
      },
    );
  }

  Future<bool> register({required String username, required String password}) async {
    emit(const AuthState.authenticating());
    final result = await _repo.register(username, password);
    return result.fold(
      (ok) {
        emit(AuthState.authenticated(user: ok.user, token: ok.token));
        return true;
      },
      (f) {
        emit(AuthState.unauthenticated(failure: f));
        return false;
      },
    );
  }

  Future<void> logout() async {
    await _repo.logout();
    emit(const AuthState.unauthenticated());
  }

  /// 被 ErrorInterceptor 在 401 时回调，清 token 并广播未登录
  Future<void> markUnauthenticated() async {
    await _repo.logout();
    emit(const AuthState.unauthenticated(failure: Failure.unauthorized()));
  }
}
