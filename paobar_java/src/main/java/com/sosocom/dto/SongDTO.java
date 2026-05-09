package com.sosocom.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sosocom.tabdoc.TabDocument;
import lombok.Data;

/**
 * 歌曲 DTO。
 *
 * 注意：原来的 `tabContent`（HTML 字符串）已移除，改为 {@link #tabDocument}（结构化 JSON 对象）。
 * 前端（paobar_fluter / paobar_web）直接消费 tabDocument，无需再做 HTML 解析。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SongDTO {

    private String id;

    private String title;

    private String artist;

    @JsonProperty("originalUrl")
    private String originalUrl;

    private String lyrics;

    /** 规范化吉他谱文档（schemaVersion=1）。 */
    @JsonProperty("tabDocument")
    private TabDocument tabDocument;

    @JsonProperty("tabImageUrl")
    private String tabImageUrl;

    private String meta;

    private String difficulty;

    private String tuning;

    private Integer capo;

    @JsonProperty("playKey")
    private String playKey;

    @JsonProperty("originalKey")
    private String originalKey;

    private String beat;

    @JsonProperty("viewCount")
    private Integer viewCount;

    @JsonProperty("favoriteCount")
    private Integer favoriteCount;
}
