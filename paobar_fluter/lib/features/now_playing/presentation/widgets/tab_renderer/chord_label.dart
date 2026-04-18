import 'package:flutter/material.dart';
import 'package:paobar/app/theme/app_typography.dart';
import 'package:paobar/app/theme/theme_extensions.dart';

/// 悬浮在字上方的和弦名（对齐 Web：红色加粗、小号字体、无背景）。
class ChordLabel extends StatelessWidget {
  const ChordLabel({required this.text, this.fontSize = 13, super.key});

  final String text;
  final double fontSize;

  @override
  Widget build(BuildContext context) {
    final palette = context.tabPalette;
    return Text(
      text,
      maxLines: 1,
      overflow: TextOverflow.visible,
      softWrap: false,
      style: TextStyle(
        color: palette.accent,
        fontWeight: FontWeight.w600,
        fontSize: fontSize,
        height: 1,
        fontFamilyFallback: kChordFontFamily,
      ),
    );
  }
}
