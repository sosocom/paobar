<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-6 bg-background-page">
    <div class="w-full max-w-sm">
      <h1 class="text-2xl font-bold text-center text-text-primary mb-2">扒谱</h1>
      <p class="text-sm text-text-secondary text-center mb-8">登录后使用歌单、收藏等功能</p>

      <!-- Tab: 登录 / 注册 -->
      <div class="flex rounded-xl bg-background-overlay/50 p-1 mb-6">
        <button
          type="button"
          class="flex-1 py-2.5 rounded-lg text-sm font-medium transition-all"
          :class="isLogin ? 'bg-primary text-white' : 'text-text-secondary'"
          @click="isLogin = true"
        >
          登录
        </button>
        <button
          type="button"
          class="flex-1 py-2.5 rounded-lg text-sm font-medium transition-all"
          :class="!isLogin ? 'bg-primary text-white' : 'text-text-secondary'"
          @click="isLogin = false"
        >
          注册
        </button>
      </div>

      <form @submit.prevent="submit" class="space-y-4">
        <div>
          <label class="block text-sm text-text-secondary mb-1">用户名</label>
          <input
            v-model="username"
            type="text"
            required
            autocomplete="username"
            class="w-full bg-background-card rounded-xl px-4 py-3 text-text-primary border border-white/10 focus:border-primary/50 focus:outline-none"
            placeholder="请输入用户名"
          />
        </div>
        <div>
          <label class="block text-sm text-text-secondary mb-1">密码</label>
          <input
            v-model="password"
            type="password"
            required
            autocomplete="current-password"
            class="w-full bg-background-card rounded-xl px-4 py-3 text-text-primary border border-white/10 focus:border-primary/50 focus:outline-none"
            :placeholder="isLogin ? '请输入密码' : '至少 6 位密码'"
          />
        </div>
        <p v-if="error" class="text-sm text-red-400">{{ error }}</p>
        <button
          type="submit"
          :disabled="loading"
          class="w-full py-3 rounded-xl bg-primary text-white font-medium hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {{ loading ? '请稍候...' : (isLogin ? '登录' : '注册') }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '@/api'
import { setToken, setStoredUser } from '@/auth'

const router = useRouter()
const route = useRoute()
const isLogin = ref(true)
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

watch([isLogin], () => {
  error.value = ''
})

const submit = async () => {
  error.value = ''
  if (!username.value.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }
  if (!isLogin.value && password.value.length < 6) {
    error.value = '密码至少 6 位'
    return
  }
  loading.value = true
  try {
    const res = isLogin.value
      ? await api.login(username.value.trim(), password.value)
      : await api.register(username.value.trim(), password.value)
    setToken(res.token)
    setStoredUser(res.user)
    const redirect = (route.query.redirect as string) || '/index'
    router.replace(redirect)
  } catch (e: any) {
    error.value = e?.message || (isLogin.value ? '登录失败' : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>
