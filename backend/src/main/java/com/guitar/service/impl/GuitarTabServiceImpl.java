package com.guitar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guitar.entity.GuitarTab;
import com.guitar.mapper.GuitarTabMapper;
import com.guitar.service.GuitarTabService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 吉他谱服务实现类
 */
@Slf4j
@Service
public class GuitarTabServiceImpl extends ServiceImpl<GuitarTabMapper, GuitarTab> implements GuitarTabService {

    @Override
    public Page<GuitarTab> pageQuery(int pageNum, int pageSize, String keyword, String artist) {
        Page<GuitarTab> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<GuitarTab> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuitarTab::getStatus, 1);
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(GuitarTab::getSongName, keyword)
                    .or()
                    .like(GuitarTab::getLyrics, keyword));
        }
        
        if (StringUtils.isNotBlank(artist)) {
            wrapper.like(GuitarTab::getArtist, artist);
        }
        
        wrapper.orderByDesc(GuitarTab::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        GuitarTab tab = this.getById(id);
        if (tab != null) {
            tab.setViewCount(tab.getViewCount() + 1);
            this.updateById(tab);
        }
    }

    @Override
    @Transactional
    public void incrementFavoriteCount(Long id) {
        GuitarTab tab = this.getById(id);
        if (tab != null) {
            tab.setFavoriteCount(tab.getFavoriteCount() + 1);
            this.updateById(tab);
        }
    }

    @Override
    @Transactional
    public void deleteByUrl(String url) {
        if (StringUtils.isNotBlank(url)) {
            LambdaQueryWrapper<GuitarTab> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GuitarTab::getOriginalUrl, url);
            
            // 查找已存在的记录
            GuitarTab existingTab = this.getOne(wrapper);
            if (existingTab != null) {
                // 物理删除旧记录
                this.removeById(existingTab.getId());
                log.info("删除相同URL的旧记录: id={}, url={}", existingTab.getId(), url);
            }
        }
    }
}
