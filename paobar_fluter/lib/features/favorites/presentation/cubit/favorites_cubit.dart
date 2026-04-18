import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:paobar/features/favorites/domain/repositories/favorites_repository.dart';

/// 全局收藏 ID 集合。被 NowPlayingPage / IndexPage 等页面共同订阅。
/// 使用 `Set<String>` 作为 State 足够轻量，不需要 Freezed。
class FavoritesCubit extends Cubit<Set<String>> {
  FavoritesCubit(this._repo) : super(const <String>{});

  final FavoritesRepository _repo;

  bool _loaded = false;

  Future<void> ensureLoaded() async {
    if (_loaded) return;
    _loaded = true;
    await refresh();
  }

  Future<void> refresh() async {
    final res = await _repo.fetchFavoriteIds();
    res.fold(
      (ids) => emit(ids.toSet()),
      (_) => null,
    );
  }

  Future<void> toggle(String songId) async {
    if (state.contains(songId)) {
      emit({...state}..remove(songId));
      final res = await _repo.unfavorite(songId);
      if (res.isError) emit({...state, songId}); // 回滚
    } else {
      emit({...state, songId});
      final res = await _repo.favorite(songId);
      if (res.isError) emit({...state}..remove(songId));
    }
  }

  void reset() {
    _loaded = false;
    emit(<String>{});
  }
}
