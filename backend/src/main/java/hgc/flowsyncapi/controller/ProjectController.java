package hgc.flowsyncapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.entity.ProjectMember;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.ProjectMemberMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目控制器 —— 处理项目管理相关请求
 * <p>
 * ┌──────────────────────────────────────────────────────────────┐
 * │ 接口列表：                                                    │
 * │ GET    /api/projects       — 获取项目列表（按用户过滤）        │
 * │ POST   /api/projects       — 创建/更新项目                   │
 * │ DELETE /api/projects/{id}  — 删除指定项目                     │
 * └──────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectInfoService projectInfoService;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取项目列表
     * <p>
     * 管理员（leader 角色）可见所有项目，普通用户只返回其参与的项目。
     * 如果 userId 为空则返回空列表。
     *
     * @param userId 当前用户 ID（从请求头获取）
     * @return 统一响应结果，包含项目列表
     */
    @GetMapping
    public ApiResponse<?> listProjects(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return ApiResponse.success(List.of());
        }
        // 检查用户是否为管理员（leader 角色）
        User user = userMapper.selectById(userId);
        if (user != null && "leader".equals(user.getRole())) {
            return ApiResponse.success(projectInfoService.getAllProjectsWithDetails());
        }
        return ApiResponse.success(projectInfoService.getProjectsByUserId(userId));
    }

    /**
     * 创建或更新项目
     * <p>
     * 接受包含项目信息和参与人 ID 列表的请求体。
     * 如果 id 为空则新建，否则更新。
     *
     * @param params 请求体，包含项目字段和 memberIds
     * @return 统一响应结果
     */
    @SuppressWarnings("unchecked")
    @PostMapping
    public ApiResponse<?> saveProject(@RequestBody Map<String, Object> params) {
        // 提取项目字段
        ProjectInfo project = new ProjectInfo();
        Object idObj = params.get("id");
        if (idObj != null) {
            project.setId(Long.valueOf(idObj.toString()));
        }
        project.setName((String) params.get("name"));
        project.setDescription((String) params.get("description"));
        project.setStatus((String) params.get("status"));
        project.setPriority((String) params.get("priority"));
        if (params.get("ownerId") != null) {
            project.setOwnerId(Long.valueOf(params.get("ownerId").toString()));
        }
        if (params.get("startDate") != null) {
            project.setStartDate(LocalDate.parse((String) params.get("startDate")));
        }
        if (params.get("endDate") != null) {
            project.setEndDate(LocalDate.parse((String) params.get("endDate")));
        }

        // 提取 memberIds
        List<Long> memberIds = null;
        if (params.get("memberIds") instanceof List) {
            memberIds = ((List<Object>) params.get("memberIds"))
                    .stream()
                    .map(v -> Long.valueOf(v.toString()))
                    .toList();
        }

        // 提取 leaderId（项目负责人）
        Long leaderId = null;
        if (params.get("leaderId") != null) {
            leaderId = Long.valueOf(params.get("leaderId").toString());
        }

        // 从请求参数中获取 userId（由拦截器附加到 params）
        Long userId = params.get("currentUserId") != null
                ? Long.valueOf(params.get("currentUserId").toString())
                : null;

        projectInfoService.saveProject(project, memberIds, leaderId, userId);
        return ApiResponse.success();
    }

    /**
     * 删除指定项目
     *
     * @param id 要删除的项目 ID
     * @return 统一响应结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteProject(@PathVariable Long id) {
        boolean deleted = projectInfoService.deleteProject(id);
        if (!deleted) {
            return ApiResponse.error("项目不存在或已被删除");
        }
        return ApiResponse.success();
    }

    /**
     * 获取指定项目的所有成员（含负责人和参与人）
     *
     * @param projectId 项目 ID
     * @return 统一响应结果，包含用户列表
     */
    @GetMapping("/{projectId}/members")
    public ApiResponse<?> getProjectMembers(@PathVariable Long projectId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        List<ProjectMember> members = projectMemberMapper.selectList(wrapper);
        if (members.isEmpty()) {
            return ApiResponse.success(new ArrayList<>());
        }
        List<Long> userIds = members.stream()
                .map(ProjectMember::getUserId)
                .collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        return ApiResponse.success(users);
    }
}
