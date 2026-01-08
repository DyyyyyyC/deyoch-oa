import { get, post, put, del } from '@/utils/axios'

/**
 * 角色管理相关API
 */

/**
 * 获取角色列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 角色列表
 */
export const getRoleList = (params = {}) => {
  return get('/role/list', params)
}

/**
 * 根据ID获取角色详情
 * @param {number} id - 角色ID
 * @returns {Promise} - 角色详情
 */
export const getRoleById = (id) => {
  return get(`/role/${id}`)
}

/**
 * 创建角色
 * @param {Object} data - 角色数据
 * @returns {Promise} - 创建结果
 */
export const createRole = (data) => {
  return post('/role', data)
}

/**
 * 更新角色
 * @param {number} id - 角色ID
 * @param {Object} data - 角色数据
 * @returns {Promise} - 更新结果
 */
export const updateRole = (id, data) => {
  return put(`/role/${id}`, data)
}

/**
 * 删除角色
 * @param {number} id - 角色ID
 * @returns {Promise} - 删除结果
 */
export const deleteRole = (id) => {
  return del(`/role/${id}`)
}

/**
 * 分配角色权限
 * @param {number} id - 角色ID
 * @param {Array} permissionIds - 权限ID数组
 * @returns {Promise} - 分配结果
 */
export const assignRolePermissions = (id, permissionIds) => {
  return post(`/role/${id}/assign-perms`, permissionIds)
}