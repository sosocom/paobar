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
      // 限制最大放大倍数，避免短谱被撑得过大（和弦名变形、丢失原版观感）
      const MAX_SCALE = 1.35
      contentScale.value = Math.min(availH / naturalH, MAX_SCALE)
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

/* ================================================================= *
 * 吉他谱渲染 —— 对齐 yopu.co 原版 xhe-sheet 布局
 *   实际 HTML 结构（来自 SeleniumCrawler 保存）:
 *     .sheet-container > .xhe-sheet
 *       > .sheet-header  .title / .info .item{.label,.text} / .meta .col{.label,.value}
 *       > .xhe-body
 *           <xhe-headline><div text-value>段落名</div></xhe-headline>
 *           <xhe-text>歌词...</xhe-text>
 *           <xhe-chord-anchor data-chord="1">
 *             <div class="chord">1<span class="chord-type">m</span></div>
 *             <div class="text">字</div>
 *           </xhe-chord-anchor>
 *           <xhe-line-break><br></xhe-line-break>
 *   主色覆盖原版 --color-accent，统一使用项目红色主调
 * ================================================================= */
.tab-content-html {
  --accent: #ef4444;        /* 和弦主色（暗背景下比 #DC2626 更易读） */
  --accent-soft: rgba(239, 68, 68, 0.14);
  --meta-border: rgba(255, 255, 255, 0.14);
  --headline-bg: rgba(255, 255, 255, 0.04);
  --underline: rgba(255, 255, 255, 0.22);
  --color-font-main: var(--text-primary, #fafafa);
  --color-font-secondary: #d4d4d8;
  --color-font-tertiary: #a1a1aa;

  font-family: 'Hiragino Sans GB', 'Microsoft YaHei', 'PingFang SC', Arial, sans-serif;
  font-size: 17px;
  color: var(--color-font-main);
  line-height: 1.5;
  letter-spacing: 0.03em;
  padding: 22px 24px 48px;
  max-width: 820px;
}

/* ---------- 外层容器 ---------- */
.tab-content-html :deep(.sheet-container),
.tab-content-html :deep(.xhe-sheet) {
  position: relative;
  padding: 0;
  background: transparent;
}

/* ---------- sheet-header：标题 + 演唱 + meta 方块（参考 yopu 桌面布局） ---------- */
.tab-content-html :deep(.sheet-header) {
  position: relative;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px dashed var(--meta-border);
}

.tab-content-html :deep(.sheet-header .title) {
  font-size: 1.7em;
  font-weight: 600;
  letter-spacing: 1px;
  margin: 4px 0 8px;
  color: var(--color-font-main);
  word-break: break-all;
}

.tab-content-html :deep(.sheet-header .info) {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 24px;
  margin-bottom: 10px;
}

.tab-content-html :deep(.sheet-header .info .item .label) {
  margin-right: 6px;
  font-size: 0.9em;
  color: var(--accent);
}

.tab-content-html :deep(.sheet-header .info .item .text) {
  color: var(--color-font-secondary);
  font-size: 0.95em;
}

/* meta 方块（4 格：拍号 / 拍速 / 选调 / 原唱调）——
   正常流，放在标题下方，一行 4 列，避免与标题重叠 */
.tab-content-html :deep(.sheet-header .meta) {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0;
  margin-top: 6px;
  padding: 6px 4px;
  border: 1px solid var(--meta-border);
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.03);
}

.tab-content-html :deep(.sheet-header .meta .col) {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  box-sizing: border-box;
  padding: 2px 8px;
  font-size: 0.85em;
  line-height: 1.8em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 列间竖向分隔线（第 2、3、4 列） */
.tab-content-html :deep(.sheet-header .meta .col + .col) {
  border-left: 1px solid var(--meta-border);
}

.tab-content-html :deep(.sheet-header .meta .col .label) {
  color: var(--color-font-tertiary);
}

.tab-content-html :deep(.sheet-header .meta .col .value) {
  color: var(--color-font-main);
  font-weight: 500;
}

/* 窄屏：4 列改 2 列，保持每格一行内完整显示 */
@media (max-width: 520px) {
  .tab-content-html :deep(.sheet-header .title) {
    font-size: 1.4em;
  }
  .tab-content-html :deep(.sheet-header .meta) {
    grid-template-columns: repeat(2, 1fr);
  }
  .tab-content-html :deep(.sheet-header .meta .col + .col) {
    border-left: none;
  }
  .tab-content-html :deep(.sheet-header .meta .col:nth-child(odd)) {
    border-right: 1px solid var(--meta-border);
  }
  .tab-content-html :deep(.sheet-header .meta .col:nth-child(-n+2)) {
    border-bottom: 1px solid var(--meta-border);
  }
}

/* 如果谱体没有 sheet-header（老数据），我们自己的 xhe-header 占位继续生效 */
.tab-content-html :deep(.xhe-header) {
  display: none;
}

/* ---------- xhe-body 正文 ---------- */
.tab-content-html :deep(.xhe-body) {
  position: relative;
  padding: 32px 0 0;   /* 顶部给第一行的和弦文本留出空间 */
}

/* 段落标题（主歌 / 副歌 / Bridge 等） */
.tab-content-html :deep(xhe-headline) {
  display: block;
  position: relative;
  margin: 1.4em -0.5em 0.7em;
  padding: 0.3em 0.7em;
  background-color: var(--headline-bg);
  border-left: 3px solid var(--accent);
  border-radius: 4px;
}

.tab-content-html :deep(xhe-headline [text-value]) {
  display: inline-block;
  line-height: 1.7em;
  font-size: 1em;
  color: var(--color-font-secondary);
  letter-spacing: 0.15em;
}

/* 第一个 headline 不要顶端 margin（避免与 xhe-body 内边距叠加） */
.tab-content-html :deep(.xhe-body > xhe-headline:first-child) {
  margin-top: 0;
}

/* ---------- 歌词行 ---------- */
.tab-content-html :deep(xhe-text) {
  display: inline;
  line-height: 3.5em;          /* 关键：预留和弦上方空间 */
  color: var(--color-font-main);
  letter-spacing: 0.07em;
}

/* xhe-line-break 内部有 <br>，让整个元素占一行强制换行 */
.tab-content-html :deep(xhe-line-break) {
  display: inline;
}

/* ---------- 和弦锚点 ---------- */
.tab-content-html :deep(xhe-chord-anchor) {
  position: relative;
  display: inline-block;
  box-sizing: border-box;
  padding: 0;
  color: var(--color-font-main);
  letter-spacing: 0.07em;
  vertical-align: baseline;
}

.tab-content-html :deep(xhe-chord-anchor[data-value-length="0"]) {
  min-width: 1em;
  margin-right: 0.15em;
}

/* 和弦文字（数字谱：1 / 6m / 5 / 2m，字母谱：Am / C / G7） */
.tab-content-html :deep(xhe-chord-anchor > .chord) {
  position: absolute;
  top: -1.4em;
  left: 50%;
  transform: translateX(-50%);
  font-family: Arial, 'Helvetica Neue', sans-serif;
  color: var(--accent);
  font-weight: 600;
  font-size: 0.95em;
  letter-spacing: 0;
  white-space: nowrap;
  user-select: none;
  line-height: 1;
  transition: background-color 1s;
}

.tab-content-html :deep(xhe-chord-anchor > .chord .chord-type) {
  font-size: 0.8em;
}

/* 按住锚点时高亮（桌面端鼠标、移动端触摸） */
.tab-content-html :deep(xhe-chord-anchor:active > .chord) {
  background-color: var(--accent-soft);
  border-radius: 3px;
  transition: background-color 0s;
}

/* 歌词字下方的细下划线（标记和弦对应字） */
.tab-content-html :deep(xhe-chord-anchor > .text) {
  display: inline-block;
  line-height: 1em;
  text-align: center;
  border-bottom: 0.07em solid var(--underline);
  padding: 0.07em 0 0.21em;
}

/* ---------- 字母谱可能出现的 hexi-chord 指法图（为将来扩展保留） ---------- */
.tab-content-html :deep(xhe-chord-anchor hexi-chord) {
  position: absolute;
  top: -3.2em;
  color: var(--accent);
  user-select: none;
  line-height: 1;
}

.tab-content-html :deep(xhe-chord-anchor hexi-chord[instrument="guitar"]) {
  left: -1em;
}

.tab-content-html :deep(hexi-chord svg) {
  display: block;
  overflow: visible;
}

.tab-content-html :deep(hexi-chord svg text),
.tab-content-html :deep(hexi-chord svg *[fill]) {
  fill: currentColor;
}

/* 选区高亮 */
.tab-content-html :deep(xhe-chord-anchor [text-value]::selection),
.tab-content-html :deep(xhe-headline::selection),
.tab-content-html :deep(xhe-text::selection) {
  background-color: var(--accent-soft);
}

/* ---------- 兜底：残留标签 ---------- */
.tab-content-html :deep(a) {
  color: var(--accent);
  text-decoration: underline;
}

.tab-content-html :deep(pre),
.tab-content-html :deep(code) {
  font-family: 'Courier New', monospace;
  color: var(--accent);
}

.tab-content-html :deep(pre) {
  background: rgba(255, 255, 255, 0.05);
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  font-size: 0.875rem;
  line-height: 1.5;
  margin: 0;
  white-space: pre-wrap;
}
</style>
