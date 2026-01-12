<template>
  <div class="management-page">
    <!-- 页面标题 -->
    <PageHeader title="消息中心" />

    <!-- 消息列表 -->
    <el-card class="table-card">
      <!-- 操作区域 -->
      <PageActionBar>
        <!-- 左侧操作按钮 -->
        <template #actions>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge-item">
            <el-button type="primary" @click="handleMarkAllRead">
              <el-icon><Check /></el-icon>
              全部已读
            </el-button>
          </el-badge>
          <el-button type="danger" @click="handleClearAll">
            <el-icon><Delete /></el-icon>
            清空消息
          </el-button>
        </template>
      </PageActionBar>

      <!-- 消息筛选 -->
      <div class="filter-bar">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部消息" name="all">
            <template #label>
              <span>全部消息</span>
              <el-badge v-if="totalCount > 0" :value="totalCount" class="tab-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="未读消息" name="unread">
            <template #label>
              <span>未读消息</span>
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="tab-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="系统消息" name="system">
            <template #label>
              <span>系统消息</span>
              <el-badge v-if="typeCount[1] > 0" :value="typeCount[1]" class="tab-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="审批通知" name="approval">
            <template #label>
              <span>审批通知</span>
              <el-badge v-if="typeCount[2] > 0" :value="typeCount[2]" class="tab-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="任务通知" name="task">
            <template #label>
              <span>任务通知</span>
              <el-badge v-if="typeCount[3] > 0" :value="typeCount[3]" class="tab-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="公告通知" name="announcement">
            <template #label>
              <span>公告通知</span>
              <el-badge v-if="typeCount[4] > 0" :value="typeCount[4]" class="tab-badge" />
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 消息列表 -->
      <div class="message-list">
        <el-empty v-if="messageList.length === 0 && !loading" description="暂无消息" />
        
        <div v-else class="message-items">
          <div
            v-for="message in messageList"
            :key="message.id"
            class="message-item"
            :class="{ 'unread': message.isRead === 0 }"
            @click="handleMessageClick(message)"
          >
            <!-- 消息头部 -->
            <div class="message-header">
              <div class="message-meta">
                <el-tag
                  :type="getMessageTypeColor(message.type)"
                  size="small"
                  class="message-type"
                >
                  {{ message.typeName }}
                </el-tag>
                <el-tag
                  v-if="message.priority > 1"
                  :type="getPriorityColor(message.priority)"
                  size="small"
                  class="message-priority"
                >
                  {{ message.priorityName }}
                </el-tag>
                <span class="message-time">{{ formatTime(message.createdAt) }}</span>
              </div>
              <div class="message-actions">
                <el-button
                  v-if="message.isRead === 0"
                  type="primary"
                  link
                  @click.stop="handleMarkRead(message)"
                >
                  标记已读
                </el-button>
                <el-button
                  type="danger"
                  link
                  @click.stop="handleDeleteMessage(message)"
                >
                  删除
                </el-button>
              </div>
            </div>

            <!-- 消息内容 -->
            <div class="message-content">
              <h4 class="message-title">{{ message.title }}</h4>
              <p class="message-text">{{ message.content }}</p>
              <div v-if="message.senderName" class="message-sender">
                来自：{{ message.senderName }}
              </div>
              <div v-if="message.relatedTitle" class="message-related">
                相关：{{ message.relatedTitle }}
              </div>
            </div>

            <!-- 未读标识 -->
            <div v-if="message.isRead === 0" class="unread-indicator"></div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="messageList.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 消息详情对话框 -->
    <MessageDetailDialog
      v-model="detailDialogVisible"
      :message="selectedMessage"
      @mark-read="handleMessageRead"
      @delete="handleMessageDeleted"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getUserMessages,
  markMessageAsRead,
  markMessagesAsRead,
  deleteMessage,
  clearAllMessages,
  getUnreadMessageCount,
  getUnreadMessageCountByType
} from '@/api/message'
import MessageDetailDialog from './components/MessageDetailDialog.vue'
import { initWebSocket, closeWebSocket } from '@/utils/websocket'
import PageHeader from '@/components/PageHeader.vue'
import PageActionBar from '@/components/PageActionBar.vue'
import '@/style/management-layout.css'

/**
 * 消息中心页面
 * 
 * 为什么需要用户ID？
 * 1. 获取当前用户的消息列表 - 每个用户只能看到发给自己的消息
 * 2. 标记消息为已读 - 需要知道是哪个用户读了消息
 * 3. 删除消息 - 只能删除自己的消息
 * 4. 获取未读消息数量 - 统计当前用户的未读消息
 * 5. WebSocket连接 - 建立用户专属的实时消息推送连接
 * 6. 清空消息 - 只清空当前用户的消息
 */

// Store
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const messageList = ref([])
const selectedMessage = ref(null)
const detailDialogVisible = ref(false)
const activeTab = ref('all')
const unreadCount = ref(0)
const totalCount = ref(0)
const typeCount = ref({})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 计算属性
const currentUserId = computed(() => {
  // 根据用户反馈，用户信息中包含 userId 字段，不是 id 字段
  const userId = userStore.userInfo?.userId
  return userId
})

// 标签页配置
const tabConfig = {
  all: { type: null, isRead: null },
  unread: { type: null, isRead: 0 },
  system: { type: 1, isRead: null },
  approval: { type: 2, isRead: null },
  task: { type: 3, isRead: null },
  announcement: { type: 4, isRead: null }
}

// 页面初始化
onMounted(() => {
  // 如果用户ID存在，直接初始化
  if (currentUserId.value) {
    initializeMessageCenter()
  } else {
    // 如果用户ID不存在，尝试从store重新获取用户信息
    userStore.getUserInfo()
  }
})

// 监听用户ID变化
watch(currentUserId, (newUserId) => {
  if (newUserId) {
    initializeMessageCenter()
  }
})

// 初始化消息中心
const initializeMessageCenter = () => {
  loadMessageList()
  loadUnreadCount()
  loadUnreadCountByType()
  initWebSocketConnection()
}

// 页面销毁
onUnmounted(() => {
  closeWebSocket()
})

// 初始化WebSocket连接
const initWebSocketConnection = () => {
  if (currentUserId.value) {
    initWebSocket(currentUserId.value, {
      onMessage: handleWebSocketMessage
    })
  }
}

// WebSocket消息处理
const handleWebSocketMessage = (message) => {
  // 更新未读数量
  loadUnreadCount()
  loadUnreadCountByType()
  
  // 如果当前显示的是相关类型的消息，刷新列表
  const config = tabConfig[activeTab.value]
  if (config.type === null || config.type === message.type) {
    if (config.isRead === null || config.isRead === 0) {
      loadMessageList()
    }
  }
  
  // 显示消息提醒
  ElMessage({
    title: '新消息',
    message: message.title,
    type: 'info',
    duration: 3000
  })
}

// 加载消息列表
const loadMessageList = async () => {
  if (!currentUserId.value) {
    return
  }
  
  loading.value = true
  try {
    const config = tabConfig[activeTab.value]
    const params = {
      userId: currentUserId.value,
      page: pagination.current,
      size: pagination.size,
      type: config.type,
      isRead: config.isRead
    }
    
    const response = await getUserMessages(params)
    
    // 处理不同的响应格式
    if (response && typeof response === 'object') {
      if (response.success !== undefined) {
        // 格式1: {success: true, data: {records: [], total: 0}}
        if (response.success && response.data) {
          messageList.value = response.data.records || response.data || []
          pagination.total = response.data.total || 0
          totalCount.value = response.data.total || 0
        } else {
          console.error('获取消息列表失败:', response.message)
          ElMessage.error('获取消息列表失败: ' + (response.message || '未知错误'))
        }
      } else if (response.records && Array.isArray(response.records)) {
        // 格式2: {records: [], total: 0, current: 1, size: 20}
        messageList.value = response.records
        pagination.total = response.total || 0
        totalCount.value = response.total || 0
      } else if (Array.isArray(response)) {
        // 格式3: 直接返回数组
        messageList.value = response
        pagination.total = response.length
        totalCount.value = response.length
      } else {
        console.error('未知的响应格式:', response)
        ElMessage.error('数据格式错误')
        messageList.value = []
        pagination.total = 0
        totalCount.value = 0
      }
    } else {
      console.error('获取消息列表失败: 响应数据无效')
      ElMessage.error('获取消息列表失败: 响应数据无效')
      messageList.value = []
      pagination.total = 0
      totalCount.value = 0
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
    ElMessage.error('加载消息列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 加载未读消息数量
const loadUnreadCount = async () => {
  if (!currentUserId.value) return
  
  try {
    const response = await getUnreadMessageCount(currentUserId.value)
    if (response.success) {
      unreadCount.value = response.data
    }
  } catch (error) {
    console.error('加载未读消息数量失败:', error)
  }
}

// 加载各类型未读消息数量
const loadUnreadCountByType = async () => {
  if (!currentUserId.value) return
  
  try {
    const response = await getUnreadMessageCountByType(currentUserId.value)
    if (response.success) {
      typeCount.value = response.data
    }
  } catch (error) {
    console.error('加载各类型未读消息数量失败:', error)
  }
}

// 标签页切换
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  pagination.current = 1
  loadMessageList()
}

// 消息点击
const handleMessageClick = (message) => {
  selectedMessage.value = message
  detailDialogVisible.value = true
  
  // 如果是未读消息，自动标记为已读
  if (message.isRead === 0) {
    handleMarkRead(message, false)
  }
}

// 标记消息已读
const handleMarkRead = async (message, showMessage = true) => {
  try {
    const response = await markMessageAsRead(message.id, currentUserId.value)
    if (response.success) {
      message.isRead = 1
      message.readTime = new Date().toISOString()
      
      if (showMessage) {
        ElMessage.success('已标记为已读')
      }
      
      // 更新未读数量
      loadUnreadCount()
      loadUnreadCountByType()
    }
  } catch (error) {
    console.error('标记消息已读失败:', error)
    ElMessage.error('标记消息已读失败')
  }
}

// 全部标记已读
const handleMarkAllRead = async () => {
  if (unreadCount.value === 0) {
    ElMessage.info('没有未读消息')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要将所有未读消息标记为已读吗？', '确认操作', {
      type: 'warning'
    })
    
    const unreadMessages = messageList.value.filter(msg => msg.isRead === 0)
    if (unreadMessages.length === 0) {
      ElMessage.info('当前页面没有未读消息')
      return
    }
    
    const messageIds = unreadMessages.map(msg => msg.id)
    const response = await markMessagesAsRead(messageIds, currentUserId.value)
    
    if (response.success) {
      // 更新本地数据
      unreadMessages.forEach(msg => {
        msg.isRead = 1
        msg.readTime = new Date().toISOString()
      })
      
      ElMessage.success('已全部标记为已读')
      loadUnreadCount()
      loadUnreadCountByType()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量标记已读失败:', error)
      ElMessage.error('批量标记已读失败')
    }
  }
}

// 删除消息
const handleDeleteMessage = async (message) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '确认删除', {
      type: 'warning'
    })
    
    const response = await deleteMessage(message.id, currentUserId.value)
    if (response.success) {
      ElMessage.success('删除成功')
      loadMessageList()
      loadUnreadCount()
      loadUnreadCountByType()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除消息失败')
    }
  }
}

// 清空所有消息
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有消息吗？此操作不可恢复！', '确认清空', {
      type: 'warning'
    })
    
    const response = await clearAllMessages(currentUserId.value)
    if (response.success) {
      ElMessage.success('清空成功')
      messageList.value = []
      pagination.total = 0
      unreadCount.value = 0
      typeCount.value = {}
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空消息失败:', error)
      ElMessage.error('清空消息失败')
    }
  }
}

// 消息已读回调
const handleMessageRead = (message) => {
  const index = messageList.value.findIndex(msg => msg.id === message.id)
  if (index !== -1) {
    messageList.value[index] = { ...message }
  }
  loadUnreadCount()
  loadUnreadCountByType()
}

// 消息删除回调
const handleMessageDeleted = (messageId) => {
  const index = messageList.value.findIndex(msg => msg.id === messageId)
  if (index !== -1) {
    messageList.value.splice(index, 1)
    pagination.total--
  }
  loadUnreadCount()
  loadUnreadCountByType()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadMessageList()
}

const handleCurrentChange = (page) => {
  pagination.current = page
  loadMessageList()
}

// 工具方法
const getMessageTypeColor = (type) => {
  const colors = {
    1: '',
    2: 'warning',
    3: 'success',
    4: 'info'
  }
  return colors[type] || ''
}

const getPriorityColor = (priority) => {
  const colors = {
    1: '',
    2: 'warning',
    3: 'danger'
  }
  return colors[priority] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 1天内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}
</script>

<style scoped>
.management-page {
  /* 使用统一的管理页面样式 */
}

.badge-item {
  margin-right: 10px;
}

.filter-bar {
  margin-bottom: 20px;
}

.tab-badge {
  margin-left: 5px;
}

.message-list {
  min-height: 400px;
}

.message-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  position: relative;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.message-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.message-item.unread {
  background: #f0f9ff;
  border-color: #409eff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-type,
.message-priority {
  font-size: 12px;
}

.message-time {
  color: #909399;
  font-size: 12px;
}

.message-actions {
  display: flex;
  gap: 8px;
}

.message-content {
  margin-bottom: 8px;
}

.message-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.message-text {
  margin: 0 0 8px 0;
  color: #606266;
  line-height: 1.5;
}

.message-sender,
.message-related {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.unread-indicator {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>