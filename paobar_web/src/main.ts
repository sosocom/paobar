import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

// vite-plugin-pwa 注入的虚拟模块：在生产环境自动注册 Service Worker，
// 在开发环境由 devOptions.enabled 控制（当前关闭）。
import { registerSW } from 'virtual:pwa-register'
registerSW({ immediate: true })

// 安装总线：注册 beforeinstallprompt / appinstalled 监听，必须在任何组件 mount 之前，
// 否则 Chrome 可能已经 dispatch 过事件而我们没抓住。
import { initInstallBus } from '@/installBus'
initInstallBus()

// 启动时若本地有 token，静默同步一次最新用户资料，
// 用以填充 isAdmin 等可能在旧缓存里不存在的字段（避免 nav / 路由守卫读到旧数据）。
import { isLoggedIn, setStoredUser } from '@/auth'
import { api } from '@/api'
if (isLoggedIn()) {
  api.getCurrentUser()
    .then(remote => setStoredUser(remote))
    .catch(() => { /* token 失效会被 api.request 自身处理，这里静默 */ })
}

createApp(App).use(router).mount('#app')
