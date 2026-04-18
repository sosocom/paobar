import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';
import 'package:paobar/features/songs/domain/repositories/songs_repository.dart';
import 'package:paobar/features/songs/presentation/cubit/songs_list_state.dart';

class SongsListCubit extends Cubit<SongsListState> {
  SongsListCubit(this._repo) : super(const SongsListState());

  final SongsRepository _repo;

  Timer? _debounce;

  Future<void> load() async {
    emit(state.copyWith(loading: true, failure: null));
    final res = await _repo.fetchSongs(SongQuery(
      genre: state.genre == '全部' ? null : state.genre,
      search: state.search.isEmpty ? null : state.search,
    ));
    res.fold(
      (list) => emit(state.copyWith(loading: false, songs: list)),
      (f) => emit(state.copyWith(loading: false, failure: f)),
    );
  }

  void setGenre(String genre) {
    emit(state.copyWith(genre: genre));
    load();
  }

  void setSearch(String search) {
    emit(state.copyWith(search: search));
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 300), load);
  }

  @override
  Future<void> close() {
    _debounce?.cancel();
    return super.close();
  }
}
