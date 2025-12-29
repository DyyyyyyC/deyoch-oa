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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { get, post, put, del } from '@/utils/axios'

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

// 获取公告列表
const getAnnouncementList = async () => {
  loading.value = true
  try {
    // 这里只是一个示例，实际应该调用后端API
    // const data = await get('/announcement/list')
    // announcementList.value = data.list
    // pagination.total = data.total
    announcementList.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error('获取公告列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索公告
const handleSearch = () => {
  // 这里可以实现带条件的搜索
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

// 添加公告
const handleAddAnnouncement = () => {
  ElMessage.info('添加公告功能开发中')
}

// 编辑公告
const handleEditAnnouncement = (row) => {
  ElMessage.info('编辑公告功能开发中')
}

// 删除公告
const handleDeleteAnnouncement = async (row) => {
  ElMessage.info('删除公告功能开发中')
}

// 状态变更
const handleStatusChange = async (row) => {
  ElMessage.info('状态变更功能开发中')
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