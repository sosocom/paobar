import 'package:paobar/features/auth/domain/entities/user.dart';

class UserModel {
  const UserModel({
    required this.id,
    required this.username,
    this.avatar,
    this.points = 0,
    this.stats,
  });

  final String id;
  final String username;
  final String? avatar;
  final int points;
  final UserStatsModel? stats;

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'].toString(),
      username: json['username']?.toString() ?? '',
      avatar: json['avatar']?.toString(),
      points: (json['points'] as num?)?.toInt() ?? 0,
      stats: json['stats'] == null
          ? null
          : UserStatsModel.fromJson(json['stats'] as Map<String, dynamic>),
    );
  }

  User toEntity() => User(
        id: id,
        username: username,
        avatar: avatar,
        points: points,
        stats: stats?.toEntity() ?? const UserStats(),
      );
}

class UserStatsModel {
  const UserStatsModel({
    this.collected = 0,
    this.playlists = 0,
    this.practiceHours = 0,
  });

  final int collected;
  final int playlists;
  final int practiceHours;

  factory UserStatsModel.fromJson(Map<String, dynamic> json) {
    return UserStatsModel(
      collected: (json['collected'] as num?)?.toInt() ?? 0,
      playlists: (json['playlists'] as num?)?.toInt() ?? 0,
      practiceHours: (json['practiceHours'] as num?)?.toInt() ?? 0,
    );
  }

  UserStats toEntity() => UserStats(
        collected: collected,
        playlists: playlists,
        practiceHours: practiceHours,
      );
}
