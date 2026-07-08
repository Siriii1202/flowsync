package hgc.flowsyncapi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.entity.ProjectMember;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.mapper.ProjectMemberMapper;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.mapper.TaskSummaryMapper;
import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

/**
 * FlowSync 协同任务管理系统 - 主启动类
 */
@SpringBootApplication
@MapperScan("hgc.flowsyncapi.mapper")
public class FlowSyncApiApplication {

    /**
     * 设置默认时区为 Asia/Shanghai，确保 LocalDate.now() 等日期操作使用正确时区
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public static void main(String[] args) {
        SpringApplication.run(FlowSyncApiApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  FlowSync API 启动成功！");
        System.out.println("  Swagger 文档: http://localhost:8080/swagger-ui.html");
        System.out.println("==========================================");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 启动时清理已删除项目的孤立数据
     */
    @Bean
    public ApplicationRunner cleanupOrphanedRecords(ProjectMemberMapper projectMemberMapper,
                                                     TaskInfoMapper taskInfoMapper,
                                                     TaskSummaryMapper taskSummaryMapper) {
        return args -> {
            try {
                // 清理已删除项目的孤立成员记录
                LambdaQueryWrapper<ProjectMember> orphanMembers = new LambdaQueryWrapper<>();
                orphanMembers.notInSql(ProjectMember::getProjectId, "SELECT id FROM project_info");
                int deletedMembers = projectMemberMapper.delete(orphanMembers);
                if (deletedMembers > 0) {
                    System.out.println("  清理孤立成员记录: " + deletedMembers + " 条");
                }

                // 清理已删除项目的孤立任务记录
                LambdaQueryWrapper<TaskInfo> orphanTasks = new LambdaQueryWrapper<>();
                orphanTasks.notInSql(TaskInfo::getProjectId, "SELECT id FROM project_info");
                int deletedTasks = taskInfoMapper.delete(orphanTasks);
                if (deletedTasks > 0) {
                    System.out.println("  清理孤立任务记录: " + deletedTasks + " 条");
                }

                // 清理已删除项目的孤立总结记录
                LambdaQueryWrapper<TaskSummary> orphanSummaries = new LambdaQueryWrapper<>();
                orphanSummaries.notInSql(TaskSummary::getProjectId, "SELECT id FROM project_info");
                int deletedSummaries = taskSummaryMapper.delete(orphanSummaries);
                if (deletedSummaries > 0) {
                    System.out.println("  清理孤立总结记录: " + deletedSummaries + " 条");
                }
            } catch (Exception e) {
                System.err.println("  清理孤立数据时出错: " + e.getMessage());
            }
        };
    }
}
