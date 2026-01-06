<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('documentManagement.title') }}</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('documentManagement.fileName')">
          <el-input v-model="searchForm.title" :placeholder="$t('documentManagement.enterTitle')" clearable />
        </el-form-item>
        <el-form-item :label="$t('documentManagement.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('userManagement.selectStatus')" clearable>
            <el-option :label="$t('common.enabled')" value="1" />
            <el-option :label="$t('common.disabled')" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 文档列表 -->
    <el-card class="table-card">
      <!-- 操作区域 -->
      <div class="action-area">
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
      
      <el-table
        v-loading="loading"
        :data="documentList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="fileName" :label="$t('documentManagement.fileName')" min-width="200">
          <template #default="scope">
            <el-link type="primary" @click="handleViewDocument(scope.row)">
              {{ scope.row.fileName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" :label="$t('documentManagement.fileType')" width="100" align="center" />
        <el-table-column prop="fileSize" :label="$t('documentManagement.fileSize')" width="120" align="center">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('documentManagement.status')" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? $t('common.enabled') : $t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('documentManagement.uploadTime')" width="180" align="center" />
        <el-table-column prop="updatedAt" :label="$t('documentManagement.updateTime')" width="180" align="center" />
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
      width="50%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form ref="documentFormRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item :label="$t('documentManagement.file')" prop="file">
          <el-upload
            v-model:file-list="fileList"
            class="upload-demo"
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
          >
            <el-button type="primary">
              <el-icon><Plus /></el-icon>{{ $t('documentManagement.chooseFile') }}
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                {{ $t('documentManagement.uploadTip') }}
              </div>
            </template>
          </el-upload>
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
          <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
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

/* 调整操作按钮间距 */
:deep(.el-button--small) {
  margin-right: 4px;
}

/* 调整表格内容居中 */
:deep(.el-table__content) {
  font-size: 14px;
}
</style>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Download, Edit, Delete, View
} from '@element-plus/icons-vue'
import {
  getDocumentList,
  createDocument,
  updateDocument,
  deleteDocument,
  uploadDocument,
  downloadDocument
} from '@/api/document'
import '@/style/management-layout.css'

const { t } = useI18n()

// 状态管理
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const searchForm = reactive({
  title: '',
  status: ''
})
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const fileList = ref([])
const documentList = ref([])
const selectedDocuments = ref([])
const documentFormRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  fileName: '',
  filePath: '',
  fileSize: 0,
  fileType: '',
  userId: null,
  deptId: null,
  status: 1
})

// 表单验证规则
const rules = reactive({
  file: [
    { required: true, message: t('documentManagement.titleRequired'), trigger: 'blur' }
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

// 处理文件变化
const handleFileChange = (file) => {
  if (file && file.raw) {
    formData.fileName = file.name
    formData.fileSize = file.size
    formData.fileType = file.type || file.name.substring(file.name.lastIndexOf('.') + 1)
  }
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
      // 处理响应数据
      let documents = []
      
      // 检查响应类型
      if (Array.isArray(res)) {
        // 情况1：直接返回数组
        documents = res
      } else if (res.code === 200) {
        // 情况2：标准Result格式，有code字段且为200
        if (Array.isArray(res.data)) {
          documents = res.data
        }
      } else if (res.data && Array.isArray(res.data)) {
        // 情况3：其他格式，数据在data字段中且为数组
        documents = res.data
      }
      
      // 更新文档列表
      documentList.value = documents
      pagination.total = documents.length
    })
    .catch(error => {
      documentList.value = []
      pagination.total = 0
      ElMessage.error('获取文档列表失败，请检查网络连接')
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

// 批量编辑文档
const handleBatchEdit = () => {
  if (selectedDocuments.value.length !== 1) {
    ElMessage.warning('请选择一个文档进行编辑')
    return
  }
  // 填充表单数据
  const row = selectedDocuments.value[0]
  dialogTitle.value = t('documentManagement.editDocument')
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
      // 批量删除
      const deletePromises = selectedDocuments.value.map(doc => deleteDocument(doc.id))
      Promise.all(deletePromises)
        .then(() => {
          ElMessage.success(t('documentManagement.deleteSuccess'))
          loadDocumentList()
          // 清空选择
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
  resetForm()
  dialogVisible.value = true
}

// 编辑文档
const handleEditDocument = (row) => {
  dialogTitle.value = t('documentManagement.editDocument')
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看文档
const handleViewDocument = (row) => {
  // 这里可以跳转到文档详情页
}

// 下载文档
const handleDownload = (row) => {
  loading.value = true
  downloadDocument(row.id)
    .then(res => {
      // 处理下载结果，通常会返回文件URL或直接返回文件流
      ElMessage.success(t('documentManagement.downloadSuccess'))
      // 这里可以根据后端返回的结果进行文件下载处理
      // 例如：window.open(res.data, '_blank')
    })
    .catch(error => {
      console.error('下载文档失败:', error)
      ElMessage.error(t('documentManagement.downloadFailed'))
    })
    .finally(() => {
      loading.value = false
    })
}

// 删除文档
const handleDeleteDocument = (row) => {
  ElMessageBox.confirm(
    t('documentManagement.deleteConfirm'),
    t('common.confirm'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }
  )
    .then(() => {
      loading.value = true
      return deleteDocument(row.id)
    })
    .then(() => {
      ElMessage.success(t('documentManagement.deleteSuccess'))
      loadDocumentList()
    })
    .catch(error => {
      if (error !== 'cancel') {
        console.error('删除文档失败:', error)
        ElMessage.error(t('documentManagement.deleteFailed'))
      }
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
    status: 1
  })
  fileList.value = []
}

// 提交表单
const handleSubmit = () => {
  if (documentFormRef.value) {
    documentFormRef.value.validate((valid) => {
      if (valid) {
        loading.value = true
        // 根据是否有文件判断是上传还是更新
        const request = fileList.value.length > 0 ? 
          uploadDocument(fileList.value[0].raw, formData) : 
          updateDocument(formData.id, formData)
        
        request
          .then(res => {
            ElMessage.success(formData.id ? t('documentManagement.editSuccess') : t('documentManagement.addSuccess'))
            dialogVisible.value = false
            loadDocumentList()
          })
          .catch(error => {
            ElMessage.error(formData.id ? t('documentManagement.editFailed') : t('documentManagement.addFailed'))
          })
          .finally(() => {
            loading.value = false
          })
      }
    })
  }
}

// 初始化
onMounted(() => {
  loadDocumentList()
})
</script>
