import 'package:equatable/equatable.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_document.dart';

/// 对齐后端 `SongDTO`。注意：原来的 HTML 字段 `tabContent` 已被移除，改成结构化的
/// [tabDocument]（爬虫入库时规范化生成），前端不再做 HTML 解析。
class Song extends Equatable {
  const Song({
    required this.id,
    required this.title,
    required this.artist,
    this.originalUrl,
    this.lyrics,
    this.tabDocument,
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
  final TabDocument? tabDocument;
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
        tabDocument,
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
