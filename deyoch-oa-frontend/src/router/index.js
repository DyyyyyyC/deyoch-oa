// 导入Vue Router的核心函数
// createRouter: 创建路由实例
// createWebHistory: 使用HTML5 History模式
import { createRouter, createWebHistory } from 'vue-router'

// 定义静态路由配置数组
// 静态路由：不需要动态权限校验的路由
const routes = [
  // 根路径重定向到登录页
  {
    path: '/',                // 路由路径
    redirect: '/login'        // 重定向到登录页
  },
  // 登录页路由
  {
    path: '/login',           // 路由路径
    name: 'Login',            // 路由名称
    // 懒加载组件，只有当路由被访问时才会加载
    component: () => import('@/views/login/Login.vue'),
    meta: {                   // 路由元信息，用于传递额外数据
      title: '登录',          // 页面标题
      requiresAuth: false     // 是否需要认证，登录页不需要
    }
  },
  // 仪表盘首页路由，包含子路由
  {
    path: '/dashboard',       // 父路由路径
    component: () => import('@/layout/index.vue'),  // 父组件（布局组件）
    meta: {
      title: '首页',          // 页面标题
      requiresAuth: true      // 需要认证才能访问
    },
    children: [               // 子路由配置
      {
        path: '',             // 子路由路径为空，表示默认子路由
        name: 'Dashboard',    // 将名称移动到子路由上
        // 懒加载仪表盘组件
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '仪表盘',    // 页面标题
          requiresAuth: true  // 需要认证才能访问
        }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),  // 使用HTML5 History模式，URL中不带#
  routes,                       // 注册路由配置
  // 滚动行为配置：页面跳转时滚动到顶部
  scrollBehavior() {
    return { top: 0 }           // 每次跳转后滚动到页面顶部
  }
})

// 路由守卫：全局前置守卫，在路由跳转前执行
router.beforeEach((to, from, next) => {
  // 设置页面标题：使用路由元信息中的title或默认值
  document.title = `${to.meta.title || 'OA系统'}`
  
  // 权限校验逻辑
  // 1. 从本地存储获取token
  const token = localStorage.getItem('token')
  
  // 2. 检查当前路由是否需要认证，且是否有token
  // 如果需要认证但没有token，则跳转到登录页
  if (to.meta.requiresAuth && !token) {
    next('/login')  // 重定向到登录页
  } else {
    next()          // 允许路由跳转
  }
})

// 导出路由实例，供main.js使用
export default router