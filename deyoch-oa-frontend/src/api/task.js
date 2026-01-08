import { get, post, put, del } from '@/utils/axios'

/**
 * 任务管理相关API
 */

// 获取任务列表（完整分页）
export const getTaskList = (params = {}) => {
  return get('/task/list', params)
}

// 获取工作台任务摘要（使用现有接口，限制数量）
export const getDashboardTasks = () => {
  return get('/task/list', { 
    page: 1,
    size: 10 // 限制返回10条
  })
}

// 获取我的待办任务（工作台专用，使用状态过滤）
export const getMyPendingTasks = (limit = 5) => {
  return get('/task/list', { 
    page: 1,
    size: limit
  })
}

// 获取我的待阅任务（工作台专用，使用状态过滤）
export const getMyReviewTasks = (limit = 5) => {
  return get('/task/list', { 
    page: 1,
    size: limit
  })
}

// 获取任务统计数据（工作台专用，通过获取所有任务来统计）
export const getTaskStats = () => {
  return get('/task/list', { 
    page: 1,
    size: 100 // 获取更多数据用于统计
  })
}

// 根据ID获取任务详情
export const getTaskById = (id) => {
  return get(`/task/${id}`)
}

// 创建任务
export const createTask = (data) => {
  return post('/task', data)
}

// 更新任务
export const updateTask = (id, data) => {
  return put(`/task/${id}`, data)
}

// 删除任务
export const deleteTask = (id) => {
  return del(`/task/${id}`)
}

// 更新任务状态
export const updateTaskStatus = (id, status) => {
  return put(`/task/${id}/status`, { status })
}

// 分配任务
export const assignTask = (id, assignee) => {
  return put(`/task/${id}/assign`, { assignee })
}