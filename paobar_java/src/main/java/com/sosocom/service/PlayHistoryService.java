package com.sosocom.service;

import com.sosocom.dto.SongDTO;

import java.util.List;

/**
 * 播放历史服务：支持 upsert 记录 + 按用户倒序拉取 + 清理。
 */
public interface PlayHistoryService {

    /**
     * 记录当前登录用户打开该歌曲的一次播放。
     * 未登录直接跳过，不报错。
     */
    boolean recordPlay(String songId);

    /**
     * 列出当前登录用户最近的播放历史，按 last_played_at DESC。
     *
     * @param limit 最多返回条数；<=0 视为默认 100
     * @return 最近打开过的歌曲，字段同 SongDTO；若未登录则返回空列表
     */
    List<SongDTO> listRecent(int limit);

    /**
     * 从播放历史中移除一条（按歌曲 ID）。
     */
    boolean removeOne(String songId);

    /**
     * 清空当前登录用户的播放历史。
     *
     * @return 删除的行数
     */
    int clearAll();
}
