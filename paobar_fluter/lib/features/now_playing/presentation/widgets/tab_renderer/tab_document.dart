import 'package:freezed_annotation/freezed_annotation.dart';

part 'tab_document.freezed.dart';

/// 整本谱：header（标题/唱编/meta）+ 结构化正文。
@freezed
class TabDocument with _$TabDocument {
  const factory TabDocument({
    required SheetHeader header,
    required List<SheetBlock> blocks,
  }) = _TabDocument;
}

@freezed
class SheetHeader with _$SheetHeader {
  const factory SheetHeader({
    required String title,
    @Default(<SheetInfoItem>[]) List<SheetInfoItem> info,
    String? meter, // 拍号
    String? bpm, // 拍速
    String? capoKey, // 选调
    String? originalKey, // 原唱调
  }) = _SheetHeader;
}

/// 唱 / 编 等 info 项
@freezed
class SheetInfoItem with _$SheetInfoItem {
  const factory SheetInfoItem({
    required String label,
    required String text,
  }) = _SheetInfoItem;
}

/// 段落块：标题（主歌/副歌）/ 文本段 / 空行
@freezed
sealed class SheetBlock with _$SheetBlock {
  const factory SheetBlock.headline(String text) = Headline;
  const factory SheetBlock.paragraph(List<LineSegment> segments) = Paragraph;
  const factory SheetBlock.blank() = Blank;
}

/// 行内片段：纯文本 / 带和弦的文本。
@freezed
sealed class LineSegment with _$LineSegment {
  /// 纯歌词（无和弦上方）
  const factory LineSegment.text(String text) = PlainText;

  /// 某字下方带下划线，上方浮着和弦名（可能为空串，空串表示纯占位，对齐 Web 的 data-value-length=0）
  const factory LineSegment.chord({
    required String chord,
    required String text,
  }) = ChordText;

  /// 换行
  const factory LineSegment.lineBreak() = LineBreak;
}
