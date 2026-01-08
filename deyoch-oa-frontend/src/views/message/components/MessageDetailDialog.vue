<template>
  <el-dialog
    v-model="visible"
    title="消息详情"
    width="700px"
    :before-close="handleClose"
  >
    <div v-if="message" class="message-detail">
      <!-- 消息头部 -->
      <div class="message-header">
        <div class="message-meta">
          <el-tag
            :type="getMessageTypeColor(message.type)"
            size="large"
            class="message-type"
          >
            {{ getMessageTypeName(message.type) }}
          </el-tag>
          <el-tag
            v-if="message.priority > 1"
            :type="getPriorityColor(message.priority)"
            size="large"
            class="message-priority"
          >
            {{ getPriorityName(message.priority) }}
          </el-tag>
          <span class="message-time">{{ formatTime(message.createdAt) }}</span>
        </div>
        <div class="message-status">
          <el-tag v-if="message.isRead === 0" type="warning" size="small">
            未读
          </el-tag>
          <el-tag v-else type="success" size="small">
            已读
          </el-tag>
        </div>
      </div>

      <!-- 消息标题 -->
      <div class="message-title">
        <h2>{{ message.title }}</h2>
      </div>

      <!-- 发送者信息 -->
      <div v-if="message.senderName" class="sender-info">
        <div class="sender-label">发送者：</div>
        <div class="sender-detail">
          <el-avatar :size="32" :src="message.senderAvatar">
            {{ message.senderName?.charAt(0) }}
          </el-avatar>
          <span class="sender-name">{{ message.senderName }}</span>
        </div>
      </div>

      <!-- 消息内容 -->
      <div class="message-content">
        <div class="content-label">消息内容：</div>
        <div class="content-text">{{ message.content }}</div>
      </div>

      <!-- 相关信息 -->
      <div v-if="message.relatedTitle" class="related-info">
        <div class="related-label">相关：</div>
        <div class="related-content">
          <el-link type="primary" @click="handleRelatedClick">
            {{ message.relatedTitle }}
          </el-link>
        </div>
      </div>

      <!-- 时间信息 -->
      <div class="time-info">
        <div class="time-item">
          <span class="time-label">发送时间：</span>
          <span class="time-value">{{ formatFullTime(message.createdAt) }}</span>
        </div>
        <div v-if="message.readTime" class="time-item">
          <span class="time-label">阅读时间：</span>
          <span class="time-value">{{ formatFullTime(message.readTime) }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="message && message.isRead === 0"
          type="primary"
          @click="handleMarkRead"
        >
          标记已读
        </el-button>
        <el-button type="danger" @click="handleDelete">
          删除消息
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  message: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'mark-read', 'delete'])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 方法
const handleClose = () => {
  visible.value = false
}

const handleMarkRead = () => {
  if (props.message) {
    const updatedMessage = { ...props.message, isRead: 1, readTime: new Date().toISOString() }
    emit('mark-read', updatedMessage)
    ElMessage.success('已标记为已读')
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '确认删除', {
      type: 'warning'
    })
    
    if (props.message) {
      emit('delete', props.message.id)
      handleClose()
    }
  } catch (error) {
    // 用户取消删除
  }
}

const handleRelatedClick = () => {
  if (props.message?.relatedType && props.message?.relatedId) {
    // 根据关联类型跳转到相应页面
    const { relatedType, relatedId } = props.message
    
    switch (relatedType) {
      case 'task':
        // 跳转到任务详情
        console.log('跳转到任务详情:', relatedId)
        break
      case 'process':
        // 跳转到流程详情
        console.log('跳转到流程详情:', relatedId)
        break
      case 'announcement':
        // 跳转到公告详情
        console.log('跳转到公告详情:', relatedId)
        break
      case 'document':
        // 跳转到文档详情
        console.log('跳转到文档详情:', relatedId)
        break
      default:
        ElMessage.info('暂不支持跳转到该类型的详情页面')
    }
  }
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

const getMessageTypeName = (type) => {
  const names = {
    1: '系统消息',
    2: '审批通知',
    3: '任务通知',
    4: '公告通知'
  }
  return names[type] || '未知类型'
}

const getPriorityColor = (priority) => {
  const colors = {
    1: '',
    2: 'warning',
    3: 'danger'
  }
  return colors[priority] || ''
}

const getPriorityName = (priority) => {
  const names = {
    1: '普通',
    2: '重要',
    3: '紧急'
  }
  return names[priority] || '普通'
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

const formatFullTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.message-detail {
  padding: 10px 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.message-type,
.message-priority {
  font-size: 14px;
}

.message-time {
  color: #909399;
  font-size: 14px;
}

.message-title {
  margin-bottom: 20px;
}

.message-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.sender-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.sender-label {
  font-weight: 500;
  color: #606266;
  margin-right: 12px;
}

.sender-detail {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sender-name {
  font-weight: 500;
  color: #303133;
}

.message-content {
  margin-bottom: 20px;
}

.content-label {
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.content-text {
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.related-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
}

.related-label {
  font-weight: 500;
  color: #606266;
  margin-right: 12px;
}

.time-info {
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.time-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.time-label {
  font-weight: 500;
  color: #909399;
  min-width: 80px;
}

.time-value {
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>