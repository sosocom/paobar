<template>
  <!-- 固定高度：留出底部导航 + iOS safe-area，这样内部列表区域才会真正滚动并触发加载更多。
       .h-screen-no-nav / --nav-height 统一在 style.css 里定义。 -->
  <div class="flex flex-col overflow-hidden h-screen-no-nav">
    <!-- Header：移除顶部标题，直接以搜索栏作为主入口，腾出更多主内容空间 -->
    <header class="flex-shrink-0 z-40 bg-background-card/95 backdrop-blur-xl px-4 pt-3 pb-2">
      <!-- Search Bar -->
      <div class="relative mb-3">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary" :size="20" />
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="搜索歌曲、歌手"
          class="w-full bg-background-overlay/50 rounded-xl pl-11 pr-11 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50"
        />
        <button
          v-show="searchQuery.length > 0"
          type="button"
          class="absolute right-2 top-1/2 -translate-y-1/2 p-1.5 rounded-full text-text-secondary hover:text-text-primary hover:bg-background-overlay/80 transition-colors focus:outline-none focus:ring-2 focus:ring-primary/40"
          aria-label="清空搜索"
          @click="searchQuery = ''"
        >
          <X :size="18" />
        </button>
      </div>
      
      <!-- Filter Tags + 右侧"安装 App"入口：
           左半是可横向滚动的筛选区，右半是固定宽度的 App 入口按钮。
           已经 standalone 运行（已安装的 PWA）时隐藏入口，避免在 App 里再看到"装 App"。 -->
      <div class="flex items-center gap-2 pb-2">
        <div class="flex-1 min-w-0 flex gap-2 overflow-x-auto">
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
        <button
          v-if="shouldShowInstallEntry"
          @click="openInstallWizard"
          class="flex items-center gap-1 px-3 py-2 rounded-full text-xs font-medium whitespace-nowrap bg-primary/15 text-primary hover:bg-primary/25 transition-colors flex-shrink-0"
          aria-label="安装为 App"
        >
          <Download :size="14" />
          <span>App</span>
        </button>
      </div>
    </header>

    <!-- 列表区：左侧可滚动列表 + 右侧通讯录式字母索引 -->
    <div class="flex flex-1 min-h-0 min-w-0 relative">
      <div 
        ref="scrollContainer"
        class="flex-1 min-w-0 pl-4 pr-0 py-3 overflow-y-auto overscroll-contain"
        @scroll="handleScroll"
        @touchstart="handleTouchStart"
        @touchmove="handleTouchMove"
        @touchend="handleTouchEnd"
      >
      <!-- Pull-to-Refresh Indicator -->
      <div 
        v-if="pullDistance > 0" 
        class="flex items-center justify-center py-2 transition-all"
        :style="{ transform: `translateY(${Math.min(pullDistance, 80)}px)` }"
      >
        <div v-if="isRefreshing" class="w-6 h-6 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <div v-else class="text-text-secondary text-sm">
          {{ pullDistance > 60 ? '释放刷新' : '下拉刷新' }}
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading && songs.length === 0" class="text-center py-12">
        <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-text-secondary text-sm mt-3">加载中...</p>
      </div>
      
      <!-- Song Cards（扁平列表；首字母筛选由后端按 indexLetter 过滤） -->
      <div class="space-y-2 mb-3">
        <div
          v-for="song in displaySongs"
          :key="song.id"
          class="bg-background-card/50 backdrop-blur-sm rounded-xl p-2.5 flex gap-2.5 hover:bg-background-card/70 transition-all relative"
        >
          <!-- Main Song Info - Clickable -->
          <div
            @click="playSong(song)"
            class="flex gap-2.5 flex-1 min-w-0 cursor-pointer"
          >
            <div class="w-14 h-14 rounded-lg overflow-hidden flex-shrink-0 bg-primary/10 flex items-center justify-center">
              <Play :size="24" class="text-primary" />
            </div>

            <div class="flex-1 min-w-0 flex flex-col justify-center">
              <h3 class="font-medium truncate leading-tight">{{ song.title }}</h3>
              <p class="text-xs text-text-secondary truncate mt-0.5">{{ song.artist }}</p>

              <p v-if="song.meta" class="text-[11px] text-text-secondary truncate mt-1">{{ song.meta }}</p>

              <div
                v-if="song.difficulty || song.playKey || song.originalKey"
                class="flex items-center gap-1.5 text-[11px] text-text-secondary mt-1"
              >
                <span v-if="song.difficulty" class="px-1.5 py-0.5 bg-background-overlay/50 rounded">{{ song.difficulty }}</span>
                <span v-if="song.playKey" class="px-1.5 py-0.5 bg-background-overlay/50 rounded">{{ song.playKey }}调</span>
                <span v-if="song.originalKey">原调: {{ song.originalKey }}</span>
              </div>
            </div>
          </div>

          <div class="flex flex-col items-center justify-center gap-1 flex-shrink-0">
            <button
              @click.stop="toggleFavorite(song)"
              class="p-1 rounded-lg transition-colors"
              :disabled="togglingFavId === song.id"
            >
              <Heart
                :size="16"
                :class="favoriteIds.has(song.id)
                  ? 'text-red-400 fill-red-400'
                  : 'text-text-secondary'"
              />
            </button>
            <button
              @click.stop="openAddToPlaylistDialog(song)"
              class="p-1 rounded-lg transition-colors"
            >
              <Plus :size="16" class="text-text-secondary" />
            </button>
          </div>
        </div>
      </div>

      <!-- Load More Indicator -->
      <div v-if="loadingMore" class="text-center py-4">
        <div class="inline-block w-6 h-6 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-text-secondary text-sm mt-2">加载更多...</p>
      </div>

      <!-- No More Data -->
      <div v-if="!hasMore && songs.length > 0" class="text-center py-4">
        <p class="text-text-secondary text-sm">没有更多数据了</p>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && songs.length === 0" class="text-center py-12">
        <p class="text-text-secondary">暂无歌曲</p>
      </div>
      </div>

      <!-- 右侧首字母快选：点击触发筛选；A 上方为清空选中按钮 -->
      <nav
        class="w-7 flex-shrink-0 self-stretch max-h-full overflow-y-auto flex flex-col items-stretch justify-start gap-0.5 py-3 px-0.5 border-l border-white/5 bg-background-card/50 backdrop-blur-sm"
        aria-label="按首字母筛选"
      >
        <button
          type="button"
          @click="clearLetter"
          class="text-[10px] leading-tight py-0.5 rounded text-center font-medium transition-colors"
          :class="selectedIndexLetter === null
            ? 'text-text-secondary/60 cursor-default'
            : 'text-primary hover:bg-primary/10'"
          :disabled="selectedIndexLetter === null"
          aria-label="清空首字母筛选"
          title="清空筛选"
        >
          <X :size="12" class="inline-block" />
        </button>
        <button
          v-for="ch in indexLetters"
          :key="ch"
          type="button"
          @click="selectLetter(ch)"
          class="text-[10px] leading-tight py-0.5 rounded text-center font-medium transition-colors"
          :class="selectedIndexLetter === ch
            ? 'bg-primary text-white'
            : 'text-primary/80 hover:text-primary hover:bg-primary/10'"
        >
          {{ ch }}
        </button>
      </nav>
    </div>

    <!-- Add to Playlist Dialog -->
    <Transition name="dialog">
      <div v-if="showAddDialog" class="fixed inset-0 z-50 flex flex-col" @click.self="closeAddDialog">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60" @click="closeAddDialog"></div>
        
        <!-- Bottom Sheet - Full height except top header area -->
        <div class="relative w-full bg-background-card rounded-t-3xl p-6 flex flex-col mt-[180px] flex-1">
          <!-- Drag Handle -->
          <div class="flex justify-center mb-4">
            <div class="w-10 h-1 bg-text-secondary/30 rounded-full"></div>
          </div>
          
          <h2 class="text-xl font-semibold mb-4">添加到歌单</h2>
          
          <!-- Playlist List -->
          <div class="flex-1 overflow-y-auto space-y-2 mb-4 pb-[100px]">
            <button
              v-for="playlist in userPlaylists"
              :key="playlist.id"
              @click="addSongToPlaylist(playlist.id)"
              :disabled="adding"
              class="w-full bg-background-overlay/50 rounded-xl p-3 flex items-center gap-3 hover:bg-background-overlay transition-colors disabled:opacity-50"
            >
              <div 
                class="w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0"
                :style="{ background: `linear-gradient(135deg, ${playlist.gradient?.[0]}, ${playlist.gradient?.[1]})` }"
              >
                <ListMusic :size="20" class="text-white" />
              </div>
              <div class="flex-1 text-left">
                <p class="font-medium">{{ playlist.name }}</p>
                <p class="text-xs text-text-secondary">{{ playlist.songCount }} 首</p>
              </div>
            </button>
            
            <!-- Empty State -->
            <div v-if="userPlaylists.length === 0" class="text-center py-8">
              <p class="text-text-secondary text-sm">暂无歌单</p>
              <p class="text-text-secondary text-xs mt-2">请先创建歌单</p>
            </div>
          </div>
          
          <!-- Success Message -->
          <div v-if="addSuccess" class="bg-green-500/10 border border-green-500/20 rounded-xl p-4 mb-4">
            <p class="text-sm text-green-400 font-medium text-center">{{ addSuccess }}</p>
          </div>
          
          <!-- Error Message -->
          <div v-if="addError" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4 mb-4">
            <p class="text-sm text-red-400 text-center">{{ addError }}</p>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script lang="ts">
/**
 * Index 页面会话级缓存（真·模块级，跨 setup 调用常驻）。
 *
 * 关键：必须放在没有 setup 修饰的 <script> 块里 —— `<script setup>` 整段会被
 * 编译进 setup() 函数体，里面的 `let` 是按实例创建的，不是模块级，根本起不到
 * 缓存作用。这就是上一版"看起来写了但没生效"的根因。
 */
import type { Song } from '@/types'

export interface IndexCache {
  searchQuery: string
  selectedTag: string
  selectedIndexLetter: string | null
  songs: Song[]
  currentPage: number
  hasMore: boolean
  scrollTop: number
  savedAt: number
}

export const INDEX_CACHE_TTL_MS = 30 * 60 * 1000

/** 跨实例的缓存槽。`<script>`（无 setup）只在模块装载时执行一次。 */
export let indexCacheSlot: IndexCache | null = null

export const setIndexCache = (c: IndexCache | null) => {
  indexCacheSlot = c
}

export const readFreshIndexCache = (): IndexCache | null => {
  if (!indexCacheSlot) return null
  if (Date.now() - indexCacheSlot.savedAt >= INDEX_CACHE_TTL_MS) return null
  return indexCacheSlot
}
</script>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { Search, Play, Heart, Plus, ListMusic, Download, X } from 'lucide-vue-next'
import { api } from '@/api'
import { isLoggedIn } from '@/auth'
import { requireLogin } from '@/authBus'
import { openInstallWizard, shouldShowInstallEntry } from '@/installBus'
import type { Song, Playlist } from '@/types'
import {
  compareSongsByTitlePinyin,
  songIndexLetter,
} from '@/utils/songIndex'

const restored = readFreshIndexCache()

const router = useRouter()
const searchQuery = ref(restored?.searchQuery ?? '')
const selectedTag = ref(restored?.selectedTag ?? '全部')
const songs = ref<Song[]>(restored?.songs ?? [])
const loading = ref(false)
const loadingMore = ref(false)
const isRefreshing = ref(false)

// 分页相关
const currentPage = ref(restored?.currentPage ?? 1)
const pageSize = 20
const hasMore = ref(restored?.hasMore ?? true)

// 下拉刷新相关
const scrollContainer = ref<HTMLElement | null>(null)
const pullDistance = ref(0)
const startY = ref(0)
const isPulling = ref(false)

const showAddDialog = ref(false)
const selectedSong = ref<Song | null>(null)
const userPlaylists = ref<Playlist[]>([])
const adding = ref(false)
const addError = ref('')
const addSuccess = ref('')

// 收藏相关
const favoriteIds = ref<Set<string>>(new Set())
const togglingFavId = ref<string | null>(null)

const filterTags = ['全部', '已收藏']

/** 当前选中的首字母筛选；null 表示未选中（显示全部） */
const selectedIndexLetter = ref<string | null>(restored?.selectedIndexLetter ?? null)

/** 右侧字母索引固定为 A–Z + #，无论列表是否有对应字母 */
const indexLetters = computed<string[]>(() => {
  const az = Array.from({ length: 26 }, (_, i) =>
    String.fromCharCode('A'.charCodeAt(0) + i),
  )
  return [...az, '#']
})

/**
 * 实际渲染的列表：
 * - 「全部」：后端按 indexLetter 已过滤好，直接展示
 * - 「已收藏」：暂不分页，整体拉回后本地按拼音排序，并按选中的字母筛选
 */
const displaySongs = computed<Song[]>(() => {
  if (selectedTag.value !== '已收藏') return songs.value
  const sorted = [...songs.value].sort(compareSongsByTitlePinyin)
  if (!selectedIndexLetter.value) return sorted
  return sorted.filter((s) => songIndexLetter(s.title) === selectedIndexLetter.value)
})

function selectLetter(letter: string) {
  if (selectedIndexLetter.value === letter) return
  selectedIndexLetter.value = letter
  scrollToTop()
}

function clearLetter() {
  if (selectedIndexLetter.value === null) return
  selectedIndexLetter.value = null
  scrollToTop()
}

function scrollToTop() {
  scrollContainer.value?.scrollTo({ top: 0, behavior: 'smooth' })
}

// 加载收藏ID列表
const loadFavoriteIds = async () => {
  try {
    const ids = await api.getFavoriteSongIds()
    favoriteIds.value = new Set(ids)
  } catch (error) {
    console.error('Failed to load favorite ids:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async (song: Song) => {
  if (!isLoggedIn()) {
    requireLogin(() => toggleFavorite(song))
    return
  }
  togglingFavId.value = song.id
  try {
    const isFav = favoriteIds.value.has(song.id)
    if (isFav) {
      await api.unfavoriteSong(song.id)
      favoriteIds.value.delete(song.id)
      favoriteIds.value = new Set(favoriteIds.value)
    } else {
      await api.favoriteSong(song.id)
      favoriteIds.value.add(song.id)
      favoriteIds.value = new Set(favoriteIds.value)
    }
  } catch (error) {
    console.error('Failed to toggle favorite:', error)
  } finally {
    togglingFavId.value = null
  }
}

// 加载歌曲列表
const loadSongs = async (page: number = 1, append: boolean = false) => {
  if (page === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    if (selectedTag.value === '已收藏') {
      // 加载收藏的歌曲（暂不支持分页）
      const allFavorites = await api.getFavoriteSongs()
      if (!append) {
        songs.value = allFavorites
      }
      hasMore.value = false
    } else {
      // 加载所有歌曲，支持搜索、分页与首字母筛选
      const params = {
        search: searchQuery.value,
        page: page,
        pageSize: pageSize,
        indexLetter: selectedIndexLetter.value,
      }
      console.log('[loadSongs] 发起请求', params)
      const newSongs = await api.getSongs(params)
      console.log('[loadSongs] 返回条数', newSongs?.length)
      
      if (append) {
        songs.value = [...songs.value, ...newSongs]
      } else {
        songs.value = newSongs
      }
      
      // 判断是否还有更多数据
      hasMore.value = newSongs.length === pageSize
    }
  } catch (error) {
    console.error('Failed to load songs:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 下拉刷新
const handleRefresh = async () => {
  isRefreshing.value = true
  currentPage.value = 1
  hasMore.value = true
  
  await loadSongs(1, false)
  
  isRefreshing.value = false
  pullDistance.value = 0
}

// 加载更多
const loadMore = async () => {
  console.log('[loadMore] 调用', { loadingMore: loadingMore.value, hasMore: hasMore.value, loading: loading.value })
  if (loadingMore.value || !hasMore.value || loading.value) {
    console.log('[loadMore] 跳过，不请求')
    return
  }
  console.log('[loadMore] 请求下一页', currentPage.value + 1)
  currentPage.value++
  await loadSongs(currentPage.value, true)
}

// 处理滚动事件（上拉加载更多）
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight
  const distanceToBottom = scrollHeight - scrollTop - clientHeight
  const shouldLoad = distanceToBottom < 100
  // 调试：每次滚动打印（可后续删掉或改为节流后打印）
  if (shouldLoad || distanceToBottom < 300) {
    console.log('[handleScroll]', { scrollTop, scrollHeight, clientHeight, distanceToBottom, shouldLoad })
  }
  if (shouldLoad) {
    loadMore()
  }
}

// 下拉刷新触摸事件
const handleTouchStart = (e: TouchEvent) => {
  const target = scrollContainer.value
  if (!target) return
  
  // 只有在顶部时才允许下拉刷新
  if (target.scrollTop === 0) {
    isPulling.value = true
    startY.value = e.touches[0].clientY
  }
}

const handleTouchMove = (e: TouchEvent) => {
  if (!isPulling.value || isRefreshing.value) return
  
  const currentY = e.touches[0].clientY
  const distance = currentY - startY.value
  
  if (distance > 0) {
    pullDistance.value = distance
    // 阻止默认滚动
    if (distance > 10) {
      e.preventDefault()
    }
  }
}

const handleTouchEnd = () => {
  if (!isPulling.value) return
  
  isPulling.value = false
  
  // 如果拉动距离超过 60px，触发刷新
  if (pullDistance.value > 60 && !isRefreshing.value) {
    handleRefresh()
  } else {
    pullDistance.value = 0
  }
}

// 加载用户歌单
const loadUserPlaylists = async () => {
  try {
    userPlaylists.value = await api.getPlaylistsByType('user')
  } catch (error) {
    console.error('Failed to load playlists:', error)
  }
}

// 打开添加到歌单对话框
const openAddToPlaylistDialog = async (song: Song) => {
  if (!isLoggedIn()) {
    requireLogin(() => openAddToPlaylistDialog(song))
    return
  }
  selectedSong.value = song
  showAddDialog.value = true
  addError.value = ''
  addSuccess.value = ''
  
  // 加载用户歌单列表
  await loadUserPlaylists()
}

// 添加歌曲到歌单
const addSongToPlaylist = async (playlistId: string) => {
  if (!selectedSong.value) return
  
  adding.value = true
  addError.value = ''
  addSuccess.value = ''
  
  try {
    await api.addSongToPlaylist(playlistId, selectedSong.value.id)
    addSuccess.value = '添加成功！'
    
    // 1秒后关闭对话框
    setTimeout(() => {
      closeAddDialog()
    }, 1000)
  } catch (error: any) {
    console.error('Failed to add song to playlist:', error)
    addError.value = error.message || '添加失败，可能已存在'
  } finally {
    adding.value = false
  }
}

// 关闭对话框
const closeAddDialog = () => {
  showAddDialog.value = false
  selectedSong.value = null
  addError.value = ''
  addSuccess.value = ''
}

// 监听筛选条件变化（标签 / 搜索词 / 首字母）
watch([selectedTag, searchQuery, selectedIndexLetter], () => {
  currentPage.value = 1
  hasMore.value = true
  loadSongs(1, false)
})

// 初始化加载
onMounted(async () => {
  if (restored) {
    // 命中缓存：跳过整体重拉，直接把滚动位置恢复到原处。
    // 收藏状态可能在详情页被切换过，需要单独刷新一份用于红心高亮。
    await nextTick()
    if (scrollContainer.value) {
      scrollContainer.value.scrollTop = restored.scrollTop
    }
    loadFavoriteIds()
  } else {
    loadSongs()
    loadFavoriteIds()
  }
})

// 离开首页（去 NowPlaying / Profile / Playlist…）前快照当前会话状态，
// 再回来时由 onMounted 走 restored 分支恢复。
onBeforeRouteLeave(() => {
  const snapshot: IndexCache = {
    searchQuery: searchQuery.value,
    selectedTag: selectedTag.value,
    selectedIndexLetter: selectedIndexLetter.value,
    songs: songs.value,
    currentPage: currentPage.value,
    hasMore: hasMore.value,
    scrollTop: scrollContainer.value?.scrollTop ?? 0,
    savedAt: Date.now(),
  }
  setIndexCache(snapshot)
})

const playSong = (song: Song) => {
  router.push({ name: 'NowPlaying', params: { id: song.id } })
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
</style>
