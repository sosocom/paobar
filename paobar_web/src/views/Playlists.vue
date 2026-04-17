<template>
  <div class="min-h-screen pb-20">
    <!-- Header -->
    <header class="sticky top-0 z-40 bg-background-card/95 backdrop-blur-xl px-4 py-4">
      <h1 class="text-2xl font-semibold text-center">歌单</h1>
    </header>

    <div class="px-4 py-6 space-y-8">
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-text-secondary text-sm mt-3">加载中...</p>
      </div>
      
      <template v-else>
        <!-- User Playlists -->
        <section>
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-medium">我的歌单</h2>
        </div>
        
        <div class="space-y-3">
          <!-- Create Playlist Button -->
          <button 
            @click="showCreateDialog = true"
            class="w-full bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 flex items-center gap-3 hover:bg-background-card/70 transition-all"
          >
            <div class="w-16 h-16 rounded-xl bg-background-overlay/50 flex items-center justify-center">
              <PlusCircle :size="28" class="text-text-secondary" />
            </div>
            <span class="text-text-secondary">创建歌单</span>
          </button>
          
          <!-- User Playlist Cards -->
          <div 
            v-for="playlist in userPlaylists" 
            :key="playlist.id"
            @click="openPlaylist(playlist)"
            class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 flex items-center gap-3 hover:bg-background-card/70 transition-all cursor-pointer"
          >
            <div 
              class="w-16 h-16 rounded-xl flex items-center justify-center"
              :style="{ background: `linear-gradient(135deg, ${playlist.gradient?.[0]}, ${playlist.gradient?.[1]})` }"
            >
              <ListMusic :size="28" class="text-white" />
            </div>
            
            <div class="flex-1">
              <h3 class="font-medium mb-1">{{ playlist.name }}</h3>
              <p class="text-sm text-text-secondary">{{ playlist.songCount }} 首</p>
            </div>
            
            <ChevronRight :size="20" class="text-text-secondary" />
          </div>
        </div>
      </section>

      <!-- AI Playlists -->
      <section>
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-medium">AI 智能歌单</h2>
          <span class="text-xs text-text-secondary">根据和弦走向生成</span>
        </div>
        
        <div class="space-y-3">
          <div 
            v-for="playlist in aiPlaylists" 
            :key="playlist.id"
            @click="openPlaylist(playlist)"
            class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 flex items-center gap-3 hover:bg-background-card/70 transition-all cursor-pointer"
          >
            <div 
              class="w-16 h-16 rounded-xl flex items-center justify-center relative overflow-hidden"
              :style="{ background: `linear-gradient(135deg, ${playlist.gradient?.[0]}, ${playlist.gradient?.[1]})` }"
            >
              <Sparkles :size="28" class="text-white" />
            </div>
            
            <div class="flex-1">
              <h3 class="font-medium mb-1">{{ playlist.name }}</h3>
              <p class="text-sm text-text-secondary">{{ playlist.songCount }} 首 · {{ playlist.chordProgression }}</p>
            </div>
            
            <ChevronRight :size="20" class="text-text-secondary" />
          </div>
        </div>
      </section>
      </template>
    </div>

    <!-- Create Playlist Dialog -->
    <Transition name="dialog">
      <div v-if="showCreateDialog" class="fixed inset-0 z-50 flex items-center justify-center" @click.self="closeCreateDialog">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60" @click="closeCreateDialog"></div>
        
        <!-- Dialog Content -->
        <div class="relative bg-background-card rounded-3xl p-6 w-[90%] max-w-md">
          <h2 class="text-xl font-semibold mb-4">创建歌单</h2>
          
          <input
            v-model="newPlaylistName"
            type="text"
            placeholder="输入歌单名称"
            class="w-full bg-background-overlay/50 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 mb-1"
            @keyup.enter="createPlaylist"
            maxlength="20"
          />
          <p class="text-xs text-text-secondary mb-4">{{ newPlaylistName.length }}/20</p>
          
          <!-- Error Message -->
          <p v-if="createError" class="text-sm text-red-400 mb-4">{{ createError }}</p>
          
          <!-- Buttons -->
          <div class="flex gap-3">
            <button
              @click="closeCreateDialog"
              class="flex-1 py-3 rounded-xl bg-background-overlay/50 text-text-secondary hover:bg-background-overlay transition-colors"
            >
              取消
            </button>
            <button
              @click="createPlaylist"
              :disabled="creating || !newPlaylistName.trim()"
              class="flex-1 py-3 rounded-xl bg-primary text-white hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {{ creating ? '创建中...' : '创建' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusCircle, ListMusic, Sparkles, ChevronRight } from 'lucide-vue-next'
import { api } from '@/api'
import type { Playlist } from '@/types'

const router = useRouter()
const userPlaylists = ref<Playlist[]>([])
const aiPlaylists = ref<Playlist[]>([])
const loading = ref(false)
const showCreateDialog = ref(false)
const newPlaylistName = ref('')
const creating = ref(false)
const createError = ref('')

// 加载歌单列表
const loadPlaylists = async () => {
  loading.value = true
  try {
    const [user, ai] = await Promise.all([
      api.getPlaylistsByType('user'),
      api.getPlaylistsByType('ai')
    ])
    userPlaylists.value = user
    aiPlaylists.value = ai
  } catch (error) {
    console.error('Failed to load playlists:', error)
  } finally {
    loading.value = false
  }
}

// 创建歌单
const createPlaylist = async () => {
  if (!newPlaylistName.value.trim()) {
    createError.value = '歌单名称不能为空'
    return
  }
  
  creating.value = true
  createError.value = ''
  
  try {
    const playlist = await api.createPlaylist(newPlaylistName.value.trim())
    // 创建成功，重新加载歌单列表
    await loadPlaylists()
    // 关闭对话框
    closeCreateDialog()
    // 跳转到新创建的歌单
    router.push({ name: 'PlaylistDetail', params: { id: playlist.id } })
  } catch (error: any) {
    console.error('Failed to create playlist:', error)
    createError.value = error.message || '创建失败，请重试'
  } finally {
    creating.value = false
  }
}

// 关闭创建对话框
const closeCreateDialog = () => {
  showCreateDialog.value = false
  newPlaylistName.value = ''
  createError.value = ''
}

onMounted(() => {
  loadPlaylists()
})

const openPlaylist = (playlist: Playlist) => {
  router.push({ name: 'PlaylistDetail', params: { id: playlist.id } })
}
</script>

<style scoped>
/* Dialog transition */
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.3s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}

.dialog-enter-active .relative,
.dialog-leave-active .relative {
  transition: transform 0.3s ease;
}

.dialog-enter-from .relative,
.dialog-leave-to .relative {
  transform: scale(0.95) translateY(20px);
}
</style>
