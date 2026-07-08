// 入口文件：Vue 应用的起点
// 就像一栋大楼的地基，所有功能都从这里开始搭建

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './global.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

// 创建 Vue 应用实例
const app = createApp(App)

// 全局错误处理：过滤掉 ResizeObserver 的无害警告
app.config.errorHandler = (err, instance, info) => {
  if (err && err.message && err.message.includes('ResizeObserver')) {
    return // 忽略 ResizeObserver 相关警告
  }
  console.error(err)
}

// 浏览器层面也过滤 ResizeObserver 警告（阻止 webpack-dev-server 的 overlay 显示）
const originalOnError = window.onerror
window.onerror = (message, source, lineno, colno, error) => {
  if (typeof message === 'string' && message.includes('ResizeObserver')) {
    return true // 阻止默认错误处理
  }
  if (originalOnError) {
    return originalOnError(message, source, lineno, colno, error)
  }
  return false
}

// 同时过滤未捕获的 Promise 拒绝中的 ResizeObserver 错误
window.addEventListener('unhandledrejection', (event) => {
  if (event.reason && event.reason.message && event.reason.message.includes('ResizeObserver')) {
    event.preventDefault()
  }
})

// 注册 Element Plus 组件库——就像给房子装上标准的电线和水管
app.use(ElementPlus)

// 注册所有图标组件，让页面可以使用丰富的图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 安装路由系统——让页面之间可以导航跳转
app.use(router)

// 挂载到 HTML 中的 #app 元素上
app.mount('#app')
