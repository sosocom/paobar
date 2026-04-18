import 'package:dio/dio.dart';
import 'package:paobar/features/auth/data/models/auth_response.dart';
import 'package:paobar/features/auth/data/models/user_model.dart';

/// 对接 paobar_java：
/// - POST /api/auth/login   { username, password } → { token, user }
/// - POST /api/auth/register
/// - GET  /api/user/profile
///
/// 响应已经被 ErrorInterceptor 解包成 data 本身，这里直接处理 body。
class AuthRemoteDataSource {
  AuthRemoteDataSource(this._dio);

  final Dio _dio;

  Future<AuthResponse> login(String username, String password) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/api/auth/login',
      data: <String, dynamic>{
        'username': username,
        'password': password,
      },
    );
    return AuthResponse.fromJson(res.data!);
  }

  Future<AuthResponse> register(String username, String password) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/api/auth/register',
      data: <String, dynamic>{
        'username': username,
        'password': password,
      },
    );
    return AuthResponse.fromJson(res.data!);
  }

  Future<UserModel> loadProfile() async {
    final res = await _dio.get<Map<String, dynamic>>('/api/user/profile');
    return UserModel.fromJson(res.data!);
  }
}
