// 用户信息API
import { get } from '@/utils/axios'

/**
 * 获取用户信息
 * @returns {Promise} - 用户信息
 */
export const getUserInfo = () => get('/user/info')

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 用户列表
 */
export const getUserList = (params) => get('/user/list', params)