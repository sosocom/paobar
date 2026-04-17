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
            <div class="flex items-center gap-2 mb-1">
              <template v-if="isEditingName">
                <input
                  v-model="editNameValue"
                  ref="nameInputRef"
                  type="text"
                  class="flex-1 min-w-0 text-xl font-semibold bg-background-overlay/50 rounded-lg px-2 py-1 border border-white/10 focus:border-primary/50 focus:outline-none"
                  @keydown.enter="savePlaylistName"
                  @keydown.escape="cancelEditName"
                />
                <button
                  @click="savePlaylistName"
                  :disabled="savingName || !editNameValue?.trim()"
                  class="p-2 rounded-lg bg-primary text-white hover:bg-primary/90 disabled:opacity-50"
                  title="保存"
                >
                  <Check :size="20" />
                </button>
                <button
                  @click="cancelEditName"
                  :disabled="savingName"
                  class="p-2 rounded-lg bg-background-overlay/50 text-text-secondary hover:bg-background-overlay disabled:opacity-50"
                  title="取消"
                >
                  <X :size="20" />
                </button>
              </template>
              <template v-else>
                <h2 class="text-xl font-semibold truncate flex-1">{{ playlist?.name }}</h2>
                <button
                  v-if="playlist?.type === 'user'"
                  @click="startEditName"
                  class="flex-shrink-0 p-2 rounded-lg text-text-secondary hover:bg-background-overlay/50 hover:text-text-primary transition-colors"
                  title="编辑歌单名"
                >
                  <Pencil :size="18" />
                </button>
              </template>
            </div>
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

    <!-- Song List (可拖动排序) -->
    <div v-else class="px-4 py-4 space-y-3 song-list-drag">
      <div 
        v-for="(song, idx) in playlistSongs" 
        :key="'song-' + song.id"
        :data-song-index="idx"
        class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-3 flex gap-2 items-center hover:bg-background-card/70 transition-all"
        :class="{ 'opacity-60': dragFromIndex === idx, 'ring-2 ring-primary/50': dragOverIndex === idx }"
        @dragover.prevent="onDragOver($event, idx)"
        @dragleave="onDragLeave($event, idx)"
        @drop.prevent="onDrop($event, idx)"
      >
        <!-- Main Song Info - Clickable -->
        <div 
          @click="playSong(song, idx)"
          class="flex gap-3 flex-1 min-w-0 cursor-pointer"
        >
          <!-- Index Number -->
          <div class="flex items-center justify-center w-6 text-text-secondary text-sm font-medium flex-shrink-0">
            {{ idx + 1 }}
          </div>
          
          <!-- Info -->
          <div class="flex-1 min-w-0">
            <h3 class="font-medium truncate mb-1">{{ song.title }}</h3>
            <p class="text-sm text-text-secondary truncate mb-2">{{ song.artist }}</p>
            <p v-if="song.meta" class="text-xs text-text-secondary truncate">
              {{ song.meta }}
            </p>
          </div>
          
          <!-- Play Icon -->
          <div class="flex items-center flex-shrink-0">
            <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
              <Play :size="20" class="text-primary ml-0.5" />
            </div>
          </div>
        </div>
        
        <!-- 置顶 / 置尾（仅用户歌单） -->
        <template v-if="playlist?.type === 'user'">
          <button
            v-if="idx > 0"
            @click.stop="moveToTop(song)"
            class="flex items-center justify-center p-2 rounded-lg text-text-secondary hover:bg-background-overlay/50 transition-colors"
            title="置顶"
          >
            <ArrowUpToLine :size="18" />
          </button>
          <button
            v-if="idx < playlistSongs.length - 1"
            @click.stop="moveToBottom(song)"
            class="flex items-center justify-center p-2 rounded-lg text-text-secondary hover:bg-background-overlay/50 transition-colors"
            title="置底"
          >
            <ArrowDownToLine :size="18" />
          </button>
        </template>
        
        <!-- Remove Button -->
        <button 
          @click.stop="confirmRemoveSong(song)"
          class="flex items-center justify-center p-2 hover:bg-red-500/10 rounded-lg transition-colors flex-shrink-0"
          title="移出歌单"
        >
          <X :size="20" class="text-red-400" />
        </button>

        <!-- 拖动手柄（仅用户歌单，放最后） -->
        <div
          v-if="playlist?.type === 'user'"
          class="flex items-center justify-center w-8 h-10 flex-shrink-0 cursor-grab active:cursor-grabbing select-none"
          draggable="true"
          @dragstart="onDragStart($event, idx)"
          @dragend="onDragEnd"
          title="拖动排序"
        >
          <GripVertical :size="18" class="text-text-secondary pointer-events-none" />
        </div>
      </div>
      
      <!-- Empty State -->
      <div v-if="playlistSongs.length === 0" class="text-center py-12">
        <Music :size="48" class="text-text-secondary mx-auto mb-3 opacity-30" />
        <p class="text-text-secondary">歌单为空</p>
      </div>
    </div>

    <!-- Confirm Remove Dialog -->
    <Transition name="dialog">
      <div v-if="showRemoveDialog" class="fixed inset-0 z-50 flex items-center justify-center" @click.self="closeRemoveDialog">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60" @click="closeRemoveDialog"></div>
        
        <!-- Dialog Content -->
        <div class="relative bg-background-card rounded-3xl p-6 w-[90%] max-w-md">
          <h2 class="text-lg font-semibold mb-2">确认移出</h2>
          <p class="text-sm text-text-secondary mb-4">
            确定要将《{{ songToRemove?.title }}》从歌单中移出吗？
          </p>
          
          <!-- Buttons -->
          <div class="flex gap-3">
            <button
              @click="closeRemoveDialog"
              class="flex-1 py-3 rounded-xl bg-background-overlay/50 text-text-secondary hover:bg-background-overlay transition-colors"
            >
              取消
            </button>
            <button
              @click="removeSong"
              :disabled="removing"
              class="flex-1 py-3 rounded-xl bg-red-500 text-white hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {{ removing ? '移出中...' : '移出' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ChevronLeft, Play, ListMusic, Sparkles, Music, X, Pencil, Check, GripVertical, ArrowUpToLine, ArrowDownToLine } from 'lucide-vue-next'
import { api } from '@/api'
import type { Song, Playlist } from '@/types'

const router = useRouter()
const route = useRoute()

const playlist = ref<Playlist | null>(null)
const playlistSongs = ref<Song[]>([])
const loading = ref(false)

const showRemoveDialog = ref(false)
const songToRemove = ref<Song | null>(null)
const removing = ref(false)

// 编辑歌单名
const isEditingName = ref(false)
const editNameValue = ref('')
const nameInputRef = ref<HTMLInputElement | null>(null)
const savingName = ref(false)

// 拖动排序
const dragFromIndex = ref<number | null>(null)
const dragOverIndex = ref<number | null>(null)
const reordering = ref(false)

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

// 确认移出歌曲
const confirmRemoveSong = (song: Song) => {
  songToRemove.value = song
  showRemoveDialog.value = true
}

// 移出歌曲
const removeSong = async () => {
  if (!songToRemove.value || !playlist.value) return
  
  removing.value = true
  
  try {
    await api.removeSongFromPlaylist(playlist.value.id, songToRemove.value.id)
    // 重新加载歌单
    await loadPlaylistDetail()
    closeRemoveDialog()
  } catch (error) {
    console.error('Failed to remove song:', error)
    alert('移出失败，请重试')
  } finally {
    removing.value = false
  }
}

// 关闭移出对话框
const closeRemoveDialog = () => {
  showRemoveDialog.value = false
  songToRemove.value = null
}

// 开始编辑歌单名
const startEditName = () => {
  if (!playlist.value) return
  editNameValue.value = playlist.value.name
  isEditingName.value = true
  nextTick(() => nameInputRef.value?.focus())
}

// 保存歌单名
const savePlaylistName = async () => {
  const name = editNameValue.value?.trim()
  if (!name || !playlist.value) return
  savingName.value = true
  try {
    const updated = await api.updatePlaylistName(playlist.value.id, name)
    if (updated) {
      playlist.value = updated
      isEditingName.value = false
    }
  } catch (e) {
    console.error('Failed to update playlist name:', e)
    alert('改名失败，请重试')
  } finally {
    savingName.value = false
  }
}

// 取消编辑歌单名
const cancelEditName = () => {
  isEditingName.value = false
  editNameValue.value = ''
}

// 置顶
const moveToTop = async (song: Song) => {
  if (!playlist.value) return
  try {
    const ok = await api.movePlaylistSongToTop(playlist.value.id, song.id)
    if (ok) await loadPlaylistDetail()
    else alert('置顶失败')
  } catch {
    alert('置顶失败，请重试')
  }
}

// 置底
const moveToBottom = async (song: Song) => {
  if (!playlist.value) return
  try {
    const ok = await api.movePlaylistSongToBottom(playlist.value.id, song.id)
    if (ok) await loadPlaylistDetail()
    else alert('置底失败')
  } catch {
    alert('置底失败，请重试')
  }
}

// 拖动开始
const onDragStart = (e: DragEvent, index: number) => {
  dragFromIndex.value = index
  const dt = e.dataTransfer
  if (dt) {
    dt.setData('text/plain', String(index))
    dt.effectAllowed = 'move'
  }
}

// 拖动结束
const onDragEnd = () => {
  dragFromIndex.value = null
  dragOverIndex.value = null
}

// 拖动经过：整行都可作为放置目标
const onDragOver = (e: DragEvent, toIndex: number) => {
  e.preventDefault()
  e.stopPropagation()
  const dt = e.dataTransfer
  if (dt) dt.dropEffect = 'move'
  dragOverIndex.value = toIndex
}

const onDragLeave = (_e: DragEvent, idx: number) => {
  if (dragOverIndex.value === idx) dragOverIndex.value = null
}

// 放下：重排并请求后端（from 优先从 dataTransfer 取，避免 dragend 先于 drop 清空）
const onDrop = async (e: DragEvent, toIndex: number) => {
  dragOverIndex.value = null
  const fromStr = e.dataTransfer?.getData('text/plain')
  const from = fromStr !== '' ? parseInt(fromStr, 10) : dragFromIndex.value
  if (Number.isNaN(from) || from === toIndex || !playlist.value) {
    dragFromIndex.value = null
    return
  }
  const list = [...playlistSongs.value]
  const [item] = list.splice(from, 1)
  list.splice(toIndex, 0, item)
  playlistSongs.value = list
  dragFromIndex.value = null
  reordering.value = true
  try {
    const ok = await api.reorderPlaylistSongs(playlist.value.id, list.map(s => s.id))
    if (!ok) {
      await loadPlaylistDetail()
      alert('排序保存失败，已恢复')
    }
  } catch {
    await loadPlaylistDetail()
    alert('排序失败，已恢复')
  } finally {
    reordering.value = false
  }
}

onMounted(() => {
  loadPlaylistDetail()
})

const goBack = () => {
  router.back()
}

const playSong = (song: Song, index: number) => {
  router.push({ 
    name: 'NowPlaying', 
    params: { id: song.id },
    query: { playlistId: playlist.value?.id }
  })
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
  transform: scale(0.95) translateY(20px);
}
</style>
