package hgc.flowsyncapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.dto.PasswordUpdateRequest;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.entity.ProjectMember;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.ProjectInfoMapper;
import hgc.flowsyncapi.mapper.ProjectMemberMapper;
import hgc.flowsyncapi.mapper.UserMapper;
import hgc.flowsyncapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户控制器 —— 处理用户管理相关请求
 * <p>
 * 【新手必读】
 * 用户管理模块负责管理系统中的所有用户账号，
 * 包括查看用户列表和修改密码等功能。
 * <p>
 * ┌──────────────────────────────────────────────────────────────┐
 * │ 接口列表：                                                    │
 * │ GET  /api/users              — 获取所有用户列表               │
 * │ POST /api/users/update-password  — 修改用户密码               │
 * └──────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 用户服务 —— 处理用户相关的业务逻辑
     */
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    /**
     * 获取所有用户列表
     * <p>
     * 【接口说明】
     * 查询并返回系统中所有注册用户的列表。
     * 前端在分配任务负责人、查看团队成员等场景下会调用此接口。
     * <p>
     * 【请求方式】GET
     * 【请求路径】/api/users
     * 【返回结果】ApiResponse.success(data) — data 为用户列表（List&lt;User&gt;）
     *
     * @return 统一响应结果，包含用户列表
     */
    @GetMapping
    public ApiResponse<?> listUsers() {
        return ApiResponse.success(userService.listUsers());
    }

    /**
     * 修改用户密码
     * <p>
     * 【接口说明】
     * 用户可以在个人设置中修改自己的登录密码。
     * 需要提供旧密码进行身份验证，验证通过后更新为新密码。
     * <p>
     * 【请求方式】POST
     * 【请求路径】/api/users/update-password
     * 【请求参数】@RequestBody PasswordUpdateRequest — 包含 userId、oldPassword、newPassword
     * 【返回结果】
     *   - 成功：ApiResponse.success()
     *   - 失败（如旧密码错误）：ApiResponse.error(message)
     *
     * @param request 密码修改请求数据（用户 ID、旧密码、新密码）
     * @return 统一响应结果
     */
    @PostMapping("/update-password")
    public ApiResponse<?> updatePassword(@RequestBody PasswordUpdateRequest request) {
        // 调用 UserService 的 updatePassword 方法修改密码
        // 该方法内部会校验旧密码是否正确
        boolean success = userService.updatePassword(request);
        if (!success) {
            return ApiResponse.error("密码修改失败，请检查旧密码是否正确");
        }
        return ApiResponse.success();
    }

    /**
     * 获取所有用户及其项目信息
     * <p>
     * 返回每个用户的详细信息，包含该用户在 project_member 表中的所有记录
     * （项目ID、项目名、角色）以及系统角色。
     *
     * @return 统一响应结果，包含用户及其项目列表
     */
    @GetMapping("/with-projects")
    public ApiResponse<?> listUsersWithProjects(@RequestHeader(value = "userId", required = false) Long userId) {
        // 查询所有用户
        List<User> users = userMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();

        // 判断当前用户是否为管理员，非管理员只能看到自己参与的项目
        boolean isAdmin = false;
        Set<Long> allowedProjectIds = null;
        if (userId != null) {
            User currentUser = userMapper.selectById(userId);
            isAdmin = currentUser != null && "leader".equals(currentUser.getRole());
            if (!isAdmin) {
                // 查询当前用户参与的所有项目
                LambdaQueryWrapper<ProjectMember> curMemberWrapper = new LambdaQueryWrapper<>();
                curMemberWrapper.eq(ProjectMember::getUserId, userId);
                List<ProjectMember> curMembers = projectMemberMapper.selectList(curMemberWrapper);
                allowedProjectIds = curMembers.stream()
                        .map(ProjectMember::getProjectId)
                        .collect(Collectors.toSet());
            }
        }

        for (User user : users) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("realName", user.getRealName());
            userData.put("role", user.getRole());
            userData.put("phone", user.getPhone());
            userData.put("email", user.getEmail());

            // 查询该用户在 project_member 中的记录
            LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(ProjectMember::getUserId, user.getId());
            List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);

            // 组装项目列表，过滤掉已删除项目及非管理员不可见的项目
            List<Map<String, Object>> projects = new ArrayList<>();
            for (ProjectMember member : members) {
                // 非管理员只显示当前用户参与的项目
                if (allowedProjectIds != null && !allowedProjectIds.contains(member.getProjectId())) {
                    continue;
                }
                ProjectInfo projectInfo = projectInfoMapper.selectById(member.getProjectId());
                if (projectInfo == null) {
                    // 项目已被删除，清理悬空的 project_member 记录
                    projectMemberMapper.deleteById(member.getId());
                    continue;
                }
                Map<String, Object> projectData = new HashMap<>();
                projectData.put("projectId", member.getProjectId());
                projectData.put("projectName", projectInfo.getName());
                projectData.put("role", member.getRole());
                projects.add(projectData);
            }
            userData.put("projects", projects);

            result.add(userData);
        }

        return ApiResponse.success(result);
    }
}
