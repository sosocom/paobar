import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/router/routes.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/core/widgets/empty_view.dart';
import 'package:paobar/core/widgets/failure_view.dart';
import 'package:paobar/core/widgets/loading_view.dart';
import 'package:paobar/core/widgets/song_tile.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/playlists/presentation/cubit/playlist_detail_cubit.dart';
import 'package:paobar/features/playlists/presentation/cubit/playlist_detail_state.dart';

class PlaylistDetailPage extends StatelessWidget {
  const PlaylistDetailPage({required this.playlistId, super.key});

  final String playlistId;

  @override
  Widget build(BuildContext context) {
    return BlocProvider<PlaylistDetailCubit>(
      create: (_) =>
          PlaylistDetailCubit(sl<PlaylistsRepository>(), playlistId)..load(),
      child: Scaffold(
        appBar: AppBar(
          title: BlocBuilder<PlaylistDetailCubit, PlaylistDetailState>(
            builder: (_, s) => Text(s.playlist?.name ?? '歌单'),
          ),
        ),
        body: BlocBuilder<PlaylistDetailCubit, PlaylistDetailState>(
          builder: (context, state) {
            if (state.loading && state.songs.isEmpty) {
              return const LoadingView();
            }
            if (state.failure != null && state.songs.isEmpty) {
              return FailureView(
                failure: state.failure!,
                onRetry: context.read<PlaylistDetailCubit>().load,
              );
            }
            if (state.songs.isEmpty) {
              return const EmptyView(message: '歌单里还没有歌');
            }
            return ReorderableListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
              itemCount: state.songs.length,
              onReorder: context.read<PlaylistDetailCubit>().reorder,
              itemBuilder: (context, i) {
                final s = state.songs[i];
                return Padding(
                  key: ValueKey(s.id),
                  padding: const EdgeInsets.symmetric(vertical: 2),
                  child: SongTile(
                    song: s,
                    onTap: () => context.push(
                      AppRoutes.nowPlayingOf(s.id, playlistId: playlistId),
                    ),
                    trailing: PopupMenuButton<String>(
                      icon: const Icon(LucideIcons.moreVertical, size: 18),
                      onSelected: (v) {
                        final cubit = context.read<PlaylistDetailCubit>();
                        switch (v) {
                          case 'top':
                            cubit.moveToTop(s.id);
                          case 'bottom':
                            cubit.moveToBottom(s.id);
                          case 'remove':
                            cubit.remove(s.id);
                        }
                      },
                      itemBuilder: (_) => const [
                        PopupMenuItem(value: 'top', child: Text('置顶')),
                        PopupMenuItem(value: 'bottom', child: Text('置底')),
                        PopupMenuItem(value: 'remove', child: Text('移除')),
                      ],
                    ),
                  ),
                );
              },
            );
          },
        ),
      ),
    );
  }
}
