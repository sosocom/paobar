import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/core/widgets/primary_button.dart';
import 'package:paobar/core/widgets/selectable_error_text.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_cubit.dart';
import 'package:paobar/features/auth/presentation/cubit/auth_state.dart';

class LoginForm extends StatefulWidget {
  const LoginForm({this.onSuccess, super.key});

  final VoidCallback? onSuccess;

  @override
  State<LoginForm> createState() => _LoginFormState();
}

class _LoginFormState extends State<LoginForm> {
  final _username = TextEditingController();
  final _password = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _obscure = true;
  bool _isRegister = false;

  @override
  void dispose() {
    _username.dispose();
    _password.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    final cubit = context.read<AuthCubit>();
    final ok = _isRegister
        ? await cubit.register(username: _username.text.trim(), password: _password.text)
        : await cubit.login(username: _username.text.trim(), password: _password.text);
    if (ok && mounted) widget.onSuccess?.call();
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AuthCubit, AuthState>(
      builder: (context, state) {
        final loading = state is Authenticating;
        final failure = state is Unauthenticated ? state.failure : null;
        return Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              TextFormField(
                controller: _username,
                textInputAction: TextInputAction.next,
                keyboardType: TextInputType.text,
                autofillHints: const [AutofillHints.username],
                decoration: const InputDecoration(
                  hintText: '用户名',
                  prefixIcon: Icon(LucideIcons.user, size: 18),
                ),
                validator: (v) =>
                    (v == null || v.trim().isEmpty) ? '请输入用户名' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _password,
                obscureText: _obscure,
                textInputAction: TextInputAction.done,
                autofillHints: const [AutofillHints.password],
                onFieldSubmitted: (_) => _submit(),
                decoration: InputDecoration(
                  hintText: '密码',
                  prefixIcon: const Icon(LucideIcons.lock, size: 18),
                  suffixIcon: IconButton(
                    icon: Icon(
                      _obscure ? LucideIcons.eye : LucideIcons.eyeOff,
                      size: 18,
                    ),
                    onPressed: () => setState(() => _obscure = !_obscure),
                  ),
                ),
                validator: (v) =>
                    (v == null || v.isEmpty) ? '请输入密码' : null,
              ),
              if (failure != null) ...[
                const SizedBox(height: 12),
                SelectableErrorText(failure.displayMessage),
              ],
              const SizedBox(height: 20),
              PrimaryButton(
                label: _isRegister ? '注册并登录' : '登录',
                loading: loading,
                onPressed: _submit,
              ),
              const SizedBox(height: 8),
              TextButton(
                onPressed: loading
                    ? null
                    : () => setState(() => _isRegister = !_isRegister),
                child: Text(
                  _isRegister ? '已有账号？立即登录' : '没有账号？立即注册',
                  style: const TextStyle(color: AppColors.textSecondary),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
