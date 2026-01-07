<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('processManagement.title') }}</h2>
    </div>

    <!-- 流程列表 -->
    <el-card class="table-card">
      <!-- 操作和搜索区域 -->
      <div class="action-search-area">
        <!-- 左侧操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="handleAddProcess">
            <el-icon><Plus /></el-icon>
            {{ $t('processManagement.addProcess') }}
          </el-button>
          <el-button type="primary" @click="handleBatchEdit" :disabled="selectedProcesses.length !== 1">
            <el-icon><Edit /></el-icon>
            {{ $t('common.edit') }}
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedProcesses.length === 0">
            <el-icon><Delete /></el-icon>
            {{ $t('common.delete') }}
          </el-button>
        </div>
        
        <!-- 右侧搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" inline>
            <el-form-item>
              <el-input 
                v-model="searchForm.processName" 
                :placeholder="$t('processManagement.enterProcessName')" 
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
        :data="processList"
        border
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="processName" :label="$t('processManagement.processName')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="processKey" :label="$t('processManagement.processKey')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="description" :label="$t('processManagement.description')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('processManagement.status')" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? $t('processManagement.statusEnabled') : $t('processManagement.statusDisabled') }}
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

    <!-- 流程表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="processFormRef"
        :model="processForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item :label="$t('processManagement.processName')" prop="processName">
          <el-input
            v-model="processForm.processName"
            :placeholder="$t('processManagement.enterProcessName')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('processManagement.processKey')" prop="processKey">
          <el-input
            v-model="processForm.processKey"
            :placeholder="$t('processManagement.enterProcessKey')"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('processManagement.description')" prop="description">
          <el-input
            v-model="processForm.description"
            type="textarea"
            :placeholder="$t('processManagement.enterDescription')"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="$t('processManagement.status')" prop="status">
          <el-switch
            v-model="processForm.status"
            :active-value="1"
            :inactive-value="0"
            :active-text="$t('processManagement.statusEnabled')"
            :inactive-text="$t('processManagement.statusDisabled')"
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { 
  getProcessList as getProcessListApi, 
  createProcess, 
  updateProcess, 
  deleteProcess 
} from '@/api/process'
import '@/style/management-layout.css'

// 获取i18n的t函数
const { t } = useI18n()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  processName: ''
})

// 流程列表
const processList = ref([])

// 选中的流程
const selectedProcesses = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const processFormRef = ref(null)
const isEditMode = ref(false)

// 对话框标题
const dialogTitle = computed(() => {
  return isEditMode.value ? t('processManagement.editProcess') : t('processManagement.addProcess')
})

// 流程表单数据
const processForm = reactive({
  id: null,
  processName: '',
  processKey: '',
  description: '',
  creatorId: null,
  status: 1
})

// 表单验证规则
const formRules = {
  processName: [
    { required: true, message: t('processManagement.enterProcessName'), trigger: 'blur' },
    { min: 2, max: 100, message: t('common.fieldLength', { min: 2, max: 100, field: t('processManagement.processName') }), trigger: 'blur' }
  ],
  processKey: [
    { required: true, message: t('processManagement.enterProcessKey'), trigger: 'blur' },
    { min: 2, max: 50, message: t('common.fieldLength', { min: 2, max: 50, field: t('processManagement.processKey') }), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: t('processManagement.processKeyFormat'), trigger: 'blur' }
  ],
  description: [
    { required: false, message: t('processManagement.enterDescription'), trigger: 'blur' },
    { max: 500, message: t('common.fieldMaxLength', { max: 500, field: t('processManagement.description') }), trigger: 'blur' }
  ]
}

// 获取流程列表
const getProcessList = async () => {
  loading.value = true
  try {
    const data = await getProcessListApi()
    
    // 实现前端搜索过滤
    let filteredProcesses = data
    
    // 按流程名称过滤
    if (searchForm.processName && searchForm.processName.trim()) {
      filteredProcesses = filteredProcesses.filter(process => 
        process.processName && process.processName.toLowerCase().includes(searchForm.processName.toLowerCase())
      )
    }
    
    processList.value = filteredProcesses
    pagination.total = filteredProcesses.length
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取流程列表失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 搜索流程
const handleSearch = () => {
  // 实现真正的搜索逻辑
  pagination.currentPage = 1
  getProcessList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.processName = ''
  getProcessList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getProcessList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getProcessList()
}

// 打开添加流程对话框
const handleAddProcess = () => {
  isEditMode.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑流程对话框
const handleEditProcess = (row) => {
  isEditMode.value = true
  processForm.id = row.id
  processForm.processName = row.processName
  processForm.processKey = row.processKey
  processForm.description = row.description
  processForm.status = row.status
  dialogVisible.value = true
}

// 批量编辑流程
const handleBatchEdit = () => {
  if (selectedProcesses.value.length !== 1) {
    ElMessage.warning('请选择一个流程进行编辑')
    return
  }
  // 填充表单数据
  const row = selectedProcesses.value[0]
  isEditMode.value = true
  processForm.id = row.id
  processForm.processName = row.processName
  processForm.processKey = row.processKey
  processForm.description = row.description
  processForm.creatorId = row.creatorId
  processForm.status = row.status
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (processFormRef.value) {
    processFormRef.value.resetFields()
  }
  processForm.id = null
  processForm.processName = ''
  processForm.processKey = ''
  processForm.description = ''
  processForm.creatorId = null
  processForm.status = 1
}

// 提交表单
const handleSubmit = async () => {
  if (!processFormRef.value) return
  await processFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
          let response
          if (isEditMode.value) {
            response = await updateProcess(processForm.id, processForm)
          } else {
            response = await createProcess(processForm)
          }
          ElMessage.success(isEditMode.value ? t('processManagement.editSuccess') : t('processManagement.addSuccess'))
          dialogVisible.value = false
          getProcessList()
        } catch (error) {
          // 只在error.message不为空时显示详细错误信息
          if (error.message) {
            ElMessage.error((isEditMode.value ? t('processManagement.editFailed') : t('processManagement.addFailed')) + '：' + error.message)
          } else {
            ElMessage.error(isEditMode.value ? t('processManagement.editFailed') : t('processManagement.addFailed'))
          }
        }
    }
  })
}

// 删除流程
const handleDeleteProcess = async (row) => {
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
    const response = await deleteProcess(row.id)
    ElMessage.success(t('processManagement.deleteSuccess'))
    getProcessList()
  } catch (error) {
    if (error !== 'cancel') {
      // 只在error.message不为空时显示详细错误信息
      if (error.message) {
        ElMessage.error(t('processManagement.deleteFailed') + '：' + error.message)
      }
    }
  }
}

// 表格行选择事件处理
const handleSelectionChange = (selection) => {
  selectedProcesses.value = selection
}

// 批量删除流程
const handleBatchDelete = async () => {
  if (selectedProcesses.value.length === 0) {
    ElMessage.warning('请选择要删除的流程')
    return
  }
  
  try {
    const processNames = selectedProcesses.value.map(process => process.processName).join(', ')
    await ElMessageBox.confirm('确定要删除流程 ' + processNames + ' 吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量删除
    for (const process of selectedProcesses.value) {
      await deleteProcess(process.id)
    }
    
    ElMessage.success('删除流程成功')
    getProcessList()
    // 清空选择
    selectedProcesses.value = []
  } catch (error) {
    if (error !== 'cancel') {
      // 只在error.message不为空时显示详细错误信息
      if (error.message) {
        ElMessage.error('删除流程失败：' + error.message)
      }
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getProcessList()
})
</script>

<style scoped>
/* 流程管理页面特定样式 */
.dialog-footer {
  text-align: right;
}

/* 修复英文模式下标签和星号之间的间隔问题 */
:deep(.el-form-item__label) {
  /* 移除默认的margin-right，使用padding来控制间隔 */
  margin-right: 0 !important;
  /* 确保星号和文本之间的间距一致 */
  position: relative;
  padding-right: 4px;
}

:deep(.el-form-item__label.is-required::before) {
  /* 调整必填星号的位置和样式 */
  margin-right: 4px;
  color: var(--el-color-danger);
  content: "*";
  font-size: 14px;
  font-family: sans-serif;
  line-height: 1;
  vertical-align: middle;
}
</style>