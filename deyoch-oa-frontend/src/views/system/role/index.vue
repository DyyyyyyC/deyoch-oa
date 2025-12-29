<template>
  <div class="role-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>{{ $t('roleManagement.title') }}</h2>
      <el-button type="primary" @click="handleAddRole">
        <el-icon><Plus /></el-icon>
        {{ $t('roleManagement.addRole') }}
      </el-button>
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
      <el-table
        v-loading="loading"
        :data="roleList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" :label="$t('roleManagement.roleName')" />
        <el-table-column prop="roleCode" :label="$t('roleManagement.roleCode')" />
        <el-table-column prop="description" :label="$t('roleManagement.description')" />
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
        <el-table-column prop="createdAt" :label="$t('roleManagement.createdAt')" width="200" />
        <el-table-column prop="updatedAt" :label="$t('roleManagement.updatedAt')" width="200" />
        <el-table-column :label="$t('roleManagement.actions')" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditRole(scope.row)">
              <el-icon><Edit /></el-icon>
              {{ $t('roleManagement.edit') }}
            </el-button>
            <el-button size="small" type="success" @click="handleAssignPermission(scope.row)">
              <el-icon><Setting /></el-icon>
              {{ $t('roleManagement.assignPermission') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteRole(scope.row)">
              <el-icon><Delete /></el-icon>
              {{ $t('roleManagement.delete') }}
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
  return form.id ? '编辑角色' : '添加角色'
})

// 表单验证规则
const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
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
    ElMessage.error('获取角色列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取权限树
const getPermissionTree = async () => {
  permissionLoading.value = true
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    const treeData = await get('/permission/tree')
    permissionTree.value = treeData
  } catch (error) {
    ElMessage.error('获取权限树失败：' + error.message)
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

// 编辑角色
const handleEditRole = (row) => {
  // 填充表单数据
  Object.assign(form, row)
  // 打开对话框
  dialogVisible.value = true
}

// 删除角色
const handleDeleteRole = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除角色 ' + row.roleName + ' 吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // axios拦截器已经处理了code检查，直接返回data字段
    await del('/role/' + row.id)
    ElMessage.success('删除角色成功')
    getRoleList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除角色失败：' + error.message)
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    // 使用现有的更新角色端点，直接传递包含status的完整row对象
    await put('/role/' + row.id, row)
  } catch (error) {
    ElMessage.error('更新状态失败：' + error.message)
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
      ElMessage.error('提交失败：' + error.message)
    }
  }
}

// 打开分配权限对话框
const handleAssignPermission = async (row) => {
  // 保存当前角色ID
  currentRoleId.value = row.id
  // 加载权限树
  await getPermissionTree()
  // 获取角色已分配的权限
  await getRolePermissions(row.id)
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
    ElMessage.error('获取角色权限失败：' + error.message)
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
    ElMessage.error('分配权限失败：' + error.message)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getRoleList()
})
</script>

<style scoped>
.role-management {
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

.dialog-footer {
  text-align: right;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}
</style>