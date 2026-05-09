package com.sosocom.util;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Locale;

/**
 * 歌曲列表按「歌名拼音」排序用 sort_key（全拼小写、无空格），入库时由爬虫写入。
 *
 * <p>实现策略：逐字符判断；汉字走 pinyin4j 取首读全拼，非汉字（英文/数字/符号）保留并小写。
 * 这样的全拼字符串与前端 {@code pinyin-pro} 的输出在首字符层面保持一致，从而
 * 「首页右侧字母筛选」的后端 {@code LIKE 'x%'} 与前端 {@code songIndexLetter} 一致。
 */
@Slf4j
public final class SongSortKeyUtil {

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    private SongSortKeyUtil() {
    }

    /**
     * 由歌名生成排序键：汉字 → 全拼小写，其他字符原样保留并 toLowerCase。
     */
    public static String fromSongName(String songName) {
        if (songName == null || songName.isBlank()) {
            return "";
        }
        String trimmed = songName.trim();
        StringBuilder sb = new StringBuilder(trimmed.length() * 4);
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            String py = pinyinOfChar(c);
            sb.append(py != null ? py : Character.toString(c).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    /**
     * 单字符拼音（小写、无声调）。非汉字返回 {@code null}，由调用方决定如何展示。
     */
    private static String pinyinOfChar(char c) {
        if (c < 0x4E00 || c > 0x9FFF) {
            return null;
        }
        try {
            String[] arr = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
            if (arr == null || arr.length == 0) {
                return null;
            }
            return arr[0];
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.warn("pinyin4j format error for char '{}': {}", c, e.getMessage());
            return null;
        }
    }
}
