package hgc.flowsyncapi.dto;

import java.util.List;

/**
 * AI 拆解方案一键导入请求 DTO
 * <p>
 * 【新手必读】
 * 当用户确认了 AI 生成的拆解方案后，前端会将用户确认后的
 * 任务列表通过这个 DTO 发送给后端，后端将其批量保存到数据库中。
 * <p>
 * 这样做的好处是：用户可以先在界面上调整 AI 给出的方案
 * （比如修改某个任务的优先级），确认无误后再一次性导入。
 */
public class AiTaskPlanImportRequest {

    /**
     * 目标项目 ID（任务将归属到这个项目下）
     */
    private Long projectId;

    /**
     * 创建人 ID（所有任务的创建者）
     */
    private Long creatorId;

    /**
     * 要导入的任务列表（用户确认后的最终版本）
     */
    private List<AiTaskPlanItem> items;

    // ==================== 构造方法 ====================

    public AiTaskPlanImportRequest() {
    }

    // ==================== Getter 和 Setter ====================

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public List<AiTaskPlanItem> getItems() {
        return items;
    }

    public void setItems(List<AiTaskPlanItem> items) {
        this.items = items;
    }
}
