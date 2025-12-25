<template>
  <div class="login-container">
    <div class="login-form-wrapper">
      <h2 class="login-title">{{ $t('login.title') }}</h2>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top">
        <el-form-item :label="$t('login.username')" prop="username">
          <el-input 
            v-model="loginForm.username" 
            :placeholder="$t('login.pleaseInputUsername')" 
            :prefix-icon="User"
          >
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('login.password')" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            :placeholder="$t('login.pleaseInputPassword')" 
            show-password 
            :prefix-icon="Lock"
          >
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn" 
            @click="handleLogin" 
            :loading="loading"
          >
            {{ $t('login.loginBtn') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const rules = {
  username: [
    { required: true, message: t('login.pleaseInputUsername'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('login.pleaseInputPassword'), trigger: 'blur' },
    { min: 6, message: t('login.passwordLength'), trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    // 调用登录 API
    const res = await login(loginForm)
    
    // 保存登录状态
    userStore.login(res.token, res.userInfo)
    
    ElMessage.success(t('login.loginSuccess'))
    
    // 跳转到首页
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || t('login.loginFailed'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.login-form-wrapper {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
}
</style>