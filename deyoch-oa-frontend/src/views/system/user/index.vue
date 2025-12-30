<template>
  <div class="user-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>{{ $t('userManagement.title') }}</h2>
      <el-button type="primary" @click="handleAddUser">
        <el-icon><Plus /></el-icon>
        {{ $t('userManagement.addUser') }}
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('userManagement.username')">
          <el-input v-model="searchForm.username" :placeholder="$t('userManagement.enterUsername')" clearable />
        </el-form-item>
        <el-form-item :label="$t('userManagement.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('userManagement.selectStatus')" clearable>
            <el-option :label="$t('userManagement.enabled')" value="1" />
            <el-option :label="$t('userManagement.disabled')" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('userManagement.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('userManagement.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" :label="$t('userManagement.username')" />
        <el-table-column prop="nickname" :label="$t('userManagement.nickname')" />
        <el-table-column prop="email" :label="$t('userManagement.email')" />
        <el-table-column prop="phone" :label="$t('userManagement.phone')" />
        <el-table-column prop="roleId" :label="$t('userManagement.roleId')" width="100" />
        <el-table-column prop="status" :label="$t('userManagement.status')" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('userManagement.createdAt')" width="200" />
        <el-table-column prop="updatedAt" :label="$t('userManagement.updatedAt')" width="200" />
        <el-table-column :label="$t('userManagement.actions')" min-width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditUser(scope.row)">
              <el-icon><Edit /></el-icon>
              {{ $t('userManagement.edit') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteUser(scope.row)">
              <el-icon><Delete /></el-icon>
              {{ $t('userManagement.delete') }}
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
          <el-input v-model="form.username" :placeholder="$t('userManagement.enterUsername')" />
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
          <el-switch v-model="form.status" active-value="1" inactive-value="0" />
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
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { get, post, put, del } from '@/utils/axios'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  status: ''
})

// 用户列表
const userList = ref([])

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

// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    const userData = await get('/user/list')
    userList.value = userData
    pagination.total = userData.length
  } catch (error) {
    ElMessage.error('获取用户列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const getRoleList = async () => {
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    const roleData = await get('/role/list')
    roleList.value = roleData
  } catch (error) {
    ElMessage.error('获取角色列表失败：' + error.message)
  }
}

// 搜索用户
const handleSearch = () => {
  // 这里可以实现带条件的搜索，暂时先调用getUserList()
  getUserList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.username = ''
  searchForm.status = ''
  getUserList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getUserList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getUserList()
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
  await getRoleList()
  // 打开对话框
  dialogVisible.value = true
}

// 编辑用户
const handleEditUser = async (row) => {
  // 填充表单数据
  Object.assign(form, row)
  // 打开对话框前加载角色列表
  await getRoleList()
  // 打开对话框
  dialogVisible.value = true
}

// 删除用户
const handleDeleteUser = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除用户 ' + row.username + ' 吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // axios拦截器已经处理了code检查，直接返回data字段
    await del('/user/' + row.id)
    ElMessage.success('删除用户成功')
    getUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败：' + error.message)
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    // 后端使用@RequestParam接收status参数，所以通过查询字符串传递
    await put(`/user/${row.id}/status?status=${row.status}`)
  } catch (error) {
    ElMessage.error('更新状态失败：' + error.message)
    // 恢复原来的状态
    getUserList()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (form.id) {
      // 编辑用户
      await put('/user/' + form.id, form)
    } else {
      // 添加用户
      await post('/user', form)
    }
    
    ElMessage.success(form.id ? '编辑用户成功' : '添加用户成功')
    dialogVisible.value = false
    getUserList()
  } catch (error) {
    ElMessage.error((form.id ? '编辑用户失败' : '添加用户失败') + '：' + error.message)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getUserList()
  // 移除了getRoleList()调用，改为在打开对话框时调用
})
</script>

<style scoped>
.user-management {
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
</style>