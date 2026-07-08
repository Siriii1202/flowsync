/**
 * 总览数据 API 模块
 * 获取仪表盘/总览页面需要用到的统计数据
 */
import request from './index'

// 获取总览统计信息（用户数、项目数、任务数、总结数等）
export function getOverview() {
  return request.get('/overview')
}
