import 'package:paobar/core/errors/error_mapper.dart';
import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/playlists/data/datasources/playlists_remote_datasource.dart';
import 'package:paobar/features/playlists/domain/entities/playlist.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

class PlaylistsRepositoryImpl implements PlaylistsRepository {
  PlaylistsRepositoryImpl(this._remote);
  final PlaylistsRemoteDataSource _remote;

  Future<Result<R>> _guard<R>(Future<R> Function() fn) async {
    try {
      return Result.success(await fn());
    } catch (e, st) {
      return Result.error(mapErrorToFailure(e, st));
    }
  }

  @override
  Future<Result<List<Playlist>>> fetchPlaylists({PlaylistType? type}) =>
      _guard(() async => (await _remote.getPlaylists(type: type)).map((e) => e.toEntity()).toList());

  @override
  Future<Result<Playlist>> fetchPlaylistById(String id) =>
      _guard(() async => (await _remote.getPlaylistById(id)).toEntity());

  @override
  Future<Result<List<Song>>> fetchPlaylistSongs(String id) =>
      _guard(() async => (await _remote.getPlaylistSongs(id)).map((e) => e.toEntity()).toList());

  @override
  Future<Result<Playlist>> createPlaylist(String name) =>
      _guard(() async => (await _remote.createPlaylist(name)).toEntity());

  @override
  Future<Result<Playlist>> rename(String id, String name) =>
      _guard(() async => (await _remote.renamePlaylist(id, name)).toEntity());

  @override
  Future<Result<void>> addSong(String playlistId, String songId) =>
      _guard(() => _remote.addSong(playlistId, songId));

  @override
  Future<Result<void>> removeSong(String playlistId, String songId) =>
      _guard(() => _remote.removeSong(playlistId, songId));

  @override
  Future<Result<void>> moveToTop(String playlistId, String songId) =>
      _guard(() => _remote.moveToTop(playlistId, songId));

  @override
  Future<Result<void>> moveToBottom(String playlistId, String songId) =>
      _guard(() => _remote.moveToBottom(playlistId, songId));

  @override
  Future<Result<void>> reorder(String playlistId, List<String> songIds) =>
      _guard(() => _remote.reorder(playlistId, songIds));
}
