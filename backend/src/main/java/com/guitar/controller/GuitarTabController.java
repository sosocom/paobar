package com.guitar.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guitar.common.Result;
import com.guitar.entity.GuitarTab;
import com.guitar.service.GuitarTabService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 吉他谱Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/tabs")
public class GuitarTabController {

    @Autowired
    private GuitarTabService guitarTabService;

    /**
     * 分页查询吉他谱
     */
    @GetMapping("/page")
    public Result<Page<GuitarTab>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String artist) {
        
        log.info("分页查询吉他谱: pageNum={}, pageSize={}, keyword={}, artist={}", 
                pageNum, pageSize, keyword, artist);
        
        Page<GuitarTab> page = guitarTabService.pageQuery(pageNum, pageSize, keyword, artist);
        return Result.success(page);
    }

    /**
     * 根据ID查询吉他谱详情
     */
    @GetMapping("/{id}")
    public Result<GuitarTab> getById(@PathVariable Long id) {
        log.info("查询吉他谱详情: id={}", id);
        
        GuitarTab tab = guitarTabService.getById(id);
        if (tab == null) {
            return Result.error("吉他谱不存在");
        }
        
        // 增加浏览次数
        guitarTabService.incrementViewCount(id);
        
        return Result.success(tab);
    }

    /**
     * 新增吉他谱
     */
    @PostMapping
    public Result<GuitarTab> add(@RequestBody GuitarTab guitarTab) {
        log.info("新增吉他谱: {}", guitarTab.getSongName());
        
        guitarTabService.save(guitarTab);
        return Result.success("添加成功", guitarTab);
    }

    /**
     * 更新吉他谱
     */
    @PutMapping
    public Result<String> update(@RequestBody GuitarTab guitarTab) {
        log.info("更新吉他谱: id={}", guitarTab.getId());
        
        guitarTabService.updateById(guitarTab);
        return Result.success("更新成功");
    }

    /**
     * 删除吉他谱
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除吉他谱: id={}", id);
        
        guitarTabService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 收藏吉他谱
     */
    @PostMapping("/{id}/favorite")
    public Result<String> favorite(@PathVariable Long id) {
        log.info("收藏吉他谱: id={}", id);
        
        guitarTabService.incrementFavoriteCount(id);
        return Result.success("收藏成功");
    }
}
