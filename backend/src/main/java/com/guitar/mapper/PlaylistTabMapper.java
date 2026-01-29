package com.guitar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guitar.entity.GuitarTab;
import com.guitar.entity.PlaylistTab;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 歌单-吉他谱关联 Mapper
 */
@Mapper
public interface PlaylistTabMapper extends BaseMapper<PlaylistTab> {

    @Select("SELECT COUNT(*) FROM playlist_tab WHERE playlist_id = #{playlistId}")
    int countByPlaylistId(@Param("playlistId") Long playlistId);

    @Select("SELECT t.* FROM guitar_tab t " +
            "INNER JOIN playlist_tab pt ON t.id = pt.tab_id " +
            "WHERE pt.playlist_id = #{playlistId} AND t.deleted = 0 " +
            "ORDER BY pt.id DESC")
    List<GuitarTab> selectTabsByPlaylistId(@Param("playlistId") Long playlistId);

    @Select("SELECT id FROM playlist_tab WHERE playlist_id = #{playlistId} AND tab_id = #{tabId} LIMIT 1")
    Long selectIdByPlaylistAndTab(@Param("playlistId") Long playlistId, @Param("tabId") Long tabId);
}
