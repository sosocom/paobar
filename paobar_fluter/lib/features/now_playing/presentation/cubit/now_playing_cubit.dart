import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:paobar/features/now_playing/presentation/cubit/now_playing_state.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/songs/domain/repositories/songs_repository.dart';

/// 同时接管"单首歌"和"来自歌单的序列"两种场景：
/// - playlistId 非空 → 拉歌单全体并定位 songId
/// - playlistId 空 → 只拉 songId 一首
///
/// 注意：`document` 直接从 `Song.tabDocument` 取用（后端爬虫入库时已规范化为结构化 JSON），
/// 前端不再做 HTML 解析。
class NowPlayingCubit extends Cubit<NowPlayingState> {
  NowPlayingCubit({
    required SongsRepository songsRepo,
    required PlaylistsRepository playlistsRepo,
  })  : _songsRepo = songsRepo,
        _playlistsRepo = playlistsRepo,
        super(const NowPlayingState());

  final SongsRepository _songsRepo;
  final PlaylistsRepository _playlistsRepo;

  Future<void> load({required String songId, String? playlistId}) async {
    emit(state.copyWith(loading: true, failure: null));

    if (playlistId != null && playlistId.isNotEmpty) {
      // 歌单列表接口可能不带 tabDocument（仅为节省带宽），因此定位到当前歌后再单独拉详情。
      final playlistRes = await _playlistsRepo.fetchPlaylistSongs(playlistId);
      await playlistRes.fold(
        (songs) async {
          final idx = songs.indexWhere((s) => s.id == songId);
          final safeIdx = idx < 0 ? 0 : idx;
          final currentId = songs.isEmpty ? songId : songs[safeIdx].id;
          final detailRes = await _songsRepo.fetchSongById(currentId);
          detailRes.fold(
            (song) {
              final playlist = [
                for (var i = 0; i < songs.length; i++)
                  i == safeIdx ? song : songs[i],
              ];
              emit(state.copyWith(
                loading: false,
                playlist: playlist,
                currentIndex: safeIdx,
                document: song.tabDocument,
              ));
            },
            (f) => emit(state.copyWith(loading: false, failure: f)),
          );
        },
        (f) async => emit(state.copyWith(loading: false, failure: f)),
      );
      return;
    }

    final res = await _songsRepo.fetchSongById(songId);
    res.fold(
      (song) => emit(state.copyWith(
        loading: false,
        playlist: [song],
        currentIndex: 0,
        document: song.tabDocument,
      )),
      (f) => emit(state.copyWith(loading: false, failure: f)),
    );
  }

  /// 切换到歌单里的某一首：若该首尚未拉详情（tabDocument 为空），则补一次详情请求。
  Future<void> _switchTo(int i) async {
    if (i < 0 || i >= state.playlist.length) return;
    final song = state.playlist[i];
    if (song.tabDocument != null) {
      emit(state.copyWith(currentIndex: i, document: song.tabDocument));
      return;
    }
    emit(state.copyWith(currentIndex: i, document: null));
    final res = await _songsRepo.fetchSongById(song.id);
    res.fold(
      (detail) {
        final updated = [
          for (var k = 0; k < state.playlist.length; k++)
            k == i ? detail : state.playlist[k],
        ];
        emit(state.copyWith(playlist: updated, document: detail.tabDocument));
      },
      (f) => emit(state.copyWith(failure: f)),
    );
  }

  void prev() {
    if (!state.hasPrev) return;
    _switchTo(state.currentIndex - 1);
  }

  void next() {
    if (!state.hasNext) return;
    _switchTo(state.currentIndex + 1);
  }

  void jumpTo(int index) => _switchTo(index);
}
