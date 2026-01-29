<template>
  <div class="min-h-screen pb-20">
    <!-- Header -->
    <header class="sticky top-0 z-40 bg-background-card/95 backdrop-blur-xl px-4 py-4">
      <h1 class="text-2xl font-semibold mb-4">谱库</h1>
      
      <!-- Search Bar -->
      <div class="relative mb-4">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary" :size="20" />
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="搜索歌曲、歌手"
          class="w-full bg-background-overlay/50 rounded-xl pl-11 pr-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50"
        />
      </div>
      
      <!-- Filter Tags -->
      <div class="flex gap-2 overflow-x-auto pb-2">
        <button 
          v-for="tag in filterTags" 
          :key="tag"
          @click="selectedTag = tag"
          class="px-4 py-2 rounded-full text-sm whitespace-nowrap transition-all"
          :class="selectedTag === tag 
            ? 'bg-primary text-white' 
            : 'bg-background-overlay/50 text-text-secondary hover:bg-background-overlay'"
        >
          {{ tag }}
        </button>
      </div>
    </header>

    <!-- Song List -->
    <div class="px-4 py-4 space-y-3">
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-text-secondary text-sm mt-3">加载中...</p>
      </div>
      
      <!-- Song Cards -->
      <div 
        v-for="song in songs" 
        :key="song.id"
        @click="playSong(song)"
        class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-3 flex gap-3 hover:bg-background-card/70 transition-all cursor-pointer"
      >
        <!-- Cover -->
        <div class="w-20 h-20 rounded-xl overflow-hidden flex-shrink-0">
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Star, Play } from 'lucide-vue-next'
import { api } from '@/api'
import type { Song } from '@/types'

const router = useRouter()
const searchQuery = ref('')
const selectedTag = ref('全部')
const songs = ref<Song[]>([])
const loading = ref(false)

const filterTags = ['全部', '已收藏']

// 加载歌曲列表
const loadSongs = async () => {
  loading.value = true
  try {
    if (selectedTag.value === '已收藏') {
      // 加载收藏的歌曲
      songs.value = await api.getFavoriteSongs()
    } else {
      // 加载所有歌曲，支持搜索
      const params = {
        search: searchQuery.value
      }
      songs.value = await api.getSongs(params)
    }
  } catch (error) {
    console.error('Failed to load songs:', error)
  } finally {
    loading.value = false
  }
}

// 监听筛选条件变化
watch([selectedTag, searchQuery], () => {
  loadSongs()
})

// 初始化加载
onMounted(() => {
  loadSongs()
})

const playSong = (song: Song) => {
  router.push({ name: 'NowPlaying', params: { id: song.id } })
}
</script>
