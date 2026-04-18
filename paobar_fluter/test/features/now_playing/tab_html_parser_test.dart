import 'package:flutter_test/flutter_test.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_document.dart';
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_html_parser.dart';

void main() {
  const parser = TabHtmlParser();

  test('parses sheet header title / info / meta', () {
    const html = '''
<xhe-sheet>
  <div class="sheet-header">
    <div class="title">风吹麦浪</div>
    <div class="info">
      <div class="item"><span class="label">唱</span><span class="text">李健</span></div>
      <div class="item"><span class="label">编</span><span class="text">老孙</span></div>
    </div>
    <div class="meta">
      <div class="col"><span class="label">拍号</span><span class="value">4/4</span></div>
      <div class="col"><span class="label">拍速</span><span class="value">72</span></div>
      <div class="col"><span class="label">选调</span><span class="value">C</span></div>
      <div class="col"><span class="label">原唱调</span><span class="value">C</span></div>
    </div>
  </div>
  <div class="xhe-body"></div>
</xhe-sheet>
''';
    final doc = parser.parse(html);
    expect(doc.header.title, '风吹麦浪');
    expect(doc.header.info.length, 2);
    expect(doc.header.info.first.label, '唱');
    expect(doc.header.meter, '4/4');
    expect(doc.header.bpm, '72');
    expect(doc.header.capoKey, 'C');
    expect(doc.header.originalKey, 'C');
  });

  test('parses chord-anchors inside xhe-text', () {
    const html = '''
<xhe-sheet>
  <div class="sheet-header"><div class="title">t</div></div>
  <div class="xhe-body">
    <xhe-headline><div text-value="主歌">主歌</div></xhe-headline>
    <xhe-text>远</xhe-text>
    <xhe-chord-anchor><div class="chord">C</div><div class="text">处</div></xhe-chord-anchor>
    <xhe-text>的天</xhe-text>
  </div>
</xhe-sheet>
''';
    final doc = parser.parse(html);
    expect(doc.blocks.length, greaterThan(1));

    final headline = doc.blocks.firstWhere((b) => b is Headline) as Headline;
    expect(headline.text, '主歌');

    final paragraph =
        doc.blocks.firstWhere((b) => b is Paragraph) as Paragraph;
    final chord = paragraph.segments.whereType<ChordText>().first;
    expect(chord.chord, 'C');
    expect(chord.text, '处');
  });

  test('gracefully handles missing xhe-sheet root', () {
    const html = '<div>no sheet here</div>';
    final doc = parser.parse(html);
    expect(doc.header.title, '');
    expect(doc.blocks, isNotEmpty);
  });
}
