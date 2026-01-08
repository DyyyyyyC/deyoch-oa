import request from '@/utils/axios'

/**
 * 通讯录管理API
 */

/**
 * 分页查询通讯录
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页大小
 * @param {string} params.keyword 搜索关键词
 * @param {number} params.deptId 部门ID
 */
export function getContactDirectory(params) {
  return request({
    url: '/contact/directory',
    method: 'get',
    params
  })
}

/**
 * 获取组织架构树
 */
export function getOrganizationTree() {
  return request({
    url: '/contact/org-tree',
    method: 'get'
  })
}

/**
 * 搜索联系人
 * @param {string} keyword 搜索关键词
 */
export function searchContacts(keyword) {
  return request({
    url: '/contact/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 获取联系人详情
 * @param {number} userId 用户ID
 */
export function getContactDetail(userId) {
  return request({
    url: `/contact/${userId}`,
    method: 'get'
  })
}

/**
 * 导出通讯录
 */
export function exportContacts() {
  return request({
    url: '/contact/export',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 导入通讯录
 * @param {FormData} formData 包含文件的表单数据
 */
export function importContacts(formData) {
  return request({
    url: '/contact/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}