import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/core/config/env.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_state.dart';

class SettingsPage extends StatelessWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('设置'),
        leading: IconButton(
          icon: const Icon(LucideIcons.chevronLeft),
          onPressed: () => context.canPop() ? context.pop() : context.go('/index'),
        ),
      ),
      body: ListView(
        children: [
          const ListTile(
            title: Text('关于', style: TextStyle(color: AppColors.textSecondary, fontSize: 13)),
            dense: true,
          ),
          ListTile(
            leading: const Icon(LucideIcons.server),
            title: const Text('API 地址'),
            subtitle: Text(Env.I.apiBaseUrl),
          ),
          ListTile(
            leading: const Icon(LucideIcons.settings2),
            title: const Text('环境'),
            subtitle: Text(Env.I.flavor.name),
          ),
          const Divider(height: 32),
          BlocBuilder<AuthCubit, AuthState>(
            builder: (context, state) {
              if (state is! Authenticated) return const SizedBox.shrink();
              return ListTile(
                leading: const Icon(LucideIcons.logOut, color: AppColors.error),
                title: const Text('退出登录', style: TextStyle(color: AppColors.error)),
                onTap: () => context.read<AuthCubit>().logout(),
              );
            },
          ),
        ],
      ),
    );
  }
}
