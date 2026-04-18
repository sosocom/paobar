import 'package:equatable/equatable.dart';

class User extends Equatable {
  const User({
    required this.id,
    required this.username,
    this.avatar,
    this.points = 0,
    this.stats = const UserStats(),
  });

  final String id;
  final String username;
  final String? avatar;
  final int points;
  final UserStats stats;

  @override
  List<Object?> get props => [id, username, avatar, points, stats];
}

class UserStats extends Equatable {
  const UserStats({
    this.collected = 0,
    this.playlists = 0,
    this.practiceHours = 0,
  });

  final int collected;
  final int playlists;
  final int practiceHours;

  @override
  List<Object?> get props => [collected, playlists, practiceHours];
}
