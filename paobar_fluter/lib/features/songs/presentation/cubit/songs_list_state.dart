import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

part 'songs_list_state.freezed.dart';

@freezed
class SongsListState with _$SongsListState {
  const factory SongsListState({
    @Default(false) bool loading,
    @Default(<Song>[]) List<Song> songs,
    @Default('全部') String genre,
    @Default('') String search,
    Failure? failure,
  }) = _SongsListState;
}
