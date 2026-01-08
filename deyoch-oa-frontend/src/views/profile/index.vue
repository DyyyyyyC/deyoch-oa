<template>
  <div class="profile-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('profile.title') }}</h2>
    </div>

    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-avatar">
            <el-avatar :size="120" class="avatar">
              {{ userInfo?.username?.charAt(0) || 'U' }}
            </el-avatar>
            <h3 class="username">{{ userInfo?.username || $t('common.anonymous') }}</h3>
            <p class="role-name">{{ userInfo?.roleName || $t('profile.noRole') }}</p>
          </div>
          
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-label">{{ $t('profile.joinDate') }}</span>
              <span class="stat-value">{{ formatDate(userInfo?.createdAt) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">{{ $t('profile.lastLogin') }}</span>
              <span class="stat-value">{{ formatDate(userInfo?.updatedAt) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 个人信息编辑 -->
      <el-col :span="16">
        <el-card class="edit-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('profile.personalInfo') }}</span>
              <el-button type="primary" @click="handleEdit" v-if="!isEditing">
                <el-icon><Edit /></el-icon>
                {{ $t('common.edit') }}
              </el-button>
            </div>
          </template>

          <el-form
            :model="form"
            :rules="rules"
            ref="formRef"
            label-width="120px"
            :disabled="!isEditing"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="$t('profile.username')" prop="username">
                  <el-input v-model="form.username" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('profile.nickname')" prop="nickname">
                  <el-input v-model="form.nickname" :placeholder="$t('profile.enterNickname')" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="$t('profile.email')" prop="email">
                  <el-input v-model="form.email" :placeholder="$t('profile.enterEmail')" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('profile.phone')" prop="phone">
                  <el-input v-model="form.phone" :placeholder="$t('profile.enterPhone')" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item :label="$t('profile.role')" prop="roleName">
              <el-input v-model="form.roleName" disabled />
            </el-form-item>

            <el-form-item v-if="isEditing">
              <el-button type="primary" @click="handleSubmit" :loading="loading">
                {{ $t('common.save') }}
              </el-button>
              <el-button @click="handleCancel">
                {{ $t('common.cancel') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card class="password-card" style="margin-top: 20px;">
          <template #header>
            <span>{{ $t('profile.changePassword') }}</span>
          </template>

          <el-form
            :model="passwordForm"
            :rules="passwordRules"
            ref="passwordFormRef"
            label-width="120px"
          >
            <el-form-item :label="$t('profile.currentPassword')" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                :placeholder="$t('profile.enterCurrentPassword')"
                show-password
              />
            </el-form-item>
            <el-form-item :label="$t('profile.newPassword')" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                :placeholder="$t('profile.enterNewPassword')"
                show-password
              />
            </el-form-item>
            <el-form-item :label="$t('profile.confirmPassword')" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                :placeholder="$t('profile.confirmNewPassword')"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handlePasswordSubmit" :loading="passwordLoading">
                {{ $t('profile.updatePassword') }}
              </el-button>
              <el-button @click="handlePasswordReset">
                {{ $t('common.reset') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserProfile, updateUserProfile, changePassword } from '@/api/user'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const passwordLoading = ref(false)
const isEditing = ref(false)
const userInfo = ref(null)
const formRef = ref(null)
const passwordFormRef = ref(null)

// 个人信息表单
const form = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  roleName: ''
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: t('profile.nicknameRequired'), trigger: 'blur' },
    { min: 2, max: 20, message: t('profile.nicknameLength'), trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('profile.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('profile.emailFormat'), trigger: ['blur', 'change'] }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: t('profile.phoneFormat'), trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: t('profile.currentPasswordRequired'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: t('profile.newPasswordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('profile.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('profile.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error(t('profile.passwordMismatch')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const data = await getUserProfile()
    userInfo.value = data
    
    // 填充表单数据
    Object.assign(form, {
      id: data.id,
      username: data.username,
      nickname: data.nickname || '',
      email: data.email || '',
      phone: data.phone || '',
      roleName: data.roleName || ''
    })
  } catch (error) {
    ElMessage.error(t('profile.getUserInfoFailed'))
  }
}

// 开始编辑
const handleEdit = () => {
  isEditing.value = true
}

// 取消编辑
const handleCancel = () => {
  isEditing.value = false
  // 重置表单数据
  if (userInfo.value) {
    Object.assign(form, {
      nickname: userInfo.value.nickname || '',
      email: userInfo.value.email || '',
      phone: userInfo.value.phone || ''
    })
  }
}

// 提交个人信息
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const updateData = {
      nickname: form.nickname,
      email: form.email,
      phone: form.phone
    }
    
    await updateUserProfile(updateData)
    
    ElMessage.success(t('profile.updateSuccess'))
    isEditing.value = false
    
    // 更新用户信息
    await getUserInfo()
    
    // 更新 store 中的用户信息
    userStore.updateUserInfo({
      ...userStore.userInfo,
      ...updateData
    })
  } catch (error) {
    if (error.name !== 'Error') {
      ElMessage.error(t('profile.updateFailed'))
    }
  } finally {
    loading.value = false
  }
}

// 提交密码修改
const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    
    await changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success(t('profile.passwordUpdateSuccess'))
    handlePasswordReset()
  } catch (error) {
    ElMessage.error(t('profile.passwordUpdateFailed'))
  } finally {
    passwordLoading.value = false
  }
}

// 重置密码表单
const handlePasswordReset = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.profile-card {
  text-align: center;
}

.profile-avatar {
  padding: 20px 0;
}

.avatar {
  margin-bottom: 15px;
  background-color: #409eff;
  color: white;
  font-size: 48px;
  font-weight: bold;
}

.username {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 20px;
}

.role-name {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.profile-stats {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-card,
.password-card {
  height: fit-content;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>