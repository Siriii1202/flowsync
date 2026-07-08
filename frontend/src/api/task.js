/**
 * 任务管理 API 模块
 */
import request from './index'

// 根据项目 ID 获取该项目的所有任务（拦截器会自动附加 userId 请求头）
export function getTasks(projectId) {
  return request.get('/tasks', { params: { projectId } })
}

// 保存任务（新建或更新）
export function saveTask(data) {
  return request.post('/tasks', data)
}

// 删除任务
export function deleteTask(id) {
  return request.delete(`/tasks/${id}`)
}

// 完成任务（附带完成总结）
export function completeTask(id, summary) {
  return request.put(`/tasks/${id}/complete`, { summary })
}

// 撤销完成
export function uncompleteTask(id) {
  return request.put(`/tasks/${id}/uncomplete`)
}

// 获取项目进度
export function getProjectProgress(projectId) {
  return request.get(`/tasks/progress/${projectId}`)
}
