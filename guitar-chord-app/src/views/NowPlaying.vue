<template>
  <div class="min-h-screen bg-background-page relative">
    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <div>
        <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-text-secondary text-sm mt-3">加载中...</p>
      </div>
    </div>
    
    <template v-else>
    <!-- Header -->
    <header class="bg-background-card/95 backdrop-blur-xl px-4 py-3 flex items-center justify-center relative">
      <button @click="goBack" class="p-1 absolute left-4">
        <ChevronLeft :size="24" class="text-text-primary" />
      </button>
      <div class="flex items-center gap-3">
        <h1 class="text-lg font-semibold">{{ currentSong?.title }}</h1>
        <span class="text-sm text-text-secondary">{{ currentSong?.key }}调</span>
      </div>
    </header>

    <!-- Chord Sheet Content -->
    <div class="px-4 py-6 pb-32 overflow-y-auto" style="height: calc(100vh - 140px);">
      <div class="space-y-6">
        <div v-for="(section, idx) in currentSong?.chordSheet.sections" :key="idx" class="space-y-3">
          <!-- Section Name -->
          <div class="text-sm font-medium text-primary mb-2">{{ section.name }}</div>
          
          <!-- Chord Lines -->
          <div v-for="(line, lineIdx) in section.lines" :key="lineIdx" class="space-y-1">
            <div class="text-primary font-mono text-sm tracking-wide">{{ line.chords }}</div>
            <div class="text-text-primary text-base">{{ line.lyrics }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Control Bar -->
    <div class="fixed bottom-0 left-0 right-0 bg-background-card/95 backdrop-blur-xl px-4 py-6 border-t border-white/5">
      <div class="flex items-center justify-between gap-5">
        <!-- Prev Section -->
        <button @click="prevSong" class="flex flex-col items-center gap-1">
          <SkipBack :size="28" class="text-text-primary" />
          <span class="text-xs text-text-secondary">{{ prevSongName }}</span>
        </button>
        
        <!-- List Button -->
        <button @click="togglePlaylist" class="flex flex-col items-center gap-1">
          <div class="w-14 h-14 rounded-full bg-primary flex items-center justify-center">
            <ListMusic :size="28" class="text-white" />
          </div>
          <span class="text-xs font-semibold text-text-primary">{{ currentIndex + 1 }}/{{ playlist.length }}</span>
        </button>
        
        <!-- Next Section -->
        <button @click="nextSong" class="flex flex-col items-center gap-1">
          <SkipForward :size="28" class="text-text-primary" />
          <span class="text-xs text-text-secondary">{{ nextSongName }}</span>
        </button>
      </div>
    </div>

    <!-- Playlist Drawer -->
    <Transition name="drawer">
      <div v-if="showPlaylist" class="fixed inset-0 z-50" @click.self="togglePlaylist">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60" @click="togglePlaylist"></div>
        
        <!-- Drawer Content -->
        <div class="absolute bottom-0 left-0 right-0 bg-background-card/95 backdrop-blur-[30px] rounded-t-3xl max-h-[70vh] flex flex-col">
          <!-- Drag Handle -->
          <div class="flex justify-center py-3">
            <div class="w-10 h-1 bg-text-secondary/30 rounded-full"></div>
          </div>
          
          <!-- Header -->
          <div class="px-4 pb-3 flex items-center justify-between border-b border-white/5">
            <h2 class="text-lg font-semibold">当前歌单</h2>
            <span class="text-sm text-text-secondary">{{ playlist.length }} 首</span>
          </div>
          
          <!-- Song List (Scrollable) -->
          <div class="flex-1 overflow-y-auto px-4 py-4">
            <div class="space-y-2">
              <div 
                v-for="(song, idx) in playlist" 
                :key="song.id"
                @click="playSongAtIndex(idx)"
                class="rounded-xl p-3 flex items-center gap-3 transition-colors cursor-pointer"
                :class="idx === currentIndex 
                  ? 'bg-primary/20' 
                  : 'hover:bg-background-overlay/30'"
              >
                <span 
                  class="text-sm font-medium w-6"
                  :class="idx === currentIndex ? 'text-primary' : 'text-text-secondary'"
                >
                  {{ idx + 1 }}
                </span>
                
                <div class="w-10 h-10 rounded-lg overflow-hidden flex-shrink-0">
                  <img :src="song.coverUrl" :alt="song.title" class="w-full h-full object-cover" />
                </div>
                
                <div class="flex-1 min-w-0">
                  <h3 
                    class="text-sm font-medium truncate"
                    :class="idx === currentIndex ? 'text-primary' : 'text-text-primary'"
                  >
                    {{ song.title }}
                  </h3>
                  <p class="text-xs text-text-secondary truncate">{{ song.artist }} · {{ song.key }}调</p>
                </div>
                
                <Music 
                  v-if="idx === currentIndex"
                  :size="20" 
                  class="text-primary flex-shrink-0"
                />
              </div>
            </div>
          </div>
          
          <!-- Bottom Control Bar in Drawer -->
          <div class="px-4 py-3 border-t border-white/5">
            <div class="flex items-center justify-between gap-5">
              <!-- Prev Section -->
              <button @click="prevSong" class="flex flex-col items-center gap-1">
                <SkipBack :size="28" class="text-text-primary" />
                <span class="text-xs text-text-secondary">{{ prevSongName }}</span>
              </button>
              
              <!-- List Button -->
              <button @click="togglePlaylist" class="flex flex-col items-center gap-1">
                <div class="w-14 h-14 rounded-full bg-primary flex items-center justify-center">
                  <ListMusic :size="28" class="text-white" />
                </div>
                <span class="text-xs font-semibold text-text-primary">{{ currentIndex + 1 }}/{{ playlist.length }}</span>
              </button>
              
              <!-- Next Section -->
              <button @click="nextSong" class="flex flex-col items-center gap-1">
                <SkipForward :size="28" class="text-text-primary" />
                <span class="text-xs text-text-secondary">{{ nextSongName }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ChevronLeft, SkipBack, SkipForward, ListMusic, Music, ChevronsUp } from 'lucide-vue-next'
import { api } from '@/api'
import type { Song } from '@/types'

const router = useRouter()
const route = useRoute()
const currentIndex = ref(0)
const showPlaylist = ref(false)
const playlist = ref<Song[]>([])
const loading = ref(false)

// 加载当前播放列表
const loadPlaylist = async () => {
  loading.value = true
  try {
    playlist.value = await api.getCurrentPlaylist()
    
    // 根据路由参数中的歌曲ID找到对应的索引
    const songId = route.params.id as string
    if (songId) {
      const index = playlist.value.findIndex(song => song.id === songId)
      if (index !== -1) {
        currentIndex.value = index
      }
    }
  } catch (error) {
    console.error('Failed to load playlist:', error)
  } finally {
    loading.value = false
  }
}

// 监听路由变化，更新当前歌曲索引
watch(() => route.params.id, (newId) => {
  if (newId && playlist.value.length > 0) {
    const index = playlist.value.findIndex(song => song.id === newId)
    if (index !== -1) {
      currentIndex.value = index
    }
  }
})

onMounted(() => {
  loadPlaylist()
})

const currentSong = computed(() => playlist.value[currentIndex.value])

const prevSongName = computed(() => {
  const prevIdx = currentIndex.value - 1
  return prevIdx >= 0 ? playlist.value[prevIdx]?.title || '' : ''
})

const nextSongName = computed(() => {
  const nextIdx = currentIndex.value + 1
  return nextIdx < playlist.value.length ? playlist.value[nextIdx]?.title || '' : ''
})

const goBack = () => {
  router.back()
}

const prevSong = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    // 更新路由，但不添加历史记录
    router.replace({ name: 'NowPlaying', params: { id: playlist.value[currentIndex.value].id } })
  }
}

const nextSong = () => {
  if (currentIndex.value < playlist.value.length - 1) {
    currentIndex.value++
    // 更新路由，但不添加历史记录
    router.replace({ name: 'NowPlaying', params: { id: playlist.value[currentIndex.value].id } })
  }
}

const togglePlaylist = () => {
  showPlaylist.value = !showPlaylist.value
}

const playSongAtIndex = (idx: number) => {
  currentIndex.value = idx
  showPlaylist.value = false
  // 更新路由
  router.replace({ name: 'NowPlaying', params: { id: playlist.value[idx].id } })
}
</script>

<style scoped>
.drawer-enter-active,
.drawer-leave-active {
  transition: all 0.3s ease;
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}

.drawer-enter-from .absolute.bottom-0,
.drawer-leave-to .absolute.bottom-0 {
  transform: translateY(100%);
}
</style>
