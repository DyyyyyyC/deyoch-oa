# 前端项目配置指南

## 1. 配置 Vite 项目

### 1.1 当前 Vite 配置

当前 `vite.config.js` 已包含以下配置：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src') // 设置@为src目录别名
    }
  },
  server: {
    port: 3000, // 开发服务器端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端API地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: 'dist', // 构建输出目录
    sourcemap: false, // 不生成sourcemap
    minify: 'terser', // 使用terser压缩
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus'],
          'axios': ['axios']
        }
      }
    }
  }
})
```

### 1.2 配置说明

- **别名配置**: `@` 指向 `src` 目录，方便导入文件
- **服务器配置**: 
  - 端口：3000
  - 代理：将 `/api` 请求代理到 `http://localhost:8080`
- **构建配置**:
  - 输出目录：`dist`
  - 不生成 sourcemap
  - 使用 terser 压缩
  - 代码分割：将依赖库分为三个chunk

### 1.3 使用说明

Vite 配置已完成，无需额外修改。如果需要调整端口或代理地址，可直接修改 `vite.config.js` 文件。

## 2. 配置 Vue Router

### 2.1 创建路由配置文件

1. 创建 `src/router` 目录
2. 创建 `src/router/index.js` 文件

```javascript
import { createRouter, createWebHistory } from 'vue-router'

// 静态路由
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '首页',
      requiresAuth: true
    },
    children: [
      {
        path: '',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '仪表盘',
          requiresAuth: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || 'OA系统'} - 企业办公自动化系统`
  
  // 权限校验
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

### 2.2 在 main.js 中引入

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import router from './router' // 引入路由配置

const app = createApp(App)
app.use(router) // 使用路由
app.mount('#app')
```

### 2.3 使用说明

- 路由已配置完成，包含登录页和首页
- 路由守卫已添加，用于权限校验和页面标题设置
- 可在 `src/router/index.js` 中添加更多路由

## 3. 配置 Pinia

### 3.1 创建状态管理文件

1. 创建 `src/stores` 目录
2. 创建 `src/stores/index.js` 文件

```javascript
import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia
```

3. 创建 `src/stores/user.js` 文件

```javascript
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token'),
    userInfo: null,
    roles: [],
    permissions: []
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission)
    }
  },
  
  actions: {
    // 登录
    login(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      this.roles = userInfo.roles || []
      this.permissions = userInfo.permissions || []
      
      // 保存到本地存储
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    
    // 登出
    logout() {
      this.token = null
      this.userInfo = null
      this.roles = []
      this.permissions = []
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    
    // 获取用户信息
    getUserInfo() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        this.userInfo = JSON.parse(userInfoStr)
        this.roles = this.userInfo.roles || []
        this.permissions = this.userInfo.permissions || []
      }
    }
  }
})
```

### 3.2 在 main.js 中引入

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import router from './router'
import pinia from './stores' // 引入Pinia配置

const app = createApp(App)
app.use(router)
app.use(pinia) // 使用Pinia
app.mount('#app')
```

### 3.3 使用说明

- Pinia 已配置完成，包含用户状态管理
- 可在组件中使用 `useUserStore()` 获取用户状态
- 可创建更多 store 文件管理其他状态

## 4. 配置 Element Plus

### 4.1 创建 Element Plus 配置文件

1. 创建 `src/plugins` 目录
2. 创建 `src/plugins/element-plus.js` 文件

```javascript
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

export default function setupElementPlus(app) {
  app.use(ElementPlus, {
    locale: zhCn,
  })
}
```

### 4.2 在 main.js 中引入

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import router from './router'
import pinia from './stores'
import setupElementPlus from './plugins/element-plus' // 引入Element Plus配置

const app = createApp(App)
app.use(router)
app.use(pinia)
setupElementPlus(app) // 使用Element Plus
app.mount('#app')
```

### 4.3 使用说明

- Element Plus 已配置完成，使用中文语言包
- 可在组件中直接使用 Element Plus 组件
- 如需按需导入，可安装 `unplugin-vue-components` 和 `unplugin-auto-import` 插件

## 5. 配置 Axios

### 5.1 创建 Axios 配置文件

1. 创建 `src/utils` 目录
2. 创建 `src/utils/axios.js` 文件

```javascript
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建Axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    
    // 添加token
    if (userStore.token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `${userStore.token}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 统一处理响应码
    if (res.code !== 200) {
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000
      })
      
      // 401: 未登录或登录过期
      if (res.code === 401) {
        const userStore = useUserStore()
        ElMessageBox.confirm('您已被登出，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          userStore.logout()
          location.reload()
        })
      }
      
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  (error) => {
    ElMessage({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

// 封装常用 HTTP 方法，简化 API 调用
export const get = (url, params) => service({ url, method: 'get', params })
export const post = (url, data) => service({ url, method: 'post', data })
export const put = (url, data) => service({ url, method: 'put', data })
export const del = (url, params) => service({ url, method: 'delete', params })

// 导出原始 service，用于特殊情况
export default service
```

### 5.2 创建 API 模块

#### 5.2.1 按业务模块拆分 API

1. 创建 `src/api` 目录
2. 创建 `src/api/auth.js` 文件（认证相关 API）

```javascript
// src/api/auth.js
import { post } from '@/utils/axios'

/**
 * 登录请求
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} - 登录结果
 */
export const login = (data) => post('/auth/login', data)

/**
 * 登出请求
 * @returns {Promise} - 登出结果
 */
export const logout = () => post('/auth/logout')
```

3. 创建 `src/api/user.js` 文件（用户管理 API）

```javascript
// src/api/user.js
import { get } from '@/utils/axios'

/**
 * 获取用户信息
 * @returns {Promise} - 用户信息
 */
export const getUserInfo = () => get('/user/info')

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @returns {Promise} - 用户列表
 */
export const getUserList = (params) => get('/user/list', params)
```

4. 创建 `src/api/index.js` 文件（统一导出所有 API）

```javascript
// src/api/index.js
// 认证相关 API
export * from './auth'

// 用户管理 API
export * from './user'

// 后续可添加更多业务模块的 API 导出
// export * from './data'
// export * from './system'
```

### 5.3 使用说明

- Axios 已配置完成，包含请求和响应拦截器
- 已封装常用 HTTP 方法（get、post、put、del），简化 API 调用
- API 按业务模块拆分，便于管理和维护
- 统一从 `@/api` 导入所需 API，使用方便

### 5.4 API 使用示例

```javascript
// 组件中使用 API
import { login, getUserInfo } from '@/api'

// 登录
try {
  const res = await login({ username: 'admin', password: '123456' })
  console.log('登录成功', res)
} catch (error) {
  console.error('登录失败', error)
}

// 获取用户信息
try {
  const res = await getUserInfo()
  console.log('用户信息', res)
} catch (error) {
  console.error('获取用户信息失败', error)
}
```

## 6. 配置 Vue I18n

### 6.1 创建国际化配置文件

1. 创建 `src/lang` 目录
2. 创建 `src/lang/zh-CN.js` 文件

```javascript
export default {
  login: {
    title: '登录',
    username: '用户名',
    password: '密码',
    pleaseInputUsername: '请输入用户名',
    pleaseInputPassword: '请输入密码',
    loginBtn: '登录',
    passwordLength: '密码长度不能少于6位'
  },
  dashboard: {
    title: '首页',
    welcome: '欢迎回来',
    todayData: '今日数据',
    totalData: '累计数据'
  },
  common: {
    confirm: '确定',
    cancel: '取消',
    delete: '删除',
    edit: '编辑',
    add: '添加',
    search: '搜索',
    reset: '重置',
    save: '保存',
    close: '关闭',
    success: '成功',
    error: '错误',
    warning: '警告',
    info: '提示'
  }
}
```

3. 创建 `src/lang/en.js` 文件

```javascript
export default {
  login: {
    title: 'Login',
    username: 'Username',
    password: 'Password',
    pleaseInputUsername: 'Please input username',
    pleaseInputPassword: 'Please input password',
    loginBtn: 'Login',
    passwordLength: 'Password length cannot be less than 6 characters'
  },
  dashboard: {
    title: 'Dashboard',
    welcome: 'Welcome back',
    todayData: 'Today\'s Data',
    totalData: 'Total Data'
  },
  common: {
    confirm: 'Confirm',
    cancel: 'Cancel',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    reset: 'Reset',
    save: 'Save',
    close: 'Close',
    success: 'Success',
    error: 'Error',
    warning: 'Warning',
    info: 'Info'
  }
}
```

4. 创建 `src/lang/index.js` 文件

```javascript
import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import en from './en'

// 从本地存储获取语言设置
const locale = localStorage.getItem('locale') || 'zh-CN'

const i18n = createI18n({
  legacy: false,
  locale,
  messages: {
    'zh-CN': zhCN,
    'en': en
  },
  globalInjection: true
})

// 监听语言变化
i18n.global.locale.value = locale

// 导出i18n实例
export default i18n
```

### 6.2 在 main.js 中引入

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import router from './router'
import pinia from './stores'
import setupElementPlus from './plugins/element-plus'
import i18n from './lang' // 引入Vue I18n配置

const app = createApp(App)
app.use(router)
app.use(pinia)
setupElementPlus(app)
app.use(i18n) // 使用Vue I18n
app.mount('#app')
```

### 6.3 创建语言切换组件

1. 创建 `src/components` 目录
2. 创建 `src/components/LanguageSwitch.vue` 文件

```vue
<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="language-switch">
      {{ currentLanguage }}
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh-CN">中文</el-dropdown-item>
        <el-dropdown-item command="en">English</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown } from '@element-plus/icons-vue'

const { locale } = useI18n()

const currentLanguage = computed(() => {
  return locale.value === 'zh-CN' ? '中文' : 'English'
})

const handleCommand = (command) => {
  locale.value = command
  localStorage.setItem('locale', command)
}
</script>

<style scoped>
.language-switch {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-icon--right {
  margin-left: 5px;
}
</style>
```

### 6.4 使用说明

- Vue I18n 已配置完成，支持中英文切换
- 语言设置会保存到本地存储
- 可在组件中使用 `$t()` 或 `t()` 函数获取翻译
- 可在 `src/lang` 目录下添加更多语言包

## 7. 完整的 main.js 文件

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import router from './router'
import pinia from './stores'
import setupElementPlus from './plugins/element-plus'
import i18n from './lang'

const app = createApp(App)

// 使用插件
app.use(router)
app.use(pinia)
setupElementPlus(app)
app.use(i18n)

// 挂载应用
app.mount('#app')
```

## 8. 使用指南

1. **配置完成后，运行 `npm run dev` 启动开发服务器**
2. **访问 `http://localhost:3000` 查看应用**
3. **在组件中使用配置的库**：
   - Vue Router: `useRouter()` 和 `useRoute()`
   - Pinia: `useUserStore()` 等
   - Element Plus: 直接使用组件，如 `<el-button>`
   - Axios: `import { login, getUserInfo } from '@/api'`
   - Vue I18n: `$t('key')` 或 `t('key')`

## 9. 注意事项

1. **确保所有目录和文件都已正确创建**
2. **确保依赖库已正确安装**
3. **根据实际需求调整配置**
4. **定期更新依赖库**
5. **遵循代码规范**

以上就是前端项目配置的详细指南，按照上述步骤操作即可完成所有配置。