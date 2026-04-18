import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/playlists/domain/entities/playlist.dart';

part 'playlists_state.freezed.dart';

@freezed
class PlaylistsState with _$PlaylistsState {
  const factory PlaylistsState({
    @Default(false) bool loading,
    @Default(<Playlist>[]) List<Playlist> userPlaylists,
    @Default(<Playlist>[]) List<Playlist> aiPlaylists,
    Failure? failure,
  }) = _PlaylistsState;
}
