<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>权限管理</h2>
    </div>

    <!-- 权限列表 -->
    <el-card class="table-card">
      <!-- 操作和搜索区域 -->
      <div class="action-search-area">
        <!-- 左侧操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="handleAddPermission">
            <el-icon><Plus /></el-icon>
            添加权限
          </el-button>
          <el-button type="primary" @click="handleBatchEdit" :disabled="selectedPermissions.length !== 1">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedPermissions.length === 0">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
        
        <!-- 右侧搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" inline>
            <el-form-item>
              <el-input 
                v-model="searchForm.permName" 
                placeholder="请输入权限名称" 
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-input 
                v-model="searchForm.permCode" 
                placeholder="请输入权限代码" 
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 权限表格 -->
      <el-table
        v-loading="loading"
        :data="permissionList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="permName" label="权限名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="permCode" label="权限代码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="permType" label="权限类型" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.permType === 'menu' ? 'success' : 'warning'">
              {{ scope.row.permType === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="component" label="组件" min-width="200" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" min-width="120" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
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
        <el-form-item label="权限名称" prop="permName">
          <el-input v-model="form.permName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限代码" prop="permCode">
          <el-input v-model="form.permCode" placeholder="请输入权限代码" />
        </el-form-item>
        <el-form-item label="权限类型" prop="permType">
          <el-select v-model="form.permType" placeholder="请选择权限类型">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
          </el-select>
        </el-form-item>
        <el-form-item label="父权限" prop="parentId">
          <el-select v-model="form.parentId" placeholder="请选择父权限" clearable>
            <el-option label="无" :value="0" />
            <el-option
              v-for="permission in permissionList"
              :key="permission.id"
              :label="permission.permName"
              :value="permission.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路径" />
        </el-form-item>
        <el-form-item label="组件" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :step="1" placeholder="请输入排序" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确认</el-button>
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
  getPermissionList,
  createPermission,
  updatePermission,
  deletePermission
} from '@/api/permission'
import '@/style/management-layout.css'

// ==================== 响应式数据 ====================

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  permName: '',
  permCode: ''
})

// 权限数据
const permissionList = ref([])
const selectedPermissions = ref([])

// 表单相关
const dialogVisible = ref(false)
const formRef = ref(null)

// 分页数据
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 权限表单数据
const form = reactive({
  id: null,
  permName: '',
  permCode: '',
  permType: 'menu',
  parentId: 0,
  path: '',
  component: '',
  icon: '',
  sort: 0,
  status: 1
})

// ==================== 计算属性 ====================

// 对话框标题
const dialogTitle = computed(() => {
  return form.id ? '编辑权限' : '添加权限'
})

// ==================== 数据加载 ====================

/**
 * 加载权限列表
 */
const loadPermissionList = async () => {
  loading.value = true
  try {
    // 构建分页参数
    const params = {
      page: pagination.current,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.permName) {
      params.permName = searchForm.permName
    }
    if (searchForm.permCode) {
      params.permCode = searchForm.permCode
    }
    
    console.log('加载权限列表，参数:', params)
    const response = await getPermissionList(params)
    console.log('权限列表响应:', response)
    
    let permissions = []
    let total = 0
    
    if (response && response.records && Array.isArray(response.records)) {
      // 分页格式: {records: [], total: 0, current: 1, size: 20}
      permissions = response.records
      total = response.total || 0
    } else if (response && response.success && response.data) {
      // 成功响应格式: {success: true, data: {records: [], total: 0}}
      if (response.data.records && Array.isArray(response.data.records)) {
        permissions = response.data.records
        total = response.data.total || 0
      } else if (Array.isArray(response.data)) {
        permissions = response.data
        total = response.data.length
      }
    } else if (Array.isArray(response)) {
      // 直接返回数组（兼容旧接口）
      permissions = response
      total = response.length
    } else {
      throw new Error('数据格式错误')
    }
    
    permissionList.value = permissions
    pagination.total = total
    
    console.log('解析后的权限列表:', permissions)
    console.log('总数:', total)
  } catch (error) {
    console.error('加载权限列表失败:', error)
    ElMessage.error('获取权限列表失败：' + (error.message || '网络错误'))
    permissionList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// ==================== 搜索功能 ====================

/**
 * 搜索权限
 */
const handleSearch = () => {
  // 重置到第一页
  pagination.current = 1
  loadPermissionList()
}

/**
 * 重置搜索表单
 */
const handleReset = () => {
  searchForm.permName = ''
  searchForm.permCode = ''
  pagination.current = 1
  loadPermissionList()
}

// ==================== 表格操作 ====================

/**
 * 处理选择变化
 */
const handleSelectionChange = (selection) => {
  selectedPermissions.value = selection
}

// ==================== 分页操作 ====================

/**
 * 分页大小改变
 */
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadPermissionList()
}

/**
 * 当前页改变
 */
const handleCurrentChange = (page) => {
  pagination.current = page
  loadPermissionList()
}

// ==================== CRUD操作 ====================

/**
 * 添加权限
 */
const handleAddPermission = () => {
  resetForm()
  dialogVisible.value = true
}

/**
 * 批量编辑权限
 */
const handleBatchEdit = () => {
  if (selectedPermissions.value.length !== 1) {
    ElMessage.warning('请选择一个权限进行编辑')
    return
  }
  
  Object.assign(form, selectedPermissions.value[0])
  dialogVisible.value = true
}

/**
 * 批量删除权限
 */
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
    selectedPermissions.value = []
    loadPermissionList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除权限失败：' + (error.message || '未知错误'))
    }
  }
}

/**
 * 编辑权限
 */
const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

/**
 * 删除权限
 */
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除权限 "${row.permName}" 吗？`, '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePermission(row.id)
    ElMessage.success('删除权限成功')
    loadPermissionList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除权限失败：' + (error.message || '未知错误'))
    }
  }
}

/**
 * 状态变更
 */
const handleStatusChange = async (row) => {
  try {
    await updatePermission(row.id, { status: row.status })
    ElMessage.success(`权限状态已${row.status === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    ElMessage.error('更新状态失败：' + (error.message || '未知错误'))
    // 恢复原来的状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// ==================== 表单处理 ====================

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(form, {
    id: null,
    permName: '',
    permCode: '',
    permType: 'menu',
    parentId: 0,
    path: '',
    component: '',
    icon: '',
    sort: 0,
    status: 1
  })
  
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (form.id) {
      await updatePermission(form.id, form)
      ElMessage.success('编辑权限成功')
    } else {
      await createPermission(form)
      ElMessage.success('添加权限成功')
    }
    
    dialogVisible.value = false
    resetForm()
    loadPermissionList()
  } catch (error) {
    if (error.message) {
      ElMessage.error((form.id ? '编辑权限失败' : '添加权限失败') + '：' + error.message)
    }
  }
}

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

// ==================== 生命周期 ====================

onMounted(() => {
  loadPermissionList()
})
</script>

<style scoped>
/* 权限管理页面特定样式 */
.dialog-footer {
  text-align: right;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>