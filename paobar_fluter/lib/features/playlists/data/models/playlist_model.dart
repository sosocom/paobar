import 'package:paobar/features/playlists/domain/entities/playlist.dart';

class PlaylistModel {
  const PlaylistModel({
    required this.id,
    required this.name,
    this.coverUrl,
    this.gradient,
    this.songCount = 0,
    this.songs = const <String>[],
    this.type = 'user',
    this.chordProgression,
  });

  final String id;
  final String name;
  final String? coverUrl;
  final List<String>? gradient;
  final int songCount;
  final List<String> songs;
  final String type;
  final String? chordProgression;

  factory PlaylistModel.fromJson(Map<String, dynamic> json) {
    final rawSongs = json['songs'];
    return PlaylistModel(
      id: json['id'].toString(),
      name: json['name']?.toString() ?? '',
      coverUrl: json['coverUrl']?.toString(),
      gradient: (json['gradient'] as List<dynamic>?)?.map((e) => e.toString()).toList(),
      songCount: (json['songCount'] as num?)?.toInt() ?? 0,
      songs: (rawSongs is List)
          ? rawSongs.map((e) => e.toString()).toList()
          : const <String>[],
      type: json['type']?.toString() ?? 'user',
      chordProgression: json['chordProgression']?.toString(),
    );
  }

  Playlist toEntity() => Playlist(
        id: id,
        name: name,
        coverUrl: coverUrl,
        gradient: gradient,
        songCount: songCount,
        songIds: songs,
        type: PlaylistType.fromString(type),
        chordProgression: chordProgression,
      );
}
