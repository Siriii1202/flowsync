/**
 * AI 辅助功能 API 模块
 * 提供智能任务拆解等 AI 能力
 */
import request from './index'

// 获取 AI 对某个任务拆解的建议
export function getTaskSuggestion(data) {
  return request.post('/ai/suggest', data)
}

// 让 AI 自动生成任务拆解方案
export function generateTaskPlan(data) {
  return request.post('/ai/generate', data)
}

// 导入 AI 生成的拆解方案（将 AI 建议转化为实际任务）
export function importTaskPlan(data) {
  return request.post('/ai/import', data)
}
