<template>
  <el-dialog
    v-model="visible"
    title="发送消息"
    width="600px"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="messageForm"
      :rules="formRules"
      label-width="80px"
    >
      <!-- 接收者信息 -->
      <div class="receiver-info">
        <el-avatar :size="40" :src="receiver?.avatar">
          {{ receiver?.realName?.charAt(0) }}
        </el-avatar>
        <div class="receiver-detail">
          <div class="receiver-name">{{ receiver?.realName }}</div>
          <div class="receiver-dept">{{ receiver?.deptName }} - {{ receiver?.position }}</div>
        </div>
      </div>

      <el-form-item label="消息类型" prop="type">
        <el-select v-model="messageForm.type" placeholder="请选择消息类型" style="width: 100%">
          <el-option label="系统消息" :value="1" />
          <el-option label="工作通知" :value="3" />
          <el-option label="一般消息" :value="1" />
        </el-select>
      </el-form-item>

      <el-form-item label="优先级" prop="priority">
        <el-radio-group v-model="messageForm.priority">
          <el-radio :label="1">普通</el-radio>
          <el-radio :label="2">重要</el-radio>
          <el-radio :label="3">紧急</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="消息标题" prop="title">
        <el-input
          v-model="messageForm.title"
          placeholder="请输入消息标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="消息内容" prop="content">
        <el-input
          v-model="messageForm.content"
          type="textarea"
          :rows="6"
          placeholder="请输入消息内容"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="sending" @click="handleSend">
          发送
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { sendMessage } from '@/api/message'
import { useUserStore } from '@/stores/user'

// Store
const userStore = useUserStore()

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  receiver: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const formRef = ref()
const sending = ref(false)

// 表单数据
const messageForm = reactive({
  type: 1,
  priority: 1,
  title: '',
  content: '',
  receiverId: null,
  senderId: null
})

// 表单验证规则
const formRules = {
  type: [
    { required: true, message: '请选择消息类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入消息标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入消息内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '内容长度在 1 到 1000 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 监听接收者变化
watch(() => props.receiver, (newReceiver) => {
  if (newReceiver) {
    messageForm.receiverId = newReceiver.id
    messageForm.senderId = userStore.userInfo?.id
  }
}, { immediate: true })

// 监听对话框显示状态
watch(visible, (newVisible) => {
  if (newVisible) {
    resetForm()
  }
})

// 方法
const handleClose = () => {
  visible.value = false
}

const resetForm = () => {
  messageForm.type = 1
  messageForm.priority = 1
  messageForm.title = ''
  messageForm.content = ''
  
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleSend = async () => {
  if (!formRef.value) return
  
  try {
    // 表单验证
    await formRef.value.validate()
    
    if (!messageForm.receiverId) {
      ElMessage.error('接收者信息异常')
      return
    }
    
    sending.value = true
    
    // 发送消息
    const response = await sendMessage({
      title: messageForm.title,
      content: messageForm.content,
      type: messageForm.type,
      priority: messageForm.priority,
      receiverId: messageForm.receiverId,
      senderId: messageForm.senderId
    })
    
    if (response.success) {
      ElMessage.success('消息发送成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.message || '消息发送失败')
    }
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('消息发送失败')
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.receiver-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.receiver-detail {
  flex: 1;
}

.receiver-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.receiver-dept {
  font-size: 14px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>