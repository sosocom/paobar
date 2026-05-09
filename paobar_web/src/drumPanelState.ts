/**
 * 鼓机"面板是否展开"的全局状态。
 *
 * 为什么单独成文件：
 *   `drumBus.ts` 静态 import 了 Tone.js（~70KB gz），而 Tone 的加载应仅发生在
 *   用户展开鼓机面板时。若 `NowPlaying.vue` 直接从 `drumBus` 取 panelOpen，
 *   Tone 就会被拽进主包。因此把纯布尔 + localStorage 的逻辑拎到这里，
 *   保证 NowPlaying 只付"一个 ref + 两行 LS"的成本。
 *
 * 约定：此模块必须保持零重依赖（除 vue）。
 */
import { ref } from 'vue'

const LS_OPEN = 'paobar.drum.open'

export const panelOpen = ref<boolean>(
  typeof localStorage !== 'undefined' && localStorage.getItem(LS_OPEN) === '1'
)

export const togglePanelOpen = () => {
  panelOpen.value = !panelOpen.value
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem(LS_OPEN, panelOpen.value ? '1' : '0')
  }
}

/** 专供外部页面在路由离开 NowPlaying 时调用，用于停鼓；drumBus 会监听该事件。
 *  走 window event 是为了避免 NowPlaying 静态 import drumBus 进而拖入 Tone。 */
export const DRUM_STOP_EVENT = 'paobar:drum-stop'
export const requestStopDrum = () => {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event(DRUM_STOP_EVENT))
  }
}
