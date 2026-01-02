<template>
  <div class="announcement-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('announcementManagement.title') }}</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('announcementManagement.title')">
          <el-input v-model="searchForm.title" :placeholder="$t('announcementManagement.enterTitle')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 公告列表 -->
    <el-card class="table-card">
      <!-- 操作区域 -->
      <div class="action-area">
        <el-button type="primary" @click="handleAddAnnouncement">
          <el-icon><Plus /></el-icon>
          {{ $t('announcementManagement.addAnnouncement') }}
        </el-button>
        <el-button type="primary" @click="handleBatchEdit" :disabled="selectedAnnouncements.length !== 1">
          <el-icon><Edit /></el-icon>
          {{ $t('common.edit') }}
        </el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedAnnouncements.length === 0">
          <el-icon><Delete /></el-icon>
          {{ $t('common.delete') }}
        </el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="announcementList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" :label="$t('announcementManagement.title')" />
        <el-table-column prop="publisher" :label="$t('announcementManagement.publisher')" />
        <el-table-column prop="publishTime" :label="$t('announcementManagement.publishTime')" width="200" />
        <el-table-column prop="status" :label="$t('announcementManagement.status')" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('common.createdAt')" width="200" />
        <el-table-column prop="updatedAt" :label="$t('common.updatedAt')" width="200" />
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

    <!-- 公告表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="announcementFormRef"
        :model="announcementForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item :label="$t('announcementManagement.title')" prop="title">
          <el-input
            v-model="announcementForm.title"
            :placeholder="$t('announcementManagement.enterTitle')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('announcementManagement.publisher')" prop="publisher">
          <el-input
            v-model="announcementForm.publisher"
            :placeholder="$t('announcementManagement.enterPublisher')"
            maxlength="50"
            show-word-limit
            :disabled="true"
          />
        </el-form-item>
        <el-form-item :label="$t('announcementManagement.content')" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :placeholder="$t('announcementManagement.enterContent')"
            :rows="6"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { 
  getAnnouncementList as getAnnouncementListApi, 
  createAnnouncement, 
  updateAnnouncement, 
  deleteAnnouncement, 
  publishAnnouncement, 
  revokeAnnouncement 
} from '@/api/announcement'

// 获取i18n的t函数
const { t } = useI18n()

// 获取用户store
const userStore = useUserStore()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  title: ''
})

// 公告列表
const announcementList = ref([])

// 选中的公告
const selectedAnnouncements = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const announcementFormRef = ref(null)
const isEditMode = ref(false)

// 对话框标题
const dialogTitle = computed(() => {
  return isEditMode.value ? t('announcementManagement.editAnnouncement') : t('announcementManagement.addAnnouncement')
})

// 公告表单数据
const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  publisher: '',
  status: 0
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: t('announcementManagement.enterTitle'), trigger: 'blur' },
    { min: 2, max: 100, message: t('common.fieldLength', { min: 2, max: 100, field: t('announcementManagement.title') }), trigger: 'blur' }
  ],
  publisher: [
    { required: true, message: t('announcementManagement.enterPublisher'), trigger: 'blur' },
    { min: 1, max: 50, message: t('common.fieldLength', { min: 1, max: 50, field: t('announcementManagement.publisher') }), trigger: 'blur' }
  ],
  content: [
    { required: true, message: t('announcementManagement.enterContent'), trigger: 'blur' },
    { min: 10, max: 1000, message: t('common.fieldLength', { min: 10, max: 1000, field: t('announcementManagement.content') }), trigger: 'blur' }
  ]
}

// 获取公告列表
const getAnnouncementList = async () => {
  loading.value = true
  try {
    const data = await getAnnouncementListApi()
    announcementList.value = data
    pagination.total = data.length
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取公告列表失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 搜索公告
const handleSearch = () => {
  getAnnouncementList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.title = ''
  getAnnouncementList()
}

// 处理多选框选择变化
const handleSelectionChange = (selection) => {
  selectedAnnouncements.value = selection
  console.log('选中的公告：', selectedAnnouncements.value)
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getAnnouncementList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getAnnouncementList()
}

// 打开添加公告对话框
const handleAddAnnouncement = () => {
  isEditMode.value = false
  resetForm()
  // 设置发布人为当前登录用户
  announcementForm.publisher = userStore.userInfo?.username || ''
  dialogVisible.value = true
}

// 批量编辑公告
const handleBatchEdit = () => {
  if (selectedAnnouncements.length !== 1) {
    ElMessage.warning('请选择一个公告进行编辑')
    return
  }
  // 填充表单数据
  const row = selectedAnnouncements[0]
  isEditMode.value = true
  announcementForm.id = row.id
  announcementForm.title = row.title
  announcementForm.content = row.content
  announcementForm.publisher = row.publisher
  announcementForm.status = row.status
  dialogVisible.value = true
}

// 批量删除公告
const handleBatchDelete = async () => {
  if (selectedAnnouncements.length === 0) {
    ElMessage.warning('请选择要删除的公告')
    return
  }
  
  try {
    const titles = selectedAnnouncements.map(announcement => announcement.title).join(', ')
    await ElMessageBox.confirm(
      t('common.confirmDelete'),
      t('common.confirm'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    // 批量删除
    for (const announcement of selectedAnnouncements) {
      const response = await deleteAnnouncement(announcement.id)
      if (response.code !== 200) {
        throw new Error(response.message)
      }
    }
    
    ElMessage.success(t('announcementManagement.deleteSuccess'))
    getAnnouncementList()
    // 清空选择
    selectedAnnouncements.value = []
  } catch (error) {
    if (error !== 'cancel') {
      // 只在error.message不为空时显示详细错误信息
      if (error.message) {
        ElMessage.error(t('announcementManagement.deleteFailed') + '：' + error.message)
      }
    }
  }
}

// 重置表单
const resetForm = () => {
  if (announcementFormRef.value) {
    announcementFormRef.value.resetFields()
  }
  announcementForm.id = null
  announcementForm.title = ''
  announcementForm.content = ''
  announcementForm.publisher = ''
  announcementForm.status = 0
}

// 提交表单
const handleSubmit = async () => {
  if (!announcementFormRef.value) return
  await announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (isEditMode.value) {
          response = await updateAnnouncement(announcementForm.id, announcementForm)
        } else {
          response = await createAnnouncement(announcementForm)
        }
        if (response.code === 200) {
          ElMessage.success(isEditMode.value ? t('announcementManagement.editSuccess') : t('announcementManagement.addSuccess'))
          dialogVisible.value = false
          getAnnouncementList()
        } else {
          // 只在response.message不为空时显示详细错误信息
          if (response.message) {
            ElMessage.error((isEditMode.value ? t('announcementManagement.editFailed') : t('announcementManagement.addFailed')) + '：' + response.message)
          } else {
            ElMessage.error(isEditMode.value ? t('announcementManagement.editFailed') : t('announcementManagement.addFailed'))
          }
        }
      } catch (error) {
        // 只在error.message不为空时显示详细错误信息
        if (error.message) {
          ElMessage.error((isEditMode.value ? t('announcementManagement.editFailed') : t('announcementManagement.addFailed')) + '：' + error.message)
        } else {
          ElMessage.error(isEditMode.value ? t('announcementManagement.editFailed') : t('announcementManagement.addFailed'))
        }
      }
    }
  })
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    // 保存原始状态，用于失败时恢复
    const originalStatus = row.status
    
    // 调用API
    if (row.status === 1) {
      await publishAnnouncement(row.id)
      ElMessage.success(t('announcementManagement.publishSuccess'))
    } else {
      await revokeAnnouncement(row.id)
      ElMessage.success(t('announcementManagement.revokeSuccess'))
    }
    
    // 刷新列表
    getAnnouncementList()
  } catch (error) {
    // 如果失败，恢复原来的状态
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(t('common.operationFailed') + '：' + (error.message || '未知错误'))
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getAnnouncementList()
})
</script>

<style scoped>
.announcement-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-area {
  display: flex;
  gap: 5px; /* 减小按钮间距 */
  margin-bottom: 15px;
  padding: 10px 0;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>