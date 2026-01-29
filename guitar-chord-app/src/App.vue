<template>
  <div class="min-h-screen bg-background-page text-text-primary">
    <router-view />
    <BottomNav v-if="showBottomNav" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'

const route = useRoute()

// 只在主要页面显示底部导航，播放页面不显示
const showBottomNav = computed(() => {
  return route.name !== 'NowPlaying'
})
</script>
