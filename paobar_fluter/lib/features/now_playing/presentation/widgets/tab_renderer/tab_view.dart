import 'package:flutter/material.dart';
import 'package:paobar/app/theme/app_typography.dart';
import 'package:paobar/app/theme/theme_extensions.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/chord_anchor_span.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/sheet_header_view.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_document.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_scale_controller.dart';

/// 谱面主渲染组件。
///
/// 性能 & 正确性要点：
/// - Paragraph 用 [RichText] + [WidgetSpan]，保证和弦锚点随行自然换行。
/// - Headline 带左侧红色竖条，对齐 Web `xhe-headline` 样式。
/// - 缩放由 [TabScaleController] 驱动，通过外层 [Transform.scale] 做整体缩放。
class TabView extends StatelessWidget {
  const TabView({
    required this.document,
    required this.scaleController,
    this.onChordTap,
    super.key,
  });

  final TabDocument document;
  final TabScaleController scaleController;
  final void Function(String chord)? onChordTap;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: scaleController,
      builder: (context, _) {
        return Transform.scale(
          scale: scaleController.scale,
          alignment: Alignment.topCenter,
          child: _Body(document: document, onChordTap: onChordTap),
        );
      },
    );
  }
}

class _Body extends StatelessWidget {
  const _Body({required this.document, this.onChordTap});

  final TabDocument document;
  final void Function(String chord)? onChordTap;

  @override
  Widget build(BuildContext context) {
    final palette = context.tabPalette;

    final baseStyle = TextStyle(
      fontSize: 17,
      // 对齐 Web `xhe-text { line-height: 3.5em }`，给上方和弦留出绝对定位空间。
      height: 3.5,
      color: palette.fontMain,
      letterSpacing: 0.07 * 17,
      fontFamilyFallback: kChineseFontFallback,
    );

    final body = <Widget>[SheetHeaderView(document: document)];

    for (final block in document.blocks) {
      body.add(
        switch (block) {
          Headline(:final text) => _Headline(text: text),
          Paragraph(:final segments) =>
            _Paragraph(segments: segments, baseStyle: baseStyle, onChordTap: onChordTap),
          Blank() => const SizedBox(height: 16),
        },
      );
    }

    return ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 820),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(24, 32, 24, 48),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: body,
        ),
      ),
    );
  }
}

class _Headline extends StatelessWidget {
  const _Headline({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    final palette = context.tabPalette;
    return Container(
      margin: const EdgeInsets.only(top: 20, bottom: 10),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: palette.headlineBg,
        borderRadius: const BorderRadius.only(
          topRight: Radius.circular(4),
          bottomRight: Radius.circular(4),
        ),
        border: Border(left: BorderSide(color: palette.accent, width: 3)),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: palette.fontSecondary,
          fontSize: 17,
          letterSpacing: 2.55,
          height: 1.7,
        ),
      ),
    );
  }
}

class _Paragraph extends StatelessWidget {
  const _Paragraph({
    required this.segments,
    required this.baseStyle,
    this.onChordTap,
  });

  final List<LineSegment> segments;
  final TextStyle baseStyle;
  final void Function(String chord)? onChordTap;

  @override
  Widget build(BuildContext context) {
    final spans = <InlineSpan>[];
    for (final seg in segments) {
      switch (seg) {
        case PlainText(:final text):
          spans.add(TextSpan(text: text));
        case ChordText(:final chord, :final text):
          spans.add(
            WidgetSpan(
              alignment: PlaceholderAlignment.baseline,
              baseline: TextBaseline.alphabetic,
              child: ChordAnchorWidget(
                chord: chord,
                text: text,
                textStyle: baseStyle,
                onTap: chord.isEmpty || onChordTap == null
                    ? null
                    : () => onChordTap!(chord),
              ),
            ),
          );
      }
    }
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Text.rich(
        TextSpan(style: baseStyle, children: spans),
        softWrap: true,
      ),
    );
  }
}
