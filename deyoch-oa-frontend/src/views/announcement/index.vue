<template>
  <div class="announcement-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>{{ $t('announcementManagement.title') }}</h2>
      <el-button type="primary" @click="handleAddAnnouncement">
        <el-icon><Plus /></el-icon>
        {{ $t('announcementManagement.addAnnouncement') }}
      </el-button>
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
      <el-table
        v-loading="loading"
        :data="announcementList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
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
        <el-table-column :label="$t('common.actions')" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditAnnouncement(scope.row)">
              <el-icon><Edit /></el-icon>
              {{ $t('common.edit') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteAnnouncement(scope.row)">
              <el-icon><Delete /></el-icon>
              {{ $t('common.delete') }}
            </el-button>
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
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="announcementForm.title"
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="发布人" prop="publisher">
          <el-input
            v-model="announcementForm.publisher"
            placeholder="请输入发布人"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            placeholder="请输入公告内容"
            :rows="6"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getAnnouncementList as getAnnouncementListApi, 
  createAnnouncement, 
  updateAnnouncement, 
  deleteAnnouncement, 
  publishAnnouncement, 
  revokeAnnouncement 
} from '@/api/announcement'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  title: ''
})

// 公告列表
const announcementList = ref([])

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
  return isEditMode.value ? '编辑公告' : '添加公告'
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
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  publisher: [
    { required: true, message: '请输入发布人', trigger: 'blur' },
    { min: 1, max: 50, message: '发布人长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 10, max: 1000, message: '内容长度在 10 到 1000 个字符', trigger: 'blur' }
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
    ElMessage.error('获取公告列表失败：' + error.message)
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
  dialogVisible.value = true
}

// 打开编辑公告对话框
const handleEditAnnouncement = (row) => {
  isEditMode.value = true
  announcementForm.id = row.id
  announcementForm.title = row.title
  announcementForm.content = row.content
  announcementForm.publisher = row.publisher
  announcementForm.status = row.status
  dialogVisible.value = true
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
          ElMessage.success(isEditMode.value ? '编辑公告成功' : '添加公告成功')
          dialogVisible.value = false
          getAnnouncementList()
        } else {
          ElMessage.error((isEditMode.value ? '编辑公告失败' : '添加公告失败') + '：' + response.message)
        }
      } catch (error) {
        ElMessage.error((isEditMode.value ? '编辑公告失败' : '添加公告失败') + '：' + error.message)
      }
    }
  })
}

// 删除公告
const handleDeleteAnnouncement = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条公告吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const response = await deleteAnnouncement(row.id)
    if (response.code === 200) {
      ElMessage.success('删除公告成功')
      getAnnouncementList()
    } else {
      ElMessage.error('删除公告失败：' + response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除公告失败：' + error.message)
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    let response
    if (row.status === 1) {
      response = await publishAnnouncement(row.id)
      ElMessage.success('发布公告成功')
    } else {
      response = await revokeAnnouncement(row.id)
      ElMessage.success('撤销公告成功')
    }
    if (response.code !== 200) {
      // 如果失败，恢复原来的状态
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error('状态变更失败：' + response.message)
    }
    getAnnouncementList()
  } catch (error) {
    // 如果失败，恢复原来的状态
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态变更失败：' + error.message)
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