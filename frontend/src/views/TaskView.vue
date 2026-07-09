<template>
  <div class="task-page">
    <!-- 页面标题 -->
    <div class="page-header-accent">
      <div class="header-left">
        <el-icon :size="22"><List /></el-icon>
        <h3>任务管理</h3>
        <span class="header-en">Task Management</span>
      </div>
      <div class="header-actions">
        <el-select v-model="filterProjectId" placeholder="按项目筛选" clearable style="width: 200px" @change="fetchTasks">
          <el-option
            v-for="p in projects"
            :key="p.id"
            :label="p.name"
            :value="p.id"
          />
        </el-select>
      </div>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 按项目分组展示 -->
    <div v-else-if="displayProjects.length > 0">
      <el-card v-for="project in displayProjects" :key="project.id" class="project-card">
        <template #header>
          <div class="card-header">
            <div class="card-header-left">
              <span class="project-name">{{ project.name }}</span>
              <el-tag type="info" size="small" class="progress-tag">
                进度：{{ getCompletedCount(project.id) }}/{{ getTotalCount(project.id) }}
              </el-tag>
            </div>
            <el-button v-if="canManageProject(project.id)" type="primary" size="small" @click="openDialog(null, project.id)">
              <el-icon><Plus /></el-icon>
              新建任务
            </el-button>
          </div>
        </template>

        <el-table :data="projectTasks[project.id] || []" border stripe style="width: 100%">
          <!-- 完成复选框列 -->
          <el-table-column label="完成" width="60" align="center">
            <template #default="{ row }">
              <el-checkbox
                v-if="isLeader || row.assigneeId === user?.id"
                :model-value="row.completed === true"
                @change="(val) => handleToggleComplete(row, val)"
              />
              <el-icon v-else :color="row.completed ? '#10B981' : '#C0C4CC'" :size="18">
                <Select v-if="row.completed" />
                <Close v-else />
              </el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="任务标题" min-width="160" />
          <el-table-column prop="assigneeName" label="负责人" width="100" />
          <el-table-column prop="priority" label="优先级" width="80">
            <template #default="{ row }">
              <el-tag :type="priorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="截止日期" width="120" />
          <el-table-column label="完成状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.completed" type="success" size="small">已完成</el-tag>
              <el-tag v-else type="danger" size="small">未完成</el-tag>
            </template>
          </el-table-column>
          <!-- 操作列 -->
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <template v-if="canManageProject(project.id)">
                <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
              </template>
              <template v-else>
                <span style="color: #c0c4cc; font-size: 13px;">无权限</span>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && displayProjects.length === 0" description="暂无任务" />

    <!-- 完成总结弹窗 -->
    <el-dialog v-model="summaryDialogVisible" title="填写完成总结" width="500px" :close-on-click-modal="false">
      <el-form ref="summaryFormRef" :model="summaryForm" :rules="summaryRules" label-width="100px">
        <el-form-item label="完成总结" prop="summary">
          <el-input
            v-model="summaryForm.summary"
            type="textarea"
            :rows="4"
            placeholder="请简要描述任务的完成情况、遇到的问题及解决方案等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelComplete">取消</el-button>
        <el-button type="primary" :loading="savingSummary" @click="confirmComplete">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 新建/编辑任务弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新建任务'" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" :disabled="!canManageProject(form.projectId)" />
        </el-form-item>
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" :disabled="!canManageProject(form.projectId)">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="assigneeId">
          <el-select v-model="form.assigneeId" placeholder="请选择负责人" style="width: 100%" :disabled="!canManageProject(form.projectId)">
            <el-option v-for="u in dialogMembers" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" style="width: 100%" :disabled="!canManageProject(form.projectId)">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.dueDate" type="date" placeholder="选择截止日期" style="width: 100%" value-format="YYYY-MM-DD" :disabled="!canManageProject(form.projectId)" />
        </el-form-item>
        <el-form-item label="任务说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入任务说明" :disabled="!canManageProject(form.projectId)" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Select, Close, List } from '@element-plus/icons-vue'
import { getTasks, saveTask, deleteTask, completeTask, uncompleteTask } from '../api/task'
import { getProjects, getProjectMembers } from '../api/project'
import { getUser } from '../utils/storage'

const loading = ref(true)
const saving = ref(false)
const savingSummary = ref(false)
const projectTasks = ref({})
const projects = ref([])
const projectMembersMap = ref({}) // { projectId: [user, ...] }
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const filterProjectId = ref('')

// 完成总结弹窗
const summaryDialogVisible = ref(false)
const summaryFormRef = ref(null)
const summaryForm = ref({ summary: '', taskId: null })
const summaryRules = {
  summary: [{ required: true, message: '请输入完成总结', trigger: 'blur' }]
}

const user = getUser()
const isSystemAdmin = computed(() => user?.role === 'leader')

// 判断当前用户是否为指定项目的负责人
function isProjectLeader(projectId) {
  const project = projects.value.find(p => p.id === projectId)
  return project && project.leaderId === user?.id
}

// 判断当前用户是否有权限管理指定项目的任务
function canManageProject(projectId) {
  // 系统管理员（user.role === 'leader'）可以管理所有项目
  // 项目负责人也可以管理自己的项目
  return isSystemAdmin.value || isProjectLeader(projectId)
}

// 当前对话框选中项目对应的成员列表
const dialogMembers = computed(() => {
  const pid = form.value.projectId
  if (!pid || !projectMembersMap.value[pid]) return []
  return projectMembersMap.value[pid]
})

// 根据筛选条件决定显示哪些项目
const displayProjects = computed(() => {
  if (filterProjectId.value) {
    return projects.value.filter(p => p.id === filterProjectId.value)
  }
  return projects.value
})

function priorityType(priority) {
  const map = { 高: 'danger', 中: 'warning', 低: 'primary', high: 'danger', medium: 'warning', low: 'primary' }
  return map[priority] || 'info'
}

// 进度计算
function getCompletedCount(projectId) {
  const tasks = projectTasks.value[projectId] || []
  return tasks.filter(t => t.completed === true).length
}

function getTotalCount(projectId) {
  return (projectTasks.value[projectId] || []).length
}

// 弹窗表单
const form = ref({
  title: '',
  projectId: '',
  assigneeId: '',
  priority: '中',
  dueDate: '',
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }]
}

// 处理完成/撤销完成
async function handleToggleComplete(row, checked) {
  if (checked) {
    // 勾选 → 弹出完成总结对话框
    summaryForm.value = { summary: '', taskId: row.id }
    summaryDialogVisible.value = true
  } else {
    // 取消勾选 → 确认撤销
    try {
      await ElMessageBox.confirm('确定要撤销该任务的完成状态吗？', '确认', {
        confirmButtonText: '确定撤销',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await uncompleteTask(row.id)
      ElMessage.success('已撤销完成状态')
      await fetchTasks()
      window.dispatchEvent(new CustomEvent('task-status-changed'))
    } catch {
      // 取消操作
    }
  }
}

// 取消完成
function cancelComplete() {
  summaryDialogVisible.value = false
  summaryForm.value = { summary: '', taskId: null }
}

// 确认完成
async function confirmComplete() {
  const valid = await summaryFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingSummary.value = true
  try {
    await completeTask(summaryForm.value.taskId, summaryForm.value.summary)
    ElMessage.success('任务已完成！')
    summaryDialogVisible.value = false
    await fetchTasks()
    window.dispatchEvent(new CustomEvent('task-status-changed'))
  } catch (error) {
    console.error('完成任务失败:', error)
  } finally {
    savingSummary.value = false
  }
}

// 打开弹窗
async function openDialog(row, projectId) {
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    const pid = projectId || filterProjectId.value || ''
    form.value = {
      title: '',
      projectId: pid,
      assigneeId: '',
      status: 'todo',
      priority: '中',
      dueDate: '',
      description: ''
    }
  }
  // 确保对话框对应项目的成员已加载
  const pid = form.value.projectId
  if (pid && !projectMembersMap.value[pid]) {
    try {
      const res = await getProjectMembers(pid)
      projectMembersMap.value[pid] = res.data || []
    } catch (error) {
      console.error('获取项目成员失败:', error)
    }
  }
  dialogVisible.value = true
}

// 保存任务
async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await saveTask(form.value)
    ElMessage.success(isEdit.value ? '任务更新成功！' : '任务创建成功！')
    dialogVisible.value = false
    await fetchTasks()
  } catch (error) {
    console.error('保存任务失败:', error)
  } finally {
    saving.value = false
  }
}

// 删除任务
async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTask(id)
    ElMessage.success('删除成功！')
    await fetchTasks()
  } catch {
    // 取消操作
  }
}

// 获取任务列表
async function fetchTasks() {
  loading.value = true
  try {
    if (filterProjectId.value) {
      // 筛选了单个项目
      const res = await getTasks(filterProjectId.value)
      projectTasks.value = { [filterProjectId.value]: res.data || [] }
    } else {
      // 遍历所有项目，并行加载每个项目的任务
      const results = await Promise.all(
        projects.value.map(p =>
          getTasks(p.id).then(res => ({ projectId: p.id, tasks: res.data || [] }))
        )
      )
      const map = {}
      results.forEach(({ projectId, tasks }) => {
        map[projectId] = tasks
      })
      projectTasks.value = map
    }
  } catch (error) {
    console.error('获取任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const projRes = await getProjects()
    projects.value = projRes.data || []
    // 预加载所有项目的成员
    const memberPromises = projects.value.map(p =>
      getProjectMembers(p.id).then(res => ({ pid: p.id, users: res.data || [] })).catch(() => null)
    )
    const memberResults = await Promise.all(memberPromises)
    const map = {}
    memberResults.forEach(r => {
      if (r) map[r.pid] = r.users
    })
    projectMembersMap.value = map
  } catch (error) {
    console.error('初始化数据失败:', error)
  }
  await fetchTasks()
})
</script>

<style scoped>
.task-page {
  padding: 4px;
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 8px;
}

.project-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-name {
  font-weight: bold;
  font-size: 16px;
}

.progress-tag {
  font-size: 12px;
}
</style>
