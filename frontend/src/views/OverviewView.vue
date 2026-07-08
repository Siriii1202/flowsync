<template>
  <div class="overview-page">
    <!-- 页面标题 -->
    <div class="page-header-accent">
      <div class="header-left">
        <el-icon :size="22"><DataAnalysis /></el-icon>
        <h3>总览</h3>
        <span class="header-en">Overview</span>
      </div>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 统计卡片 - 2x2 大正方形 -->
    <div v-else class="stat-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-grid-item">
        <el-card shadow="hover" class="stat-card" @click="navigateTo(card.route)">
          <div class="stat-card-inner">
            <div class="stat-icon" :style="{ background: card.bgColor }">
              <el-icon :size="64" color="#fff">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-value">{{ card.value }}</p>
              <p class="stat-label">{{ card.label }}</p>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOverview } from '../api/overview'
import {
  DataAnalysis, UserFilled, Folder, List, Document
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(true)
const overviewData = ref({})

// 统计数据卡片配置
const statCards = computed(() => [
  {
    label: '用户数',
    value: overviewData.value.userCount ?? 0,
    icon: UserFilled,
    bgColor: '#818CF8',
    route: '/members'
  },
  {
    label: '项目数',
    value: overviewData.value.projectCount ?? 0,
    icon: Folder,
    bgColor: '#7C8B9E',
    route: '/projects'
  },
  {
    label: '任务数',
    value: overviewData.value.taskCount ?? 0,
    icon: List,
    bgColor: '#B0A8A0',
    route: '/tasks'
  },
  {
    label: '总结数',
    value: overviewData.value.summaryCount ?? 0,
    icon: Document,
    bgColor: '#A5B4FC',
    route: '/summaries'
  }
])

// 点击卡片跳转到对应页面
function navigateTo(route) {
  router.push(route)
}

// 页面加载时获取数据
onMounted(async () => {
  try {
    const res = await getOverview()
    overviewData.value = res.data || {}
  } catch (error) {
    console.error('获取概览数据失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.overview-page {
  padding: 4px;
  height: calc(100vh - 160px);
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 8px;
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 28px;
  height: 100%;
  padding: 4px;
}

.stat-grid-item {
  display: flex;
}

.stat-card {
  cursor: pointer;
  border-radius: 16px;
  width: 100%;
  height: 100%;
}

.stat-card :deep(.el-card__body) {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 36px;
}

.stat-icon {
  width: 130px;
  height: 130px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  text-align: left;
}

.stat-value {
  font-size: 72px;
  font-weight: 800;
  color: #303133;
  margin: 0;
  line-height: 1;
}

.stat-label {
  font-size: 24px;
  color: #909399;
  margin: 12px 0 0;
}
</style>
