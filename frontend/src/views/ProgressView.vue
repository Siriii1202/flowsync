<template>
  <div class="progress-page">
    <!-- 页面标题 -->
    <div class="page-header-accent">
      <div class="header-left">
        <el-icon :size="22"><TrendCharts /></el-icon>
        <h3>进度跟踪</h3>
        <span class="header-en">Progress Tracking</span>
      </div>
    </div>
    <div class="page-layout">
      <!-- 左侧项目列表 -->
      <div class="project-sidebar">
        <div class="sidebar-header">
          <h3>项目列表</h3>
        </div>
        <div v-if="loadingProjects" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>
        <el-menu
          v-else
          :default-active="activeProjectId?.toString()"
          @select="(index) => selectProject(Number(index))"
        >
          <el-menu-item v-for="p in projects" :key="p.id" :index="p.id.toString()">
            <div class="project-item">
              <span class="project-name">{{ p.name }}</span>
              <el-progress
                :percentage="p.progressPercent || 0"
                :stroke-width="6"
                :status="p.progressPercent >= 100 ? 'success' : undefined"
                style="width: 100%; margin-top: 4px;"
              />
              <span class="project-stat">{{ p.completedTasks }}/{{ p.totalTasks }} 任务</span>
            </div>
          </el-menu-item>
        </el-menu>
        <el-empty v-if="!loadingProjects && projects.length === 0" description="暂无项目" :image-size="80" />
      </div>

      <!-- 右侧任务详情 -->
      <div class="task-content">
        <template v-if="activeProject">
          <div class="content-header">
            <h3>{{ activeProject.name }}</h3>
            <el-progress
              :percentage="activeProject.progressPercent || 0"
              :status="activeProject.progressPercent >= 100 ? 'success' : undefined"
              :stroke-width="12"
              style="width: 300px;"
            >
              <span class="progress-text">{{ activeProject.completedTasks }}/{{ activeProject.totalTasks }} ({{ activeProject.progressPercent }}%)</span>
            </el-progress>
          </div>

          <div v-if="loadingTasks" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>

          <el-table v-else :data="tasks" border stripe style="width: 100%">
            <el-table-column prop="title" label="任务标题" min-width="160" />
            <el-table-column label="完成状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.completed" type="success" size="small">已完成</el-tag>
                <el-tag v-else type="info" size="small">未完成</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="完成总结" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.completionSummary">{{ row.completionSummary }}</span>
                <span v-else style="color: #c0c4cc;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="assigneeName" label="负责人" width="100" />
            <el-table-column prop="dueDate" label="截止日期" width="120" />
          </el-table>

          <el-empty v-if="!loadingTasks && tasks.length === 0" description="该项目暂无任务" />
        </template>

        <div v-else class="no-project-selected">
          <el-empty description="请从左侧选择一个项目查看进度" :image-size="120" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getProjects } from '../api/project'
import { getTasks } from '../api/task'
import { TrendCharts } from '@element-plus/icons-vue'
import { getOverview } from '../api/overview'

const loadingProjects = ref(true)
const loadingTasks = ref(false)
const projects = ref([])
const tasks = ref([])
const activeProjectId = ref(null)

const activeProject = computed(() => {
  if (!activeProjectId.value) return null
  return projects.value.find(p => p.id === activeProjectId.value) || null
})

function selectProject(projectId) {
  activeProjectId.value = projectId
  fetchTasks(projectId)
}

async function fetchTasks(projectId) {
  loadingTasks.value = true
  try {
    const res = await getTasks(projectId)
    tasks.value = res.data || []
  } catch (error) {
    console.error('获取任务列表失败:', error)
  } finally {
    loadingTasks.value = false
  }
}

async function fetchProjects() {
  loadingProjects.value = true
  try {
    // 从 overview 接口获取含进度的项目列表
    const res = await getOverview()
    projects.value = res.data?.projects || []
    // 默认选中第一个项目
    if (projects.value.length > 0) {
      activeProjectId.value = projects.value[0].id
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
  } finally {
    loadingProjects.value = false
  }
}

onMounted(async () => {
  await fetchProjects()
  if (activeProjectId.value) {
    await fetchTasks(activeProjectId.value)
  }
})
</script>

<style scoped>
.progress-page {
  padding: 4px;
  height: calc(100vh - 120px);
}

.page-layout {
  display: flex;
  height: 100%;
  gap: 16px;
}

.project-sidebar {
  width: 280px;
  min-width: 280px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06);
  overflow-y: auto;
  padding: 0;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #F3F4F6;
}

.project-sidebar .el-menu {
  border-right: none;
}

.project-sidebar .el-menu-item {
  height: auto;
  line-height: normal;
  padding: 8px 16px;
  align-items: flex-start;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.project-item {
  display: flex;
  flex-direction: column;
  width: 100%;
  padding: 4px 0;
}

.project-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 2px;
}

.project-stat {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.task-content {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06);
  padding: 20px;
  overflow-y: auto;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F3F4F6;
}

.content-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.progress-text {
  font-size: 13px;
  color: #606266;
}

.no-project-selected {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 8px;
}
</style>
