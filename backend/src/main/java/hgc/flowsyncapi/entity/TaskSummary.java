package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 任务总结实体类 —— 对应数据库中的 task_summary 表
 * <p>
 * 【新手必读】
 * 任务总结用于记录对任务或项目的"阶段性总结"。
 * 比如：当一个任务完成后，负责人可以写一段总结，
 * 回顾完成过程中遇到的问题、积累的经验等。
 * <p>
 * summaryType 字段可以区分是"任务总结"还是"项目总结"，
 * 方便在查看时进行分类展示。
 */
@TableName("task_summary")
public class TaskSummary {

    /**
     * 总结唯一 ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的项目 ID（关联 project_info 表的 id）
     */
    private Long projectId;

    /**
     * 关联的任务 ID（关联 task_info 表的 id，如果是项目总结可以为空）
     */
    private Long taskId;

    /**
     * 总结类型（如：task=任务总结, project=项目总结）
     */
    private String summaryType;

    /**
     * 总结内容（详细的文字总结）
     */
    private String content;

    /**
     * 创建人 ID（谁写的总结，关联 user 表）
     */
    private Long createdBy;

    /**
     * 总结创建时间
     */
    private LocalDateTime createTime;

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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(String summaryType) {
        this.summaryType = summaryType;
    }

    // 兼容前端传 type 字段
    public void setType(String type) {
        this.summaryType = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
