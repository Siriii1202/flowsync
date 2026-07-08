package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.service.TaskSummaryService;
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

/**
 * 任务总结控制器 —— 处理任务/项目总结相关请求
 * <p>
 * 【新手必读】
 * 总结用于记录任务或项目的"阶段性复盘"。比如一个迭代结束后，
 * 团队成员可以写一篇总结，回顾遇到的问题、积累的经验等。
 * 总结分为"任务总结"和"项目总结"两种类型，方便分类查看。
 * <p>
 * ┌──────────────────────────────────────────────────────────────┐
 * │ 接口列表：                                                    │
 * │ GET  /api/summaries  — 获取所有总结列表                       │
 * │ GET  /api/summaries?projectId=xxx  — 按项目筛选总结            │
 * │ POST /api/summaries  — 创建新总结                            │ │ PUT   /api/summaries/{id}  — 更新指定总结                        │
│ │ DELETE /api/summaries/{id}  — 删除指定总结                        │
│ └──────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/summaries")
public class TaskSummaryController {

    /**
     * 任务总结服务 —— 处理总结相关的业务逻辑
     */
    @Autowired
    private TaskSummaryService taskSummaryService;

    /**
     * 获取所有总结列表
     * <p>
     * 【接口说明】
     * 查询并返回系统中所有任务/项目总结的列表。
     * 前端可以在"总结查看"页面展示这些总结，方便团队成员回顾学习。
     * <p>
     * 【请求方式】GET
     * 【请求路径】/api/summaries 或 /api/summaries?projectId=xxx
     * 【请求参数】
     *   - projectId（可选）: Long 类型，按项目 ID 筛选总结
     * 【返回结果】ApiResponse.success(data) — data 为总结列表（List&lt;TaskSummary&gt;）
     *
     * @param projectId 项目 ID（可选参数）
     * @return 统一响应结果，包含总结列表
     */
    @GetMapping
    public ApiResponse<?> listSummaries(@RequestParam(required = false) Long projectId,
                                        @RequestHeader(value = "userId", required = false) Long userId) {
        if (projectId != null) {
            return ApiResponse.success(taskSummaryService.listSummariesByProject(projectId));
        }
        return ApiResponse.success(taskSummaryService.listSummaries(userId));
    }

    /**
     * 创建新总结
     * <p>
     * 【接口说明】
     * 任务完成或项目结束后，调用此接口创建一条总结记录。
     * 总结可以包含遇到的问题、解决方案、经验教训等内容。
     * <p>
     * 【请求方式】POST
     * 【请求路径】/api/summaries
     * 【请求参数】@RequestBody TaskSummary — 总结信息的 JSON 对象
     * 【返回结果】ApiResponse.success() — 创建成功无返回数据
     *
     * @param taskSummary 前端传入的总结信息
     * @return 统一响应结果
     */
    @PostMapping
    public ApiResponse<?> saveSummary(@RequestBody TaskSummary taskSummary) {
        taskSummaryService.saveSummary(taskSummary);
        return ApiResponse.success();
    }

    /**
     * 更新指定总结
     *
     * @param id          总结 ID
     * @param taskSummary 更新后的总结信息
     * @return 统一响应结果
     */
    @PutMapping("/{id}")
    public ApiResponse<?> updateSummary(@PathVariable Long id, @RequestBody TaskSummary taskSummary) {
        taskSummary.setId(id);
        taskSummaryService.updateSummary(taskSummary);
        return ApiResponse.success();
    }

    /**
     * 删除指定总结
     *
     * @param id 总结 ID
     * @return 统一响应结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteSummary(@PathVariable Long id) {
        taskSummaryService.deleteSummary(id);
        return ApiResponse.success();
    }
}
