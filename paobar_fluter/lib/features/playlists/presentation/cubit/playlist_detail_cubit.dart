import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/playlists/presentation/cubit/playlist_detail_state.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

class PlaylistDetailCubit extends Cubit<PlaylistDetailState> {
  PlaylistDetailCubit(this._repo, this.playlistId)
      : super(const PlaylistDetailState());

  final PlaylistsRepository _repo;
  final String playlistId;

  Future<void> load() async {
    emit(state.copyWith(loading: true, failure: null));
    final pl = await _repo.fetchPlaylistById(playlistId);
    final songs = await _repo.fetchPlaylistSongs(playlistId);
    emit(state.copyWith(
      loading: false,
      playlist: pl.valueOrNull ?? state.playlist,
      songs: songs.valueOrNull ?? state.songs,
      failure: pl.failureOrNull ?? songs.failureOrNull,
    ));
  }

  /// 乐观更新：先排好本地，再发请求；失败回滚并加载。
  Future<void> reorder(int oldIndex, int newIndex) async {
    if (newIndex > oldIndex) newIndex -= 1;
    final list = List<Song>.from(state.songs);
    final moved = list.removeAt(oldIndex);
    list.insert(newIndex, moved);
    emit(state.copyWith(songs: list));

    final res = await _repo.reorder(playlistId, list.map((s) => s.id).toList());
    if (res.isError) await load();
  }

  Future<void> remove(String songId) async {
    final res = await _repo.removeSong(playlistId, songId);
    if (res.isSuccess) {
      emit(state.copyWith(
        songs: state.songs.where((s) => s.id != songId).toList(),
      ));
    }
  }

  Future<void> moveToTop(String songId) async {
    final res = await _repo.moveToTop(playlistId, songId);
    if (res.isSuccess) await load();
  }

  Future<void> moveToBottom(String songId) async {
    final res = await _repo.moveToBottom(playlistId, songId);
    if (res.isSuccess) await load();
  }
}
