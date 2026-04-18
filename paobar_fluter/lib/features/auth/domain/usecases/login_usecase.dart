import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/auth/domain/entities/user.dart';
import 'package:paobar/features/auth/domain/repositories/auth_repository.dart';

class LoginUseCase {
  LoginUseCase(this._repo);
  final AuthRepository _repo;

  Future<Result<({String token, User user})>> call({
    required String username,
    required String password,
  }) =>
      _repo.login(username, password);
}
