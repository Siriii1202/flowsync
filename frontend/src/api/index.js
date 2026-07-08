/**
 * Axios 实例配置文件
 *
 * Axios 是一个用于发送 HTTP 请求的工具，
 * 就像是一个"信使"，负责前端和后端之间的通信。
 *
 * 拦截器（Interceptor）是 Axios 提供的一个强大功能：
 * - 请求拦截器：在发出请求前做一些处理
 * - 响应拦截器：在收到响应后做一些处理
 *
 * 这就像快递公司的"中转站"：
 * 发件时先包装检查，收件时先验货再交给收件人。
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getUser } from '../utils/storage'

// 创建 Axios 实例
// baseURL 设置了所有请求的公共前缀
const request = axios.create({
  baseURL: '/api',
  timeout: 30000 // 30秒超时，防止请求卡死
})

// ---- 请求拦截器 ----
// 在发送每个请求之前自动执行
request.interceptors.request.use(
  (config) => {
    // 从 sessionStorage 获取当前登录用户信息
    const user = getUser()
    if (user && user.id) {
      // 将用户 ID 附加到请求参数中
      // 这样后端就知道是谁在操作了
      config.params = {
        ...config.params,
        currentUserId: user.id
      }
      // 同时将 userId 放入请求头，用于权限过滤
      config.headers = config.headers || {}
      config.headers['userId'] = user.id
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// ---- 响应拦截器 ----
// 在收到每个响应之后自动执行
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果后端返回了业务错误（success = false），也进入错误处理流程
    if (res.success === false) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  (error) => {
    // 统一处理网络错误——不用在每个页面都写一遍错误处理
    const message = error.response?.data?.message || '请求失败，请检查网络连接'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
