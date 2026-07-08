package hgc.flowsyncapi.dto;

import java.util.List;

/**
 * AI 任务拆解方案响应 DTO
 * <p>
 * 【新手必读】
 * 这个类封装了 AI 返回的完整拆解方案。
 * 包含一段汇总描述（summary）和一系列具体的任务项列表（items）。
 * <p>
 * 前端收到这个响应后，可以将拆解方案展示给用户，
 * 用户确认后可以调用"一键导入"接口，将方案中的任务批量创建到系统中。
 */
public class AiTaskPlanResponse {

    /**
     * AI 生成的方案汇总描述（如：本项目建议分为 5 个阶段完成...）
     */
    private String summary;

    /**
     * AI 拆解出的具体任务列表
     */
    private List<AiTaskPlanItem> items;

    // ==================== 构造方法 ====================

    public AiTaskPlanResponse() {
    }

    public AiTaskPlanResponse(String summary, List<AiTaskPlanItem> items) {
        this.summary = summary;
        this.items = items;
    }

    // ==================== Getter 和 Setter ====================

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<AiTaskPlanItem> getItems() {
        return items;
    }

    public void setItems(List<AiTaskPlanItem> items) {
        this.items = items;
    }
}
