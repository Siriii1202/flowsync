package hgc.flowsyncapi.dto;

/**
 * AI 拆解方案中的单个任务项 DTO
 * <p>
 * 包含了任务标题、描述、优先级、截止日期和负责人等信息。
 */
public class AiTaskPlanItem {

    private String title;

    private String description;

    private String priority;

    /**
     * 建议截止日期（格式：YYYY-MM-DD）
     */
    private String dueDate;

    private Long assigneeId;

    public AiTaskPlanItem() {
    }

    public AiTaskPlanItem(String title, String description, String priority,
                          String dueDate, Long assigneeId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assigneeId = assigneeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
