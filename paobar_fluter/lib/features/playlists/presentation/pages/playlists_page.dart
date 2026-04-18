import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/router/routes.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/core/widgets/empty_view.dart';
import 'package:paobar/core/widgets/error_view.dart';
import 'package:paobar/core/widgets/loading_view.dart';
import 'package:paobar/core/widgets/playlist_tile.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/playlists/presentation/cubit/playlists_cubit.dart';
import 'package:paobar/features/playlists/presentation/cubit/playlists_state.dart';

class PlaylistsPage extends StatelessWidget {
  const PlaylistsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider<PlaylistsCubit>(
      create: (_) => PlaylistsCubit(sl<PlaylistsRepository>())..load(),
      child: Scaffold(
        appBar: AppBar(
          title: const Text('歌单'),
          actions: [
            IconButton(
              icon: const Icon(LucideIcons.plus),
              onPressed: () => _showCreateDialog(context),
            ),
          ],
        ),
        body: BlocBuilder<PlaylistsCubit, PlaylistsState>(
          builder: (context, state) {
            if (state.loading && state.userPlaylists.isEmpty && state.aiPlaylists.isEmpty) {
              return const LoadingView();
            }
            if (state.failure != null && state.userPlaylists.isEmpty) {
              return ErrorView(
                message: state.failure!.displayMessage,
                onRetry: context.read<PlaylistsCubit>().load,
              );
            }
            if (state.userPlaylists.isEmpty && state.aiPlaylists.isEmpty) {
              return const EmptyView(message: '还没有歌单，点右上"+"新建');
            }
            return RefreshIndicator(
              onRefresh: context.read<PlaylistsCubit>().load,
              child: ListView(
                padding: const EdgeInsets.all(8),
                children: [
                  if (state.userPlaylists.isNotEmpty) ...[
                    const _SectionHeader('我的歌单'),
                    for (final p in state.userPlaylists)
                      PlaylistTile(
                        playlist: p,
                        onTap: () => context.push(AppRoutes.playlistDetailOf(p.id)),
                      ),
                  ],
                  if (state.aiPlaylists.isNotEmpty) ...[
                    const _SectionHeader('AI 智能歌单'),
                    for (final p in state.aiPlaylists)
                      PlaylistTile(
                        playlist: p,
                        onTap: () => context.push(AppRoutes.playlistDetailOf(p.id)),
                      ),
                  ],
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  void _showCreateDialog(BuildContext context) {
    final controller = TextEditingController();
    final cubit = context.read<PlaylistsCubit>();
    showDialog<void>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('新建歌单'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(hintText: '歌单名称'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () async {
              final name = controller.text.trim();
              if (name.isEmpty) return;
              final ok = await cubit.create(name);
              if (ok && context.mounted) Navigator.of(context).pop();
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
      child: Text(
        text,
        style: const TextStyle(
          color: AppColors.textSecondary,
          fontSize: 13,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}
