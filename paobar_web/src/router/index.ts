import { createRouter, createWebHashHistory } from 'vue-router'
import { isLoggedIn, isAdmin } from '@/auth'
import { requireLogin } from '@/authBus'
import Index from '@/views/Index.vue'
import Playlists from '@/views/Playlists.vue'
import PlaylistDetail from '@/views/PlaylistDetail.vue'
import NowPlaying from '@/views/NowPlaying.vue'
import Crawler from '@/views/Crawler.vue'
import Profile from '@/views/Profile.vue'
import Login from '@/views/Login.vue'
import Favorites from '@/views/Favorites.vue'
import History from '@/views/History.vue'
import Settings from '@/views/Settings.vue'
import Admin from '@/views/Admin.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/index' },
    { path: '/login', name: 'Login', component: Login, meta: { public: true } },
    // 浏览类页面：无需登录
    { path: '/index', name: 'Index', component: Index, meta: { public: true } },
    { path: '/library', redirect: '/index' },
    { path: '/now-playing/:id', name: 'NowPlaying', component: NowPlaying, meta: { public: true } },
    // 扒谱：管理员专属（后端 /api/crawler/** 也已强制 is_admin=1）
    { path: '/crawler', name: 'Crawler', component: Crawler, meta: { requiresAuth: true, requiresAdmin: true } },
    // 用户页面：需要登录
    { path: '/playlists', name: 'Playlists', component: Playlists, meta: { requiresAuth: true } },
    { path: '/playlist/:id', name: 'PlaylistDetail', component: PlaylistDetail, meta: { requiresAuth: true } },
    { path: '/profile', name: 'Profile', component: Profile, meta: { requiresAuth: true } },
    { path: '/favorites', name: 'Favorites', component: Favorites, meta: { requiresAuth: true } },
    { path: '/history', name: 'History', component: History, meta: { requiresAuth: true } },
    { path: '/settings', name: 'Settings', component: Settings, meta: { requiresAuth: true } },
    // 管理员后台
    { path: '/admin', name: 'Admin', component: Admin, meta: { requiresAuth: true, requiresAdmin: true } },
  ],
})

router.beforeEach((to, _from, next) => {
  // 已登录时访问登录页 → 跳回首页
  if (to.name === 'Login' && isLoggedIn()) {
    next({ path: '/index', replace: true })
    return
  }
  // 需要登录的页面，未登录 → 弹登录弹窗 + 阻止导航
  if (to.meta.requiresAuth && !isLoggedIn()) {
    requireLogin(() => {
      router.push(to.fullPath)
    })
    next(false) // 阻止跳转，留在当前页
    return
  }
  // 管理员页面，非管理员 → 直接踢回首页（前端 UX 拦截，后端是真正闸门）
  if (to.meta.requiresAdmin && !isAdmin.value) {
    next({ path: '/index', replace: true })
    return
  }
  next()
})

export default router
