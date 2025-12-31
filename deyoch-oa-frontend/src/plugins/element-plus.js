import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 动态导入Element Plus的语言包
const loadElementPlusLocale = async (locale) => {
  switch (locale) {
    case 'zh-CN':
      return (await import('element-plus/dist/locale/zh-cn.mjs')).default
    case 'en':
      return (await import('element-plus/dist/locale/en.mjs')).default
    default:
      return (await import('element-plus/dist/locale/zh-cn.mjs')).default
  }
}

export default async function setupElementPlus(app) {
  // 获取当前语言
  const currentLocale = localStorage.getItem('locale') || 'zh-CN'
  // 加载对应语言包
  const elementLocale = await loadElementPlusLocale(currentLocale)
  
  // 安装Element Plus
  app.use(ElementPlus, {
    locale: elementLocale,
  })
  
  return app
}
