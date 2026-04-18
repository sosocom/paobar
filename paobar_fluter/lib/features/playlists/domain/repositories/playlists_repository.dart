import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/playlists/domain/entities/playlist.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

abstract class PlaylistsRepository {
  Future<Result<List<Playlist>>> fetchPlaylists({PlaylistType? type});
  Future<Result<Playlist>> fetchPlaylistById(String id);
  Future<Result<List<Song>>> fetchPlaylistSongs(String id);
  Future<Result<Playlist>> createPlaylist(String name);
  Future<Result<Playlist>> rename(String id, String name);
  Future<Result<void>> addSong(String playlistId, String songId);
  Future<Result<void>> removeSong(String playlistId, String songId);
  Future<Result<void>> moveToTop(String playlistId, String songId);
  Future<Result<void>> moveToBottom(String playlistId, String songId);
  Future<Result<void>> reorder(String playlistId, List<String> songIds);
}
