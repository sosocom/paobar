package com.sosocom.tabdoc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 把源站爬到的 `<xhe-sheet>` HTML 规范化成 {@link TabDocument}。
 *
 * 源站结构（以 yopu.co 数字谱为例）：
 *
 *   .sheet-container > .xhe-sheet
 *     > .sheet-header
 *         .title
 *         .info .item{.label .text}
 *         .meta .col{.label .value}
 *     > .xhe-body
 *         <xhe-headline><div text-value>段落名</div></xhe-headline>
 *         <xhe-text>歌词...</xhe-text>
 *         <xhe-chord-anchor data-chord="1">
 *             <div class="chord">1<span class="chord-type">m</span></div>
 *             <div class="text">字</div>
 *         </xhe-chord-anchor>
 *         <xhe-line-break><br></xhe-line-break>
 *
 * 规范化后产出的 JSON 约 20-30% 于源 HTML 体积，且所有空白、class 名、data-debug
 * JSON 都被丢弃，前端只消费结构化内容。
 */
@Slf4j
@Component
public class TabHtmlNormalizer {

    /** 空白折叠：任意连续空白 → 单空格。 */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ============================================================
    // Public API
    // ============================================================

    /** HTML → TabDocument。fallback：解析异常时返回只含 title 的空文档。 */
    public TabDocument normalize(String html, String fallbackTitle) {
        if (html == null || html.isBlank()) {
            TabDocument empty = new TabDocument();
            empty.setTitle(fallbackTitle);
            empty.setBlocks(new ArrayList<>());
            return empty;
        }

        try {
            Document doc = Jsoup.parse(html, "", Parser.htmlParser());
            Element root = findRoot(doc);

            TabDocument out = new TabDocument();
            out.setSchemaVersion(1);

            parseHeader(root, out);
            if (out.getTitle() == null || out.getTitle().isBlank()) {
                out.setTitle(fallbackTitle);
            }

            Element body = root.selectFirst(".xhe-body");
            if (body == null) body = root;
            out.setBlocks(parseBody(body));

            return out;
        } catch (Exception e) {
            log.error("TabHtmlNormalizer 解析失败，降级为空文档: {}", e.getMessage(), e);
            TabDocument fallback = new TabDocument();
            fallback.setTitle(fallbackTitle);
            fallback.setBlocks(new ArrayList<>());
            return fallback;
        }
    }

    /** 便捷方法：直接得到可以入库的 JSON 字符串。 */
    public String normalizeToJson(String html, String fallbackTitle) {
        TabDocument doc = normalize(html, fallbackTitle);
        try {
            return objectMapper.writeValueAsString(doc);
        } catch (JsonProcessingException e) {
            log.error("TabDocument 序列化失败", e);
            return "{\"schemaVersion\":1,\"blocks\":[]}";
        }
    }

    // ============================================================
    // Root / Header
    // ============================================================

    private Element findRoot(Document doc) {
        for (String sel : new String[]{".xhe-sheet", "xhe-sheet", ".sheet-container"}) {
            Element e = doc.selectFirst(sel);
            if (e != null) return e;
        }
        return doc.body() != null ? doc.body() : doc;
    }

    private void parseHeader(Element root, TabDocument out) {
        Element header = root.selectFirst(".sheet-header");
        if (header == null) return;

        Element titleEl = header.selectFirst(".title");
        if (titleEl != null) out.setTitle(collapseWhitespace(titleEl.text()));

        List<InfoItem> infos = new ArrayList<>();
        for (Element item : header.select(".info .item")) {
            Element label = item.selectFirst(".label");
            Element text = item.selectFirst(".text");
            String l = label != null ? cleanLabel(label.text()) : "";
            String t = text != null ? collapseWhitespace(text.text()) : "";
            if (!l.isEmpty() || !t.isEmpty()) {
                infos.add(new InfoItem(l, t));
            }
        }
        if (!infos.isEmpty()) out.setInfo(infos);

        for (Element col : header.select(".meta .col")) {
            Element label = col.selectFirst(".label");
            Element value = col.selectFirst(".value");
            if (label == null || value == null) continue;
            String l = label.text();
            String v = collapseWhitespace(value.text());
            if (v.isEmpty()) continue;
            if (l.contains("拍号")) out.setMeter(v);
            else if (l.contains("拍速") || l.contains("BPM")) out.setBpm(v);
            else if (l.contains("选调")) out.setCapoKey(v);
            else if (l.contains("原唱")) out.setOriginalKey(v);
        }
    }

    /** label 文案去掉尾部的冒号。 */
    private String cleanLabel(String raw) {
        String s = collapseWhitespace(raw);
        if (s.endsWith(":") || s.endsWith("：")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    // ============================================================
    // Body
    // ============================================================

    private List<SheetBlock> parseBody(Element body) {
        List<SheetBlock> blocks = new ArrayList<>();
        List<LineSegment> segments = new ArrayList<>();
        walkBody(body, blocks, segments);
        flushParagraph(blocks, segments);
        return blocks;
    }

    /**
     * 递归遍历 body：遇到 headline / line-break 会 flush 当前段落；
     * 遇到 xhe-text / xhe-chord-anchor 则吸收为 segment；
     * 遇到未知包装层直接下钻（源站偶尔套 `<xhe-line>` 这样的容器）。
     */
    private void walkBody(Element container, List<SheetBlock> blocks, List<LineSegment> segments) {
        for (Node node : container.childNodes()) {
            if (node instanceof TextNode tn) {
                String t = collapseWhitespace(tn.text());
                if (!t.isEmpty()) segments.add(LineSegment.text(t));
                continue;
            }
            if (!(node instanceof Element el)) continue;

            String tag = Objects.toString(el.tagName(), "").toLowerCase();
            switch (tag) {
                case "xhe-headline" -> {
                    flushParagraph(blocks, segments);
                    segments.clear();
                    blocks.add(SheetBlock.headline(parseHeadlineText(el)));
                }
                case "xhe-text" -> segments.addAll(parseXheText(el));
                case "xhe-chord-anchor" -> segments.add(parseChordAnchor(el));
                case "xhe-line-break", "br" -> {
                    flushParagraph(blocks, segments);
                    segments.clear();
                    blocks.add(SheetBlock.blank());
                }
                default -> walkBody(el, blocks, segments);
            }
        }
    }

    private void flushParagraph(List<SheetBlock> blocks, List<LineSegment> segments) {
        if (segments.isEmpty()) return;
        blocks.add(SheetBlock.paragraph(new ArrayList<>(segments)));
    }

    private String parseHeadlineText(Element el) {
        Element textValue = el.selectFirst("[text-value]");
        String raw = textValue != null ? textValue.text() : el.text();
        return collapseWhitespace(raw);
    }

    private List<LineSegment> parseXheText(Element node) {
        List<LineSegment> out = new ArrayList<>();
        for (Node child : node.childNodes()) {
            if (child instanceof TextNode tn) {
                String t = collapseWhitespace(tn.text());
                if (!t.isEmpty()) out.add(LineSegment.text(t));
            } else if (child instanceof Element el) {
                if ("xhe-chord-anchor".equalsIgnoreCase(el.tagName())) {
                    out.add(parseChordAnchor(el));
                } else {
                    String t = collapseWhitespace(el.text());
                    if (!t.isEmpty()) out.add(LineSegment.text(t));
                }
            }
        }
        return out;
    }

    private LineSegment parseChordAnchor(Element node) {
        // 源站 `.chord` 节点内部通常是 "数字 + <span class='chord-type'>m/7/...</span>"，
        // jsoup 的 .text() 会把全部后代文本拼起来，所以直接取 .chord 文本就是最终和弦名。
        // data-chord 属性会漏掉 chord-type 后缀（例如 data-chord="3" + chord-type="m"），
        // 不可靠，仅作为 .chord 缺失时的降级。
        String chord = "";
        Element chordEl = node.selectFirst(".chord");
        if (chordEl != null) {
            chord = collapseWhitespace(chordEl.text());
        } else if (node.hasAttr("data-chord")) {
            chord = node.attr("data-chord").trim();
        }

        Element textEl = node.selectFirst(".text");
        String text = textEl != null ? collapseWhitespace(textEl.text()) : "";

        // data-value-length=0 的占位锚点：text 为空字符串，保留 chord
        return LineSegment.chord(chord, text);
    }

    // ============================================================
    // Utils
    // ============================================================

    private static String collapseWhitespace(String s) {
        if (s == null || s.isEmpty()) return "";
        // 处理 &nbsp; 也一并当空白折叠
        String replaced = s.replace('\u00A0', ' ');
        return WHITESPACE.matcher(replaced).replaceAll(" ").trim();
    }
}
