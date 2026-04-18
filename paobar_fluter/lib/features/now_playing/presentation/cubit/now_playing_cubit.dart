import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:paobar/features/now_playing/presentation/cubit/now_playing_state.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_document.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_html_parser.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/songs/domain/repositories/songs_repository.dart';

/// 同时接管"单首歌"和"来自歌单的序列"两种场景：
/// - playlistId 非空 → 拉歌单全体并定位 songId
/// - playlistId 空 → 只拉 songId 一首
class NowPlayingCubit extends Cubit<NowPlayingState> {
  NowPlayingCubit({
    required SongsRepository songsRepo,
    required PlaylistsRepository playlistsRepo,
  })  : _songsRepo = songsRepo,
        _playlistsRepo = playlistsRepo,
        super(const NowPlayingState());

  final SongsRepository _songsRepo;
  final PlaylistsRepository _playlistsRepo;

  static const _parser = TabHtmlParser();

  Future<void> load({required String songId, String? playlistId}) async {
    emit(state.copyWith(loading: true, failure: null));

    if (playlistId != null && playlistId.isNotEmpty) {
      final res = await _playlistsRepo.fetchPlaylistSongs(playlistId);
      res.fold(
        (songs) {
          final idx = songs.indexWhere((s) => s.id == songId).clamp(0, songs.length - 1);
          emit(state.copyWith(
            loading: false,
            playlist: songs,
            currentIndex: idx < 0 ? 0 : idx,
            document: _parseTab(idx < 0 ? null : songs[idx].tabContent),
          ));
        },
        (f) => emit(state.copyWith(loading: false, failure: f)),
      );
      return;
    }

    final res = await _songsRepo.fetchSongById(songId);
    res.fold(
      (song) => emit(state.copyWith(
        loading: false,
        playlist: [song],
        currentIndex: 0,
        document: _parseTab(song.tabContent),
      )),
      (f) => emit(state.copyWith(loading: false, failure: f)),
    );
  }

  void prev() {
    if (!state.hasPrev) return;
    final i = state.currentIndex - 1;
    emit(state.copyWith(
      currentIndex: i,
      document: _parseTab(state.playlist[i].tabContent),
    ));
  }

  void next() {
    if (!state.hasNext) return;
    final i = state.currentIndex + 1;
    emit(state.copyWith(
      currentIndex: i,
      document: _parseTab(state.playlist[i].tabContent),
    ));
  }

  void jumpTo(int index) {
    if (index < 0 || index >= state.playlist.length) return;
    emit(state.copyWith(
      currentIndex: index,
      document: _parseTab(state.playlist[index].tabContent),
    ));
  }

  TabDocument? _parseTab(String? html) {
    if (html == null || html.isEmpty) return null;
    return _parser.parse(html);
  }
}
