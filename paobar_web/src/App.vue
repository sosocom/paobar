<template>
  <div class="min-h-screen bg-background-page text-text-primary">
    <router-view />
    <BottomNav v-if="showBottomNav" />
    <!-- 全局登录弹窗 -->
    <LoginDialog />
    <!-- PWA 安装到主屏幕引导（仅在有底部导航的主页面里显示，
         保证定位到 nav 上方；安装/稍后/iOS 指引逻辑全在组件内部） -->
    <InstallPrompt v-if="showBottomNav" />
    <!-- 手动触发的安装向导（全局可用，首页 App 按钮等入口都能拉起） -->
    <InstallWizardDialog />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import LoginDialog from '@/components/LoginDialog.vue'
import InstallPrompt from '@/components/InstallPrompt.vue'
import InstallWizardDialog from '@/components/InstallWizardDialog.vue'

const route = useRoute()

// 子页面和特殊页面不显示底部导航
const hideNavPages = ['NowPlaying', 'Login', 'Favorites', 'Settings']
const showBottomNav = computed(() => {
  return !hideNavPages.includes(route.name as string)
})
</script>
