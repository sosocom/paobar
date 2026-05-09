package com.sosocom.tabdoc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 正文段落块。用"扁平字段 + type 辨别符"的方式表达 union：
 *
 *   {"type": "headline",  "text": "主歌 A"}
 *   {"type": "paragraph", "segments": [ ... ]}
 *   {"type": "blank"}
 *
 * 这样在 Java（Jackson）和 Dart（Freezed `unionKey: 'type'`）两侧都天然对齐，
 * 不需要多态子类或自定义序列化器。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SheetBlock {

    public static final String TYPE_HEADLINE = "headline";
    public static final String TYPE_PARAGRAPH = "paragraph";
    public static final String TYPE_BLANK = "blank";

    /** "headline" | "paragraph" | "blank"。 */
    private String type;

    /** 仅 headline 使用：段落名（"主歌 A" 之类）。 */
    private String text;

    /** 仅 paragraph 使用：行内片段序列。 */
    private List<LineSegment> segments;

    public static SheetBlock headline(String text) {
        SheetBlock b = new SheetBlock();
        b.type = TYPE_HEADLINE;
        b.text = text;
        return b;
    }

    public static SheetBlock paragraph(List<LineSegment> segments) {
        SheetBlock b = new SheetBlock();
        b.type = TYPE_PARAGRAPH;
        b.segments = segments;
        return b;
    }

    public static SheetBlock blank() {
        SheetBlock b = new SheetBlock();
        b.type = TYPE_BLANK;
        return b;
    }
}
