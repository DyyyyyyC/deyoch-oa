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
          <el-button type="info" @click="handleExpandAll">
            全部展开
          </el-button>
          <el-button type="info" @click="handleCollapseAll">
            全部折叠
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
                @input="handleSearch"
              />
            </el-form-item>
            <el-form-item>
              <el-input 
                v-model="searchForm.permCode" 
                placeholder="请输入权限代码" 
                clearable
                @input="handleSearch"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 错误状态显示 -->
      <div v-if="errorState.hasError" class="error-container">
        <el-alert
          :title="errorState.errorMessage"
          type="error"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>{{ errorState.errorMessage }}</p>
            <el-button 
              v-if="errorState.canRetry" 
              type="primary" 
              size="small" 
              @click="handleRetry"
              style="margin-top: 10px;"
            >
              重试
            </el-button>
          </template>
        </el-alert>
      </div>

      <!-- 树形表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="permissionTreeData"
        border
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :selectable="isRowSelectable" />
        <el-table-column prop="permName" label="权限名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="permission-name-cell">
              <el-icon v-if="row.permType === 'menu'" class="permission-icon menu-icon">
                <Menu />
              </el-icon>
              <el-icon v-else class="permission-icon button-icon">
                <Operation />
              </el-icon>
              <span 
                class="permission-name" 
                :class="{ 'matched': row._matched }"
                v-html="currentKeyword ? highlightKeyword(row.permName, currentKeyword) : row.permName"
              ></span>
              <el-tag v-if="row.level !== undefined" size="small" type="info" class="level-tag">
                L{{ row.level + 1 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="permCode" label="权限代码" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span 
              :class="{ 'matched': row._matched }"
              v-html="currentKeyword ? highlightKeyword(row.permCode, currentKeyword) : row.permCode"
            ></span>
          </template>
        </el-table-column>
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
            <el-button type="success" link @click="handleAddChild(row)">添加子权限</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
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
          <el-tree-select
            v-model="form.parentId"
            :data="permissionTreeData"
            :props="{ label: 'permName', value: 'id' }"
            placeholder="请选择父权限"
            clearable
            check-strictly
            :render-after-expand="false"
          >
            <template #default="{ node, data }">
              <span>{{ data.permName }}</span>
              <span style="color: #8492a6; font-size: 13px; margin-left: 8px;">({{ data.permCode }})</span>
            </template>
          </el-tree-select>
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

    <!-- 权限表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="120px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限名称" prop="permName">
              <el-input 
                v-model="form.permName" 
                placeholder="请输入权限名称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限代码" prop="permCode">
              <el-input 
                v-model="form.permCode" 
                placeholder="请输入权限代码"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限类型" prop="permType">
              <el-select v-model="form.permType" placeholder="请选择权限类型" style="width: 100%">
                <el-option label="菜单" value="menu">
                  <el-icon><Menu /></el-icon>
                  <span style="margin-left: 8px;">菜单</span>
                </el-option>
                <el-option label="按钮" value="button">
                  <el-icon><Operation /></el-icon>
                  <span style="margin-left: 8px;">按钮</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch 
                v-model="form.status" 
                :active-value="1" 
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="父权限" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentPermissionOptions"
            :props="{ label: 'permName', value: 'id', children: 'children' }"
            placeholder="请选择父权限（不选择则为根权限）"
            clearable
            check-strictly
            :render-after-expand="false"
            style="width: 100%"
            :disabled="isEditingRootPermission"
          >
            <template #default="{ node, data }">
              <div class="tree-select-option">
                <el-icon class="option-icon">
                  <Menu v-if="data.permType === 'menu'" />
                  <Operation v-else />
                </el-icon>
                <span class="option-name">{{ data.permName }}</span>
                <span class="option-code">({{ data.permCode }})</span>
              </div>
            </template>
          </el-tree-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="路径" prop="path">
              <el-input 
                v-model="form.path" 
                placeholder="请输入路径"
                maxlength="200"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input 
                v-model="form.icon" 
                placeholder="请输入图标类名"
                maxlength="50"
              >
                <template #prefix>
                  <el-icon v-if="form.icon" :class="form.icon" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="组件" prop="component">
          <el-input 
            v-model="form.component" 
            placeholder="请输入组件路径"
            maxlength="200"
          />
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number 
            v-model="form.sort" 
            :min="0" 
            :max="9999"
            :step="1" 
            placeholder="请输入排序值"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ form.id ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Menu, Operation } from '@element-plus/icons-vue'
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

// 当前搜索关键词
const currentKeyword = ref('')

// 权限数据
const permissionList = ref([])
const permissionTreeData = ref([])
const selectedPermissions = ref([])

// 表单相关
const dialogVisible = ref(false)
const formRef = ref(null)
const tableRef = ref(null)

// 错误处理状态
const errorState = reactive({
  hasError: false,
  errorMessage: '',
  canRetry: false
})

// 重试计数
const retryCount = ref(0)
const MAX_RETRY = 3

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

// ==================== 工具函数 ====================

/**
 * 将扁平的权限数据转换为树形结构
 */
const buildPermissionTree = (permissions) => {
  const map = new Map()
  const roots = []
  
  // 创建映射并初始化
  permissions.forEach(permission => {
    const node = {
      ...permission,
      children: [],
      level: 0,
      hasChildren: false
    }
    map.set(permission.id, node)
  })
  
  // 构建树形结构
  permissions.forEach(permission => {
    const node = map.get(permission.id)
    if (permission.parentId && permission.parentId !== 0 && map.has(permission.parentId)) {
      const parent = map.get(permission.parentId)
      node.level = parent.level + 1
      parent.children.push(node)
      parent.hasChildren = true
    } else {
      roots.push(node)
    }
  })
  
  return roots
}

/**
 * 搜索过滤树形权限数据
 */
const filterPermissionTree = (tree, keyword) => {
  if (!keyword) return tree
  
  const filterNode = (node) => {
    const matches = node.permName.includes(keyword) || 
                   node.permCode.includes(keyword)
    
    const filteredChildren = node.children
      ? node.children.map(filterNode).filter(Boolean)
      : []
    
    if (matches || filteredChildren.length > 0) {
      return {
        ...node,
        children: filteredChildren,
        _matched: matches
      }
    }
    
    return null
  }
  
  return tree.map(filterNode).filter(Boolean)
}

/**
 * 高亮搜索关键词
 */
const highlightKeyword = (text, keyword) => {
  if (!keyword || !text) return text
  
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark style="background-color: #fff2cc; padding: 0 2px;">$1</mark>')
}

/**
 * 判断行是否可选择
 */
const isRowSelectable = (row) => {
  return true
}

// ==================== 数据加载 ====================

/**
 * 加载权限列表
 */
const loadPermissionList = async (isRetry = false) => {
  loading.value = true
  errorState.hasError = false
  
  try {
    const response = await getPermissionList()
    
    let permissions = []
    if (response && response.records && Array.isArray(response.records)) {
      permissions = response.records
    } else if (Array.isArray(response)) {
      permissions = response
    } else {
      throw new Error('数据格式错误')
    }
    
    processPermissionData(permissions)
    retryCount.value = 0
  } catch (error) {
    console.error('加载权限列表失败:', error)
    
    errorState.hasError = true
    errorState.errorMessage = error.message || '网络错误'
    errorState.canRetry = retryCount.value < MAX_RETRY
    
    if (error.response?.status === 403) {
      errorState.errorMessage = '权限不足，请联系管理员'
      errorState.canRetry = false
      ElMessage.error('权限不足，请联系管理员')
    } else if (error.response?.status >= 500) {
      errorState.errorMessage = '服务器错误，请稍后重试'
      ElMessage.error('服务器错误，请稍后重试')
    } else {
      ElMessage.error('获取权限列表失败：' + errorState.errorMessage)
    }
    
    if (!isRetry && errorState.canRetry) {
      retryCount.value++
      setTimeout(() => {
        loadPermissionList(true)
      }, 2000 * retryCount.value)
    }
  } finally {
    loading.value = false
  }
}

/**
 * 处理权限数据
 */
const processPermissionData = (permissions) => {
  permissionList.value = permissions
  permissionTreeData.value = buildPermissionTree([...permissions])
  
  // 应用搜索过滤
  const keyword = searchForm.permName || searchForm.permCode
  currentKeyword.value = keyword
  
  if (keyword) {
    permissionTreeData.value = filterPermissionTree(permissionTreeData.value, keyword)
  }
}

/**
 * 手动重试
 */
const handleRetry = () => {
  retryCount.value = 0
  loadPermissionList(false)
}

// ==================== 搜索功能 ====================

/**
 * 搜索权限
 */
const handleSearch = () => {
  const keyword = searchForm.permName || searchForm.permCode
  currentKeyword.value = keyword
  
  if (keyword) {
    permissionTreeData.value = filterPermissionTree(buildPermissionTree([...permissionList.value]), keyword)
  } else {
    permissionTreeData.value = buildPermissionTree([...permissionList.value])
  }
}

/**
 * 重置搜索表单
 */
const handleReset = () => {
  searchForm.permName = ''
  searchForm.permCode = ''
  currentKeyword.value = ''
  permissionTreeData.value = buildPermissionTree([...permissionList.value])
}

// ==================== 表格操作 ====================

/**
 * 处理选择变化
 */
const handleSelectionChange = (selection) => {
  selectedPermissions.value = selection
}

/**
 * 全部展开
 */
const handleExpandAll = () => {
  if (!tableRef.value) return
  
  const expandAllNodes = (nodes) => {
    nodes.forEach(node => {
      if (node.children && node.children.length > 0) {
        tableRef.value.toggleRowExpansion(node, true)
        expandAllNodes(node.children)
      }
    })
  }
  
  expandAllNodes(permissionTreeData.value)
}

/**
 * 全部折叠
 */
const handleCollapseAll = () => {
  if (!tableRef.value) return
  
  const collapseAllNodes = (nodes) => {
    nodes.forEach(node => {
      if (node.children && node.children.length > 0) {
        tableRef.value.toggleRowExpansion(node, false)
        collapseAllNodes(node.children)
      }
    })
  }
  
  collapseAllNodes(permissionTreeData.value)
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
 * 添加子权限
 */
const handleAddChild = (row) => {
  resetForm()
  form.parentId = row.id
  dialogVisible.value = true
}

/**
 * 删除权限
 */
const handleDelete = async (row) => {
  try {
    let confirmMessage = `确定要删除权限 "${row.permName}" 吗？`
    
    if (row.children && row.children.length > 0) {
      confirmMessage = `权限 "${row.permName}" 包含 ${row.children.length} 个子权限，确定要一并删除吗？`
    }
    
    await ElMessageBox.confirm(confirmMessage, '确认删除', {
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
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedPermissions.value.length} 个权限吗？\n权限名称：${permNames}`,
      '确认批量删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    
    // 批量删除
    for (const perm of selectedPermissions.value) {
      await deletePermission(perm.id)
    }
    
    ElMessage.success(`成功删除 ${selectedPermissions.value.length} 个权限`)
    selectedPermissions.value = []
    loadPermissionList(false)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('批量删除失败：' + (error.message || '未知错误'))
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
 * 添加子权限
 */
const handleAddChild = (row) => {
  resetForm()
  form.parentId = row.id
  dialogVisible.value = true
}

/**
 * 删除权限
 */
const handleDelete = async (row) => {
  try {
    let confirmMessage = `确定要删除权限 "${row.permName}" 吗？`
    
    if (row.children && row.children.length > 0) {
      confirmMessage = `权限 "${row.permName}" 包含 ${row.children.length} 个子权限，确定要一并删除吗？`
    }
    
    await ElMessageBox.confirm(confirmMessage, '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      distinguishCancelAndClose: true
    })
    
    await deletePermission(row.id)
    ElMessage.success('删除权限成功')
    loadPermissionList(false)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除权限失败：' + (error.message || '未知错误'))
    }
  }
}

/**
 * 状态变更
 */
const handleStatusChange = async (row) => {
  const originalStatus = row.status === 1 ? 0 : 1
  row._statusLoading = true
  
  try {
    await updatePermission(row.id, { status: row.status })
    ElMessage.success(`权限状态已${row.status === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    ElMessage.error('更新状态失败：' + (error.message || '未知错误'))
    // 恢复原来的状态
    row.status = originalStatus
  } finally {
    row._statusLoading = false
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
 * 关闭对话框
 */
const handleDialogClose = () => {
  dialogVisible.value = false
  resetForm()
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    if (form.id) {
      await updatePermission(form.id, form)
      ElMessage.success('编辑权限成功')
    } else {
      await createPermission(form)
      ElMessage.success('添加权限成功')
    }
    
    dialogVisible.value = false
    resetForm()
    loadPermissionList(false)
  } catch (error) {
    if (error.message) {
      ElMessage.error((form.id ? '编辑权限失败' : '添加权限失败') + '：' + error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

// ==================== 表单验证 ====================

// 最大权限层级深度
const MAX_PERMISSION_LEVEL = 5

// 表单验证规则
const rules = {
  permName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '权限名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  permCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { min: 2, max: 50, message: '权限编码长度在 2 到 50 个字符', trigger: 'blur' },
    { validator: validatePermissionCode, trigger: 'blur' }
  ],
  permType: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ],
  parentId: [
    { validator: validateParentId, trigger: 'change' }
  ],
  sort: [
    { type: 'number', min: 0, max: 9999, message: '排序值必须在 0-9999 之间', trigger: 'blur' }
  ]
}

/**
 * 验证权限码唯一性
 */
function validatePermissionCode(rule, value, callback) {
  if (!value) {
    callback()
    return
  }
  
  // 检查权限码格式
  const codeRegex = /^[a-zA-Z][a-zA-Z0-9:_-]*$/
  if (!codeRegex.test(value)) {
    callback(new Error('权限编码格式不正确，应以字母开头，只能包含字母、数字、冒号、下划线和横线'))
    return
  }
  
  // 检查权限码是否已存在（排除当前编辑的权限）
  const existingPerm = permissionList.value.find(perm => 
    perm.permCode === value && perm.id !== form.id
  )
  
  if (existingPerm) {
    callback(new Error('权限编码已存在'))
  } else {
    callback()
  }
}

/**
 * 验证父权限选择（防止循环引用）
 */
function validateParentId(rule, value, callback) {
  if (!value || value === 0) {
    callback()
    return
  }
  
  // 如果是编辑模式，检查是否会造成循环引用
  if (form.id) {
    if (value === form.id) {
      callback(new Error('不能选择自己作为父权限'))
      return
    }
    
    // 检查是否选择了自己的子权限作为父权限
    const isChildPermission = checkIsChildPermission(form.id, value)
    if (isChildPermission) {
      callback(new Error('不能选择子权限作为父权限'))
      return
    }
  }
  
  // 检查权限层级深度
  const parentLevel = getPermissionLevel(value)
  if (parentLevel >= MAX_PERMISSION_LEVEL - 1) {
    callback(new Error(`权限层级不能超过 ${MAX_PERMISSION_LEVEL} 级`))
    return
  }
  
  callback()
}

/**
 * 获取权限层级深度
 */
const getPermissionLevel = (permissionId) => {
  let level = 0
  let current = permissionList.value.find(p => p.id === permissionId)
  
  while (current && current.parentId !== 0) {
    level++
    current = permissionList.value.find(p => p.id === current.parentId)
    
    // 防止无限循环
    if (level > MAX_PERMISSION_LEVEL) {
      break
    }
  }
  
  return level
}

/**
 * 检查是否是子权限
 */
const checkIsChildPermission = (parentId, childId) => {
  const findInChildren = (nodes, targetId) => {
    for (const node of nodes) {
      if (node.id === targetId) {
        return true
      }
      if (node.children && node.children.length > 0) {
        if (findInChildren(node.children, targetId)) {
          return true
        }
      }
    }
    return false
  }
  
  // 找到父权限节点
  const parentNode = permissionList.value.find(p => p.id === parentId)
  if (!parentNode) return false
  
  // 构建父权限的子树
  const parentTree = buildPermissionTree(permissionList.value.filter(p => 
    p.id === parentId || isDescendant(p.id, parentId)
  ))
  
  return findInChildren(parentTree, childId)
}

/**
 * 检查是否是后代权限
 */
const isDescendant = (nodeId, ancestorId) => {
  let current = permissionList.value.find(p => p.id === nodeId)
  while (current && current.parentId !== 0) {
    if (current.parentId === ancestorId) {
      return true
    }
    current = permissionList.value.find(p => p.id === current.parentId)
  }
  return false
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadPermissionList()
})

// ==================== 监听器 ====================

// 监听搜索关键词变化
watch(searchKeyword, (newKeyword) => {
  if (newKeyword) {
    nextTick(() => {
      autoExpandMatchedNodes()
    })
  } else {
    expandedRowKeys.value = []
  }
})
</script>

<style scoped>
/* 权限管理页面特定样式 */
.dialog-footer {
  text-align: right;
}

/* 权限名称单元格样式 */
.permission-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.permission-icon {
  font-size: 16px;
}

.permission-icon.menu-icon {
  color: #67c23a;
}

.permission-icon.button-icon {
  color: #e6a23c;
}

.permission-name {
  font-weight: 500;
  flex: 1;
}

.permission-name.matched {
  font-weight: 600;
}

.level-tag {
  margin-left: auto;
  font-size: 10px;
  padding: 2px 6px;
}

/* 搜索匹配高亮样式 */
.matched {
  background-color: rgba(64, 158, 255, 0.1);
  border-radius: 3px;
  padding: 2px 4px;
}

:deep(mark) {
  background-color: #fff2cc;
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 600;
}

/* 错误状态样式 */
.error-container {
  margin: 20px 0;
}

.error-container .el-alert {
  margin-bottom: 20px;
}
</style>