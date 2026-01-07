// 导入Vue Router的核心函数
// createRouter: 创建路由实例
// createWebHistory: 使用HTML5 History模式
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 定义静态路由配置数组
// 静态路由：不需要动态权限校验的路由
const constantRoutes = [
  // 根路径重定向到dashboard页面
  {
    path: '/',                // 路由路径
    redirect: '/dashboard'    // 重定向到dashboard页面
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
  },
  // 仪表盘首页路由，包含子路由
  {
    path: '/dashboard',       // 父路由路径
    component: () => import('@/layout/index.vue'),  // 父组件（布局组件）
    meta: {
      title: '工作台',         // 页面标题
      requiresAuth: true,     // 需要认证才能访问
      // 首页不需要特定权限，所有登录用户都能访问
    },
    children: [               // 子路由配置
      {
        path: '',             // 子路由路径为空，表示默认子路由
        name: 'Dashboard',    // 将名称移动到子路由上
        // 懒加载仪表盘组件
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '工作台',    // 页面标题
          requiresAuth: true,  // 需要认证才能访问
          // 仪表盘不需要特定权限，所有登录用户都能访问
        }
      }
    ]
  },
  // 个人资料页面路由
  {
    path: '/profile',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '个人资料',
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: {
          title: '个人资料',
          requiresAuth: true
        }
      }
    ]
  }
]

// 定义动态路由配置数组
// 动态路由：需要根据用户权限动态加载的路由
const asyncRoutes = [
  // 系统管理菜单
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '系统管理',
      requiresAuth: true,
      permission: 'sys:manage'
    },
    children: [
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          permission: 'sys:user:manage'
        }
      },
      {
        path: 'role',
        name: 'RoleList',
        component: () => import('@/views/system/role/index.vue'),
        meta: {
          title: '角色管理',
          requiresAuth: true,
          permission: 'sys:role:manage'
        }
      },
      {
        path: 'permission',
        name: 'PermissionList',
        component: () => import('@/views/system/permission/index.vue'),
        meta: {
          title: '权限管理',
          requiresAuth: true,
          permission: 'sys:perm:manage'
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
      permission: 'oa:announcement:manage'
    },
    children: [
      {
        path: 'list',
        name: 'AnnouncementList',
        component: () => import('@/views/announcement/index.vue'),
        meta: {
          title: '公告列表',
          requiresAuth: true,
          permission: 'oa:announcement:manage'
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
      permission: 'oa:task:manage'
    },
    children: [
      {
        path: 'list',
        name: 'TaskList',
        component: () => import('@/views/task/index.vue'),
        meta: {
          title: '任务列表',
          requiresAuth: true,
          permission: 'oa:task:manage'
        }
      }
    ]
  },
  // 日程管理菜单
  {
    path: '/schedule',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '日程管理',
      requiresAuth: true,
      permission: 'oa:schedule:manage'
    },
    children: [
      {
        path: 'list',
        name: 'ScheduleList',
        component: () => import('@/views/schedule/index.vue'),
        meta: {
          title: '日程列表',
          requiresAuth: true,
          permission: 'oa:schedule:manage'
        }
      }
    ]
  },
  // 流程管理菜单
  {
    path: '/process',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '流程管理',
      requiresAuth: true,
      permission: 'oa:process:manage'
    },
    children: [
      {
        path: 'definition',
        name: 'ProcessList',
        component: () => import('@/views/process/definition/index.vue'),
        meta: {
          title: '流程定义',
          requiresAuth: true,
          permission: 'oa:process:manage'
        }
      },
      {
        path: 'instance',
        name: 'ProcessInstanceList',
        component: () => import('@/views/process/instance/index.vue'),
        meta: {
          title: '流程实例',
          requiresAuth: true,
          permission: 'oa:process:manage'
        }
      }
    ]
  },
  // 文档管理菜单
  {
    path: '/document',
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '文档管理',
      requiresAuth: true,
      permission: 'oa:document:manage'
    },
    children: [
      {
        path: 'list',
        name: 'DocumentList',
        component: () => import('@/views/document/index.vue'),
        meta: {
          title: '文档列表',
          requiresAuth: true,
          permission: 'oa:document:manage'
        }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),  // 使用HTML5 History模式，URL中不带#
  routes: [...constantRoutes, ...asyncRoutes],  // 初始就注册所有路由，包括静态和动态路由
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
  
  // 2. 特殊处理：访问登录页时，如果已有token，跳转到dashboard
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }
  
  // 3. 检查当前路由是否需要认证
  // 对于未定义requiresAuth的路由，默认需要认证
  // 确保所有需要认证的路由都经过动态路由加载流程
  if (to.meta.requiresAuth === false) {
    // 明确不需要认证的路由，直接放行
    next()
    return
  }
  
  // 4. 需要认证的路由，检查是否有token
  if (!token) {
    next('/login')  // 没有token，跳转到登录页
    return
  }
  
  // 5. 有token，已注册所有路由，直接放行
  // 后续可以根据系统完善情况，在这里添加权限过滤逻辑
  next()
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