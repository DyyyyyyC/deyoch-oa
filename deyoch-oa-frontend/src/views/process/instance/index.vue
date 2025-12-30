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
        <el-table-column :label="$t('common.actions')" width="240" fixed="right">
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
        <el-form-item label="实例名称" prop="instanceName">
          <el-input
            v-model="processInstanceForm.instanceName"
            placeholder="请输入流程实例名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="流程ID" prop="processId">
          <el-input
            v-model="processInstanceForm.processId"
            placeholder="请输入流程ID"
            type="number"
          />
        </el-form-item>
        <el-form-item label="发起人" prop="initiator">
          <el-input
            v-model="processInstanceForm.initiator"
            placeholder="请输入发起人"
            maxlength="50"
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
import { Plus, Edit, Delete, VideoPlay, CircleCheck } from '@element-plus/icons-vue'
import { 
  getProcessInstanceList as getProcessInstanceListApi, 
  createProcessInstance, 
  updateProcessInstance, 
  deleteProcessInstance, 
  startProcessInstance, 
  completeProcessInstance 
} from '@/api/process'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  instanceName: ''
})

// 流程实例列表
const processInstanceList = ref([])

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
  return isEditMode.value ? '编辑流程实例' : '添加流程实例'
})

// 流程实例表单数据
const processInstanceForm = reactive({
  id: null,
  processId: 0, // 初始值改为数字0，确保类型正确
  instanceName: '',
  initiator: '',
  status: 0
})

// 表单验证规则
const formRules = {
  instanceName: [
    { required: true, message: '请输入流程实例名称', trigger: 'blur' },
    { min: 2, max: 100, message: '流程实例名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  processId: [
    { required: true, message: '请输入流程ID', trigger: 'blur' },
    { type: 'number', message: '流程ID必须是数字', trigger: 'blur' }
  ],
  initiator: [
    { required: true, message: '请输入发起人', trigger: 'blur' },
    { min: 1, max: 50, message: '发起人长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

// 获取流程实例列表
const getProcessInstanceList = async () => {
  loading.value = true
  try {
    const data = await getProcessInstanceListApi()
    processInstanceList.value = data
    pagination.total = data.length
  } catch (error) {
    ElMessage.error('获取流程实例列表失败：' + error.message)
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
  processInstanceForm.processId = 0 // 重置为数字0，确保类型正确
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
        ElMessage.success(isEditMode.value ? '编辑流程实例成功' : '添加流程实例成功')
        dialogVisible.value = false
        getProcessInstanceList()
      } catch (error) {
        ElMessage.error((isEditMode.value ? '编辑流程实例失败' : '添加流程实例失败') + '：' + error.message)
      }
    }
  })
}

// 删除流程实例
const handleDeleteProcessInstance = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条流程实例吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const response = await deleteProcessInstance(row.id)
    ElMessage.success('删除流程实例成功')
    getProcessInstanceList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除流程实例失败：' + error.message)
    }
  }
}

// 启动流程实例
const handleStartProcessInstance = async (row) => {
  try {
    const response = await startProcessInstance(row.id)
    ElMessage.success('启动流程实例成功')
    getProcessInstanceList()
  } catch (error) {
    ElMessage.error('启动流程实例失败：' + error.message)
  }
}

// 完成流程实例
const handleCompleteProcessInstance = async (row) => {
  try {
    const response = await completeProcessInstance(row.id)
    ElMessage.success('完成流程实例成功')
    getProcessInstanceList()
  } catch (error) {
    ElMessage.error('完成流程实例失败：' + error.message)
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
    case 0: return '待启动'
    case 1: return '运行中'
    case 2: return '已完成'
    default: return '未知状态'
  }
}

// 组件挂载时获取数据
onMounted(() => {
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