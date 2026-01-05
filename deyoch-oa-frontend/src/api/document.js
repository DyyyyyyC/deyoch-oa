import { post, get, put, del } from '@/utils/axios'

/**
 * 获取文档列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 返回文档列表
 */
export const getDocumentList = (params = {}) => {
  return get('/document/list', params)
}

/**
 * 根据ID获取文档详情
 * @param {number} id - 文档ID
 * @returns {Promise} - 返回文档详情
 */
export const getDocumentById = (id) => {
  return get(`/document/${id}`)
}

/**
 * 创建文档
 * @param {Object} data - 文档数据
 * @returns {Promise} - 返回创建结果
 */
export const createDocument = (data) => {
  return post('/document', data)
}

/**
 * 更新文档
 * @param {number} id - 文档ID
 * @param {Object} data - 文档数据
 * @returns {Promise} - 返回更新结果
 */
export const updateDocument = (id, data) => {
  return put(`/document/${id}`, data)
}

/**
 * 删除文档
 * @param {number} id - 文档ID
 * @returns {Promise} - 返回删除结果
 */
export const deleteDocument = (id) => {
  return del(`/document/${id}`)
}

/**
 * 更新文档状态
 * @param {number} id - 文档ID
 * @param {number} status - 状态值
 * @returns {Promise} - 返回更新结果
 */
export const updateDocumentStatus = (id, status) => {
  return post(`/document/${id}/status`, { status })
}

/**
 * 上传文档
 * @param {FormData} formData - 表单数据，包含文件和文档信息
 * @returns {Promise} - 返回上传结果
 */
export const uploadDocument = (formData) => {
  return post('/document/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载文档
 * @param {number} id - 文档ID
 * @returns {Promise} - 返回下载结果
 */
export const downloadDocument = (id) => {
  return get(`/document/${id}/download`)
}