import { get, post, put, del } from '@/utils/axios'

/**
 * 公告管理相关API
 */

// 获取公告列表
export const getAnnouncementList = (params = {}) => {
  return get('/announcement/list', { params })
}

// 根据ID获取公告详情
export const getAnnouncementById = (id) => {
  return get(`/announcement/${id}`)
}

// 创建公告
export const createAnnouncement = (data) => {
  return post('/announcement', data)
}

// 更新公告
export const updateAnnouncement = (id, data) => {
  return put(`/announcement/${id}`, data)
}

// 删除公告
export const deleteAnnouncement = (id) => {
  return del(`/announcement/${id}`)
}

// 发布公告
export const publishAnnouncement = (id) => {
  return post(`/announcement/${id}/publish`)
}

// 撤销公告
export const revokeAnnouncement = (id) => {
  return post(`/announcement/${id}/revoke`)
}
