import { createRouter, createWebHashHistory } from 'vue-router'
import { isLoggedIn } from '@/auth'
import { requireLogin } from '@/authBus'
import Index from '@/views/Index.vue'
import Playlists from '@/views/Playlists.vue'
import PlaylistDetail from '@/views/PlaylistDetail.vue'
import NowPlaying from '@/views/NowPlaying.vue'
import Crawler from '@/views/Crawler.vue'
import Profile from '@/views/Profile.vue'
import Login from '@/views/Login.vue'
import Favorites from '@/views/Favorites.vue'
import Settings from '@/views/Settings.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/index' },
    { path: '/login', name: 'Login', component: Login, meta: { public: true } },
    // 浏览类页面：无需登录
    { path: '/index', name: 'Index', component: Index, meta: { public: true } },
    { path: '/library', redirect: '/index' },
    { path: '/now-playing/:id', name: 'NowPlaying', component: NowPlaying, meta: { public: true } },
    { path: '/crawler', name: 'Crawler', component: Crawler, meta: { public: true } },
    // 用户页面：需要登录
    { path: '/playlists', name: 'Playlists', component: Playlists, meta: { requiresAuth: true } },
    { path: '/playlist/:id', name: 'PlaylistDetail', component: PlaylistDetail, meta: { requiresAuth: true } },
    { path: '/profile', name: 'Profile', component: Profile, meta: { requiresAuth: true } },
    { path: '/favorites', name: 'Favorites', component: Favorites, meta: { requiresAuth: true } },
    { path: '/settings', name: 'Settings', component: Settings, meta: { requiresAuth: true } },
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
  next()
})

export default router
