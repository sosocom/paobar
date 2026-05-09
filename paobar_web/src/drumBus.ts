/**
 * 鼓机全局总线。
 *
 * 设计动机：
 *   之前鼓机状态挂在 `DrumMachine.vue` 组件里，`NowPlaying` 切歌或 `v-if`
 *   控制面板显隐时，组件会被销毁重建，Tone 引擎/Transport 随之 dispose，
 *   播放必然中断。用户希望"一旦打开播放，切歌不中断"，所以把引擎抽到全局。
 *
 * 模块加载时机：
 *   本文件静态 import Tone.js（~200KB raw / ~70KB gz），但只会被 `DrumMachine.vue`
 *   引用。而 `DrumMachine.vue` 是在 `NowPlaying.vue` 里通过 `defineAsyncComponent`
 *   懒加载的，所以用户"没展开鼓机面板"时 Tone 不会进主包。
 *
 * 状态分层：
 *   - 全局响应式：是否打开面板 / 是否在播 / 当前拍 / 当前风格 / BPM / Count-in
 *   - 全局非响应式：Tone 合成器、Sequence（通过闭包持有）
 *   - 每首歌上下文：meter / bpmRaw / songId，由 NowPlaying 通过 setSongContext 推入
 *
 * 为什么能"切歌不停"：
 *   Tone.Transport 独立于 Vue 组件树，只要不显式 stop，就持续跑；
 *   切歌时我们仅根据新 meter / songId 决定要不要**热替换**内部 Sequence。
 */
import { computed, ref, watch } from 'vue'
import * as Tone from 'tone'
import {
  BPM_FALLBACK,
  BPM_MAX,
  BPM_MIN,
  DRUM_PATTERNS,
  normalizeMeter,
  parseBpm,
  patternsForMeter,
  type DrumPattern,
} from '@/utils/drumPatterns'
import { DRUM_STOP_EVENT, panelOpen, togglePanelOpen } from '@/drumPanelState'

// ─── localStorage keys ───
const LS_STYLE_PREFIX = 'paobar.drum.style.' // + meter
const LS_BPM_PREFIX = 'paobar.drum.bpm.'     // + songId
const LS_COUNTIN = 'paobar.drum.countIn'

// panelOpen / togglePanelOpen 来自 drumPanelState（那个模块不依赖 Tone），
// 这里把它们 re-export 出去，让 DrumMachine.vue 继续从本模块一把拿走所有需要的东西。
export { panelOpen, togglePanelOpen }

// ─── 公共响应式状态（组件可直接绑定） ───
export const isPlaying = ref(false)
export const currentBeat = ref(-1)
export const currentStyleId = ref<string>('')
export const bpm = ref<number>(BPM_FALLBACK)
export const countIn = ref<boolean>(
  typeof localStorage !== 'undefined' && localStorage.getItem(LS_COUNTIN) === '1'
)

// 当前曲目上下文（由 NowPlaying 通过 setSongContext 推入）
interface SongCtx { meter: string; bpmRaw: string | null; songId: string | null }
const songContext = ref<SongCtx>({ meter: '4/4', bpmRaw: null, songId: null })

export const meterNorm = computed(() => songContext.value.meter)
export const availablePatterns = computed<DrumPattern[]>(() => patternsForMeter(meterNorm.value))
export const beatCount = computed<number>(() => {
  const num = parseInt(meterNorm.value.split('/')[0] || '4', 10)
  return num > 0 && num < 12 ? num : 4
})
export const currentPattern = computed<DrumPattern>(() => {
  return DRUM_PATTERNS.find(p => p.id === currentStyleId.value) ?? availablePatterns.value[0]
})

// ─── 非响应式引擎对象（闭包持有，避免被 Vue 代理包一层影响性能） ───
let kick: Tone.MembraneSynth | null = null
let snare: Tone.NoiseSynth | null = null
let hat: Tone.MetalSynth | null = null
let seq: Tone.Sequence<number> | null = null
let synthsReady = false

// 记录最后一次 buildSequence 使用的 pattern id，用于判断是否真的需要热替换
let lastBuiltPatternId: string | null = null

// 切歌时通过 Transport.scheduleOnce 安排的预备拍 id 列表；快速切歌时
// 用 Transport.clear() 清掉这批未触发的事件，避免 hi-hat 叠加。
const pendingCountInIds: number[] = []

const ensureSynths = () => {
  if (synthsReady) return
  kick = new Tone.MembraneSynth({
    pitchDecay: 0.05,
    octaves: 6,
    oscillator: { type: 'sine' },
    envelope: { attack: 0.001, decay: 0.35, sustain: 0.01, release: 1.2, attackCurve: 'exponential' },
  }).toDestination()
  kick.volume.value = -2

  snare = new Tone.NoiseSynth({
    noise: { type: 'white' },
    envelope: { attack: 0.001, decay: 0.18, sustain: 0, release: 0.2 },
  }).toDestination()
  snare.volume.value = -6

  hat = new Tone.MetalSynth({
    envelope: { attack: 0.001, decay: 0.1, release: 0.01 },
    harmonicity: 5.1,
    modulationIndex: 32,
    resonance: 4000,
    octaves: 1.5,
  } as any).toDestination()
  hat.volume.value = -14

  synthsReady = true
}

// ─── 偏好读取辅助 ───
const pickDefaultStyle = (meter: string): string => {
  const remembered = typeof localStorage !== 'undefined'
    ? localStorage.getItem(LS_STYLE_PREFIX + meter) : null
  const list = patternsForMeter(meter)
  if (remembered && list.some(p => p.id === remembered)) return remembered
  return list[0]?.id ?? DRUM_PATTERNS[0].id
}

const pickDefaultBpm = (songId: string | null, bpmRaw: string | null | undefined): number => {
  if (songId && typeof localStorage !== 'undefined') {
    const saved = localStorage.getItem(LS_BPM_PREFIX + songId)
    if (saved) {
      const v = parseInt(saved, 10)
      if (v >= BPM_MIN && v <= BPM_MAX) return v
    }
  }
  return parseBpm(bpmRaw) ?? BPM_FALLBACK
}

// ─── Sequence 构建 / 热替换 ───
const buildSequence = () => {
  if (seq) {
    try { seq.stop(); seq.dispose() } catch { /* noop */ }
    seq = null
  }
  const pat = currentPattern.value
  if (!pat) return
  const stepsArr = Array.from({ length: pat.steps }, (_, i) => i)

  seq = new Tone.Sequence<number>((time, step) => {
    // 每个 step 内都从 computed 取最新 pattern：切风格/切歌时无缝热替换
    const p = currentPattern.value
    const k = p.tracks.kick?.[step]
    const s = p.tracks.snare?.[step]
    const h = p.tracks.hat?.[step]
    if (k && k !== '.') kick?.triggerAttackRelease('C1', '8n', time, k === 'X' ? 1 : 0.85)
    if (s && s !== '.') snare?.triggerAttackRelease('16n', time, 0.9)
    if (h && h !== '.') hat?.triggerAttackRelease('C6', '32n', time, 0.5)

    const beatIndex = Math.floor(step / 4)
    if (beatIndex !== currentBeat.value) {
      Tone.Draw.schedule(() => { currentBeat.value = beatIndex }, time)
    }
  }, stepsArr, '16n')

  lastBuiltPatternId = pat.id
}

/**
 * 只有在"新 pattern 的格子数与当前 seq 不同"时才真的 dispose 重建；
 * 如果只是同 meter 内切换风格且格子数相同，沿用当前 seq（回调内已经取最新 pattern）。
 * 这样才能做到"切换风格/切歌时鼓机不断档"。
 */
const switchPatternHot = () => {
  const pat = currentPattern.value
  if (!pat) return
  const sameStepsAsCurrent = seq && pat.steps === (seq as any)._events?.length
  if (lastBuiltPatternId === pat.id) return
  if (sameStepsAsCurrent) {
    // 格子数一样，Sequence 回调已经用 computed 取最新 pattern，不需要重建。
    // 仅更新 id 记录。
    lastBuiltPatternId = pat.id
    return
  }
  // 不同 meter → 格子数不同，必须 dispose 重建
  if (seq) { try { seq.stop(); seq.dispose() } catch { /* noop */ } }
  seq = null
  buildSequence()
  // 从"当前 Transport 位置下一小节"切入，避免中间半拍产生错位
  seq?.start('+0.001')
}

const clearPendingCountIn = () => {
  while (pendingCountInIds.length) {
    const id = pendingCountInIds.pop()!
    try { Tone.Transport.clear(id) } catch { /* noop */ }
  }
}

/**
 * 切歌时若用户开启了"4 拍预备"，先静音停掉当前 seq，下一小节边界开始 4 下
 * hi-hat 计数，再起新 seq。Transport 不停，所以走带宽和 AudioContext 状态都
 * 不变，避免 iOS 上重启 Transport 引发的卡顿。
 *
 * 时间域注意：`Transport.nextSubdivision()` 返回的是 AudioContext 绝对时间，
 * 但 `Transport.scheduleOnce` / `Sequence.start` 接收的是 Transport 时间
 * （Transport.start 后从 0 开始计）。两者数值会差几十秒，直接当 Transport 时
 * 间用会让事件落在 Transport 时间轴的远未来，永远不响。所以这里用
 * BarsBeatsSixteenths（"bar:beat:16th"）字符串，这是 Tone 解析为 Transport
 * 时间的标准格式，跨 BPM 都成立。
 */
const restartWithCountIn = () => {
  clearPendingCountIn()
  if (seq) { try { seq.stop(); seq.dispose() } catch { /* noop */ } }
  seq = null
  lastBuiltPatternId = null
  currentBeat.value = -1

  // Transport.position 形如 "1:2.5:0"，取小节号 +1 作为下一小节边界
  const posStr = String(Tone.Transport.position)
  const currentBar = parseInt(posStr.split(':')[0] || '0', 10)
  const nextBar = currentBar + 1

  for (let i = 0; i < 4; i++) {
    const slot = i
    const id = Tone.Transport.scheduleOnce((time) => {
      hat?.triggerAttackRelease('C6', '32n', time, 0.8)
      Tone.Draw.schedule(() => { currentBeat.value = slot % beatCount.value }, time)
    }, `${nextBar}:${i}:0`)
    pendingCountInIds.push(id)
  }

  buildSequence()
  // 新 seq 在 count-in 后正好接上：next-bar + 1 个小节
  seq?.start(`${nextBar + 1}:0:0`)
}

// ─── 对外 API ───

/**
 * NowPlaying 每次加载/切换当前曲目时调用。
 * 会按新 meter 更新风格默认值、按新 songId 更新 BPM（沿用记忆或 fallback）。
 * 若当前正在播放，会做热替换保持连续。
 */
export const setSongContext = (ctx: { meter?: string | null; bpmRaw?: string | null; songId?: string | null }) => {
  const nextMeter = normalizeMeter(ctx.meter)
  const nextBpmRaw = ctx.bpmRaw ?? null
  const nextSongId = ctx.songId ?? null

  const prevSongId = songContext.value.songId
  songContext.value = { meter: nextMeter, bpmRaw: nextBpmRaw, songId: nextSongId }

  // 仅在首次（无任何风格）时挑默认；UI 已收敛为单一固定节奏（rock-basic），
  // 切歌不再按 meter 重新挑。否则会与 DrumMachine 的 FIXED_STYLE_ID 锁定逻辑
  // 抢占触发 currentStyleId 的 watcher，把刚排好的 count-in + 新 seq 冲掉。
  if (!currentStyleId.value) {
    currentStyleId.value = pickDefaultStyle(nextMeter)
  }

  // songId 变化 → 更新 BPM；若有记忆值，尊重用户之前为这首歌设定的速度
  if (prevSongId !== nextSongId) {
    bpm.value = pickDefaultBpm(nextSongId, nextBpmRaw)
    if (synthsReady) Tone.Transport.bpm.value = bpm.value
  }

  // 正在播的话，按需热替换 Sequence
  if (isPlaying.value && synthsReady) {
    const isSongSwitch = prevSongId !== null && nextSongId !== null && prevSongId !== nextSongId
    if (isSongSwitch && countIn.value) {
      // 用户希望"切到下一首"也响 4 下预备拍再继续打鼓
      restartWithCountIn()
    } else {
      switchPatternHot()
    }
  }
}

export const start = async () => {
  // 必须同步运行在用户手势里（iOS 限制）
  await Tone.start()
  ensureSynths()

  Tone.Transport.bpm.value = bpm.value
  Tone.Transport.stop()
  Tone.Transport.cancel(0)
  currentBeat.value = -1
  buildSequence()

  if (countIn.value) {
    // 预备 4 拍 hi-hat 节拍器；序列在 1 小节后起
    for (let i = 0; i < 4; i++) {
      Tone.Transport.schedule((time) => {
        hat?.triggerAttackRelease('C6', '32n', time, 0.8)
        Tone.Draw.schedule(() => { currentBeat.value = i % beatCount.value }, time)
      }, `0:${i}`)
    }
    seq?.start('1m')
  } else {
    seq?.start(0)
  }

  Tone.Transport.start('+0.05')
  isPlaying.value = true
}

export const stop = () => {
  if (synthsReady) {
    Tone.Transport.stop()
    Tone.Transport.cancel(0)
  }
  clearPendingCountIn()
  if (seq) { try { seq.stop(); seq.dispose() } catch { /* noop */ } }
  seq = null
  lastBuiltPatternId = null
  isPlaying.value = false
  currentBeat.value = -1
}

export const toggle = async () => {
  if (isPlaying.value) stop()
  else await start()
}

export const bumpBpm = (delta: number) => {
  const next = Math.min(BPM_MAX, Math.max(BPM_MIN, bpm.value + delta))
  if (next !== bpm.value) bpm.value = next
}

// ─── 持久化 watchers ───
watch(currentStyleId, (v) => {
  if (!v || typeof localStorage === 'undefined') return
  localStorage.setItem(LS_STYLE_PREFIX + meterNorm.value, v)
  // 切风格实时生效（热替换）
  if (isPlaying.value && synthsReady) switchPatternHot()
})

let bpmSaveTimer: number | null = null
watch(bpm, (v) => {
  if (synthsReady) Tone.Transport.bpm.value = v
  const sid = songContext.value.songId
  if (!sid || typeof localStorage === 'undefined') return
  if (bpmSaveTimer) window.clearTimeout(bpmSaveTimer)
  bpmSaveTimer = window.setTimeout(() => {
    localStorage.setItem(LS_BPM_PREFIX + sid, String(v))
  }, 300)
})

watch(countIn, (v) => {
  if (typeof localStorage === 'undefined') return
  localStorage.setItem(LS_COUNTIN, v ? '1' : '0')
})

// 页签隐藏 → 暂停，免得后台继续响（iOS 其实会自动暂停 AudioContext，桌面端靠这个）
if (typeof document !== 'undefined') {
  document.addEventListener('visibilitychange', () => {
    if (document.hidden && isPlaying.value) stop()
  })
}

// NowPlaying 离开路由时会 dispatch 这个事件，让鼓机停下来。
// 用 window event 是为了让 NowPlaying 完全不需要静态引用本文件（避免 Tone 被拉入主包）。
if (typeof window !== 'undefined') {
  window.addEventListener(DRUM_STOP_EVENT, () => {
    if (isPlaying.value) stop()
  })
}
