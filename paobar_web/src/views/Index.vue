<template>
  <!-- 固定高度：留出底部导航 5rem，这样内部列表区域才会真正滚动并触发加载更多 -->
  <div class="flex flex-col overflow-hidden pb-20" style="height: calc(100vh - 5rem);">
    <!-- Header -->
    <header class="flex-shrink-0 z-40 bg-background-card/95 backdrop-blur-xl px-4 py-4">
      <h1 class="text-2xl font-semibold mb-4 text-center">首页</h1>
      
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

    <!-- Song List Container: 可滚动区域，下拉刷新 + 上拉加载更多 -->
    <div 
      ref="scrollContainer"
      class="flex-1 min-h-0 px-4 py-4 space-y-3 overflow-y-auto overscroll-contain"
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
      
      <!-- Song Cards -->
      <div 
        v-for="song in songs" 
        :key="song.id"
        class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-3 flex gap-3 hover:bg-background-card/70 transition-all relative"
      >
        <!-- Main Song Info - Clickable -->
        <div 
          @click="playSong(song)"
          class="flex gap-3 flex-1 min-w-0 cursor-pointer"
        >
          <!-- Cover / Icon -->
          <div class="w-20 h-20 rounded-xl overflow-hidden flex-shrink-0 bg-primary/10 flex items-center justify-center">
            <Play :size="32" class="text-primary" />
          </div>
          
          <!-- Info -->
          <div class="flex-1 min-w-0">
            <h3 class="font-medium truncate mb-1">{{ song.title }}</h3>
            <p class="text-sm text-text-secondary truncate mb-2">{{ song.artist }}</p>
            
            <!-- Meta 信息 -->
            <p v-if="song.meta" class="text-xs text-text-secondary truncate mb-2">{{ song.meta }}</p>
            
            <div class="flex items-center gap-3 text-xs text-text-secondary">
              <span v-if="song.difficulty" class="px-2 py-0.5 bg-background-overlay/50 rounded">{{ song.difficulty }}</span>
              <span v-if="song.playKey" class="px-2 py-0.5 bg-background-overlay/50 rounded">{{ song.playKey }}调</span>
              <span v-if="song.originalKey" class="text-text-secondary">原调: {{ song.originalKey }}</span>
            </div>
          </div>
        </div>
        
        <!-- Action Buttons -->
        <div class="flex flex-col items-center justify-center gap-2">
          <!-- Favorite -->
          <button
            @click.stop="toggleFavorite(song)"
            class="p-1.5 rounded-lg transition-colors"
            :disabled="togglingFavId === song.id"
          >
            <Heart
              :size="18"
              :class="favoriteIds.has(song.id)
                ? 'text-red-400 fill-red-400'
                : 'text-text-secondary'"
            />
          </button>
          <!-- Add to Playlist -->
          <button
            @click.stop="openAddToPlaylistDialog(song)"
            class="p-1.5 rounded-lg transition-colors"
          >
            <Plus :size="18" class="text-text-secondary" />
          </button>
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

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Play, Heart, Plus, ListMusic } from 'lucide-vue-next'
import { api } from '@/api'
import { isLoggedIn } from '@/auth'
import { requireLogin } from '@/authBus'
import type { Song, Playlist } from '@/types'

const router = useRouter()
const searchQuery = ref('')
const selectedTag = ref('全部')
const songs = ref<Song[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const isRefreshing = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = 20
const hasMore = ref(true)

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
      // 加载所有歌曲，支持搜索和分页
      const params = {
        search: searchQuery.value,
        page: page,
        pageSize: pageSize
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

// 监听筛选条件变化
watch([selectedTag, searchQuery], () => {
  currentPage.value = 1
  hasMore.value = true
  loadSongs(1, false)
})

// 初始化加载
onMounted(() => {
  loadSongs()
  loadFavoriteIds()
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
