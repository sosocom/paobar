package com.sosocom.tabdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对 {@link TabHtmlNormalizer} 的行为做最小回归保护。
 */
class TabHtmlNormalizerTest {

    private final TabHtmlNormalizer normalizer = new TabHtmlNormalizer();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesHeaderTitleInfoAndMeta() {
        String html = """
            <xhe-sheet>
              <div class="sheet-header">
                <div class="title">风吹麦浪</div>
                <div class="info">
                  <div class="item"><span class="label">唱:</span><span class="text">李健</span></div>
                  <div class="item"><span class="label">编</span><span class="text">老孙</span></div>
                </div>
                <div class="meta">
                  <div class="col"><span class="label">拍号</span><span class="value">4/4</span></div>
                  <div class="col"><span class="label">拍速</span><span class="value">72</span></div>
                  <div class="col"><span class="label">选调</span><span class="value">C</span></div>
                  <div class="col"><span class="label">原唱调</span><span class="value">A</span></div>
                </div>
              </div>
              <div class="xhe-body"></div>
            </xhe-sheet>
            """;

        TabDocument doc = normalizer.normalize(html, "fallback");

        assertEquals("风吹麦浪", doc.getTitle());
        assertNotNull(doc.getInfo());
        assertEquals(2, doc.getInfo().size());
        assertEquals("唱", doc.getInfo().get(0).getLabel(),
                "label 结尾的冒号应该被清掉");
        assertEquals("李健", doc.getInfo().get(0).getText());
        assertEquals("4/4", doc.getMeter());
        assertEquals("72", doc.getBpm());
        assertEquals("C", doc.getCapoKey());
        assertEquals("A", doc.getOriginalKey());
    }

    @Test
    void parsesChordAnchorsInsideXheText() {
        String html = """
            <xhe-sheet>
              <div class="sheet-header"><div class="title">t</div></div>
              <div class="xhe-body">
                <xhe-headline><div text-value="主歌">主歌 A</div></xhe-headline>
                <xhe-text>远处</xhe-text>
                <xhe-chord-anchor data-chord="6"><div class="chord">6<span class="chord-type">m</span></div><div class="text">金</div></xhe-chord-anchor>
                <xhe-text>色的麦浪</xhe-text>
                <xhe-line-break><br></xhe-line-break>
                <xhe-text>下一段</xhe-text>
              </div>
            </xhe-sheet>
            """;

        TabDocument doc = normalizer.normalize(html, "fallback");
        List<SheetBlock> blocks = doc.getBlocks();
        assertNotNull(blocks);
        assertFalse(blocks.isEmpty());

        // 期望顺序：headline, paragraph(远处 + 6m金 + 色的麦浪), blank, paragraph(下一段)
        assertEquals(SheetBlock.TYPE_HEADLINE, blocks.get(0).getType());
        assertEquals("主歌 A", blocks.get(0).getText());

        assertEquals(SheetBlock.TYPE_PARAGRAPH, blocks.get(1).getType());
        List<LineSegment> segs = blocks.get(1).getSegments();
        assertEquals(3, segs.size());
        assertEquals("远处", segs.get(0).getText());
        assertEquals(LineSegment.TYPE_CHORD, segs.get(1).getType());
        assertEquals("6m", segs.get(1).getChord(),
                ".chord 节点文本应直接包含 chord-type 后缀（回归：之前 data-chord 与后缀重复拼接导致 '6mm'）");
        assertEquals("金", segs.get(1).getText());
        assertEquals("色的麦浪", segs.get(2).getText());

        assertEquals(SheetBlock.TYPE_BLANK, blocks.get(2).getType());
        assertEquals(SheetBlock.TYPE_PARAGRAPH, blocks.get(3).getType());
    }

    @Test
    void collapsesWhitespaceFromIndentedHtmlSource() {
        String html = """
            <xhe-sheet>
              <div class="sheet-header"><div class="title">t</div></div>
              <div class="xhe-body">
                <xhe-text>
                  多余
                  空白  应当折叠
                </xhe-text>
              </div>
            </xhe-sheet>
            """;

        TabDocument doc = normalizer.normalize(html, "t");
        assertEquals("多余 空白 应当折叠", doc.getBlocks().get(0).getSegments().get(0).getText());
    }

    @Test
    void jsonSerializationOmitsNullFields() throws Exception {
        String html = """
            <xhe-sheet>
              <div class="xhe-body">
                <xhe-line-break><br></xhe-line-break>
              </div>
            </xhe-sheet>
            """;

        String json = normalizer.normalizeToJson(html, "fallback");
        assertTrue(json.contains("\"schemaVersion\":1"));
        assertTrue(json.contains("\"type\":\"blank\""));
        assertFalse(json.contains("\"text\":null"), "NON_NULL 序列化，应不出现 null 字段");

        TabDocument roundTrip = objectMapper.readValue(json, TabDocument.class);
        assertEquals(1, roundTrip.getBlocks().size());
        assertEquals(SheetBlock.TYPE_BLANK, roundTrip.getBlocks().get(0).getType());
    }

    @Test
    void fallbackOnInvalidHtml() {
        TabDocument doc = normalizer.normalize("<div>not a sheet</div>", "fb");
        // 没有 sheet-header，title 使用 fallback
        assertEquals("fb", doc.getTitle());
        assertNotNull(doc.getBlocks());
        // body 回退用整个 div 文本
        assertInstanceOf(List.class, doc.getBlocks());
    }
}
