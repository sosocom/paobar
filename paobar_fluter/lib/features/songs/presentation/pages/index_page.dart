import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/router/routes.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/core/widgets/empty_view.dart';
import 'package:paobar/core/widgets/failure_view.dart';
import 'package:paobar/core/widgets/loading_view.dart';
import 'package:paobar/core/widgets/song_tile.dart';
import 'package:paobar/features/songs/domain/repositories/songs_repository.dart';
import 'package:paobar/features/songs/presentation/cubit/songs_list_cubit.dart';
import 'package:paobar/features/songs/presentation/cubit/songs_list_state.dart';

class IndexPage extends StatelessWidget {
  const IndexPage({super.key});

  static const _genres = <String>['全部', '民谣', '流行', '摇滚', '经典'];

  @override
  Widget build(BuildContext context) {
    return BlocProvider<SongsListCubit>(
      create: (_) => SongsListCubit(sl<SongsRepository>())..load(),
      child: Scaffold(
        appBar: AppBar(
          title: const Text('谱库'),
          actions: [
            IconButton(
              tooltip: '抓取',
              icon: const Icon(LucideIcons.download),
              onPressed: () => context.push(AppRoutes.crawler),
            ),
          ],
        ),
        body: Column(
          children: [
            const _SearchBar(),
            const _GenreFilter(genres: _genres),
            const Expanded(child: _SongsList()),
          ],
        ),
      ),
    );
  }
}

class _SearchBar extends StatelessWidget {
  const _SearchBar();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
      child: TextField(
        textInputAction: TextInputAction.search,
        decoration: const InputDecoration(
          hintText: '搜索歌曲或歌手',
          prefixIcon: Icon(LucideIcons.search, size: 18),
          isDense: true,
        ),
        onChanged: context.read<SongsListCubit>().setSearch,
      ),
    );
  }
}

class _GenreFilter extends StatelessWidget {
  const _GenreFilter({required this.genres});

  final List<String> genres;

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<SongsListCubit, SongsListState>(
      buildWhen: (p, c) => p.genre != c.genre,
      builder: (context, state) {
        return SizedBox(
          height: 44,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
            itemCount: genres.length,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (context, i) {
              final g = genres[i];
              final selected = g == state.genre;
              return ChoiceChip(
                label: Text(g),
                selected: selected,
                onSelected: (_) => context.read<SongsListCubit>().setGenre(g),
                backgroundColor: AppColors.backgroundCard,
                selectedColor: AppColors.primary,
                labelStyle: TextStyle(
                  color: selected ? Colors.white : AppColors.textSecondary,
                ),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: BorderSide(
                    color: selected ? Colors.transparent : AppColors.divider,
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }
}

class _SongsList extends StatelessWidget {
  const _SongsList();

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<SongsListCubit, SongsListState>(
      builder: (context, state) {
        if (state.loading && state.songs.isEmpty) {
          return const LoadingView();
        }
        if (state.failure != null && state.songs.isEmpty) {
          return FailureView(
            failure: state.failure!,
            onRetry: context.read<SongsListCubit>().load,
          );
        }
        if (state.songs.isEmpty) {
          return const EmptyView(message: '没有找到相关歌曲');
        }
        return RefreshIndicator(
          onRefresh: context.read<SongsListCubit>().load,
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
            itemCount: state.songs.length,
            separatorBuilder: (_, __) => const SizedBox(height: 4),
            itemBuilder: (context, i) {
              final song = state.songs[i];
              return SongTile(
                song: song,
                onTap: () => context.push(AppRoutes.nowPlayingOf(song.id)),
              );
            },
          ),
        );
      },
    );
  }
}
