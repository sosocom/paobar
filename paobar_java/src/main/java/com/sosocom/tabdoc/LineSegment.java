package com.sosocom.tabdoc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 段落内的行内片段。union 表达方式与 SheetBlock 一致（type 辨别符）。
 *
 *   {"type": "text",  "text": "远处"}
 *   {"type": "chord", "chord": "6m", "text": "金"}
 *
 * 注：不再需要显式 lineBreak 片段——源站用 xhe-line-break 表达段落分隔，
 * 我们在 Normalizer 里直接将它们映射为 paragraph flush + blank block。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LineSegment {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_CHORD = "chord";

    /** "text" | "chord"。 */
    private String type;

    /**
     * text / chord 两种类型都使用：
     * - text 时：这一段纯歌词
     * - chord 时：这一个字（可能为空串，表示纯和弦占位）
     */
    private String text;

    /** 仅 chord 使用：和弦级数或名称，例如 "1"、"6m"、"Am7"。 */
    private String chord;

    public static LineSegment text(String text) {
        LineSegment s = new LineSegment();
        s.type = TYPE_TEXT;
        s.text = text;
        return s;
    }

    public static LineSegment chord(String chord, String text) {
        LineSegment s = new LineSegment();
        s.type = TYPE_CHORD;
        s.chord = chord;
        s.text = text;
        return s;
    }
}
