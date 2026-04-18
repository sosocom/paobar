import 'package:dio/dio.dart';
import 'package:paobar/features/playlists/data/models/playlist_model.dart';
import 'package:paobar/features/playlists/domain/entities/playlist.dart';
import 'package:paobar/features/songs/data/models/song_model.dart';

class PlaylistsRemoteDataSource {
  PlaylistsRemoteDataSource(this._dio);
  final Dio _dio;

  Future<List<PlaylistModel>> getPlaylists({PlaylistType? type}) async {
    final res = await _dio.get<List<dynamic>>(
      '/api/playlists',
      queryParameters: type == null ? null : {'type': type.apiValue},
    );
    return (res.data ?? <dynamic>[])
        .whereType<Map<String, dynamic>>()
        .map(PlaylistModel.fromJson)
        .toList();
  }

  Future<PlaylistModel> getPlaylistById(String id) async {
    final res = await _dio.get<Map<String, dynamic>>('/api/playlists/$id');
    return PlaylistModel.fromJson(res.data!);
  }

  Future<List<SongModel>> getPlaylistSongs(String id) async {
    final res = await _dio.get<List<dynamic>>('/api/playlists/$id/songs');
    return (res.data ?? <dynamic>[])
        .whereType<Map<String, dynamic>>()
        .map(SongModel.fromJson)
        .toList();
  }

  Future<PlaylistModel> createPlaylist(String name) async {
    final res = await _dio.post<Map<String, dynamic>>(
      '/api/playlists',
      data: {'name': name},
    );
    return PlaylistModel.fromJson(res.data!);
  }

  Future<PlaylistModel> renamePlaylist(String id, String name) async {
    final res = await _dio.put<Map<String, dynamic>>(
      '/api/playlists/$id',
      data: {'name': name},
    );
    return PlaylistModel.fromJson(res.data!);
  }

  Future<void> addSong(String playlistId, String songId) {
    return _dio.post<void>(
      '/api/playlists/$playlistId/songs',
      data: {'songId': songId},
    );
  }

  Future<void> removeSong(String playlistId, String songId) {
    return _dio.delete<void>('/api/playlists/$playlistId/songs/$songId');
  }

  Future<void> moveToTop(String playlistId, String songId) {
    return _dio.put<void>('/api/playlists/$playlistId/songs/$songId/move-to-top');
  }

  Future<void> moveToBottom(String playlistId, String songId) {
    return _dio.put<void>(
      '/api/playlists/$playlistId/songs/$songId/move-to-bottom',
    );
  }

  Future<void> reorder(String playlistId, List<String> songIds) {
    return _dio.put<void>(
      '/api/playlists/$playlistId/songs/reorder',
      data: songIds,
    );
  }
}
