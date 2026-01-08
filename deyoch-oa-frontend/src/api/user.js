import { get, post, put, del } from '@/utils/axios'

/**
 * 用户管理相关API
 */

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 用户列表
 */
export const getUserList = (params = {}) => {
  return get('/user/list', params)
}

/**
 * 根据ID获取用户详情
 * @param {number} id - 用户ID
 * @returns {Promise} - 用户详情
 */
export const getUserById = (id) => {
  return get(`/user/${id}`)
}

/**
 * 创建用户
 * @param {Object} data - 用户数据
 * @returns {Promise} - 创建结果
 */
export const createUser = (data) => {
  return post('/user', data)
}

/**
 * 更新用户
 * @param {number} id - 用户ID
 * @param {Object} data - 用户数据
 * @returns {Promise} - 更新结果
 */
export const updateUser = (id, data) => {
  return put(`/user/${id}`, data)
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 * @returns {Promise} - 删除结果
 */
export const deleteUser = (id) => {
  return del(`/user/${id}`)
}

/**
 * 更新用户状态
 * @param {number} id - 用户ID
 * @param {number} status - 状态值
 * @returns {Promise} - 更新结果
 */
export const updateUserStatus = (id, status) => {
  return put(`/user/${id}/status?status=${status}`)
}

/**
 * 获取当前登录用户信息
 * @returns {Promise} - 用户信息
 */
export const getCurrentUser = () => {
  return get('/user/current')
}

/**
 * 获取用户个人资料
 * @returns {Promise} - 用户个人资料
 */
export const getUserProfile = () => {
  return get('/user/current')
}

/**
 * 更新当前用户信息
 * @param {Object} data - 用户数据
 * @returns {Promise} - 更新结果
 */
export const updateCurrentUser = (data) => {
  return put('/user/profile', data)
}

/**
 * 更新用户个人资料
 * @param {Object} data - 用户数据
 * @returns {Promise} - 更新结果
 */
export const updateUserProfile = (data) => {
  return put('/user/profile', data)
}

/**
 * 修改当前用户密码
 * @param {Object} data - 密码数据
 * @returns {Promise} - 修改结果
 */
export const changePassword = (data) => {
  return put('/user/change-password', data)
}

// 兼容旧版本的API
export const getUserInfo = getCurrentUser