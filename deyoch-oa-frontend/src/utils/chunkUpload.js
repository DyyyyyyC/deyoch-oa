import SparkMD5 from 'spark-md5'
import {
  initChunkUpload,
  uploadChunk,
  mergeChunks,
  cancelUpload,
  checkChunkExists
} from '@/api/upload'

/**
 * 分片上传工具类
 */
export class ChunkUploader {
  constructor(options = {}) {
    // 默认配置
    this.chunkSize = options.chunkSize || 2 * 1024 * 1024 // 2MB
    this.maxConcurrent = options.maxConcurrent || 3 // 最大并发数
    this.retryTimes = options.retryTimes || 3 // 重试次数
    this.enableMd5 = options.enableMd5 !== false // 是否启用MD5校验
    
    // 回调函数
    this.onProgress = options.onProgress || (() => {})
    this.onSuccess = options.onSuccess || (() => {})
    this.onError = options.onError || (() => {})
    this.onCancel = options.onCancel || (() => {})
    
    // 状态变量
    this.file = null
    this.uploadId = null
    this.chunks = []
    this.uploadedChunks = new Set()
    this.isUploading = false
    this.isCancelled = false
    this.fileMd5 = null
  }

  /**
   * 开始上传文件
   * @param {File} file 要上传的文件
   * @param {number} userId 用户ID
   */
  async upload(file, userId) {
    try {
      this.file = file
      this.isUploading = true
      this.isCancelled = false
      this.uploadedChunks.clear()

      // 1. 分片文件
      this.chunks = this.createChunks(file)
      
      // 2. 计算文件MD5（可选）
      if (this.enableMd5) {
        this.fileMd5 = await this.calculateFileMd5(file)
      }

      // 3. 初始化上传
      const initResult = await initChunkUpload({
        fileName: file.name,
        fileSize: file.size,
        totalChunks: this.chunks.length,
        md5Hash: this.fileMd5,
        userId: userId
      })

      if (!initResult.success) {
        throw new Error(initResult.message)
      }

      this.uploadId = initResult.data.uploadId

      // 4. 检查已上传的分片（断点续传）
      await this.checkExistingChunks()

      // 5. 上传分片
      await this.uploadChunks()

      // 6. 合并分片
      const mergeResult = await mergeChunks(this.uploadId)
      if (!mergeResult.success) {
        throw new Error(mergeResult.message)
      }

      this.isUploading = false
      this.onSuccess(mergeResult.data)

    } catch (error) {
      this.isUploading = false
      if (!this.isCancelled) {
        this.onError(error)
      }
    }
  }

  /**
   * 取消上传
   */
  async cancel() {
    this.isCancelled = true
    this.isUploading = false

    if (this.uploadId) {
      try {
        await cancelUpload(this.uploadId)
      } catch (error) {
        console.error('取消上传失败:', error)
      }
    }

    this.onCancel()
  }

  /**
   * 创建文件分片
   * @param {File} file 文件对象
   * @returns {Array} 分片数组
   */
  createChunks(file) {
    const chunks = []
    let start = 0

    while (start < file.size) {
      const end = Math.min(start + this.chunkSize, file.size)
      chunks.push({
        index: chunks.length,
        start: start,
        end: end,
        blob: file.slice(start, end)
      })
      start = end
    }

    return chunks
  }

  /**
   * 计算文件MD5
   * @param {File} file 文件对象
   * @returns {Promise<string>} MD5哈希值
   */
  calculateFileMd5(file) {
    return new Promise((resolve, reject) => {
      const spark = new SparkMD5.ArrayBuffer()
      const fileReader = new FileReader()
      let currentChunk = 0
      const chunks = Math.ceil(file.size / this.chunkSize)

      fileReader.onload = (e) => {
        spark.append(e.target.result)
        currentChunk++

        if (currentChunk < chunks) {
          loadNext()
        } else {
          resolve(spark.end())
        }
      }

      fileReader.onerror = () => {
        reject(new Error('计算文件MD5失败'))
      }

      const loadNext = () => {
        const start = currentChunk * this.chunkSize
        const end = Math.min(start + this.chunkSize, file.size)
        fileReader.readAsArrayBuffer(file.slice(start, end))
      }

      loadNext()
    })
  }

  /**
   * 检查已上传的分片（断点续传）
   */
  async checkExistingChunks() {
    const checkPromises = this.chunks.map(async (chunk) => {
      try {
        const result = await checkChunkExists(this.uploadId, chunk.index)
        if (result.success && result.data) {
          this.uploadedChunks.add(chunk.index)
        }
      } catch (error) {
        console.warn(`检查分片 ${chunk.index} 失败:`, error)
      }
    })

    await Promise.all(checkPromises)
    
    // 更新进度
    this.updateProgress()
  }

  /**
   * 上传所有分片
   */
  async uploadChunks() {
    const pendingChunks = this.chunks.filter(chunk => 
      !this.uploadedChunks.has(chunk.index)
    )

    if (pendingChunks.length === 0) {
      return // 所有分片都已上传
    }

    // 控制并发上传
    const uploadQueue = [...pendingChunks]
    const uploadingPromises = []

    while (uploadQueue.length > 0 || uploadingPromises.length > 0) {
      if (this.isCancelled) {
        throw new Error('上传已取消')
      }

      // 启动新的上传任务
      while (uploadingPromises.length < this.maxConcurrent && uploadQueue.length > 0) {
        const chunk = uploadQueue.shift()
        const promise = this.uploadSingleChunk(chunk)
        uploadingPromises.push(promise)
      }

      // 等待任意一个上传完成
      if (uploadingPromises.length > 0) {
        const completedIndex = await Promise.race(
          uploadingPromises.map((promise, index) => 
            promise.then(() => index).catch(() => index)
          )
        )

        // 移除已完成的Promise
        uploadingPromises.splice(completedIndex, 1)
      }
    }
  }

  /**
   * 上传单个分片
   * @param {Object} chunk 分片对象
   */
  async uploadSingleChunk(chunk) {
    let retryCount = 0

    while (retryCount <= this.retryTimes) {
      if (this.isCancelled) {
        throw new Error('上传已取消')
      }

      try {
        // 计算分片MD5（可选）
        let chunkMd5 = null
        if (this.enableMd5) {
          chunkMd5 = await this.calculateChunkMd5(chunk.blob)
        }

        // 上传分片
        const result = await uploadChunk({
          uploadId: this.uploadId,
          chunkIndex: chunk.index,
          chunk: chunk.blob,
          chunkMd5: chunkMd5
        })

        if (result.success) {
          this.uploadedChunks.add(chunk.index)
          this.updateProgress()
          return
        } else {
          throw new Error(result.message)
        }

      } catch (error) {
        retryCount++
        if (retryCount > this.retryTimes) {
          throw new Error(`分片 ${chunk.index} 上传失败: ${error.message}`)
        }
        
        // 等待一段时间后重试
        await this.sleep(1000 * retryCount)
      }
    }
  }

  /**
   * 计算分片MD5
   * @param {Blob} blob 分片数据
   * @returns {Promise<string>} MD5哈希值
   */
  calculateChunkMd5(blob) {
    return new Promise((resolve, reject) => {
      const spark = new SparkMD5.ArrayBuffer()
      const fileReader = new FileReader()

      fileReader.onload = (e) => {
        spark.append(e.target.result)
        resolve(spark.end())
      }

      fileReader.onerror = () => {
        reject(new Error('计算分片MD5失败'))
      }

      fileReader.readAsArrayBuffer(blob)
    })
  }

  /**
   * 更新上传进度
   */
  updateProgress() {
    const progress = {
      uploadedChunks: this.uploadedChunks.size,
      totalChunks: this.chunks.length,
      percentage: Math.round((this.uploadedChunks.size / this.chunks.length) * 100),
      uploadedSize: this.uploadedChunks.size * this.chunkSize,
      totalSize: this.file.size
    }

    this.onProgress(progress)
  }

  /**
   * 延时函数
   * @param {number} ms 延时毫秒数
   */
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
}

/**
 * 格式化文件大小
 * @param {number} size 文件大小（字节）
 * @returns {string} 格式化后的大小
 */
export function formatFileSize(size) {
  if (size === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(size) / Math.log(k))
  
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + units[i]
}

/**
 * 获取文件扩展名
 * @param {string} fileName 文件名
 * @returns {string} 扩展名
 */
export function getFileExtension(fileName) {
  return fileName.slice((fileName.lastIndexOf('.') - 1 >>> 0) + 2)
}

/**
 * 检查文件类型是否允许
 * @param {File} file 文件对象
 * @param {Array} allowedTypes 允许的文件类型
 * @returns {boolean} 是否允许
 */
export function isFileTypeAllowed(file, allowedTypes) {
  if (!allowedTypes || allowedTypes.length === 0) {
    return true
  }
  
  const fileType = file.type
  const fileName = file.name
  const extension = getFileExtension(fileName).toLowerCase()
  
  return allowedTypes.some(type => {
    if (type.startsWith('.')) {
      return type.toLowerCase() === '.' + extension
    } else if (type.includes('/')) {
      return fileType === type
    } else {
      return extension === type.toLowerCase()
    }
  })
}