// 导入Axios库，用于发送HTTP请求
import axios from 'axios'
// 导入Element Plus的消息提示和对话框组件，用于显示请求结果
import { ElMessage, ElMessageBox } from 'element-plus'
// 导入用户状态管理Store，用于获取和管理用户token
import { useUserStore } from '@/stores/user'
// 导入i18n实例，用于国际化
import i18n from '@/lang'

/**
 * 创建Axios实例
 * 用于配置全局的请求参数和默认设置
 */
const service = axios.create({
  baseURL: '/api', // API基础URL，使用相对路径，配合Vite代理
  timeout: 10000 // 请求超时时间，10秒
})

/**
 * 请求拦截器
 * 在发送请求之前执行，用于添加token、设置请求头等
 */
service.interceptors.request.use(
  /**
   * 请求成功处理函数
   * @param {Object} config - 请求配置对象
   * @returns {Object} - 修改后的请求配置
   */
  (config) => {
    // 获取用户Store实例
    const userStore = useUserStore()
    
    // 如果用户已登录，添加token到请求头
    if (userStore.token) {
      // 确保headers对象存在
      config.headers = config.headers || {}
      // 添加Authorization头，值为Bearer + token
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    // 返回修改后的请求配置
    return config
  },
  /**
   * 请求失败处理函数
   * @param {Object} error - 错误对象
   * @returns {Promise} - 被拒绝的Promise
   */
  (error) => {
    // 将错误传递给调用者
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 在收到响应之后执行，用于统一处理响应、错误处理等
 */
service.interceptors.response.use(
  /**
   * 响应成功处理函数
   * @param {Object} response - 响应对象
   * @returns {Object} - 处理后的响应数据
   */
  (response) => {
    // 获取响应数据
    const res = response.data
    
    // 统一处理响应码
      // 这里假设后端约定：code=200表示成功，其他表示失败
      if (res.code !== 200) {
        // 特殊处理401错误：未登录或登录过期
        if (res.code === 401) {
          // 获取用户Store实例
          const userStore = useUserStore()
          // 使用i18n翻译提示信息
          const t = i18n.global.t
          // 显示确认对话框，提示用户重新登录
          ElMessageBox.confirm(t('auth.unauthorized'), t('auth.prompt'), {
            confirmButtonText: t('auth.loginAgain'), // 确认按钮文本
            cancelButtonText: t('common.cancel'), // 取消按钮文本
            type: 'warning' // 对话框类型为警告
          }).then(() => {
            // 用户确认后，执行登出操作
            userStore.logout()
            // 刷新页面，重新进入登录流程
            location.reload()
          })
        }
        
        // 将错误传递给调用者，不显示错误消息
        return Promise.reject(new Error(res.message || 'Error'))
      } else {
        // 响应成功，返回res.data，这是前端组件期望的数组格式
      return res.data
      }
  },
  /**
   * 响应失败处理函数
   * @param {Object} error - 错误对象
   * @returns {Promise} - 被拒绝的Promise
   */
  (error) => {
    // 不显示错误消息，由调用者自行处理
    console.error('Axios response error:', error)
    // 将错误传递给调用者
    return Promise.reject(error)
  }
)
// 封装常用 HTTP 方法
export const get = (url, params) => service({ url, method: 'get', params })
export const post = (url, data) => service({ url, method: 'post', data })
export const put = (url, data) => service({ url, method: 'put', data })
export const del = (url, params) => service({ url, method: 'delete', params })
// 导出Axios实例，供其他模块使用
export default service