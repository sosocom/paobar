package com.sosocom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosocom.entity.CrawlerTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

/**
 * 爬虫任务Mapper接口
 *
 * <p>除常规 CRUD 外还承担"待爬队列"语义：task_status=PENDING 行 = 待消费任务，
 * task_status=PROCESSING 行 = 正在抓取；多次失败会先回 PENDING 重试，
 * retry_count 达上限则由服务层 DELETE 该行；SUCCESS = 已成功历史。</p>
 */
@Mapper
public interface CrawlerTaskMapper extends BaseMapper<CrawlerTask> {

    /**
     * 在给定 URL 列表中筛出"已在队列里"（PENDING 或 PROCESSING）的子集。
     * 用于账号批量入队前的去重，避免重复排队同一个 URL。
     */
    @Select({
        "<script>",
        "SELECT url FROM crawler_task",
        "WHERE task_status IN ('PENDING','PROCESSING')",
        "AND url IN",
        "<foreach collection='urls' item='u' open='(' separator=',' close=')'>#{u}</foreach>",
        "</script>"
    })
    List<String> findOpenQueueUrls(@Param("urls") Collection<String> urls);

    /**
     * 选下一条 PENDING 行的 id（FIFO，按主键升序）。
     * 配合 {@link #markProcessing(Long)} 的乐观锁完成"原子领取"语义：
     * 即使 select 与 update 之间有别的 worker 抢先翻状态，update 也只会改到 0 行，
     * 服务层据此知道该重试 select 下一行。
     */
    @Select("SELECT id FROM crawler_task WHERE task_status='PENDING' ORDER BY id LIMIT 1")
    Long selectNextPendingId();

    /**
     * 把指定 id 的任务从 PENDING 翻成 PROCESSING。
     * WHERE 里带 task_status='PENDING' 是乐观锁兜底：
     * 别的 worker 已抢到的话本次返回 0，调用方应重新 select 下一条。
     *
     * @return 受影响行数；1 = 抢到这行；0 = 已被别人抢走或不再是 PENDING
     */
    @Update("UPDATE crawler_task SET task_status='PROCESSING', update_time=NOW() "
            + "WHERE id=#{id} AND task_status='PENDING'")
    int markProcessing(@Param("id") Long id);

    /** 启动时把残留的 PROCESSING 行复位回 PENDING（上次进程崩了/重启遗留）。 */
    @Update("UPDATE crawler_task SET task_status='PENDING', update_time=NOW() "
            + "WHERE task_status='PROCESSING'")
    int resetStaleProcessing();

    /** 当前队列里待处理（PENDING + PROCESSING）的任务总数；用作 worker 唤醒判定。 */
    @Select("SELECT COUNT(*) FROM crawler_task WHERE task_status IN ('PENDING','PROCESSING')")
    long countOpenTasks();

    /**
     * 统计某账号 code 下所有 task_status 在给定集合里的任务数。
     * 用于账号进度页 / 状态查询的兜底（即便 in-memory 状态丢失也能从 DB 重建）。
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM crawler_task",
        "WHERE account_code = #{accountCode}",
        "AND task_status IN",
        "<foreach collection='statuses' item='s' open='(' separator=',' close=')'>#{s}</foreach>",
        "</script>"
    })
    long countByAccountAndStatuses(@Param("accountCode") String accountCode,
                                   @Param("statuses") Collection<String> statuses);
}
