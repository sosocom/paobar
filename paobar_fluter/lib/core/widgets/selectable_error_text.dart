import 'package:flutter/material.dart';
import 'package:paobar/app/theme/app_colors.dart';

/// 规则约定：错误显示使用 `SelectableText.rich` + 红色以便用户复制、定位。
class SelectableErrorText extends StatelessWidget {
  const SelectableErrorText(this.message, {this.fontSize = 14, super.key});

  final String message;
  final double fontSize;

  @override
  Widget build(BuildContext context) {
    return SelectableText.rich(
      TextSpan(
        text: message,
        style: TextStyle(
          color: AppColors.error,
          fontSize: fontSize,
          height: 1.5,
        ),
      ),
      textAlign: TextAlign.center,
    );
  }
}
