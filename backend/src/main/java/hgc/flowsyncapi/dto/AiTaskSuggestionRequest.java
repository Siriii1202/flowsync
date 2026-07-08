package hgc.flowsyncapi.dto;

/**
 * AI 任务建议请求 DTO
 * <p>
 * 【新手必读】
 * 当用户创建一个新任务时，可以调用 AI 接口获取智能建议。
 * 比如用户创建了一个"开发登录功能"的任务，AI 可能会建议
 * "注意密码加密"、"考虑使用 JWT Token"等。
 * <p>
 * 这个 DTO 封装了传递给 AI 所需的上下文信息。
 */
public class AiTaskSuggestionRequest {

    /**
     * 项目名称（AI 需要知道任务所属项目的背景）
     */
    private String projectName;

    /**
     * 任务标题（AI 根据标题给出建议）
     */
    private String taskTitle;

    /**
     * 任务描述（AI 根据详细描述给出更精准的建议）
     */
    private String description;

    // ==================== 构造方法 ====================

    public AiTaskSuggestionRequest() {
    }

    public AiTaskSuggestionRequest(String projectName, String taskTitle, String description) {
        this.projectName = projectName;
        this.taskTitle = taskTitle;
        this.description = description;
    }

    // ==================== Getter 和 Setter ====================

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
