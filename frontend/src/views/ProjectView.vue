<template>
  <div class="project-page">
    <!-- 页面标题 -->
    <div class="page-header-accent">
      <div class="header-left">
        <el-icon :size="22"><Folder /></el-icon>
        <h3>项目管理</h3>
        <span class="header-en">Project Management</span>
      </div>
      <div class="header-actions">
        <el-button v-if="isLeader" type="primary" @click="openDialog(null)">
          <el-icon><Plus /></el-icon>
          新建项目
        </el-button>
      </div>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 项目列表 -->
    <div v-else class="table-container">
      <el-table :data="projects" border stripe style="width: 100%">
      <el-table-column prop="name" label="项目名称" min-width="150" />
      <el-table-column prop="description" label="项目说明" min-width="200">
        <template #default="{ row }">
          <span class="desc-wrap">{{ row.description }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-select
            v-if="isLeader"
            :model-value="row.status"
            @change="(val) => handleStatusChange(row, val)"
            size="small"
            style="width: 90px"
          >
            <el-option label="规划中" value="planning" />
            <el-option label="进行中" value="active" />
            <el-option label="已完成" value="completed" />
            <el-option label="已暂停" value="paused" />
          </el-select>
          <el-tag v-else :type="statusType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="priorityType(row.priority)" size="small">
            {{ row.priority }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="leaderName" label="负责人" width="100" />
      <el-table-column label="参与者" min-width="140">
        <template #default="{ row }">
          <span class="member-names">{{ (row.memberNames || []).join('、') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="时间范围" width="220">
        <template #default="{ row }">
          {{ row.startDate }} ~ {{ row.endDate }}
        </template>
      </el-table-column>
      <!-- 操作列：仅负责人显示 -->
      <el-table-column v-if="isLeader" label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <!-- 新建/编辑项目弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑项目' : '新建项目'" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目说明" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.leaderId" placeholder="请选择项目负责人" style="width: 100%">
            <el-option v-for="u in users" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="参与人">
          <el-select v-model="form.memberIds" multiple placeholder="请选择参与人" style="width: 100%">
            <el-option v-for="u in users" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="规划中" value="planning" />
            <el-option label="进行中" value="active" />
            <el-option label="已完成" value="completed" />
            <el-option label="已暂停" value="paused" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-col :span="11">
            <el-date-picker v-model="form.startDate" type="date" placeholder="开始日期" style="width: 100%" value-format="YYYY-MM-DD" />
          </el-col>
          <el-col :span="2" style="text-align: center;">~</el-col>
          <el-col :span="11">
            <el-date-picker v-model="form.endDate" type="date" placeholder="结束日期" style="width: 100%" value-format="YYYY-MM-DD" />
          </el-col>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Plus } from '@element-plus/icons-vue'
import { getProjects, saveProject, deleteProject } from '../api/project'
import { getUsers } from '../api/user'
import { getUser } from '../utils/storage'

const loading = ref(true)
const saving = ref(false)
const projects = ref([])
const users = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const user = getUser()
const isLeader = computed(() => user?.role === 'leader')

// 状态映射
function statusType(status) {
  const map = { planning: 'info', active: 'primary', completed: 'success', paused: 'warning' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { planning: '规划中', active: '进行中', completed: '已完成', paused: '已暂停' }
  return map[status] || status
}

function priorityType(priority) {
  const map = { 高: 'danger', 中: 'warning', 低: 'primary', high: 'danger', medium: 'warning', low: 'primary' }
  return map[priority] || 'info'
}

// 弹窗表单数据
const form = ref({
  name: '',
  description: '',
  status: 'planning',
  priority: '中',
  startDate: '',
  endDate: '',
  leaderId: null,
  memberIds: []
})

// 监听负责人选择，自动加入参与人列表
watch(() => form.value.leaderId, (newLeaderId) => {
  if (newLeaderId && !form.value.memberIds.includes(newLeaderId)) {
    form.value.memberIds.push(newLeaderId)
  }
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

// 打开新建/编辑弹窗
function openDialog(row) {
  if (row) {
    isEdit.value = true
    form.value = {
      ...row,
      memberIds: row.memberIds || []
    }
  } else {
    isEdit.value = false
    form.value = {
      name: '',
      description: '',
      status: 'planning',
      priority: '中',
      startDate: '',
      endDate: '',
      leaderId: null,
      memberIds: []
    }
  }
  dialogVisible.value = true
}

// 保存项目
async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const { memberIds, leaderId, ...projectData } = form.value
    await saveProject({
      ...projectData,
      memberIds,
      leaderId,
      ownerId: user?.id
    })
    ElMessage.success(isEdit.value ? '项目更新成功！' : '项目创建成功！')
    dialogVisible.value = false
    // 重新加载列表
    await fetchProjects()
  } catch (error) {
    console.error('保存项目失败:', error)
  } finally {
    saving.value = false
  }
}

// 删除项目
async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个项目吗？相关任务也将被删除。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProject(id)
    ElMessage.success('删除成功！')
    await fetchProjects()
  } catch {
    // 用户取消操作
  }
}

// 快速修改状态
async function handleStatusChange(row, newStatus) {
  try {
    await saveProject({
      ...row,
      status: newStatus,
      memberIds: row.memberIds || [],
      leaderId: null,
      ownerId: user?.id
    })
    ElMessage.success('状态已更新')
    await fetchProjects()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 获取项目列表
async function fetchProjects() {
  try {
    const res = await getProjects()
    projects.value = res.data || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([
    fetchProjects(),
    getUsers().then(res => { users.value = res.data || [] }).catch(() => {})
  ])
})
</script>

<style scoped>
.project-page {
  padding: 4px;
}

.desc-wrap {
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
  display: block;
  max-height: 3em;
  overflow-y: auto;
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 8px;
}
</style>
