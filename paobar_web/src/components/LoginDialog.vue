<template>
  <Transition name="login-dialog">
    <div v-if="showLoginDialog" class="fixed inset-0 z-[100] flex items-end justify-center" @click.self="dismiss">
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="dismiss"></div>

      <!-- Bottom Sheet -->
      <div class="relative w-full max-w-md bg-background-card rounded-t-3xl px-6 pt-5 pb-8 animate-slide-up">
        <!-- Drag Handle -->
        <div class="flex justify-center mb-4">
          <div class="w-10 h-1 bg-text-secondary/30 rounded-full"></div>
        </div>

        <!-- Close Button -->
        <button @click="dismiss" class="absolute top-5 right-5 p-1 text-text-secondary hover:text-text-primary transition-colors">
          <X :size="20" />
        </button>

        <h2 class="text-xl font-bold text-text-primary mb-1">{{ isLogin ? '登录' : '注册' }}</h2>
        <p class="text-sm text-text-secondary mb-5">登录后即可使用收藏、歌单等功能</p>

        <!-- Tab -->
        <div class="flex rounded-xl bg-background-overlay/50 p-1 mb-5">
          <button
            type="button"
            class="flex-1 py-2 rounded-lg text-sm font-medium transition-all"
            :class="isLogin ? 'bg-primary text-white' : 'text-text-secondary'"
            @click="isLogin = true; error = ''"
          >登录</button>
          <button
            type="button"
            class="flex-1 py-2 rounded-lg text-sm font-medium transition-all"
            :class="!isLogin ? 'bg-primary text-white' : 'text-text-secondary'"
            @click="isLogin = false; error = ''"
          >注册</button>
        </div>

        <!-- Form -->
        <form @submit.prevent="submit" class="space-y-3">
          <input
            v-model="username"
            type="text"
            required
            autocomplete="username"
            class="w-full bg-background-overlay/50 rounded-xl px-4 py-3 text-sm text-text-primary border border-white/10 focus:border-primary/50 focus:outline-none"
            placeholder="用户名"
          />
          <input
            v-model="password"
            type="password"
            required
            autocomplete="current-password"
            class="w-full bg-background-overlay/50 rounded-xl px-4 py-3 text-sm text-text-primary border border-white/10 focus:border-primary/50 focus:outline-none"
            :placeholder="isLogin ? '密码' : '至少 6 位密码'"
          />
          <p v-if="error" class="text-xs text-red-400">{{ error }}</p>
          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-50 transition-colors"
          >
            {{ loading ? '请稍候...' : (isLogin ? '登录' : '注册') }}
          </button>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { X } from 'lucide-vue-next'
import { api } from '@/api'
import { setToken, setStoredUser } from '@/auth'
import { showLoginDialog, onLoginSuccess, dismissLoginDialog } from '@/authBus'

const isLogin = ref(true)
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

// 每次打开弹窗时重置表单
watch(showLoginDialog, (val) => {
  if (val) {
    username.value = ''
    password.value = ''
    error.value = ''
    isLogin.value = true
  }
})

const dismiss = () => {
  dismissLoginDialog()
}

const submit = async () => {
  error.value = ''
  if (!username.value.trim()) { error.value = '请输入用户名'; return }
  if (!password.value) { error.value = '请输入密码'; return }
  if (!isLogin.value && password.value.length < 6) { error.value = '密码至少 6 位'; return }

  loading.value = true
  try {
    const res = isLogin.value
      ? await api.login(username.value.trim(), password.value)
      : await api.register(username.value.trim(), password.value)
    setToken(res.token)
    setStoredUser(res.user)
    onLoginSuccess() // 关闭弹窗 + 执行回调
  } catch (e: any) {
    error.value = e?.message || (isLogin.value ? '登录失败' : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-dialog-enter-active,
.login-dialog-leave-active {
  transition: opacity 0.25s ease;
}
.login-dialog-enter-from,
.login-dialog-leave-to {
  opacity: 0;
}
.animate-slide-up {
  animation: slideUp 0.3s ease-out;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to   { transform: translateY(0); }
}
</style>
