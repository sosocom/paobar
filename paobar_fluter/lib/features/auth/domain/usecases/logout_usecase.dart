import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/auth/domain/repositories/auth_repository.dart';

class LogoutUseCase {
  LogoutUseCase(this._repo);
  final AuthRepository _repo;

  Future<Result<void>> call() => _repo.logout();
}
