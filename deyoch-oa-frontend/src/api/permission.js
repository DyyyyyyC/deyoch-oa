import { get, post, put, del } from '@/utils/axios'

/**
 * 权限管理相关API
 */

/**
 * 获取权限列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 权限列表
 */
export const getPermissionList = (params = {}) => {
  return get('/permission/list', params)
}

/**
 * 根据ID获取权限详情
 * @param {number} id - 权限ID
 * @returns {Promise} - 权限详情
 */
export const getPermissionById = (id) => {
  return get(`/permission/${id}`)
}

/**
 * 创建权限
 * @param {Object} data - 权限数据
 * @returns {Promise} - 创建结果
 */
export const createPermission = (data) => {
  return post('/permission', data)
}

/**
 * 更新权限
 * @param {number} id - 权限ID
 * @param {Object} data - 权限数据
 * @returns {Promise} - 更新结果
 */
export const updatePermission = (id, data) => {
  return put(`/permission/${id}`, data)
}

/**
 * 删除权限
 * @param {number} id - 权限ID
 * @returns {Promise} - 删除结果
 */
export const deletePermission = (id) => {
  return del(`/permission/${id}`)
}

/**
 * 获取权限树
 * @returns {Promise} - 权限树数据
 */
export const getPermissionTree = () => {
  return get('/permission/tree')
}

/**
 * 获取角色的权限列表
 * @param {number} roleId - 角色ID
 * @returns {Promise} - 权限ID数组
 */
export const getRolePermissions = (roleId) => {
  return get(`/role/${roleId}/perms`)
}