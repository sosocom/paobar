import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/core/widgets/empty_view.dart';
import 'package:paobar/core/widgets/error_view.dart';
import 'package:paobar/core/widgets/loading_view.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/features/favorites/presentation/cubit/favorites_cubit.dart';
import 'package:paobar/features/now_playing/presentation/cubit/now_playing_cubit.dart';
import 'package:paobar/features/now_playing/presentation/cubit/now_playing_state.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/chord_diagram_sheet.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_scale_controller.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_view.dart';
import 'package:paobar/features/playlists/domain/repositories/playlists_repository.dart';
import 'package:paobar/features/songs/domain/entities/song.dart';
import 'package:paobar/features/songs/domain/repositories/songs_repository.dart';

class NowPlayingPage extends StatelessWidget {
  const NowPlayingPage({required this.songId, this.playlistId, super.key});

  final String songId;
  final String? playlistId;

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider<NowPlayingCubit>(
          create: (_) => NowPlayingCubit(
            songsRepo: sl<SongsRepository>(),
            playlistsRepo: sl<PlaylistsRepository>(),
          )..load(songId: songId, playlistId: playlistId),
        ),
        BlocProvider<FavoritesCubit>.value(
          value: sl<FavoritesCubit>()..ensureLoaded(),
        ),
      ],
      child: _NowPlayingView(fromPlaylist: playlistId != null),
    );
  }
}

class _NowPlayingView extends StatefulWidget {
  const _NowPlayingView({required this.fromPlaylist});
  final bool fromPlaylist;

  @override
  State<_NowPlayingView> createState() => _NowPlayingViewState();
}

class _NowPlayingViewState extends State<_NowPlayingView> {
  final _scale = TabScaleController();
  final _viewportKey = GlobalKey();

  @override
  void dispose() {
    _scale.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<NowPlayingCubit, NowPlayingState>(
      listenWhen: (p, c) => p.document != c.document,
      listener: (context, _) {
        // 文档变更后，下一帧根据内容尺寸做自适应缩放
        WidgetsBinding.instance.addPostFrameCallback((_) => _autoFit());
      },
      builder: (context, state) {
        return Scaffold(
          appBar: _buildAppBar(context, state),
          body: _buildBody(state),
          bottomSheet: widget.fromPlaylist ? _BottomBar(scale: _scale) : null,
        );
      },
    );
  }

  PreferredSizeWidget _buildAppBar(BuildContext context, NowPlayingState state) {
    final song = state.currentSong;
    return AppBar(
      leading: IconButton(
        icon: const Icon(LucideIcons.chevronLeft),
        onPressed: () => context.canPop() ? context.pop() : context.go('/index'),
      ),
      title: song == null
          ? const Text('')
          : Row(
              mainAxisAlignment: MainAxisAlignment.center,
              mainAxisSize: MainAxisSize.min,
              children: [
                Flexible(
                  child: Text(
                    song.title,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 17),
                  ),
                ),
                const SizedBox(width: 8),
                Flexible(
                  child: Text(
                    song.artist,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: AppColors.textSecondary, fontSize: 13),
                  ),
                ),
              ],
            ),
      actions: song == null
          ? null
          : [
              _FavoriteButton(song: song),
              IconButton(
                icon: const Icon(LucideIcons.plus, size: 20, color: AppColors.textSecondary),
                onPressed: () {
                  // TODO: openAddToPlaylistDialog
                },
              ),
              const SizedBox(width: 4),
            ],
    );
  }

  Widget _buildBody(NowPlayingState state) {
    if (state.loading) return const LoadingView();
    if (state.failure != null) {
      return ErrorView(
        message: state.failure!.displayMessage,
        onRetry: () {},
      );
    }
    final doc = state.document;
    if (doc == null) {
      return const EmptyView(message: '暂无吉他谱内容');
    }

    return Container(
      key: _viewportKey,
      color: AppColors.backgroundPage,
      child: SingleChildScrollView(
        physics: const BouncingScrollPhysics(),
        child: Center(
          child: TabView(
            document: doc,
            scaleController: _scale,
            onChordTap: (c) => showChordDiagram(context, c),
          ),
        ),
      ),
    );
  }

  void _autoFit() {
    final renderObject = _viewportKey.currentContext?.findRenderObject();
    if (renderObject is! RenderBox) return;
    final available = renderObject.size.height;
    // 这里保守地只在首屏做自适应：内容真实高度用 0 触发降级，交给用户捏合缩放。
    // 想做得更准可以把内容 Column 也打 GlobalKey 再量尺寸。
    _scale.autoFit(contentHeight: available * 0.8, availableHeight: available);
  }
}

class _FavoriteButton extends StatelessWidget {
  const _FavoriteButton({required this.song});
  final Song song;

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<FavoritesCubit, Set<String>>(
      builder: (context, ids) {
        final favored = ids.contains(song.id);
        return IconButton(
          onPressed: () => context.read<FavoritesCubit>().toggle(song.id),
          icon: Icon(
            favored ? LucideIcons.heart : LucideIcons.heart,
            color: favored ? AppColors.error : AppColors.textSecondary,
            size: 20,
          ),
        );
      },
    );
  }
}

class _BottomBar extends StatelessWidget {
  const _BottomBar({required this.scale});
  final TabScaleController scale;

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<NowPlayingCubit, NowPlayingState>(
      builder: (context, state) {
        return Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
          decoration: const BoxDecoration(
            color: AppColors.backgroundCard,
            border: Border(top: BorderSide(color: Color(0x14FFFFFF))),
          ),
          child: SafeArea(
            top: false,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                _NavButton(
                  icon: LucideIcons.skipBack,
                  label: state.prevSongName ?? '—',
                  enabled: state.hasPrev,
                  onTap: context.read<NowPlayingCubit>().prev,
                ),
                Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      width: 56,
                      height: 56,
                      decoration: const BoxDecoration(
                        color: AppColors.primary,
                        shape: BoxShape.circle,
                      ),
                      alignment: Alignment.center,
                      child: const Icon(LucideIcons.listMusic, color: Colors.white),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${state.currentIndex + 1}/${state.playlist.length}',
                      style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
                    ),
                  ],
                ),
                _NavButton(
                  icon: LucideIcons.skipForward,
                  label: state.nextSongName ?? '—',
                  enabled: state.hasNext,
                  onTap: context.read<NowPlayingCubit>().next,
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _NavButton extends StatelessWidget {
  const _NavButton({
    required this.icon,
    required this.label,
    required this.enabled,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool enabled;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final color = enabled ? AppColors.textPrimary : AppColors.textSecondary.withOpacity(0.3);
    return InkWell(
      onTap: enabled ? onTap : null,
      child: SizedBox(
        width: 96,
        child: Column(
          children: [
            Icon(icon, size: 26, color: color),
            const SizedBox(height: 4),
            Text(
              label,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(fontSize: 12, color: color),
            ),
          ],
        ),
      ),
    );
  }
}
