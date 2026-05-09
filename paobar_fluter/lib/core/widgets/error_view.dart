import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'package:paobar/app/theme/app_colors.dart';
import 'package:paobar/core/widgets/selectable_error_text.dart';

class ErrorView extends StatelessWidget {
  const ErrorView({
    required this.message,
    this.onAction,
    this.actionLabel = '重试',
    this.actionIcon = LucideIcons.rotateCcw,
    super.key,
  });

  final String message;
  final VoidCallback? onAction;
  final String actionLabel;
  final IconData actionIcon;

  /// 兼容旧调用方：`ErrorView(message: ..., onRetry: ...)`。
  factory ErrorView.retry({
    Key? key,
    required String message,
    VoidCallback? onRetry,
  }) =>
      ErrorView(
        key: key,
        message: message,
        onAction: onRetry,
      );

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(
              LucideIcons.alertTriangle,
              size: 40,
              color: AppColors.error,
            ),
            const SizedBox(height: 12),
            SelectableErrorText(message),
            if (onAction != null) ...[
              const SizedBox(height: 16),
              OutlinedButton.icon(
                onPressed: onAction,
                icon: Icon(actionIcon, size: 16),
                label: Text(actionLabel),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
