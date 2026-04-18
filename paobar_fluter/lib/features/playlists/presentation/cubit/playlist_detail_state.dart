import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/playlists/domain/entities/playlist.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

part 'playlist_detail_state.freezed.dart';

@freezed
class PlaylistDetailState with _$PlaylistDetailState {
  const factory PlaylistDetailState({
    @Default(false) bool loading,
    Playlist? playlist,
    @Default(<Song>[]) List<Song> songs,
    Failure? failure,
  }) = _PlaylistDetailState;
}
