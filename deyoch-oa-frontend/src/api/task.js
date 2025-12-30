import { get, post, put, del } from '@/utils/axios'

/**
 * 任务管理相关API
 */

// 获取任务列表
export const getTaskList = () => {
  return get('/task/list')
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