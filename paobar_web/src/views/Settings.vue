<template>
  <div class="min-h-screen">
    <!-- Header -->
    <header class="bg-background-card/95 backdrop-blur-xl px-4 py-4">
      <div class="flex items-center relative">
        <button @click="$router.back()" class="p-1 absolute left-0">
          <ChevronLeft :size="24" class="text-text-primary" />
        </button>
        <h1 class="text-lg font-semibold text-center w-full">设置</h1>
      </div>
    </header>

    <div class="px-4 py-6 space-y-6">
      <!-- Account Section -->
      <section>
        <h2 class="text-xs text-text-secondary uppercase tracking-wider mb-3 px-1">账号</h2>
        <div class="bg-background-card/50 backdrop-blur-sm rounded-2xl overflow-hidden divide-y divide-white/5">
          <div class="p-4 flex items-center justify-between">
            <span class="text-text-primary">用户名</span>
            <span class="text-text-secondary text-sm">{{ username }}</span>
          </div>
          <button @click="showChangePassword = !showChangePassword" class="p-4 w-full flex items-center justify-between">
            <span class="text-text-primary">修改密码</span>
            <ChevronRight :size="18" class="text-text-secondary" />
          </button>
        </div>

        <!-- Change Password Form -->
        <div v-if="showChangePassword" class="bg-background-card/50 backdrop-blur-sm rounded-2xl p-4 mt-2 space-y-3">
          <div>
            <label class="text-xs text-text-secondary mb-1 block">当前密码</label>
            <input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="输入当前密码"
              class="w-full bg-background-overlay/50 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50"
            />
          </div>
          <div>
            <label class="text-xs text-text-secondary mb-1 block">新密码</label>
            <input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="输入新密码"
              class="w-full bg-background-overlay/50 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50"
            />
          </div>
          <div>
            <label class="text-xs text-text-secondary mb-1 block">确认新密码</label>
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
              class="w-full bg-background-overlay/50 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50"
            />
          </div>
          <div class="flex gap-2 pt-1">
            <button
              @click="showChangePassword = false"
              class="flex-1 px-4 py-2.5 bg-background-overlay/50 rounded-xl text-sm text-text-secondary hover:bg-background-overlay transition-colors"
            >
              取消
            </button>
            <button
              @click="changePassword"
              :disabled="changingPassword"
              class="flex-1 px-4 py-2.5 bg-primary rounded-xl text-sm text-white hover:bg-primary/90 transition-colors disabled:opacity-50"
            >
              {{ changingPassword ? '提交中...' : '确认修改' }}
            </button>
          </div>
          <p v-if="passwordMsg" class="text-xs" :class="passwordSuccess ? 'text-green-400' : 'text-red-400'">{{ passwordMsg }}</p>
        </div>
      </section>

      <!-- Display Section -->
      <section>
        <h2 class="text-xs text-text-secondary uppercase tracking-wider mb-3 px-1">显示</h2>
        <div class="bg-background-card/50 backdrop-blur-sm rounded-2xl overflow-hidden divide-y divide-white/5">
          <div class="p-4 flex items-center justify-between">
            <span class="text-text-primary">字体大小</span>
            <div class="flex items-center gap-2">
              <button
                v-for="size in fontSizes"
                :key="size.value"
                @click="setFontSize(size.value)"
                class="px-3 py-1 rounded-lg text-xs transition-all"
                :class="currentFontSize === size.value
                  ? 'bg-primary text-white'
                  : 'bg-background-overlay/50 text-text-secondary hover:bg-background-overlay'"
              >
                {{ size.label }}
              </button>
            </div>
          </div>
          <div class="p-4 flex items-center justify-between">
            <span class="text-text-primary">自动滚动</span>
            <button
              @click="autoScroll = !autoScroll"
              class="w-12 h-7 rounded-full transition-colors relative"
              :class="autoScroll ? 'bg-primary' : 'bg-background-overlay/50'"
            >
              <span
                class="absolute top-0.5 w-6 h-6 bg-white rounded-full shadow transition-transform"
                :class="autoScroll ? 'translate-x-5' : 'translate-x-0.5'"
              ></span>
            </button>
          </div>
        </div>
      </section>

      <!-- About Section -->
      <section>
        <h2 class="text-xs text-text-secondary uppercase tracking-wider mb-3 px-1">关于</h2>
        <div class="bg-background-card/50 backdrop-blur-sm rounded-2xl overflow-hidden divide-y divide-white/5">
          <div class="p-4 flex items-center justify-between">
            <span class="text-text-primary">版本</span>
            <span class="text-text-secondary text-sm">1.0.0</span>
          </div>
          <div class="p-4 flex items-center justify-between">
            <span class="text-text-primary">清除缓存</span>
            <button
              @click="clearCache"
              class="px-3 py-1 bg-background-overlay/50 rounded-lg text-xs text-text-secondary hover:bg-background-overlay transition-colors"
            >
              清除
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { getStoredUser } from '@/auth'

const user = getStoredUser()
const username = ref(user?.username || '未知')

// --- 修改密码 ---
const showChangePassword = ref(false)
const changingPassword = ref(false)
const passwordMsg = ref('')
const passwordSuccess = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const changePassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value
  passwordMsg.value = ''
  if (!oldPassword || !newPassword) {
    passwordMsg.value = '请填写完整'
    passwordSuccess.value = false
    return
  }
  if (newPassword.length < 4) {
    passwordMsg.value = '新密码长度至少 4 位'
    passwordSuccess.value = false
    return
  }
  if (newPassword !== confirmPassword) {
    passwordMsg.value = '两次输入的密码不一致'
    passwordSuccess.value = false
    return
  }
  changingPassword.value = true
  try {
    const { getToken } = await import('@/auth')
    const token = getToken()
    const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8001'
    const res = await fetch(`${API_BASE_URL}/api/user/password`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({ oldPassword, newPassword }),
    })
    const result = await res.json().catch(() => ({ code: 500, message: '网络错误' }))
    if (result.code === 200) {
      passwordMsg.value = '密码修改成功'
      passwordSuccess.value = true
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
      setTimeout(() => { showChangePassword.value = false; passwordMsg.value = '' }, 1500)
    } else {
      passwordMsg.value = result.message || '修改失败'
      passwordSuccess.value = false
    }
  } catch {
    passwordMsg.value = '网络错误'
    passwordSuccess.value = false
  } finally {
    changingPassword.value = false
  }
}

// --- 字体大小 ---
const fontSizes = [
  { label: '小', value: 'small' },
  { label: '中', value: 'medium' },
  { label: '大', value: 'large' },
]
const currentFontSize = ref(localStorage.getItem('paobar_font_size') || 'medium')
const setFontSize = (size: string) => {
  currentFontSize.value = size
  localStorage.setItem('paobar_font_size', size)
}

// --- 自动滚动 ---
const autoScroll = ref(localStorage.getItem('paobar_auto_scroll') !== 'false')

// --- 清除缓存 ---
const clearCache = () => {
  const keysToKeep = ['paobar_token', 'paobar_user']
  const allKeys: string[] = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && !keysToKeep.includes(key)) {
      allKeys.push(key)
    }
  }
  allKeys.forEach(k => localStorage.removeItem(k))
  alert('缓存已清除')
}
</script>
