package hgc.flowsyncapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hgc.flowsyncapi.entity.TaskInfo;

/**
 * 任务 Mapper 接口 —— 操作 task_info 表的数据库访问层
 * <p>
 * 继承 BaseMapper 后自动获得对该表的 CRUD 方法。
 */
public interface TaskInfoMapper extends BaseMapper<TaskInfo> {
}
