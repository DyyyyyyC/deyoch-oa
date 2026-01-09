<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('taskManagement.title') }}</h2>
    </div>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <!-- 操作和搜索区域 -->
      <div class="action-search-area">
        <!-- 左侧操作按钮 -->
        <div class="action-buttons">
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
        
        <!-- 右侧搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" inline>
            <el-form-item>
              <el-input 
                v-model="searchForm.title" 
                :placeholder="$t('taskManagement.enterTitle')" 
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-input 
                v-model="searchForm.assigneeId" 
                :placeholder="$t('taskManagement.enterAssignee')" 
                clearable
              />
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
        :data="taskList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" :label="$t('taskManagement.title')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" :label="$t('taskManagement.description')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="assigneeName" :label="$t('taskManagement.assignee')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="creatorName" :label="$t('taskManagement.creator')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="priority" :label="$t('taskManagement.priority')" min-width="120">
          <template #default="scope">
            <el-tag :type="getPriorityTagType(scope.row.priority)">
              {{ getPriorityText(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('taskManagement.status')" min-width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" :label="$t('taskManagement.startTime')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="endTime" :label="$t('taskManagement.endTime')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createdAt" :label="$t('common.createdAt')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" :label="$t('common.updatedAt')" min-width="180" show-overflow-tooltip />
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
        <el-form-item :label="$t('taskManagement.description')" prop="content">
          <el-input
            v-model="taskForm.content"
            type="textarea"
            :placeholder="$t('taskManagement.enterDescription')"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('taskManagement.assignee')" prop="assigneeId">
          <el-select
            v-model="taskForm.assigneeId"
            :placeholder="$t('taskManagement.selectAssignee')"
            filterable
            remote
            clearable
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
            @focus="handleUserSelectFocus"
            reserve-keyword
          >
            <el-option
              v-for="user in filteredUserList"
              :key="user.id"
              :label="`${user.username} (${user.nickname || user.email})`"
              :value="user.id"
            >
              <div style="display: flex; justify-content: space-between;">
                <span>{{ user.username }}</span>
                <span style="color: #8492a6; font-size: 13px;">{{ user.nickname || user.email }}</span>
              </div>
            </el-option>
          </el-select>
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
          <el-select v-model="taskForm.status" :placeholder="$t('common.selectStatus')" style="width: 100%">
            <el-option :label="$t('common.notStarted')" :value="0"></el-option>
            <el-option :label="$t('common.inProgress')" :value="1"></el-option>
            <el-option :label="$t('common.completed')" :value="2"></el-option>
            <el-option :label="$t('common.cancelled')" :value="3"></el-option>
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

<style scoped>
/* 任务管理页面特定样式 */
.dialog-footer {
  text-align: right;
}
</style>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, CircleCheck, VideoPlay } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import {
  getTaskList as getTaskListApi,
  createTask,
  updateTask,
  deleteTask,
  updateTaskStatus
} from '@/api/task'
import { getUserList as getUserListApi } from '@/api/user'
import '@/style/management-layout.css'

// 获取i18n的t函数
const { t } = useI18n()

// 获取用户store
const userStore = useUserStore()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  title: '',
  assigneeId: ''
})

// 任务列表
const taskList = ref([])

// 用户列表（用于负责人下拉选择）
const userList = ref([])
const filteredUserList = ref([])
const userSearchLoading = ref(false)

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
  content: '',
  assigneeId: '',
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
  assigneeId: [
    { required: true, message: t('taskManagement.selectAssignee'), trigger: 'change' }
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

// 获取用户列表
// 优化：只在需要时（打开创建/编辑对话框时）才加载用户列表
// 这样可以减少页面初始化时的网络请求，提高页面加载速度
// 用户列表用于：
// 1. 创建/编辑任务时选择负责人
// 2. 显示任务的创建人和负责人姓名
// 3. 搜索时按负责人筛选任务
const getUserList = async () => {
  try {
    console.log('开始加载用户列表（用于任务负责人选择）')
    const data = await getUserListApi()
    console.log('用户列表API响应:', data)
    
    // 处理不同的响应格式
    let users = []
    if (Array.isArray(data)) {
      users = data
    } else if (data && data.records && Array.isArray(data.records)) {
      users = data.records
    } else if (data && data.data && Array.isArray(data.data)) {
      users = data.data
    } else if (data && data.success && data.data && Array.isArray(data.data.records)) {
      users = data.data.records
    } else {
      console.error('用户列表数据格式错误:', data)
      users = []
    }
    
    userList.value = users
    // 初始显示前10个用户
    filteredUserList.value = users.slice(0, 10)
    console.log('用户列表加载完成，共', userList.value.length, '个用户')
  } catch (error) {
    console.error('获取用户列表失败:', error)
    userList.value = []
    filteredUserList.value = []
  }
}

// 用户搜索防抖定时器
let searchTimer = null

// 搜索用户（远程搜索）
const searchUsers = (query) => {
  userSearchLoading.value = true
  
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 防抖处理
  searchTimer = setTimeout(() => {
    if (query && query.trim()) {
      // 根据用户名、昵称或邮箱搜索
      filteredUserList.value = userList.value.filter(user => 
        user.username.toLowerCase().includes(query.toLowerCase()) ||
        (user.nickname && user.nickname.toLowerCase().includes(query.toLowerCase())) ||
        (user.email && user.email.toLowerCase().includes(query.toLowerCase()))
      ).slice(0, 20) // 限制搜索结果数量
    } else {
      // 无搜索条件时显示前10个活跃用户
      filteredUserList.value = userList.value
        .filter(user => user.status === 1) // 只显示启用的用户
        .slice(0, 10)
    }
    userSearchLoading.value = false
  }, 300) // 300ms防抖延迟
}

// 处理用户选择框获得焦点
const handleUserSelectFocus = () => {
  if (filteredUserList.value.length === 0 && userList.value.length > 0) {
    // 初始显示前10个启用的用户
    filteredUserList.value = userList.value
      .filter(user => user.status === 1)
      .slice(0, 10)
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: t('common.notStarted'),
    1: t('common.inProgress'),
    2: t('common.completed'),
    3: t('common.cancelled')
  }
  return statusMap[status] || t('common.unknown')
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',  // 未开始
    1: 'info',     // 进行中
    2: 'success',  // 已完成
    3: 'danger'    // 已取消
  }
  return typeMap[status] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const priorityMap = {
    1: t('common.low'),
    2: t('common.medium'),
    3: t('common.high')
  }
  return priorityMap[priority] || t('common.medium')
}

// 获取优先级标签类型
const getPriorityTagType = (priority) => {
  const typeMap = {
    1: 'info',     // 低优先级
    2: 'success',  // 中优先级
    3: 'danger'    // 高优先级
  }
  return typeMap[priority] || 'success'
}

// 根据用户ID获取用户名
const getUserNameById = (userId) => {
  if (!userId) return '-'
  const user = userList.value.find(u => u.id === userId)
  return user ? user.username : '-'
}

// 获取任务列表
const getTaskList = async () => {
  loading.value = true
  try {
    // 调用分页接口
    const response = await getTaskListApi({
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.title
    })
    
    // 处理分页响应数据
    let tasks = []
    let total = 0
    
    if (response && response.records && Array.isArray(response.records)) {
      // 新的分页格式：PageResult
      tasks = response.records
      total = response.total || 0
    } else if (Array.isArray(response)) {
      // 旧格式兼容：直接返回数组
      tasks = response
      total = response.length
    }
    
    taskList.value = tasks
    pagination.total = total
  } catch (error) {
    taskList.value = []
    pagination.total = 0
    if (error.message) {
      ElMessage.error('获取任务列表失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 搜索任务
const handleSearch = () => {
  // 实现真正的搜索逻辑
  loading.value = true
  
  try {
    // 获取完整任务列表
    getTaskList().then(() => {
      // 在前端进行搜索过滤
      let filteredTasks = [...taskList.value]
      
      // 按标题搜索
      if (searchForm.title && searchForm.title.trim()) {
        filteredTasks = filteredTasks.filter(task => 
          task.title.toLowerCase().includes(searchForm.title.toLowerCase()) ||
          (task.content && task.content.toLowerCase().includes(searchForm.title.toLowerCase()))
        )
      }
      
      // 按负责人搜索
      if (searchForm.assigneeId && searchForm.assigneeId.trim()) {
        filteredTasks = filteredTasks.filter(task => 
          task.assigneeName && task.assigneeName.toLowerCase().includes(searchForm.assigneeId.toLowerCase())
        )
      }
      
      // 更新显示的任务列表
      taskList.value = filteredTasks
      pagination.total = filteredTasks.length
    })
  } catch (error) {
    if (error.message) {
      ElMessage.error('搜索失败：' + error.message)
    }
  }
}

// 重置搜索表单
const handleReset = () => {
  searchForm.title = ''
  searchForm.assigneeId = ''
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
  // 在打开对话框时才加载用户列表
  if (userList.value.length === 0) {
    getUserList()
  }
}

// 批量编辑任务
const handleBatchEdit = () => {
  if (selectedTasks.value.length !== 1) {
    ElMessage.warning('请选择一个任务进行编辑')
    return
  }
  // 填充表单数据
  const row = selectedTasks.value[0]
  isEditMode.value = true
  taskForm.id = row.id
  taskForm.title = row.title
  taskForm.content = row.content
  taskForm.assigneeId = row.assigneeId
  taskForm.priority = row.priority
  taskForm.status = row.status
  taskForm.startTime = row.startTime
  taskForm.endTime = row.endTime
  dialogVisible.value = true
  // 在打开对话框时才加载用户列表
  if (userList.value.length === 0) {
    getUserList()
  }
}

// 批量删除任务
const handleBatchDelete = async () => {
  if (selectedTasks.value.length === 0) {
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
    for (const task of selectedTasks.value) {
      await deleteTask(task.id)
    }
    
    ElMessage.success(t('taskManagement.deleteSuccess'))
    getTaskList()
    // 清空选择
    selectedTasks.value = []
  } catch (error) {
    if (error !== 'cancel') {
      if (error.message) {
        ElMessage.error(t('taskManagement.deleteFailed') + '：' + error.message)
      } else {
        ElMessage.error(t('taskManagement.deleteFailed'))
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
  taskForm.content = ''
  taskForm.assigneeId = ''
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
        if (isEditMode.value) {
          await updateTask(taskForm.id, taskForm)
        } else {
          await createTask(taskForm)
        }
        ElMessage.success(isEditMode.value ? t('taskManagement.editSuccess') : t('taskManagement.addSuccess'))
        dialogVisible.value = false
        getTaskList()
      } catch (error) {
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
    await updateTaskStatus(row.id, status)
    // 根据状态获取对应的文本
    const statusText = status === 0 ? t('common.notStarted') : status === 1 ? t('common.inProgress') : status === 2 ? t('common.completed') : t('common.cancelled')
    ElMessage.success(t('taskManagement.updateStatusSuccess'))
    getTaskList()
  } catch (error) {
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
  // 用户列表将在需要时（打开创建/编辑对话框时）才加载，避免页面初始化时的不必要请求
  console.log('任务管理页面初始化完成，用户列表将在需要时懒加载')
})
</script>
