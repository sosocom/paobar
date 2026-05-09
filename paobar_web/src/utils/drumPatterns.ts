/**
 * 鼓机预设节奏库。
 *
 * 设计约定：
 *   - 所有 pattern 的 tracks 都是等长字符串，长度 = steps；
 *   - 用 '16n' 作为统一 subdivision（一拍=4 格），这样 4/4=16 格、3/4=12 格、6/8=12 格；
 *   - 每格字符 'x' 表示该位置击打、'.' 表示空；
 *   - 'X'（大写）用于"重音"，前端 UI 与音量层都可以识别（当前 Phase 1 只在视觉上高亮）。
 *
 * 为什么不直接用 Tone.Pattern 的 array 形式？
 *   - 字符串可读性好，review 节奏时一眼看得出；
 *   - Tone.Sequence 的回调里用 index 访问字符串 O(1)，性能没有差别。
 */

export type DrumTrackKey = 'kick' | 'snare' | 'hat'

export interface DrumPattern {
  id: string
  meter: string                // '4/4' | '3/4' | '6/8' ...
  labelZh: string              // UI 下拉里的中文名
  steps: number                // 小节内的 16 分位格数
  tracks: Partial<Record<DrumTrackKey, string>>
}

/**
 * Phase 1 自带的全部节奏型。
 * 每种拍号至少 2 个预设，覆盖"活泼/慢速"两档。
 * 规则（beat 与 step 的关系，1 拍 = 4 个 16 分格）：
 *   - 4/4：beat1=0, beat2=4, beat3=8, beat4=12
 *   - 3/4：beat1=0, beat2=4, beat3=8
 *   - 6/8：按 16 分格算 12 格；compound 重音在 1 和 4
 */
export const DRUM_PATTERNS: DrumPattern[] = [
  // ── 4/4 ──
  {
    id: 'rock-basic',
    meter: '4/4',
    labelZh: '流行摇滚',
    steps: 16,
    tracks: {
      kick:  'X.......x.......',
      snare: '....x.......x...',
      hat:   'x.x.x.x.x.x.x.x.',
    },
  },
  {
    id: 'folk-ballad',
    meter: '4/4',
    labelZh: '民谣慢拍',
    steps: 16,
    tracks: {
      kick:  'X.......x.......',
      snare: '........x.......',
      hat:   'x...x...x...x...',
    },
  },

  // ── 3/4 ──
  {
    id: 'waltz',
    meter: '3/4',
    labelZh: '华尔兹',
    steps: 12,
    tracks: {
      kick:  'X...........',
      hat:   'x...x...x...',
    },
  },
  {
    id: 'slow-three',
    meter: '3/4',
    labelZh: '慢三',
    steps: 12,
    tracks: {
      kick:  'X...........',
      snare: '....x.......',
      hat:   'x...x...x...',
    },
  },

  // ── 6/8 ──
  {
    id: 'compound-68',
    meter: '6/8',
    labelZh: '6/8 律动',
    steps: 12,
    tracks: {
      kick:  'X.....x.....',
      hat:   'x.x.x.x.x.x.',
    },
  },
  {
    id: 'ballad-68',
    meter: '6/8',
    labelZh: '6/8 民谣',
    steps: 12,
    tracks: {
      kick:  'X...........',
      snare: '......x.....',
      hat:   'x.x.x.x.x.x.',
    },
  },
]

/** 把 "4/4" / " 4 / 4 " / "C" 等解析成标准拍号字符串；未知返回 '4/4'。 */
export const normalizeMeter = (raw: string | undefined | null): string => {
  if (!raw) return '4/4'
  const s = String(raw).trim()
  // 常见的通用符号兜底
  if (s === 'C' || s === 'c') return '4/4'
  const m = s.match(/(\d+)\s*\/\s*(\d+)/)
  if (!m) return '4/4'
  return `${m[1]}/${m[2]}`
}

/** 从 "♩=92" / "92" / "60-80" / "BPM 108" 等任意字符串里提取第一个合法 BPM；无则返回 null。 */
export const parseBpm = (raw: string | undefined | null): number | null => {
  if (!raw) return null
  const m = String(raw).match(/(\d{2,3})/)
  if (!m) return null
  const v = parseInt(m[1], 10)
  if (!isFinite(v)) return null
  if (v < 30 || v > 260) return null
  return v
}

/** 选择当前 meter 下可用的预设；若没有就降级到 4/4，再没有就返回空数组。 */
export const patternsForMeter = (meterRaw: string | undefined | null): DrumPattern[] => {
  const meter = normalizeMeter(meterRaw)
  const exact = DRUM_PATTERNS.filter(p => p.meter === meter)
  if (exact.length > 0) return exact
  return DRUM_PATTERNS.filter(p => p.meter === '4/4')
}

export const BPM_MIN = 40
export const BPM_MAX = 220
export const BPM_FALLBACK = 80
