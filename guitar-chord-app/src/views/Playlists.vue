<template>
  <div class="min-h-screen pb-20">
    <!-- Header -->
    <header class="sticky top-0 z-40 bg-background-card/95 backdrop-blur-xl px-4 py-4">
      <h1 class="text-2xl font-semibold">歌单</h1>
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

onMounted(() => {
  loadPlaylists()
})

const openPlaylist = (playlist: Playlist) => {
  router.push({ name: 'PlaylistDetail', params: { id: playlist.id } })
}
</script>
