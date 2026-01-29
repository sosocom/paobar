package com.guitar.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guitar.entity.GuitarTab;

/**
 * 吉他谱服务接口
 */
public interface GuitarTabService extends IService<GuitarTab> {

    /**
     * 分页查询吉他谱
     */
    Page<GuitarTab> pageQuery(int pageNum, int pageSize, String keyword, String artist);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);

    /**
     * 增加收藏次数
     */
    void incrementFavoriteCount(Long id);

    /**
     * 根据 URL 删除吉他谱（用于重新爬取）
     */
    void deleteByUrl(String url);
}
