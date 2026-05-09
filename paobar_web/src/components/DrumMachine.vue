<template>
  <!-- 薄壳组件：UI 只负责渲染和触发，所有状态 / 引擎都在 @/drumBus。
       组件 mount / unmount 不影响 Tone 引擎，因此切歌 NowPlaying 内重建
       DrumMachine 时，播放不会断档。
       UI 收敛：每首歌固定 "流行摇滚" 节奏，不再让用户挑；操作并到一行：
       [播放] · [beat 圆点] · [4 拍预备] · [BPM 步进]。 -->
  <div class="drum-machine px-4 py-3 bg-background-card/70 backdrop-blur-xl border-t border-white/5">
    <div class="flex items-center gap-3">
      <button
        type="button"
        @click="onToggle"
        class="w-10 h-10 rounded-full bg-primary text-white flex items-center justify-center shadow-sm hover:brightness-110 flex-shrink-0"
        :aria-label="isPlaying ? '停止' : '播放'"
      >
        <Pause v-if="isPlaying" :size="18" />
        <Play v-else :size="18" class="ml-0.5" />
      </button>

      <div class="flex items-center gap-1.5 flex-1 min-w-0">
        <span
          v-for="b in beatCount"
          :key="b"
          class="rounded-full transition-all duration-75 flex-shrink-0"
          :class="[
            currentBeat === b - 1 && isPlaying
              ? (b === 1 ? 'bg-primary' : 'bg-white')
              : 'bg-white/15',
            b === 1 ? 'w-3 h-3' : 'w-2 h-2',
          ]"
        />
      </div>

      <label class="flex items-center gap-1.5 text-xs text-text-secondary cursor-pointer select-none flex-shrink-0">
        <input type="checkbox" v-model="countIn" class="w-3.5 h-3.5 accent-primary" />
        <span>4 拍预备</span>
      </label>

      <div class="flex items-center gap-1 h-9 px-2 rounded-lg bg-background-overlay/50 border border-white/5 flex-shrink-0">
        <button
          type="button"
          @click="bumpBpm(-2)"
          class="w-6 h-6 flex items-center justify-center text-text-secondary hover:text-text-primary"
          aria-label="BPM -"
        >
          <Minus :size="14" />
        </button>
        <span class="text-sm font-mono tabular-nums w-8 text-center text-text-primary">{{ bpm }}</span>
        <button
          type="button"
          @click="bumpBpm(2)"
          class="w-6 h-6 flex items-center justify-center text-text-secondary hover:text-text-primary"
          aria-label="BPM +"
        >
          <Plus :size="14" />
        </button>
      </div>
    </div>

    <p v-if="showIosHint" class="mt-2 text-[11px] text-text-secondary/70 leading-relaxed">
      若无声音，请检查手机静音键是否打开（iOS 限制：静音键关闭时浏览器也听不到鼓机声）。
    </p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { Minus, Pause, Play, Plus } from 'lucide-vue-next'
import {
  beatCount,
  bpm,
  bumpBpm,
  countIn,
  currentBeat,
  currentStyleId,
  isPlaying,
  setSongContext,
  toggle,
} from '@/drumBus'

// UI 收敛后只用这一种节奏；不论 meter 是 4/4 / 3/4 / 6/8，统一播这个 16 格 4/4 模板
const FIXED_STYLE_ID = 'rock-basic'

const props = defineProps<{
  meter?: string | null
  bpmRaw?: string | null
  songId?: string | null
}>()

// 挂载 / props 变化都把最新上下文推给 bus；bus 会按需热替换 pattern；
// 紧接着把 style 强制锁回固定节奏，避免 bus 内部按 meter 自动切换。
const syncContext = () => {
  setSongContext({
    meter: props.meter ?? null,
    bpmRaw: props.bpmRaw ?? null,
    songId: props.songId ?? null,
  })
  if (currentStyleId.value !== FIXED_STYLE_ID) {
    currentStyleId.value = FIXED_STYLE_ID
  }
}

onMounted(syncContext)
watch(() => [props.meter, props.bpmRaw, props.songId], syncContext)

const onToggle = async () => {
  try {
    await toggle()
  } catch (e) {
    console.error('drum toggle failed:', e)
  }
}

// 一次性展示 iOS 静音键提示
const showIosHint = ref(false)
onMounted(() => {
  const ua = navigator.userAgent || ''
  if (/iPhone|iPad|iPod/i.test(ua)) showIosHint.value = true
})
</script>

<style scoped>
.drum-machine { /* 动画交给父组件 <Transition name="drum-slide"> */ }
</style>
