package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 任务控制器 —— 处理任务管理相关请求
 * <p>
 * 【新手必读】
 * 任务是项目中最小的可执行单元。每个项目下可以包含多个任务，
 * 比如"设计数据库"、"编写登录接口"等。这个控制器提供了
 * 任务的查询、创建和删除功能。
 * <p>
 * ┌──────────────────────────────────────────────────────────────┐
 * │ 接口列表：                                                    │
 * │ GET    /api/tasks              — 获取任务列表（可按项目 ID 筛选）│
 * │ POST   /api/tasks              — 创建新任务                    │
 * │ DELETE /api/tasks/{id}         — 删除指定任务                  │
 * │ PUT    /api/tasks/{id}/complete   — 完成任务                   │
 * │ PUT    /api/tasks/{id}/uncomplete — 撤销完成                   │
 * │ GET    /api/tasks/progress/{projectId}  — 获取项目进度         │
 * └──────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    /**
     * 任务信息服务 —— 处理任务相关的业务逻辑
     */
    @Autowired
    private TaskInfoService taskInfoService;

    /**
     * 获取任务列表
     * <p>
     * 通过请求头 userId 过滤，只返回该用户能看到的任务。
     * 如果 userId 不存在则返回空列表。
     * 同时兼容原有的 projectId 筛选参数。
     *
     * @param projectId 项目 ID（可选参数，传了则只查该项目下的任务）
     * @param userId    当前用户 ID（从请求头获取）
     * @return 统一响应结果，包含任务列表
     */
    @GetMapping
    public ApiResponse<?> listTasks(@RequestParam(required = false) Long projectId,
                                    @RequestHeader(value = "userId", required = false) Long userId) {
        // 如果指定了 projectId，直接按项目查询
        if (projectId != null) {
            return ApiResponse.success(taskInfoService.listTasks(projectId));
        }
        // 否则按用户过滤
        if (userId == null) {
            return ApiResponse.success(java.util.List.of());
        }
        return ApiResponse.success(taskInfoService.getTasksByUserId(userId));
    }

    /**
     * 创建新任务
     * <p>
     * 【接口说明】
     * 前端在"新建任务"页面填写任务信息（标题、描述、负责人、截止日期等）后，
     * 调用此接口将任务保存到数据库。
     * <p>
     * 【请求方式】POST
     * 【请求路径】/api/tasks
     * 【请求参数】@RequestBody TaskInfo — 任务信息的 JSON 对象
     * 【返回结果】ApiResponse.success() — 创建成功无返回数据
     *
     * @param taskInfo 前端传入的任务信息
     * @return 统一响应结果
     */
    @PostMapping
    public ApiResponse<?> saveTask(@RequestBody TaskInfo taskInfo) {
        taskInfoService.saveTask(taskInfo);
        return ApiResponse.success();
    }

    /**
     * 删除指定任务
     * <p>
     * 【接口说明】
     * 根据任务 ID 删除对应的任务。如果该任务有子任务，
     * 子任务也会被一并删除。
     * <p>
     * 【请求方式】DELETE
     * 【请求路径】/api/tasks/{id}
     * 【请求参数】@PathVariable Long id — 要删除的任务 ID（从 URL 路径中获取）
     * 【返回结果】ApiResponse.success() — 删除成功无返回数据
     *
     * @param id 要删除的任务 ID
     * @return 统一响应结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteTask(@PathVariable Long id) {
        taskInfoService.deleteTask(id);
        return ApiResponse.success();
    }

    /**
     * 完成任务
     * <p>
     * 【接口说明】
     * 标记指定任务为已完成，可附带完成总结。
     * <p>
     * 【请求方式】PUT
     * 【请求路径】/api/tasks/{id}/complete
     * 【请求参数】@RequestBody Map<String, String> body — 包含 summary 字段
     * 【返回结果】ApiResponse.success()
     *
     * @param id   任务 ID
     * @param body 请求体，包含 summary 字段
     * @return 统一响应结果
     */
    @PutMapping("/{id}/complete")
    public ApiResponse<?> completeTask(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String summary = body != null ? body.get("summary") : null;
        taskInfoService.completeTask(id, summary);
        return ApiResponse.success();
    }

    /**
     * 撤销完成
     * <p>
     * 【接口说明】
     * 将指定任务从已完成状态恢复为未完成状态。
     * <p>
     * 【请求方式】PUT
     * 【请求路径】/api/tasks/{id}/uncomplete
     * 【返回结果】ApiResponse.success()
     *
     * @param id 任务 ID
     * @return 统一响应结果
     */
    @PutMapping("/{id}/uncomplete")
    public ApiResponse<?> uncompleteTask(@PathVariable Long id) {
        taskInfoService.uncompleteTask(id);
        return ApiResponse.success();
    }

    /**
     * 获取项目进度
     * <p>
     * 【接口说明】
     * 获取指定项目的任务完成进度统计。
     * <p>
     * 【请求方式】GET
     * 【请求路径】/api/tasks/progress/{projectId}
     * 【返回结果】ApiResponse.success(data) — data 包含 totalTasks, completedTasks, progressPercent
     *
     * @param projectId 项目 ID
     * @return 统一响应结果，包含进度数据
     */
    @GetMapping("/progress/{projectId}")
    public ApiResponse<?> getProjectProgress(@PathVariable Long projectId) {
        return ApiResponse.success(taskInfoService.getProjectProgress(projectId));
    }
}
