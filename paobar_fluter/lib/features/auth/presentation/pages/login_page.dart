import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:paobar/app/router/routes.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/features/auth/presentation/widgets/login_form.dart';

class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    final redirect = GoRouterState.of(context).uri.queryParameters['redirect'];
    return Scaffold(
      appBar: AppBar(
        title: const Text('登录'),
        leading: IconButton(
          icon: const Icon(Icons.close),
          onPressed: () => context.go(redirect ?? AppRoutes.index),
        ),
      ),
      body: SafeArea(
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 420),
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const SizedBox(height: 12),
                  const Text(
                    '欢迎回到',
                    style: TextStyle(
                      color: AppColors.textSecondary,
                      fontSize: 14,
                    ),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    '泡吧吉他谱',
                    style: TextStyle(
                      fontSize: 28,
                      fontWeight: FontWeight.w700,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 32),
                  LoginForm(
                    onSuccess: () => context.go(redirect ?? AppRoutes.index),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
