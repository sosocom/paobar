import 'package:flutter/material.dart';
import 'package:paobar/app/theme/theme_extensions.dart';
import 'package:paobar/core/responsive/screen_type.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_document.dart';

/// 谱面头部：标题 + 唱/编 info + 2x2 / 4x1 meta 带。
/// 完全对齐 NowPlaying.vue 里 sheet-header 的结构与视觉。
class SheetHeaderView extends StatelessWidget {
  const SheetHeaderView({required this.header, super.key});

  final SheetHeader header;

  @override
  Widget build(BuildContext context) {
    final palette = context.tabPalette;
    final compact = context.screenType.isCompact;

    final metaCols = <({String label, String value})>[
      if (header.meter != null) (label: '拍号', value: header.meter!),
      if (header.bpm != null) (label: '拍速', value: header.bpm!),
      if (header.capoKey != null) (label: '选调', value: header.capoKey!),
      if (header.originalKey != null) (label: '原唱调', value: header.originalKey!),
    ];

    return Container(
      padding: const EdgeInsets.only(bottom: 16),
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        border: Border(
          bottom: BorderSide(
            color: palette.metaBorder,
            style: BorderStyle.solid,
          ),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            header.title,
            style: TextStyle(
              fontSize: compact ? 22 : 26,
              fontWeight: FontWeight.w600,
              color: palette.fontMain,
              height: 1.3,
              letterSpacing: 1,
            ),
          ),
          if (header.info.isNotEmpty) ...[
            const SizedBox(height: 8),
            Wrap(
              spacing: 24,
              runSpacing: 4,
              children: [
                for (final i in header.info)
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.baseline,
                    textBaseline: TextBaseline.alphabetic,
                    children: [
                      Text(
                        i.label,
                        style: TextStyle(color: palette.accent, fontSize: 13),
                      ),
                      const SizedBox(width: 6),
                      Text(
                        i.text,
                        style: TextStyle(color: palette.fontSecondary, fontSize: 14),
                      ),
                    ],
                  ),
              ],
            ),
          ],
          if (metaCols.isNotEmpty) ...[
            const SizedBox(height: 10),
            _MetaStrip(cols: metaCols, twoColumns: compact),
          ],
        ],
      ),
    );
  }
}

class _MetaStrip extends StatelessWidget {
  const _MetaStrip({required this.cols, required this.twoColumns});

  final List<({String label, String value})> cols;
  final bool twoColumns;

  @override
  Widget build(BuildContext context) {
    final palette = context.tabPalette;

    Widget cell(int i, int count) {
      final c = cols[i];
      return Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
        decoration: BoxDecoration(
          border: Border(
            left: i == 0 || (twoColumns && i == 2)
                ? BorderSide.none
                : BorderSide(color: palette.metaBorder),
            bottom: twoColumns && i < 2
                ? BorderSide(color: palette.metaBorder)
                : BorderSide.none,
          ),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.baseline,
          textBaseline: TextBaseline.alphabetic,
          children: [
            Text(
              c.label,
              style: TextStyle(color: palette.fontTertiary, fontSize: 12),
            ),
            const SizedBox(width: 4),
            Flexible(
              child: Text(
                c.value,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: palette.fontMain,
                  fontSize: 13,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ],
        ),
      );
    }

    return Container(
      decoration: BoxDecoration(
        border: Border.all(color: palette.metaBorder),
        borderRadius: BorderRadius.circular(8),
        color: const Color(0x07FFFFFF),
      ),
      child: twoColumns
          ? Column(
              children: [
                Row(
                  children: [
                    Expanded(child: cell(0, cols.length)),
                    Expanded(child: cell(1, cols.length)),
                  ],
                ),
                if (cols.length >= 3)
                  Row(
                    children: [
                      Expanded(child: cell(2, cols.length)),
                      if (cols.length >= 4)
                        Expanded(child: cell(3, cols.length))
                      else
                        const Expanded(child: SizedBox.shrink()),
                    ],
                  ),
              ],
            )
          : Row(
              children: [
                for (var i = 0; i < cols.length; i++)
                  Expanded(child: cell(i, cols.length)),
              ],
            ),
    );
  }
}
