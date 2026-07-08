/**
 * 登录认证 API 模块
 * 负责处理用户登录相关请求
 */
import request from './index'

// 登录：提交用户名和密码，换取登录凭证
export function login(data) {
  return request.post('/auth/login', data)
}

// 注册：提交注册信息，创建新用户
export function register(data) {
  return request.post('/auth/register', data)
}
