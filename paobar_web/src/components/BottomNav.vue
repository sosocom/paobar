<template>
  <!-- 底部导航：
       - fixed 在视口底部，standalone 模式下紧贴屏幕下沿
       - 内容高度固定 16（64px），额外 padding-bottom 由 iOS safe-area 自动撑开
       - 背景半透明 + 背后模糊，iOS 风格 -->
  <nav
    class="fixed bottom-0 left-0 right-0 z-50 bg-background-card/95 backdrop-blur-xl border-t border-white/5"
    style="padding-bottom: env(safe-area-inset-bottom, 0px);"
  >
    <div class="flex justify-around items-center h-16 px-2">
      <router-link
        v-for="item in navItems"
        :key="item.name"
        :to="item.path"
        class="flex flex-col items-center gap-0.5 px-3 py-1.5 transition-colors"
        :class="isActive(item.path) ? 'text-primary' : 'text-text-secondary'"
      >
        <component :is="item.icon" :size="22" :stroke-width="2" />
        <span class="text-[11px] leading-none">{{ item.label }}</span>
      </router-link>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Library, ListMusic, Guitar, User } from 'lucide-vue-next'
import { isAdmin } from '@/auth'

const route = useRoute()

// 扒谱仅管理员可见，普通用户登录态下也不会出现入口
const navItems = computed(() => {
  const items = [
    { name: 'index', path: '/index', label: '首页', icon: Library },
    { name: 'playlists', path: '/playlists', label: '歌单', icon: ListMusic },
  ]
  if (isAdmin.value) {
    items.push({ name: 'crawler', path: '/crawler', label: '扒谱', icon: Guitar })
  }
  items.push({ name: 'profile', path: '/profile', label: '我的', icon: User })
  return items
})

const isActive = (path: string) => {
  return route.path === path
}
</script>
