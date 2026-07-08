package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 任务日志实体类 —— 对应数据库中的 task_log 表
 * <p>
 * 【新手必读】
 * 任务日志用于记录某个任务的"进度更新"或"重要操作"。
 * 比如：张三完成了任务进度的 50%，并写了一条备注说明当前进展，
 * 这条记录就会保存到 task_log 表中。
 * <p>
 * 通过查看日志，项目负责人可以随时了解每个任务的最新进展。
 */
@TableName("task_log")
public class TaskLog {

    /**
     * 日志唯一 ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的任务 ID（关联 task_info 表的 id）
     */
    private Long taskId;

    /**
     * 进度百分比（0-100 之间的整数，表示完成了百分之多少）
     */
    private Integer progressPercent;

    /**
     * 日志内容（描述本次更新的具体内容或备注）
     */
    private String content;

    /**
     * 操作人 ID（谁记录了这条日志，关联 user 表）
     */
    private Long operatorId;

    /**
     * 日志创建时间
     */
    private LocalDateTime createTime;

    // ==================== Getter 和 Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
