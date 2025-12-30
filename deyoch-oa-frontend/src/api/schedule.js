import { get, post, put, del } from '@/utils/axios'

/**
 * 日程管理相关API
 */

// 获取日程列表
export const getScheduleList = () => {
  return get('/schedule/list')
}

// 根据ID获取日程详情
export const getScheduleById = (id) => {
  return get(`/schedule/${id}`)
}

// 创建日程
export const createSchedule = (data) => {
  return post('/schedule', data)
}

// 更新日程
export const updateSchedule = (id, data) => {
  return put(`/schedule/${id}`, data)
}

// 删除日程
export const deleteSchedule = (id) => {
  return del(`/schedule/${id}`)
}

// 更新日程状态
export const updateScheduleStatus = (id, status) => {
  return put(`/schedule/${id}/status`, { status })
}

// 分享日程
export const shareSchedule = (id, sharedUsers) => {
  return post(`/schedule/${id}/share`, { sharedUsers })
}

// 获取分享给我的日程
export const getSharedScheduleList = () => {
  return get('/schedule/shared')
}