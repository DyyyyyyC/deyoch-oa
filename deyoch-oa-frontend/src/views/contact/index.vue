<template>
  <div class="contact-container">
    <!-- 页面标题 -->
    <PageHeader title="通讯录">
      <template #extra>
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button type="success" @click="handleImport">
          <el-icon><Upload /></el-icon>
          导入
        </el-button>
      </template>
    </PageHeader>

    <el-card class="box-card">
      <div class="contact-content">
        <!-- 左侧组织架构树 -->
        <div class="org-tree-panel">
          <el-card shadow="never">
            <template #header>
              <span>组织架构</span>
            </template>
            <el-tree
              ref="orgTreeRef"
              :data="orgTreeData"
              :props="treeProps"
              node-key="id"
              :expand-on-click-node="false"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <span class="tree-node">
                  <el-icon v-if="data.type === 'dept'">
                    <OfficeBuilding />
                  </el-icon>
                  <el-icon v-else>
                    <User />
                  </el-icon>
                  <span class="node-label">{{ node.label }}</span>
                  <span v-if="data.type === 'user' && data.position" class="node-position">
                    ({{ data.position }})
                  </span>
                </span>
              </template>
            </el-tree>
          </el-card>
        </div>

        <!-- 右侧联系人列表 -->
        <div class="contact-list-panel">
          <el-card shadow="never">
            <template #header>
              <span>联系人列表</span>
            </template>

            <!-- 搜索区域 -->
            <PageActionBar>
              <template #search>
                <el-input
                  v-model="searchForm.keyword"
                  placeholder="搜索姓名、电话、邮箱"
                  style="width: 300px"
                  clearable
                  @keyup.enter="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
              </template>
            </PageActionBar>

            <!-- 联系人表格 -->
            <el-table
              v-loading="loading"
              :data="contactList"
              style="width: 100%"
              @row-click="handleRowClick"
            >
              <el-table-column prop="realName" label="姓名" width="120">
                <template #default="{ row }">
                  <div class="user-info">
                    <el-avatar :size="32" :src="row.avatar">
                      {{ row.realName?.charAt(0) }}
                    </el-avatar>
                    <span class="user-name">{{ row.realName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="employeeId" label="工号" width="100" />
              <el-table-column prop="position" label="职位" width="150" />
              <el-table-column prop="deptName" label="部门" width="150" />
              <el-table-column prop="phone" label="电话" width="130">
                <template #default="{ row }">
                  <span v-if="row.phone" class="phone-link" @click.stop="handleCall(row.phone)">
                    {{ row.phone }}
                  </span>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>
              <el-table-column prop="email" label="邮箱" min-width="180">
                <template #default="{ row }">
                  <span v-if="row.email" class="email-link" @click.stop="handleEmail(row.email)">
                    {{ row.email }}
                  </span>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>
              <el-table-column prop="officeLocation" label="办公地点" width="150" />
              <el-table-column prop="extension" label="分机" width="80" />
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click.stop="handleViewDetail(row)">
                    详情
                  </el-button>
                  <el-button type="primary" link @click.stop="handleSendMessage(row)">
                    发消息
                  </el-button>
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
        </div>
      </div>
    </el-card>

    <!-- 联系人详情对话框 -->
    <ContactDetailDialog
      v-model="detailDialogVisible"
      :contact="selectedContact"
    />

    <!-- 发送消息对话框 -->
    <SendMessageDialog
      v-model="messageDialogVisible"
      :receiver="selectedContact"
      @success="handleMessageSent"
    />

    <!-- 导入文件对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入通讯录" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx"
        drag
        @change="handleFileChange"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmImport">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download,
  Upload,
  UploadFilled,
  OfficeBuilding,
  User,
  Search
} from '@element-plus/icons-vue'
import {
  getContactDirectory,
  getOrganizationTree,
  searchContacts,
  exportContacts,
  importContacts
} from '@/api/contact'
import ContactDetailDialog from './components/ContactDetailDialog.vue'
import SendMessageDialog from './components/SendMessageDialog.vue'
import PageHeader from '@/components/PageHeader.vue'
import PageActionBar from '@/components/PageActionBar.vue'

// 响应式数据
const loading = ref(false)
const orgTreeData = ref([])
const contactList = ref([])
const selectedContact = ref(null)
const detailDialogVisible = ref(false)
const messageDialogVisible = ref(false)
const importDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  deptId: null
})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 组件引用
const orgTreeRef = ref()
const uploadRef = ref()

// 页面初始化
onMounted(() => {
  loadOrganizationTree()
  loadContactList()
})

// 加载组织架构树
const loadOrganizationTree = async () => {
  try {
    const response = await getOrganizationTree()
    if (response.success) {
      orgTreeData.value = response.data
    }
  } catch (error) {
    console.error('加载组织架构失败:', error)
    ElMessage.error('加载组织架构失败')
  }
}

// 加载联系人列表
const loadContactList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword,
      deptId: searchForm.deptId
    }
    
    const response = await getContactDirectory(params)
    if (response.success) {
      contactList.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('加载联系人列表失败:', error)
    ElMessage.error('加载联系人列表失败')
  } finally {
    loading.value = false
  }
}

// 树节点点击事件
const handleNodeClick = (data) => {
  if (data.type === 'dept') {
    searchForm.deptId = data.id
    searchForm.keyword = ''
    pagination.current = 1
    loadContactList()
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  searchForm.deptId = null
  loadContactList()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.deptId = null
  pagination.current = 1
  orgTreeRef.value?.setCurrentKey(null)
  loadContactList()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadContactList()
}

// 当前页改变
const handleCurrentChange = (page) => {
  pagination.current = page
  loadContactList()
}

// 行点击事件
const handleRowClick = (row) => {
  selectedContact.value = row
  detailDialogVisible.value = true
}

// 查看详情
const handleViewDetail = (row) => {
  selectedContact.value = row
  detailDialogVisible.value = true
}

// 发送消息
const handleSendMessage = (row) => {
  selectedContact.value = row
  messageDialogVisible.value = true
}

// 消息发送成功回调
const handleMessageSent = () => {
  ElMessage.success('消息发送成功')
}

// 拨打电话
const handleCall = (phone) => {
  window.open(`tel:${phone}`)
}

// 发送邮件
const handleEmail = (email) => {
  window.open(`mailto:${email}`)
}

// 导出通讯录
const handleExport = async () => {
  try {
    const response = await exportContacts()
    
    // 创建下载链接
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `通讯录_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 导入通讯录
const handleImport = () => {
  importDialogVisible.value = true
}

// 文件选择改变
const handleFileChange = (file) => {
  console.log('选择文件:', file.name)
}

// 确认导入
const handleConfirmImport = async () => {
  const files = uploadRef.value?.uploadFiles
  if (!files || files.length === 0) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  const file = files[0].raw
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await importContacts(formData)
    if (response.success) {
      ElMessage.success('导入成功')
      importDialogVisible.value = false
      uploadRef.value?.clearFiles()
      loadContactList()
    } else {
      ElMessage.error(response.message || '导入失败')
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  }
}
</script>

<style scoped>
.contact-container {
  padding: 20px;
}

.contact-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 200px);
}

.org-tree-panel {
  width: 300px;
  flex-shrink: 0;
}

.contact-list-panel {
  flex: 1;
  min-width: 0;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 5px;
}

.node-label {
  font-weight: 500;
}

.node-position {
  color: #909399;
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-weight: 500;
}

.phone-link,
.email-link {
  color: #409eff;
  cursor: pointer;
}

.phone-link:hover,
.email-link:hover {
  text-decoration: underline;
}

.text-muted {
  color: #c0c4cc;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>