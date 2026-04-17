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
    <header class="bg-background-card/95 backdrop-blur-xl px-4 py-3 flex items-center relative">
      <button @click="goBack" class="p-1 flex-shrink-0">
        <ChevronLeft :size="24" class="text-text-primary" />
      </button>
      <div class="flex-1 min-w-0 flex items-center justify-center gap-2 px-2">
        <h1 class="text-lg font-semibold truncate">{{ currentSong?.title }}</h1>
        <span v-if="currentSong?.artist" class="text-sm text-text-secondary whitespace-nowrap">{{ currentSong.artist }}</span>
      </div>
      <!-- Right Actions: Favorite + Add to Playlist -->
      <div class="flex items-center gap-1 flex-shrink-0">
        <button
          @click="toggleFavorite"
          class="p-1.5 rounded-lg transition-colors"
          :disabled="togglingFav"
        >
          <Heart
            :size="20"
            :class="isFavorited
              ? 'text-red-400 fill-red-400'
              : 'text-text-secondary'"
          />
        </button>
        <button
          @click="openAddToPlaylistDialog"
          class="p-1.5 rounded-lg transition-colors"
        >
          <Plus :size="20" class="text-text-secondary" />
        </button>
      </div>
    </header>

    <!-- Song Info Bar -->
    <div v-if="currentSong?.meta" class="bg-background-card/50 px-4 py-3 border-b border-white/5">
      <div class="text-xs text-text-secondary text-center">
        {{ currentSong.meta }}
      </div>
    </div>

    <!-- Chord Sheet Content -->
    <div
      ref="sheetContainer"
      class="px-4 py-6 overflow-y-auto overflow-x-hidden"
      :class="fromPlaylist ? 'pb-32' : 'pb-8'"
      :style="fromPlaylist ? 'height: calc(100vh - 180px)' : 'height: calc(100vh - 100px)'"
    >
      <!-- HTML渲染的吉他谱内容：动态缩放铺满一屏，居中 -->
      <div 
        v-if="currentSong?.tabContent" 
        ref="sheetContent"
        class="tab-content-html"
        :style="{
          width: 'fit-content',
          margin: '0 auto',
          transformOrigin: 'top center',
          transform: `scale(${contentScale})`,
        }"
        v-html="currentSong.tabContent"
      ></div>
      
      <!-- 如果没有tabContent，显示占位符 -->
      <div v-else class="text-center text-text-secondary py-12">
        <p>暂无吉他谱内容</p>
      </div>
    </div>

    <!-- Bottom Control Bar - Only show when from playlist -->
    <div v-if="fromPlaylist" class="fixed bottom-0 left-0 right-0 bg-background-card/95 backdrop-blur-xl px-4 py-6 border-t border-white/5">
      <div class="flex items-center justify-between gap-5">
        <!-- Prev Section -->
        <button @click="prevSong" class="flex flex-col items-center gap-1" :disabled="currentIndex === 0">
          <SkipBack :size="28" :class="currentIndex === 0 ? 'text-text-secondary/30' : 'text-text-primary'" />
          <span class="text-xs text-text-secondary truncate max-w-[80px]">{{ prevSongName }}</span>
        </button>
        
        <!-- List Button -->
        <button @click="togglePlaylist" class="flex flex-col items-center gap-1">
          <div class="w-14 h-14 rounded-full bg-primary flex items-center justify-center">
            <ListMusic :size="28" class="text-white" />
          </div>
          <span class="text-xs font-semibold text-text-primary">{{ currentIndex + 1 }}/{{ playlist.length }}</span>
        </button>
        
        <!-- Next Section -->
        <button @click="nextSong" class="flex flex-col items-center gap-1" :disabled="currentIndex === playlist.length - 1">
          <SkipForward :size="28" :class="currentIndex === playlist.length - 1 ? 'text-text-secondary/30' : 'text-text-primary'" />
          <span class="text-xs text-text-secondary truncate max-w-[80px]">{{ nextSongName }}</span>
        </button>
      </div>
    </div>

    <!-- Playlist Drawer - Only show when from playlist -->
    <Transition name="drawer">
      <div v-if="showPlaylist && fromPlaylist" class="fixed inset-0 z-50" @click.self="togglePlaylist">
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
                
                <div class="w-10 h-10 rounded-lg overflow-hidden flex-shrink-0 bg-background-overlay/30 flex items-center justify-center">
                  <Music :size="20" class="text-primary" />
                </div>
                
                <div class="flex-1 min-w-0">
                  <h3 
                    class="text-sm font-medium truncate"
                    :class="idx === currentIndex ? 'text-primary' : 'text-text-primary'"
                  >
                    {{ song.title }}
                  </h3>
                  <p class="text-xs text-text-secondary truncate">
                    {{ song.artist }}
                    <span v-if="song.playKey"> · {{ song.playKey }}调</span>
                  </p>
                </div>
                
                <Music 
                  v-if="idx === currentIndex"
                  :size="20" 
                  class="text-primary flex-shrink-0 animate-pulse"
                />
              </div>
            </div>
          </div>
          
          <!-- Bottom Control Bar in Drawer -->
          <div class="px-4 py-3 border-t border-white/5">
            <div class="flex items-center justify-between gap-5">
              <!-- Prev Section -->
              <button @click="prevSong" class="flex flex-col items-center gap-1" :disabled="currentIndex === 0">
                <SkipBack :size="28" :class="currentIndex === 0 ? 'text-text-secondary/30' : 'text-text-primary'" />
                <span class="text-xs text-text-secondary truncate max-w-[80px]">{{ prevSongName }}</span>
              </button>
              
              <!-- List Button -->
              <button @click="togglePlaylist" class="flex flex-col items-center gap-1">
                <div class="w-14 h-14 rounded-full bg-primary flex items-center justify-center">
                  <ListMusic :size="28" class="text-white" />
                </div>
                <span class="text-xs font-semibold text-text-primary">{{ currentIndex + 1 }}/{{ playlist.length }}</span>
              </button>
              
              <!-- Next Section -->
              <button @click="nextSong" class="flex flex-col items-center gap-1" :disabled="currentIndex === playlist.length - 1">
                <SkipForward :size="28" :class="currentIndex === playlist.length - 1 ? 'text-text-secondary/30' : 'text-text-primary'" />
                <span class="text-xs text-text-secondary truncate max-w-[80px]">{{ nextSongName }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
    <!-- Add to Playlist Dialog -->
    <Transition name="dialog">
      <div v-if="showAddDialog" class="fixed inset-0 z-50 flex flex-col" @click.self="closeAddDialog">
        <div class="absolute inset-0 bg-black/60" @click="closeAddDialog"></div>
        <div class="relative w-full bg-background-card rounded-t-3xl p-6 flex flex-col mt-[180px] flex-1">
          <div class="flex justify-center mb-4">
            <div class="w-10 h-1 bg-text-secondary/30 rounded-full"></div>
          </div>
          <h2 class="text-xl font-semibold mb-4">添加到歌单</h2>
          <div class="flex-1 overflow-y-auto space-y-2 mb-4 pb-[100px]">
            <button
              v-for="pl in userPlaylists"
              :key="pl.id"
              @click="addSongToPlaylist(pl.id)"
              :disabled="addingToPlaylist"
              class="w-full bg-background-overlay/50 rounded-xl p-3 flex items-center gap-3 hover:bg-background-overlay transition-colors disabled:opacity-50"
            >
              <div
                class="w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0"
                :style="{ background: `linear-gradient(135deg, ${pl.gradient?.[0] || '#666'}, ${pl.gradient?.[1] || '#333'})` }"
              >
                <ListMusic :size="20" class="text-white" />
              </div>
              <div class="flex-1 text-left">
                <p class="font-medium">{{ pl.name }}</p>
                <p class="text-xs text-text-secondary">{{ pl.songCount }} 首</p>
              </div>
            </button>
            <div v-if="userPlaylists.length === 0" class="text-center py-8">
              <p class="text-text-secondary text-sm">暂无歌单</p>
            </div>
          </div>
          <div v-if="addMsg" class="rounded-xl p-4 mb-4" :class="addMsgOk ? 'bg-green-500/10 border border-green-500/20' : 'bg-red-500/10 border border-red-500/20'">
            <p class="text-sm text-center" :class="addMsgOk ? 'text-green-400' : 'text-red-400'">{{ addMsg }}</p>
          </div>
        </div>
      </div>
    </Transition>

    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ChevronLeft, SkipBack, SkipForward, ListMusic, Music, Heart, Plus } from 'lucide-vue-next'
import { api } from '@/api'
import { isLoggedIn } from '@/auth'
import { requireLogin } from '@/authBus'
import type { Song, Playlist as PlaylistType } from '@/types'

const router = useRouter()
const route = useRoute()
const currentIndex = ref(0)
const showPlaylist = ref(false)
const playlist = ref<Song[]>([])
const loading = ref(false)

// ---- 内容自适应缩放 ----
const sheetContainer = ref<HTMLElement | null>(null)
const sheetContent = ref<HTMLElement | null>(null)
const contentScale = ref(1)

/**
 * 计算内容缩放比：
 * - 内容高度 < 容器可用高度 → 放大到刚好铺满
 * - 内容高度 >= 容器可用高度 → 保持原始大小，正常滚动
 */
const calcContentScale = () => {
  // 先重置为 1 以获取真实尺寸
  contentScale.value = 1
  nextTick(() => {
    const container = sheetContainer.value
    const content = sheetContent.value
    if (!container || !content) return

    const cs = getComputedStyle(container)
    const padY = parseFloat(cs.paddingTop) + parseFloat(cs.paddingBottom)
    const availH = container.clientHeight - padY
    const naturalH = content.scrollHeight

    if (naturalH > 0 && naturalH < availH) {
      contentScale.value = availH / naturalH
    }
  })
}

// 窗口大小变化时重新计算
const onResize = () => calcContentScale()
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

// 收藏相关
const favoriteIds = ref<Set<string>>(new Set())
const togglingFav = ref(false)
const isFavorited = computed(() => {
  const song = playlist.value[currentIndex.value]
  return song ? favoriteIds.value.has(song.id) : false
})

// 添加到歌单相关
const showAddDialog = ref(false)
const userPlaylists = ref<PlaylistType[]>([])
const addingToPlaylist = ref(false)
const addMsg = ref('')
const addMsgOk = ref(false)

// 判断是否来自歌单
const fromPlaylist = computed(() => {
  return route.query.playlistId !== undefined
})

// 加载播放列表
const loadPlaylist = async () => {
  loading.value = true
  try {
    const songId = route.params.id as string
    const playlistId = route.query.playlistId as string
    
    if (playlistId) {
      // 来自歌单，加载该歌单的歌曲
      playlist.value = await api.getPlaylistSongs(playlistId)
    } else {
      // 来自谱库或其他地方，只加载当前歌曲
      const song = await api.getSongById(songId)
      if (song) {
        playlist.value = [song]
      }
    }
    
    // 找到当前歌曲在列表中的索引
    if (songId && playlist.value.length > 0) {
      const index = playlist.value.findIndex(song => song.id === songId)
      if (index !== -1) {
        currentIndex.value = index
      }
    }
  } catch (error) {
    console.error('Failed to load playlist:', error)
  } finally {
    loading.value = false
    // 加载完成后计算内容缩放
    nextTick(() => calcContentScale())
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
  loadFavoriteIds()
})

const currentSong = computed(() => playlist.value[currentIndex.value])

// 歌曲切换时重新计算缩放
watch(currentSong, () => {
  nextTick(() => calcContentScale())
})

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
    const playlistId = route.query.playlistId as string
    // 更新路由，保持 playlistId 参数
    router.replace({ 
      name: 'NowPlaying', 
      params: { id: playlist.value[currentIndex.value].id },
      query: playlistId ? { playlistId } : {}
    })
  }
}

const nextSong = () => {
  if (currentIndex.value < playlist.value.length - 1) {
    currentIndex.value++
    const playlistId = route.query.playlistId as string
    // 更新路由，保持 playlistId 参数
    router.replace({ 
      name: 'NowPlaying', 
      params: { id: playlist.value[currentIndex.value].id },
      query: playlistId ? { playlistId } : {}
    })
  }
}

const togglePlaylist = () => {
  showPlaylist.value = !showPlaylist.value
}

const playSongAtIndex = (idx: number) => {
  currentIndex.value = idx
  showPlaylist.value = false
  const playlistId = route.query.playlistId as string
  // 更新路由，保持 playlistId 参数
  router.replace({ 
    name: 'NowPlaying', 
    params: { id: playlist.value[idx].id },
    query: playlistId ? { playlistId } : {}
  })
}

// ---- 收藏 ----
const loadFavoriteIds = async () => {
  try {
    const ids = await api.getFavoriteSongIds()
    favoriteIds.value = new Set(ids)
  } catch (e) {
    console.error('Failed to load favorite ids:', e)
  }
}

const toggleFavorite = async () => {
  if (!isLoggedIn()) {
    requireLogin(() => toggleFavorite())
    return
  }
  const song = playlist.value[currentIndex.value]
  if (!song) return
  togglingFav.value = true
  try {
    if (isFavorited.value) {
      await api.unfavoriteSong(song.id)
      favoriteIds.value.delete(song.id)
    } else {
      await api.favoriteSong(song.id)
      favoriteIds.value.add(song.id)
    }
    favoriteIds.value = new Set(favoriteIds.value)
  } catch (e) {
    console.error('Failed to toggle favorite:', e)
  } finally {
    togglingFav.value = false
  }
}

// ---- 添加到歌单 ----
const openAddToPlaylistDialog = async () => {
  if (!isLoggedIn()) {
    requireLogin(() => openAddToPlaylistDialog())
    return
  }
  showAddDialog.value = true
  addMsg.value = ''
  try {
    userPlaylists.value = await api.getPlaylistsByType('user')
  } catch (e) {
    console.error('Failed to load playlists:', e)
  }
}

const closeAddDialog = () => {
  showAddDialog.value = false
  addMsg.value = ''
}

const addSongToPlaylist = async (plId: string) => {
  const song = playlist.value[currentIndex.value]
  if (!song) return
  addingToPlaylist.value = true
  addMsg.value = ''
  try {
    await api.addSongToPlaylist(plId, song.id)
    addMsg.value = '添加成功！'
    addMsgOk.value = true
    setTimeout(closeAddDialog, 1000)
  } catch (e: any) {
    addMsg.value = e.message || '添加失败'
    addMsgOk.value = false
  } finally {
    addingToPlaylist.value = false
  }
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
  transform: translateY(100%);
}

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

/* 吉他谱HTML内容样式 */
.tab-content-html {
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  line-height: 1.6;
  color: var(--text-primary);
}

/* ============================================ */
/* 和弦显示样式 - 关键！ */
/* ============================================ */

/* 和弦容器 - 使用相对定位作为参考点 */
.tab-content-html :deep(hexi-chord) {
  position: relative !important;
  display: inline !important;
  vertical-align: baseline !important;
  margin: 0 !important;
  padding: 0 !important;
  line-height: 1 !important;
  width: 0 !important;
  min-width: 0 !important;
  max-width: 0 !important;
  overflow: visible !important;
  font-size: 0 !important;
}

/* 和弦 SVG/Text - 绝对定位到文字上方 */
.tab-content-html :deep(hexi-chord svg) {
  position: absolute;
  left: 50%;
  bottom: 100%;
  transform: translateX(-50%);
  margin-bottom: 8px;
  display: block;
  z-index: 10;
  font-size: 14px;
  font-weight: bold;
  color: #409eff;
}

/* 和弦文字样式 */
.tab-content-html :deep(hexi-chord svg text) {
  fill: #409eff;
  font-weight: bold;
  font-size: 14px;
}

/* 整体内容区域 */
.tab-content-html :deep(.xhe-body) {
  position: relative;
  line-height: 2.5;
  padding-top: 0;
}

/* 每一行内容 */
.tab-content-html :deep(xhe-headline),
.tab-content-html :deep(xhe-line) {
  position: relative;
  display: block;
  line-height: 2.5;
  margin: 0;
  padding: 0;
}

/* 歌词文本正常显示 */
.tab-content-html :deep(.text) {
  display: inline-block;
  vertical-align: baseline;
}

/* 和弦后面紧跟的空 text div - 不占空间 */
.tab-content-html :deep(xhe-chord-anchor .text[text-value=""]) {
  display: inline !important;
  width: 0 !important;
  margin: 0 -4px 0 0;
  padding: 0 !important;
}

/* xhe-chord-anchor 容器也不占空间 */
.tab-content-html :deep(xhe-chord-anchor) {
  display: inline !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* ============================================ */
/* 通用样式 */
/* ============================================ */

.tab-content-html :deep(h1),
.tab-content-html :deep(h2),
.tab-content-html :deep(h3),
.tab-content-html :deep(h4) {
  color: var(--primary);
  margin: 1rem 0 0.5rem 0;
  font-weight: 600;
}

.tab-content-html :deep(p) {
  margin: 0.5rem 0;
}

.tab-content-html :deep(pre) {
  background: rgba(255, 255, 255, 0.05);
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  color: var(--primary);
  margin: 0;
  white-space: pre-wrap;
}

.tab-content-html :deep(code) {
  font-family: 'Courier New', monospace;
  color: var(--primary);
}

.tab-content-html :deep(a) {
  color: var(--primary);
  text-decoration: underline;
}

/* 确保包含和弦的段落有足够空间 */
.tab-content-html :deep(.xhe-sheet) {
  padding: 10px;
  font-size: 16px;
}

.tab-content-html :deep(.xhe-header) {
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tab-content-html :deep(.xhe-title) {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 6px;
  text-align: center;
  color: var(--text-primary);
}

.tab-content-html :deep(.xhe-info) {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.tab-content-html :deep(.xhe-meta) {
  display: flex;
  justify-content: center;
  gap: 10px;
  font-size: 11px;
  color: var(--text-secondary);
}

.tab-content-html :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
}

.tab-content-html :deep(th),
.tab-content-html :deep(td) {
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 0.5rem;
  text-align: left;
}

.tab-content-html :deep(th) {
  background: rgba(255, 255, 255, 0.05);
  font-weight: 600;
}
</style>
