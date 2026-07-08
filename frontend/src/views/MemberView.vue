<template>
  <div class="member-page">
    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 成员表格 -->
    <el-table v-else :data="members" border stripe style="width: 100%">
      <el-table-column prop="username" label="用户名" width="100" />
      <el-table-column prop="realName" label="真实姓名" width="100" />
      <el-table-column label="级别" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'leader' ? 'danger' : 'info'" size="small" effect="dark">
            {{ row.role === 'leader' ? '管理员' : '组员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="项目角色" min-width="240">
        <template #default="{ row }">
          <div v-if="row.projects && row.projects.length > 0" class="project-roles">
            <el-tag
              v-for="p in row.projects"
              :key="p.projectId"
              :type="p.role === 'leader' ? 'danger' : 'success'"
              size="small"
              class="project-role-tag"
            >
              {{ p.projectName }} - {{ p.role === 'leader' ? '负责人' : '参与者' }}
            </el-tag>
          </div>
          <span v-else class="no-project">暂无参与项目</span>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsersWithProjects } from '../api/user'
const loading = ref(true)
const members = ref([])

onMounted(async () => {
  try {
    const res = await getUsersWithProjects()
    members.value = res.data || []
  } catch (error) {
    console.error('获取成员列表失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.member-page {
  padding: 4px;
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 8px;
}

.project-roles {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.project-role-tag {
  margin: 0;
}

.no-project {
  color: #9CA3AF;
  font-size: 13px;
}
</style>
