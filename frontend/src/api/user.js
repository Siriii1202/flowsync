/**
 * 用户管理 API 模块
 */
import request from './index'

// 获取所有用户列表
export function getUsers() {
  return request.get('/users')
}

// 获取用户列表（含项目角色信息）
export function getUsersWithProjects() {
  return request.get('/users/with-projects')
}

// 修改密码
export function updatePassword(data) {
  return request.put('/users/password', data)
}
