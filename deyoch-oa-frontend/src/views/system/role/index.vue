<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('roleManagement.title') }}</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('roleManagement.roleName')">
          <el-input v-model="searchForm.roleName" :placeholder="$t('roleManagement.enterRoleName')" clearable />
        </el-form-item>
        <el-form-item :label="$t('roleManagement.roleCode')">
          <el-input v-model="searchForm.roleCode" :placeholder="$t('roleManagement.enterRoleCode')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('roleManagement.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('roleManagement.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 角色列表 -->
    <el-card class="table-card">
      <!-- 操作区域 -->
      <div class="action-area">
        <el-button type="primary" @click="handleAddRole">
          <el-icon><Plus /></el-icon>
          {{ $t('roleManagement.addRole') }}
        </el-button>
        <el-button type="primary" @click="handleBatchEdit" :disabled="selectedRoles.length !== 1">
          <el-icon><Edit /></el-icon>
          {{ $t('roleManagement.edit') }}
        </el-button>
        <el-button type="success" @click="handleBatchAssignPermission" :disabled="selectedRoles.length !== 1">
          <el-icon><Setting /></el-icon>
          {{ $t('roleManagement.assignPermission') }}
        </el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRoles.length === 0">
          <el-icon><Delete /></el-icon>
          {{ $t('roleManagement.delete') }}
        </el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="roleList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="roleName" :label="$t('roleManagement.roleName')" width="120" />
        <el-table-column prop="roleCode" :label="$t('roleManagement.roleCode')" width="120" />
        <el-table-column prop="description" :label="$t('roleManagement.description')" width="200" />
        <el-table-column prop="status" :label="$t('roleManagement.status')" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('roleManagement.createdAt')" width="180" />
        <el-table-column prop="updatedAt" :label="$t('roleManagement.updatedAt')" width="180" />
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

    <!-- 添加/编辑角色对话框 -->
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
        <el-form-item :label="$t('roleManagement.roleName')" prop="roleName">
          <el-input v-model="form.roleName" :placeholder="$t('roleManagement.enterRoleName')" />
        </el-form-item>
        <el-form-item :label="$t('roleManagement.roleCode')" prop="roleCode">
          <el-input v-model="form.roleCode" :placeholder="$t('roleManagement.enterRoleCode')" />
        </el-form-item>
        <el-form-item :label="$t('roleManagement.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="$t('roleManagement.enterDescription')" />
        </el-form-item>
        <el-form-item :label="$t('roleManagement.status')" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('roleManagement.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ $t('roleManagement.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 角色分配权限对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="$t('roleManagement.assignPermission')"
      width="600px"
    >
      <div class="permission-tree">
        <el-tree
          v-loading="permissionLoading"
          :data="permissionTree"
          show-checkbox
          node-key="id"
          ref="permissionTreeRef"
          :default-expanded-keys="[0]"
          :props="defaultProps"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">{{ $t('roleManagement.cancel') }}</el-button>
          <el-button type="primary" @click="handlePermissionSubmit">{{ $t('roleManagement.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Setting } from '@element-plus/icons-vue'
import { get, post, put, del } from '@/utils/axios'
import { useI18n } from 'vue-i18n'

// 获取i18n的t函数
const { t } = useI18n()

// 加载状态
const loading = ref(false)
const permissionLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  roleName: '',
  roleCode: ''
})

// 角色列表
const roleList = ref([])

// 选中的角色
const selectedRoles = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框控制
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const formRef = ref(null)
const permissionTreeRef = ref(null)
const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

// 当前选中的角色ID（用于分配权限）
const currentRoleId = ref(null)

// 权限树数据
const permissionTree = ref([])

// 树的默认属性
const defaultProps = {
  children: 'children',
  label: 'permName'
}

// 对话框标题
const dialogTitle = computed(() => {
  return form.id ? t('roleManagement.edit') : t('roleManagement.addRole')
})

// 表单验证规则
const rules = {
  roleName: [
    { required: true, message: t('roleManagement.enterRoleName'), trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: t('roleManagement.enterRoleCode'), trigger: 'blur' },
    { min: 2, max: 30, message: '角色编码长度在 2 到 30 个字符', trigger: 'blur' }
  ]
}

// 获取角色列表
const getRoleList = async () => {
  loading.value = true
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    const roleData = await get('/role/list')
    roleList.value = roleData
    pagination.total = roleData.length
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取角色列表失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 处理多选框选择变化
const handleSelectionChange = (selection) => {
  selectedRoles.value = selection
  console.log('选中的角色：', selectedRoles.value)
}

// 获取权限树
const getPermissionTree = async () => {
  permissionLoading.value = true
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    const treeData = await get('/permission/tree')
    permissionTree.value = treeData
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取权限树失败：' + error.message)
    }
  } finally {
    permissionLoading.value = false
  }
}

// 搜索角色
const handleSearch = () => {
  // 这里可以实现带条件的搜索，暂时先调用getRoleList()
  getRoleList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.roleName = ''
  searchForm.roleCode = ''
  getRoleList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getRoleList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getRoleList()
}

// 添加角色
const handleAddRole = () => {
  // 重置表单
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
  // 打开对话框
  dialogVisible.value = true
}

// 批量编辑角色
const handleBatchEdit = () => {
  if (selectedRoles.value.length !== 1) {
    ElMessage.warning('请选择一个角色进行编辑')
    return
  }
  // 填充表单数据
  Object.assign(form, selectedRoles.value[0])
  // 打开对话框
  dialogVisible.value = true
}

// 批量删除角色
const handleBatchDelete = async () => {
  if (selectedRoles.value.length === 0) {
    ElMessage.warning('请选择要删除的角色')
    return
  }
  
  try {
    const roleNames = selectedRoles.value.map(role => role.roleName).join(', ')
    await ElMessageBox.confirm('确定要删除角色 ' + roleNames + ' 吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量删除
    for (const role of selectedRoles.value) {
      // axios拦截器已经处理了code检查，直接返回data字段
      await del('/role/' + role.id)
    }
    
    ElMessage.success('删除角色成功')
    getRoleList()
    // 清空选择
    selectedRoles.value = []
  } catch (error) {
    if (error !== 'cancel') {
      // 只在error.message不为空时显示详细错误信息
      if (error.message) {
        ElMessage.error('删除角色失败：' + error.message)
      }
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    // 使用现有的更新角色端点，直接传递包含status的完整row对象
    await put('/role/' + row.id, row)
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('更新状态失败：' + error.message)
    }
    // 恢复原来的状态
    getRoleList()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    let response
    if (form.id) {
      // 编辑角色
      await put('/role/' + form.id, form)
    } else {
      // 添加角色
      await post('/role', form)
    }
    
    ElMessage.success(form.id ? '编辑角色成功' : '添加角色成功')
    dialogVisible.value = false
    getRoleList()
  } catch (error) {
    if (error.name === 'Error') {
      // 只在error.message不为空时显示详细错误信息
      if (error.message) {
        ElMessage.error('提交失败：' + error.message)
      }
    }
  }
}

// 批量打开分配权限对话框
const handleBatchAssignPermission = async () => {
  if (selectedRoles.value.length !== 1) {
    ElMessage.warning('请选择一个角色进行权限分配')
    return
  }
  // 保存当前角色ID
  currentRoleId.value = selectedRoles.value[0].id
  // 加载权限树
  await getPermissionTree()
  // 获取角色已分配的权限
  await getRolePermissions(selectedRoles.value[0].id)
  // 打开对话框
  permissionDialogVisible.value = true
}

// 获取角色已分配的权限
const getRolePermissions = async (roleId) => {
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    const permissionIds = await get('/role/' + roleId + '/perms')
    // 设置默认选中的权限
    setTimeout(() => {
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(permissionIds)
      }
    }, 100)
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取角色权限失败：' + error.message)
    }
  }
}

// 提交权限分配
const handlePermissionSubmit = async () => {
  if (!permissionTreeRef.value) return
  
  try {
    // 获取选中的权限ID
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    
    // axios拦截器已经处理了code检查，直接返回data字段
    await post('/role/' + currentRoleId.value + '/assign-perms', checkedKeys)
    ElMessage.success('分配权限成功')
    permissionDialogVisible.value = false
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('分配权限失败：' + error.message)
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getRoleList()
})
</script>

<style scoped>
/* 角色管理页面特定样式 */
.dialog-footer {
  text-align: right;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}
</style>