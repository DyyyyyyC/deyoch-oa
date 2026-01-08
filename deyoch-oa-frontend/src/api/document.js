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
  return get(`/document/${id}/download`, {}, {
    responseType: 'blob'
  })
}

/**
 * 获取工作台最近文件（限制数量）
 * 使用现有接口，限制返回数量
 * @returns {Promise} - 最近文件列表
 */
export const getRecentDocuments = () => {
  return get('/document/list', { 
    page: 1,
    size: 5 // 限制返回5条最近文件
  })
}

// ========================================
// 文档版本管理相关API
// ========================================

/**
 * 上传新版本文档
 * @param {number} documentId - 文档ID
 * @param {FormData} formData - 表单数据，包含文件和变更日志
 * @returns {Promise} - 返回上传结果
 */
export const uploadNewVersion = (documentId, formData) => {
  return post(`/document/${documentId}/version`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取文档版本历史
 * @param {number} documentId - 文档ID
 * @returns {Promise} - 返回版本历史列表
 */
export const getDocumentVersions = (documentId) => {
  return get(`/document/${documentId}/versions`)
}

/**
 * 下载指定版本文档
 * @param {number} documentId - 文档ID
 * @param {string} version - 版本号
 * @returns {Promise} - 返回下载结果
 */
export const downloadDocumentVersion = (documentId, version) => {
  return get(`/document/${documentId}/version/${version}/download`, {}, {
    responseType: 'blob'
  })
}

/**
 * 回退到指定版本
 * @param {number} documentId - 文档ID
 * @param {string} version - 版本号
 * @returns {Promise} - 返回回退结果
 */
export const revertToVersion = (documentId, version) => {
  return post(`/document/${documentId}/revert/${version}`)
}

/**
 * 比较文档版本
 * @param {number} documentId - 文档ID
 * @param {string} version1 - 版本1
 * @param {string} version2 - 版本2
 * @returns {Promise} - 返回比较结果
 */
export const compareVersions = (documentId, version1, version2) => {
  return get(`/document/${documentId}/compare`, {
    version1,
    version2
  })
}