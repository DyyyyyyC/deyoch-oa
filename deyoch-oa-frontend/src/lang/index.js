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
 */
const i18n = createI18n({
  legacy: false, // 启用 Composition API 模式，不使用传统的 Options API
  locale, // 设置初始语言
  messages: { // 配置语言包
    'zh-CN': zhCN, // 中文语言包
    'en': en // 英文语言包
  },
  globalInjection: true // 全局注入$t和$i18n，可在模板中直接使用$t()
})

/**
 * 监听语言变化
 * 确保当前语言与localStorage中保存的语言一致
 * 当locale.value变化时，会自动更新所有使用$t()的文本
 */
i18n.global.locale.value = locale

/**
 * 导出i18n实例
 * 供main.js导入并使用
 */
export default i18n