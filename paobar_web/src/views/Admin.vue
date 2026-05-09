<template>
  <div class="flex flex-col overflow-hidden" style="height: calc(100dvh - var(--status-height));">
    <!-- Header -->
    <header class="flex-shrink-0 bg-background-card/95 backdrop-blur-xl px-4 py-4">
      <div class="flex items-center relative">
        <button @click="$router.back()" class="p-1 absolute left-0">
          <ChevronLeft :size="24" class="text-text-primary" />
        </button>
        <h1 class="text-lg font-semibold text-center w-full">管理员后台</h1>
      </div>
      <div class="mt-3 relative">
        <Search :size="18" class="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary" />
        <input
          v-model="keyword"
          type="text"
          placeholder="按用户名搜索..."
          class="w-full bg-background-overlay/50 rounded-xl pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50"
          @keyup.enter="onSearch"
        />
      </div>
    </header>

    <!-- User List -->
    <div class="flex-1 min-h-0 overflow-y-auto px-4 py-4 space-y-3">
      <div v-if="loading && users.length === 0" class="text-center py-12">
        <div class="inline-block w-8 h-8 border-3 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p class="text-text-secondary text-sm mt-3">加载中...</p>
      </div>

      <div v-else-if="!loading && users.length === 0" class="text-center py-20">
        <Users :size="48" class="mx-auto text-text-secondary/30 mb-4" />
        <p class="text-text-secondary text-sm">没有匹配的用户</p>
      </div>

      <div
        v-for="user in users"
        :key="user.id"
        class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 flex items-start gap-3"
        :class="{ 'opacity-60': user.status === 0 }"
      >
        <!-- Avatar -->
        <div class="w-12 h-12 rounded-full overflow-hidden flex-shrink-0 bg-background-overlay/50">
          <img
            v-if="user.avatar"
            :src="user.avatar"
            :alt="user.username"
            class="w-full h-full object-cover"
            @error="onAvatarError"
          />
          <div v-else class="w-full h-full flex items-center justify-center text-text-secondary">
            <User :size="20" />
          </div>
        </div>

        <!-- Info -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 flex-wrap">
            <span class="font-medium text-text-primary truncate">{{ user.username }}</span>
            <span
              v-if="user.isAdmin"
              class="text-[10px] px-2 py-0.5 rounded-full bg-primary/15 text-primary"
            >管理员</span>
            <span
              v-if="user.status === 0"
              class="text-[10px] px-2 py-0.5 rounded-full bg-red-500/15 text-red-400"
            >已禁用</span>
            <span
              v-if="String(user.id) === String(currentUserId)"
              class="text-[10px] px-2 py-0.5 rounded-full bg-background-overlay/50 text-text-secondary"
            >当前账号</span>
          </div>
          <p class="text-xs text-text-secondary mt-0.5">
            ID: {{ user.id }} · 收藏 {{ user.collected ?? 0 }} · 歌单 {{ user.playlistsCount ?? 0 }}
          </p>
          <p v-if="user.createTime" class="text-xs text-text-secondary/70 mt-0.5">
            注册：{{ user.createTime }}
          </p>

          <!-- Actions：禁止操作自己 -->
          <div
            v-if="String(user.id) !== String(currentUserId)"
            class="mt-3 flex flex-wrap gap-2"
          >
            <button
              @click="toggleAdmin(user)"
              :disabled="!!busyId"
              class="text-xs px-3 py-1.5 rounded-lg transition-colors"
              :class="user.isAdmin
                ? 'bg-background-overlay/50 text-text-secondary hover:bg-background-overlay'
                : 'bg-primary/15 text-primary hover:bg-primary/25'"
            >
              {{ user.isAdmin ? '取消管理员' : '设为管理员' }}
            </button>
            <button
              @click="toggleStatus(user)"
              :disabled="!!busyId"
              class="text-xs px-3 py-1.5 rounded-lg transition-colors"
              :class="user.status === 1
                ? 'bg-red-500/10 text-red-400 hover:bg-red-500/20'
                : 'bg-green-500/10 text-green-400 hover:bg-green-500/20'"
            >
              {{ user.status === 1 ? '禁用账号' : '启用账号' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div
        v-if="total > pageSize"
        class="flex items-center justify-center gap-3 pt-2"
      >
        <button
          @click="goPage(page - 1)"
          :disabled="page <= 1 || loading"
          class="px-3 py-1.5 rounded-lg bg-background-card/50 text-sm text-text-secondary disabled:opacity-30"
        >上一页</button>
        <span class="text-xs text-text-secondary">
          第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 人）
        </span>
        <button
          @click="goPage(page + 1)"
          :disabled="page >= totalPages || loading"
          class="px-3 py-1.5 rounded-lg bg-background-card/50 text-sm text-text-secondary disabled:opacity-30"
        >下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ChevronLeft, Search, Users, User } from 'lucide-vue-next'
import { api } from '@/api'
import { currentUser } from '@/auth'
import type { AdminUser } from '@/types'

const users = ref<AdminUser[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const loading = ref(false)
const busyId = ref<string | null>(null)

const currentUserId = computed(() => currentUser.value?.id || '')
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const load = async () => {
  loading.value = true
  try {
    const res = await api.adminListUsers({
      keyword: keyword.value.trim() || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    users.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error('加载用户列表失败:', e)
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  page.value = 1
  load()
}

const goPage = (target: number) => {
  if (target < 1 || target > totalPages.value || target === page.value) return
  page.value = target
  load()
}

const toggleAdmin = async (user: AdminUser) => {
  if (String(user.id) === String(currentUserId.value)) return
  const next = !user.isAdmin
  const tip = next ? `确定将「${user.username}」升为管理员？` : `确定取消「${user.username}」的管理员身份？`
  if (!confirm(tip)) return
  busyId.value = user.id
  try {
    const ok = await api.adminSetUserAdmin(user.id, next)
    if (ok) {
      user.isAdmin = next
    } else {
      alert('操作失败')
    }
  } finally {
    busyId.value = null
  }
}

const toggleStatus = async (user: AdminUser) => {
  if (String(user.id) === String(currentUserId.value)) return
  const enable = user.status !== 1
  const tip = enable ? `确定启用账号「${user.username}」？` : `确定禁用账号「${user.username}」？被禁用的账号将无法登录。`
  if (!confirm(tip)) return
  busyId.value = user.id
  try {
    const ok = await api.adminSetUserStatus(user.id, enable)
    if (ok) {
      user.status = enable ? 1 : 0
    } else {
      alert('操作失败')
    }
  } finally {
    busyId.value = null
  }
}

const onAvatarError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}

onMounted(() => {
  load()
})
</script>
