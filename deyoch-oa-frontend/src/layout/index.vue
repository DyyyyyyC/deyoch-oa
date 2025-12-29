<template>
  <div class="layout-container">
    <header class="layout-header">
      <div class="header-content">
        <h1 class="logo">OA系统</h1>
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
    <div class="layout-body">
      <!-- 左侧菜单 -->
      <aside class="layout-sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
        <!-- 折叠按钮 -->
        <div class="sidebar-toggle" @click="toggleCollapse">
          <el-icon><component :is="isCollapsed ? Expand : Fold" /></el-icon>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          :collapse="isCollapsed"
          :collapse-transition="false"
          :default-openeds="defaultOpenedMenus"
        >
          <!-- 首页 -->
          <el-menu-item index="/dashboard">
            <template #title>
              <el-icon><House /></el-icon>
              {{ $t('dashboard.title') }}
            </template>
          </el-menu-item>
          
          <!-- 系统管理 -->
          <el-sub-menu index="system" v-if="hasPermission('sys:user:manage') || hasPermission('sys:role:manage') || hasPermission('sys:perm:manage')">
            <template #title>
              <el-icon><Setting /></el-icon>
              {{ $t('systemManagement.title') }}
            </template>
            <el-menu-item index="/system/user" v-if="hasPermission('sys:user:manage')">
              <template #title>{{ $t('userManagement.title') }}</template>
            </el-menu-item>
            <el-menu-item index="/system/role" v-if="hasPermission('sys:role:manage')">
              <template #title>{{ $t('roleManagement.title') }}</template>
            </el-menu-item>
            <el-menu-item index="/system/permission" v-if="hasPermission('sys:perm:manage')">
              <template #title>{{ $t('permissionManagement.title') }}</template>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 公告管理 -->
          <el-menu-item index="/announcement/list" v-if="hasPermission('oa:announcement:manage')">
            <template #title>
              <el-icon><Bell /></el-icon>
              {{ $t('announcementManagement.title') }}
            </template>
          </el-menu-item>
          
          <!-- 任务管理 -->
          <el-menu-item index="/task/list" v-if="hasPermission('oa:task:manage')">
            <template #title>
              <el-icon><List /></el-icon>
              {{ $t('taskManagement.title') }}
            </template>
          </el-menu-item>
        </el-menu>
      </aside>
      
      <!-- 主内容区域 -->
      <main class="layout-main">
        <router-view />
      </main>
    </div>
    <footer class="layout-footer">
      <p>{{ $t('common.copyright') }} © 2025 OA系统</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { ArrowDown, House, Setting, Bell, List, Expand, Fold } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t } = useI18n()

const userInfo = ref(null)

// 菜单折叠状态
const isCollapsed = ref(false)

// 活动菜单，根据当前路由自动设置
const activeMenu = computed(() => {
  return route.path
})

// 默认展开的菜单
const defaultOpenedMenus = ref(['system'])

// 切换菜单折叠状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 权限检查函数
const hasPermission = (permission) => {
  const hasPerm = userStore.permissions.includes(permission)
  return hasPerm
}

// 监听用户权限变化，更新菜单
watch(
  () => userStore.permissions,
  () => {
    // 权限变化时的处理
  },
  { deep: true }
)

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
/* CSS变量定义 */
:root {
  --primary-color: #409eff;
  --secondary-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
  --bg-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --card-bg: rgba(255, 255, 255, 0.95);
  --text-primary: #303133;
  --text-secondary: #606266;
  --border-color: #e4e7ed;
  --shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  --shadow-hover: 0 10px 30px rgba(0, 0, 0, 0.15);
  --transition: all 0.3s ease;
}

/* 全局容器样式 */
.layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  animation: fadeIn 0.5s ease-in;
}

/* 头部样式 */
.layout-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all 0.3s ease;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  height: 72px;
  width: 100%;
  max-width: none;
  margin: 0;
  backdrop-filter: blur(10px);
}

.logo {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: white;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 24px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: white;
}

/* 主内容布局 */
.layout-body {
  display: flex;
  flex: 1;
  width: 100%;
  padding: 24px;
  box-sizing: border-box;
  gap: 24px;
  transition: all 0.3s ease;
  overflow: hidden;
}

/* 左侧菜单样式 */
.layout-sidebar {
  width: 260px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: var(--shadow-light);
  border-radius: 16px;
  overflow: hidden;
  flex-shrink: 0;
  transition: width 0.3s ease, transform 0.3s ease;
  position: relative;
  backdrop-filter: blur(10px);
  animation: slideInLeft 0.6s ease-out;
}

/* 折叠状态样式 */
.layout-sidebar.sidebar-collapsed {
  width: 80px;
}

/* 折叠按钮样式 */
.sidebar-toggle {
  position: absolute;
  right: -16px;
  top: 20px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  z-index: 10;
  transition: all 0.3s ease;
  animation: pulse 2s infinite;
}

.sidebar-toggle:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
  animation: none;
}

/* 菜单样式 */
.sidebar-menu {
  border-right: none;
  height: 100%;
  background: transparent;
}

/* 主内容区域样式 */
.layout-main {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: var(--shadow-light);
  border-radius: 16px;
  overflow: auto;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  animation: slideInRight 0.6s ease-out 0.1s both;
  padding: 24px;
  box-sizing: border-box;
}

/* 底部样式 */
.layout-footer {
  background: rgba(255, 255, 255, 0.9);
  text-align: center;
  padding: 20px;
  color: var(--text-secondary);
  font-size: 14px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

/* 动画定义 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .logo {
    font-size: 20px;
  }
  
  .layout-body {
    flex-direction: column;
    padding: 16px;
    gap: 16px;
  }
  
  .layout-sidebar {
    width: 100%;
    margin-bottom: 16px;
  }
  
  .layout-sidebar.sidebar-collapsed {
    width: 100%;
  }
  
  .layout-main {
    padding: 20px;
  }
  
  .sidebar-toggle {
    right: 16px;
    top: 16px;
  }
}
</style>