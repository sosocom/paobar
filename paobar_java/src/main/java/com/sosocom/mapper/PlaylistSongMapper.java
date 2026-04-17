package com.sosocom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosocom.entity.PlaylistSong;
import org.apache.ibatis.annotations.Mapper;

/**
 * 歌单-歌曲关联Mapper接口
 */
@Mapper
public interface PlaylistSongMapper extends BaseMapper<PlaylistSong> {
}
