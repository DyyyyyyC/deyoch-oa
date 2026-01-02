<template>
  <div class="task-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('taskManagement.title') }}</h2>
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
      <!-- 操作区域 -->
      <div class="action-area">
        <el-button type="primary" @click="handleAddTask">
          <el-icon><Plus /></el-icon>
          {{ $t('taskManagement.addTask') }}
        </el-button>
        <el-button type="primary" @click="handleBatchEdit" :disabled="selectedTasks.length !== 1">
          <el-icon><Edit /></el-icon>
          {{ $t('common.edit') }}
        </el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedTasks.length === 0">
          <el-icon><Delete /></el-icon>
          {{ $t('common.delete') }}
        </el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="taskList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" :label="$t('taskManagement.title')" />
        <el-table-column prop="assignee" :label="$t('taskManagement.assignee')" />
        <el-table-column prop="creator" :label="$t('taskManagement.creator')" />
        <el-table-column prop="priority" :label="$t('taskManagement.priority')" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.priority === 1 ? 'info' : scope.row.priority === 2 ? 'success' : 'danger'">
              {{ scope.row.priority === 1 ? $t('common.low') : scope.row.priority === 2 ? $t('common.medium') : $t('common.high') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('taskManagement.status')" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : scope.row.status === 1 ? 'info' : scope.row.status === 2 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? $t('common.notStarted') : scope.row.status === 1 ? $t('common.inProgress') : scope.row.status === 2 ? $t('common.completed') : $t('common.cancelled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" :label="$t('taskManagement.startTime')" width="200" />
        <el-table-column prop="endTime" :label="$t('taskManagement.endTime')" width="200" />
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

    <!-- 任务表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item :label="$t('taskManagement.title')" prop="title">
          <el-input
            v-model="taskForm.title"
            :placeholder="$t('taskManagement.enterTitle')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('taskManagement.description')" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :placeholder="$t('taskManagement.enterDescription')"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('taskManagement.assignee')" prop="assignee">
          <el-input
            v-model="taskForm.assignee"
            :placeholder="$t('taskManagement.enterAssignee')"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('taskManagement.priority')" prop="priority">
          <el-radio-group v-model="taskForm.priority">
            <el-radio :value="1">{{ $t('common.low') }}</el-radio>
            <el-radio :value="2">{{ $t('common.medium') }}</el-radio>
            <el-radio :value="3">{{ $t('common.high') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('taskManagement.startTime')" prop="startTime">
          <el-date-picker
            v-model="taskForm.startTime"
            type="datetime"
            :placeholder="$t('common.selectStartTime')"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('taskManagement.endTime')" prop="endTime">
          <el-date-picker
            v-model="taskForm.endTime"
            type="datetime"
            :placeholder="$t('common.selectEndTime')"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('taskManagement.status')" prop="status" v-if="isEditMode">
          <el-select v-model="taskForm.status" :placeholder="$t('common.selectStatus')">
            <el-option :label="$t('common.notStarted')" value="0"></el-option>
            <el-option :label="$t('common.inProgress')" value="1"></el-option>
            <el-option :label="$t('common.completed')" value="2"></el-option>
            <el-option :label="$t('common.cancelled')" value="3"></el-option>
          </el-select>
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
import { Plus, Edit, Delete, CircleCheck, VideoPlay } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import {
  getTaskList as getTaskListApi,
  createTask,
  updateTask,
  deleteTask,
  updateTaskStatus
} from '@/api/task'

// 获取i18n的t函数
const { t } = useI18n()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  title: '',
  assignee: ''
})

// 任务列表
const taskList = ref([])

// 选中的任务
const selectedTasks = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const taskFormRef = ref(null)
const isEditMode = ref(false)

// 对话框标题
const dialogTitle = computed(() => {
  return isEditMode.value ? t('taskManagement.editTask') : t('taskManagement.addTask')
})

// 任务表单数据
const taskForm = reactive({
  id: null,
  title: '',
  description: '',
  assignee: '',
  priority: 2, // 默认中优先级
  status: 0, // 默认未开始
  startTime: '',
  endTime: ''
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: t('taskManagement.enterTitle'), trigger: 'blur' },
    { min: 2, max: 100, message: t('common.fieldLength', { min: 2, max: 100, field: t('taskManagement.title') }), trigger: 'blur' }
  ],
  assignee: [
    { required: true, message: t('taskManagement.enterAssignee'), trigger: 'blur' },
    { min: 1, max: 50, message: t('common.fieldLength', { min: 1, max: 50, field: t('taskManagement.assignee') }), trigger: 'blur' }
  ],
  priority: [
    { required: true, message: t('common.pleaseSelect', { name: t('taskManagement.priority') }), trigger: 'change' }
  ],
  startTime: [
    { required: true, message: t('common.selectStartTime'), trigger: 'change' }
  ],
  endTime: [
    { required: true, message: t('common.selectEndTime'), trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (taskForm.startTime && value && new Date(value) < new Date(taskForm.startTime)) {
          callback(new Error(t('common.endTimeCannotBeEarlier')))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 获取任务列表
const getTaskList = async () => {
  loading.value = true
  try {
    const data = await getTaskListApi()
    taskList.value = data
    pagination.total = data.length
  } catch (error) {
    taskList.value = []
    pagination.total = 0
    // 只在error.message不为空时显示错误信息
    if (error.message) {
      ElMessage.error('获取任务列表失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 搜索任务
const handleSearch = () => {
  // 这里可以实现带条件的搜索，目前先调用getTaskList
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

// 处理多选框选择变化
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
  console.log('选中的任务：', selectedTasks.value)
}

// 页码变化 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getTaskList()
}

// 打开添加任务对话框
const handleAddTask = () => {
  isEditMode.value = false
  resetForm()
  dialogVisible.value = true
}

// 批量编辑任务
const handleBatchEdit = () => {
  if (selectedTasks.length !== 1) {
    ElMessage.warning('请选择一个任务进行编辑')
    return
  }
  // 填充表单数据
  const row = selectedTasks[0]
  isEditMode.value = true
  taskForm.id = row.id
  taskForm.title = row.title
  taskForm.description = row.description
  taskForm.assignee = row.assignee
  taskForm.priority = row.priority
  taskForm.status = row.status
  taskForm.startTime = row.startTime
  taskForm.endTime = row.endTime
  dialogVisible.value = true
}

// 批量删除任务
const handleBatchDelete = async () => {
  if (selectedTasks.length === 0) {
    ElMessage.warning('请选择要删除的任务')
    return
  }
  
  try {
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
    for (const task of selectedTasks) {
      const response = await deleteTask(task.id)
      if (response.code !== 200) {
        throw new Error(response.message)
      }
    }
    
    ElMessage.success(t('taskManagement.deleteSuccess'))
    getTaskList()
    // 清空选择
    selectedTasks.value = []
  } catch (error) {
    if (error !== 'cancel') {
      // 只在error.message不为空时显示错误信息
      if (error.message) {
        ElMessage.error(t('taskManagement.deleteFailed') + '：' + error.message)
      }
    }
  }
}

// 重置表单
const resetForm = () => {
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
  }
  taskForm.id = null
  taskForm.title = ''
  taskForm.description = ''
  taskForm.assignee = ''
  taskForm.priority = 2
  taskForm.status = 0
  taskForm.startTime = ''
  taskForm.endTime = ''
}

// 提交表单
const handleSubmit = async () => {
  if (!taskFormRef.value) return
  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (isEditMode.value) {
          response = await updateTask(taskForm.id, taskForm)
        } else {
          response = await createTask(taskForm)
        }
        if (response.code === 200) {
          ElMessage.success(isEditMode.value ? t('taskManagement.editSuccess') : t('taskManagement.addSuccess'))
          dialogVisible.value = false
          getTaskList()
        } else {
          // 只在response.message不为空时显示详细错误信息
          if (response.message) {
            ElMessage.error((isEditMode.value ? t('taskManagement.editFailed') : t('taskManagement.addFailed')) + '：' + response.message)
          } else {
            ElMessage.error(isEditMode.value ? t('taskManagement.editFailed') : t('taskManagement.addFailed'))
          }
        }
      } catch (error) {
        // 只在error.message不为空时显示详细错误信息
        if (error.message) {
          ElMessage.error((isEditMode.value ? t('taskManagement.editFailed') : t('taskManagement.addFailed')) + '：' + error.message)
        } else {
          ElMessage.error(isEditMode.value ? t('taskManagement.editFailed') : t('taskManagement.addFailed'))
        }
      }
    }
  })
}

// 更新任务状态
const handleUpdateTaskStatus = async (row, status) => {
  try {
    const response = await updateTaskStatus(row.id, status)
    if (response.code === 200) {
      // 根据状态获取对应的文本
      const statusText = status === 0 ? t('common.notStarted') : status === 1 ? t('common.inProgress') : status === 2 ? t('common.completed') : t('common.cancelled')
      ElMessage.success(t('taskManagement.updateStatusSuccess'))
      getTaskList()
    } else {
      // 只在response.message不为空时显示详细错误信息
      if (response.message) {
        ElMessage.error(t('taskManagement.updateStatusFailed') + '：' + response.message)
      } else {
        ElMessage.error(t('taskManagement.updateStatusFailed'))
      }
    }
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error(t('taskManagement.updateStatusFailed') + '：' + error.message)
    } else {
      ElMessage.error(t('taskManagement.updateStatusFailed'))
    }
  }
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
