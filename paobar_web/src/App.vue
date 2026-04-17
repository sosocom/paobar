<template>
  <div class="min-h-screen bg-background-page text-text-primary">
    <router-view />
    <BottomNav v-if="showBottomNav" />
    <!-- 全局登录弹窗 -->
    <LoginDialog />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import LoginDialog from '@/components/LoginDialog.vue'

const route = useRoute()

// 子页面和特殊页面不显示底部导航
const hideNavPages = ['NowPlaying', 'Login', 'Favorites', 'Settings']
const showBottomNav = computed(() => {
  return !hideNavPages.includes(route.name as string)
})
</script>
