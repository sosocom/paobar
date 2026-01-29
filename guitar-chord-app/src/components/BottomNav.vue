<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-background-card/95 backdrop-blur-xl border-t border-white/5 z-50">
    <div class="flex justify-around items-center h-20 px-4">
      <router-link 
        v-for="item in navItems" 
        :key="item.name"
        :to="item.path"
        class="flex flex-col items-center gap-1 px-4 py-2 transition-colors"
        :class="isActive(item.path) ? 'text-primary' : 'text-text-secondary'"
      >
        <component :is="item.icon" :size="24" :stroke-width="2" />
        <span class="text-xs">{{ item.label }}</span>
      </router-link>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { Library, ListMusic, User } from 'lucide-vue-next'

const route = useRoute()

const navItems = [
  { name: 'library', path: '/library', label: '谱库', icon: Library },
  { name: 'playlists', path: '/playlists', label: '歌单', icon: ListMusic },
  { name: 'profile', path: '/profile', label: '我的', icon: User }
]

const isActive = (path: string) => {
  return route.path === path
}
</script>
