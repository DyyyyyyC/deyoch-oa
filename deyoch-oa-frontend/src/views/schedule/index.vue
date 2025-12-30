<template>
  <div class="schedule-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>{{ $t('scheduleManagement.title') }}</h2>
      <el-button type="primary" @click="handleAddSchedule">
        <el-icon><Plus /></el-icon>
        {{ $t('scheduleManagement.addSchedule') }}
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('scheduleManagement.scheduleTitle')">
          <el-input v-model="searchForm.title" :placeholder="$t('scheduleManagement.enterTitle')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日程列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="scheduleList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" :label="$t('scheduleManagement.scheduleTitle')" min-width="170"/>
        <el-table-column prop="content" :label="$t('scheduleManagement.content')" min-width="200" />
        <el-table-column prop="startTime" :label="$t('scheduleManagement.startTime')" width="200" />
        <el-table-column prop="endTime" :label="$t('scheduleManagement.endTime')" width="200" />
        <el-table-column prop="location" :label="$t('scheduleManagement.location')" width="200" />
        <el-table-column prop="reminderTime" :label="$t('scheduleManagement.reminderTime')" width="200" />
        <el-table-column prop="status" :label="$t('scheduleManagement.status')" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? '未开始' : scope.row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('common.createdAt')" width="200" />
        <el-table-column prop="updatedAt" :label="$t('common.updatedAt')" width="200" />
        <el-table-column :label="$t('common.actions')" min-width="340" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditSchedule(scope.row)">
              <el-icon><Edit /></el-icon>
              {{ $t('common.edit') }}
            </el-button>
            <el-button size="small" type="success" @click="handleUpdateScheduleStatus(scope.row, 2)" :disabled="scope.row.status === 2">
              <el-icon><CircleCheck /></el-icon>
              {{ $t('common.complete') }}
            </el-button>
            <el-button size="small" type="warning" @click="handleUpdateScheduleStatus(scope.row, 1)" :disabled="scope.row.status === 1">
              <el-icon><VideoPlay /></el-icon>
              {{ $t('common.start') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteSchedule(scope.row)">
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

    <!-- 日程表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item :label="$t('scheduleManagement.scheduleTitle')" prop="title">
          <el-input
            v-model="scheduleForm.title"
            :placeholder="$t('scheduleManagement.enterTitle')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('scheduleManagement.content')" prop="content">
          <el-input
            v-model="scheduleForm.content"
            type="textarea"
            :placeholder="$t('scheduleManagement.enterContent')"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('scheduleManagement.startTime')" prop="startTime">
          <el-date-picker
            v-model="scheduleForm.startTime"
            type="datetime"
            :placeholder="$t('common.selectStartTime')"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('scheduleManagement.endTime')" prop="endTime">
          <el-date-picker
            v-model="scheduleForm.endTime"
            type="datetime"
            :placeholder="$t('common.selectEndTime')"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('scheduleManagement.location')" prop="location">
          <el-input
            v-model="scheduleForm.location"
            :placeholder="$t('scheduleManagement.enterLocation')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('scheduleManagement.reminderTime')" prop="reminderTime">
          <el-date-picker
            v-model="scheduleForm.reminderTime"
            type="datetime"
            :placeholder="$t('common.selectReminderTime')"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('scheduleManagement.status')" prop="status" v-if="isEditMode">
          <el-select v-model="scheduleForm.status" :placeholder="$t('common.selectStatus')">
            <el-option :label="$t('common.notStarted')" value="0"></el-option>
            <el-option :label="$t('common.inProgress')" value="1"></el-option>
            <el-option :label="$t('common.completed')" value="2"></el-option>
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

// 导入真实的API调用
import {
  getScheduleList as getScheduleListApi,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  updateScheduleStatus
} from '@/api/schedule'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  title: ''
})

// 日程列表
const scheduleList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const scheduleFormRef = ref(null)
const isEditMode = ref(false)

// 对话框标题
const dialogTitle = computed(() => {
  return isEditMode.value ? '编辑日程' : '添加日程'
})

// 日程表单数据
const scheduleForm = reactive({
  id: null,
  title: '',
  content: '',
  startTime: '',
  endTime: '',
  location: '',
  reminderTime: '',
  status: 0 // 默认未开始
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入日程标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: false, message: '请输入日程内容', trigger: 'blur' },
    { max: 500, message: '内容长度不能超过 500 个字符', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (scheduleForm.startTime && value && new Date(value) < new Date(scheduleForm.startTime)) {
          callback(new Error('结束时间不能早于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 获取日程列表
const getScheduleList = async () => {
  loading.value = true
  try {
    const data = await getScheduleListApi()
    scheduleList.value = data
    pagination.total = data.length
  } catch (error) {
    scheduleList.value = []
    pagination.total = 0
    ElMessage.error('获取日程列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索日程
const handleSearch = () => {
  // 这里可以实现带条件的搜索，目前先调用getScheduleList
  getScheduleList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.title = ''
  getScheduleList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getScheduleList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getScheduleList()
}

// 打开添加日程对话框
const handleAddSchedule = () => {
  isEditMode.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑日程对话框
const handleEditSchedule = (row) => {
  isEditMode.value = true
  scheduleForm.id = row.id
  scheduleForm.title = row.title
  scheduleForm.content = row.content
  scheduleForm.startTime = row.startTime
  scheduleForm.endTime = row.endTime
  scheduleForm.location = row.location
  scheduleForm.reminderTime = row.reminderTime
  scheduleForm.status = row.status
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (scheduleFormRef.value) {
    scheduleFormRef.value.resetFields()
  }
  scheduleForm.id = null
  scheduleForm.title = ''
  scheduleForm.content = ''
  scheduleForm.startTime = ''
  scheduleForm.endTime = ''
  scheduleForm.location = ''
  scheduleForm.reminderTime = ''
  scheduleForm.status = 0
}

// 提交表单
const handleSubmit = async () => {
  if (!scheduleFormRef.value) return
  await scheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (isEditMode.value) {
          response = await updateSchedule(scheduleForm.id, scheduleForm)
        } else {
          response = await createSchedule(scheduleForm)
        }
        if (response.code === 200) {
          ElMessage.success(isEditMode.value ? '编辑日程成功' : '添加日程成功')
          dialogVisible.value = false
          getScheduleList()
        } else {
          ElMessage.error((isEditMode.value ? '编辑日程失败' : '添加日程失败') + '：' + response.message)
        }
      } catch (error) {
        ElMessage.error((isEditMode.value ? '编辑日程失败' : '添加日程失败') + '：' + error.message)
      }
    }
  })
}

// 删除日程
const handleDeleteSchedule = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条日程吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const response = await deleteSchedule(row.id)
    if (response.code === 200) {
      ElMessage.success('删除日程成功')
      getScheduleList()
    } else {
      ElMessage.error('删除日程失败：' + response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除日程失败：' + error.message)
    }
  }
}

// 更新日程状态
const handleUpdateScheduleStatus = async (row, status) => {
  try {
    const response = await updateScheduleStatus(row.id, status)
    if (response.code === 200) {
      const statusText = status === 0 ? '未开始' : status === 1 ? '进行中' : status === 2 ? '已结束' : '已取消'
      ElMessage.success(`日程已${statusText}`)
      getScheduleList()
    } else {
      ElMessage.error('更新日程状态失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('更新日程状态失败：' + error.message)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getScheduleList()
})
</script>

<style scoped>
.schedule-management {
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
