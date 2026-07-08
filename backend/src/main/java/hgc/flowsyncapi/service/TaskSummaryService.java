package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.entity.ProjectMember;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.ProjectInfoMapper;
import hgc.flowsyncapi.mapper.ProjectMemberMapper;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.mapper.TaskSummaryMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务总结服务 —— 处理任务/项目总结相关的业务逻辑
 * <p>
 * 【新手必读】
 * 总结是对已完成任务或项目的回顾和反思。通过写总结可以：
 * - 记录完成过程中遇到的问题和解决方案
 * - 分享经验和教训
 * - 为后续项目提供参考
 * <p>
 * summaryType 字段用于区分是"任务总结"还是"项目总结"。
 */
@Service
public class TaskSummaryService {

    private final TaskSummaryMapper taskSummaryMapper;
    private final ProjectInfoMapper projectInfoMapper;
    private final TaskInfoMapper taskInfoMapper;
    private final UserMapper userMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public TaskSummaryService(TaskSummaryMapper taskSummaryMapper,
                              ProjectInfoMapper projectInfoMapper,
                              TaskInfoMapper taskInfoMapper,
                              UserMapper userMapper,
                              ProjectMemberMapper projectMemberMapper) {
        this.taskSummaryMapper = taskSummaryMapper;
        this.projectInfoMapper = projectInfoMapper;
        this.taskInfoMapper = taskInfoMapper;
        this.userMapper = userMapper;
        this.projectMemberMapper = projectMemberMapper;
    }

    /**
     * 查询所有总结（含关联信息），按 id 倒序排列
     */
    public List<Map<String, Object>> listSummaries() {
        LambdaQueryWrapper<TaskSummary> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TaskSummary::getId);
        List<TaskSummary> list = taskSummaryMapper.selectList(wrapper);
        return enrichSummaries(list);
    }

    /**
     * 查询总结列表（按用户参与项目过滤），管理员可见全部
     * <p>
     * 非管理员只能看到自己参与项目的总结。
     *
     * @param userId 当前用户 ID（为 null 时返回全部）
     * @return 过滤后的总结列表
     */
    public List<Map<String, Object>> listSummaries(Long userId) {
        LambdaQueryWrapper<TaskSummary> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TaskSummary::getId);

        // 如果 userId 不为空且用户不是管理员，则按参与项目过滤
        if (userId != null) {
            User user = userMapper.selectById(userId);
            // 非管理员（leader）需要过滤
            if (user == null || !"leader".equals(user.getRole())) {
                // 查询用户参与的所有项目
                LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
                memberWrapper.eq(ProjectMember::getUserId, userId);
                List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);
                if (members.isEmpty()) {
                    return new ArrayList<>();
                }
                List<Long> projectIds = members.stream()
                        .map(ProjectMember::getProjectId)
                        .collect(Collectors.toList());
                wrapper.in(TaskSummary::getProjectId, projectIds);
            }
        }

        List<TaskSummary> list = taskSummaryMapper.selectList(wrapper);
        return enrichSummaries(list);
    }

    /**
     * 按项目 ID 查询总结（含关联信息）
     */
    public List<Map<String, Object>> listSummariesByProject(Long projectId) {
        LambdaQueryWrapper<TaskSummary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskSummary::getProjectId, projectId);
        wrapper.orderByDesc(TaskSummary::getId);
        List<TaskSummary> list = taskSummaryMapper.selectList(wrapper);
        return enrichSummaries(list);
    }

    /**
     * 将总结列表补充关联信息（项目名、任务名、创建人）
     */
    private List<Map<String, Object>> enrichSummaries(List<TaskSummary> summaries) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TaskSummary s : summaries) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("projectId", s.getProjectId());
            map.put("taskId", s.getTaskId());
            map.put("createdBy", s.getCreatedBy());

            // 总结类型映射：task → 任务总结, project → 项目总结，其他原样
            String rawType = s.getSummaryType();
            if ("task".equals(rawType)) {
                map.put("type", "任务总结");
            } else if ("project".equals(rawType)) {
                map.put("type", "项目总结");
            } else {
                map.put("type", rawType);
            }

            map.put("content", s.getContent());
            map.put("createdAt", s.getCreateTime());

            // 查项目名
            ProjectInfo project = projectInfoMapper.selectById(s.getProjectId());
            map.put("projectName", project != null ? project.getName() : "未知项目");

            // 查任务名
            if (s.getTaskId() != null) {
                TaskInfo task = taskInfoMapper.selectById(s.getTaskId());
                map.put("taskName", task != null ? task.getTitle() : "未知任务");
            } else {
                map.put("taskName", null);
            }

            // 查创建人
            if (s.getCreatedBy() != null) {
                User user = userMapper.selectById(s.getCreatedBy());
                map.put("creatorName", user != null ? user.getRealName() : "未知");
            } else {
                map.put("creatorName", "未知");
            }

            result.add(map);
        }
        return result;
    }

    /**
     * 新增总结
     */
    public boolean saveSummary(TaskSummary summary) {
        return taskSummaryMapper.insert(summary) > 0;
    }

    /**
     * 更新总结
     */
    public boolean updateSummary(TaskSummary summary) {
        return taskSummaryMapper.updateById(summary) > 0;
    }

    /**
     * 删除总结
     */
    public boolean deleteSummary(Long id) {
        return taskSummaryMapper.deleteById(id) > 0;
    }
}
