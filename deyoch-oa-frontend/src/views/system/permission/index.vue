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

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="permissionTreeData"
        border
        style="width: 100%"
        row-key="id"
        :tree-props="treeTableConfig.treeProps"
        :default-expand-all="false"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="permName" :label="$t('permissionManagement.permName')" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="permission-name-cell" :style="{ paddingLeft: (row.level || 0) * 20 + 'px' }">
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
                L{{ row.level }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="permCode" :label="$t('permissionManagement.permCode')" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span 
              :class="{ 'matched': row._matched }"
              v-html="currentKeyword ? highlightKeyword(row.permCode, currentKeyword) : row.permCode"
            ></span>
          </template>
        </el-table-column>
        <el-table-column prop="permType" :label="$t('permissionManagement.permType')" min-width="120">
          <template #default="scope">
            <el-tag :type="scope.row.permType === 'menu' ? 'success' : 'warning'">
              {{ scope.row.permType === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
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
import { Plus, Edit, Delete, Menu, Operation } from '@element-plus/icons-vue'
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

// 数据转换工具函数
/**
 * 将扁平的权限数据转换为树形结构
 * @param {Array} permissions 扁平权限数据
 * @returns {Array} 树形权限数据
 */
const buildPermissionTree = (permissions) => {
  const map = new Map()
  const roots = []
  
  // 创建映射并初始化
  permissions.forEach(permission => {
    permission.children = []
    permission.level = 0
    permission.hasChildren = false
    map.set(permission.id, permission)
  })
  
  // 构建树形结构
  permissions.forEach(permission => {
    if (permission.parentId && permission.parentId !== 0 && map.has(permission.parentId)) {
      const parent = map.get(permission.parentId)
      permission.level = parent.level + 1
      parent.children.push(permission)
      parent.hasChildren = true
    } else {
      roots.push(permission)
    }
  })
  
  return roots
}

/**
 * 搜索过滤树形权限数据
 * @param {Array} tree 树形权限数据
 * @param {String} keyword 搜索关键词
 * @returns {Array} 过滤后的树形数据
 */
const filterPermissionTree = (tree, keyword) => {
  if (!keyword) return tree
  
  const filterNode = (node) => {
    const matches = node.permName.includes(keyword) || 
                   node.permCode.includes(keyword) || 
                   (node.description && node.description.includes(keyword))
    
    const filteredChildren = node.children
      ? node.children.filter(filterNode)
      : []
    
    if (matches || filteredChildren.length > 0) {
      return {
        ...node,
        children: filteredChildren,
        _matched: matches, // 标记是否匹配，用于高亮
        _expanded: matches || filteredChildren.length > 0 // 标记是否应该展开
      }
    }
    
    return null
  }
  
  return tree.filter(filterNode).filter(Boolean)
}

/**
 * 高亮搜索关键词
 * @param {String} text 原始文本
 * @param {String} keyword 关键词
 * @returns {String} 高亮后的HTML
 */
const highlightKeyword = (text, keyword) => {
  if (!keyword || !text) return text
  
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark style="background-color: #fff2cc; padding: 0 2px;">$1</mark>')
}

// 加载状态
const loading = ref(false)
const treeLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  permName: '',
  permCode: ''
})

// 当前搜索关键词
const currentKeyword = ref('')

// 数据缓存
const permissionCache = ref(null)
const cacheTimestamp = ref(0)
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

// 搜索防抖定时器
let searchTimer = null

// 错误处理状态
const errorState = reactive({
  hasError: false,
  errorMessage: '',
  canRetry: false
})

// 重试计数
const retryCount = ref(0)
const MAX_RETRY = 3

// 权限列表（树形数据）
const permissionList = ref([])
const permissionTreeData = ref([])

// 选中的权限
const selectedPermissions = ref([])

// 权限树数据（用于右侧展示）
const permissionTree = ref([])

// 父权限列表（用于下拉选择）
const parentPermissionList = ref([])

// 树形表格配置
const treeTableConfig = {
  rowKey: 'id',
  treeProps: {
    children: 'children',
    hasChildren: 'hasChildren'
  },
  defaultExpandAll: false,
  expandRowKeys: []
}

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 组件引用
const formRef = ref(null)
const tableRef = ref(null)
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
    { min: 2, max: 50, message: '权限编码长度在 2 到 50 个字符', trigger: 'blur' },
    { validator: validatePermissionCode, trigger: 'blur' }
  ],
  parentId: [
    { validator: validateParentId, trigger: 'change' }
  ]
}

// 验证权限码唯一性
const validatePermissionCode = (rule, value, callback) => {
  if (!value) {
    callback()
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

// 最大权限层级深度
const MAX_PERMISSION_LEVEL = 5

// 验证父权限选择（防止循环引用）
const validateParentId = (rule, value, callback) => {
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

// 获取权限层级深度
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

// 检查是否是子权限
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

// 检查是否是后代权限
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

// 检查缓存是否有效
const isCacheValid = () => {
  return permissionCache.value && 
         cacheTimestamp.value && 
         (Date.now() - cacheTimestamp.value) < CACHE_DURATION
}

// 加载权限列表
const loadPermissionList = async (useCache = true, isRetry = false) => {
  // 如果有有效缓存且允许使用缓存，直接使用缓存数据
  if (useCache && isCacheValid()) {
    console.log('使用缓存数据')
    processPermissionData(permissionCache.value)
    return
  }
  
  loading.value = true
  errorState.hasError = false
  
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
    } else {
      throw new Error('数据格式错误')
    }
    
    // 缓存数据
    permissionCache.value = permissions
    cacheTimestamp.value = Date.now()
    
    processPermissionData(permissions, total)
    
    // 重置重试计数
    retryCount.value = 0
  } catch (error) {
    console.error('加载权限列表失败:', error)
    
    // 设置错误状态
    errorState.hasError = true
    errorState.errorMessage = error.message || '网络错误'
    errorState.canRetry = retryCount.value < MAX_RETRY
    
    // 显示错误消息
    if (error.response?.status === 403) {
      errorState.errorMessage = '权限不足，请联系管理员'
      errorState.canRetry = false
      ElMessage.error('权限不足，请联系管理员')
    } else if (error.response?.status >= 500) {
      errorState.errorMessage = '服务器错误，请稍后重试'
      ElMessage.error('服务器错误，请稍后重试')
    } else if (error.code === 'NETWORK_ERROR') {
      errorState.errorMessage = '网络连接失败，请检查网络'
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('获取权限列表失败：' + errorState.errorMessage)
    }
    
    // 如果不是重试且可以重试，则自动重试
    if (!isRetry && errorState.canRetry) {
      retryCount.value++
      setTimeout(() => {
        console.log(`自动重试第 ${retryCount.value} 次`)
        loadPermissionList(false, true)
      }, 2000 * retryCount.value) // 递增延迟
    }
  } finally {
    loading.value = false
  }
}

// 手动重试
const handleRetry = () => {
  retryCount.value = 0
  loadPermissionList(false)
}

// 处理权限数据
const processPermissionData = (permissions, total = 0) => {
  // 存储扁平数据
  permissionList.value = permissions
  pagination.total = total
  
  // 转换为树形数据
  permissionTreeData.value = buildPermissionTree([...permissions])
  
  // 应用搜索过滤
  const keyword = searchForm.permName || searchForm.permCode
  currentKeyword.value = keyword
  
  if (keyword) {
    permissionTreeData.value = filterPermissionTree(permissionTreeData.value, keyword)
    
    // 自动展开匹配的节点
    setTimeout(() => {
      autoExpandMatchedNodes(permissionTreeData.value)
    }, 100)
  }
  
  // 过滤出父权限列表（用于添加/编辑时选择父权限）
  parentPermissionList.value = permissions.filter(perm => perm.parentId === 0)
}

// 自动展开匹配的节点
const autoExpandMatchedNodes = (nodes) => {
  if (!tableRef.value) return
  
  nodes.forEach(node => {
    if (node._expanded && node.children && node.children.length > 0) {
      tableRef.value.toggleRowExpansion(node, true)
      autoExpandMatchedNodes(node.children)
    }
  })
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

// 搜索权限（防抖）
const handleSearch = () => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 防抖处理
  searchTimer = setTimeout(() => {
    // 重新加载数据并应用搜索过滤
    pagination.currentPage = 1
    loadPermissionList(false) // 搜索时不使用缓存
  }, 300) // 300ms防抖延迟
}

// 清除缓存
const clearCache = () => {
  permissionCache.value = null
  cacheTimestamp.value = 0
}

// 重置搜索表单
const handleReset = () => {
  searchForm.permName = ''
  searchForm.permCode = ''
  currentKeyword.value = ''
  loadPermissionList()
}

// 全部展开
const handleExpandAll = () => {
  if (!tableRef.value) return
  
  // 递归展开所有节点
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

// 全部折叠
const handleCollapseAll = () => {
  if (!tableRef.value) return
  
  // 递归折叠所有节点
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

// 编辑权限
const handleEdit = (row) => {
  // 填充表单数据
  Object.assign(form, row)
  // 打开对话框
  dialogVisible.value = true
}

// 添加子权限
const handleAddChild = (row) => {
  // 重置表单
  form.id = null
  form.permName = ''
  form.permCode = ''
  form.permType = 'menu'
  form.parentId = row.id // 设置父权限ID
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  // 打开对话框
  dialogVisible.value = true
}

// 删除权限
const handleDelete = async (row) => {
  try {
    // 检查是否有子权限
    if (row.children && row.children.length > 0) {
      const result = await ElMessageBox.confirm(
        `权限 "${row.permName}" 包含 ${row.children.length} 个子权限，确定要一并删除吗？`,
        '确认删除',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          distinguishCancelAndClose: true
        }
      )
    } else {
      await ElMessageBox.confirm(
        `确定要删除权限 "${row.permName}" 吗？`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    }
    
    await deletePermission(row.id)
    ElMessage.success('删除权限成功')
    
    // 清除缓存并重新加载数据
    clearCache()
    loadPermissionList(false)
    loadPermissionTree()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除权限失败：' + (error.message || '未知错误'))
    }
  }
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
    
    // 清除缓存并重新加载数据
    clearCache()
    loadPermissionList(false)
    loadPermissionTree()
  } catch (error) {
    if (error.message) {
      ElMessage.error((form.id ? '编辑权限失败' : '添加权限失败') + '：' + error.message)
    } else {
      ElMessage.error(form.id ? '编辑权限失败' : '添加权限失败')
    }
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

/* 树形表格层级样式 */
:deep(.el-table__row) {
  .el-table__indent {
    padding-left: 20px;
  }
}

/* 权限层级颜色区分 */
:deep(.el-table__body-wrapper) {
  .el-table__row[data-level="0"] {
    background-color: #fafafa;
  }
  
  .el-table__row[data-level="1"] {
    background-color: #f5f7fa;
  }
  
  .el-table__row[data-level="2"] {
    background-color: #ffffff;
  }
}

/* 权限名称单元格样式 */
.permission-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
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