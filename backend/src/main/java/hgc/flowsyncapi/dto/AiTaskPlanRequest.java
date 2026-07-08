package hgc.flowsyncapi.dto;

/**
 * AI 任务拆解方案请求 DTO
 * <p>
 * 【新手必读】
 * 当用户有一个大项目时，可以让 AI 帮忙将项目拆解成多个具体任务。
 * 比如用户说"我要开发一个博客系统"，AI 可以自动拆解为：
 * - 设计数据库表结构
 * - 编写用户注册登录功能
 * - 实现文章发布功能
 * - 等等……
 * <p>
 * 这个 DTO 封装了传递给 AI 所需的项目信息。
 */
public class AiTaskPlanRequest {

    /**
     * 项目 ID（如果项目已存在，传此 ID 用于关联）
     */
    private Long projectId;

    /**
     * 操作人 ID（谁发起的拆解请求）
     */
    private Long operatorId;

    /**
     * 项目名称（如：校园二手交易平台）
     */
    private String projectName;

    /**
     * 项目目标（如：实现一个让同学们可以买卖二手物品的平台）
     */
    private String goal;

    /**
     * 项目详细描述
     */
    private String description;

    // ==================== 构造方法 ====================

    public AiTaskPlanRequest() {
    }

    // ==================== Getter 和 Setter ====================

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
