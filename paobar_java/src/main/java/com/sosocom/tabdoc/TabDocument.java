package com.sosocom.tabdoc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 规范化吉他谱文档 —— 爬虫入库时从源站 HTML 解析出的结构化表示。
 *
 * 设计要点：
 * - schemaVersion 用于未来字段演进时做兼容。
 * - blocks 是段落序列（headline / paragraph / blank）。
 * - 与 paobar_fluter 的 TabDocument 一一对应，直接 JSON 互通。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TabDocument {

    /** 数据结构版本号，从 1 开始。 */
    private int schemaVersion = 1;

    /** 歌名。 */
    private String title;

    /** 演唱者 / 编谱 等信息条目。 */
    private List<InfoItem> info;

    /** 拍号，例如 "4/4"。 */
    private String meter;

    /** 拍速（BPM），字符串形式保留源站原样。 */
    private String bpm;

    /** 选调（演奏调），例如 "G"。 */
    private String capoKey;

    /** 原唱调，例如 "A"。 */
    private String originalKey;

    /** 和弦风格："number"（数字谱）| "letter"（字母谱）。 */
    private String chordStyle;

    /** 乐器："guitar" | "ukulele" | ...。 */
    private String instrument;

    /** 正文段落序列。 */
    private List<SheetBlock> blocks;
}
