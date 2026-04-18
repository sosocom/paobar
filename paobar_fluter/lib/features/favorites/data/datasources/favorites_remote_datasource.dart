import 'package:dio/dio.dart';
import 'package:paobar/features/songs/data/models/song_model.dart';

class FavoritesRemoteDataSource {
  FavoritesRemoteDataSource(this._dio);
  final Dio _dio;

  Future<List<SongModel>> getFavorites() async {
    final res = await _dio.get<List<dynamic>>('/api/user/favorites');
    return (res.data ?? <dynamic>[])
        .whereType<Map<String, dynamic>>()
        .map(SongModel.fromJson)
        .toList();
  }

  Future<List<String>> getFavoriteIds() async {
    final res = await _dio.get<List<dynamic>>('/api/user/favorite-ids');
    return (res.data ?? <dynamic>[]).map((e) => e.toString()).toList();
  }

  Future<void> favorite(String songId) async {
    await _dio.post<void>('/api/user/favorites', data: {'songId': songId});
  }

  Future<void> unfavorite(String songId) async {
    await _dio.delete<void>('/api/user/favorites/$songId');
  }
}
