<template>
  <div class="ai-task-page">
    <!-- 页面标题 -->
    <div class="page-header-accent">
      <div class="header-left">
        <el-icon :size="22"><MagicStick /></el-icon>
        <h3>AI 任务拆解</h3>
        <span class="header-en">AI Task Breakdown</span>
      </div>
    </div>

    <!-- 顶部输入区域 -->
    <el-card class="input-card" shadow="hover">
      <el-form :model="form" label-width="100px">
        <el-form-item label="选择项目">
          <el-select v-model="form.projectId" placeholder="请先选择一个项目" style="width: 100%" @change="handleProjectChange">
            <el-option
              v-for="p in projects"
              :key="p.id"
              :label="p.name"
              :value="p.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="任务目标">
          <el-input
            v-model="form.goal"
            type="textarea"
            :rows="3"
            placeholder="请输入你想要拆解的任务目标，例如：开发一个用户登录注册功能"
          />
        </el-form-item>

        <el-form-item label="补充说明">
          <el-input
            v-model="form.context"
            type="textarea"
            :rows="2"
            placeholder="可选的补充信息，帮助 AI 更准确理解你的需求"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="generating" @click="handleGenerate" :disabled="!form.projectId || !form.goal" v-if="canManageCurrentProject()">
            <el-icon><MagicStick /></el-icon>
            生成拆解方案
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 左右两栏 - 始终显示 -->
    <div class="result-section">
      <div class="result-columns">
        <!-- 左栏：AI 拆解结果 -->
        <div class="left-column">
          <div class="column-header">
            <span class="column-title">AI 拆解结果（共 {{ suggestions.length }} 项）</span>
          </div>
          <el-table
            v-if="suggestions.length > 0"
            :data="suggestions"
            border
            stripe
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="title" label="任务标题" min-width="140" />
            <el-table-column prop="description" label="任务说明" min-width="160" show-overflow-tooltip />
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="{ row }">
                <el-tag :type="priorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dueDate" label="截止日期" width="110" />
            <el-table-column label="负责人" width="130">
              <template #default="{ row }">
                <el-select v-model="row.assigneeId" placeholder="选择" size="small" style="width: 110px">
                  <el-option
                    v-for="u in projectMembers"
                    :key="u.id"
                    :label="u.realName || u.username"
                    :value="u.id"
                  />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="suggestions.length === 0" description="点击「生成拆解方案」获取 AI 建议" :image-size="80" />
          <div class="column-footer" v-if="suggestions.length > 0">
            <el-button type="success" :loading="importing" @click="handleImport" :disabled="selectedItems.length === 0" v-if="canManageCurrentProject()">
              <el-icon><Download /></el-icon>
              导入选中任务（{{ selectedItems.length }} 项）
            </el-button>
          </div>
        </div>

        <!-- 右栏：已有任务列表 -->
        <div class="right-column">
          <div class="column-header">
            <span class="column-title">已有任务列表</span>
            <el-button type="primary" size="small" @click="openCreateDialog" v-if="canManageCurrentProject()">
              <el-icon><Plus /></el-icon>
              新建任务
            </el-button>
          </div>
          <el-table
            :data="existingTasks"
            border
            stripe
            style="width: 100%"
            v-loading="loadingTasks"
          >
            <el-table-column label="完成" width="55" align="center">
              <template #default="{ row }">
                <el-checkbox
                  :model-value="row.completed === true"
                  @change="(val) => handleToggleComplete(row, val)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="title" label="任务标题" min-width="140" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">
                  {{ statusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="{ row }">
                <el-tag :type="priorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loadingTasks && existingTasks.length === 0" description="暂无任务" />
        </div>
      </div>
    </div>

    <!-- 新建任务弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建任务" width="550px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="负责人" prop="assigneeId">
          <el-select v-model="createForm.assigneeId" placeholder="请选择负责人" style="width: 100%">
            <el-option v-for="u in projectMembers" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="createForm.status" style="width: 100%">
            <el-option label="待开始" value="todo" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="done" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="createForm.priority" style="width: 100%">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="createForm.dueDate" type="date" placeholder="选择截止日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="任务说明">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入任务说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleCreateTask">保存</el-button>
      </template>
    </el-dialog>

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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Download, Plus } from '@element-plus/icons-vue'
import { getProjects } from '../api/project'
import { getProjectMembers } from '../api/project'
import { getUsers } from '../api/user'
import { generateTaskPlan, importTaskPlan } from '../api/ai'
import { getTasks, saveTask, completeTask, uncompleteTask } from '../api/task'
import { getUser } from '../utils/storage'

const STORAGE_KEY = 'ai_task_breakdown_state'

const projects = ref([])
const members = ref([])
const projectMembers = ref([]) // 当前选中项目的成员
const suggestions = ref([])
const selectedItems = ref([])
const generating = ref(false)
const importing = ref(false)
const existingTasks = ref([])
const loadingTasks = ref(false)
const saving = ref(false)
const savingSummary = ref(false)

// 顶部表单
const form = ref({
  projectId: '',
  goal: '',
  context: ''
})

// 新建任务弹窗
const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createForm = ref({
  title: '',
  assigneeId: '',
  status: 'todo',
  priority: '中',
  dueDate: '',
  description: ''
})
const createRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
}

// 完成总结弹窗
const summaryDialogVisible = ref(false)
const summaryFormRef = ref(null)
const summaryForm = ref({ summary: '', taskId: null })
const summaryRules = {
  summary: [{ required: true, message: '请输入完成总结', trigger: 'blur' }]
}

// 优先级样式
function priorityType(priority) {
  const map = { 高: 'danger', 中: 'warning', 低: 'primary', high: 'danger', medium: 'warning', low: 'primary' }
  return map[priority] || 'info'
}

// 状态样式
function statusType(status) {
  const map = { todo: 'info', in_progress: 'primary', done: 'success' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { todo: '待开始', in_progress: '进行中', done: '已完成' }
  return map[status] || status
}

// 选择变化
function handleSelectionChange(selection) {
  selectedItems.value = selection
}

// 项目切换时刷新已有任务列表和项目成员
async function handleProjectChange() {
  if (form.value.projectId) {
    await Promise.all([
      fetchExistingTasks(),
      loadProjectMembers()
    ])
  } else {
    existingTasks.value = []
    projectMembers.value = []
  }
}

// 加载当前选中项目的成员
async function loadProjectMembers() {
  try {
    const res = await getProjectMembers(form.value.projectId)
    projectMembers.value = res.data || []
  } catch (error) {
    console.error('获取项目成员失败:', error)
    projectMembers.value = []
  }
}

// 获取已有任务列表
async function fetchExistingTasks() {
  if (!form.value.projectId) return
  loadingTasks.value = true
  try {
    const res = await getTasks(form.value.projectId)
    existingTasks.value = res.data || []
  } catch (error) {
    console.error('获取任务列表失败:', error)
  } finally {
    loadingTasks.value = false
  }
}

// 检查当前用户是否有权限管理当前选中的项目
function canManageCurrentProject() {
  const u = getUser()
  if (!form.value.projectId) return false
  const project = projects.value.find(p => p.id === form.value.projectId)
  return u?.role === 'leader' || (project && project.leaderId === u?.id)
}

// 生成拆解方案
async function handleGenerate() {
  if (!form.value.projectId || !form.value.goal) {
    ElMessage.warning('请先选择项目和输入任务目标')
    return
  }

  generating.value = true
  try {
    const res = await generateTaskPlan({
      projectId: form.value.projectId,
      goal: form.value.goal,
      description: form.value.context
    })
    // 为每个建议项添加临时 ID
    const items = res.data?.items || []
    suggestions.value = items.map((item, index) => ({
      ...item,
      tempId: `temp_${index}_${Date.now()}`
    }))
    // 保存到 sessionStorage
    saveStateToStorage()
    ElMessage.success('方案生成成功！请审核并调整后再导入。')
  } catch (error) {
    console.error('生成方案失败:', error)
  } finally {
    generating.value = false
  }
}

// 导入选中任务
async function handleImport() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先勾选要导入的任务')
    return
  }

  // 权限检查：只有系统管理员或项目负责人才能导入任务
  const user = getUser()
  const project = projects.value.find(p => p.id === form.value.projectId)
  const isSystemAdmin = user?.role === 'leader'
  const isProjectLeader = project && project.leaderId === user?.id
  if (!isSystemAdmin && !isProjectLeader) {
    ElMessage.error('您没有权限向该项目导入任务')
    return
  }

  importing.value = true
  try {
    await importTaskPlan({
      projectId: form.value.projectId,
      creatorId: user?.id,
      items: selectedItems.value
    })
    ElMessage.success(`成功导入 ${selectedItems.value.length} 项任务！`)
    // 清空 AI 结果并清除 sessionStorage
    suggestions.value = []
    selectedItems.value = []
    sessionStorage.removeItem(STORAGE_KEY)
    // 刷新右栏任务列表
    await fetchExistingTasks()
  } catch (error) {
    console.error('导入任务失败:', error)
  } finally {
    importing.value = false
  }
}

// 打开新建任务弹窗
function openCreateDialog() {
  createForm.value = {
    title: '',
    assigneeId: '',
    status: 'todo',
    priority: '中',
    dueDate: '',
    description: ''
  }
  createDialogVisible.value = true
}

// 新建任务
async function handleCreateTask() {
  // 权限检查
  if (!canManageCurrentProject()) {
    ElMessage.error('您没有权限向该项目创建任务')
    return
  }

  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await saveTask({
      ...createForm.value,
      projectId: form.value.projectId
    })
    ElMessage.success('任务创建成功！')
    createDialogVisible.value = false
    await fetchExistingTasks()
  } catch (error) {
    console.error('创建任务失败:', error)
  } finally {
    saving.value = false
  }
}

// 处理完成/撤销完成
async function handleToggleComplete(row, checked) {
  if (checked) {
    summaryForm.value = { summary: '', taskId: row.id }
    summaryDialogVisible.value = true
  } else {
    try {
      await ElMessageBox.confirm('确定要撤销该任务的完成状态吗？', '确认', {
        confirmButtonText: '确定撤销',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await uncompleteTask(row.id)
      ElMessage.success('已撤销完成状态')
      await fetchExistingTasks()
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
    await fetchExistingTasks()
    window.dispatchEvent(new CustomEvent('task-status-changed'))
  } catch (error) {
    console.error('完成任务失败:', error)
  } finally {
    savingSummary.value = false
  }
}

// 保存状态到 sessionStorage
function saveStateToStorage() {
  try {
    const state = {
      suggestions: suggestions.value,
      form: form.value
    }
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  } catch (e) {
    // 忽略存储错误
  }
}

// 从 sessionStorage 恢复状态
function restoreStateFromStorage() {
  try {
    const saved = sessionStorage.getItem(STORAGE_KEY)
    if (saved) {
      const state = JSON.parse(saved)
      if (state.suggestions && state.suggestions.length > 0) {
        suggestions.value = state.suggestions
      }
      if (state.form) {
        form.value.projectId = state.form.projectId || ''
        form.value.goal = state.form.goal || ''
        form.value.context = state.form.context || ''
      }
    }
  } catch (e) {
    // 忽略恢复错误
  }
}

// 监听表单和 suggestions 变化，保存到 sessionStorage
watch(form, () => {
  saveStateToStorage()
}, { deep: true })

watch(suggestions, () => {
  saveStateToStorage()
}, { deep: true })

onMounted(async () => {
  try {
    const [projRes, userRes] = await Promise.all([
      getProjects(),
      getUsers()
    ])
    projects.value = projRes.data || []
    members.value = userRes.data || []

    // 恢复 sessionStorage 中的状态
    restoreStateFromStorage()

    // 如果恢复后有项目 ID，加载该项目的数据
    if (form.value.projectId) {
      await Promise.all([
        fetchExistingTasks(),
        loadProjectMembers()
      ])
    }
  } catch (error) {
    console.error('初始化数据失败:', error)
  }
})
</script>

<style scoped>
.ai-task-page {
  padding: 4px;
}

.input-card {
  margin-bottom: 20px;
}

.result-section {
  margin-top: 20px;
}

.result-columns {
  display: flex;
  gap: 20px;
}

.left-column,
.right-column {
  flex: 1;
  min-width: 0;
  background: var(--bg-card, #FFFFFF);
  border-radius: var(--radius-card, 16px);
  padding: 24px 20px 20px;
  position: relative;
  transition: all 0.15s ease;
}

/* 左栏右上边缘彩色阴影 */
.left-column {
  box-shadow: 5px -5px 14px rgba(129, 140, 248, 0.35), var(--shadow-card, 0 1px 3px rgba(0,0,0,0.04));
}

/* 右栏右上边缘彩色阴影 */
.right-column {
  box-shadow: 5px -5px 14px rgba(59, 130, 246, 0.30), var(--shadow-card, 0 1px 3px rgba(0,0,0,0.04));
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.column-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.column-footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
