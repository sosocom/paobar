<template>
  <div class="min-h-screen pb-nav">
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
          <img :src="user.avatar || 'https://picsum.photos/seed/default/200/200'" :alt="user.username" class="w-full h-full object-cover" />
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1 flex-wrap">
            <h1 class="text-xl font-semibold truncate">{{ user.username }}</h1>
            <span
              v-if="user.isAdmin"
              class="text-[10px] px-2 py-0.5 rounded-full bg-primary/15 text-primary font-medium"
            >管理员</span>
          </div>
          <p class="text-sm text-text-secondary">ID: {{ user.id }}</p>
        </div>
      </div>

      <!-- 管理员快捷入口：直达后台管理页 -->
      <button
        v-if="user.isAdmin"
        @click="router.push('/admin')"
        class="w-full mb-4 flex items-center gap-3 px-4 py-3 rounded-2xl bg-primary/10 border border-primary/20 hover:bg-primary/15 transition-colors active:scale-[0.99]"
      >
        <div class="w-9 h-9 rounded-xl bg-primary/15 flex items-center justify-center flex-shrink-0">
          <Shield :size="18" class="text-primary" />
        </div>
        <div class="flex-1 text-left min-w-0">
          <p class="text-sm font-medium text-text-primary">管理员后台</p>
          <p class="text-xs text-text-secondary truncate">用户管理 · 扒谱 · 任务监控</p>
        </div>
        <ChevronRight :size="18" class="text-primary flex-shrink-0" />
      </button>
      
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
        @click="item.action"
        class="w-full bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 flex items-center gap-3 hover:bg-background-card/70 transition-all active:scale-[0.98]"
      >
        <div class="w-10 h-10 rounded-xl bg-background-overlay/50 flex items-center justify-center">
          <component :is="item.icon" :size="20" class="text-text-secondary" />
        </div>
        <span class="flex-1 text-left text-text-primary">{{ item.label }}</span>
        <span v-if="item.badge" class="text-xs text-text-secondary bg-background-overlay/50 px-2 py-0.5 rounded-full">{{ item.badge }}</span>
        <ChevronRight :size="18" class="text-text-secondary" />
      </button>
    </section>

    <!-- Logout Button — 与菜单区分开，留出底部间距 -->
    <section class="px-4 mt-8 mb-10">
      <button
        type="button"
        @click="logout"
        class="w-full bg-red-500/10 border border-red-500/20 rounded-2xl p-4 flex items-center justify-center gap-2 hover:bg-red-500/20 transition-all active:scale-[0.98]"
      >
        <LogOut :size="20" class="text-red-400" />
        <span class="text-red-400 font-medium">退出登录</span>
      </button>
    </section>
    </template>

    <div v-else class="text-center py-12 text-text-secondary">
      <p>加载失败或未登录</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Heart, ListMusic, History, Settings, ChevronRight, LogOut, Guitar, Shield } from 'lucide-vue-next'
import { api } from '@/api'
import { clearAuth, getStoredUser, setStoredUser } from '@/auth'
import type { UserProfile } from '@/types'

const router = useRouter()

const user = ref<UserProfile | null>(null)
const loading = ref(false)

// 加载用户信息：先用本地缓存兜底，再从服务端拉最新
// forceLoading=true 时强制显示 loading 状态（用于登录成功后刷新，保证用户看到"正在更新"）
const loadUser = async (forceLoading = false) => {
  const cached = getStoredUser()
  if (cached && !forceLoading) {
    user.value = cached
  }
  loading.value = forceLoading || !cached
  try {
    const remote = await api.getCurrentUser()
    user.value = remote
    setStoredUser(remote)
  } catch (error) {
    console.error('Failed to load user:', error)
  } finally {
    loading.value = false
  }
}

// LoginDialog 在用户已经位于 /profile 时会派发该事件，让本页立即重拉最新资料
const onProfileRefresh = () => {
  loadUser(true)
}

onMounted(() => {
  loadUser()
  window.addEventListener('profile:refresh', onProfileRefresh)
})

onUnmounted(() => {
  window.removeEventListener('profile:refresh', onProfileRefresh)
})

const stats = computed(() => {
  if (!user.value) return []
  return [
    { label: '收藏', value: user.value.stats.collected },
    { label: '歌单', value: user.value.stats.playlists },
    { label: '练习时长', value: user.value.stats.practiceHours }
  ]
})

const menuItems = computed(() => {
  const items = [
    {
      label: '收藏的歌曲',
      icon: Heart,
      badge: user.value?.stats.collected ? String(user.value.stats.collected) : '',
      action: () => router.push('/favorites'),
    },
    {
      label: '我的歌单',
      icon: ListMusic,
      badge: user.value?.stats.playlists ? String(user.value.stats.playlists) : '',
      action: () => router.push('/playlists'),
    },
    {
      label: '播放历史',
      icon: History,
      badge: '',
      action: () => router.push('/history'),
    },
    {
      label: '设置',
      icon: Settings,
      badge: '',
      action: () => router.push('/settings'),
    },
  ]
  // 管理员专属入口（管理员后台已在顶部胶囊按钮提供，菜单只放高频的"扒谱"）
  if (user.value?.isAdmin) {
    items.push({
      label: '扒谱',
      icon: Guitar,
      badge: '管理员',
      action: () => router.push('/crawler'),
    })
  }
  return items
})

function logout() {
  clearAuth()
  router.replace('/login')
}
</script>
