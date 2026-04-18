import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/router/routes.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_state.dart';

class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('我的')),
      body: BlocBuilder<AuthCubit, AuthState>(
        builder: (context, state) {
          final user = state is Authenticated ? state.user : null;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: AppColors.backgroundCard,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Row(
                  children: [
                    CircleAvatar(
                      radius: 28,
                      backgroundColor: AppColors.primary.withOpacity(0.2),
                      child: Text(
                        user == null || user.username.isEmpty
                            ? '?'
                            : user.username.substring(0, 1).toUpperCase(),
                        style: const TextStyle(
                          color: AppColors.primary,
                          fontSize: 20,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            user?.username ?? '未登录',
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            user == null ? '点击右侧登录' : '积分：${user.points}',
                            style: const TextStyle(
                              color: AppColors.textSecondary,
                              fontSize: 13,
                            ),
                          ),
                        ],
                      ),
                    ),
                    if (user == null)
                      FilledButton(
                        onPressed: () => context.push(AppRoutes.login),
                        child: const Text('登录'),
                      )
                    else
                      IconButton(
                        icon: const Icon(LucideIcons.logOut),
                        onPressed: () => context.read<AuthCubit>().logout(),
                      ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              _MenuTile(
                icon: LucideIcons.heart,
                label: '我的收藏',
                onTap: () => context.push(AppRoutes.favorites),
              ),
              _MenuTile(
                icon: LucideIcons.listMusic,
                label: '我的歌单',
                onTap: () => context.push(AppRoutes.playlists),
              ),
              _MenuTile(
                icon: LucideIcons.download,
                label: '从网页抓取',
                onTap: () => context.push(AppRoutes.crawler),
              ),
              _MenuTile(
                icon: LucideIcons.settings,
                label: '设置',
                onTap: () => context.push(AppRoutes.settings),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _MenuTile extends StatelessWidget {
  const _MenuTile({required this.icon, required this.label, required this.onTap});
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Material(
        color: AppColors.backgroundCard,
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(12),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
            child: Row(
              children: [
                Icon(icon, size: 20, color: AppColors.textSecondary),
                const SizedBox(width: 12),
                Expanded(child: Text(label, style: const TextStyle(fontSize: 15))),
                const Icon(LucideIcons.chevronRight, size: 16, color: AppColors.textSecondary),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
