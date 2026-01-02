<template>
  <div class="process-instance-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>{{ $t('processInstanceManagement.title') }}</h2>
      <el-button type="primary" @click="handleAddProcessInstance">
        <el-icon><Plus /></el-icon>
        {{ $t('processInstanceManagement.addProcessInstance') }}
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('processInstanceManagement.instanceName')">
          <el-input v-model="searchForm.instanceName" :placeholder="$t('processInstanceManagement.enterInstanceName')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 流程实例列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="processInstanceList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="instanceName" :label="$t('processInstanceManagement.instanceName')" />
        <el-table-column prop="processName" :label="$t('processInstanceManagement.processName')" width="180" />
        <el-table-column prop="initiator" :label="$t('processInstanceManagement.initiator')" />
        <el-table-column prop="startTime" :label="$t('processInstanceManagement.startTime')" width="200" />
        <el-table-column prop="endTime" :label="$t('processInstanceManagement.endTime')" width="200" />
        <el-table-column prop="status" :label="$t('processInstanceManagement.status')" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('common.createdAt')" width="200" />
        <el-table-column prop="updatedAt" :label="$t('common.updatedAt')" width="200" />
        <el-table-column :label="$t('common.actions')" min-width="270" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleStartProcessInstance(scope.row)" :disabled="scope.row.status !== 0">
              <el-icon><VideoPlay /></el-icon>
              {{ $t('processInstanceManagement.start') }}
            </el-button>
            <el-button size="small" type="success" @click="handleCompleteProcessInstance(scope.row)" :disabled="scope.row.status !== 1">
              <el-icon><CircleCheck /></el-icon>
              {{ $t('processInstanceManagement.complete') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteProcessInstance(scope.row)">
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

    <!-- 流程实例表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="processInstanceFormRef"
        :model="processInstanceForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item :label="$t('processInstanceManagement.instanceName')" prop="instanceName">
          <el-input
            v-model="processInstanceForm.instanceName"
            :placeholder="$t('processInstanceManagement.enterInstanceName')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('processInstanceManagement.processName')" prop="processId">
          <el-select
            v-model="processInstanceForm.processId"
            :placeholder="$t('common.pleaseSelect', { name: $t('processInstanceManagement.processName') })"
            clearable
          >
            <el-option
              v-for="process in processList"
              :key="process.id"
              :label="process.processName"
              :value="process.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('processInstanceManagement.initiator')" prop="initiator">
          <el-input
            v-model="processInstanceForm.initiator"
            :placeholder="$t('processInstanceManagement.enterInitiator')"
            maxlength="50"
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
import { Plus, Edit, Delete, VideoPlay, CircleCheck } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { 
  getProcessInstanceList as getProcessInstanceListApi, 
  createProcessInstance, 
  updateProcessInstance, 
  deleteProcessInstance, 
  startProcessInstance, 
  completeProcessInstance,
  getProcessList
} from '@/api/process'

// 获取i18n的t函数
const { t } = useI18n()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  instanceName: ''
})

// 流程实例列表
const processInstanceList = ref([])

// 流程列表（用于关联流程实例）
const processList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const processInstanceFormRef = ref(null)
const isEditMode = ref(false)

// 对话框标题
const dialogTitle = computed(() => {
  return isEditMode.value ? t('processInstanceManagement.editProcessInstance') : t('processInstanceManagement.addProcessInstance')
})

// 流程实例表单数据
const processInstanceForm = reactive({
  id: null,
  processId: undefined, // 初始值改为undefined，确保下拉框显示占位符
  instanceName: '',
  initiator: '',
  status: 0
})

// 表单验证规则
const formRules = {
  instanceName: [
    { required: true, message: t('processInstanceManagement.enterInstanceName'), trigger: 'blur' },
    { min: 2, max: 100, message: t('common.fieldLength', { min: 2, max: 100, field: t('processInstanceManagement.instanceName') }), trigger: 'blur' }
  ],
  processId: [
    { required: true, message: t('processInstanceManagement.enterProcessId'), trigger: 'blur' },
    { type: 'number', message: t('common.fieldMustBeNumber', { field: t('processInstanceManagement.processName') }), trigger: 'blur' }
  ],
  initiator: [
    { required: true, message: t('processInstanceManagement.enterInitiator'), trigger: 'blur' },
    { min: 1, max: 50, message: t('common.fieldLength', { min: 1, max: 50, field: t('processInstanceManagement.initiator') }), trigger: 'blur' }
  ]
}

// 加载流程列表（用于关联流程实例）
const loadProcessList = async () => {
  try {
    const data = await getProcessList()
    processList.value = data
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取流程列表失败：' + error.message)
    }
  }
}

// 获取流程实例列表
const getProcessInstanceList = async () => {
  loading.value = true
  try {
    const data = await getProcessInstanceListApi()
    // 将流程名称与流程实例关联
    processInstanceList.value = data.map(instance => {
      const process = processList.value.find(p => p.id === instance.processId)
      return {
        ...instance,
        processName: process ? process.processName : '未知流程'
      }
    })
    pagination.total = data.length
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取流程实例列表失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 搜索流程实例
const handleSearch = () => {
  getProcessInstanceList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.instanceName = ''
  getProcessInstanceList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getProcessInstanceList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getProcessInstanceList()
}

// 打开添加流程实例对话框
const handleAddProcessInstance = () => {
  isEditMode.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑流程实例对话框
const handleEditProcessInstance = (row) => {
  isEditMode.value = true
  processInstanceForm.id = row.id
  processInstanceForm.processId = row.processId
  processInstanceForm.instanceName = row.instanceName
  processInstanceForm.initiator = row.initiator
  processInstanceForm.status = row.status
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (processInstanceFormRef.value) {
    processInstanceFormRef.value.resetFields()
  }
  processInstanceForm.id = null
  processInstanceForm.processId = undefined // 重置为undefined，确保下拉框显示占位符
  processInstanceForm.instanceName = ''
  processInstanceForm.initiator = ''
  processInstanceForm.status = 0
}

// 提交表单
const handleSubmit = async () => {
  if (!processInstanceFormRef.value) return
  // 将processId转换为数字类型，解决Element Plus el-input type="number"返回字符串的问题
  processInstanceForm.processId = Number(processInstanceForm.processId)
  await processInstanceFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
          let response
          if (isEditMode.value) {
            response = await updateProcessInstance(processInstanceForm.id, processInstanceForm)
          } else {
            response = await createProcessInstance(processInstanceForm)
          }
          ElMessage.success(isEditMode.value ? t('processInstanceManagement.editSuccess') : t('processInstanceManagement.addSuccess'))
          dialogVisible.value = false
          getProcessInstanceList()
        } catch (error) {
          // 只在error.message不为空时显示详细错误信息
          if (error.message) {
            ElMessage.error((isEditMode.value ? t('processInstanceManagement.editFailed') : t('processInstanceManagement.addFailed')) + '：' + error.message)
          } else {
            ElMessage.error(isEditMode.value ? t('processInstanceManagement.editFailed') : t('processInstanceManagement.addFailed'))
          }
        }
    }
  })
}

// 删除流程实例
const handleDeleteProcessInstance = async (row) => {
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
    const response = await deleteProcessInstance(row.id)
    ElMessage.success(t('processInstanceManagement.deleteSuccess'))
    getProcessInstanceList()
  } catch (error) {
    if (error !== 'cancel') {
      // 只在error.message不为空时显示详细错误信息
      if (error.message) {
        ElMessage.error(t('processInstanceManagement.deleteFailed') + '：' + error.message)
      }
    }
  }
}

// 启动流程实例
const handleStartProcessInstance = async (row) => {
  try {
    const response = await startProcessInstance(row.id)
    ElMessage.success(t('processInstanceManagement.startSuccess'))
    getProcessInstanceList()
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error(t('processInstanceManagement.startFailed') + '：' + error.message)
    }
  }
}

// 完成流程实例
const handleCompleteProcessInstance = async (row) => {
  try {
    const response = await completeProcessInstance(row.id)
    ElMessage.success(t('processInstanceManagement.completeSuccess'))
    getProcessInstanceList()
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error(t('processInstanceManagement.completeFailed') + '：' + error.message)
    }
  }
}

// 根据状态获取标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 0: return 'warning' // 待启动
    case 1: return 'success' // 运行中
    case 2: return 'info' // 已完成
    default: return 'danger' // 其他
  }
}

// 根据状态获取文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return t('processInstanceManagement.statusPending')
    case 1: return t('processInstanceManagement.statusRunning')
    case 2: return t('processInstanceManagement.statusCompleted')
    default: return t('processInstanceManagement.statusUnknown')
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  // 先加载流程列表，再加载流程实例列表
  await loadProcessList()
  getProcessInstanceList()
})
</script>

<style scoped>
.process-instance-management {
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