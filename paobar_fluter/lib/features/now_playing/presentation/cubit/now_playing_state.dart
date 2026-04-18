import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_document.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

part 'now_playing_state.freezed.dart';

@freezed
class NowPlayingState with _$NowPlayingState {
  const factory NowPlayingState({
    @Default(false) bool loading,
    @Default(<Song>[]) List<Song> playlist,
    @Default(0) int currentIndex,
    TabDocument? document,
    Failure? failure,
  }) = _NowPlayingState;
}

extension NowPlayingStateX on NowPlayingState {
  Song? get currentSong =>
      playlist.isEmpty ? null : playlist[currentIndex.clamp(0, playlist.length - 1)];

  String? get prevSongName {
    if (currentIndex <= 0) return null;
    return playlist[currentIndex - 1].title;
  }

  String? get nextSongName {
    if (currentIndex >= playlist.length - 1) return null;
    return playlist[currentIndex + 1].title;
  }

  bool get hasPrev => currentIndex > 0;
  bool get hasNext => currentIndex < playlist.length - 1;
}
