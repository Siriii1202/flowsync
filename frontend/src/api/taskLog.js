/**
 * 任务进度日志 API 模块
 * 记录任务进展的每一步变化
 */
import request from './index'

// 获取某个任务的所有进度日志
export function getTaskLogs(taskId) {
  return request.get('/task-logs', { params: { taskId } })
}

// 新增一条进度记录
export function saveTaskLog(data) {
  return request.post('/task-logs', data)
}
