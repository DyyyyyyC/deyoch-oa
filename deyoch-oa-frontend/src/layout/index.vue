<template>
  <div class="layout-container">
    <header class="layout-header">
      <div class="header-content">
        <h1 class="logo">{{ $t('dashboard.title') }}</h1>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="40">
                {{ userInfo?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.username || $t('common.anonymous') }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">{{ $t('common.profile') }}</el-dropdown-item>
                <el-dropdown-item command="logout" divided>{{ $t('common.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    <main class="layout-main">
      <router-view />
    </main>
    <footer class="layout-footer">
      <p>{{ $t('common.copyright') }} © 2025 OA系统</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { logout } from '@/api'
import { useI18n } from 'vue-i18n'
import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

const userInfo = ref(null)

onMounted(() => {
  userInfo.value = userStore.userInfo
})

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info(t('common.comingSoon'))
      break
    case 'logout':
      try {
        await logout()
        userStore.logout()
        router.push('/login')
        ElMessage.success(t('login.logoutSuccess'))
      } catch (error) {
        userStore.logout()
        router.push('/login')
        ElMessage.success(t('login.logoutSuccess'))
      }
      break
    default:
      break
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.layout-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  font-size: 20px;
  margin: 0;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.layout-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.layout-footer {
  background-color: #f5f7fa;
  text-align: center;
  padding: 16px;
  color: #909399;
  font-size: 14px;
}
</style>