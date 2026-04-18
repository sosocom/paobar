import 'package:paobar/core/errors/error_mapper.dart';
import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/songs/data/datasources/songs_remote_datasource.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';
import 'package:paobar/features/songs/domain/repositories/songs_repository.dart';

class SongsRepositoryImpl implements SongsRepository {
  SongsRepositoryImpl(this._remote);

  final SongsRemoteDataSource _remote;

  @override
  Future<Result<List<Song>>> fetchSongs(SongQuery query) async {
    try {
      final list = await _remote.getSongs(query);
      return Result.success(list.map((e) => e.toEntity()).toList());
    } catch (e, st) {
      return Result.error(mapErrorToFailure(e, st));
    }
  }

  @override
  Future<Result<Song>> fetchSongById(String id) async {
    try {
      final song = await _remote.getSongById(id);
      return Result.success(song.toEntity());
    } catch (e, st) {
      return Result.error(mapErrorToFailure(e, st));
    }
  }
}
