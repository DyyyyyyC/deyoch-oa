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
        <el-table-column :label="$t('common.actions')" width="240" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditTask(scope.row)">
              <el-icon><Edit /></el-icon>
              {{ $t('common.edit') }}
            </el-button>
            <el-button size="small" type="success" @click="handleUpdateTaskStatus(scope.row, 2)" :disabled="scope.row.status === 2">
              <el-icon><CircleCheck /></el-icon>
              完成
            </el-button>
            <el-button size="small" type="warning" @click="handleUpdateTaskStatus(scope.row, 1)" :disabled="scope.row.status === 1">
              <el-icon><VideoPlay /></el-icon>
              开始
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
        <el-form-item label="任务标题" prop="title">
          <el-input
            v-model="taskForm.title"
            placeholder="请输入任务标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            placeholder="请输入任务描述"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="负责人" prop="assignee">
          <el-input
            v-model="taskForm.assignee"
            placeholder="请输入负责人"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="taskForm.priority">
            <el-radio label="1">低</el-radio>
            <el-radio label="2">中</el-radio>
            <el-radio label="3">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="taskForm.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="taskForm.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEditMode">
          <el-select v-model="taskForm.status" placeholder="请选择状态">
            <el-option label="未开始" value="0"></el-option>
            <el-option label="进行中" value="1"></el-option>
            <el-option label="已完成" value="2"></el-option>
            <el-option label="已取消" value="3"></el-option>
          </el-select>
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
import { Plus, Edit, Delete, CircleCheck, VideoPlay } from '@element-plus/icons-vue'
import {
  getTaskList as getTaskListApi,
  createTask,
  updateTask,
  deleteTask,
  updateTaskStatus
} from '@/api/task'

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

// 对话框状态
const dialogVisible = ref(false)
const taskFormRef = ref(null)
const isEditMode = ref(false)

// 对话框标题
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑任务' : '添加任务'
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
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  assignee: [
    { required: true, message: '请输入负责人', trigger: 'blur' },
    { min: 1, max: 50, message: '负责人长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (taskForm.startTime && value && new Date(value) < new Date(taskForm.startTime)) {
          callback(new Error('结束时间不能早于开始时间'))
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
    ElMessage.error('获取任务列表失败：' + error.message)
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

// 当前页变化
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

// 打开编辑任务对话框
const handleEditTask = (row) => {
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
          ElMessage.success(isEditMode.value ? '编辑任务成功' : '添加任务成功')
          dialogVisible.value = false
          getTaskList()
        } else {
          ElMessage.error((isEditMode.value ? '编辑任务失败' : '添加任务失败') + '：' + response.message)
        }
      } catch (error) {
        ElMessage.error((isEditMode.value ? '编辑任务失败' : '添加任务失败') + '：' + error.message)
      }
    }
  })
}

// 删除任务
const handleDeleteTask = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条任务吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const response = await deleteTask(row.id)
    if (response.code === 200) {
      ElMessage.success('删除任务成功')
      getTaskList()
    } else {
      ElMessage.error('删除任务失败：' + response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除任务失败：' + error.message)
    }
  }
}

// 更新任务状态
const handleUpdateTaskStatus = async (row, status) => {
  try {
    const response = await updateTaskStatus(row.id, status)
    if (response.code === 200) {
      const statusText = status === 0 ? '未开始' : status === 1 ? '进行中' : status === 2 ? '已完成' : '已取消'
      ElMessage.success(`任务已${statusText}`)
      getTaskList()
    } else {
      ElMessage.error('更新任务状态失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('更新任务状态失败：' + error.message)
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
