import 'package:equatable/equatable.dart';

/// 对齐 `paobar_web/src/types/index.ts` 的 Song interface。
class Song extends Equatable {
  const Song({
    required this.id,
    required this.title,
    required this.artist,
    this.originalUrl,
    this.lyrics,
    this.tabContent,
    this.tabImageUrl,
    this.meta,
    this.difficulty,
    this.tuning,
    this.capo,
    this.playKey,
    this.originalKey,
    this.beat,
    this.viewCount = 0,
    this.favoriteCount = 0,
  });

  final String id;
  final String title;
  final String artist;
  final String? originalUrl;
  final String? lyrics;
  final String? tabContent;
  final String? tabImageUrl;
  final String? meta;
  final String? difficulty;
  final String? tuning;
  final int? capo;
  final String? playKey;
  final String? originalKey;
  final String? beat;
  final int viewCount;
  final int favoriteCount;

  @override
  List<Object?> get props => [
        id,
        title,
        artist,
        originalUrl,
        lyrics,
        tabContent,
        tabImageUrl,
        meta,
        difficulty,
        tuning,
        capo,
        playKey,
        originalKey,
        beat,
        viewCount,
        favoriteCount,
      ];
}

class SongQuery extends Equatable {
  const SongQuery({
    this.genre,
    this.search,
    this.page,
    this.pageSize,
  });

  final String? genre;
  final String? search;
  final int? page;
  final int? pageSize;

  SongQuery copyWith({
    String? genre,
    String? search,
    int? page,
    int? pageSize,
  }) {
    return SongQuery(
      genre: genre ?? this.genre,
      search: search ?? this.search,
      page: page ?? this.page,
      pageSize: pageSize ?? this.pageSize,
    );
  }

  @override
  List<Object?> get props => [genre, search, page, pageSize];
}
