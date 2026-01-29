package com.guitar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guitar.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 歌单 Mapper
 */
@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {
}
