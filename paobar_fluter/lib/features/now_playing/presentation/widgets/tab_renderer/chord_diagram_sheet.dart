import 'package:flutter/material.dart';
import 'package:paobar/app/theme/app_colors.dart';

/// 点击和弦名后弹出的指法图底部表单（预留实现，未来接 assets/chords/*.json）。
Future<void> showChordDiagram(BuildContext context, String chord) {
  return showModalBottomSheet<void>(
    context: context,
    builder: (_) => Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            chord,
            style: const TextStyle(
              color: AppColors.chordAccent,
              fontSize: 32,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 12),
          const Text(
            '指法图待接入',
            style: TextStyle(color: AppColors.textSecondary),
          ),
          const SizedBox(height: 24),
        ],
      ),
    ),
  );
}
