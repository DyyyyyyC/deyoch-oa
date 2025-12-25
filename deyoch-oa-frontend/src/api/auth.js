// 认证相关API
import { post } from '@/utils/axios'

/**
 * 登录请求
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} - 登录结果
 */
export const login = (data) => post('/auth/login', data)

/**
 * 登出请求
 * @returns {Promise} - 登出结果
 */
export const logout = () => post('/auth/logout')