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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目服务 —— 处理项目管理相关的业务逻辑
 * <p>
 * 【新手必读】
 * 这个 Service 负责所有与"项目"相关的业务操作，包括：
 * - 查询项目列表（支持按条件排序）
 * - 保存或更新项目（新增或修改）
 * - 删除项目
 * <p>
 * 项目是任务管理的顶层单位，一个项目下可以包含多个任务。
 */
@Service
public class ProjectInfoService {

    /**
     * 项目 Mapper，用于操作 project_info 表
     */
    private final ProjectInfoMapper projectInfoMapper;

    private final ProjectMemberMapper projectMemberMapper;

    private final UserMapper userMapper;

    private final TaskInfoMapper taskInfoMapper;

    private final TaskSummaryMapper taskSummaryMapper;

    /**
     * 构造方法注入
     */
    public ProjectInfoService(ProjectInfoMapper projectInfoMapper, ProjectMemberMapper projectMemberMapper, UserMapper userMapper, TaskInfoMapper taskInfoMapper, TaskSummaryMapper taskSummaryMapper) {
        this.projectInfoMapper = projectInfoMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.userMapper = userMapper;
        this.taskInfoMapper = taskInfoMapper;
        this.taskSummaryMapper = taskSummaryMapper;
    }

    /**
     * 查询所有项目，按 id 倒序排列
     *
     * @return 项目列表（按 id 从大到小排列）
     */
    public List<ProjectInfo> listProjects() {
        LambdaQueryWrapper<ProjectInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectInfo::getId);
        return projectInfoMapper.selectList(wrapper);
    }

    /**
     * 查询所有项目（带完整详情），管理员可见全部
     * <p>
     * 与 getProjectsByUserId 返回格式一致，但不按用户过滤，
     * 返回系统中所有项目的完整信息（含负责人、参与人列表等）。
     *
     * @return Map 列表，包含所有 ProjectInfo 字段 + leaderName + memberIds + memberNames
     */
    public List<Map<String, Object>> getAllProjectsWithDetails() {
        LambdaQueryWrapper<ProjectInfo> projectWrapper = new LambdaQueryWrapper<>();
        projectWrapper.orderByDesc(ProjectInfo::getId);
        List<ProjectInfo> projectList = projectInfoMapper.selectList(projectWrapper);

        if (projectList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> projectIds = projectList.stream()
                .map(ProjectInfo::getId)
                .collect(Collectors.toList());

        // 查询所有项目的所有成员
        LambdaQueryWrapper<ProjectMember> allMembersWrapper = new LambdaQueryWrapper<>();
        allMembersWrapper.in(ProjectMember::getProjectId, projectIds);
        List<ProjectMember> allMembers = projectMemberMapper.selectList(allMembersWrapper);

        // 按 projectId 分组
        Map<Long, List<ProjectMember>> membersByProject = allMembers.stream()
                .collect(Collectors.groupingBy(ProjectMember::getProjectId));

        // 查询所有 leader 的 userId
        Map<Long, Long> projectLeaderMap = allMembers.stream()
                .filter(m -> "leader".equals(m.getRole()))
                .collect(Collectors.toMap(ProjectMember::getProjectId, ProjectMember::getUserId, (a, b) -> a));

        // 查询所有相关用户的 real_name
        List<Long> allUserIds = allMembers.stream()
                .map(ProjectMember::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(allUserIds);
            for (User u : users) {
                userNameMap.put(u.getId(), u.getRealName());
            }
        }

        // 组装返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProjectInfo p : projectList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("status", p.getStatus());
            map.put("priority", p.getPriority());
            map.put("ownerId", p.getOwnerId());
            map.put("startDate", p.getStartDate());
            map.put("endDate", p.getEndDate());
            map.put("createTime", p.getCreateTime());

            Long leaderUserId = projectLeaderMap.get(p.getId());
            map.put("leaderId", leaderUserId);
            map.put("leaderName", leaderUserId != null ? userNameMap.get(leaderUserId) : null);

            // 添加成员信息
            List<ProjectMember> projectMembers = membersByProject.getOrDefault(p.getId(), List.of());
            List<Long> memberIds = projectMembers.stream()
                    .map(ProjectMember::getUserId)
                    .collect(Collectors.toList());
            map.put("memberIds", memberIds);

            List<String> memberNames = projectMembers.stream()
                    .map(m -> userNameMap.get(m.getUserId()))
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            map.put("memberNames", memberNames);

            result.add(map);
        }
        return result;
    }

    /**
     * 根据用户 ID 查询该用户参与的所有项目，并附带负责人姓名
     * <p>
     * 通过 project_member 表先查出用户所在的 projectId 列表，再查询项目信息，
     * 最后组装每个项目的 leaderName（负责人在 sys_user 表中的 real_name）。
     *
     * @param userId 用户 ID
     * @return Map 列表，包含所有 ProjectInfo 字段 + leaderName
     */
    public List<Map<String, Object>> getProjectsByUserId(Long userId) {
        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getUserId, userId);
        List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);
        if (members.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> projectIds = members.stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<ProjectInfo> projectWrapper = new LambdaQueryWrapper<>();
        projectWrapper.in(ProjectInfo::getId, projectIds);
        projectWrapper.orderByDesc(ProjectInfo::getId);
        List<ProjectInfo> projectList = projectInfoMapper.selectList(projectWrapper);

        // 查询所有项目的所有成员（含负责人和参与人）
        LambdaQueryWrapper<ProjectMember> allMembersWrapper = new LambdaQueryWrapper<>();
        allMembersWrapper.in(ProjectMember::getProjectId, projectIds);
        List<ProjectMember> allMembers = projectMemberMapper.selectList(allMembersWrapper);

        // 按 projectId 分组
        Map<Long, List<ProjectMember>> membersByProject = allMembers.stream()
                .collect(Collectors.groupingBy(ProjectMember::getProjectId));

        // 查询所有 leader 的 userId
        Map<Long, Long> projectLeaderMap = allMembers.stream()
                .filter(m -> "leader".equals(m.getRole()))
                .collect(Collectors.toMap(ProjectMember::getProjectId, ProjectMember::getUserId, (a, b) -> a));

        // 查询所有相关用户的 real_name
        List<Long> allUserIds = allMembers.stream()
                .map(ProjectMember::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(allUserIds);
            for (User u : users) {
                userNameMap.put(u.getId(), u.getRealName());
            }
        }

        // 组装返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProjectInfo p : projectList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("status", p.getStatus());
            map.put("priority", p.getPriority());
            map.put("ownerId", p.getOwnerId());
            map.put("startDate", p.getStartDate());
            map.put("endDate", p.getEndDate());
            map.put("createTime", p.getCreateTime());

            Long leaderUserId = projectLeaderMap.get(p.getId());
            map.put("leaderId", leaderUserId);
            map.put("leaderName", leaderUserId != null ? userNameMap.get(leaderUserId) : null);

            // 添加成员信息
            List<ProjectMember> projectMembers = membersByProject.getOrDefault(p.getId(), List.of());
            List<Long> memberIds = projectMembers.stream()
                    .map(ProjectMember::getUserId)
                    .collect(Collectors.toList());
            map.put("memberIds", memberIds);

            List<String> memberNames = projectMembers.stream()
                    .map(m -> userNameMap.get(m.getUserId()))
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            map.put("memberNames", memberNames);

            result.add(map);
        }
        return result;
    }

    /**
     * 创建项目并添加负责人和参与人
     * <p>
     * 将 leaderId（或创建者）添加为 leader，memberIds 中的用户添加为 member。
     *
     * @param project   项目对象
     * @param memberIds 参与人 ID 列表
     * @param leaderId  负责人 ID（若为 null 则使用 userId）
     * @param userId    创建者 ID
     * @return 创建后的项目
     */
    public ProjectInfo createProject(ProjectInfo project, List<Long> memberIds, Long leaderId, Long userId) {
        projectInfoMapper.insert(project);
        Long projectId = project.getId();

        // 添加负责人（leader）
        Long actualLeaderId = leaderId != null ? leaderId : userId;
        ProjectMember leader = new ProjectMember();
        leader.setProjectId(projectId);
        leader.setUserId(actualLeaderId);
        leader.setRole("leader");
        projectMemberMapper.insert(leader);

        // 添加创建者（如果与 leader 不同，也加入为 member）
        if (userId != null && !userId.equals(actualLeaderId)) {
            ProjectMember creator = new ProjectMember();
            creator.setProjectId(projectId);
            creator.setUserId(userId);
            creator.setRole("member");
            projectMemberMapper.insert(creator);
        }

        // 添加参与人
        if (memberIds != null) {
            for (Long memberId : memberIds) {
                if (!memberId.equals(actualLeaderId) && !memberId.equals(userId)) {
                    ProjectMember member = new ProjectMember();
                    member.setProjectId(projectId);
                    member.setUserId(memberId);
                    member.setRole("member");
                    projectMemberMapper.insert(member);
                }
            }
        }

        return project;
    }

    /**
     * 保存或更新项目
     * <p>
     * 如果 id 为空则新增（会自动构建 ProjectMember），
     * 如果 id 不为空则更新项目信息并重新设置参与人。
     *
     * @param project   项目对象
     * @param memberIds 参与人 ID 列表
     * @param leaderId  负责人 ID
     * @param userId    当前用户 ID（新增时使用）
     * @return 操作结果（true 表示成功）
     */
    public boolean saveProject(ProjectInfo project, List<Long> memberIds, Long leaderId, Long userId) {
        if (project.getId() == null) {
            // id 为空 → 新增
            createProject(project, memberIds, leaderId, userId);
            return true;
        } else {
            // id 不为空 → 更新
            projectInfoMapper.updateById(project);

            // 先处理负责人变更（放在成员处理之前，避免旧负责人被重复添加）
            if (leaderId != null) {
                // 清除新负责人在该项目下的所有旧记录（无论角色）
                LambdaQueryWrapper<ProjectMember> cleanupWrapper = new LambdaQueryWrapper<>();
                cleanupWrapper.eq(ProjectMember::getProjectId, project.getId())
                        .eq(ProjectMember::getUserId, leaderId);
                projectMemberMapper.delete(cleanupWrapper);

                // 查找现有负责人记录
                LambdaQueryWrapper<ProjectMember> leaderWrapper = new LambdaQueryWrapper<>();
                leaderWrapper.eq(ProjectMember::getProjectId, project.getId())
                        .eq(ProjectMember::getRole, "leader");
                ProjectMember existingLeader = projectMemberMapper.selectOne(leaderWrapper);
                if (existingLeader != null) {
                    // 负责人变更：将现有 leader 记录的 userId 更新为新负责人
                    existingLeader.setUserId(leaderId);
                    projectMemberMapper.updateById(existingLeader);
                } else {
                    // 没有负责人（理论上不会发生），直接新增
                    ProjectMember newLeader = new ProjectMember();
                    newLeader.setProjectId(project.getId());
                    newLeader.setUserId(leaderId);
                    newLeader.setRole("leader");
                    projectMemberMapper.insert(newLeader);
                }
            }

            // 再处理参与人
            if (memberIds != null) {
                // 删除旧的 member 角色参与人（保留 leader）
                LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ProjectMember::getProjectId, project.getId())
                       .ne(ProjectMember::getRole, "leader");
                projectMemberMapper.delete(wrapper);
                // 添加新的参与人（跳过负责人，避免重复）
                for (Long memberId : memberIds) {
                    if (!memberId.equals(leaderId)) {
                        ProjectMember member = new ProjectMember();
                        member.setProjectId(project.getId());
                        member.setUserId(memberId);
                        member.setRole("member");
                        projectMemberMapper.insert(member);
                    }
                }
            }
            return true;
        }
    }

    /**
     * 根据 id 删除项目（级联删除关联的任务、总结和成员）
     *
     * @param id 要删除的项目 ID
     * @return 删除成功返回 true，失败返回 false（如记录不存在）
     */
    @Transactional
    public boolean deleteProject(Long id) {
        // 级联删除该项目下的所有任务
        LambdaQueryWrapper<TaskInfo> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(TaskInfo::getProjectId, id);
        taskInfoMapper.delete(taskWrapper);

        // 级联删除该项目下的所有总结
        LambdaQueryWrapper<TaskSummary> summaryWrapper = new LambdaQueryWrapper<>();
        summaryWrapper.eq(TaskSummary::getProjectId, id);
        taskSummaryMapper.delete(summaryWrapper);

        // 删除项目成员
        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getProjectId, id);
        projectMemberMapper.delete(memberWrapper);

        // 删除项目本身
        int rows = projectInfoMapper.deleteById(id);
        if (rows <= 0) {
            throw new RuntimeException("项目不存在或已被删除，事务回滚");
        }
        return true;
    }
}
