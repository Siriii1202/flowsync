package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.dto.AiTaskPlanImportRequest;
import hgc.flowsyncapi.dto.AiTaskPlanItem;
import hgc.flowsyncapi.dto.AiTaskPlanRequest;
import hgc.flowsyncapi.dto.AiTaskPlanResponse;
import hgc.flowsyncapi.dto.AiTaskSuggestionRequest;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.service.QwenService;
import hgc.flowsyncapi.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 智能控制器 —— 处理 AI 助手相关请求
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private QwenService qwenService;

    @Autowired
    private TaskInfoService taskInfoService;

    /**
     * AI 拆解项目 —— 将项目目标拆解为具体的任务列表
     */
    @PostMapping("/generate")
    public ApiResponse<AiTaskPlanResponse> generatePlan(@RequestBody AiTaskPlanRequest request) {
        AiTaskPlanResponse response = qwenService.generateTaskPlan(request);
        if (response == null || response.getItems().isEmpty()) {
            return ApiResponse.error("AI 拆解失败，请稍后重试");
        }
        return ApiResponse.success(response);
    }

    /**
     * 一键导入 AI 拆解方案 —— 将 AI 生成的任务列表批量保存到数据库
     */
    @PostMapping("/import")
    public ApiResponse<?> importTaskPlan(@RequestBody AiTaskPlanImportRequest request) {
        for (AiTaskPlanItem item : request.getItems()) {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setProjectId(request.getProjectId());
            taskInfo.setTitle(item.getTitle());
            taskInfo.setDescription(item.getDescription());
            taskInfo.setPriority(item.getPriority());
            taskInfo.setAssigneeId(item.getAssigneeId());
            taskInfo.setCreatorId(request.getCreatorId());
            taskInfo.setStatus("todo");
            taskInfo.setCreateTime(LocalDateTime.now());

            // 设置截止日期（从 AI 方案中获取）
            String dueDateStr = item.getDueDate();
            if (dueDateStr != null && !dueDateStr.isEmpty()) {
                try {
                    taskInfo.setDueDate(LocalDate.parse(dueDateStr));
                } catch (Exception e) {
                    // 日期格式不合法，跳过
                }
            }

            taskInfoService.saveTask(taskInfo);
        }
        return ApiResponse.success();
    }
}