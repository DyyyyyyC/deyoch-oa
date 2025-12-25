import { createApp } from 'vue'
import App from './App.vue'
import './style.css'
import router from './router' // 引入路由配置
import pinia from './stores' // 引入Pinia配置
import setupElementPlus from './plugins/element-plus' // 引入Element Plus配置
import i18n from './lang' // 引入Vue I18n配置

const app = createApp(App)
app.use(router) // 使用路由
app.use(pinia) // 使用Pinia
setupElementPlus(app) // 使用Element Plus
app.use(i18n) // 使用Vue I18n
app.mount('#app')
