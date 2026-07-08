package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务实体类 —— 对应数据库中的 task_info 表
 * <p>
 * 【新手必读】
 * 任务是项目中最小的可执行单元。一个任务就是一件具体要做的事情，
 * 比如"编写用户注册接口"就是一个任务。任务可以设置负责人、截止日期等。
 * <p>
 * 任务支持"父子层级"：如果 parentId 不为空，表示这个任务是某个
 * 父任务的子任务，用于实现任务的拆解和细化。
 */
@TableName("task_info")
public class TaskInfo {

    /**
     * 任务唯一 ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目 ID（关联 project_info 表的 id）
     */
    private Long projectId;

    /**
     * 父任务 ID（为空表示是顶层任务；不为空表示是某个任务的子任务）
     */
    private Long parentId;

    /**
     * 任务标题（简洁描述任务内容）
     */
    private String title;

    /**
     * 任务详细描述
     */
    private String description;

    /**
     * 负责人 ID（关联 user 表的 id，谁负责完成这个任务）
     */
    private Long assigneeId;

    /**
     * 创建人 ID（关联 user 表的 id，谁创建了这个任务）
     */
    private Long creatorId;

    /**
     * 任务状态（如：todo=待办, in_progress=进行中, done=已完成）
     */
    private String status;

    /**
     * 任务优先级（如：high=高, medium=中, low=低）
     */
    private String priority;

    /**
     * 截止日期（哪天之前必须完成）
     */
    private LocalDate dueDate;

    /**
     * AI 建议（由千问 AI 生成的智能建议，如风险提示、资源建议等）
     */
    private String aiSuggestion;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否完成（false=未完成, true=已完成）
     */
    private Boolean completed;

    /**
     * 完成时间（任务完成时的时间戳）
     */
    private LocalDateTime completedAt;

    /**
     * 完成总结（任务完成时的总结文本）
     */
    private String completionSummary;

    // ==================== Getter 和 Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getAiSuggestion() {
        return aiSuggestion;
    }

    public void setAiSuggestion(String aiSuggestion) {
        this.aiSuggestion = aiSuggestion;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getCompletionSummary() {
        return completionSummary;
    }

    public void setCompletionSummary(String completionSummary) {
        this.completionSummary = completionSummary;
    }
}
