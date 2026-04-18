import 'package:flutter/material.dart';
import 'package:paobar/app/theme/theme_extensions.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/chord_label.dart';

/// 一个"带和弦的字"：上方红色和弦名 + 下方带底边线的歌词字。
/// 通过 [WidgetSpan] 嵌入 RichText，保证随文本自然换行。
class ChordAnchorWidget extends StatelessWidget {
  const ChordAnchorWidget({
    required this.chord,
    required this.text,
    required this.textStyle,
    this.onTap,
    super.key,
  });

  final String chord;
  final String text;
  final TextStyle textStyle;
  final VoidCallback? onTap;

  static const double _chordGap = 2;

  @override
  Widget build(BuildContext context) {
    final palette = context.tabPalette;
    final underlineHeight = (textStyle.fontSize ?? 17) * 0.07;
    final chordFontSize = (textStyle.fontSize ?? 17) * 0.85;

    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // 和弦名（允许为空字符串 → 用同等高度占位，保持上下对齐）
          SizedBox(
            height: chordFontSize + 2,
            child: chord.isEmpty
                ? const SizedBox.shrink()
                : ChordLabel(text: chord, fontSize: chordFontSize),
          ),
          const SizedBox(height: _chordGap),
          // 歌词字，底部细下划线
          Container(
            decoration: BoxDecoration(
              border: Border(
                bottom: BorderSide(color: palette.underline, width: underlineHeight),
              ),
            ),
            padding: EdgeInsets.only(bottom: underlineHeight * 2),
            child: Text(
              text.isEmpty ? ' ' : text,
              style: textStyle,
              softWrap: false,
            ),
          ),
        ],
      ),
    );
  }
}
