import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/auth/domain/entities/user.dart';

abstract class AuthRepository {
  Future<Result<({String token, User user})>> login(String username, String password);

  Future<Result<({String token, User user})>> register(String username, String password);

  Future<Result<User>> loadCurrentUser();

  Future<Result<void>> logout();

  Future<String?> readCachedToken();
}
