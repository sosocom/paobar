import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_document.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';

class SongModel {
  SongModel({
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

  factory SongModel.fromJson(Map<String, dynamic> json) {
    final tabDocRaw = json['tabDocument'];
    final tabDoc = tabDocRaw is Map<String, dynamic>
        ? TabDocument.fromJson(tabDocRaw)
        : null;
    return SongModel(
      id: json['id'].toString(),
      title: json['title']?.toString() ?? '',
      artist: json['artist']?.toString() ?? '',
      originalUrl: json['originalUrl']?.toString(),
      lyrics: json['lyrics']?.toString(),
      tabDocument: tabDoc,
      tabImageUrl: json['tabImageUrl']?.toString(),
      meta: json['meta']?.toString(),
      difficulty: json['difficulty']?.toString(),
      tuning: json['tuning']?.toString(),
      capo: (json['capo'] as num?)?.toInt(),
      playKey: json['playKey']?.toString(),
      originalKey: json['originalKey']?.toString(),
      beat: json['beat']?.toString(),
      viewCount: (json['viewCount'] as num?)?.toInt() ?? 0,
      favoriteCount: (json['favoriteCount'] as num?)?.toInt() ?? 0,
    );
  }

  Song toEntity() => Song(
        id: id,
        title: title,
        artist: artist,
        originalUrl: originalUrl,
        lyrics: lyrics,
        tabDocument: tabDocument,
        tabImageUrl: tabImageUrl,
        meta: meta,
        difficulty: difficulty,
        tuning: tuning,
        capo: capo,
        playKey: playKey,
        originalKey: originalKey,
        beat: beat,
        viewCount: viewCount,
        favoriteCount: favoriteCount,
      );
}
