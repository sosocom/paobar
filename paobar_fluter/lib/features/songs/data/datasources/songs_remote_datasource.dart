import 'package:dio/dio.dart';
import 'package:paobar/features/songs/data/models/song_model.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

/// 对接 /api/songs（GET list / by id / current）。
class SongsRemoteDataSource {
  SongsRemoteDataSource(this._dio);

  final Dio _dio;

  Future<List<SongModel>> getSongs(SongQuery query) async {
    final params = <String, dynamic>{};
    if (query.genre != null && query.genre != '全部') params['genre'] = query.genre;
    if (query.search != null && query.search!.isNotEmpty) params['search'] = query.search;
    if (query.page != null) params['page'] = query.page;
    if (query.pageSize != null) params['pageSize'] = query.pageSize;

    final res = await _dio.get<List<dynamic>>('/api/songs', queryParameters: params);
    return (res.data ?? <dynamic>[])
        .whereType<Map<String, dynamic>>()
        .map(SongModel.fromJson)
        .toList();
  }

  Future<SongModel> getSongById(String id) async {
    final res = await _dio.get<Map<String, dynamic>>('/api/songs/$id');
    return SongModel.fromJson(res.data!);
  }

  Future<List<SongModel>> getCurrentPlaylist() async {
    final res = await _dio.get<List<dynamic>>('/api/songs/current');
    return (res.data ?? <dynamic>[])
        .whereType<Map<String, dynamic>>()
        .map(SongModel.fromJson)
        .toList();
  }
}
