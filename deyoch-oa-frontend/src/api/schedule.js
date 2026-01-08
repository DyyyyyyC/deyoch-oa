import { get, post, put, del } from '@/utils/axios'

/**
 * 日程管理相关API
 */

// 获取日程列表（完整分页）
export const getScheduleList = (params = {}) => {
  return get('/schedule/list', params)
}

/**
 * 获取指定日期的日程（工作台专用）
 * 使用现有接口，前端过滤指定日期
 * @param {string} date - 日期 YYYY-MM-DD
 * @returns {Promise} - 当天日程列表
 */
export const getScheduleByDate = (date) => {
  // 使用现有的列表接口，前端过滤日期
  return get('/schedule/list').then(response => {
    if (response && response.data) {
      const schedules = Array.isArray(response.data) ? response.data : []
      return schedules.filter(schedule => {
        if (schedule.startTime) {
          const scheduleDate = new Date(schedule.startTime).toISOString().split('T')[0]
          return scheduleDate === date
        }
        return false
      })
    }
    return []
  })
}

/**
 * 获取日期范围内的日程（工作台日历专用）
 * 使用现有接口，前端过滤日期范围
 * @param {string} startDate - 开始日期 YYYY-MM-DD
 * @param {string} endDate - 结束日期 YYYY-MM-DD
 * @returns {Promise} - 日程列表
 */
export const getScheduleByDateRange = (startDate, endDate) => {
  return get('/schedule/list').then(response => {
    if (response && response.data) {
      const schedules = Array.isArray(response.data) ? response.data : []
      return schedules.filter(schedule => {
        if (schedule.startTime) {
          const scheduleDate = new Date(schedule.startTime).toISOString().split('T')[0]
          return scheduleDate >= startDate && scheduleDate <= endDate
        }
        return false
      })
    }
    return []
  })
}

/**
 * 获取当月日程概览（工作台专用）
 * @returns {Promise} - 当月日程概览
 */
export const getCurrentMonthSchedules = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const startDate = `${year}-${month.toString().padStart(2, '0')}-01`
  const endDate = new Date(year, month, 0).toISOString().split('T')[0]
  
  return getScheduleByDateRange(startDate, endDate)
}

/**
 * 获取今日日程（工作台专用）
 * @returns {Promise} - 今日日程列表
 */
export const getTodaySchedules = () => {
  const today = new Date().toISOString().split('T')[0]
  return getScheduleByDate(today)
}

/**
 * 获取即将到来的日程（工作台专用）
 * @param {number} days - 未来几天，默认7天
 * @returns {Promise} - 即将到来的日程
 */
export const getUpcomingSchedules = (days = 7) => {
  const today = new Date()
  const futureDate = new Date(today.getTime() + days * 24 * 60 * 60 * 1000)
  const startDate = today.toISOString().split('T')[0]
  const endDate = futureDate.toISOString().split('T')[0]
  
  return getScheduleByDateRange(startDate, endDate)
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