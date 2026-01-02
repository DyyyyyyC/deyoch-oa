// 导入Pinia的defineStore函数，用于定义状态管理store
import { defineStore } from 'pinia'

/**
 * 用户状态管理Store
 * 用于管理用户的登录状态、token、用户信息、角色和权限
 */
export const useUserStore = defineStore('user', {
  /**
   * 状态定义
   * 初始化时从localStorage获取token和userInfo，实现状态持久化
   */
  state: () => {
    const userInfoStr = localStorage.getItem('userInfo')
    const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null
    return {
      token: localStorage.getItem('token'), // 用户认证token，从本地存储获取
      tokenExpireTime: localStorage.getItem('tokenExpireTime'), // token过期时间，从本地存储获取
      userInfo: userInfo,                   // 用户详细信息，从本地存储获取
      roles: userInfo?.roles || [],         // 用户角色列表，从userInfo中获取
      permissions: userInfo?.permissions || [], // 用户权限列表，从userInfo中获取
      routes: []                           // 已加载的动态路由列表
    }
  },
  
  /**
   * 持久化配置
   * 实现状态的持久化存储
   */
  persist: {
    enabled: true, // 启用持久化
    strategies: [
      {
        key: 'user', // 存储键名
        storage: localStorage, // 存储方式：localStorage
        paths: ['token', 'tokenExpireTime', 'userInfo', 'roles', 'permissions'] // 需要持久化的字段，添加tokenExpireTime
      }
    ]
  },
  
  /**
   * 计算属性（getters）
   * 用于派生状态，类似于Vue组件中的computed
   */
  getters: {
    /**
     * 检查用户是否已登录
     * @returns {boolean} 是否已登录
     */
    isLoggedIn: (state) => !!state.token, // 通过token是否存在判断登录状态
    
    /**
     * 检查用户是否具有指定权限
     * @param {string} permission - 权限标识
     * @returns {boolean} 是否具有该权限
     */
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission) // 检查权限列表中是否包含指定权限
    },
    
    /**
     * 检查token是否过期
     * @returns {boolean} 是否过期
     */
    isTokenExpired: (state) => {
      if (!state.tokenExpireTime) return false
      return new Date().getTime() > new Date(state.tokenExpireTime).getTime()
    }
  },
  
  /**
   * 方法（actions）
   * 用于修改状态，支持异步操作
   */
  actions: {
    /**
     * 用户登录
     * @param {string} token - 登录成功返回的token
     * @param {object} userInfo - 用户详细信息
     */
    login(token, userInfo) {
      // 确保userInfo是对象
      const validUserInfo = userInfo || {};
      
      // 计算token过期时间
      // 假设后端返回expireTime或设置默认过期时间24小时
      const now = new Date();
      const expiresIn = validUserInfo.expiresIn || 24 * 60 * 60 * 1000; // 默认24小时
      const tokenExpireTime = validUserInfo.expireTime || new Date(now.getTime() + expiresIn).toISOString();
      
      // 更新状态
      this.token = token;
      this.tokenExpireTime = tokenExpireTime;
      this.userInfo = validUserInfo;
      this.roles = validUserInfo.roles || [];       // 处理角色，默认空数组
      this.permissions = validUserInfo.permissions || []; // 处理权限，默认空数组
      this.routes = []; // 初始化动态路由列表为空
      
      // 保存到本地存储，实现状态持久化
      localStorage.setItem('token', token);
      localStorage.setItem('tokenExpireTime', tokenExpireTime);
      localStorage.setItem('userInfo', JSON.stringify(validUserInfo));
    },
    
    /**
     * 用户登出
     * 清除所有用户相关状态和本地存储
     */
    logout() {
      // 清除状态
      this.token = null
      this.tokenExpireTime = null
      this.userInfo = null
      this.roles = []
      this.permissions = []
      this.routes = [] // 清除动态路由列表
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('tokenExpireTime')
      localStorage.removeItem('userInfo')
    },
    
    /**
     * 从本地存储获取用户信息
     * 用于页面刷新后恢复用户状态
     */
    getUserInfo() {
      // 从本地存储获取用户信息
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        // 解析用户信息并更新状态
        this.userInfo = JSON.parse(userInfoStr)
        this.roles = this.userInfo.roles || []
        this.permissions = this.userInfo.permissions || []
        this.routes = [] // 初始化动态路由列表为空
      }
    }
  }
})