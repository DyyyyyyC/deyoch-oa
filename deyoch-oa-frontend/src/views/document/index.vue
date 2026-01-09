<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('documentManagement.title') }}</h2>
    </div>

    <!-- 文档列表 -->
    <el-card class="table-card">
      <!-- 操作和搜索区域 -->
      <div class="action-search-area">
        <!-- 左侧操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="handleAddDocument">
            <el-icon><Plus /></el-icon>{{ $t('documentManagement.addDocument') }}
          </el-button>
          <el-button type="primary" @click="handleBatchEdit" :disabled="selectedDocuments.length !== 1">
            <el-icon><Edit /></el-icon>{{ $t('common.edit') }}
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedDocuments.length === 0">
            <el-icon><Delete /></el-icon>{{ $t('common.delete') }}
          </el-button>
        </div>
        
        <!-- 右侧搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" inline>
            <el-form-item>
              <el-input 
                v-model="searchForm.title" 
                :placeholder="$t('documentManagement.enterTitle')" 
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-select 
                v-model="searchForm.status" 
                :placeholder="$t('userManagement.selectStatus')" 
                clearable
              >
                <el-option :label="$t('common.enabled')" value="1" />
                <el-option :label="$t('common.disabled')" value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
              <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="documentList"
        border
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="fileName" :label="$t('documentManagement.fileName')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" min-width="80" align="center">
          <template #default="scope">
            <el-tag size="small" type="info">v{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" :label="$t('documentManagement.uploader')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="fileType" :label="$t('documentManagement.fileType')" min-width="100" show-overflow-tooltip />
        <el-table-column prop="fileSize" :label="$t('documentManagement.fileSize')" min-width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('documentManagement.status')" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? $t('common.enabled') : $t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('documentManagement.uploadTime')" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column prop="updatedAt" :label="$t('documentManagement.updateTime')" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column label="操作" min-width="260" align="center" fixed="right">
          <template #default="scope">
            <div class="operation-buttons">
              <el-button type="primary" size="small" @click="handleDownload(scope.row)">
                <el-icon><Download /></el-icon>下载
              </el-button>
              <el-button type="info" size="small" @click="handleViewVersions(scope.row)">
                <el-icon><Clock /></el-icon>版本
              </el-button>
              <el-button type="success" size="small" @click="handleUploadNewVersion(scope.row)">
                <el-icon><Upload /></el-icon>新版本
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 文档表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form ref="documentFormRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="文件" prop="file">
          <!-- 使用分片上传组件 -->
          <ChunkUpload
            v-if="!formData.id"
            ref="chunkUploadRef"
            @success="handleUploadSuccess"
            @error="handleUploadError"
            :accept="'.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar'"
            :max-size="2048"
          />
          <!-- 编辑模式显示当前文件信息 -->
          <div v-else class="current-file-info">
            <el-icon><Document /></el-icon>
            <span>{{ formData.fileName }}</span>
            <el-tag size="small" type="info">v{{ formData.version }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item v-if="isNewVersion" label="变更日志" prop="changeLog">
          <el-input
            v-model="formData.changeLog"
            type="textarea"
            :rows="3"
            placeholder="请描述本次更新的内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('documentManagement.status')" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            :active-text="$t('common.enabled')"
            :inactive-text="$t('common.disabled')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="uploading">
            {{ uploading ? '上传中...' : $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 版本历史弹窗 -->
    <el-dialog
      v-model="versionDialogVisible"
      title="版本历史"
      width="80%"
      :close-on-click-modal="false"
    >
      <el-table
        v-loading="versionLoading"
        :data="versionList"
        border
        style="width: 100%"
      >
        <el-table-column prop="version" label="版本号" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isCurrent ? 'success' : 'info'" size="small">
              v{{ scope.row.version }}
              <span v-if="scope.row.isCurrent">(当前)</span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="changeLog" label="变更日志" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.changeLog">{{ scope.row.changeLog }}</span>
            <span v-else class="text-muted">无变更日志</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="创建者" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleDownloadVersion(scope.row)">
              <el-icon><Download /></el-icon>下载
            </el-button>
            <el-button 
              v-if="!scope.row.isCurrent" 
              type="warning" 
              size="small" 
              @click="handleRevertVersion(scope.row)"
            >
              <el-icon><RefreshLeft /></el-icon>回退
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="versionDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 文档管理页面特定样式 */
.content-preview {
  cursor: help;
  color: #606266;
  font-size: 14px;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 调整操作按钮间距和布局 */
.operation-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: nowrap;
  align-items: center;
}

.operation-buttons .el-button {
  margin-right: 0;
  flex-shrink: 0;
  min-width: 70px;
}

:deep(.el-button--small) {
  margin-right: 0;
  padding: 6px 10px;
  font-size: 12px;
}

/* 调整表格内容居中 */
:deep(.el-table__content) {
  font-size: 14px;
}

/* 当前文件信息样式 */
.current-file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.current-file-info .el-icon {
  color: #409eff;
}

/* 文本静音样式 */
.text-muted {
  color: #909399;
  font-style: italic;
}

/* 版本标签样式 */
.version-tag {
  margin-left: 8px;
}
</style>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Download, Edit, Delete, View, Clock, Upload, 
  Document, RefreshLeft
} from '@element-plus/icons-vue'
import {
  getDocumentList,
  createDocument,
  updateDocument,
  deleteDocument,
  uploadDocument,
  downloadDocument,
  uploadNewVersion,
  getDocumentVersions,
  downloadDocumentVersion,
  revertToVersion
} from '@/api/document'
import ChunkUpload from '@/components/ChunkUpload.vue'
import '@/style/management-layout.css'

const { t } = useI18n()

// 状态管理
const loading = ref(false)
const uploading = ref(false)
const versionLoading = ref(false)
const dialogVisible = ref(false)
const versionDialogVisible = ref(false)
const dialogTitle = ref('')
const isNewVersion = ref(false)
const currentDocument = ref(null)

const searchForm = reactive({
  title: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const documentList = ref([])
const selectedDocuments = ref([])
const versionList = ref([])
const documentFormRef = ref(null)
const chunkUploadRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  fileName: '',
  filePath: '',
  fileSize: 0,
  fileType: '',
  userId: null,
  deptId: null,
  status: 1,
  version: '1.0',
  changeLog: ''
})

// 表单验证规则
const rules = reactive({
  file: [
    { 
      required: true, 
      validator: (rule, value, callback) => {
        if (!formData.id && !chunkUploadRef.value?.hasFile()) {
          callback(new Error('请选择要上传的文件'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  changeLog: [
    { 
      required: true, 
      message: '请输入变更日志', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (isNewVersion.value && !value?.trim()) {
          callback(new Error('上传新版本时必须填写变更日志'))
        } else {
          callback()
        }
      }
    }
  ]
})

// 文件大小格式化
const formatFileSize = (size) => {
  if (!size) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(size) / Math.log(k))
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 分片上传成功回调
const handleUploadSuccess = (result) => {
  ElMessage.success('文件上传成功')
  formData.fileName = result.fileName
  formData.filePath = result.filePath
  formData.fileSize = result.fileSize
  formData.fileType = result.fileType
}

// 分片上传失败回调
const handleUploadError = (error) => {
  ElMessage.error('文件上传失败：' + error.message)
}

// 加载文档列表
const loadDocumentList = () => {
  loading.value = true
  getDocumentList({
    page: pagination.currentPage,
    size: pagination.pageSize,
    keyword: searchForm.title,
    status: searchForm.status
  })
    .then(res => {
      let documents = []
      let total = 0
      
      if (res && typeof res === 'object') {
        if (res.records && Array.isArray(res.records)) {
          documents = res.records
          total = res.total || 0
        } else if (Array.isArray(res)) {
          documents = res
          total = res.length
        }
      }
      
      documentList.value = documents
      pagination.total = total
    })
    .catch(error => {
      documentList.value = []
      pagination.total = 0
      ElMessage.error('获取文档列表失败：' + (error.message || '未知错误'))
    })
    .finally(() => {
      loading.value = false
    })
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadDocumentList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.title = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadDocumentList()
}

// 分页变化
const handleSizeChange = (newSize) => {
  pagination.pageSize = newSize
  pagination.currentPage = 1
  loadDocumentList()
}

const handleCurrentChange = (newPage) => {
  pagination.currentPage = newPage
  loadDocumentList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedDocuments.value = selection
}

// 查看版本历史
const handleViewVersions = (row) => {
  currentDocument.value = row
  versionDialogVisible.value = true
  loadVersionHistory(row.id)
}

// 加载版本历史
const loadVersionHistory = (documentId) => {
  versionLoading.value = true
  getDocumentVersions(documentId)
    .then(res => {
      versionList.value = res || []
    })
    .catch(error => {
      ElMessage.error('获取版本历史失败：' + error.message)
      versionList.value = []
    })
    .finally(() => {
      versionLoading.value = false
    })
}

// 上传新版本
const handleUploadNewVersion = (row) => {
  currentDocument.value = row
  dialogTitle.value = `上传新版本 - ${row.fileName}`
  isNewVersion.value = true
  resetForm()
  formData.id = row.id
  formData.fileName = row.fileName
  formData.version = row.version
  formData.status = row.status
  dialogVisible.value = true
}

// 下载指定版本
const handleDownloadVersion = (version) => {
  if (!currentDocument.value) return
  
  versionLoading.value = true
  downloadDocumentVersion(currentDocument.value.id, version.version)
    .then(blob => {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `${version.fileName}_v${version.version}`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('下载成功')
    })
    .catch(error => {
      ElMessage.error('下载失败：' + error.message)
    })
    .finally(() => {
      versionLoading.value = false
    })
}

// 回退版本
const handleRevertVersion = (version) => {
  ElMessageBox.confirm(
    `确定要回退到版本 v${version.version} 吗？此操作会将该版本设为当前版本。`,
    '确认回退',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      versionLoading.value = true
      return revertToVersion(currentDocument.value.id, version.version)
    })
    .then(() => {
      ElMessage.success('版本回退成功')
      loadVersionHistory(currentDocument.value.id)
      loadDocumentList()
    })
    .catch(error => {
      if (error !== 'cancel') {
        ElMessage.error('版本回退失败：' + error.message)
      }
    })
    .finally(() => {
      versionLoading.value = false
    })
}

// 批量编辑文档
const handleBatchEdit = () => {
  if (selectedDocuments.value.length !== 1) {
    ElMessage.warning('请选择一个文档进行编辑')
    return
  }
  const row = selectedDocuments.value[0]
  dialogTitle.value = t('documentManagement.editDocument')
  isNewVersion.value = false
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 批量删除文档
const handleBatchDelete = () => {
  if (selectedDocuments.value.length === 0) {
    ElMessage.warning('请选择要删除的文档')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedDocuments.value.length} 个文档吗？`,
    t('common.confirm'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }
  )
    .then(() => {
      loading.value = true
      const deletePromises = selectedDocuments.value.map(doc => deleteDocument(doc.id))
      Promise.all(deletePromises)
        .then(() => {
          ElMessage.success(t('documentManagement.deleteSuccess'))
          loadDocumentList()
          selectedDocuments.value = []
        })
        .catch(error => {
          ElMessage.error(t('documentManagement.deleteFailed'))
        })
        .finally(() => {
          loading.value = false
        })
    })
    .catch(() => {
      // 用户取消删除
    })
}

// 添加文档
const handleAddDocument = () => {
  dialogTitle.value = t('documentManagement.addDocument')
  isNewVersion.value = false
  resetForm()
  dialogVisible.value = true
}

// 下载文档
const handleDownload = (row) => {
  loading.value = true
  downloadDocument(row.id)
    .then(blob => {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', row.fileName)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success(t('documentManagement.downloadSuccess'))
    })
    .catch(error => {
      console.error('下载文档失败:', error)
      ElMessage.error(t('documentManagement.downloadFailed'))
    })
    .finally(() => {
      loading.value = false
    })
}

// 重置表单
const resetForm = () => {
  if (documentFormRef.value) {
    documentFormRef.value.resetFields()
  }
  Object.assign(formData, {
    id: null,
    fileName: '',
    filePath: '',
    fileSize: 0,
    fileType: '',
    userId: null,
    deptId: null,
    status: 1,
    version: '1.0',
    changeLog: ''
  })
  if (chunkUploadRef.value) {
    chunkUploadRef.value.reset()
  }
}

// 提交表单
const handleSubmit = () => {
  if (documentFormRef.value) {
    documentFormRef.value.validate((valid) => {
      if (valid) {
        uploading.value = true
        
        if (isNewVersion.value && formData.id) {
          // 上传新版本
          const uploadFormData = new FormData()
          const file = chunkUploadRef.value?.getFile()
          if (file) {
            uploadFormData.append('file', file)
            uploadFormData.append('changeLog', formData.changeLog)
            uploadFormData.append('status', formData.status.toString())
            
            uploadNewVersion(formData.id, uploadFormData)
              .then(res => {
                ElMessage.success('新版本上传成功')
                dialogVisible.value = false
                loadDocumentList()
                if (versionDialogVisible.value) {
                  loadVersionHistory(formData.id)
                }
              })
              .catch(error => {
                ElMessage.error('新版本上传失败：' + error.message)
              })
              .finally(() => {
                uploading.value = false
              })
          } else {
            uploading.value = false
            ElMessage.error('请选择要上传的文件')
          }
        } else if (!formData.id) {
          // 上传新文档
          const file = chunkUploadRef.value?.getFile()
          if (file) {
            const uploadFormData = new FormData()
            uploadFormData.append('file', file)
            uploadFormData.append('status', formData.status.toString())
            
            uploadDocument(uploadFormData)
              .then(res => {
                ElMessage.success(t('documentManagement.addSuccess'))
                dialogVisible.value = false
                loadDocumentList()
              })
              .catch(error => {
                ElMessage.error(t('documentManagement.addFailed'))
              })
              .finally(() => {
                uploading.value = false
              })
          } else {
            uploading.value = false
            ElMessage.error('请选择要上传的文件')
          }
        } else {
          // 更新现有文档信息
          updateDocument(formData.id, {
            status: formData.status
          })
            .then(res => {
              ElMessage.success(t('documentManagement.editSuccess'))
              dialogVisible.value = false
              loadDocumentList()
            })
            .catch(error => {
              ElMessage.error(t('documentManagement.editFailed'))
            })
            .finally(() => {
              uploading.value = false
            })
        }
      }
    })
  }
}

// 初始化
onMounted(() => {
  loadDocumentList()
})
</script>
