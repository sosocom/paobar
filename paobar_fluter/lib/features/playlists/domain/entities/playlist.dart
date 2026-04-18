import 'package:equatable/equatable.dart';

/// 对齐 web 的 Playlist interface。
class Playlist extends Equatable {
  const Playlist({
    required this.id,
    required this.name,
    this.coverUrl,
    this.gradient,
    this.songCount = 0,
    this.songIds = const <String>[],
    this.type = PlaylistType.user,
    this.chordProgression,
  });

  final String id;
  final String name;
  final String? coverUrl;
  final List<String>? gradient;
  final int songCount;
  final List<String> songIds;
  final PlaylistType type;
  final String? chordProgression;

  @override
  List<Object?> get props =>
      [id, name, coverUrl, gradient, songCount, songIds, type, chordProgression];
}

enum PlaylistType {
  user,
  ai;

  static PlaylistType fromString(String? s) {
    switch (s) {
      case 'ai':
      case 'AI':
        return PlaylistType.ai;
      default:
        return PlaylistType.user;
    }
  }

  String get apiValue => name;
}
