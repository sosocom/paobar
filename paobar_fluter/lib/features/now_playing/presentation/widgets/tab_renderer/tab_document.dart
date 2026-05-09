import 'package:freezed_annotation/freezed_annotation.dart';

part 'tab_document.freezed.dart';
part 'tab_document.g.dart';

/// 规范化吉他谱文档。字段完全对齐 `paobar_java` 的 `com.sosocom.tabdoc.TabDocument`，
/// 直接 `JSON ↔ freezed` 互通。爬虫入库时由 Java 侧 `TabHtmlNormalizer` 生成。
@freezed
class TabDocument with _$TabDocument {
  const factory TabDocument({
    @Default(1) int schemaVersion,
    String? title,
    @Default(<InfoItem>[]) List<InfoItem> info,
    String? meter,
    String? bpm,
    String? capoKey,
    String? originalKey,
    String? chordStyle,
    String? instrument,
    @Default(<SheetBlock>[]) List<SheetBlock> blocks,
  }) = _TabDocument;

  factory TabDocument.fromJson(Map<String, dynamic> json) =>
      _$TabDocumentFromJson(json);
}

/// "唱: 李健"、"编: 菜鸟吉他" 这样的 label-text 项。
@freezed
class InfoItem with _$InfoItem {
  const factory InfoItem({
    required String label,
    required String text,
  }) = _InfoItem;

  factory InfoItem.fromJson(Map<String, dynamic> json) =>
      _$InfoItemFromJson(json);
}

/// 正文段落块。union 辨别符使用 `type` 字段（值 "headline"/"paragraph"/"blank"）。
@Freezed(unionKey: 'type', unionValueCase: FreezedUnionCase.none)
sealed class SheetBlock with _$SheetBlock {
  const factory SheetBlock.headline({required String text}) = Headline;

  const factory SheetBlock.paragraph({
    @Default(<LineSegment>[]) List<LineSegment> segments,
  }) = Paragraph;

  const factory SheetBlock.blank() = Blank;

  factory SheetBlock.fromJson(Map<String, dynamic> json) =>
      _$SheetBlockFromJson(json);
}

/// 行内片段。union 辨别符同样为 `type`（值 "text"/"chord"）。
@Freezed(unionKey: 'type', unionValueCase: FreezedUnionCase.none)
sealed class LineSegment with _$LineSegment {
  /// 纯歌词。
  const factory LineSegment.text({required String text}) = PlainText;

  /// 某字带上方和弦名（chord 可为空串，表示纯占位锚点）。
  const factory LineSegment.chord({
    required String chord,
    @Default('') String text,
  }) = ChordText;

  factory LineSegment.fromJson(Map<String, dynamic> json) =>
      _$LineSegmentFromJson(json);
}
