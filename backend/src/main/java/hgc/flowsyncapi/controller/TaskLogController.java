package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskLog;
import hgc.flowsyncapi.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务日志控制器 —— 处理任务日志相关请求
 * <p>
 * 【新手必读】
 * 任务日志用于记录任务的"进度更新"。比如一个任务完成了 50%，
 * 负责人可以写一条日志来说明当前进展。这样项目管理者就能
 * 随时了解每个任务的最新状态。
 * <p>
 * ┌──────────────────────────────────────────────────────────────┐
 * │ 接口列表：                                                    │
 * │ GET  /api/task-logs       — 获取日志列表（可按任务 ID 筛选）    │
 * │ POST /api/task-logs       — 创建新日志                        │
 * └──────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/task-logs")
public class TaskLogController {

    /**
     * 任务日志服务 —— 处理任务日志相关的业务逻辑
     */
    @Autowired
    private TaskLogService taskLogService;

    /**
     * 获取任务日志列表
     * <p>
     * 【接口说明】
     * 查询任务日志列表。如果不传 taskId，则返回所有日志；
     * 如果传了 taskId，则只返回指定任务的日志（按时间倒序排列）。
     * <p>
     * 【请求方式】GET
     * 【请求路径】/api/task-logs?taskId=xxx
     * 【请求参数】
     *   - taskId（可选）: Long 类型，按任务 ID 筛选日志
     * 【返回结果】ApiResponse.success(data) — data 为日志列表（List&lt;TaskLog&gt;）
     *
     * @param taskId 任务 ID（可选参数，传了则只查该任务的日志）
     * @return 统一响应结果，包含日志列表
     */
    @GetMapping
    public ApiResponse<?> listTaskLogs(@RequestParam(required = false) Long taskId) {
        return ApiResponse.success(taskLogService.listTaskLogs(taskId));
    }

    /**
     * 创建新日志
     * <p>
     * 【接口说明】
     * 当任务的进度有更新时，调用此接口记录一条日志。
     * 日志包含进度百分比、备注内容和操作人等信息。
     * <p>
     * 【请求方式】POST
     * 【请求路径】/api/task-logs
     * 【请求参数】@RequestBody TaskLog — 日志信息的 JSON 对象
     * 【返回结果】ApiResponse.success() — 创建成功无返回数据
     *
     * @param taskLog 前端传入的日志信息
     * @return 统一响应结果
     */
    @PostMapping
    public ApiResponse<?> saveTaskLog(@RequestBody TaskLog taskLog) {
        taskLogService.saveTaskLog(taskLog);
        return ApiResponse.success();
    }
}
