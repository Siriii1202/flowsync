<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 系统标题 -->
      <div class="login-header">
        <el-icon :size="40" color="#6366F1"><Connection /></el-icon>
        <h2 class="login-title">FlowSync 协同任务管理系统</h2>
        <p class="login-subtitle">团队任务协同管理平台</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 测试账号提示 -->
      <div class="login-tips">
        <el-icon><InfoFilled /></el-icon>
        <span>测试账号：leader/123456, member1/123456, member2/123456</span>
      </div>

      <!-- 注册入口 -->
      <div class="register-entry">
        <span>还没有账号？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, InfoFilled, Connection } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { setUser, setToken } from '../utils/storage'
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

// 登录表单数据
const form = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 处理登录
async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    if (!res.success) {
      ElMessage.error(res.message || '登录失败')
      return
    }
    const responseData = res.data
    if (responseData) {
      setUser(responseData.user || responseData)
      if (responseData.token) {
        setToken(responseData.token)
      }
    }
    ElMessage.success('登录成功！')
    router.push('/overview')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #EEF2FF 0%, #F4F3FF 50%, #F8F9FB 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 22px;
  color: #1F1F1F;
  margin: 12px 0 6px;
}

.login-subtitle {
  font-size: 13px;
  color: #6B7280;
}

.login-btn {
  width: 100%;
}

.login-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6B7280;
  background: #F8F9FB;
  padding: 10px 12px;
  border-radius: 6px;
  margin-top: 16px;
}

.register-entry {
  text-align: center;
  margin-top: 14px;
  font-size: 14px;
  color: #6B7280;
}

.register-link {
  color: #6366F1;
  text-decoration: none;
  margin-left: 4px;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
