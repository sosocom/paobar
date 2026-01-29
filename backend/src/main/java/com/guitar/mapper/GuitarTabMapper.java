package com.guitar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guitar.entity.GuitarTab;
import org.apache.ibatis.annotations.Mapper;

/**
 * 吉他谱Mapper接口
 */
@Mapper
public interface GuitarTabMapper extends BaseMapper<GuitarTab> {
}
