import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

abstract class SongsRepository {
  Future<Result<List<Song>>> fetchSongs(SongQuery query);
  Future<Result<Song>> fetchSongById(String id);
}
