import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import './style/management-layout.css' // 引入统一管理页面布局样式
import router from './router' // 引入路由配置
import pinia from './stores' // 引入Pinia配置
import setupElementPlus from './plugins/element-plus' // 引入Element Plus配置
import i18n from './lang' // 引入Vue I18n配置

const app = createApp(App)
app.use(router) // 使用路由
app.use(pinia) // 使用Pinia
app.use(i18n) // 使用Vue I18n

// 异步设置Element Plus
setupElementPlus(app).then(() => {
  app.mount('#app') // 挂载应用
})
