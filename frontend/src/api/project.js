/**
 * 项目管理 API 模块
 * 负责项目和任务相关数据的增删改查（CRUD）
 */
import request from './index'
import { getUser } from '../utils/storage'

// 获取项目列表（拦截器会自动附加 userId 请求头）
export function getProjects() {
  return request.get('/projects')
}

// 保存项目（新建或更新）
export function saveProject(data) {
  const user = getUser()
  return request.post('/projects', {
    ...data,
    currentUserId: user?.id
  })
}

// 删除项目
export function deleteProject(id) {
  return request.delete(`/projects/${id}`)
}

// 获取指定项目的所有成员
export function getProjectMembers(projectId) {
  return request.get(`/projects/${projectId}/members`)
}
