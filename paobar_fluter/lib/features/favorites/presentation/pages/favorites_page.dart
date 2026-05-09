import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:paobar/app/router/routes.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/core/widgets/empty_view.dart';
import 'package:paobar/core/widgets/failure_view.dart';
import 'package:paobar/core/widgets/loading_view.dart';
import 'package:paobar/core/widgets/song_tile.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/favorites/domain/repositories/favorites_repository.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

class FavoritesPage extends StatelessWidget {
  const FavoritesPage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider<_FavoritesListCubit>(
      create: (_) => _FavoritesListCubit(sl<FavoritesRepository>())..load(),
      child: Scaffold(
        appBar: AppBar(title: const Text('我的收藏')),
        body: BlocBuilder<_FavoritesListCubit, _FavoritesListState>(
          builder: (context, state) {
            if (state.loading && state.songs.isEmpty) return const LoadingView();
            if (state.failure != null && state.songs.isEmpty) {
              return FailureView(
                failure: state.failure!,
                onRetry: context.read<_FavoritesListCubit>().load,
              );
            }
            if (state.songs.isEmpty) {
              return const EmptyView(message: '还没有收藏');
            }
            return RefreshIndicator(
              onRefresh: context.read<_FavoritesListCubit>().load,
              child: ListView.separated(
                padding: const EdgeInsets.all(8),
                itemCount: state.songs.length,
                separatorBuilder: (_, __) => const SizedBox(height: 4),
                itemBuilder: (_, i) {
                  final s = state.songs[i];
                  return SongTile(
                    song: s,
                    onTap: () => context.push(AppRoutes.nowPlayingOf(s.id)),
                  );
                },
              ),
            );
          },
        ),
      ),
    );
  }
}

class _FavoritesListState {
  const _FavoritesListState({
    this.loading = false,
    this.songs = const <Song>[],
    this.failure,
  });
  final bool loading;
  final List<Song> songs;
  final Failure? failure;

  _FavoritesListState copyWith({
    bool? loading,
    List<Song>? songs,
    Failure? failure,
  }) =>
      _FavoritesListState(
        loading: loading ?? this.loading,
        songs: songs ?? this.songs,
        failure: failure,
      );
}

class _FavoritesListCubit extends Cubit<_FavoritesListState> {
  _FavoritesListCubit(this._repo) : super(const _FavoritesListState());
  final FavoritesRepository _repo;

  Future<void> load() async {
    emit(state.copyWith(loading: true));
    final res = await _repo.fetchFavorites();
    res.fold(
      (s) => emit(state.copyWith(loading: false, songs: s)),
      (f) => emit(state.copyWith(loading: false, failure: f)),
    );
  }
}
