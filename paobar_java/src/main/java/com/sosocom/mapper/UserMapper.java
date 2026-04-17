package com.sosocom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosocom.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 不走 @TableLogic 逻辑，直接按 id 查用户（用于修复 deleted 为 null 的脏数据）
     */
    @Select("SELECT * FROM user WHERE id = #{id} LIMIT 1")
    User selectByIdRaw(Long id);

    /**
     * 修复 deleted 为 null 的数据
     */
    @Update("UPDATE user SET deleted = 0 WHERE id = #{id} AND deleted IS NULL")
    int fixDeletedNull(Long id);
}
