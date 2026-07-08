package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目实体类 —— 对应数据库中的 project_info 表
 * <p>
 * 【新手必读】
 * 项目是任务管理的核心组织单位。一个项目包含多个任务，
 * 例如："开发校园二手交易平台"就是一个项目，
 * 它下面可以拆分出"设计数据库"、"编写后端接口"等多个任务。
 */
@TableName("project_info")
public class ProjectInfo {

    /**
     * 项目唯一 ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称（如：校园二手交易平台开发）
     */
    private String name;

    /**
     * 项目详细描述（说明项目目标和范围）
     */
    private String description;

    /**
     * 项目状态（如：planning=规划中, in_progress=进行中, completed=已完成）
     */
    private String status;

    /**
     * 项目优先级（如：high=高, medium=中, low=低）
     */
    private String priority;

    /**
     * 项目负责人 ID（关联 user 表的 id）
     */
    private Long ownerId;

    /**
     * 项目开始日期
     */
    private LocalDate startDate;

    /**
     * 项目结束日期（截止日期）
     */
    private LocalDate endDate;

    /**
     * 项目创建时间
     */
    private LocalDateTime createTime;

    // ==================== Getter 和 Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
