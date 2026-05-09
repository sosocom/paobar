import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/router/app_router.dart';
import 'package:paobar/core/errors/failure.dart';
import 'package:paobar/core/widgets/error_view.dart';

/// 统一的 Failure 展示：
/// - `UnauthorizedFailure` → 按钮文案"去登录"，点击跳 `/login?redirect=<当前路径>`
/// - 其他 Failure → 按钮文案"重试"，点击调 [onRetry]
///
/// 所有需要展示 `Failure` 的页面都应复用这个组件，保证 401 UX 一致。
class FailureView extends StatelessWidget {
  const FailureView({
    required this.failure,
    this.onRetry,
    super.key,
  });

  final Failure failure;
  final VoidCallback? onRetry;

  @override
  Widget build(BuildContext context) {
    final isUnauth = failure is UnauthorizedFailure;
    if (isUnauth) {
      return ErrorView(
        message: failure.displayMessage,
        actionLabel: '去登录',
        actionIcon: LucideIcons.logIn,
        onAction: () => goToLoginFromAnywhere(context),
      );
    }
    return ErrorView(
      message: failure.displayMessage,
      onAction: onRetry,
    );
  }
}
