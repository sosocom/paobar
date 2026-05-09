<!--
  安装到主屏幕：底部自动提示条

  这是一个被动提示：
    - 3 秒缓冲后判断是否出现（iOS Safari / Android 拿到 BIP 事件）
    - 点"稍后"→ 7 天内不再出现
    - 点主 CTA → 打开 `InstallWizardDialog`（手动向导），由它处理所有平台分支
      这样微信/iOS 非 Safari 等被限制的环境也能看到正确的引导，不再有"点了按钮看不懂的教程"问题。
    - 已 standalone（已装过） → 永不出现
-->
<template>
  <Transition name="slide-up">
    <div
      v-if="autoBarVisible"
      class="fixed left-3 right-3 z-[60] bg-background-card/95 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl overflow-hidden"
      :style="{ bottom: `calc(var(--nav-height) + 0.75rem)` }"
    >
      <div class="flex items-center gap-3 p-3">
        <div class="w-10 h-10 rounded-xl bg-primary/15 flex items-center justify-center flex-shrink-0">
          <Smartphone :size="20" class="text-primary" />
        </div>

        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium truncate">添加到主屏幕</p>
          <p class="text-[11px] text-text-secondary truncate">
            {{ subtitle }}
          </p>
        </div>

        <button
          type="button"
          class="px-3 py-1.5 text-[11px] text-text-secondary hover:text-text-primary transition-colors"
          @click="dismissAutoBar"
        >
          稍后
        </button>
        <button
          type="button"
          class="px-3 py-1.5 text-xs rounded-lg bg-primary text-white font-medium hover:brightness-110 transition-all flex-shrink-0"
          @click="openInstallWizard"
        >
          {{ ctaLabel }}
        </button>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { Smartphone } from 'lucide-vue-next'
import {
  isIOS,
  isIOSNonSafari,
  isInAppBrowser,
  autoBarVisible,
  canPromptNative,
  dismissAutoBar,
  openInstallWizard,
  maybeShowAutoBar,
} from '@/installBus'

// 副标题 / CTA 文案跟着环境走，避免"装 App"按钮出现在不支持的场景里
const subtitle = computed(() => {
  if (isInAppBrowser) return '在微信/QQ 里没法装，点这里看解决方法'
  if (isIOSNonSafari) return '苹果只支持 Safari，切换浏览器即可'
  if (isIOS) return '像 App 一样全屏运行，再也没有浏览器栏'
  return '一键安装，全屏沉浸体验'
})

const ctaLabel = computed(() => {
  if (isInAppBrowser || isIOSNonSafari) return '查看方法'
  if (canPromptNative.value) return '安装'
  return '查看方法'
})

onMounted(() => {
  // iOS 或延迟到位的 Android 都给 3 秒缓冲，避免刚进页面就弹
  window.setTimeout(maybeShowAutoBar, 3000)
})
</script>

<style scoped>
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.35s cubic-bezier(0.2, 0.9, 0.2, 1), opacity 0.25s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(120%);
  opacity: 0;
}
</style>
