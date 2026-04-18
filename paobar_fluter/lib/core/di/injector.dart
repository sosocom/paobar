import 'package:dio/dio.dart';
import 'package:get_it/get_it.dart';
import 'package:logger/logger.dart';
import 'package:paobar/core/network/api_client.dart';
import 'package:paobar/core/network/interceptors/auth_interceptor.dart';
import 'package:paobar/core/network/interceptors/error_interceptor.dart';
import 'package:paobar/core/platform/clipboard_service.dart';
import 'package:paobar/core/platform/haptics_service.dart';
import 'package:paobar/core/platform/share_service.dart';
import 'package:paobar/core/storage/cache_box.dart';
import 'package:paobar/core/storage/kv_storage.dart';
import 'package:paobar/core/storage/secure_storage.dart';
import 'package:paobar/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:paobar/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:paobar/features/auth/domain/repositories/auth_repository.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:paobar/features/favorites/data/datasources/favorites_remote_datasource.dart';
import 'package:paobar/features/favorites/data/repositories/favorites_repository_impl.dart';
import 'package:paobar/features/favorites/domain/repositories/favorites_repository.dart';
import 'package:paobar/features/favorites/presentation/cubit/favorites_cubit.dart';
import 'package:paobar/features/playlists/data/datasources/playlists_remote_datasource.dart';
import 'package:paobar/features/playlists/data/repositories/playlists_repository_impl.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/songs/data/datasources/songs_remote_datasource.dart';
import 'package:paobar/features/songs/data/repositories/songs_repository_impl.dart';
import 'package:paobar/features/songs/domain/repositories/songs_repository.dart';

/// 全局 service locator。测试里用 `sl.reset()` 清空后逐个 `registerLazySingleton`
/// 替换成 mock 即可。
final GetIt sl = GetIt.instance;

/// 初始化顺序：
/// 1) 纯数据层（无外部依赖）
/// 2) 存储（SharedPreferences 需要 await）
/// 3) 网络（需要 SecureStorage 拿 token + AuthCubit 处理 401）
/// 4) 数据源 + Repository（需要 Dio）
/// 5) 展示层 Cubit 工厂
Future<void> configureDependencies() async {
  // ---------- 0. 日志 ----------
  sl.registerLazySingleton<Logger>(() => Logger(printer: PrettyPrinter(methodCount: 0)));

  // ---------- 1. 存储 ----------
  await CacheBox.init();
  sl.registerLazySingleton<SecureStorage>(SecureStorage.new);
  final kv = await KvStorage.create();
  sl.registerLazySingleton<KvStorage>(() => kv);

  // ---------- 2. 平台服务 ----------
  sl.registerLazySingleton<ClipboardService>(ClipboardService.new);
  sl.registerLazySingleton<ShareService>(ShareService.new);
  sl.registerLazySingleton<HapticsService>(HapticsService.new);

  // ---------- 3. 网络：需要 AuthCubit 处理 401，但 AuthCubit 依赖 Repository，
  //    Repository 又依赖 Dio。我们用「延迟回调」解决循环：
  //    ErrorInterceptor 的 onUnauthorized 里通过 sl 实时读取 AuthCubit。
  sl.registerLazySingleton<AuthInterceptor>(() => AuthInterceptor(sl()));
  sl.registerLazySingleton<ErrorInterceptor>(
    () => ErrorInterceptor(
      onUnauthorized: () async {
        if (sl.isRegistered<AuthCubit>()) {
          await sl<AuthCubit>().markUnauthenticated();
        }
      },
    ),
  );
  sl.registerLazySingleton<ApiClient>(
    () => ApiClient.build(
      authInterceptor: sl<AuthInterceptor>(),
      errorInterceptor: sl<ErrorInterceptor>(),
    ),
  );
  sl.registerLazySingleton<Dio>(() => sl<ApiClient>().dio);

  // ---------- 4. 数据源 + Repository ----------
  sl.registerLazySingleton<AuthRemoteDataSource>(() => AuthRemoteDataSource(sl<Dio>()));
  sl.registerLazySingleton<AuthRepository>(
    () => AuthRepositoryImpl(sl<AuthRemoteDataSource>(), sl<SecureStorage>()),
  );

  sl.registerLazySingleton<SongsRemoteDataSource>(() => SongsRemoteDataSource(sl<Dio>()));
  sl.registerLazySingleton<SongsRepository>(
    () => SongsRepositoryImpl(sl<SongsRemoteDataSource>()),
  );

  sl.registerLazySingleton<PlaylistsRemoteDataSource>(() => PlaylistsRemoteDataSource(sl<Dio>()));
  sl.registerLazySingleton<PlaylistsRepository>(
    () => PlaylistsRepositoryImpl(sl<PlaylistsRemoteDataSource>()),
  );

  sl.registerLazySingleton<FavoritesRemoteDataSource>(() => FavoritesRemoteDataSource(sl<Dio>()));
  sl.registerLazySingleton<FavoritesRepository>(
    () => FavoritesRepositoryImpl(sl<FavoritesRemoteDataSource>()),
  );

  // ---------- 5. 全局单例 Cubit ----------
  sl.registerLazySingleton<AuthCubit>(() => AuthCubit(sl<AuthRepository>()));
  sl.registerLazySingleton<FavoritesCubit>(() => FavoritesCubit(sl<FavoritesRepository>()));
}
