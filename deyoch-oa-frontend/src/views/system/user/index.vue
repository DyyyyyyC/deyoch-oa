<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('userManagement.title') }}</h2>
    </div>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <!-- 操作和搜索区域 -->
      <div class="action-search-area">
        <!-- 左侧操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" @click="handleAddUser">
            <el-icon><Plus /></el-icon>
            {{ $t('userManagement.addUser') }}
          </el-button>
          <el-button type="primary" @click="handleBatchEdit" :disabled="selectedUsers.length !== 1">
            <el-icon><Edit /></el-icon>
            {{ $t('userManagement.edit') }}
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedUsers.length === 0">
            <el-icon><Delete /></el-icon>
            {{ $t('userManagement.delete') }}
          </el-button>
        </div>
        
        <!-- 右侧搜索区域 -->
        <div class="search-area">
          <el-form :model="searchForm" inline>
            <el-form-item>
              <el-input 
                v-model="searchForm.username" 
                :placeholder="$t('userManagement.enterUsername')" 
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-select 
                v-model="searchForm.status" 
                :placeholder="$t('userManagement.selectStatus')" 
                clearable
              >
                <el-option :label="$t('userManagement.enabled')" value="1" />
                <el-option :label="$t('userManagement.disabled')" value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">{{ $t('userManagement.search') }}</el-button>
              <el-button @click="handleReset">{{ $t('userManagement.reset') }}</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="userList"
        border
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" :label="$t('userManagement.username')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" :label="$t('userManagement.nickname')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" :label="$t('userManagement.email')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" :label="$t('userManagement.phone')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="roleName" :label="$t('userManagement.role')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('userManagement.status')" min-width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('userManagement.createdAt')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" :label="$t('userManagement.updatedAt')" min-width="180" show-overflow-tooltip />
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

    <!-- 添加/编辑用户对话框 -->
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
        <el-form-item :label="$t('userManagement.username')" prop="username">
          <el-input v-model="form.username" :placeholder="$t('userManagement.enterUsername')" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="$t('userManagement.nickname')" prop="nickname">
          <el-input v-model="form.nickname" :placeholder="$t('userManagement.enterNickname')" />
        </el-form-item>
        <el-form-item :label="$t('userManagement.email')" prop="email">
          <el-input v-model="form.email" :placeholder="$t('userManagement.enterEmail')" />
        </el-form-item>
        <el-form-item :label="$t('userManagement.phone')" prop="phone">
          <el-input v-model="form.phone" :placeholder="$t('userManagement.enterPhone')" />
        </el-form-item>
        <el-form-item :label="$t('userManagement.password')" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" :placeholder="$t('userManagement.enterPassword')" />
        </el-form-item>
        <el-form-item :label="$t('userManagement.roleId')" prop="roleId">
          <el-select v-model="form.roleId" :placeholder="$t('userManagement.selectRole')">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('userManagement.status')" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('userManagement.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ $t('userManagement.confirm') }}</el-button>
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
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  updateUserStatus
} from '@/api/user'
import { getRoleList } from '@/api/role'
import '@/style/management-layout.css'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  status: ''
})

// 用户列表
const userList = ref([])

// 选中的用户
const selectedUsers = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 角色列表（用于下拉选择）
const roleList = ref([])

// 对话框控制
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  roleId: null,
  status: 1
})

// 对话框标题
const dialogTitle = computed(() => {
  return form.id ? '编辑用户' : '添加用户'
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取角色列表（仅在需要时调用）
const loadRoleList = async () => {
  try {
    // 使用API调用，不传分页参数获取所有角色
    const roleData = await getRoleList()
    // 如果返回的是分页数据，取records字段
    if (roleData && roleData.records) {
      roleList.value = roleData.records
    } else if (Array.isArray(roleData)) {
      roleList.value = roleData
    } else {
      roleList.value = []
    }
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('获取角色列表失败：' + error.message)
    }
  }
}

// 获取用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    // 调用分页接口
    const response = await getUserList({
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.username
    })
    
    // 处理分页响应数据
    let users = []
    let total = 0
    
    // 检查响应数据结构
    if (response && typeof response === 'object') {
      if (response.records && Array.isArray(response.records)) {
        // PageResult格式：{ current, size, total, pages, records }
        users = response.records
        total = response.total || 0
      } else if (Array.isArray(response)) {
        // 直接数组格式
        users = response
        total = response.length
      }
    }
    
    // 后端已经返回了roleName，不需要前端处理
    userList.value = users
    pagination.total = total
  } catch (error) {
    ElMessage.error('获取用户列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 处理多选框选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 搜索用户
const handleSearch = () => {
  // 实现真正的搜索逻辑
  loading.value = true
  
  try {
    // 获取完整用户列表
    loadUserList().then(() => {
      // 在前端进行搜索过滤
      let filteredUsers = [...userList.value]
      
      // 按用户名搜索
      if (searchForm.username && searchForm.username.trim()) {
        filteredUsers = filteredUsers.filter(user => 
          user.username.toLowerCase().includes(searchForm.username.toLowerCase()) ||
          user.nickname.toLowerCase().includes(searchForm.username.toLowerCase())
        )
      }
      
      // 按状态搜索
      if (searchForm.status !== '' && searchForm.status !== null && searchForm.status !== undefined) {
        filteredUsers = filteredUsers.filter(user => 
          user.status === Number(searchForm.status)
        )
      }
      
      // 更新显示的用户列表
      userList.value = filteredUsers
      pagination.total = filteredUsers.length
    })
  } catch (error) {
    if (error.message) {
      ElMessage.error('搜索失败：' + error.message)
    }
  }
}

// 重置搜索表单
const handleReset = () => {
  searchForm.username = ''
  searchForm.status = ''
  loadUserList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadUserList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadUserList()
}

// 添加用户
const handleAddUser = async () => {
  // 重置表单
  form.id = null
  form.username = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.password = ''
  form.roleId = null
  form.status = 1
  // 打开对话框前加载角色列表
  await loadRoleList()
  // 打开对话框
  dialogVisible.value = true
}

// 批量编辑用户
const handleBatchEdit = async () => {
  if (selectedUsers.value.length !== 1) {
    ElMessage.warning('请选择一个用户进行编辑')
    return
  }
  // 填充表单数据
  const user = selectedUsers.value[0]
  
  // 重置表单
  form.id = null
  form.username = ''
  form.nickname = ''
  form.email = ''
  form.phone = ''
  form.password = ''
  form.roleId = null
  form.status = 1
  
  // 填充数据
  form.id = user.id
  form.username = user.username
  form.nickname = user.nickname
  form.email = user.email
  form.phone = user.phone
  form.roleId = Number(user.roleId)
  form.status = Number(user.status)
  
  // 打开对话框前加载角色列表
  await loadRoleList()
  // 打开对话框
  dialogVisible.value = true
}

// 批量删除用户
const handleBatchDelete = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  
  try {
    const usernames = selectedUsers.value.map(user => user.username).join(', ')
    await ElMessageBox.confirm('确定要删除用户 ' + usernames + ' 吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量删除
    for (const user of selectedUsers.value) {
      // 使用API调用
      await deleteUser(user.id)
    }
    
    ElMessage.success('删除用户成功')
    loadUserList()
    // 清空选择
    selectedUsers.value = []
  } catch (error) {
    if (error !== 'cancel') {
      // 只在error.message不为空时显示详细错误信息
      if (error.message) {
        ElMessage.error('删除用户失败：' + error.message)
      }
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    // 使用API调用
    await updateUserStatus(row.id, row.status)
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error('更新状态失败：' + error.message)
    }
    // 恢复原来的状态
    loadUserList()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (form.id) {
      // 编辑用户
      await updateUser(form.id, form)
    } else {
      // 添加用户
      await createUser(form)
    }
    
    ElMessage.success(form.id ? '编辑用户成功' : '添加用户成功')
    dialogVisible.value = false
    loadUserList()
  } catch (error) {
    // 只在error.message不为空时显示详细错误信息
    if (error.message) {
      ElMessage.error((form.id ? '编辑用户失败' : '添加用户失败') + '：' + error.message)
    } else {
      ElMessage.error(form.id ? '编辑用户失败' : '添加用户失败')
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
/* 用户管理页面特定样式 */
.dialog-footer {
  text-align: right;
}
</style>