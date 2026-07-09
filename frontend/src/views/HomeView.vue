<template>
  <div class="home-container">
    <!-- 左侧菜单 -->
    <el-aside width="220px" class="home-aside">
      <div class="aside-header">
        <div class="aside-logo">
          <el-icon :size="32"><Connection /></el-icon>
          <span class="aside-title">FlowSync</span>
        </div>
        <div class="aside-greeting"><span class="wave-icon">👋</span> hello, {{ user?.username || '用户' }}</div>
      </div>

      <el-menu
        :default-active="currentRoute"
        router
      >
        <!-- 工作台分组 -->
        <el-menu-item-group>
          <template #title>
            <span class="group-title">
              <span class="group-dot"></span>
              工作台
            </span>
          </template>
          <el-menu-item index="/overview">
            <el-icon><DataAnalysis /></el-icon>
            <span>总览</span>
          </el-menu-item>
        </el-menu-item-group>

        <!-- 业务管理分组 -->
        <el-menu-item-group>
          <template #title>
            <span class="group-title">
              <span class="group-dot"></span>
              业务管理
            </span>
          </template>
          <el-menu-item index="/projects">
            <el-icon><Folder /></el-icon>
            <span>项目管理</span>
          </el-menu-item>
          <!-- AI 任务拆解仅负责人可见 -->
          <el-menu-item v-if="user.role === 'leader' || hasProjectLeaderRole" index="/ai-tasks">
            <el-icon><MagicStick /></el-icon>
            <span>任务拆解</span>
          </el-menu-item>
          <el-menu-item index="/tasks">
            <el-icon><List /></el-icon>
            <span>任务管理</span>
          </el-menu-item>
          <el-menu-item index="/progress">
            <el-icon><Timer /></el-icon>
            <span>进度跟踪</span>
          </el-menu-item>
          <el-menu-item index="/summaries">
            <el-icon><Document /></el-icon>
            <span>总结中心</span>
          </el-menu-item>
        </el-menu-item-group>

        <!-- 系统信息分组 -->
        <el-menu-item-group>
          <template #title>
            <span class="group-title">
              <span class="group-dot"></span>
              系统信息
            </span>
          </template>
          <el-menu-item index="/members">
            <el-icon><UserFilled /></el-icon>
            <span>成员列表</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><Setting /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container>
      <el-main class="home-main">
        <div class="home-top-bar">
          <div class="task-count-text">
            <span class="count-text-pending">待完成 {{ pendingTasks }}</span>
            <span class="count-text-divider">|</span>
            <span class="count-text-overdue">逾期 {{ overdueTasks }}</span>
          </div>
        </div>
        <div class="today-due-banner" :class="todayPendingTasks > 0 ? 'banner-yellow' : 'banner-green'">
          <el-icon :size="16" style="margin-right: 6px;"><Timer /></el-icon>
          <span>今天是DDL的未完成项目还有 <strong>{{ todayPendingTasks }}</strong> 个</span>
        </div>
        <router-view />
      </el-main>
    </el-container>

    <!-- 浮动退出登录 -->
    <div class="logout-float">
      <el-button text @click="handleLogout">
        <el-icon :size="16"><SwitchButton /></el-icon>
        <span>退出登录</span>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getUser, removeUser } from '../utils/storage'
import {
  DataAnalysis, Folder, MagicStick, List, Timer,
  Document, UserFilled, Setting, SwitchButton, Connection
} from '@element-plus/icons-vue'
import { getOverview } from '../api/overview'
import { getProjects } from '../api/project'

const router = useRouter()
const route = useRoute()

// 获取当前登录用户信息
const user = computed(() => getUser() || {})

// 获取当前路由路径，用于菜单高亮
const currentRoute = computed(() => route.path)

// 待完成任务（全部未完成）
const pendingTasks = ref(0)
// 今日截止未完成任务
const todayPendingTasks = ref(0)
// 逾期任务
const overdueTasks = ref(0)

// 项目列表（用于判断用户是否为项目负责人）
const projects = ref([])
// 用户是否为任一项目的负责人
const hasProjectLeaderRole = ref(false)

// 加载概览数据
async function loadOverview() {
  try {
    const res = await getOverview()
    pendingTasks.value = res.data?.pendingTasks || 0
    todayPendingTasks.value = res.data?.todayPendingTasks || 0
    overdueTasks.value = res.data?.overdueTasks || 0
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

// 加载项目列表
async function loadProjects() {
  try {
    const res = await getProjects()
    projects.value = res.data || []
    // 检查当前用户是否为任一项目的负责人
    const userData = getUser()
    hasProjectLeaderRole.value = projects.value.some(p => p.leaderId === userData?.id)
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

onMounted(() => {
  loadOverview()
  loadProjects()
  // 监听任务状态变更事件，实时刷新概览数据
  window.addEventListener('task-status-changed', loadOverview)
})

onUnmounted(() => {
  window.removeEventListener('task-status-changed', loadOverview)
})

// 路由切换时刷新概览数据（确保任务打钩/撤销后顶部计数同步更新）
watch(() => route.path, () => {
  loadOverview()
})

// 退出登录
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 清除 sessionStorage 中的用户信息
    removeUser()
    ElMessage.success('已退出登录')
    // 跳转到登录页
    router.push('/login')
  } catch {
    // 用户取消了退出操作
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
  display: flex;
}

.home-aside {
  background: linear-gradient(180deg, #FAFAFE 0%, #FFFFFF 60px);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  box-shadow: 1px 0 4px rgba(0,0,0,0.04);
  z-index: 1;
}

.aside-header {
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 12px;
  padding-bottom: 8px;
}

.aside-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #EEF2FF, #F4F3FF);
  border-radius: 14px;
  width: 100%;
}

.aside-logo .el-icon {
  color: #6366F1;
  flex-shrink: 0;
}

.aside-title {
  color: #4F46E5;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.5px;
  line-height: 1.3;
}

.aside-greeting {
  font-size: 14px;
  color: #9CA3AF;
  line-height: 1.4;
  text-align: center;
  font-weight: 500;
  width: 100%;
}

.wave-icon {
  margin-right: 4px;
  font-size: 16px;
}

/* 主模块顶部栏 */
.home-top-bar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

/* 任务计数文字（与导览栏菜单字号一致） */
.task-count-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary, #6B7280);
}

.count-text-divider {
  color: #D1D5DB;
}

/* 覆盖 Element Plus 默认菜单样式 */
.home-aside .el-menu {
  border-right: none;
  background-color: transparent;
  flex: 1;
}

.home-aside .el-menu-item-group {
  margin-top: 4px;
}

.home-aside .el-menu-item-group:first-child {
  margin-top: 8px;
}

.home-aside .el-menu-item-group:last-child {
  margin-bottom: 8px;
}

/* 分组标题 */
.home-aside .el-menu-item-group__title {
  padding: 0;
  margin: 0;
  height: auto;
  line-height: normal;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 22px 0 8px;
  font-size: 15px;
  font-weight: 700;
  color: #6366F1;
  letter-spacing: 0;
}

.group-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #A5B4FC;
}

/* 浮动退出登录 */
.logout-float {
  position: fixed;
  top: 10px;
  right: 10px;
  z-index: 1000;
}

.logout-float .el-button {
  color: #9CA3AF !important;
  background: #fff !important;
  border: 1px solid #F3F4F6 !important;
  border-radius: 20px;
  padding: 6px 14px !important;
  font-size: 13px;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.logout-float .el-button:hover {
  color: #EF4444 !important;
  border-color: #FECACA !important;
  background: #FEF2F2 !important;
}

.home-main {
  background: #F8F9FB;
  padding: 20px 28px 28px;
  overflow-y: auto;
  height: 100vh;
}

/* 今日截止任务提示条 - 基础样式 */
.today-due-banner {
  display: flex;
  align-items: center;
  padding: 10px 18px;
  margin-bottom: 16px;
  border-radius: 8px;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

/* 有未完成 → 黄色 */
.today-due-banner.banner-yellow {
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  color: #92400E;
  box-shadow: 0 2px 8px rgba(251, 191, 36, 0.15);
}

.today-due-banner.banner-yellow strong {
  font-weight: 700;
  color: #D97706;
}

/* 全部完成 → 绿色 */
.today-due-banner.banner-green {
  background: linear-gradient(135deg, #D1FAE5, #A7F3D0);
  color: #065F46;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.15);
}

.today-due-banner.banner-green strong {
  font-weight: 700;
  color: #059669;
}
</style>
