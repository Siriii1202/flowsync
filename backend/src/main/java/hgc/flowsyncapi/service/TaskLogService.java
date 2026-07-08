package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.entity.TaskLog;
import hgc.flowsyncapi.mapper.TaskLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务日志服务 —— 处理任务日志相关的业务逻辑
 * <p>
 * 【新手必读】
 * 任务日志用于追踪每个任务的执行过程。当一个任务有进展时，
 * 团队成员可以记录日志，说明完成了多少进度、遇到了什么问题等。
 * <p>
 * 日志是项目管理和过程追溯的重要依据。
 */
@Service
public class TaskLogService {

    /**
     * 任务日志 Mapper，用于操作 task_log 表
     */
    private final TaskLogMapper taskLogMapper;

    /**
     * 构造方法注入
     */
    public TaskLogService(TaskLogMapper taskLogMapper) {
        this.taskLogMapper = taskLogMapper;
    }

    /**
     * 查询任务日志列表
     * <p>
     * 如果传入了 taskId，则只查询该任务下的日志；
     * 如果 taskId 为空，则查询所有日志。
     * 查询结果按 id 倒序排列，最新的日志显示在前面。
     *
     * @param taskId 任务 ID（可选，为空时查询所有日志）
     * @return 任务日志列表
     */
    public List<TaskLog> listTaskLogs(Long taskId) {
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();

        // 如果 taskId 不为空，添加筛选条件
        if (taskId != null) {
            wrapper.eq(TaskLog::getTaskId, taskId);
        }

        // 按 id 倒序排列
        wrapper.orderByDesc(TaskLog::getId);

        return taskLogMapper.selectList(wrapper);
    }

    /**
     * 新增任务日志
     * <p>
     * 注意：日志只支持"新增"操作，不支持修改或删除。
     * 因为日志是操作的历史记录，一旦创建就不应该被修改，
     * 这样才能保证记录的完整性和可信度。
     *
     * @param taskLog 任务日志对象
     * @return 操作结果（true 表示成功）
     */
    public boolean saveTaskLog(TaskLog taskLog) {
        return taskLogMapper.insert(taskLog) > 0;
    }
}
