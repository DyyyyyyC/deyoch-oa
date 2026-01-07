<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('processInstanceManagement.title') }}</h2>
    </div>

    <!-- 流程实例列表 -->
    <el-card class="table-card">
      <!-- 操作和搜索区域 -->
      <div class="action-search-area">
        <!-- 左侧操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="handleAddProcessInstance">
            <el-icon><Plus /></el-icon>
            {{ $t('processInstanceManagement.addProcessInstance') }}
          </el-button>
          <el-button type="primary" @click="handleBatchEdit" :disabled="selectedProcessInstances.length !== 1">
            <el-icon><Edit /></el-icon>
            {{ $t('common.edit') }}
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedProcessInstances.length === 0">
            <el-icon><Delete /></el-icon>
            {{ $t('common.delete') }}
          </el-button>
        </div>
        
        <!-- 右侧搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" inline>
            <el-form-item>
              <el-input 
                v-model="searchForm.instanceName" 
                :placeholder="$t('processInstanceManagement.enterInstanceName')" 
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
        :data="processInstanceList"
        border
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="instanceName" :label="$t('processInstanceManagement.instanceName')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="processName" :label="$t('processInstanceManagement.processName')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="creatorName" :label="$t('processInstanceManagement.initiator')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="startTime" :label="$t('processInstanceManagement.startTime')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="endTime" :label="$t('processInstanceManagement.endTime')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('processInstanceManagement.status')" min-width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-form-item :label="$t('processInstanceManagement.initiator')" prop="userId">
          <el-input
            v-model="processInstanceForm.userId"
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
import '@/style/management-layout.css'

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

// 选中的流程实例
const selectedProcessInstances = ref([])

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
  userId: '',
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
  userId: [
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
    const processedData = data.map(instance => {
      const process = processList.value.find(p => p.id === instance.processId)
      return {
        ...instance,
        processName: process ? process.processName : '未知流程'
      }
    })
    
    // 实现前端搜索过滤
    let filteredInstances = processedData
    
    // 按实例名称过滤
    if (searchForm.instanceName && searchForm.instanceName.trim()) {
      filteredInstances = filteredInstances.filter(instance => 
        instance.instanceName && instance.instanceName.toLowerCase().includes(searchForm.instanceName.toLowerCase())
      )
    }
    
    processInstanceList.value = filteredInstances
    pagination.total = filteredInstances.length
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取流程实例列表失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 处理多选框选择变化
const handleSelectionChange = (selection) => {
  selectedProcessInstances.value = selection
  console.log('选中的流程实例：', selectedProcessInstances.value)
}

// 搜索流程实例
const handleSearch = () => {
  // 实现真正的搜索逻辑
  pagination.currentPage = 1
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
  processInstanceForm.userId = row.userId
  processInstanceForm.status = row.status
  dialogVisible.value = true
}

// 批量编辑流程实例
const handleBatchEdit = () => {
  if (selectedProcessInstances.value.length !== 1) {
    ElMessage.warning('请选择一个流程实例进行编辑')
    return
  }
  // 填充表单数据
  const row = selectedProcessInstances.value[0]
  isEditMode.value = true
  processInstanceForm.id = row.id
  processInstanceForm.processId = row.processId
  processInstanceForm.instanceName = row.instanceName
  processInstanceForm.userId = row.userId
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
  processInstanceForm.userId = ''
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

// 批量删除流程实例
const handleBatchDelete = async () => {
  if (selectedProcessInstances.value.length === 0) {
    ElMessage.warning('请选择要删除的流程实例')
    return
  }
  
  try {
    const instanceNames = selectedProcessInstances.value.map(instance => instance.instanceName).join(', ')
    await ElMessageBox.confirm('确定要删除流程实例 ' + instanceNames + ' 吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量删除
    for (const instance of selectedProcessInstances.value) {
      await deleteProcessInstance(instance.id)
    }
    
    ElMessage.success('删除流程实例成功')
    getProcessInstanceList()
    // 清空选择
    selectedProcessInstances.value = []
  } catch (error) {
    if (error !== 'cancel') {
      // 只在error.message不为空时显示详细错误信息
      if (error.message) {
        ElMessage.error('删除流程实例失败：' + error.message)
      }
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
/* 流程实例管理页面特定样式 */
.dialog-footer {
  text-align: right;
}
</style>