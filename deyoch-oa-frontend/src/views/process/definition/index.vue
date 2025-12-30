<template>
  <div class="process-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>{{ $t('processManagement.title') }}</h2>
      <el-button type="primary" @click="handleAddProcess">
        <el-icon><Plus /></el-icon>
        {{ $t('processManagement.addProcess') }}
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('processManagement.processName')">
          <el-input v-model="searchForm.processName" :placeholder="$t('processManagement.enterProcessName')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 流程列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="processList"
        border
        style="width: 100%"
        fit
      >
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="processName" :label="$t('processManagement.processName')" min-width="180" />
        <el-table-column prop="processKey" :label="$t('processManagement.processKey')" min-width="150" />
        <el-table-column prop="description" :label="$t('processManagement.description')" min-width="200" />
        <el-table-column prop="status" :label="$t('processManagement.status')" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('common.createdAt')" width="180" />
        <el-table-column prop="updatedAt" :label="$t('common.updatedAt')" width="180" />
        <el-table-column :label="$t('common.actions')" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditProcess(scope.row)">
              <el-icon><Edit /></el-icon>
              {{ $t('common.edit') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteProcess(scope.row)">
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
        <el-form-item label="流程名称" prop="processName">
          <el-input
            v-model="processForm.processName"
            placeholder="请输入流程名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="流程标识" prop="processKey">
          <el-input
            v-model="processForm.processKey"
            placeholder="请输入流程标识"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="processForm.description"
            type="textarea"
            placeholder="请输入流程描述"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="processForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { 
  getProcessList as getProcessListApi, 
  createProcess, 
  updateProcess, 
  deleteProcess 
} from '@/api/process'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  processName: ''
})

// 流程列表
const processList = ref([])

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
  return isEditMode.value ? '编辑流程' : '添加流程'
})

// 流程表单数据
const processForm = reactive({
  id: null,
  processName: '',
  processKey: '',
  description: '',
  status: 1
})

// 表单验证规则
const formRules = {
  processName: [
    { required: true, message: '请输入流程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '流程名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  processKey: [
    { required: true, message: '请输入流程标识', trigger: 'blur' },
    { min: 2, max: 50, message: '流程标识长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '流程标识只能包含字母、数字、下划线和连字符', trigger: 'blur' }
  ],
  description: [
    { required: false, message: '请输入流程描述', trigger: 'blur' },
    { max: 500, message: '流程描述长度不能超过 500 个字符', trigger: 'blur' }
  ]
}

// 获取流程列表
const getProcessList = async () => {
  loading.value = true
  try {
    const data = await getProcessListApi()
    processList.value = data
    pagination.total = data.length
  } catch (error) {
    ElMessage.error('获取流程列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索流程
const handleSearch = () => {
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

// 重置表单
const resetForm = () => {
  if (processFormRef.value) {
    processFormRef.value.resetFields()
  }
  processForm.id = null
  processForm.processName = ''
  processForm.processKey = ''
  processForm.description = ''
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
        ElMessage.success(isEditMode.value ? '编辑流程成功' : '添加流程成功')
        dialogVisible.value = false
        getProcessList()
      } catch (error) {
        ElMessage.error((isEditMode.value ? '编辑流程失败' : '添加流程失败') + '：' + error.message)
      }
    }
  })
}

// 删除流程
const handleDeleteProcess = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条流程吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const response = await deleteProcess(row.id)
    ElMessage.success('删除流程成功')
    getProcessList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除流程失败：' + error.message)
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getProcessList()
})
</script>

<style scoped>
.process-management {
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