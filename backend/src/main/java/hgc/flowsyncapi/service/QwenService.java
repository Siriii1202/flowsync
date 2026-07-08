package hgc.flowsyncapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hgc.flowsyncapi.dto.AiTaskPlanItem;
import hgc.flowsyncapi.dto.AiTaskPlanRequest;
import hgc.flowsyncapi.dto.AiTaskPlanResponse;
import hgc.flowsyncapi.dto.AiTaskSuggestionRequest;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.mapper.ProjectInfoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI 服务 —— 接入 DeepSeek 大模型的智能服务
 * <p>
 * 通过 DeepSeek 的 OpenAI 兼容接口 (https://api.deepseek.com/v1/chat/completions)
 * 提供智能建议和任务拆解功能。
 */
@Service
public class QwenService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ProjectInfoMapper projectInfoMapper;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

    public QwenService(RestTemplate restTemplate, ObjectMapper objectMapper, ProjectInfoMapper projectInfoMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.projectInfoMapper = projectInfoMapper;
    }

    /**
     * 获取 AI 对任务的智能建议
     */
    public String getTaskSuggestion(AiTaskSuggestionRequest request) {
        String prompt = "你是一个项目管理助手。请为以下任务提供建议。\n"
            + "项目名称：" + request.getProjectName() + "\n"
            + "任务标题：" + request.getTaskTitle() + "\n"
            + "任务描述：" + request.getDescription() + "\n"
            + "请给出具体的建议（包括可能的风险、需要的资源、注意事项等）。";

        String response = callDeepSeek(prompt);
        if (response == null) {
            return "【AI 建议】\n"
                + "针对任务「" + request.getTaskTitle() + "」（所属项目：" + request.getProjectName() + "），建议如下：\n\n"
                + "1. 风险提示：建议在开发前充分调研相关技术方案，避免技术选型失误。\n"
                + "2. 资源建议：建议安排 2-3 名开发人员配合完成，并预留 20% 的缓冲时间应对突发问题。\n"
                + "3. 注意事项：注意与项目中其他任务的接口对接，确保模块间的兼容性。\n"
                + "4. 最佳实践：建议采用迭代开发方式，每完成一个子功能就进行测试验证。\n";
        }
        return "【AI 建议】\n" + response;
    }

    /**
     * 生成 AI 任务拆解方案
     */
    public AiTaskPlanResponse generateTaskPlan(AiTaskPlanRequest request) {
        if (request == null) {
            return buildFallbackPlan();
        }

        // 通过 projectId 从数据库查询项目信息（名称、日期范围）
        String projectName = request.getProjectName();
        LocalDate projectStartDate = null;
        LocalDate projectEndDate = null;
        if (request.getProjectId() != null) {
            ProjectInfo project = projectInfoMapper.selectById(request.getProjectId());
            if (project != null) {
                projectName = project.getName();
                projectStartDate = project.getStartDate();
                projectEndDate = project.getEndDate();
            }
        }
        // 仍然没有项目名，返回兜底方案
        if (projectName == null || projectName.isBlank()) {
            return buildFallbackPlan();
        }

        String goal = request.getGoal() != null ? request.getGoal() : "";
        String description = request.getDescription() != null ? request.getDescription() : "";

        // 构建日期范围提示
        String dateRangeHint = "";
        if (projectStartDate != null && projectEndDate != null) {
            dateRangeHint = "项目时间范围：" + projectStartDate + " 至 " + projectEndDate + "。"
                + "请确保每个任务的截止日期在这个范围内。\n";
        } else if (projectEndDate != null) {
            dateRangeHint = "项目截止日期：" + projectEndDate + "。请确保所有任务的截止日期不晚于此日期。\n";
        }

        String prompt = "你是一个专业的项目管理助手。请将以下项目拆解成具体的任务列表。\n"
            + "项目名称：" + projectName + "\n"
            + (goal.isEmpty() ? "" : "项目目标：" + goal + "\n")
            + (description.isEmpty() ? "" : "项目描述：" + description + "\n")
            + dateRangeHint
            + "\n请将项目拆解为 3-5 个具体任务，每个任务包含：标题、详细描述、优先级(high/medium/low)、截止日期（格式：YYYY-MM-DD，根据项目时间范围合理分配）。\n"
            + "请以 JSON 格式返回，格式为：{\"summary\": \"汇总描述\", \"items\": [{\"title\": \"...\", \"description\": \"...\", \"priority\": \"high\", \"dueDate\": \"2026-07-15\"}]}\n"
            + "只返回 JSON，不要包含 Markdown 代码块标记。";

        String response = callDeepSeek(prompt);
        if (response != null) {
            try {
                // 尝试从响应中提取 JSON
                String jsonStr = extractJson(response);
                if (jsonStr != null) {
                    JsonNode root = objectMapper.readTree(jsonStr);
                    String summary = root.has("summary") ? root.get("summary").asText() : "";
                    List<AiTaskPlanItem> items = new ArrayList<>();
                    if (root.has("items")) {
                        for (JsonNode itemNode : root.get("items")) {
                            String title = itemNode.has("title") ? itemNode.get("title").asText() : "";
                            String desc = itemNode.has("description") ? itemNode.get("description").asText() : "";
                            String pri = itemNode.has("priority") ? itemNode.get("priority").asText() : "medium";
                            String dueDate = itemNode.has("dueDate") ? itemNode.get("dueDate").asText() : "";
                            // 钳制日期到项目时间范围内，防止 AI 返回超出范围的日期
                            if ((projectStartDate != null || projectEndDate != null) && !dueDate.isEmpty()) {
                                dueDate = clampDueDate(dueDate, projectStartDate, projectEndDate);
                            }
                            items.add(new AiTaskPlanItem(title, desc, pri, dueDate, 1L));
                        }
                    }
                    if (!items.isEmpty()) {
                        return new AiTaskPlanResponse(summary, items);
                    }
                }
            } catch (Exception e) {
                // 解析失败，使用默认拆解
            }
        }

        // 兜底：使用项目名生成默认拆解
        return generateDefaultPlan(projectName, goal, projectStartDate, projectEndDate);
    }

    /**
     * 调用 DeepSeek API
     */
    private String callDeepSeek(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", "你是一个专业的项目管理助手。"),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.7,
            "max_tokens", 2000
        );

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("choices").get(0).path("message").path("content").asText(null);
            }
        } catch (Exception e) {
            // API 调用失败，返回 null 以便调用方使用兜底方案
            System.err.println("DeepSeek API 调用失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从文本中提取 JSON 字符串
     */
    private String extractJson(String text) {
        if (text == null) return null;
        // 尝试找到第一个 { 和最后一个 }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    /**
     * 将日期字符串钳制到项目时间范围内
     * 如果日期早于项目开始日期，则设为项目开始日期
     * 如果日期晚于项目结束日期，则设为项目结束日期
     */
    private String clampDueDate(String dueDateStr, LocalDate projectStartDate, LocalDate projectEndDate) {
        if (dueDateStr == null || dueDateStr.isBlank()) return dueDateStr;
        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            if (projectStartDate != null && dueDate.isBefore(projectStartDate)) {
                dueDate = projectStartDate;
            }
            if (projectEndDate != null && dueDate.isAfter(projectEndDate)) {
                dueDate = projectEndDate;
            }
            return dueDate.toString();
        } catch (Exception e) {
            return dueDateStr;
        }
    }

    /**
     * 使用项目名生成默认拆解方案
     */
    private AiTaskPlanResponse generateDefaultPlan(String projectName, String goal,
                                                    LocalDate projectStartDate, LocalDate projectEndDate) {
        LocalDate today = LocalDate.now();
        // 确定基准日期：如果项目有开始日期且晚于今天，则用项目开始日期；否则用今天
        LocalDate baseDate = today;
        if (projectStartDate != null && projectStartDate.isAfter(today)) {
            baseDate = projectStartDate;
        }
        // 确定项目总天数，用于按比例分配任务截止日期
        long totalDays = 30; // 默认30天
        if (projectEndDate != null) {
            totalDays = java.time.temporal.ChronoUnit.DAYS.between(baseDate, projectEndDate);
            if (totalDays < 1) totalDays = 30;
        }

        String summary = "【AI 拆解方案】\n"
            + "针对项目「" + projectName + "」，AI 建议按照以下 " + 4 + " 个阶段逐步推进：\n"
            + (goal.isEmpty() ? "" : "项目目标：" + goal + "\n")
            + "每个阶段均包含明确的目标和交付物，建议按顺序执行。\n";

        List<AiTaskPlanItem> items = new ArrayList<>();

        items.add(new AiTaskPlanItem(
            projectName + " - 需求分析与规划",
            "深入分析项目需求，明确功能模块划分，编写需求文档。\n输出物：需求规格说明书",
            "high",
            baseDate.plusDays(Math.max(1, totalDays / 10)).toString(),
            1L
        ));

        items.add(new AiTaskPlanItem(
            projectName + " - 系统设计与架构",
            "完成系统架构设计、数据库设计、接口设计。\n输出物：设计文档、ER图、接口规范",
            "high",
            baseDate.plusDays(Math.max(3, totalDays * 3 / 10)).toString(),
            1L
        ));

        items.add(new AiTaskPlanItem(
            projectName + " - 核心功能开发",
            "按照设计文档进行核心功能开发，包括前后端编码实现。\n输出物：可运行的代码",
            "high",
            baseDate.plusDays(Math.max(7, totalDays * 7 / 10)).toString(),
            2L
        ));

        // 最后一项使用 totalDays，确保不超过项目结束日期
        long lastTaskDays = Math.min(totalDays, Math.max(10, totalDays));
        items.add(new AiTaskPlanItem(
            projectName + " - 测试与部署",
            "编写测试用例，执行功能测试和集成测试，部署上线。\n输出物：测试报告、部署文档",
            "medium",
            baseDate.plusDays(lastTaskDays).toString(),
            2L
        ));

        return new AiTaskPlanResponse(summary, items);
    }

    /**
     * 构建兜底方案
     */
    public AiTaskPlanResponse buildFallbackPlan() {
        String summary = "【标准拆解方案】\n"
            + "由于 AI 服务暂时不可用，这里提供一个通用的项目拆解方案：\n"
            + "按照「准备资料 -> 执行主体 -> 检查总结」的标准流程推进。\n";

        List<AiTaskPlanItem> items = new ArrayList<>();
        LocalDate today = LocalDate.now();
        // 兜底方案默认按30天项目周期分配
        long defaultDays = 30;

        items.add(new AiTaskPlanItem(
            "准备资料与前期调研",
            "收集项目所需的相关资料和参考案例，进行技术调研和可行性分析。",
            "high",
            today.plusDays(Math.max(2, defaultDays / 15)).toString(),
            1L
        ));

        items.add(new AiTaskPlanItem(
            "执行主体工作",
            "按照项目计划执行核心工作内容，定期进行进度检查和调整。",
            "high",
            today.plusDays(Math.max(7, defaultDays * 7 / 10)).toString(),
            2L
        ));

        items.add(new AiTaskPlanItem(
            "检查与总结",
            "对完成的工作进行检查验收，编写总结报告，记录经验教训。",
            "medium",
            today.plusDays(defaultDays).toString(),
            2L
        ));

        return new AiTaskPlanResponse(summary, items);
    }
}
