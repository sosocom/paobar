import 'package:paobar/core/errors/error_mapper.dart';
import 'package:paobar/core/result/result.dart';
import 'package:paobar/core/storage/secure_storage.dart';
import 'package:paobar/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:paobar/features/auth/domain/entities/user.dart';
import 'package:paobar/features/auth/domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  AuthRepositoryImpl(this._remote, this._secure);

  final AuthRemoteDataSource _remote;
  final SecureStorage _secure;

  @override
  Future<Result<({String token, User user})>> login(
    String username,
    String password,
  ) async {
    try {
      final res = await _remote.login(username, password);
      await _secure.writeToken(res.token);
      return Result.success((token: res.token, user: res.user.toEntity()));
    } catch (e, st) {
      return Result.error(mapErrorToFailure(e, st));
    }
  }

  @override
  Future<Result<({String token, User user})>> register(
    String username,
    String password,
  ) async {
    try {
      final res = await _remote.register(username, password);
      await _secure.writeToken(res.token);
      return Result.success((token: res.token, user: res.user.toEntity()));
    } catch (e, st) {
      return Result.error(mapErrorToFailure(e, st));
    }
  }

  @override
  Future<Result<User>> loadCurrentUser() async {
    try {
      final user = await _remote.loadProfile();
      return Result.success(user.toEntity());
    } catch (e, st) {
      return Result.error(mapErrorToFailure(e, st));
    }
  }

  @override
  Future<Result<void>> logout() async {
    await _secure.deleteToken();
    await _secure.deleteRefreshToken();
    return const Result.success(null);
  }

  @override
  Future<String?> readCachedToken() => _secure.readToken();
}
