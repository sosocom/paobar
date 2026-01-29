<template>
  <div class="min-h-screen pb-20">
    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
      <p class="text-text-secondary text-sm mt-3">加载中...</p>
    </div>
    
    <template v-else-if="user">
    <!-- User Header -->
    <header class="bg-background-card/95 backdrop-blur-xl px-4 py-6">
      <div class="flex items-center gap-4 mb-6">
        <div class="w-20 h-20 rounded-full overflow-hidden">
          <img :src="user.avatar" :alt="user.username" class="w-full h-full object-cover" />
        </div>
        <div>
          <h1 class="text-xl font-semibold mb-1">{{ user.username }}</h1>
          <p class="text-sm text-text-secondary">ID: {{ user.id }}</p>
        </div>
      </div>
      
      <!-- Points Card -->
      <div 
        class="rounded-2xl p-4 relative overflow-hidden"
        style="background: linear-gradient(135deg, #DC2626, #7C2D12)"
      >
        <div class="relative z-10">
          <p class="text-white/80 text-sm mb-1">我的点数</p>
          <div class="flex items-end justify-between">
            <span class="text-3xl font-bold text-white">{{ user.points }}</span>
            <button class="px-4 py-1.5 bg-white/20 backdrop-blur-sm rounded-lg text-sm text-white hover:bg-white/30 transition-all">
              充值
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- Stats -->
    <section class="px-4 py-6">
      <div class="grid grid-cols-3 gap-4">
        <div 
          v-for="stat in stats" 
          :key="stat.label"
          class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 text-center"
        >
          <div class="text-2xl font-bold text-text-primary mb-1">{{ stat.value }}</div>
          <div class="text-xs text-text-secondary">{{ stat.label }}</div>
        </div>
      </div>
    </section>

    <!-- Menu Items -->
    <section class="px-4 space-y-2">
      <button 
        v-for="item in menuItems" 
        :key="item.label"
        class="w-full bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 flex items-center gap-3 hover:bg-background-card/70 transition-all"
      >
        <div class="w-10 h-10 rounded-xl bg-background-overlay/50 flex items-center justify-center">
          <component :is="item.icon" :size="20" class="text-text-secondary" />
        </div>
        <span class="flex-1 text-left text-text-primary">{{ item.label }}</span>
        <ChevronRight :size="20" class="text-text-secondary" />
      </button>
    </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Heart, ListMusic, Download, History, Settings, ChevronRight } from 'lucide-vue-next'
import { api } from '@/api'
import type { UserProfile } from '@/types'

const user = ref<UserProfile | null>(null)
const loading = ref(false)

// 加载用户信息
const loadUser = async () => {
  loading.value = true
  try {
    user.value = await api.getCurrentUser()
  } catch (error) {
    console.error('Failed to load user:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUser()
})

const stats = computed(() => {
  if (!user.value) return []
  return [
    { label: '收藏', value: user.value.stats.collected },
    { label: '歌单', value: user.value.stats.playlists },
    { label: '练习时长', value: user.value.stats.practiceHours }
  ]
})

const menuItems = [
  { label: '收藏的歌曲', icon: Heart },
  { label: '创建的歌单', icon: ListMusic },
  { label: '已下载', icon: Download },
  { label: '播放历史', icon: History },
  { label: '设置', icon: Settings }
]
</script>
