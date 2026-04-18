import 'package:paobar/core/errors/error_mapper.dart';
import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/favorites/data/datasources/favorites_remote_datasource.dart';
import 'package:paobar/features/favorites/domain/repositories/favorites_repository.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

class FavoritesRepositoryImpl implements FavoritesRepository {
  FavoritesRepositoryImpl(this._remote);
  final FavoritesRemoteDataSource _remote;

  Future<Result<R>> _guard<R>(Future<R> Function() fn) async {
    try {
      return Result.success(await fn());
    } catch (e, st) {
      return Result.error(mapErrorToFailure(e, st));
    }
  }

  @override
  Future<Result<List<Song>>> fetchFavorites() =>
      _guard(() async => (await _remote.getFavorites()).map((e) => e.toEntity()).toList());

  @override
  Future<Result<List<String>>> fetchFavoriteIds() =>
      _guard(() => _remote.getFavoriteIds());

  @override
  Future<Result<void>> favorite(String songId) => _guard(() => _remote.favorite(songId));

  @override
  Future<Result<void>> unfavorite(String songId) => _guard(() => _remote.unfavorite(songId));
}
