<template>
  <div class="permission-management">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>{{ $t('permissionManagement.title') }}</h2>
      <el-button type="primary" @click="handleAddPermission">
        <el-icon><Plus /></el-icon>
        {{ $t('permissionManagement.addPermission') }}
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('permissionManagement.permName')">
          <el-input v-model="searchForm.permName" :placeholder="$t('permissionManagement.enterPermName')" clearable />
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.permCode')">
          <el-input v-model="searchForm.permCode" :placeholder="$t('permissionManagement.enterPermCode')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('permissionManagement.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('permissionManagement.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 权限列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="permissionList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="permName" :label="$t('permissionManagement.permName')" />
        <el-table-column prop="permCode" :label="$t('permissionManagement.permCode')" />
        <el-table-column prop="parentId" :label="$t('permissionManagement.parentId')" width="120" />
        <el-table-column prop="parentName" :label="$t('permissionManagement.parentName')" />
        <el-table-column prop="url" :label="$t('permissionManagement.url')" />
        <el-table-column prop="sort" :label="$t('permissionManagement.sort')" width="100" />
        <el-table-column prop="status" :label="$t('permissionManagement.status')" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('permissionManagement.createdAt')" width="200" />
        <el-table-column prop="updatedAt" :label="$t('permissionManagement.updatedAt')" width="200" />
        <el-table-column :label="$t('permissionManagement.actions')" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditPermission(scope.row)">
              <el-icon><Edit /></el-icon>
              {{ $t('permissionManagement.edit') }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeletePermission(scope.row)">
              <el-icon><Delete /></el-icon>
              {{ $t('permissionManagement.delete') }}
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
        <el-form-item :label="$t('permissionManagement.parentPermission')" prop="parentId">
          <el-select v-model="form.parentId" :placeholder="$t('permissionManagement.selectParentPermission')">
            <el-option :label="$t('permissionManagement.rootPermission')" value="0" />
            <el-option
              v-for="perm in parentPermissionList"
              :key="perm.id"
              :label="perm.permName"
              :value="perm.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('permissionManagement.url')" prop="url">
          <el-input v-model="form.url" :placeholder="$t('permissionManagement.enterUrl')" />
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
import { get, post, put, del } from '@/utils/axios'

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
const form = reactive({
  id: null,
  permName: '',
  permCode: '',
  parentId: 0,
  url: '',
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

// 获取权限列表
const getPermissionList = async () => {
  loading.value = true
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    const permData = await get('/permission/list')
    permissionList.value = permData
    pagination.total = permData.length
    
    // 过滤出父权限列表（用于添加/编辑时选择父权限）
    parentPermissionList.value = permData.filter(perm => perm.parentId === 0)
  } catch (error) {
    ElMessage.error('获取权限列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取权限树
const getPermissionTree = async () => {
  treeLoading.value = true
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    const treeData = await get('/permission/tree')
    permissionTree.value = treeData
  } catch (error) {
    ElMessage.error('获取权限树失败：' + error.message)
  } finally {
    treeLoading.value = false
  }
}

// 搜索权限
const handleSearch = () => {
  // 这里可以实现带条件的搜索，暂时先调用getPermissionList()
  getPermissionList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.permName = ''
  searchForm.permCode = ''
  getPermissionList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getPermissionList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getPermissionList()
}

// 添加权限
const handleAddPermission = () => {
  // 重置表单
  form.id = null
  form.permName = ''
  form.permCode = ''
  form.parentId = 0
  form.url = ''
  form.sort = 0
  form.status = 1
  // 打开对话框
  dialogVisible.value = true
}

// 编辑权限
const handleEditPermission = (row) => {
  // 填充表单数据
  Object.assign(form, row)
  // 打开对话框
  dialogVisible.value = true
}

// 删除权限
const handleDeletePermission = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除权限 ' + row.permName + ' 吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // axios拦截器已经处理了code检查，直接返回data字段
    await del('/permission/' + row.id)
    ElMessage.success('删除权限成功')
    getPermissionList()
    getPermissionTree()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除权限失败：' + error.message)
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    // axios拦截器已经处理了code检查，直接返回data字段
    await put('/permission/' + row.id, {
      status: row.status
    })
  } catch (error) {
    ElMessage.error('更新状态失败：' + error.message)
    // 恢复原来的状态
    getPermissionList()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (form.id) {
      // 编辑权限
      await put('/permission/' + form.id, form)
    } else {
      // 添加权限
      await post('/permission', form)
    }
    
    ElMessage.success(form.id ? '编辑权限成功' : '添加权限成功')
    dialogVisible.value = false
    getPermissionList()
    getPermissionTree()
  } catch (error) {
    ElMessage.error((form.id ? '编辑权限失败' : '添加权限失败') + '：' + error.message)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getPermissionList()
  getPermissionTree()
})
</script>

<style scoped>
.permission-management {
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