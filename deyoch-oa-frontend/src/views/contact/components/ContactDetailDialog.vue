<template>
  <el-dialog
    v-model="visible"
    title="联系人详情"
    width="600px"
    :before-close="handleClose"
  >
    <div v-if="contact" class="contact-detail">
      <!-- 基本信息 -->
      <div class="detail-section">
        <h4 class="section-title">基本信息</h4>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="avatar-section">
              <el-avatar :size="80" :src="contact.avatar">
                {{ contact.realName?.charAt(0) }}
              </el-avatar>
            </div>
          </el-col>
          <el-col :span="16">
            <div class="info-grid">
              <div class="info-item">
                <label>姓名：</label>
                <span>{{ contact.realName || '-' }}</span>
              </div>
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ contact.username || '-' }}</span>
              </div>
              <div class="info-item">
                <label>工号：</label>
                <span>{{ contact.employeeId || '-' }}</span>
              </div>
              <div class="info-item">
                <label>职位：</label>
                <span>{{ contact.position || '-' }}</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 联系方式 -->
      <div class="detail-section">
        <h4 class="section-title">联系方式</h4>
        <div class="info-grid">
          <div class="info-item">
            <label>电话：</label>
            <span v-if="contact.phone" class="contact-link" @click="handleCall(contact.phone)">
              <el-icon><Phone /></el-icon>
              {{ contact.phone }}
            </span>
            <span v-else>-</span>
          </div>
          <div class="info-item">
            <label>邮箱：</label>
            <span v-if="contact.email" class="contact-link" @click="handleEmail(contact.email)">
              <el-icon><Message /></el-icon>
              {{ contact.email }}
            </span>
            <span v-else>-</span>
          </div>
          <div class="info-item">
            <label>分机：</label>
            <span>{{ contact.extension || '-' }}</span>
          </div>
          <div class="info-item">
            <label>办公地点：</label>
            <span>{{ contact.officeLocation || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 组织信息 -->
      <div class="detail-section">
        <h4 class="section-title">组织信息</h4>
        <div class="info-grid">
          <div class="info-item">
            <label>部门：</label>
            <span>{{ contact.deptName || '-' }}</span>
          </div>
          <div class="info-item">
            <label>角色：</label>
            <span>{{ contact.roleName || '-' }}</span>
          </div>
          <div class="info-item">
            <label>状态：</label>
            <el-tag :type="contact.status === 1 ? 'success' : 'danger'">
              {{ contact.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleSendMessage">
          <el-icon><ChatDotSquare /></el-icon>
          发送消息
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { Phone, Message, ChatDotSquare } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  contact: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'send-message'])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 方法
const handleClose = () => {
  visible.value = false
}

const handleCall = (phone) => {
  window.open(`tel:${phone}`)
}

const handleEmail = (email) => {
  window.open(`mailto:${email}`)
}

const handleSendMessage = () => {
  emit('send-message', props.contact)
  handleClose()
}
</script>

<style scoped>
.contact-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.avatar-section {
  text-align: center;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  min-width: 80px;
  font-weight: 500;
  color: #606266;
}

.info-item span {
  color: #303133;
}

.contact-link {
  color: #409eff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.contact-link:hover {
  text-decoration: underline;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>