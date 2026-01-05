import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 导入Element Plus语言包
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'

// 安装Element Plus，配置语言
export default function setupElementPlus(app) {
  // 获取当前语言
  const locale = localStorage.getItem('locale') || 'zh-CN'
  // 根据当前语言选择对应的Element Plus语言包
  const elementLocale = locale === 'zh-CN' ? zhCn : en
  
  // 安装Element Plus，传入语言配置
  app.use(ElementPlus, {
    locale: elementLocale
  })
  
  return Promise.resolve(app)
}
