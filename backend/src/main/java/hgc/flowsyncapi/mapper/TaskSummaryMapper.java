package hgc.flowsyncapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hgc.flowsyncapi.entity.TaskSummary;

/**
 * 任务总结 Mapper 接口 —— 操作 task_summary 表的数据库访问层
 * <p>
 * 【新手必读】
 * 继承 BaseMapper 后自动获得对该表的 CRUD 方法，
 * 无需编写任何 SQL 语句即可完成对 task_summary 表的基本操作。
 */
public interface TaskSummaryMapper extends BaseMapper<TaskSummary> {
}
