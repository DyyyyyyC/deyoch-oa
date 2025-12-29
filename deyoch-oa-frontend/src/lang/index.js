// 导入Vue I18n的createI18n函数，用于创建国际化实例
import { createI18n } from 'vue-i18n'
// 导入中文语言包
import zhCN from './zh-CN'
// 导入英文语言包
import en from './en'

/**
 * 从本地存储获取语言设置
 * 优先使用localStorage中保存的语言，否则默认使用中文
 */
const locale = localStorage.getItem('locale') || 'zh-CN'

/**
 * 创建Vue I18n实例
 * 用于配置和管理应用的国际化
 * Vue I18n v12.0.0-alpha.3 使用Composition API模式
 */
const i18n = createI18n({
  legacy: true, // 使用传统模式，确保$t可以在模板中直接使用
  locale, // 设置初始语言
  messages: { // 配置语言包
    'zh-CN': zhCN, // 中文语言包
    'en': en // 英文语言包
  },
  globalInjection: true, // 全局注入$t和$i18n，可在模板中直接使用$t()
  fallbackLocale: 'zh-CN' // 设置回退语言为中文
})

/**
 * 导出i18n实例
 * 供main.js导入并使用
 */
export default i18n