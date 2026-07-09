package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.entity.ProjectMember;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.*;
import hgc.flowsyncapi.service.ProjectInfoService;
import hgc.flowsyncapi.service.TaskInfoService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 概览统计服务 —— 提供系统整体的统计数据
 * <p>
 * 【新手必读】
 * 概览功能通常用在系统首页或仪表盘上，用数字直观展示系统的整体情况。
 * 管理员可以一目了然地看到系统有多少用户、多少项目、多少任务等。
 * <p>
 * 这个 Service 同时注入了多个 Mapper，因为它需要从多张表中统计数据。
 */
@Service
public class OverviewService {

    /**
     * 用户 Mapper
     */
    private final UserMapper userMapper;

    /**
     * 项目 Mapper
     */
    private final ProjectInfoMapper projectInfoMapper;

    /**
     * 任务 Mapper
     */
    private final TaskInfoMapper taskInfoMapper;

    /**
     * 任务总结 Mapper
     */
    private final TaskSummaryMapper taskSummaryMapper;

    /**
     * 项目成员 Mapper
     */
    private final ProjectMemberMapper projectMemberMapper;

    /**
     * 任务服务
     */
    private final TaskInfoService taskInfoService;

    /**
     * 构造方法注入（多个 Mapper 通过构造参数一次性注入）
     */
    public OverviewService(UserMapper userMapper, ProjectInfoMapper projectInfoMapper,
                           TaskInfoMapper taskInfoMapper, TaskSummaryMapper taskSummaryMapper,
                           ProjectMemberMapper projectMemberMapper, TaskInfoService taskInfoService) {
        this.userMapper = userMapper;
        this.projectInfoMapper = projectInfoMapper;
        this.taskInfoMapper = taskInfoMapper;
        this.taskSummaryMapper = taskSummaryMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.taskInfoService = taskInfoService;
    }

    /**
     * 获取系统概览统计数据
     * <p>
     * userId 为空时返回全局统计；不为空时返回该用户能看到的统计。
     * <p>
     * 返回的 Map 包含以下字段：
     * - userCount：用户总数
     * - projectCount：项目总数（全局）或用户参与的项目数
     * - taskCount：任务总数（全局）或用户能看到的任务数
     * - summaryCount：总结总数（全局）或用户能看到的总结数
     * - projects：项目列表（含进度数据）
     *
     * @param userId 用户 ID（可选，为空时返回全局统计）
     * @return 包含各项统计数据的 Map
     */
    public Map<String, Object> getOverview(Long userId) {
        Map<String, Object> overview = new HashMap<>();

        // 用户数始终为全局统计
        overview.put("userCount", userMapper.selectCount(null));

        if (userId == null) {
            // 全局统计
            overview.put("projectCount", projectInfoMapper.selectCount(null));

            // 只统计属于现有项目的任务（排除已删除项目遗留的孤立任务）
            LambdaQueryWrapper<TaskInfo> taskFilter = new LambdaQueryWrapper<>();
            taskFilter.inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
            overview.put("taskCount", taskInfoMapper.selectCount(taskFilter));

            LambdaQueryWrapper<TaskSummary> summaryFilter = new LambdaQueryWrapper<>();
            summaryFilter.inSql(TaskSummary::getProjectId, "SELECT id FROM project_info");
            overview.put("summaryCount", taskSummaryMapper.selectCount(summaryFilter));

            // 全部待完成任务（未完成的任务）
            LambdaQueryWrapper<TaskInfo> pendingFilter = new LambdaQueryWrapper<>();
            pendingFilter.eq(TaskInfo::getCompleted, false)
                    .inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
            overview.put("pendingTasks", taskInfoMapper.selectCount(pendingFilter));

            // 今日截止任务数（全局）- 只统计属于现有项目的任务
            LocalDate today = LocalDate.now();
            LambdaQueryWrapper<TaskInfo> globalDueToday = new LambdaQueryWrapper<>();
            globalDueToday.eq(TaskInfo::getDueDate, today)
                    .inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
            overview.put("todayDueTasks", taskInfoMapper.selectCount(globalDueToday));

            // 今日待完成任务（截止日=今天 且 未完成）- 只统计属于现有项目的任务
            LambdaQueryWrapper<TaskInfo> todayPendingQ = new LambdaQueryWrapper<>();
            todayPendingQ.eq(TaskInfo::getDueDate, today)
                    .eq(TaskInfo::getCompleted, false)
                    .inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
            overview.put("todayPendingTasks", taskInfoMapper.selectCount(todayPendingQ));

            // 逾期任务（截止日<今天 且 未完成）- 只统计属于现有项目的任务
            LambdaQueryWrapper<TaskInfo> overdueQ = new LambdaQueryWrapper<>();
            overdueQ.lt(TaskInfo::getDueDate, today)
                    .eq(TaskInfo::getCompleted, false)
                    .inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
            overview.put("overdueTasks", taskInfoMapper.selectCount(overdueQ));

            // 项目进度列表（全局）
            List<ProjectInfo> projects = projectInfoMapper.selectList(null);
            overview.put("projects", buildProjectProgressList(projects));
        } else {
            // 查询用户系统角色
            User user = userMapper.selectById(userId);
            boolean isLeader = user != null && "leader".equals(user.getRole());

            if (isLeader) {
                // 系统角色为 leader，返回全局统计（不加过滤）
                overview.put("projectCount", projectInfoMapper.selectCount(null));

                // 只统计属于现有项目的任务（排除孤立任务）
                LambdaQueryWrapper<TaskInfo> leaderTaskFilter = new LambdaQueryWrapper<>();
                leaderTaskFilter.inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
                overview.put("taskCount", taskInfoMapper.selectCount(leaderTaskFilter));

                LambdaQueryWrapper<TaskSummary> leaderSummaryFilter = new LambdaQueryWrapper<>();
                leaderSummaryFilter.inSql(TaskSummary::getProjectId, "SELECT id FROM project_info");
                overview.put("summaryCount", taskSummaryMapper.selectCount(leaderSummaryFilter));

                // 全部待完成任务（自己负责的未完成的任务）
                LambdaQueryWrapper<TaskInfo> leaderPendingFilter = new LambdaQueryWrapper<>();
                leaderPendingFilter.eq(TaskInfo::getAssigneeId, userId)
                        .eq(TaskInfo::getCompleted, false)
                        .inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
                overview.put("pendingTasks", taskInfoMapper.selectCount(leaderPendingFilter));

                // 今日截止任务数（全局）- 只统计属于现有项目的任务
                LocalDate today = LocalDate.now();
                LambdaQueryWrapper<TaskInfo> dueTodayWrapper = new LambdaQueryWrapper<>();
                dueTodayWrapper.eq(TaskInfo::getDueDate, today)
                        .inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
                overview.put("todayDueTasks", taskInfoMapper.selectCount(dueTodayWrapper));

                // 今日待完成任务（截止日=今天 且 未完成 且 本人负责）
                LambdaQueryWrapper<TaskInfo> todayPendingQ = new LambdaQueryWrapper<>();
                todayPendingQ.eq(TaskInfo::getAssigneeId, userId)
                        .eq(TaskInfo::getDueDate, today)
                        .eq(TaskInfo::getCompleted, false)
                        .inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
                overview.put("todayPendingTasks", taskInfoMapper.selectCount(todayPendingQ));

                // 逾期任务（自己负责的）
                LambdaQueryWrapper<TaskInfo> overdueQ = new LambdaQueryWrapper<>();
                overdueQ.eq(TaskInfo::getAssigneeId, userId)
                        .lt(TaskInfo::getDueDate, today)
                        .eq(TaskInfo::getCompleted, false)
                        .inSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
                overview.put("overdueTasks", taskInfoMapper.selectCount(overdueQ));

                List<ProjectInfo> projects = projectInfoMapper.selectList(null);
                overview.put("projects", buildProjectProgressList(projects));

                return overview;
            }

            // 非 leader，按项目过滤
            // 查询用户参与的项目（排除已删除项目的孤立记录）
            LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(ProjectMember::getUserId, userId);
            List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);
            List<Long> projectIds = members.stream()
                    .map(ProjectMember::getProjectId)
                    .distinct()
                    .filter(pid -> projectInfoMapper.selectById(pid) != null) // 只保留存在的项目
                    .collect(Collectors.toList());

            // 项目数：用户参与的项目数量
            overview.put("projectCount", (long) projectIds.size());

            // 任务数：用户能看到的任务
            overview.put("taskCount", (long) taskInfoService.getTasksByUserId(userId).size());

            // 总结数：用户能看到的项目的总结数量
            if (projectIds.isEmpty()) {
                overview.put("summaryCount", 0L);
            } else {
                LambdaQueryWrapper<TaskSummary> summaryWrapper = new LambdaQueryWrapper<>();
                summaryWrapper.in(TaskSummary::getProjectId, projectIds);
                overview.put("summaryCount", taskSummaryMapper.selectCount(summaryWrapper));
            }

            // 今日截止任务数（用户可见项目）
            LocalDate today = LocalDate.now();
            if (projectIds.isEmpty()) {
                overview.put("pendingTasks", 0L);
                overview.put("todayDueTasks", 0L);
                overview.put("todayPendingTasks", 0L);
                overview.put("overdueTasks", 0L);
            } else {
                // 全部待完成任务（自己负责的任务）
                LambdaQueryWrapper<TaskInfo> pendingQ = new LambdaQueryWrapper<>();
                pendingQ.eq(TaskInfo::getAssigneeId, userId)
                        .in(TaskInfo::getProjectId, projectIds)
                        .eq(TaskInfo::getCompleted, false);
                overview.put("pendingTasks", taskInfoMapper.selectCount(pendingQ));

                LambdaQueryWrapper<TaskInfo> dueTodayWrapper = new LambdaQueryWrapper<>();
                dueTodayWrapper.in(TaskInfo::getProjectId, projectIds)
                        .eq(TaskInfo::getDueDate, today);
                overview.put("todayDueTasks", taskInfoMapper.selectCount(dueTodayWrapper));

                // 今日待完成任务（截止日=今天 且 未完成 且 本人负责）
                LambdaQueryWrapper<TaskInfo> todayPendingQ = new LambdaQueryWrapper<>();
                todayPendingQ.eq(TaskInfo::getAssigneeId, userId)
                        .in(TaskInfo::getProjectId, projectIds)
                        .eq(TaskInfo::getDueDate, today)
                        .eq(TaskInfo::getCompleted, false);
                overview.put("todayPendingTasks", taskInfoMapper.selectCount(todayPendingQ));

                // 逾期任务（自己负责的）
                LambdaQueryWrapper<TaskInfo> overdueQ = new LambdaQueryWrapper<>();
                overdueQ.eq(TaskInfo::getAssigneeId, userId)
                        .in(TaskInfo::getProjectId, projectIds)
                        .lt(TaskInfo::getDueDate, today)
                        .eq(TaskInfo::getCompleted, false);
                overview.put("overdueTasks", taskInfoMapper.selectCount(overdueQ));
            }

            // 项目进度列表（仅用户参与的项目）
            if (projectIds.isEmpty()) {
                overview.put("projects", new ArrayList<>());
            } else {
                LambdaQueryWrapper<ProjectInfo> projectWrapper = new LambdaQueryWrapper<>();
                projectWrapper.in(ProjectInfo::getId, projectIds);
                List<ProjectInfo> projects = projectInfoMapper.selectList(projectWrapper);
                overview.put("projects", buildProjectProgressList(projects));
            }
        }

        return overview;
    }

    /**
     * 构建项目进度列表
     */
    private List<Map<String, Object>> buildProjectProgressList(List<ProjectInfo> projects) {
        List<Map<String, Object>> projectProgressList = new ArrayList<>();
        for (ProjectInfo project : projects) {
            Map<String, Object> progress = new HashMap<>();
            progress.put("id", project.getId());
            progress.put("name", project.getName());

            long totalTasks = taskInfoMapper.selectCount(
                    new LambdaQueryWrapper<TaskInfo>().eq(TaskInfo::getProjectId, project.getId()));
            long completedTasks = taskInfoMapper.selectCount(
                    new LambdaQueryWrapper<TaskInfo>().eq(TaskInfo::getProjectId, project.getId())
                            .eq(TaskInfo::getCompleted, true));

            int progressPercent = totalTasks > 0 ? (int) Math.round((double) completedTasks / totalTasks * 100) : 0;

            progress.put("totalTasks", totalTasks);
            progress.put("completedTasks", completedTasks);
            progress.put("progressPercent", progressPercent);
            projectProgressList.add(progress);
        }
        return projectProgressList;
    }
}
