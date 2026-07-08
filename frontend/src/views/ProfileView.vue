<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <!-- 左侧：当前信息展示 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>当前信息</span>
            </div>
          </template>

          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userInfo.username }}</span>
          </div>
          <el-divider />

          <div class="info-item">
            <span class="info-label">真实姓名</span>
            <span class="info-value">{{ userInfo.realName || '-' }}</span>
          </div>
          <el-divider />

          <div class="info-item">
            <span class="info-label">角色</span>
            <span class="info-value">
              <el-tag :type="userInfo.role === 'leader' ? 'danger' : 'success'" size="small">
                {{ userInfo.role === 'leader' ? '负责人' : '成员' }}
              </el-tag>
            </span>
          </div>
          <el-divider />

          <div class="info-item">
            <span class="info-label">电话</span>
            <span class="info-value">{{ userInfo.phone || '-' }}</span>
          </div>
          <el-divider />

          <div class="info-item">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo.email || '-' }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：修改密码 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleUpdatePassword">
                确认修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { updatePassword } from '../api/user'
import { getUser } from '../utils/storage'
const formRef = ref(null)
const saving = ref(false)

// 从 sessionStorage 获取当前用户信息
const userInfo = ref(getUser() || {})

// 修改密码表单
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 修改密码
async function handleUpdatePassword() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updatePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码修改成功！请重新登录。')
    // 清空表单
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  padding: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.info-label {
  width: 80px;
  font-size: 14px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
