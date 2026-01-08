import request from '@/utils/axios'

/**
 * 分片上传API
 */

/**
 * 初始化分片上传
 * @param {Object} data 上传参数
 * @param {string} data.fileName 文件名
 * @param {number} data.fileSize 文件大小
 * @param {number} data.totalChunks 总分片数
 * @param {string} data.md5Hash 文件MD5哈希值
 * @param {number} data.userId 用户ID
 */
export function initChunkUpload(data) {
  return request({
    url: '/upload/chunk/init',
    method: 'post',
    params: data
  })
}

/**
 * 上传分片
 * @param {Object} data 分片数据
 * @param {string} data.uploadId 上传ID
 * @param {number} data.chunkIndex 分片索引
 * @param {File} data.chunk 分片文件
 * @param {string} data.chunkMd5 分片MD5哈希值
 */
export function uploadChunk(data) {
  const formData = new FormData()
  formData.append('uploadId', data.uploadId)
  formData.append('chunkIndex', data.chunkIndex)
  formData.append('chunk', data.chunk)
  if (data.chunkMd5) {
    formData.append('chunkMd5', data.chunkMd5)
  }

  return request({
    url: '/upload/chunk/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 合并分片
 * @param {string} uploadId 上传ID
 */
export function mergeChunks(uploadId) {
  return request({
    url: '/upload/chunk/merge',
    method: 'post',
    params: { uploadId }
  })
}

/**
 * 获取上传进度
 * @param {string} uploadId 上传ID
 */
export function getUploadProgress(uploadId) {
  return request({
    url: '/upload/chunk/progress',
    method: 'get',
    params: { uploadId }
  })
}

/**
 * 取消上传
 * @param {string} uploadId 上传ID
 */
export function cancelUpload(uploadId) {
  return request({
    url: '/upload/chunk/cancel',
    method: 'post',
    params: { uploadId }
  })
}

/**
 * 检查分片是否存在
 * @param {string} uploadId 上传ID
 * @param {number} chunkIndex 分片索引
 */
export function checkChunkExists(uploadId, chunkIndex) {
  return request({
    url: '/upload/chunk/check',
    method: 'get',
    params: { uploadId, chunkIndex }
  })
}

/**
 * 清理过期上传记录
 */
export function cleanExpiredUploads() {
  return request({
    url: '/upload/chunk/cleanup',
    method: 'post'
  })
}