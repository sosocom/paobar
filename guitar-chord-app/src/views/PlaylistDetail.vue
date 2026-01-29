<template>
  <div class="min-h-screen pb-20">
    <!-- Header with Back Button -->
    <header class="sticky top-0 z-40 bg-background-card/95 backdrop-blur-xl">
      <!-- Top Bar -->
      <div class="px-4 py-3 flex items-center gap-3">
        <button @click="goBack" class="p-1">
          <ChevronLeft :size="24" class="text-text-primary" />
        </button>
        <h1 class="text-lg font-semibold flex-1">歌单</h1>
      </div>
      
      <!-- Playlist Info Card -->
      <div class="px-4 pb-4">
        <div class="bg-background-overlay/50 backdrop-blur-sm rounded-2xl p-4 flex items-center gap-4">
          <div 
            class="w-20 h-20 rounded-xl flex items-center justify-center flex-shrink-0"
            :style="playlist ? `background: linear-gradient(135deg, ${playlist.gradient?.[0]}, ${playlist.gradient?.[1]})` : ''"
          >
            <Sparkles v-if="playlist?.type === 'ai'" :size="32" class="text-white" />
            <ListMusic v-else :size="32" class="text-white" />
          </div>
          
          <div class="flex-1 min-w-0">
            <h2 class="text-xl font-semibold mb-1 truncate">{{ playlist?.name }}</h2>
            <p class="text-sm text-text-secondary mb-2">{{ playlist?.songCount }} 首歌曲</p>
            <p v-if="playlist?.type === 'ai'" class="text-xs text-primary">
              和弦走向: {{ playlist?.chordProgression }}
            </p>
          </div>
        </div>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
      <p class="text-text-secondary text-sm mt-3">加载中...</p>
    </div>

    <!-- Song List -->
    <div v-else class="px-4 py-4 space-y-3">
      <div 
        v-for="(song, idx) in playlistSongs" 
        :key="song.id"
        @click="playSong(song, idx)"
        class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-3 flex gap-3 hover:bg-background-card/70 transition-all cursor-pointer"
      >
        <!-- Index Number -->
        <div class="flex items-center justify-center w-8 text-text-secondary text-sm">
          {{ idx + 1 }}
        </div>
        
        <!-- Cover -->
        <div class="w-16 h-16 rounded-xl overflow-hidden flex-shrink-0">
          <img :src="song.coverUrl" :alt="song.title" class="w-full h-full object-cover" />
        </div>
        
        <!-- Info -->
        <div class="flex-1 min-w-0">
          <h3 class="font-medium truncate mb-1">{{ song.title }}</h3>
          <p class="text-sm text-text-secondary truncate mb-2">{{ song.artist }}</p>
          
          <div class="flex items-center gap-3 text-xs text-text-secondary">
            <!-- Difficulty Stars -->
            <div class="flex items-center gap-1">
              <Star 
                v-for="i in 5" 
                :key="i"
                :size="12"
                :class="i <= song.difficulty ? 'fill-primary text-primary' : 'text-text-secondary'"
              />
            </div>
            
            <span class="px-2 py-0.5 bg-background-overlay/50 rounded">{{ song.key }}调</span>
            <span>{{ song.chordCount }}个和弦</span>
          </div>
        </div>
        
        <!-- Play Icon -->
        <div class="flex items-center">
          <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
            <Play :size="20" class="text-primary ml-0.5" />
          </div>
        </div>
      </div>
      
      <!-- Empty State -->
      <div v-if="playlistSongs.length === 0" class="text-center py-12">
        <Music :size="48" class="text-text-secondary mx-auto mb-3 opacity-30" />
        <p class="text-text-secondary">歌单为空</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ChevronLeft, Star, Play, ListMusic, Sparkles, Music } from 'lucide-vue-next'
import { api } from '@/api'
import type { Song, Playlist } from '@/types'

const router = useRouter()
const route = useRoute()

const playlist = ref<Playlist | null>(null)
const playlistSongs = ref<Song[]>([])
const loading = ref(false)

// 加载歌单详情
const loadPlaylistDetail = async () => {
  loading.value = true
  try {
    const playlistId = route.params.id as string
    
    // 并行加载歌单信息和歌曲列表
    const [playlistData, songs] = await Promise.all([
      api.getPlaylistById(playlistId),
      api.getPlaylistSongs(playlistId)
    ])
    
    playlist.value = playlistData
    playlistSongs.value = songs
  } catch (error) {
    console.error('Failed to load playlist detail:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPlaylistDetail()
})

const goBack = () => {
  router.back()
}

const playSong = (song: Song, index: number) => {
  router.push({ name: 'NowPlaying', params: { id: song.id } })
}
</script>
