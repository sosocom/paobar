import 'package:html/dom.dart' as dom;
import 'package:html/parser.dart' show parseFragment;
import 'package:paobar/features/now_playing/presentation/widgets/tab_renderer/tab_document.dart';

/// 把后端保存的 `<xhe-sheet>` HTML 解析成 [TabDocument]。
/// 结构参考 `paobar_web/src/views/NowPlaying.vue` 的注释：
///
///   .sheet-container > .xhe-sheet
///     > .sheet-header
///         .title
///         .info .item{.label .text}
///         .meta .col{.label .value}
///     > .xhe-body
///         <xhe-headline><div text-value>段落名</div></xhe-headline>
///         <xhe-text>歌词...</xhe-text>
///         <xhe-chord-anchor data-chord="1">
///           <div class="chord">1<span class="chord-type">m</span></div>
///           <div class="text">字</div>
///         </xhe-chord-anchor>
///         <xhe-line-break><br></xhe-line-break>
///
/// 解析容错：若找不到 .sheet-header 或 .xhe-body，降级为把整段文字当作一个 paragraph。
class TabHtmlParser {
  const TabHtmlParser();

  TabDocument parse(String html) {
    final frag = parseFragment(html);
    final root = _findRoot(frag);

    final header = _parseHeader(root);
    final body = _findFirst(root, ['xhe-body', '.xhe-body']) ?? root;
    final blocks = _parseBody(body);

    return TabDocument(header: header, blocks: blocks);
  }

  // ---------- 根定位 ----------

  dom.Node _findRoot(dom.DocumentFragment frag) {
    for (final candidate in ['xhe-sheet', '.xhe-sheet', '.sheet-container']) {
      final node = _findFirst(frag, [candidate]);
      if (node != null) return node;
    }
    return frag;
  }

  // ---------- Header ----------

  SheetHeader _parseHeader(dom.Node root) {
    final header = _findFirst(root, ['.sheet-header']);
    if (header == null) {
      return const SheetHeader(title: '');
    }

    final title = _textOf(_findFirst(header, ['.title'])) ?? '';

    final info = <SheetInfoItem>[];
    for (final item in _findAll(header, ['.info .item'])) {
      final label = _textOf(_findFirst(item, ['.label'])) ?? '';
      final text = _textOf(_findFirst(item, ['.text'])) ?? '';
      if (label.isEmpty && text.isEmpty) continue;
      info.add(SheetInfoItem(label: label, text: text));
    }

    String? meter, bpm, capoKey, originalKey;
    for (final col in _findAll(header, ['.meta .col'])) {
      final label = _textOf(_findFirst(col, ['.label'])) ?? '';
      final value = _textOf(_findFirst(col, ['.value'])) ?? '';
      if (value.isEmpty) continue;
      if (label.contains('拍号')) meter = value;
      else if (label.contains('拍速') || label.contains('BPM')) bpm = value;
      else if (label.contains('选调')) capoKey = value;
      else if (label.contains('原唱')) originalKey = value;
    }

    return SheetHeader(
      title: title,
      info: info,
      meter: meter,
      bpm: bpm,
      capoKey: capoKey,
      originalKey: originalKey,
    );
  }

  // ---------- Body ----------

  List<SheetBlock> _parseBody(dom.Node body) {
    final blocks = <SheetBlock>[];
    var segments = <LineSegment>[];

    void flush() {
      if (segments.isEmpty) return;
      blocks.add(SheetBlock.paragraph(List<LineSegment>.from(segments)));
      segments = <LineSegment>[];
    }

    for (final node in body.nodes) {
      if (node is! dom.Element) {
        if (node is dom.Text) {
          final t = node.text.trim();
          if (t.isNotEmpty) segments.add(LineSegment.text(node.text));
        }
        continue;
      }

      final tag = node.localName?.toLowerCase() ?? '';
      switch (tag) {
        case 'xhe-headline':
          flush();
          final text = _textOf(_findFirst(node, ['[text-value]'])) ?? node.text.trim();
          blocks.add(SheetBlock.headline(text));
        case 'xhe-text':
          segments.addAll(_parseXheText(node));
        case 'xhe-chord-anchor':
          segments.add(_parseChordAnchor(node));
        case 'xhe-line-break':
        case 'br':
          flush();
          blocks.add(const SheetBlock.blank());
        default:
          // 未知标签兜底：把文本原样输出
          final t = node.text.trim();
          if (t.isNotEmpty) segments.add(LineSegment.text(node.text));
      }
    }
    flush();
    return blocks;
  }

  List<LineSegment> _parseXheText(dom.Element node) {
    // xhe-text 内部通常是纯文本或嵌套 chord-anchor
    final out = <LineSegment>[];
    for (final child in node.nodes) {
      if (child is dom.Text) {
        if (child.text.isNotEmpty) out.add(LineSegment.text(child.text));
      } else if (child is dom.Element) {
        if (child.localName == 'xhe-chord-anchor') {
          out.add(_parseChordAnchor(child));
        } else {
          out.add(LineSegment.text(child.text));
        }
      }
    }
    return out;
  }

  LineSegment _parseChordAnchor(dom.Element node) {
    final chord = _textOf(_findFirst(node, ['.chord'])) ?? '';
    final text = _textOf(_findFirst(node, ['.text'])) ?? '';
    return LineSegment.chord(chord: chord, text: text);
  }

  // ---------- 选择器工具（package:html 的原生 querySelector 不支持 [text-value] 这种属性选择器时降级） ----------

  dom.Element? _findFirst(dom.Node root, List<String> selectors) {
    for (final s in selectors) {
      try {
        if (root is dom.Element) {
          final n = root.querySelector(s);
          if (n != null) return n;
        } else if (root is dom.Document) {
          final n = root.querySelector(s);
          if (n != null) return n;
        } else if (root is dom.DocumentFragment) {
          final n = root.querySelector(s);
          if (n != null) return n;
        }
      } catch (_) {
        // 某些选择器（如 [text-value]）可能不被支持，手动兜底
        final manual = _manualQuery(root, s);
        if (manual != null) return manual;
      }
    }
    return null;
  }

  Iterable<dom.Element> _findAll(dom.Node root, List<String> selectors) {
    for (final s in selectors) {
      try {
        if (root is dom.Element) return root.querySelectorAll(s);
        if (root is dom.Document) return root.querySelectorAll(s);
        if (root is dom.DocumentFragment) return root.querySelectorAll(s);
      } catch (_) {/* 继续下一个选择器 */}
    }
    return const <dom.Element>[];
  }

  dom.Element? _manualQuery(dom.Node root, String selector) {
    // 目前只需要手动兜底 [text-value]
    if (selector == '[text-value]') {
      return _walk(root).firstWhere(
        (e) => e.attributes.containsKey('text-value'),
        orElse: () => dom.Element.html('<span/>'),
      );
    }
    return null;
  }

  Iterable<dom.Element> _walk(dom.Node root) sync* {
    if (root is dom.Element) yield root;
    for (final child in root.nodes) {
      yield* _walk(child);
    }
  }

  String? _textOf(dom.Element? el) {
    if (el == null) return null;
    final t = el.text.trim();
    return t.isEmpty ? null : t;
  }
}
