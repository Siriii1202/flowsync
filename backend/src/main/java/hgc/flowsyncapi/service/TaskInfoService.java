package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.entity.ProjectMember;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.TaskLog;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.mapper.ProjectMemberMapper;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.mapper.TaskLogMapper;
import hgc.flowsyncapi.mapper.TaskSummaryMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务服务 —— 处理任务管理相关的业务逻辑
 */
@Service
public class TaskInfoService {

    private final TaskInfoMapper taskInfoMapper;
    private final TaskLogMapper taskLogMapper;
    private final TaskSummaryMapper taskSummaryMapper;
    private final UserMapper userMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public TaskInfoService(TaskInfoMapper taskInfoMapper, TaskLogMapper taskLogMapper,
                           TaskSummaryMapper taskSummaryMapper, UserMapper userMapper,
                           ProjectMemberMapper projectMemberMapper) {
        this.taskInfoMapper = taskInfoMapper;
        this.taskLogMapper = taskLogMapper;
        this.taskSummaryMapper = taskSummaryMapper;
        this.userMapper = userMapper;
        this.projectMemberMapper = projectMemberMapper;
    }

    /**
     * 查询任务列表（含负责人用户名和所属项目名）
     */
    public List<Map<String, Object>> listTasks(Long projectId) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(TaskInfo::getProjectId, projectId);
        }

        wrapper.orderByDesc(TaskInfo::getId);

        List<TaskInfo> taskList = taskInfoMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (TaskInfo t : taskList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("projectId", t.getProjectId());
            map.put("parentId", t.getParentId());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("assigneeId", t.getAssigneeId());
            map.put("creatorId", t.getCreatorId());
            map.put("status", t.getStatus());
            map.put("priority", t.getPriority());
            map.put("dueDate", t.getDueDate());
            map.put("aiSuggestion", t.getAiSuggestion());
            map.put("createTime", t.getCreateTime());
            map.put("completed", t.getCompleted());
            map.put("completedAt", t.getCompletedAt());
            map.put("completionSummary", t.getCompletionSummary());

            // 查负责人用户名
            if (t.getAssigneeId() != null) {
                User user = userMapper.selectById(t.getAssigneeId());
                map.put("assigneeName", user != null ? user.getRealName() : "未知");
            } else {
                map.put("assigneeName", null);
            }

            result.add(map);
        }
        return result;
    }

    /**
     * 根据用户 ID 查询该用户能看到的任务
     * <p>
     * 先查出用户参与的项目 ID 列表，再查询这些项目下的所有任务。
     *
     * @param userId 用户 ID
     * @return 任务列表（含负责人用户名）
     */
    public List<Map<String, Object>> getTasksByUserId(Long userId) {
        // 查出用户参与的项目 ID 列表
        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getUserId, userId);
        List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);
        if (members.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> projectIds = members.stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toList());

        // 查询这些项目下的所有任务
        LambdaQueryWrapper<TaskInfo> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.in(TaskInfo::getProjectId, projectIds);
        taskWrapper.orderByDesc(TaskInfo::getId);
        List<TaskInfo> taskList = taskInfoMapper.selectList(taskWrapper);

        // 组装返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (TaskInfo t : taskList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("projectId", t.getProjectId());
            map.put("parentId", t.getParentId());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("assigneeId", t.getAssigneeId());
            map.put("creatorId", t.getCreatorId());
            map.put("status", t.getStatus());
            map.put("priority", t.getPriority());
            map.put("dueDate", t.getDueDate());
            map.put("aiSuggestion", t.getAiSuggestion());
            map.put("createTime", t.getCreateTime());
            map.put("completed", t.getCompleted());
            map.put("completedAt", t.getCompletedAt());
            map.put("completionSummary", t.getCompletionSummary());

            if (t.getAssigneeId() != null) {
                User user = userMapper.selectById(t.getAssigneeId());
                map.put("assigneeName", user != null ? user.getRealName() : "未知");
            } else {
                map.put("assigneeName", null);
            }

            result.add(map);
        }
        return result;
    }

    /**
     * 保存或更新任务
     * <p>
     * id 为空时执行"新增"操作，id 不为空时执行"更新"操作。
     * 使用 MyBatis-Plus 的 insert 和 updateById 方法。
     *
     * @param task 任务对象
     * @return 操作结果（true 表示成功）
     */
    public boolean saveTask(TaskInfo task) {
        if (task.getId() == null) {
            return taskInfoMapper.insert(task) > 0;
        } else {
            return taskInfoMapper.updateById(task) > 0;
        }
    }

    /**
     * 根据 id 删除任务
     *
     * @param id 要删除的任务 ID
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean deleteTask(Long id) {
        return taskInfoMapper.deleteById(id) > 0;
    }

    /**
     * 标记任务完成
     * <p>
     * 设置 completed=true, completedAt=当前时间, completionSummary=总结文本。
     * 同时自动创建一条 TaskLog 记录和一条 TaskSummary 记录。
     *
     * @param taskId  任务 ID
     * @param summary 完成总结
     * @return 操作结果（true 表示成功）
     */
    public boolean completeTask(Long taskId, String summary) {
        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在，ID: " + taskId);
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新任务完成状态
        task.setCompleted(true);
        task.setCompletedAt(now);
        task.setCompletionSummary(summary);
        taskInfoMapper.updateById(task);

        // 创建任务日志
        TaskLog log = new TaskLog();
        log.setTaskId(taskId);
        log.setProgressPercent(100);
        log.setContent(summary != null ? summary : "任务已完成");
        log.setOperatorId(task.getAssigneeId() != null ? task.getAssigneeId() : task.getCreatorId());
        log.setCreateTime(now);
        taskLogMapper.insert(log);

        // 创建任务总结
        TaskSummary taskSummary = new TaskSummary();
        taskSummary.setProjectId(task.getProjectId());
        taskSummary.setTaskId(taskId);
        taskSummary.setSummaryType("task");
        taskSummary.setContent(summary != null ? summary : "任务已完成");
        taskSummary.setCreatedBy(task.getAssigneeId() != null ? task.getAssigneeId() : task.getCreatorId());
        taskSummary.setCreateTime(now);
        taskSummaryMapper.insert(taskSummary);

        return true;
    }

    /**
     * 撤销完成
     * <p>
     * 将 completed 重置为 false，清空 completedAt 和 completionSummary。
     *
     * @param taskId 任务 ID
     * @return 操作结果（true 表示成功）
     */
    public boolean uncompleteTask(Long taskId) {
        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在，ID: " + taskId);
        }

        task.setCompleted(false);
        task.setCompletedAt(null);
        task.setCompletionSummary(null);
        taskInfoMapper.updateById(task);

        return true;
    }

    /**
     * 获取项目进度
     * <p>
     * 返回指定项目的任务总数和已完成数。
     *
     * @param projectId 项目 ID
     * @return 包含 totalTasks, completedTasks, progressPercent 的 Map
     */
    public Map<String, Object> getProjectProgress(Long projectId) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<TaskInfo> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(TaskInfo::getProjectId, projectId);
        long totalTasks = taskInfoMapper.selectCount(totalWrapper);

        LambdaQueryWrapper<TaskInfo> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(TaskInfo::getProjectId, projectId)
                .eq(TaskInfo::getCompleted, true);
        long completedTasks = taskInfoMapper.selectCount(completedWrapper);

        int progressPercent = totalTasks > 0 ? (int) Math.round((double) completedTasks / totalTasks * 100) : 0;

        result.put("totalTasks", totalTasks);
        result.put("completedTasks", completedTasks);
        result.put("progressPercent", progressPercent);

        return result;
    }
}
