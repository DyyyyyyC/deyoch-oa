<template>
  <div class="layout-container">
    <header class="layout-header">
      <div class="header-left">
        <div class="logo-container">
          <el-icon class="logo-icon" :size="32"><Platform /></el-icon>
          <h1 class="logo-text">Deyoch OA</h1>
        </div>
      </div>
      <div class="header-right">
        <div class="header-actions">
          <el-tooltip :content="$t('common.search')" placement="bottom">
            <el-icon class="action-icon"><Search /></el-icon>
          </el-tooltip>
          <el-tooltip :content="$t('announcementManagement.title')" placement="bottom">
            <el-badge :value="5" class="notice-badge">
              <el-icon class="action-icon"><Bell /></el-icon>
            </el-badge>
          </el-tooltip>
          <language-switch class="lang-switch" />
        </div>
        <el-dropdown @command="handleCommand" trigger="click">
          <div class="user-profile">
            <el-avatar :size="32" class="user-avatar">
              {{ userInfo?.username?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="user-name">{{ userInfo?.username || $t('common.anonymous') }}</span>
            <el-icon><CaretBottom /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>{{ $t('common.profile') }}
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>{{ $t('common.logout') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <div class="layout-body">
      <!-- 左侧菜单 -->
      <aside class="layout-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu-vertical"
          router
          :collapse="isCollapsed"
          :collapse-transition="true"
          :default-openeds="defaultOpenedMenus"
          background-color="#ffffff"
          text-color="#595959"
          active-text-color="#1890ff"
        >
          <!-- 首页 -->
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <template #title>{{ $t('dashboard.title') }}</template>
          </el-menu-item>
          
          <!-- 系统管理 -->
          <el-sub-menu index="system" v-if="hasPermission('sys:user:manage') || hasPermission('sys:role:manage') || hasPermission('sys:perm:manage')">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>{{ $t('systemManagement.title') }}</span>
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
            <el-icon><Bell /></el-icon>
            <template #title>{{ $t('announcementManagement.title') }}</template>
          </el-menu-item>
          
          <!-- 任务管理 -->
          <el-menu-item index="/task/list" v-if="hasPermission('oa:task:manage')">
            <el-icon><List /></el-icon>
            <template #title>{{ $t('taskManagement.title') }}</template>
          </el-menu-item>
          
          <!-- 日程管理 -->
          <el-menu-item index="/schedule/list" v-if="hasPermission('oa:schedule:manage')">
            <el-icon><Calendar /></el-icon>
            <template #title>{{ $t('scheduleManagement.title') }}</template>
          </el-menu-item>
          
          <!-- 流程管理 -->
          <el-sub-menu index="process" v-if="hasPermission('oa:process:manage')">
            <template #title>
              <el-icon><DocumentCopy /></el-icon>
              <span>{{ $t('processManagement.title') }}</span>
            </template>
            <el-menu-item index="/process/definition" v-if="hasPermission('oa:process:manage')">
              <template #title>{{ $t('processManagement.processDefinition') }}</template>
            </el-menu-item>
            <el-menu-item index="/process/instance" v-if="hasPermission('oa:process:manage')">
              <template #title>{{ $t('processManagement.processInstance') }}</template>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 文档管理 -->
          <el-menu-item index="/document/list" v-if="hasPermission('oa:document:manage')">
            <el-icon><Document /></el-icon>
            <template #title>{{ $t('documentManagement.title') }}</template>
          </el-menu-item>
        </el-menu>
        <!-- 折叠按钮放在右下方 -->
        <div class="sidebar-collapse-trigger" @click="toggleCollapse">
          <el-icon :size="16"><component :is="isCollapsed ? Expand : Fold" /></el-icon>
        </div>
      </aside>
      
      <!-- 主内容区域 -->
      <main class="layout-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import LanguageSwitch from '@/components/LanguageSwitch.vue'
import {
  ArrowDown, House, Setting, Bell, List, Expand, Fold,
  Platform, Search, User, SwitchButton, CaretBottom, DocumentCopy, Calendar, Document
} from '@element-plus/icons-vue'

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
.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background-color: #f0f2f5;
}

/* 头部样式 */
.layout-header {
  height: 64px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  color: #1890ff;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #001529;
  margin: 0;
  white-space: nowrap;
}


.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #595959;
}

.action-icon {
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
}

.action-icon:hover {
  color: #1890ff;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-profile:hover {
  background: #f5f5f5;
}

.user-name {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

/* 主体布局 */
.layout-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 侧边栏 */
.layout-sidebar {
  background: #fff;
  box-shadow: 2px 0 8px rgba(0,21,41,.05);
  z-index: 90;
  position: relative;
  display: flex;
  flex-direction: column;
}

.sidebar-menu-vertical:not(.el-menu--collapse) {
  width: 240px;
}

.sidebar-menu-vertical {
  border-right: none;
  flex: 1;
}

.sidebar-menu-vertical :deep(.el-menu-item), .sidebar-menu-vertical :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-menu-vertical :deep(.el-menu-item.is-active) {
  background-color: #e6f7ff;
  border-right: 3px solid #1890ff;
}

/* 折叠触发器 */
.sidebar-collapse-trigger {
  position: absolute;
  bottom: 16px;
  right: -12px;
  width: 24px;
  height: 24px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #8c8c8c;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 100;
}

.sidebar-collapse-trigger:hover {
  color: #1890ff;
  transform: scale(1.1);
}

/* 主内容 */
.layout-main {
  flex: 1;
  padding: 0;
  overflow-y: auto;
  background-color: #f0f2f5;
}

/* 响应式 */
@media (max-width: 768px) {
  .logo-text {
    display: none;
  }
  .user-name {
    display: none;
  }
}
</style>