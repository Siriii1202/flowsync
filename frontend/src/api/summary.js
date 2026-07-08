/**
 * 总结中心 API 模块
 */
import request from './index'

// 获取所有总结记录
export function getSummaries(projectId) {
  const params = {}
  if (projectId) {
    params.projectId = projectId
  }
  return request.get('/summaries', { params })
}

// 保存总结（新增）
export function saveSummary(data) {
  return request.post('/summaries', data)
}

// 更新总结
export function updateSummary(data) {
  return request.put(`/summaries/${data.id}`, data)
}

// 删除总结
export function deleteSummary(id) {
  return request.delete(`/summaries/${id}`)
}

// 按项目筛选总结
export function getSummariesByProject(projectId) {
  return request.get('/summaries', { params: { projectId } })
}
