import 'package:paobar/core/result/result.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

abstract class FavoritesRepository {
  Future<Result<List<Song>>> fetchFavorites();
  Future<Result<List<String>>> fetchFavoriteIds();
  Future<Result<void>> favorite(String songId);
  Future<Result<void>> unfavorite(String songId);
}
