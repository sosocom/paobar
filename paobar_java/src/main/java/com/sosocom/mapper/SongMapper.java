package com.sosocom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosocom.entity.Song;
import org.apache.ibatis.annotations.Mapper;

/**
 * 歌曲Mapper接口
 */
@Mapper
public interface SongMapper extends BaseMapper<Song> {
}
