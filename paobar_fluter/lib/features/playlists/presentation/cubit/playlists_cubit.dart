import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:paobar/features/playlists/domain/entities/playlist.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/playlists/presentation/cubit/playlists_state.dart';

class PlaylistsCubit extends Cubit<PlaylistsState> {
  PlaylistsCubit(this._repo) : super(const PlaylistsState());

  final PlaylistsRepository _repo;

  Future<void> load() async {
    emit(state.copyWith(loading: true, failure: null));
    final u = await _repo.fetchPlaylists(type: PlaylistType.user);
    final a = await _repo.fetchPlaylists(type: PlaylistType.ai);
    emit(state.copyWith(
      loading: false,
      userPlaylists: u.valueOrNull ?? state.userPlaylists,
      aiPlaylists: a.valueOrNull ?? state.aiPlaylists,
      failure: u.failureOrNull ?? a.failureOrNull,
    ));
  }

  Future<bool> create(String name) async {
    final r = await _repo.createPlaylist(name);
    return r.fold(
      (ok) {
        emit(state.copyWith(userPlaylists: [ok, ...state.userPlaylists]));
        return true;
      },
      (_) => false,
    );
  }
}
