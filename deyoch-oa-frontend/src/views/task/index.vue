<template>
  <div class="task-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>{{ $t('taskManagement.title') }}</h2>
      <el-button type="primary" @click="handleAddTask">
        <el-icon><Plus /></el-icon>
        {{ $t('taskManagement.addTask') }}
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('taskManagement.title')">
          <el-input v-model="searchForm.title" :placeholder="$t('taskManagement.enterTitle')" clearable />
        </el-form-item>
        <el-form-item :label="$t('taskManagement.assignee')">
          <el-input v-model="searchForm.assignee" :placeholder="$t('taskManagement.enterAssignee')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="taskList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" :label="$t('taskManagement.title')" />
        <el-table-column prop="assignee" :label="$t('taskManagement.assignee')" />
        <el-table-column prop="priority" :label="$t('taskManagement.priority')" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.priority === 1 ? 'info' : scope.row.priority === 2 ? 'success' : 'danger'">
              {{ scope.row.priority === 1 ? '低' : scope.row.priority === 2 ? '中' : '高' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('taskManagement.status')" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : scope.row.status === 1 ? 'info' : scope.row.status === 2 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? '未开始' : scope.row.status === 1 ? '进行中' : scope.row.status === 2 ? '已完成' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" :label="$t('taskManagement.startTime')" width="200" />
        <el-table-column prop="endTime" :label="$t('taskManagement.endTime')" width="200" />
        <el-table-column prop="createdAt" :label="$t('common.createdAt')" width="200" />
        <el-table-column prop="updatedAt" :label="$t('common.updatedAt')" width="200" />
        <el-table-column :label="$t('common.actions')" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditTask(scope.row)">
              <el-icon><Edit /></el-icon>
              {{ $t('common.edit') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteTask(scope.row)">
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
  title: '',
  assignee: ''
})

// 任务列表
const taskList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 获取任务列表
const getTaskList = async () => {
  loading.value = true
  try {
    // 这里只是一个示例，实际应该调用后端API
    // const data = await get('/task/list')
    // taskList.value = data.list
    // pagination.total = data.total
    taskList.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error('获取任务列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索任务
const handleSearch = () => {
  // 这里可以实现带条件的搜索
  getTaskList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.title = ''
  searchForm.assignee = ''
  getTaskList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getTaskList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getTaskList()
}

// 添加任务
const handleAddTask = () => {
  ElMessage.info('添加任务功能开发中')
}

// 编辑任务
const handleEditTask = (row) => {
  ElMessage.info('编辑任务功能开发中')
}

// 删除任务
const handleDeleteTask = async (row) => {
  ElMessage.info('删除任务功能开发中')
}

// 组件挂载时获取数据
onMounted(() => {
  getTaskList()
})
</script>

<style scoped>
.task-management {
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