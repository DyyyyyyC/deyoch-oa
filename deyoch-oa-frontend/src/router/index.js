// 导入Vue Router的核心函数
// createRouter: 创建路由实例
// createWebHistory: 使用HTML5 History模式
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 定义静态路由配置数组
// 静态路由：不需要动态权限校验的路由
const constantRoutes = [
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
    meta: {
      title: '登录',          // 页面标题
      requiresAuth: false     // 是否需要认证，登录页不需要
    }
  }
]

// 定义动态路由配置数组
// 动态路由：需要根据用户权限动态加载的路由
const asyncRoutes = [
  // 仪表盘首页路由，包含子路由
  {
    path: '/dashboard',       // 父路由路径
    component: () => import('@/layout/index.vue'),  // 父组件（布局组件）
    meta: {
      title: '首页',          // 页面标题
      requiresAuth: true,     // 需要认证才能访问
      permission: 'dashboard:view'  // 权限标识
    },
    children: [               // 子路由配置
      {
        path: '',             // 子路由路径为空，表示默认子路由
        name: 'Dashboard',    // 将名称移动到子路由上
        // 懒加载仪表盘组件
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '仪表盘',    // 页面标题
          requiresAuth: true,  // 需要认证才能访问
          permission: 'dashboard:view'  // 权限标识
        }
      }
    ]
  },
  // 系统管理菜单
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '系统管理',
      requiresAuth: true,
      permission: 'system:manage'
    },
    children: [
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          permission: 'user:list'
        }
      },
      {
        path: 'role',
        name: 'RoleList',
        component: () => import('@/views/system/role/index.vue'),
        meta: {
          title: '角色管理',
          requiresAuth: true,
          permission: 'role:list'
        }
      },
      {
        path: 'permission',
        name: 'PermissionList',
        component: () => import('@/views/system/permission/index.vue'),
        meta: {
          title: '权限管理',
          requiresAuth: true,
          permission: 'permission:list'
        }
      }
    ]
  },
  // 公告管理菜单
  {
    path: '/announcement',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '公告管理',
      requiresAuth: true,
      permission: 'announcement:manage'
    },
    children: [
      {
        path: 'list',
        name: 'AnnouncementList',
        component: () => import('@/views/announcement/index.vue'),
        meta: {
          title: '公告列表',
          requiresAuth: true,
          permission: 'announcement:list'
        }
      }
    ]
  },
  // 任务管理菜单
  {
    path: '/task',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '任务管理',
      requiresAuth: true,
      permission: 'task:manage'
    },
    children: [
      {
        path: 'list',
        name: 'TaskList',
        component: () => import('@/views/task/index.vue'),
        meta: {
          title: '任务列表',
          requiresAuth: true,
          permission: 'task:list'
        }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),  // 使用HTML5 History模式，URL中不带#
  routes: constantRoutes,        // 初始只注册静态路由
  // 滚动行为配置：页面跳转时滚动到顶部
  scrollBehavior() {
    return { top: 0 }           // 每次跳转后滚动到页面顶部
  }
})

// 路由守卫：全局前置守卫，在路由跳转前执行
router.beforeEach((to, from, next) => {
  // 设置页面标题：使用路由元信息中的title或默认值
  document.title = `${to.meta.title || 'OA系统'}`
  
  // 1. 从本地存储获取token
  const token = localStorage.getItem('token')
  
  // 2. 检查当前路由是否需要认证
  if (!to.meta.requiresAuth) {
    // 不需要认证的路由，直接放行
    next()
    return
  }
  
  // 3. 需要认证的路由，检查是否有token
  if (!token) {
    next('/login')  // 没有token，跳转到登录页
    return
  }
  
  // 4. 有token，检查是否已加载动态路由
  const userStore = useUserStore()
  if (userStore.routes === undefined || userStore.routes.length === 0) {
    // 未加载动态路由，需要加载
    try {
      // 获取用户权限
      const permissions = userStore.permissions || []
      
      // 根据权限过滤动态路由
      const accessedRoutes = filterAsyncRoutes(asyncRoutes, permissions)
      
      // 动态添加路由
      accessedRoutes.forEach(route => {
        router.addRoute(route)
      })
      
      // 保存已加载的路由
      userStore.routes = accessedRoutes
      
      // 重新跳转，确保路由已加载
      next({ ...to, replace: true })
    } catch (error) {
      // 加载动态路由失败，清除token并跳转到登录页
      userStore.logout()
      next('/login')
    }
  } else {
    // 已加载动态路由，直接放行
    next()
  }
})

/**
 * 根据用户权限过滤动态路由
 * @param {Array} routes 动态路由配置
 * @param {Array} permissions 用户权限列表
 * @returns {Array} 过滤后的路由列表
 */
function filterAsyncRoutes(routes, permissions) {
  const res = []
  
  routes.forEach(route => {
    const tmp = { ...route }
    
    // 检查当前路由是否需要权限，以及用户是否拥有该权限
    if (hasPermission(permissions, tmp)) {
      // 递归过滤子路由
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, permissions)
      }
      res.push(tmp)
    }
  })
  
  return res
}

/**
 * 检查用户是否拥有路由所需权限
 * @param {Array} permissions 用户权限列表
 * @param {Object} route 路由配置
 * @returns {boolean} 是否拥有权限
 */
function hasPermission(permissions, route) {
  // 如果路由没有配置permission，则默认不需要权限
  if (!route.meta || !route.meta.permission) {
    return true
  }
  
  // 检查用户是否拥有该权限
  return permissions.includes(route.meta.permission)
}

// 导出路由实例，供main.js使用
export default router