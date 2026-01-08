<template>
  <div class="chunk-upload">
    <el-upload
      ref="uploadRef"
      :before-upload="handleBeforeUpload"
      :http-request="handleUpload"
      :show-file-list="false"
      :accept="acceptTypes"
      :multiple="multiple"
      :disabled="uploading"
      drag
    >
      <div class="upload-area">
        <el-icon class="upload-icon" :size="50">
          <UploadFilled />
        </el-icon>
        <div class="upload-text">
          <p>将文件拖到此处，或<em>点击上传</em></p>
          <p class="upload-tip">
            支持大文件上传，{{ maxSizeText }}以内
            <span v-if="acceptTypes">，支持{{ acceptTypesText }}格式</span>
          </p>
        </div>
      </div>
    </el-upload>

    <!-- 上传进度 -->
    <div v-if="uploadList.length > 0" class="upload-list">
      <div
        v-for="item in uploadList"
        :key="item.id"
        class="upload-item"
        :class="{ 'upload-error': item.status === 'error' }"
      >
        <div class="file-info">
          <el-icon class="file-icon">
            <Document />
          </el-icon>
          <div class="file-detail">
            <div class="file-name">{{ item.fileName }}</div>
            <div class="file-size">{{ formatFileSize(item.fileSize) }}</div>
          </div>
        </div>

        <div class="upload-progress">
          <el-progress
            :percentage="item.progress"
            :status="getProgressStatus(item.status)"
            :stroke-width="6"
          />
          <div class="progress-text">
            <span v-if="item.status === 'uploading'">
              {{ item.progress }}% ({{ item.uploadedChunks }}/{{ item.totalChunks }} 分片)
            </span>
            <span v-else-if="item.status === 'success'" class="success-text">
              上传成功
            </span>
            <span v-else-if="item.status === 'error'" class="error-text">
              {{ item.errorMessage }}
            </span>
            <span v-else-if="item.status === 'merging'" class="merging-text">
              合并文件中...
            </span>
          </div>
        </div>

        <div class="upload-actions">
          <el-button
            v-if="item.status === 'uploading'"
            type="danger"
            size="small"
            @click="cancelUpload(item)"
          >
            取消
          </el-button>
          <el-button
            v-else-if="item.status === 'error'"
            type="primary"
            size="small"
            @click="retryUpload(item)"
          >
            重试
          </el-button>
          <el-button
            v-if="item.status !== 'uploading'"
            type="info"
            size="small"
            @click="removeUpload(item)"
          >
            移除
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document } from '@element-plus/icons-vue'
import { ChunkUploader, formatFileSize, isFileTypeAllowed } from '@/utils/chunkUpload'
import { useUserStore } from '@/stores/user'

// Props
const props = defineProps({
  // 接受的文件类型
  acceptTypes: {
    type: String,
    default: ''
  },
  // 最大文件大小（字节）
  maxSize: {
    type: Number,
    default: 100 * 1024 * 1024 // 100MB
  },
  // 是否支持多文件上传
  multiple: {
    type: Boolean,
    default: false
  },
  // 分片大小（字节）
  chunkSize: {
    type: Number,
    default: 2 * 1024 * 1024 // 2MB
  },
  // 最大并发数
  maxConcurrent: {
    type: Number,
    default: 3
  },
  // 是否启用MD5校验
  enableMd5: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits(['success', 'error', 'progress'])

// Store
const userStore = useUserStore()

// 响应式数据
const uploadRef = ref()
const uploading = ref(false)
const uploadList = ref([])
let uploadIdCounter = 0

// 计算属性
const maxSizeText = computed(() => formatFileSize(props.maxSize))

const acceptTypesText = computed(() => {
  if (!props.acceptTypes) return ''
  return props.acceptTypes.split(',').map(type => type.trim()).join('、')
})

// 方法
const handleBeforeUpload = (file) => {
  // 文件大小检查
  if (file.size > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${maxSizeText.value}`)
    return false
  }

  // 文件类型检查
  if (props.acceptTypes) {
    const allowedTypes = props.acceptTypes.split(',').map(type => type.trim())
    if (!isFileTypeAllowed(file, allowedTypes)) {
      ElMessage.error(`不支持的文件格式，请上传 ${acceptTypesText.value} 格式的文件`)
      return false
    }
  }

  return true
}

const handleUpload = async (options) => {
  const file = options.file
  
  // 创建上传项
  const uploadItem = {
    id: ++uploadIdCounter,
    fileName: file.name,
    fileSize: file.size,
    file: file,
    status: 'uploading',
    progress: 0,
    uploadedChunks: 0,
    totalChunks: 0,
    errorMessage: '',
    uploader: null
  }

  uploadList.value.push(uploadItem)
  uploading.value = true

  try {
    // 创建分片上传器
    const uploader = new ChunkUploader({
      chunkSize: props.chunkSize,
      maxConcurrent: props.maxConcurrent,
      enableMd5: props.enableMd5,
      onProgress: (progress) => {
        uploadItem.progress = progress.percentage
        uploadItem.uploadedChunks = progress.uploadedChunks
        uploadItem.totalChunks = progress.totalChunks
        
        emit('progress', {
          file: file,
          progress: progress
        })
      },
      onSuccess: (filePath) => {
        uploadItem.status = 'success'
        uploadItem.progress = 100
        uploadItem.filePath = filePath
        
        ElMessage.success(`${file.name} 上传成功`)
        
        emit('success', {
          fileName: file.name,
          filePath: filePath,
          fileSize: file.size,
          fileType: file.type || file.name.substring(file.name.lastIndexOf('.') + 1),
          file: file,
          uploadItem: uploadItem
        })
        
        checkUploadingStatus()
      },
      onError: (error) => {
        uploadItem.status = 'error'
        uploadItem.errorMessage = error.message || '上传失败'
        
        ElMessage.error(`${file.name} 上传失败: ${uploadItem.errorMessage}`)
        
        emit('error', {
          file: file,
          error: error,
          uploadItem: uploadItem
        })
        
        checkUploadingStatus()
      },
      onCancel: () => {
        uploadItem.status = 'cancelled'
        uploadItem.errorMessage = '上传已取消'
        
        ElMessage.info(`${file.name} 上传已取消`)
        
        checkUploadingStatus()
      }
    })

    uploadItem.uploader = uploader

    // 开始上传
    await uploader.upload(file, userStore.userInfo?.id)

  } catch (error) {
    uploadItem.status = 'error'
    uploadItem.errorMessage = error.message || '上传失败'
    
    ElMessage.error(`${file.name} 上传失败: ${uploadItem.errorMessage}`)
    
    emit('error', {
      file: file,
      error: error,
      uploadItem: uploadItem
    })
    
    checkUploadingStatus()
  }
}

const cancelUpload = async (uploadItem) => {
  if (uploadItem.uploader) {
    await uploadItem.uploader.cancel()
  }
}

const retryUpload = async (uploadItem) => {
  uploadItem.status = 'uploading'
  uploadItem.progress = 0
  uploadItem.uploadedChunks = 0
  uploadItem.errorMessage = ''
  
  uploading.value = true
  
  try {
    await uploadItem.uploader.upload(uploadItem.file, userStore.userInfo?.id)
  } catch (error) {
    uploadItem.status = 'error'
    uploadItem.errorMessage = error.message || '上传失败'
    
    ElMessage.error(`${uploadItem.fileName} 重试失败: ${uploadItem.errorMessage}`)
    
    checkUploadingStatus()
  }
}

const removeUpload = (uploadItem) => {
  const index = uploadList.value.findIndex(item => item.id === uploadItem.id)
  if (index !== -1) {
    uploadList.value.splice(index, 1)
  }
  
  checkUploadingStatus()
}

const checkUploadingStatus = () => {
  const hasUploading = uploadList.value.some(item => item.status === 'uploading')
  uploading.value = hasUploading
}

const getProgressStatus = (status) => {
  switch (status) {
    case 'success':
      return 'success'
    case 'error':
    case 'cancelled':
      return 'exception'
    default:
      return ''
  }
}

// 清空上传列表
const clearUploadList = () => {
  uploadList.value = []
  uploading.value = false
}

// 获取成功上传的文件
const getSuccessUploads = () => {
  return uploadList.value.filter(item => item.status === 'success')
}

// 检查是否有文件
const hasFile = () => {
  return uploadList.value.length > 0
}

// 获取第一个文件（用于单文件上传）
const getFile = () => {
  if (uploadList.value.length > 0) {
    return uploadList.value[0].file
  }
  return null
}

// 重置组件状态
const reset = () => {
  uploadList.value = []
  uploading.value = false
}

// 暴露方法给父组件
defineExpose({
  clearUploadList,
  getSuccessUploads,
  hasFile,
  getFile,
  reset
})
</script>

<style scoped>
.chunk-upload {
  width: 100%;
}

.upload-area {
  padding: 40px 20px;
  text-align: center;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
}

.upload-icon {
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-text p {
  margin: 0 0 8px 0;
  color: #606266;
}

.upload-text em {
  color: #409eff;
  font-style: normal;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.upload-list {
  margin-top: 20px;
}

.upload-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  background: #fafafa;
}

.upload-item.upload-error {
  border-color: #f56c6c;
  background: #fef0f0;
}

.file-info {
  display: flex;
  align-items: center;
  min-width: 200px;
}

.file-icon {
  font-size: 24px;
  color: #409eff;
  margin-right: 12px;
}

.file-detail {
  flex: 1;
}

.file-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  word-break: break-all;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.upload-progress {
  flex: 1;
  margin: 0 20px;
}

.progress-text {
  margin-top: 4px;
  font-size: 12px;
  text-align: center;
}

.success-text {
  color: #67c23a;
}

.error-text {
  color: #f56c6c;
}

.merging-text {
  color: #e6a23c;
}

.upload-actions {
  display: flex;
  gap: 8px;
}
</style>