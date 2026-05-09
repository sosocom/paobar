<!--
  PWA 安装向导（居中弹窗）

  由 `installBus.openInstallWizard()` 触发——首页"全部/已收藏"行的 App 按钮。
  比底部条 `InstallPrompt.vue` 更强势：一个 modal，解释清楚收益 + 一键安装/平台专属教程。

  平台分支（优先级从高到低）：
    1. 'inapp'     — 微信/QQ/抖音/小红书 等 App 内置浏览器：
                     完全禁用了 PWA 安装入口，只能引导用户"复制链接到系统浏览器打开"。
    2. 'ios-other' — iOS 上用的是 Chrome/Edge/UC 等非 Safari 浏览器：
                     Apple 只允许 Safari 加到主屏。引导用户复制链接改用 Safari 打开。
    3. 'native'    — Android/Chrome/Edge 且拿到 deferredPrompt：
                     主 CTA 是"立即安装"，直接调起浏览器原生安装弹窗。
    4. 'ios'       — iOS Safari：没法 programmatic install，展示 3 步引导。
    5. 'fallback'  — 桌面 Firefox / 错过事件的 Chrome 等：
                     引导去浏览器菜单里找"安装应用"。
-->
<template>
  <Transition name="dialog-fade">
    <div
      v-if="wizardVisible"
      class="fixed inset-0 z-[90] flex items-center justify-center p-4"
      @click.self="close"
    >
      <!-- 背景遮罩 -->
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="close"></div>

      <!-- 弹窗主体 -->
      <div
        class="relative w-full max-w-sm bg-background-card rounded-3xl p-6 shadow-2xl animate-fade-in-up"
      >
        <!-- 关闭按钮 -->
        <button
          type="button"
          class="absolute top-3 right-3 p-1 text-text-secondary hover:text-text-primary transition-colors"
          @click="close"
          aria-label="关闭"
        >
          <X :size="20" />
        </button>

        <!-- 头部：图标 + 标题 + 价值主张 -->
        <div class="flex flex-col items-center text-center mb-5">
          <div class="w-16 h-16 rounded-2xl bg-primary/15 flex items-center justify-center mb-3">
            <component :is="headerIcon" :size="32" class="text-primary" />
          </div>
          <h2 class="text-lg font-semibold text-text-primary mb-1">{{ headerTitle }}</h2>
          <p class="text-xs text-text-secondary leading-relaxed" v-html="headerDesc"></p>
        </div>

        <!-- 特性列表（仅在"能安装"的分支下展示） -->
        <ul v-if="mode === 'native' || mode === 'ios' || mode === 'fallback'" class="space-y-2 mb-5">
          <li class="flex items-center gap-2 text-xs text-text-secondary">
            <Check :size="14" class="text-primary flex-shrink-0" />
            <span>桌面图标，一键打开</span>
          </li>
          <li class="flex items-center gap-2 text-xs text-text-secondary">
            <Check :size="14" class="text-primary flex-shrink-0" />
            <span>全屏沉浸，没有地址栏干扰</span>
          </li>
          <li class="flex items-center gap-2 text-xs text-text-secondary">
            <Check :size="14" class="text-primary flex-shrink-0" />
            <span>自动缓存，离线也能弹唱</span>
          </li>
        </ul>

        <!-- 分支 1：App 内置浏览器（微信/QQ 等） -->
        <template v-if="mode === 'inapp'">
          <!-- 右上角箭头指引图示 -->
          <div class="relative bg-background-overlay/30 rounded-xl p-4 mb-4">
            <div class="flex items-start gap-3">
              <div class="flex flex-col items-center gap-1 flex-shrink-0">
                <MoreHorizontal :size="22" class="text-primary" />
                <span class="text-[10px] text-text-secondary">右上角</span>
              </div>
              <div class="flex-1 text-xs text-text-secondary leading-relaxed">
                点屏幕右上角的 <span class="text-text-primary font-medium">•••</span> 菜单 →
                选 <span class="text-text-primary font-medium">"在浏览器中打开"</span> /
                <span class="text-text-primary font-medium">"用 Safari 打开"</span>
              </div>
            </div>
          </div>
          <p class="text-xs text-text-secondary leading-relaxed mb-4 text-center">
            或者复制下面的链接，粘贴到系统浏览器地址栏
          </p>
          <button
            type="button"
            class="w-full flex items-center justify-center gap-2 py-3 rounded-xl bg-primary text-white text-sm font-medium hover:brightness-110 transition-all mb-2"
            @click="onCopyLink"
          >
            <Copy :size="16" />
            <span>{{ copyState === 'done' ? '已复制，去浏览器粘贴打开' : '复制链接' }}</span>
          </button>
          <button
            type="button"
            class="w-full py-2.5 text-xs text-text-secondary hover:text-text-primary transition-colors"
            @click="close"
          >
            稍后再说
          </button>
        </template>

        <!-- 分支 2：iOS 非 Safari（iOS Chrome/Edge/UC 等） -->
        <template v-else-if="mode === 'ios-other'">
          <p class="text-xs text-text-secondary leading-relaxed mb-4 text-center">
            苹果系统只允许
            <span class="text-text-primary font-medium">Safari</span>
            把网页加到主屏。请复制链接后用 Safari 打开，再点页面里的「App」按钮。
          </p>
          <button
            type="button"
            class="w-full flex items-center justify-center gap-2 py-3 rounded-xl bg-primary text-white text-sm font-medium hover:brightness-110 transition-all mb-2"
            @click="onCopyLink"
          >
            <Copy :size="16" />
            <span>{{ copyState === 'done' ? '已复制，打开 Safari 粘贴' : '复制链接' }}</span>
          </button>
          <button
            type="button"
            class="w-full py-2.5 text-xs text-text-secondary hover:text-text-primary transition-colors"
            @click="close"
          >
            稍后再说
          </button>
        </template>

        <!-- 分支 3：Android/Chrome 可一键 -->
        <template v-else-if="mode === 'native'">
          <button
            type="button"
            class="w-full py-3 rounded-xl bg-primary text-white text-sm font-medium hover:brightness-110 transition-all mb-2"
            :disabled="installing"
            @click="onInstallNative"
          >
            <span v-if="!installing">立即安装</span>
            <span v-else>正在弹出安装窗口…</span>
          </button>
          <button
            type="button"
            class="w-full py-2.5 text-xs text-text-secondary hover:text-text-primary transition-colors"
            @click="close"
          >
            暂不安装
          </button>
        </template>

        <!-- 分支 4：iOS Safari 分步引导 -->
        <template v-else-if="mode === 'ios'">
          <ol class="space-y-2.5 mb-5">
            <li class="flex items-start gap-3">
              <span class="inline-flex w-5 h-5 rounded-full bg-primary/20 text-primary items-center justify-center text-[11px] font-semibold flex-shrink-0">1</span>
              <span class="text-xs text-text-secondary leading-relaxed">
                点 Safari 底部中间的 <span class="text-text-primary font-medium">共享按钮</span>
                （一个向上箭头的方框）
              </span>
            </li>
            <li class="flex items-start gap-3">
              <span class="inline-flex w-5 h-5 rounded-full bg-primary/20 text-primary items-center justify-center text-[11px] font-semibold flex-shrink-0">2</span>
              <span class="text-xs text-text-secondary leading-relaxed">
                菜单中向下滑动，选
                <span class="text-text-primary font-medium">"添加到主屏幕"</span>
              </span>
            </li>
            <li class="flex items-start gap-3">
              <span class="inline-flex w-5 h-5 rounded-full bg-primary/20 text-primary items-center justify-center text-[11px] font-semibold flex-shrink-0">3</span>
              <span class="text-xs text-text-secondary leading-relaxed">
                右上角点 <span class="text-text-primary font-medium">"添加"</span>
                ，桌面就会出现 <span class="text-text-primary font-medium">"泡吧吉他谱"</span> 图标
              </span>
            </li>
          </ol>
          <button
            type="button"
            class="w-full py-2.5 rounded-xl bg-background-overlay/50 text-text-secondary text-xs hover:bg-background-overlay transition-colors"
            @click="close"
          >
            我知道了
          </button>
        </template>

        <!-- 分支 5：通用 fallback -->
        <template v-else>
          <p class="text-xs text-text-secondary leading-relaxed mb-4 text-center">
            在浏览器地址栏右侧或菜单里找
            <span class="text-text-primary font-medium">"安装应用"</span> /
            <span class="text-text-primary font-medium">"添加到主屏幕"</span>，
            点击即可装到桌面。
          </p>
          <button
            type="button"
            class="w-full py-2.5 rounded-xl bg-background-overlay/50 text-text-secondary text-xs hover:bg-background-overlay transition-colors"
            @click="close"
          >
            我知道了
          </button>
        </template>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Smartphone, X, Check, Copy, MoreHorizontal, ExternalLink } from 'lucide-vue-next'
import {
  wizardVisible,
  closeInstallWizard,
  isIOS,
  isIOSNonSafari,
  isInAppBrowser,
  canPromptNative,
  triggerNativeInstall,
  copyCurrentUrl,
} from '@/installBus'

const installing = ref(false)
const copyState = ref<'idle' | 'done'>('idle')

// 展现分支选择（优先级从高到低：越受限的环境越要优先兜住）
const mode = computed<'inapp' | 'ios-other' | 'native' | 'ios' | 'fallback'>(() => {
  if (isInAppBrowser) return 'inapp'
  if (isIOSNonSafari) return 'ios-other'
  if (canPromptNative.value) return 'native'
  if (isIOS) return 'ios'
  return 'fallback'
})

// 头部图标/标题/描述随分支调整，避免无法安装时文案还写"一键装到桌面"
const headerIcon = computed(() => {
  if (mode.value === 'inapp' || mode.value === 'ios-other') return ExternalLink
  return Smartphone
})

const headerTitle = computed(() => {
  switch (mode.value) {
    case 'inapp':
      return '请用系统浏览器打开'
    case 'ios-other':
      return '请用 Safari 打开'
    default:
      return '安装到主屏幕'
  }
})

const headerDesc = computed(() => {
  switch (mode.value) {
    case 'inapp':
      return '当前是 App 内置浏览器，<br/>没法把网页加到主屏'
    case 'ios-other':
      return '苹果系统只允许 Safari 加到主屏'
    default:
      return '一键装到桌面，打开像 App 一样全屏无浏览器栏<br />离线也能看已缓存的吉他谱'
  }
})

const close = () => {
  if (installing.value) return
  closeInstallWizard()
}

// 每次打开弹窗重置复制态，避免上一次的"已复制"残留
watch(wizardVisible, (v) => {
  if (v) copyState.value = 'idle'
})

const onCopyLink = async () => {
  const ok = await copyCurrentUrl()
  if (ok) {
    copyState.value = 'done'
  }
}

const onInstallNative = async () => {
  installing.value = true
  try {
    const outcome = await triggerNativeInstall()
    if (outcome !== 'accepted') {
      closeInstallWizard()
    }
  } finally {
    installing.value = false
  }
}
</script>

<style scoped>
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.2s ease;
}
.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

.animate-fade-in-up {
  animation: fadeInUp 0.22s ease-out;
}
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
