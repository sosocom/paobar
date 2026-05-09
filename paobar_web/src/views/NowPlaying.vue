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
      <!-- Right Actions: Drum Machine + Chord Mode Toggle + Favorite + Add to Playlist -->
      <div class="flex items-center gap-1 flex-shrink-0">
        <button
          @click="toggleDrumMachine"
          class="p-1.5 rounded-lg transition-colors"
          :title="showDrumMachine ? '收起鼓机' : '展开鼓机'"
          aria-label="鼓机"
        >
          <Drum
            :size="20"
            :class="showDrumMachine ? 'text-primary' : 'text-text-secondary'"
          />
        </button>
        <button
          @click="toggleChordMode"
          class="p-1.5 rounded-lg transition-colors"
          :title="chordMode === 'diagram' ? '切换到级数图' : '切换到和弦图'"
        >
          <Guitar
            :size="20"
            :class="chordMode === 'diagram' ? 'text-primary' : 'text-text-secondary'"
          />
        </button>
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

    <!-- 鼓机面板：懒加载（只有第一次展开才下载 tone.js + 组件代码）。
         不依赖 currentSong —— Tone 引擎在 drumBus 里全局常驻，切歌瞬间 currentSong
         会短暂变 undefined，但组件保持挂载可以避免视觉闪烁；DrumMachine 内部
         watch 空 props 时会把 null 传给 bus.setSongContext，由 bus 自行兜底。 -->
    <Transition name="drum-slide">
      <DrumMachine
        v-if="showDrumMachine"
        :meter="currentSong?.tabDocument?.meter ?? null"
        :bpm-raw="currentSong?.tabDocument?.bpm ?? null"
        :song-id="currentSong?.id ?? null"
      />
    </Transition>

    <!-- Chord Sheet Content -->
    <div
      ref="sheetContainer"
      class="px-4 pt-1 pb-6 overflow-y-auto overflow-x-hidden"
      :class="fromPlaylist ? 'pb-16' : 'pb-8'"
      :style="sheetHeightStyle"
    >
      <!-- 按需拉取当前歌曲详情时的小 loading -->
      <div v-if="detailLoading" class="text-center text-text-secondary py-8">
        <div class="inline-block w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-xs mt-2">正在加载吉他谱...</p>
      </div>

      <!-- 结构化渲染的吉他谱：动态缩放铺满一屏，居中 -->
      <div
        v-else-if="currentSong?.tabDocument"
        ref="sheetContent"
        class="tab-content-html"
        :class="chordMode === 'diagram' ? 'mode-diagram' : 'mode-number'"
        :style="{
          width: 'fit-content',
          margin: '0 auto',
          transformOrigin: 'top center',
          transform: `scale(${contentScale})`,
        }"
      >
        <div class="xhe-sheet">
          <!-- 头部信息条：只保留拍号 / 拍速 / 选调 / 原唱调 一行展示。
               标题、唱/词/曲等已在页面顶部 header 体现，不再重复占版面。 -->
          <div v-if="metaCols.length" class="sheet-header">
            <div class="meta">
              <div v-for="col in metaCols" :key="col.label" class="col">
                <span class="label">{{ col.label }}</span>
                <span class="value">{{ col.value }}</span>
              </div>
            </div>
          </div>

          <!-- 谱体：headline / paragraph(segments) / blank -->
          <div class="xhe-body">
            <template
              v-for="(block, i) in currentSong.tabDocument.blocks"
              :key="'block-' + i"
            >
              <xhe-headline v-if="block.type === 'headline'">
                <div text-value>{{ block.text }}</div>
              </xhe-headline>
              <div v-else-if="block.type === 'paragraph'" class="xhe-paragraph">
                <template
                  v-for="(seg, j) in block.segments"
                  :key="'seg-' + i + '-' + j"
                >
                  <xhe-text v-if="seg.type === 'text'">{{ seg.text }}</xhe-text>
                  <xhe-chord-anchor
                    v-else
                    :data-chord="seg.chord"
                    :data-value-length="seg.text.length"
                  >
                    <div class="chord">
                      <ChordDiagram
                        v-if="chordMode === 'diagram'"
                        :chord="seg.chord"
                        :capo-key="currentSong.tabDocument.capoKey || currentSong.tabDocument.originalKey"
                      />
                      <template v-else>{{ seg.chord }}</template>
                    </div>
                    <div class="text">{{ seg.text }}</div>
                  </xhe-chord-anchor>
                </template>
              </div>
              <div v-else class="xhe-blank"></div>
            </template>
          </div>
        </div>
      </div>

      <!-- 没有 tabDocument 时的占位符 -->
      <div v-else class="text-center text-text-secondary py-12">
        <p>暂无吉他谱内容</p>
      </div>
    </div>

    <!-- Bottom Control Bar - Only show when from playlist -->
    <div v-if="fromPlaylist" class="fixed bottom-0 left-0 right-0 bg-background-card/95 backdrop-blur-xl px-3 py-2 border-t border-white/5">
      <div class="flex items-center gap-2">
        <!-- Prev Section -->
        <button
          @click="prevSong"
          class="flex items-center gap-1.5 flex-1 min-w-0 px-2 py-1 rounded-md disabled:opacity-40"
          :disabled="currentIndex === 0"
        >
          <SkipBack :size="16" class="text-text-primary flex-shrink-0" />
          <span class="text-xs text-text-secondary truncate">{{ prevSongName || '上一首' }}</span>
        </button>

        <!-- Center: text-only counter -->
        <button
          @click="togglePlaylist"
          class="px-3 py-1 rounded-full bg-primary/15 text-primary font-semibold text-sm flex-shrink-0"
        >
          {{ currentIndex + 1 }}/{{ playlist.length }}
        </button>

        <!-- Next Section -->
        <button
          @click="nextSong"
          class="flex items-center gap-1.5 flex-1 min-w-0 px-2 py-1 rounded-md justify-end disabled:opacity-40"
          :disabled="currentIndex === playlist.length - 1"
        >
          <span class="text-xs text-text-secondary truncate">{{ nextSongName || '下一首' }}</span>
          <SkipForward :size="16" class="text-text-primary flex-shrink-0" />
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
          <div class="px-3 py-2 border-t border-white/5">
            <div class="flex items-center gap-2">
              <!-- Prev Section -->
              <button
                @click="prevSong"
                class="flex items-center gap-1.5 flex-1 min-w-0 px-2 py-1 rounded-md disabled:opacity-40"
                :disabled="currentIndex === 0"
              >
                <SkipBack :size="16" class="text-text-primary flex-shrink-0" />
                <span class="text-xs text-text-secondary truncate">{{ prevSongName || '上一首' }}</span>
              </button>

              <!-- Center: text-only counter -->
              <button
                @click="togglePlaylist"
                class="px-3 py-1 rounded-full bg-primary/15 text-primary font-semibold text-sm flex-shrink-0"
              >
                {{ currentIndex + 1 }}/{{ playlist.length }}
              </button>

              <!-- Next Section -->
              <button
                @click="nextSong"
                class="flex items-center gap-1.5 flex-1 min-w-0 px-2 py-1 rounded-md justify-end disabled:opacity-40"
                :disabled="currentIndex === playlist.length - 1"
              >
                <span class="text-xs text-text-secondary truncate">{{ nextSongName || '下一首' }}</span>
                <SkipForward :size="16" class="text-text-primary flex-shrink-0" />
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
import { ref, computed, defineAsyncComponent, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { onBeforeRouteLeave, useRouter, useRoute } from 'vue-router'
import { ChevronLeft, SkipBack, SkipForward, ListMusic, Music, Heart, Plus, Guitar, Drum } from 'lucide-vue-next'
import { api } from '@/api'
import { isLoggedIn } from '@/auth'
import { requireLogin } from '@/authBus'
import type { Song, Playlist as PlaylistType, TabDocument } from '@/types'
import ChordDiagram from '@/components/ChordDiagram.vue'
import {
  panelOpen as drumPanelOpen,
  togglePanelOpen as toggleDrumPanel,
  requestStopDrum,
} from '@/drumPanelState'
// 鼓机组件异步加载：tone.js (~70KB) 只在用户真的点开鼓机时才下载。
// NowPlaying 不直接 import drumBus（那个文件 static import 了 Tone），
// 所以 panelOpen / togglePanelOpen 只从 drumPanelState 拿，保持主包体积。
const DrumMachine = defineAsyncComponent(() => import('@/components/DrumMachine.vue'))

const router = useRouter()
const route = useRoute()
const currentIndex = ref(0)
const showPlaylist = ref(false)
const playlist = ref<Song[]>([])
const loading = ref(false)
// 歌单列表接口只返回轻量 DTO（不含 tabDocument），切到该曲时若还没详情则按需拉一次。
const detailLoading = ref(false)

// ---- 和弦展示模式（级数 / 和弦图），偏好持久化到 localStorage ----
const CHORD_MODE_KEY = 'paobar.chord-mode'
type ChordMode = 'number' | 'diagram'
const chordMode = ref<ChordMode>(
  (typeof localStorage !== 'undefined' && (localStorage.getItem(CHORD_MODE_KEY) as ChordMode)) || 'number'
)
const toggleChordMode = () => {
  chordMode.value = chordMode.value === 'number' ? 'diagram' : 'number'
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem(CHORD_MODE_KEY, chordMode.value)
  }
  // 切模式后 line-height 会变，整体内容高度变化，重新算一次缩放。
  nextTick(() => calcContentScale())
}

// ---- 鼓机面板显隐：状态与持久化在 drumBus 里全局管理，
//      这样不同页面/组件共享同一个开关，切歌 / 重挂载都能保持"上次是开的"。
const showDrumMachine = drumPanelOpen
const toggleDrumMachine = () => {
  toggleDrumPanel()
  nextTick(() => calcContentScale())
}

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

const onResize = () => calcContentScale()
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

const favoriteIds = ref<Set<string>>(new Set())
const togglingFav = ref(false)
const isFavorited = computed(() => {
  const song = playlist.value[currentIndex.value]
  return song ? favoriteIds.value.has(song.id) : false
})

const showAddDialog = ref(false)
const userPlaylists = ref<PlaylistType[]>([])
const addingToPlaylist = ref(false)
const addMsg = ref('')
const addMsgOk = ref(false)

const fromPlaylist = computed(() => {
  return route.query.playlistId !== undefined
})

// 鼓机面板近似高度（两行 + 间距 + 可能的提示文案），写死足以覆盖展开态的最大值；
// 小一点的瑕疵是显示 iOS 提示时底部可能稍有富余，视觉上无感。
const DRUM_PANEL_HEIGHT = 110
const sheetHeightStyle = computed(() => {
  // 底部导航做小后实际高度 ~46px（py-2 + 28px 按钮 + 1px border），
  // 给 iOS 安全区/视觉呼吸再多留一点。
  const base = fromPlaylist.value ? 64 : 60
  const extra = showDrumMachine.value ? DRUM_PANEL_HEIGHT : 0
  return `height: calc(100dvh - var(--status-height) - ${base + extra}px)`
})

const loadPlaylist = async () => {
  loading.value = true
  try {
    const songId = route.params.id as string
    const playlistId = route.query.playlistId as string

    if (playlistId) {
      playlist.value = await api.getPlaylistSongs(playlistId)
    } else {
      const song = await api.getSongById(songId)
      if (song) {
        playlist.value = [song]
      }
    }

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
    // 列表接口不带 tabDocument，单曲接口带。这里统一补齐当前曲目的详情。
    await ensureCurrentDetail()
    nextTick(() => calcContentScale())
  }
}

/**
 * 如果当前曲目还没有 tabDocument（典型是从歌单列表进来），
 * 这里按需拉一次 `/api/songs/:id` 并把详情合并回 playlist。
 */
const ensureCurrentDetail = async () => {
  const song = playlist.value[currentIndex.value]
  if (!song || song.tabDocument) return
  detailLoading.value = true
  try {
    const detail = await api.getSongById(song.id)
    if (detail) {
      playlist.value.splice(currentIndex.value, 1, { ...song, ...detail })
    }
  } catch (e) {
    console.error('Failed to load song detail:', e)
  } finally {
    detailLoading.value = false
  }
}

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

// 离开 NowPlaying 路由时让鼓机停下来（切歌仍留在本路由，不会触发）。
// requestStopDrum 走 window event，drumBus 未加载时是空操作，不会把 Tone 拉进主包。
onBeforeRouteLeave((to) => {
  if (to.name !== 'NowPlaying') {
    requestStopDrum()
  }
})

const currentSong = computed(() => playlist.value[currentIndex.value])

// 记录播放历史：每次 currentSong.id 真正变化时（首次加载 / 列表内切歌 / 路由跳转）
// 都向后端 upsert 一条记录。未登录时 api 内部会静默忽略。
// 用一个 Set 去重，防止 watch 触发两次（例如 loadPlaylist + route watch 同时命中）。
const recordedIds = new Set<string>()
watch(() => currentSong.value?.id, (id) => {
  if (!id) return
  if (recordedIds.has(id)) return
  recordedIds.add(id)
  api.recordPlayHistory(id)
}, { immediate: true })

// 顶部信息栏里四格 meta 的展平逻辑，顺序固定为 拍号 / 拍速 / 选调 / 原唱调。
const metaCols = computed<Array<{ label: string; value: string }>>(() => {
  const doc: TabDocument | undefined = currentSong.value?.tabDocument
  if (!doc) return []
  const candidates: Array<{ label: string; value?: string }> = [
    { label: '拍号', value: doc.meter },
    { label: '拍速', value: doc.bpm },
    { label: '选调', value: doc.capoKey },
    { label: '原唱调', value: doc.originalKey },
  ]
  return candidates
    .filter((c): c is { label: string; value: string } => !!c.value && c.value.trim() !== '')
})

// 切歌后既要刷新详情又要重算缩放
watch(currentSong, async () => {
  await ensureCurrentDetail()
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
/* Drum machine slide-down transition */
.drum-slide-enter-active,
.drum-slide-leave-active {
  transition: max-height 0.25s ease, opacity 0.25s ease;
  overflow: hidden;
}
.drum-slide-enter-from,
.drum-slide-leave-to {
  max-height: 0;
  opacity: 0;
}
.drum-slide-enter-to,
.drum-slide-leave-from {
  max-height: 200px;
  opacity: 1;
}

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
 * 吉他谱渲染 —— 数据来自规范化 TabDocument（schemaVersion=1）
 *   DOM 结构（由模板渲染生成，保留 yopu 风格的 xhe-* 标签以沿用视觉）：
 *     .tab-content-html > .xhe-sheet
 *       > .sheet-header  .title / .info .item{.label,.text} / .meta .col{.label,.value}
 *       > .xhe-body
 *           <xhe-headline><div text-value>段落名</div></xhe-headline>
 *           <div class="xhe-paragraph">              一整行歌词（块级）
 *             <xhe-text>歌词片段</xhe-text>
 *             <xhe-chord-anchor data-chord="3m" data-value-length="1">
 *               <div class="chord">3m</div>
 *               <div class="text">字</div>
 *             </xhe-chord-anchor>
 *             ...
 *           </div>
 *           <div class="xhe-blank"></div>       空白分段
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
  padding: 0 24px 48px;
  max-width: 820px;
}

/* ---------- 外层容器 ---------- */
.tab-content-html :deep(.xhe-sheet) {
  position: relative;
  padding: 0;
  background: transparent;
}

/* ---------- sheet-header：只保留拍号 / 拍速 / 选调 / 原唱调 一行 meta ---------- */
.tab-content-html :deep(.sheet-header) {
  position: relative;
  margin-bottom: 16px;
}

/* 四格 meta 强制单行：任意宽度下都横排，不再换行 */
.tab-content-html :deep(.sheet-header .meta) {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 1fr;
  gap: 0;
  padding: 4px 2px;
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
  padding: 2px 6px;
  font-size: 0.85em;
  line-height: 1.7em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

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

/* 窄屏上字再小一点，保证 4 格仍在一行 */
@media (max-width: 520px) {
  .tab-content-html :deep(.sheet-header .meta .col) {
    padding: 2px 4px;
    font-size: 0.78em;
  }
}

/* ---------- xhe-body 正文 ---------- */
.tab-content-html :deep(.xhe-body) {
  position: relative;
  padding: 6px 0 0;
}

/* 段落标题（主歌 / 副歌 / Bridge 等）—— 红色渐隐"标签条"风格，
   左侧实心、向右逐渐隐入背景，既保留分段功能又不突兀 */
.tab-content-html :deep(xhe-headline) {
  display: block;
  position: relative;
  margin: -1rem 0 1rem 0;
  padding: 0.04em 0.55em;
  background: linear-gradient(
    to right,
    rgba(239, 68, 68, 0.22),
    rgba(239, 68, 68, 0.08) 55%,
    transparent 100%
  );
  border-left: 2px solid var(--accent);
  border-radius: 3px;
}

.tab-content-html :deep(xhe-headline [text-value]) {
  display: inline-block;
  line-height: 1.15em;
  font-size: 0.78em;
  font-weight: 500;
  color: var(--color-font-secondary);
  letter-spacing: 0.1em;
}

.tab-content-html :deep(.xhe-body > xhe-headline:first-child) {
  margin-top: 0;
}

/* ---------- 歌词行（段落：一行或多行 chord+lyric） ----------
 * 和弦空位由 line-height 的 leading 承担，而不是 margin-top —— 这样
 * 歌词折行时，每个视觉行都会在行盒上方留出和弦位置，chord 不会穿到上一行。
 * line-height ≈ 3em 是经过计算的最小安全值：
 *   anchor top = 0.75*line-height - 0.87em（.text baseline 偏移）
 *   chord height ≈ 0.95em（font-size 0.95em）
 *   需要 anchor top > chord height + desired-gap，才不会溢出行盒顶部。
 */
.tab-content-html :deep(.xhe-paragraph) {
  display: block;
  position: relative;
  line-height: 3em;
  margin-top: 0.2em;
  color: var(--color-font-main);
  letter-spacing: 0.07em;
}

/* 段落/Headline 紧挨着的第一个段落不再叠加额外 margin */
.tab-content-html :deep(.xhe-body > .xhe-paragraph:first-child),
.tab-content-html :deep(xhe-headline + .xhe-paragraph) {
  margin-top: 0;
}

.tab-content-html :deep(xhe-text) {
  display: inline;
  line-height: 3em;
  color: var(--color-font-main);
  letter-spacing: 0.07em;
}

/* 空行分段 */
.tab-content-html :deep(.xhe-blank) {
  display: block;
  height: 0.6em;
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

/* 没有承载字的纯和弦锚点（比如 intro 段开头的空和弦），留一点宽度 */
.tab-content-html :deep(xhe-chord-anchor[data-value-length="0"]) {
  min-width: 1em;
  margin-right: 0.15em;
}

/* 和弦文字（数字谱：1 / 6m / 5 / 2m，字母谱：Am / C / G7） */
.tab-content-html :deep(xhe-chord-anchor > .chord) {
  position: absolute;
  /* 因为 anchor 是 inline-block 继承了段落 line-height=3em，anchor 高度==行盒，
   * anchor top 就等于当前视觉行的 top。于是：
   *   chord 底到歌词顶 ≈ |top| + 0.05em
   *   chord 顶到上一行歌词底 ≈ line-height − |top| − 2em
   * 目标：chord 贴近本行歌词（约 0.25em），上一行留白 ≈ 0.8em。 */
  top: -0.2em;
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

/* 选区高亮 */
.tab-content-html :deep(xhe-headline::selection),
.tab-content-html :deep(xhe-text::selection) {
  background-color: var(--accent-soft);
}

/* ================================================================= *
 * 和弦图（diagram）模式：chord 由文字换成 52px 高的 SVG 指法图。
 *   需要更大的行距承载 SVG，同时把 chord 的 top 往下移，让图整体贴近
 *   本行歌词、与上一行之间留呼吸。
 *   SVG 高度 52px ≈ 3.06em（font-size 17px），line-height 5em 给出
 *   约 0.7em 的上方呼吸 + 0.2em 的下方贴合。
 * ================================================================= */
.tab-content-html.mode-diagram :deep(.xhe-paragraph) {
  /* SVG 渲染高度约 40px（≈2.35em），上方留 ~0.55em 呼吸即可。 */
  line-height: 3.9em;
}

/* diagram 模式下，和弦图有 ~2.35em 高度且绝对定位到首行上方，
   headline / sheet-header 与其后的段落需要额外留白，避免和弦图压到上方信息条。 */
.tab-content-html.mode-diagram :deep(xhe-headline) {
  margin-bottom: 1.6em;
}

.tab-content-html.mode-diagram :deep(xhe-headline + .xhe-paragraph) {
  margin-top: 1em;
}

/* sheet-header 与紧随其后的首段之间也要给和弦图留位（图 1 反馈：
   切到和弦图模式时，第一行歌词上方的指法图会顶到拍号 / 拍速信息条）。
   chord SVG 顶端 = -1.35em，给 ~0.4em 呼吸即可，再多就显得空。 */
.tab-content-html.mode-diagram :deep(.sheet-header) {
  margin-bottom: 1.75em;
}

.tab-content-html.mode-diagram :deep(xhe-text) {
  line-height: 3.9em;
}

.tab-content-html.mode-diagram :deep(xhe-chord-anchor > .chord) {
  /* SVG 本身自带文字和指板，不再需要外层的文字样式。
     top: -1.35em 让和弦图底边贴到歌词上沿（0 间距）。 */
  top: -1.35em;
  font-size: 1em;       /* SVG 走 px 尺寸，这里清除 0.95em 缩放避免叠加 */
  font-weight: normal;
  line-height: 1;
  background: transparent !important;
}

/* 连续两个带和弦的字：SVG 宽 ~30px 而汉字只有 ~17px，
   居中叠放会相互遮挡，所以给相邻的第二个 anchor 加左外边距，
   让两张和弦图之间留出 ~2px 安全间距。 */
.tab-content-html.mode-diagram :deep(xhe-chord-anchor + xhe-chord-anchor) {
  margin-left: 1em;
}

.tab-content-html.mode-diagram :deep(xhe-chord-anchor:active > .chord) {
  background: transparent !important;
}
</style>
