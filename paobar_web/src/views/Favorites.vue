<template>
  <!-- 扣掉 iOS 状态栏高度：#app 已经 padding-top 了 --status-height，
       这里再用 100vh 就会超出屏幕底部 --status-height。 -->
  <div class="flex flex-col overflow-hidden" style="height: calc(100dvh - var(--status-height));">
    <!-- Header -->
    <header class="flex-shrink-0 bg-background-card/95 backdrop-blur-xl px-4 py-4">
      <div class="flex items-center relative">
        <button @click="$router.back()" class="p-1 absolute left-0">
          <ChevronLeft :size="24" class="text-text-primary" />
        </button>
        <h1 class="text-lg font-semibold text-center w-full">收藏的歌曲</h1>
      </div>
    </header>

    <!-- Song List -->
    <div class="flex-1 min-h-0 overflow-y-auto px-4 py-4 space-y-3">
      <!-- Loading -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-text-secondary text-sm mt-3">加载中...</p>
      </div>

      <!-- Empty -->
      <div v-else-if="songs.length === 0" class="text-center py-20">
        <Heart :size="48" class="mx-auto text-text-secondary/30 mb-4" />
        <p class="text-text-secondary text-sm">还没有收藏的歌曲</p>
        <p class="text-text-secondary/60 text-xs mt-1">在歌曲详情中点击收藏按钮添加</p>
      </div>

      <!-- Song Items -->
      <div
        v-else
        v-for="song in songs"
        :key="song.id"
        @click="goToSong(song)"
        class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 flex items-center gap-3 active:scale-[0.98] transition-transform cursor-pointer"
      >
        <div class="flex-1 min-w-0">
          <div class="font-medium text-text-primary truncate">{{ song.title }}</div>
          <div class="text-sm text-text-secondary truncate mt-0.5">{{ song.artist }}</div>
        </div>
        <button
          @click.stop="unfavorite(song)"
          class="p-2 rounded-xl hover:bg-red-500/10 transition-colors"
          :disabled="removingId === song.id"
        >
          <HeartOff v-if="removingId === song.id" :size="18" class="text-text-secondary animate-pulse" />
          <Heart v-else :size="18" class="text-red-400 fill-red-400" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronLeft, Heart, HeartOff } from 'lucide-vue-next'
import { api } from '@/api'
import type { Song } from '@/types'

const router = useRouter()
const songs = ref<Song[]>([])
const loading = ref(false)
const removingId = ref<string | null>(null)

const loadFavorites = async () => {
  loading.value = true
  try {
    songs.value = await api.getFavoriteSongs()
  } catch (error) {
    console.error('Failed to load favorites:', error)
  } finally {
    loading.value = false
  }
}

const goToSong = (song: Song) => {
  router.push(`/now-playing/${song.id}`)
}

const unfavorite = async (song: Song) => {
  removingId.value = song.id
  try {
    const success = await api.unfavoriteSong(song.id)
    if (success) {
      songs.value = songs.value.filter(s => s.id !== song.id)
    }
  } catch (error) {
    console.error('Failed to unfavorite:', error)
  } finally {
    removingId.value = null
  }
}

onMounted(() => {
  loadFavorites()
})
</script>
