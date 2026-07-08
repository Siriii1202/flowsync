<template>
  <div class="summary-page">
    <!-- 页面标题 -->
    <div class="page-header-accent">
      <div class="header-left">
        <el-icon :size="22"><Document /></el-icon>
        <h3>总结中心</h3>
        <span class="header-en">Summary Center</span>
      </div>
      <div class="header-actions">
        <el-select v-model="filterProjectId" placeholder="按项目筛选" clearable style="width: 200px" @change="fetchSummaries">
          <el-option
            v-for="p in projects"
            :key="p.id"
            :label="p.name"
            :value="p.id"
          />
        </el-select>
        <el-button type="primary" @click="openDialog">
          <el-icon><Plus /></el-icon>
          新增总结
        </el-button>
      </div>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 总结列表 -->
    <el-table v-else :data="summaries" border stripe style="width: 100%">
      <el-table-column prop="projectName" label="项目名" width="140" />
      <el-table-column prop="taskName" label="关联任务" width="140">
        <template #default="{ row }">
          {{ row.taskName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="总结类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === '最终总结' ? 'success' : 'primary'" size="small">
            {{ row.type }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
      <el-table-column prop="creatorName" label="创建人" width="100" />
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canOperate(row)" type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="canOperate(row)" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && summaries.length === 0" description="暂无总结记录" />

    <!-- 新增/编辑总结弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editMode ? '编辑总结' : '新增总结'" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" @change="onProjectChange">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联任务">
          <el-select v-model="form.taskId" placeholder="可选，选择关联的任务" clearable style="width: 100%">
            <el-option v-for="t in filteredTasks" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="总结类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="阶段总结" value="阶段总结" />
            <el-option label="最终总结" value="最终总结" />
          </el-select>
        </el-form-item>
        <el-form-item label="总结内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请描述完成了什么、遇到了什么问题、有什么收获和反思" />
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
import { Document, Plus } from '@element-plus/icons-vue'
import { getSummaries, saveSummary, updateSummary, deleteSummary } from '../api/summary'
import { getProjects } from '../api/project'
import { getTasks } from '../api/task'
import { getUser } from '../utils/storage'

const user = getUser()
const loading = ref(true)
const saving = ref(false)
const summaries = ref([])
const projects = ref([])
const allTasks = ref([])
const dialogVisible = ref(false)
const editMode = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const filterProjectId = ref('')

// 当前用户是否是 leader
const isLeader = computed(() => user?.role === 'leader')

// 表单数据
const form = ref({
  projectId: '',
  taskId: '',
  type: '阶段总结',
  content: ''
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  type: [{ required: true, message: '请选择总结类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入总结内容', trigger: 'blur' }]
}

// 判断当前用户是否有权限操作该总结
function canOperate(row) {
  if (isLeader.value) return true // leader 可以操作所有
  return row.createdBy === user?.id // 其他人只能操作自己的
}

// 根据选中的项目，筛选对应的任务
const filteredTasks = computed(() => {
  if (!form.value.projectId) return []
  return allTasks.value.filter(t => t.projectId === form.value.projectId)
})

// 项目切换时清空关联任务
function onProjectChange() {
  form.value.taskId = ''
}

// 打开新增弹窗
function openDialog() {
  editMode.value = false
  editingId.value = null
  form.value = { projectId: '', taskId: '', type: '阶段总结', content: '' }
  dialogVisible.value = true
}

// 打开编辑弹窗
function handleEdit(row) {
  editMode.value = true
  editingId.value = row.id
  form.value = {
    projectId: row.projectId,
    taskId: row.taskId || '',
    type: row.type === '任务总结' ? 'task' : row.type === '项目总结' ? 'project' : row.type,
    content: row.content
  }
  dialogVisible.value = true
}

// 删除总结
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除这条总结吗？删除后不可恢复。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSummary(row.id)
    ElMessage.success('删除成功！')
    await fetchSummaries()
  } catch {
    // 用户取消操作
  }
}

// 保存总结（新增或编辑）
async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = {
      ...form.value,
      createdBy: user?.id
    }
    if (editMode.value) {
      data.id = editingId.value
      await updateSummary(data)
      ElMessage.success('总结更新成功！')
    } else {
      await saveSummary(data)
      ElMessage.success('总结添加成功！')
    }
    dialogVisible.value = false
    await fetchSummaries()
  } catch (error) {
    console.error('保存总结失败:', error)
  } finally {
    saving.value = false
  }
}

// 获取总结列表
async function fetchSummaries() {
  loading.value = true
  try {
    const res = await getSummaries(filterProjectId.value || undefined)
    summaries.value = res.data || []
  } catch (error) {
    console.error('获取总结列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const [projRes, taskRes] = await Promise.all([
      getProjects(),
      getTasks()
    ])
    projects.value = projRes.data || []
    allTasks.value = taskRes.data || []
  } catch (error) {
    console.error('初始化数据失败:', error)
  }
  await fetchSummaries()
})
</script>

<style scoped>
.summary-page {
  padding: 4px;
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 8px;
}
</style>
