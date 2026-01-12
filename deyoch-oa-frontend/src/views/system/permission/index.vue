<template>
  <div class="permission-tree-table">
    <!-- 页面头部 -->
    <PageHeader title="权限管理" />
    
    <!-- 操作栏 -->
    <PageActionBar>
      <el-button type="primary" @click="handleAddPermission">
        <el-icon><Plus /></el-icon>
        添加权限
      </el-button>
      <el-button 
        type="primary" 
        @click="handleBatchEdit" 
        :disabled="selectedPermissions.length !== 1"
      >
        <el-icon><Edit /></el-icon>
        编辑权限
      </el-button>
      <el-button 
        type="danger" 
        @click="handleBatchDelete" 
        :disabled="selectedPermissions.length === 0"
      >
        <el-icon><Delete /></el-icon>
        批量删除 ({{ selectedPermissions.length }})
      </el-button>
      <el-button type="info" @click="handleExpandAll">
        <el-icon><ArrowDown /></el-icon>
        全部展开
      </el-button>
      <el-button type="info" @click="handleCollapseAll">
        <el-icon><ArrowUp /></el-icon>
        全部折叠
      </el-button>
      
      <template #right>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索权限名称或代码"
          style="width: 300px"
          clearable
          @input="handleSearchInput"
          @clear="handleSearchClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </template>
    </PageActionBar>

    <!-- 树形表格卡片 -->
    <el-card>
      <!-- 统计信息 -->
      <div class="table-stats" v-if="!loading">
        <el-tag type="info" size="small">
          总计: {{ totalPermissions }} 个权限
        </el-tag>
        <el-tag v-if="searchKeyword" type="warning" size="small" style="margin-left: 8px;">
          搜索结果: {{ filteredCount }} 个
        </el-tag>
        <el-tag v-if="selectedPermissions.length > 0" type="success" size="small" style="margin-left: 8px;">
          已选择: {{ selectedPermissions.length }} 个
        </el-tag>
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
        :data="displayPermissions"
        border
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
        :expand-row-keys="expandedRowKeys"
        @selection-change="handleSelectionChange"
        @expand-change="handleExpandChange"
        empty-text="暂无权限数据"
      >
        <!-- 选择列 -->
        <el-table-column type="selection" width="55" :selectable="isRowSelectable" />
        
        <!-- 权限名称（树形结构） -->
        <el-table-column prop="permName" label="权限名称" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="permission-name-cell">
              <!-- 权限图标 -->
              <el-icon 
                :class="[
                  'permission-icon',
                  row.permType === 'menu' ? 'menu-icon' : 'button-icon'
                ]"
              >
                <Menu v-if="row.permType === 'menu'" />
                <Operation v-else />
              </el-icon>
              
              <!-- 权限名称 -->
              <span 
                :class="[
                  'permission-name',
                  `permission-name--level-${Math.min(row.level || 0, 3)}`,
                  { 'permission-name--disabled': row.status === 0 }
                ]"
                v-html="highlightText(row.permName, searchKeyword)"
              />
              
              <!-- 层级标签 -->
              <el-tag 
                v-if="row.level !== undefined" 
                size="small" 
                :type="getLevelTagType(row.level)"
                class="level-tag"
              >
                L{{ row.level + 1 }}
              </el-tag>
              
              <!-- 子权限数量 -->
              <el-tag 
                v-if="row.children && row.children.length > 0" 
                size="small" 
                type="info" 
                class="children-count-tag"
              >
                {{ row.children.length }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <!-- 权限代码 -->
        <el-table-column prop="permCode" label="权限代码" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <code 
              class="permission-code"
              v-html="highlightText(row.permCode, searchKeyword)"
            />
          </template>
        </el-table-column>
        
        <!-- 权限类型 -->
        <el-table-column prop="permType" label="权限类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.permType === 'menu' ? 'success' : 'warning'"
              size="small"
            >
              {{ row.permType === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 路径 -->
        <el-table-column prop="path" label="路径" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <code v-if="row.path" class="path-code">{{ row.path }}</code>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <!-- 组件 -->
        <el-table-column prop="component" label="组件" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <code v-if="row.component" class="component-code">{{ row.component }}</code>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <!-- 图标 -->
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" :class="row.icon" />
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        
        <!-- 排序 -->
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        
        <!-- 状态 -->
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              :loading="row._statusLoading"
            />
          </template>
        </el-table-column>
        
        <!-- 创建时间 -->
        <el-table-column prop="createTime" label="创建时间" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <!-- 操作列 -->
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="success" link size="small" @click="handleAddChild(row)">
                <el-icon><Plus /></el-icon>
                添加子权限
              </el-button>
              <el-button 
                type="danger" 
                link 
                size="small" 
                @click="handleDelete(row)"
                :disabled="row.children && row.children.length > 0 && !canDeleteWithChildren"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Edit, Delete, Menu, Operation, Search, 
  ArrowDown, ArrowUp 
} from '@element-plus/icons-vue'
import {
  getPermissionList,
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission
} from '@/api/permission'
import PageHeader from '@/components/PageHeader.vue'
import PageActionBar from '@/components/PageActionBar.vue'

// ==================== 响应式数据 ====================

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 搜索关键词
const searchKeyword = ref('')
let searchTimer = null

// 权限数据
const permissionList = ref([])
const permissionTreeData = ref([])
const selectedPermissions = ref([])

// 表格展开状态
const expandedRowKeys = ref([])

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

// 是否正在编辑根权限
const isEditingRootPermission = computed(() => {
  return form.id && form.parentId === 0
})

// 父权限选项（用于表单选择）
const parentPermissionOptions = computed(() => {
  if (form.id) {
    // 编辑时，过滤掉自己和自己的子权限
    return filterSelfAndChildren(permissionTreeData.value, form.id)
  }
  return permissionTreeData.value
})

// 显示的权限数据（应用搜索过滤）
const displayPermissions = computed(() => {
  if (!searchKeyword.value) {
    return permissionTreeData.value
  }
  return filterPermissionTree(permissionTreeData.value, searchKeyword.value)
})

// 总权限数量
const totalPermissions = computed(() => {
  return countAllPermissions(permissionList.value)
})

// 过滤后的权限数量
const filteredCount = computed(() => {
  return countAllPermissions(flattenTree(displayPermissions.value))
})

// 是否可以删除带有子权限的权限
const canDeleteWithChildren = computed(() => {
  // 这里可以根据用户权限或配置来决定
  return true
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
    const matches = node.permName.toLowerCase().includes(keyword.toLowerCase()) || 
                   node.permCode.toLowerCase().includes(keyword.toLowerCase())
    
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
const highlightText = (text, keyword) => {
  if (!keyword || !text) return text
  
  const regex = new RegExp(`(${escapeRegExp(keyword)})`, 'gi')
  return text.replace(regex, '<mark class="search-highlight">$1</mark>')
}

/**
 * 转义正则表达式特殊字符
 */
const escapeRegExp = (string) => {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 扁平化树形数据
 */
const flattenTree = (tree) => {
  const result = []
  const flatten = (nodes) => {
    nodes.forEach(node => {
      result.push(node)
      if (node.children && node.children.length > 0) {
        flatten(node.children)
      }
    })
  }
  flatten(tree)
  return result
}

/**
 * 统计权限总数
 */
const countAllPermissions = (permissions) => {
  return permissions.length
}

/**
 * 过滤掉自己和子权限（用于父权限选择）
 */
const filterSelfAndChildren = (tree, excludeId) => {
  const filterNode = (node) => {
    if (node.id === excludeId) {
      return null
    }
    
    const filteredChildren = node.children
      ? node.children.map(filterNode).filter(Boolean)
      : []
    
    return {
      ...node,
      children: filteredChildren
    }
  }
  
  return tree.map(filterNode).filter(Boolean)
}

/**
 * 获取层级标签类型
 */
const getLevelTagType = (level) => {
  const types = ['', 'success', 'warning', 'danger']
  return types[Math.min(level, types.length - 1)] || 'info'
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

/**
 * 判断行是否可选择
 */
const isRowSelectable = (row) => {
  // 可以根据业务需求添加选择限制
  return true
}

// ==================== 数据加载 ====================

/**
 * 加载权限列表
 */
const loadPermissionList = async (useCache = true, isRetry = false) => {
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
        loadPermissionList(false, true)
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
  
  // 如果有搜索关键词，自动展开匹配的节点
  if (searchKeyword.value) {
    nextTick(() => {
      autoExpandMatchedNodes()
    })
  }
}

/**
 * 自动展开匹配的节点
 */
const autoExpandMatchedNodes = () => {
  if (!tableRef.value) return
  
  const expandedKeys = []
  const collectExpandedKeys = (nodes) => {
    nodes.forEach(node => {
      if (node._matched || (node.children && node.children.some(child => child._matched))) {
        expandedKeys.push(node.id)
        if (node.children) {
          collectExpandedKeys(node.children)
        }
      }
    })
  }
  
  collectExpandedKeys(displayPermissions.value)
  expandedRowKeys.value = expandedKeys
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
 * 搜索输入处理（防抖）
 */
const handleSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(() => {
    if (searchKeyword.value) {
      autoExpandMatchedNodes()
    }
  }, 300)
}

/**
 * 清除搜索
 */
const handleSearchClear = () => {
  searchKeyword.value = ''
  expandedRowKeys.value = []
}

// ==================== 表格操作 ====================

/**
 * 处理选择变化
 */
const handleSelectionChange = (selection) => {
  selectedPermissions.value = selection
}

/**
 * 处理展开变化
 */
const handleExpandChange = (row, expanded) => {
  if (expanded) {
    if (!expandedRowKeys.value.includes(row.id)) {
      expandedRowKeys.value.push(row.id)
    }
  } else {
    const index = expandedRowKeys.value.indexOf(row.id)
    if (index > -1) {
      expandedRowKeys.value.splice(index, 1)
    }
  }
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
  
  expandAllNodes(displayPermissions.value)
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
  
  collapseAllNodes(displayPermissions.value)
  expandedRowKeys.value = []
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
.permission-tree-table {
  padding: 0;
}

/* 统计信息样式 */
.table-stats {
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

/* 权限名称单元格样式 */
.permission-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
}

.permission-icon {
  font-size: 16px;
  flex-shrink: 0;
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
  min-width: 0;
}

.permission-name--level-0 {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.permission-name--level-1 {
  font-weight: 500;
  color: #409eff;
}

.permission-name--level-2 {
  color: #67c23a;
}

.permission-name--level-3 {
  color: #e6a23c;
}

.permission-name--disabled {
  color: #c0c4cc;
  text-decoration: line-through;
}

.level-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 2px;
  font-weight: 500;
}

.children-count-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  background-color: #f0f9ff;
  color: #409eff;
  border: 1px solid #b3d8ff;
}

/* 权限代码样式 */
.permission-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  color: #606266;
  border: 1px solid #e4e7ed;
}

/* 路径和组件代码样式 */
.path-code,
.component-code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #909399;
  background-color: #f8f9fa;
  padding: 2px 4px;
  border-radius: 2px;
}

/* 文本静音样式 */
.text-muted {
  color: #c0c4cc;
  font-style: italic;
}

/* 搜索高亮样式 */
:deep(.search-highlight) {
  background-color: #fff2cc;
  color: #e6a23c;
  font-weight: 600;
  padding: 1px 2px;
  border-radius: 2px;
}

/* 表格操作按钮样式 */
.table-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.table-actions .el-button {
  margin: 0;
  padding: 4px 8px;
  font-size: 12px;
}

/* 对话框样式优化 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 树选择器选项样式 */
.tree-select-option {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.option-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.option-name {
  font-weight: 500;
  flex: 1;
}

.option-code {
  color: #909399;
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

/* 表格行样式优化 */
:deep(.el-table__row) {
  transition: background-color 0.2s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 表格展开图标样式 */
:deep(.el-table__expand-icon) {
  color: #409eff;
  font-size: 14px;
}

:deep(.el-table__expand-icon--expanded) {
  transform: rotate(90deg);
}

/* 状态开关样式 */
:deep(.el-switch) {
  --el-switch-on-color: #67c23a;
  --el-switch-off-color: #dcdfe6;
}

/* 错误状态样式 */
.error-container {
  margin: 20px 0;
}

.error-container .el-alert {
  border-radius: 6px;
}

/* 表格加载状态优化 */
:deep(.el-loading-mask) {
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .permission-name-cell {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .level-tag,
  .children-count-tag {
    align-self: flex-start;
  }
  
  .table-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
  
  .table-actions .el-button {
    width: 100%;
    justify-content: flex-start;
  }
}

/* 表格边框优化 */
:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
}

/* 选择框样式优化 */
:deep(.el-checkbox) {
  --el-checkbox-checked-bg-color: #409eff;
  --el-checkbox-checked-border-color: #409eff;
}

/* 标签样式优化 */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}

/* 表单样式优化 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__wrapper) {
  border-radius: 4px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 4px;
}

/* 对话框样式优化 */
:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
  border-top: 1px solid #f0f0f0;
}

/* 树选择器样式优化 */
:deep(.el-tree-select) {
  --el-tree-node-hover-bg-color: #f5f7fa;
}

/* 动画效果 */
.permission-name,
.permission-code,
.table-actions .el-button {
  transition: all 0.2s ease;
}

.permission-name:hover {
  color: #409eff;
}

/* 层级缩进线条 */
:deep(.el-table__indent) {
  position: relative;
}

:deep(.el-table__indent::before) {
  content: '';
  position: absolute;
  left: 10px;
  top: -50%;
  bottom: 50%;
  width: 1px;
  background-color: #e4e7ed;
}

/* 空状态样式 */
:deep(.el-table__empty-text) {
  color: #909399;
  font-size: 14px;
}
</style>