import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:paobar/core/di/injector.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:paobar/features/auth/presentation/widgets/login_form.dart';

/// 对齐 Web 的 LoginDialog：401 时或用户手动点需要登录的入口时弹起。
Future<bool?> showLoginDialog(BuildContext context) {
  return showModalBottomSheet<bool>(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    builder: (_) => BlocProvider<AuthCubit>.value(
      value: sl<AuthCubit>(),
      child: Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
          left: 20,
          right: 20,
          top: 8,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const SizedBox(height: 12),
            const Text(
              '登录 · 泡吧吉他谱',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 16),
            LoginForm(onSuccess: () => Navigator.of(context).pop(true)),
            const SizedBox(height: 12),
          ],
        ),
      ),
    ),
  );
}
