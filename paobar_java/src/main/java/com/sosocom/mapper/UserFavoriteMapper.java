package com.sosocom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosocom.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏Mapper接口
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
