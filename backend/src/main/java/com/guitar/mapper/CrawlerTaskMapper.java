package com.guitar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guitar.entity.CrawlerTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爬虫任务Mapper接口
 */
@Mapper
public interface CrawlerTaskMapper extends BaseMapper<CrawlerTask> {
}
