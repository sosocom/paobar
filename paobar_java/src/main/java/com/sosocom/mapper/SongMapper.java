package com.sosocom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosocom.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

/**
 * 歌曲Mapper接口
 */
@Mapper
public interface SongMapper extends BaseMapper<Song> {

    /**
     * 批量查询：从给定 URL 列表中筛出已经在库（song.original_url）里的那些。
     * 用于"账号批量爬取"流程在入队前一次性过滤已入库的链接，避免逐条扒页面。
     *
     * <p>调用方需自行保证 urls 非空（MyBatis 的 in 标签对空集合会报错）。</p>
     */
    @Select({
        "<script>",
        "SELECT original_url FROM song",
        "WHERE original_url IN",
        "<foreach collection='urls' item='u' open='(' separator=',' close=')'>#{u}</foreach>",
        "</script>"
    })
    List<String> findExistingOriginalUrls(@Param("urls") Collection<String> urls);

    /**
     * 拍号回填：当本次爬取到的歌曲带有真实拍号时，把它同步到所有同(song_name, artist)
     * 但拍号缺失/未知的历史记录中，避免老数据停留在 "未知"。
     *
     * <p>仅覆盖 meter 为 NULL / "未知" / "" / 字符串 "null" 的行；已有真实拍号
     * 的旧记录保持不变，避免误覆盖手动修订过的数据。同时把结构化 beat 字段
     * 也同步上，保持数据库内两份字段一致。</p>
     *
     * @return 受影响的历史行数（不含本次爬入的当前行）
     */
    @Update("""
        UPDATE song
        SET tab_content_json = JSON_SET(tab_content_json, '$.meter', #{meter}),
            beat = #{meter},
            update_time = CURRENT_TIMESTAMP
        WHERE song_name = #{songName}
          AND artist = #{artist}
          AND id <> #{excludeId}
          AND tab_content_json IS NOT NULL
          AND (
                JSON_EXTRACT(tab_content_json, '$.meter') IS NULL
             OR JSON_UNQUOTE(JSON_EXTRACT(tab_content_json, '$.meter')) IN ('未知', '', 'null')
          )
        """)
    int backfillMeterByName(@Param("meter") String meter,
                            @Param("songName") String songName,
                            @Param("artist") String artist,
                            @Param("excludeId") Long excludeId);
}
