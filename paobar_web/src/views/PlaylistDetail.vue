<template>
  <div class="min-h-screen pb-nav">
    <!-- Sticky 透明顶栏：仅保留返回按钮，针对歌单整体的操作下沉到 Hero -->
    <header class="sticky top-0 z-40 bg-gradient-to-b from-background-card/85 via-background-card/35 to-transparent backdrop-blur-md">
      <div class="px-3 py-2.5 flex items-center">
        <button
          @click="goBack"
          class="p-2 rounded-full hover:bg-background-overlay/50 transition-colors"
          aria-label="返回"
        >
          <ChevronLeft :size="22" class="text-text-primary" />
        </button>
      </div>
    </header>

    <!-- Hero：封面 + 标题 + 类型徽章；带 ambient glow -->
    <section v-if="playlist" class="relative px-4 pt-2 pb-5 overflow-hidden">
      <div
        aria-hidden="true"
        class="pointer-events-none absolute -top-16 -left-16 w-80 h-80 rounded-full opacity-35 blur-3xl"
        :style="`background: radial-gradient(circle, ${playlist.gradient?.[0] || '#6366f1'}, transparent 70%)`"
      ></div>
      <div
        aria-hidden="true"
        class="pointer-events-none absolute -top-8 right-0 w-72 h-72 rounded-full opacity-30 blur-3xl"
        :style="`background: radial-gradient(circle, ${playlist.gradient?.[1] || '#a855f7'}, transparent 70%)`"
      ></div>

      <div class="relative flex items-center gap-4">
        <!-- Cover -->
        <div
          class="w-28 h-28 rounded-2xl flex items-center justify-center flex-shrink-0 shadow-xl shadow-black/40 ring-1 ring-white/10"
          :style="`background: linear-gradient(135deg, ${playlist.gradient?.[0] || '#6366f1'}, ${playlist.gradient?.[1] || '#a855f7'})`"
        >
          <Sparkles v-if="playlist.type === 'ai'" :size="44" class="text-white drop-shadow" />
          <ListMusic v-else :size="44" class="text-white drop-shadow" />
        </div>

        <!-- 右侧：标题 + 编辑/删除（同排） + 元信息 -->
        <div class="flex-1 min-w-0">
          <template v-if="isEditingName">
            <input
              v-model="editNameValue"
              ref="nameInputRef"
              type="text"
              class="w-full text-xl font-bold bg-background-overlay/60 rounded-lg px-2 py-1.5 border border-white/10 focus:border-primary/50 focus:outline-none mb-2"
              @keydown.enter="savePlaylistName"
              @keydown.escape="cancelEditName"
            />
            <div class="flex items-center gap-2">
              <button
                @click="savePlaylistName"
                :disabled="savingName || !editNameValue?.trim()"
                class="px-3 py-1.5 rounded-lg bg-primary text-white text-sm font-medium hover:bg-primary/90 disabled:opacity-50 inline-flex items-center gap-1.5"
              >
                <Check :size="14" />
                保存
              </button>
              <button
                @click="cancelEditName"
                :disabled="savingName"
                class="px-3 py-1.5 rounded-lg bg-background-overlay/50 text-text-secondary text-sm hover:bg-background-overlay disabled:opacity-50 inline-flex items-center gap-1.5"
              >
                <X :size="14" />
                取消
              </button>
            </div>
          </template>
          <template v-else>
            <!-- 第一行：标题 + 编辑/删除 -->
            <div class="flex items-start gap-3 mb-2">
              <h2 class="flex-1 min-w-0 text-2xl font-bold leading-tight line-clamp-2 break-words">{{ playlist.name }}</h2>
              <div
                v-if="playlist.type === 'user'"
                class="flex items-center gap-1 flex-shrink-0 -mr-1"
              >
                <button
                  @click="startEditName"
                  class="p-1.5 rounded-full text-text-secondary hover:text-text-primary hover:bg-background-overlay/60 transition-colors"
                  title="重命名"
                  aria-label="重命名"
                >
                  <Pencil :size="16" />
                </button>
                <button
                  @click="showDeletePlaylistDialog = true"
                  class="p-1.5 rounded-full text-text-secondary hover:text-red-400 hover:bg-red-500/10 transition-colors"
                  title="删除歌单"
                  aria-label="删除歌单"
                >
                  <Trash2 :size="16" />
                </button>
              </div>
            </div>

            <!-- 第二行：meta（左对齐保持原间距）+ 播放全部（右对齐，同一行） -->
            <div class="flex items-center gap-2 flex-wrap">
              <span
                v-if="playlist.type === 'ai'"
                class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-primary/15 text-primary tracking-wide"
              >
                <Sparkles :size="10" />
                AI 推荐
              </span>
              <span
                v-else
                class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-white/10 text-text-secondary tracking-wide"
              >
                我的歌单
              </span>
              <span class="text-[11px] text-text-secondary">
                共 {{ playlist.songCount ?? 0 }} 首
              </span>
              <span
                v-if="playlist.type === 'ai' && playlist.chordProgression"
                class="text-[11px] text-text-secondary"
              >
                · 和弦走向
                <span class="text-primary font-medium ml-0.5">{{ playlist.chordProgression }}</span>
              </span>

              <button
                @click="playAll"
                :disabled="playlistSongs.length === 0"
                class="ml-auto inline-flex items-center gap-1 px-3 py-1 rounded-full bg-primary text-white text-xs font-semibold shadow-md shadow-primary/30 active:scale-95 transition-transform disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100"
              >
                <Play :size="12" class="fill-white ml-0.5" />
                播放全部
              </button>
            </div>
          </template>
        </div>
      </div>
    </section>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
      <p class="text-text-secondary text-sm mt-3">加载中...</p>
    </div>

    <!-- Song List (可拖动排序，左右顶满) -->
    <div v-else class="pb-4 space-y-2 song-list-drag">
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
        <!-- 拖动手柄（仅用户歌单，放在序号之前） -->
        <div
          v-if="playlist?.type === 'user'"
          class="flex items-center justify-center w-6 h-10 flex-shrink-0 cursor-grab active:cursor-grabbing select-none text-text-secondary/60 hover:text-text-secondary transition-colors"
          draggable="true"
          @dragstart="onDragStart($event, idx)"
          @dragend="onDragEnd"
          title="拖动排序"
        >
          <GripVertical :size="16" class="pointer-events-none" />
        </div>

        <!-- 整行可点击：序号 + 信息（点击 = 播放） -->
        <div 
          @click="playSong(song, idx)"
          class="flex gap-3 flex-1 min-w-0 cursor-pointer items-center"
        >
          <div class="flex items-center justify-center w-6 text-text-secondary text-sm font-medium flex-shrink-0">
            {{ idx + 1 }}
          </div>
          
          <div class="flex-1 min-w-0">
            <h3 class="font-medium truncate mb-1">{{ song.title }}</h3>
            <p class="text-sm text-text-secondary truncate" :class="{ 'mb-1': song.meta }">{{ song.artist }}</p>
            <p v-if="song.meta" class="text-xs text-text-secondary truncate">
              {{ song.meta }}
            </p>
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
        
        <!-- 移出歌单：小图标，最右 -->
        <button 
          @click.stop="confirmRemoveSong(song)"
          class="flex items-center justify-center p-2 rounded-lg text-text-secondary/70 hover:text-red-400 hover:bg-red-500/10 transition-colors flex-shrink-0"
          title="移出歌单"
          aria-label="移出歌单"
        >
          <Trash2 :size="16" />
        </button>
      </div>
      
      <!-- Empty State -->
      <div v-if="playlistSongs.length === 0" class="text-center py-12">
        <Music :size="48" class="text-text-secondary mx-auto mb-3 opacity-30" />
        <p class="text-text-secondary">歌单为空</p>
      </div>
    </div>

    <!-- Confirm Delete Playlist Dialog -->
    <Transition name="dialog">
      <div v-if="showDeletePlaylistDialog" class="fixed inset-0 z-50 flex items-center justify-center" @click.self="closeDeletePlaylistDialog">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60" @click="closeDeletePlaylistDialog"></div>

        <!-- Dialog Content -->
        <div class="relative bg-background-card rounded-3xl p-6 w-[90%] max-w-md">
          <h2 class="text-lg font-semibold mb-2">删除歌单</h2>
          <p class="text-sm text-text-secondary mb-4">
            确定要删除歌单《{{ playlist?.name }}》吗？此操作不可恢复，歌单内的歌曲收藏关系将被移除。
          </p>

          <!-- Buttons -->
          <div class="flex gap-3">
            <button
              @click="closeDeletePlaylistDialog"
              :disabled="deletingPlaylist"
              class="flex-1 py-3 rounded-xl bg-background-overlay/50 text-text-secondary hover:bg-background-overlay disabled:opacity-50 transition-colors"
            >
              取消
            </button>
            <button
              @click="deletePlaylist"
              :disabled="deletingPlaylist"
              class="flex-1 py-3 rounded-xl bg-red-500 text-white hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {{ deletingPlaylist ? '删除中...' : '删除' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

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
import { ChevronLeft, Play, ListMusic, Sparkles, Music, X, Pencil, Check, GripVertical, ArrowUpToLine, ArrowDownToLine, Trash2 } from 'lucide-vue-next'
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

// 删除整个歌单
const showDeletePlaylistDialog = ref(false)
const deletingPlaylist = ref(false)

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

// 删除整个歌单
const deletePlaylist = async () => {
  if (!playlist.value) return
  deletingPlaylist.value = true
  try {
    const ok = await api.deletePlaylist(playlist.value.id)
    if (ok) {
      closeDeletePlaylistDialog()
      router.replace({ name: 'Playlists' })
    } else {
      alert('删除失败，请重试')
    }
  } catch (error) {
    console.error('Failed to delete playlist:', error)
    alert('删除失败，请重试')
  } finally {
    deletingPlaylist.value = false
  }
}

const closeDeletePlaylistDialog = () => {
  if (deletingPlaylist.value) return
  showDeletePlaylistDialog.value = false
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

// 播放全部：跳到第一首并附上 playlistId，由 NowPlaying 接管顺序播放
const playAll = () => {
  if (!playlist.value || playlistSongs.value.length === 0) return
  playSong(playlistSongs.value[0], 0)
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
