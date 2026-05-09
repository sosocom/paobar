<template>
  <!-- 与 Favorites 保持一致：100dvh 减掉状态栏高度，防止顶部被电池栏遮挡 -->
  <div class="flex flex-col overflow-hidden" style="height: calc(100dvh - var(--status-height));">
    <!-- Header -->
    <header class="flex-shrink-0 bg-background-card/95 backdrop-blur-xl px-4 py-4">
      <div class="flex items-center relative">
        <button @click="$router.back()" class="p-1 absolute left-0">
          <ChevronLeft :size="24" class="text-text-primary" />
        </button>
        <h1 class="text-lg font-semibold text-center w-full">播放历史</h1>
        <!-- 只有有数据时才显示"清空"按钮，避免空状态下多一个无用按钮 -->
        <button
          v-if="songs.length > 0 && !loading"
          @click="onClear"
          class="absolute right-0 p-1 text-sm text-text-secondary hover:text-red-400 transition-colors"
          :disabled="clearing"
        >
          {{ clearing ? '清除中' : '清空' }}
        </button>
      </div>
    </header>

    <!-- Song List -->
    <div class="flex-1 min-h-0 overflow-y-auto px-4 py-4 space-y-3">
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-text-secondary text-sm mt-3">加载中...</p>
      </div>

      <div v-else-if="songs.length === 0" class="text-center py-20">
        <History :size="48" class="mx-auto text-text-secondary/30 mb-4" />
        <p class="text-text-secondary text-sm">还没有播放历史</p>
        <p class="text-text-secondary/60 text-xs mt-1">打开任意一首曲谱都会自动记录到这里</p>
      </div>

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
          @click.stop="removeOne(song)"
          class="p-2 rounded-xl hover:bg-red-500/10 transition-colors"
          :disabled="removingId === song.id"
          aria-label="从历史中移除"
        >
          <Trash2 :size="18" :class="removingId === song.id ? 'text-text-secondary animate-pulse' : 'text-text-secondary hover:text-red-400'" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronLeft, History, Trash2 } from 'lucide-vue-next'
import { api } from '@/api'
import type { Song } from '@/types'

const router = useRouter()
const songs = ref<Song[]>([])
const loading = ref(false)
const removingId = ref<string | null>(null)
const clearing = ref(false)

const loadHistory = async () => {
  loading.value = true
  try {
    songs.value = await api.getPlayHistory(100)
  } catch (error) {
    console.error('Failed to load play history:', error)
  } finally {
    loading.value = false
  }
}

const goToSong = (song: Song) => {
  router.push(`/now-playing/${song.id}`)
}

const removeOne = async (song: Song) => {
  removingId.value = song.id
  try {
    const ok = await api.deletePlayHistory(song.id)
    if (ok) {
      songs.value = songs.value.filter(s => s.id !== song.id)
    }
  } finally {
    removingId.value = null
  }
}

const onClear = async () => {
  if (clearing.value) return
  // 用原生 confirm 足够——移动端清空历史是破坏性操作，必须二次确认
  if (!window.confirm('确定要清空全部播放历史吗？此操作不可撤销。')) return
  clearing.value = true
  try {
    const ok = await api.clearPlayHistory()
    if (ok) {
      songs.value = []
    }
  } finally {
    clearing.value = false
  }
}

onMounted(() => {
  loadHistory()
})
</script>
