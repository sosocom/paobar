package com.sosocom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosocom.entity.PlayHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 播放历史 Mapper。
 * 除了 BaseMapper 自带的 CRUD 外，提供 MySQL 原生 upsert，
 * 避免先 SELECT 再 INSERT/UPDATE 的两次往返 + 并发竞争。
 */
@Mapper
public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {

    /**
     * 记录一次播放：
     *   - 首次 (userId, songId) 写入一行，play_count=1，last_played_at=NOW()
     *   - 已有则 play_count += 1，last_played_at = NOW()
     *
     * 依赖 play_history 表的 uk_user_song 唯一索引做 ON DUPLICATE KEY。
     */
    @Update({
            "INSERT INTO play_history (user_id, song_id, play_count, last_played_at, create_time) ",
            "VALUES (#{userId}, #{songId}, 1, NOW(), NOW()) ",
            "ON DUPLICATE KEY UPDATE ",
            "  play_count = play_count + 1, ",
            "  last_played_at = NOW()"
    })
    int upsertPlay(@Param("userId") Long userId, @Param("songId") Long songId);
}
