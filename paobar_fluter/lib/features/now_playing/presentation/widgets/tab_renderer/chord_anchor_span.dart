import 'package:flutter/material.dart';
import 'package:paobar/app/theme/theme_extensions.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/chord_label.dart';

/// 一个"带和弦的字"：上方红色和弦名 + 下方带底边线的歌词字。
///
/// 实现策略：
/// - 上方 `SizedBox` 为和弦预留固定高度，即使没有和弦（空锚点）也占位，
///   让同一行内所有锚点的歌词字保持在同一 y 坐标。
/// - 外层 `Baseline` widget 显式声明本 WidgetSpan 的 alphabetic baseline
///   等于"歌词字 baseline 的 y"。这样 `Text.rich + PlaceholderAlignment.baseline`
///   就能让同行内的纯文本和锚点里的歌词字精确对齐。
///
/// 行间给和弦预留的垂直空间由父级 `Text.style.height` 控制（见 `tab_view.dart`）。
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

  /// 和弦名与歌词字之间的垂直间距。
  static const double _chordGap = 2;

  @override
  Widget build(BuildContext context) {
    final palette = context.tabPalette;
    final lyricFontSize = textStyle.fontSize ?? 17;
    final underlineHeight = lyricFontSize * 0.07;
    final chordFontSize = lyricFontSize * 0.85;
    final chordBoxHeight = chordFontSize + 2;
    final isEmpty = text.isEmpty;
    final displayText = isEmpty ? ' ' : text;

    // 歌词字 alphabetic baseline 大致位于字体顶往下 ~82%。
    // 把整个 WidgetSpan 的 baseline 声明在这个位置，让 Text.rich 正确对齐。
    final lyricBaselineFromLyricTop = lyricFontSize * 0.82;
    final widgetBaselineFromTop =
        chordBoxHeight + _chordGap + lyricBaselineFromLyricTop;

    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: Baseline(
        baseline: widgetBaselineFromTop,
        baselineType: TextBaseline.alphabetic,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // —— 和弦名（空锚点也占位，保证同行歌词字上下对齐）——
            SizedBox(
              height: chordBoxHeight,
              child: chord.isEmpty
                  ? const SizedBox.shrink()
                  : Center(
                      child: ChordLabel(text: chord, fontSize: chordFontSize),
                    ),
            ),
            const SizedBox(height: _chordGap),
            // —— 歌词字 + 底部下划线 ——
            Container(
              constraints: BoxConstraints(
                minWidth: isEmpty ? lyricFontSize : 0,
              ),
              margin: EdgeInsets.only(
                right: isEmpty ? lyricFontSize * 0.15 : 0,
              ),
              decoration: BoxDecoration(
                border: Border(
                  bottom: BorderSide(
                    color: palette.underline,
                    width: underlineHeight,
                  ),
                ),
              ),
              padding: EdgeInsets.only(bottom: underlineHeight * 2),
              child: Text(
                displayText,
                style: textStyle.copyWith(height: 1),
                softWrap: false,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
