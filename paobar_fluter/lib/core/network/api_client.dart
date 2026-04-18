import 'package:dio/dio.dart';
import 'package:paobar/core/config/env.dart';
import 'package:paobar/core/network/interceptors/auth_interceptor.dart';
import 'package:paobar/core/network/interceptors/error_interceptor.dart';
import 'package:paobar/core/network/interceptors/logging_interceptor.dart';

/// 全局 Dio 单例构造器。
/// 经 [ErrorInterceptor] 解包后，`response.data` 会变成后端包络里的 data 字段，
/// 因此 retrofit 接口可以直接把返回值声明为业务类型，不再需要解外层包裹。
class ApiClient {
  ApiClient._(this.dio);

  factory ApiClient.build({
    required AuthInterceptor authInterceptor,
    required ErrorInterceptor errorInterceptor,
  }) {
    final dio = Dio(
      BaseOptions(
        baseUrl: Env.I.apiBaseUrl,
        connectTimeout: Duration(milliseconds: Env.I.connectTimeoutMs),
        receiveTimeout: Duration(milliseconds: Env.I.receiveTimeoutMs),
        sendTimeout: Duration(milliseconds: Env.I.receiveTimeoutMs),
        headers: <String, String>{
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        responseType: ResponseType.json,
      ),
    );

    dio.interceptors.addAll([
      authInterceptor,
      errorInterceptor,
      if (Env.I.enableLogging) buildLoggingInterceptor(),
    ]);

    return ApiClient._(dio);
  }

  final Dio dio;
}
