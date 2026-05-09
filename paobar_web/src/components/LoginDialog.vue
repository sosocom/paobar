<template>
  <Transition name="login-dialog">
    <div v-if="showLoginDialog" class="fixed inset-0 z-[100] flex items-center justify-center p-4" @click.self="dismiss">
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="dismiss"></div>

      <!-- Centered Dialog -->
      <div class="relative w-full max-w-md bg-background-card rounded-3xl px-6 pt-6 pb-6 animate-fade-in-up shadow-2xl">
        <!-- Close Button -->
        <button @click="dismiss" class="absolute top-4 right-4 p-1 text-text-secondary hover:text-text-primary transition-colors">
          <X :size="20" />
        </button>

        <h2 class="text-xl font-bold text-text-primary mb-1 pr-8">{{ isLogin ? '登录' : '注册' }}</h2>
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
import { useRouter } from 'vue-router'
import { X } from 'lucide-vue-next'
import { api } from '@/api'
import { setToken, setStoredUser } from '@/auth'
import { showLoginDialog, onLoginSuccess, dismissLoginDialog } from '@/authBus'

const router = useRouter()

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
    onLoginSuccess() // 关闭弹窗 + 执行排队回调（收藏/加歌单等失败前置动作）
    // 登录成功统一落到"我的"页，让用户看到账号已就绪 + 刷新最新数据。
    // 如果当前已经在 /profile（例如从底部 Tab 触发登录），手动调 Profile 内的刷新事件。
    if (router.currentRoute.value.name === 'Profile') {
      window.dispatchEvent(new Event('profile:refresh'))
    } else {
      router.push({ name: 'Profile' })
    }
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
.animate-fade-in-up {
  animation: fadeInUp 0.22s ease-out;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px) scale(0.98); }
  to   { opacity: 1; transform: translateY(0)    scale(1);    }
}
</style>
