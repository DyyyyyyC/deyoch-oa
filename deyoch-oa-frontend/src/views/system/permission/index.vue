<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('permissionManagement.title') }}</h2>
    </div>

    <!-- 权限列表 -->
    <el-card class="table-card">
      <!-- 操作和搜索区域 -->
      <div class="action-search-area">
        <!-- 左侧操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="handleAddPermission">
            <el-icon><Plus /></el-icon>
            {{ $t('permissionManagement.addPermission') }}
          </el-button>
          <el-button type="primary" @click="handleBatchEdit" :disabled="selectedPermissions.length !== 1">
            <el-icon><Edit /></el-icon>
            {{ $t('permissionManagement.edit') }}
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedPermissions.length === 0">
            <el-icon><Delete /></el-icon>
            {{ $t('permissionManagement.delete') }}
          </el-button>
        </div>
        
        <!-- 右侧搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" inline>
            <el-form-item>
              <el-input 
                v-model="searchForm.permName" 
                :placeholder="$t('permissionManagement.enterPermName')" 
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-input 
                v-model="searchForm.permCode" 
                :placeholder="$t('permissionManagement.enterPermCode')" 
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">{{ $t('permissionManagement.search') }}</el-button>
              <el-button @click="handleReset">{{ $t('permissionManagement.reset') }}</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="permissionList"
        border
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="permName" :label="$t('permissionManagement.permName')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="permCode" :label="$t('permissionManagement.permCode')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="permType" :label="$t('permissionManagement.permType')" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.permType === 'menu' ? 'success' : 'warning'">
              {{ scope.row.permType === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentName" :label="$t('permissionManagement.parentName')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="path" :label="$t('permissionManagement.path')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="component" :label="$t('permissionManagement.component')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="icon" :label="$t('permissionManagement.icon')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="sort" :label="$t('permissionManagement.sort')" min-width="100" />
        <el-table-column prop="status" :label="$t('permissionManagement.status')" min-width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('permissionManagement.createdAt')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" :label="$t('permissionManagement.updatedAt')" min-width="180" show-overflow-tooltip />
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

    <!-- 添加/编辑权限对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="100px"
      >
        <el-form-item :label="$t('permissionManagement.permName')" prop="permName">
          <el-input v-model="form.permName" :placeholder="$t('permissionManagement.enterPermName')" />
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.permCode')" prop="permCode">
          <el-input v-model="form.permCode" :placeholder="$t('permissionManagement.enterPermCode')" />
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.permType')" prop="permType">
          <el-select v-model="form.permType" :placeholder="$t('permissionManagement.selectPermType')">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.parentPermission')" prop="parentId">
          <el-select v-model="form.parentId" :placeholder="$t('permissionManagement.selectParentPermission')">
              <el-option :label="$t('permissionManagement.rootPermission')" :value="0" />
              <el-option
                v-for="perm in parentPermissionList"
                :key="perm.id"
                :label="perm.permName"
                :value="perm.id"
              />
            </el-select>
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.path')" prop="path">
          <el-input v-model="form.path" :placeholder="$t('permissionManagement.enterPath')" />
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.component')" prop="component">
          <el-input v-model="form.component" :placeholder="$t('permissionManagement.enterComponent')" />
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.icon')" prop="icon">
          <el-input v-model="form.icon" :placeholder="$t('permissionManagement.enterIcon')" />
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.sort')" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :step="1" :placeholder="$t('permissionManagement.enterSort')" />
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.status')" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('permissionManagement.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ $t('permissionManagement.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限树展示 -->
    <el-card class="tree-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('permissionManagement.permissionTree') }}</span>
        </div>
      </template>
      <div class="permission-tree">
        <el-tree
          v-loading="treeLoading"
          :data="permissionTree"
          node-key="id"
          :props="defaultProps"
          :default-expanded-keys="[0]"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <span class="node-label">{{ node.label }}</span>
              <span class="node-code">({{ data.permCode }})</span>
            </span>
          </template>
        </el-tree>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getPermissionList,
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission
} from '@/api/permission'
import { useI18n } from 'vue-i18n'
import '@/style/management-layout.css'

// 获取i18n的t函数
const { t } = useI18n()

// 加载状态
const loading = ref(false)
const treeLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  permName: '',
  permCode: ''
})

// 权限列表
const permissionList = ref([])

// 选中的权限
const selectedPermissions = ref([])

// 权限树数据
const permissionTree = ref([])

// 父权限列表（用于下拉选择）
const parentPermissionList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框控制
const dialogVisible = ref(false)
const formRef = ref(null)
// 权限表单数据
const form = reactive({
  id: null,
  permName: '',
  permCode: '',
  permType: 'menu', // 默认权限类型为菜单
  parentId: 0,
  path: '',
  component: '',
  icon: '',
  sort: 0,
  status: 1
})

// 树的默认属性
const defaultProps = {
  children: 'children',
  label: 'permName'
}

// 对话框标题
const dialogTitle = computed(() => {
  return form.id ? '编辑权限' : '添加权限'
})

// 表单验证规则
const rules = {
  permName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '权限名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  permCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { min: 2, max: 50, message: '权限编码长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 加载权限列表
const loadPermissionList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.permName || searchForm.permCode
    }
    
    const response = await getPermissionList(params)
    
    // 处理分页响应数据
    let permissions = []
    let total = 0
    
    if (response && response.records && Array.isArray(response.records)) {
      // 新的分页格式：PageResult
      permissions = response.records
      total = response.total || 0
    } else if (Array.isArray(response)) {
      // 旧格式兼容：直接返回数组
      permissions = response
      total = response.length
    }
    
    // 处理parentName字段
    const processedData = permissions.map(perm => {
      let parentName = ''
      if (perm.parentId === 0) {
        parentName = '根权限'
      } else {
        const parentPerm = permissions.find(p => p.id === perm.parentId)
        parentName = parentPerm ? parentPerm.permName : '未知权限'
      }
      return { ...perm, parentName }
    })
    
    permissionList.value = processedData
    pagination.total = total
    
    // 过滤出父权限列表（用于添加/编辑时选择父权限）
    parentPermissionList.value = processedData.filter(perm => perm.parentId === 0)
  } catch (error) {
    ElMessage.error('获取权限列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 处理多选框选择变化
const handleSelectionChange = (selection) => {
  selectedPermissions.value = selection
  console.log('选中的权限：', selectedPermissions.value)
}

// 加载权限树
const loadPermissionTree = async () => {
  treeLoading.value = true
  try {
    const treeData = await getPermissionTree()
    permissionTree.value = treeData
  } catch (error) {
    ElMessage.error('获取权限树失败：' + (error.message || '未知错误'))
  } finally {
    treeLoading.value = false
  }
}

// 搜索权限
const handleSearch = () => {
  // 实现真正的搜索逻辑
  pagination.currentPage = 1
  loadPermissionList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.permName = ''
  searchForm.permCode = ''
  loadPermissionList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadPermissionList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadPermissionList()
}

// 添加权限
const handleAddPermission = () => {
  // 重置表单
  form.id = null
  form.permName = ''
  form.permCode = ''
  form.permType = 'menu' // 默认权限类型为菜单
  form.parentId = 0
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  // 打开对话框
  dialogVisible.value = true
}

// 批量编辑权限
const handleBatchEdit = () => {
  if (selectedPermissions.value.length !== 1) {
    ElMessage.warning('请选择一个权限进行编辑')
    return
  }
  // 填充表单数据
  Object.assign(form, selectedPermissions.value[0])
  // 打开对话框
  dialogVisible.value = true
}

// 批量删除权限
const handleBatchDelete = async () => {
  if (selectedPermissions.value.length === 0) {
    ElMessage.warning('请选择要删除的权限')
    return
  }
  
  try {
    const permNames = selectedPermissions.value.map(perm => perm.permName).join(', ')
    await ElMessageBox.confirm('确定要删除权限 ' + permNames + ' 吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量删除
    for (const perm of selectedPermissions.value) {
      await deletePermission(perm.id)
    }
    
    ElMessage.success('删除权限成功')
    loadPermissionList()
    loadPermissionTree()
    // 清空选择
    selectedPermissions.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除权限失败：' + (error.message || '未知错误'))
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await updatePermission(row.id, { status: row.status })
  } catch (error) {
    ElMessage.error('更新状态失败：' + (error.message || '未知错误'))
    // 恢复原来的状态
    loadPermissionList()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (form.id) {
      // 编辑权限
      await updatePermission(form.id, form)
    } else {
      // 添加权限
      await createPermission(form)
    }
    
    ElMessage.success(form.id ? '编辑权限成功' : '添加权限成功')
    dialogVisible.value = false
    loadPermissionList()
    loadPermissionTree()
  } catch (error) {
    ElMessage.error((form.id ? '编辑权限失败' : '添加权限失败') + '：' + (error.message || '未知错误'))
  }
}

// 组件挂载时获取数据
onMounted(() => {
  loadPermissionList()
  loadPermissionTree()
})
</script>

<style scoped>
/* 权限管理页面特定样式 */
.dialog-footer {
  text-align: right;
}

.tree-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}

.tree-node {
  display: flex;
  align-items: center;
}

.node-label {
  font-weight: 500;
}

.node-code {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>