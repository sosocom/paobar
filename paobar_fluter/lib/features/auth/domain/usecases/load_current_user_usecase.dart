import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/auth/domain/entities/user.dart';
import 'package:paobar/features/auth/domain/repositories/auth_repository.dart';

class LoadCurrentUserUseCase {
  LoadCurrentUserUseCase(this._repo);
  final AuthRepository _repo;

  Future<Result<User>> call() => _repo.loadCurrentUser();
}
