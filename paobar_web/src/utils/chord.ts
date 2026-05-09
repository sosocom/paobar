/**
 * 吉他和弦工具：
 *  - 纳什维尔数字系统：调号 + 级数 → 具体和弦名（G 调的 6m = Em）
 *  - 常用和弦指法库（标准调弦 EADGBE，6→1 弦）
 *
 * 约定：
 *  - NOTE_NAMES 使用升号记法（C#/D#/F#/G#/A#）。降号输入会被归一化。
 *  - 级数支持前缀 #/b 和任意后缀：b3、#4、5sus4、3m7 都能解析。
 *  - 传入的字符串如果已经是具体和弦（"Em"），原样返回，便于同一份模板
 *    同时渲染数字谱和字母谱。
 */

export const NOTE_NAMES = [
  'C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B',
] as const

const FLAT_TO_SHARP: Record<string, string> = {
  Db: 'C#', Eb: 'D#', Gb: 'F#', Ab: 'G#', Bb: 'A#',
}

/** 把 key 名称（如 "G", "Bb"）映射成 0..11 的半音序号。解析失败回落到 C (0)。 */
export function keyRootIndex(key: string): number {
  if (!key) return 0
  const k = key.trim()
  const sharp = FLAT_TO_SHARP[k] ?? k
  const idx = (NOTE_NAMES as readonly string[]).indexOf(sharp)
  return idx >= 0 ? idx : 0
}

const DEGREE_STEPS: Record<string, number> = {
  '1': 0, '2': 2, '3': 4, '4': 5, '5': 7, '6': 9, '7': 11,
}

/** 级数正则：可选 #/b + 1-7 + 任意后缀（m / 7 / m7 / sus4 / add9 / maj7 等）。 */
const DEGREE_RE = /^([#b]?)([1-7])(.*)$/

/**
 * 把级数（"6m"、"b3"、"5sus4"）按调号转换为具体和弦名（"Em"）。
 * 如果输入本身就是字母谱和弦（"G"、"Am7"），原样返回。
 */
export function degreeToChord(degree: string, key: string): string {
  if (!degree) return degree
  const trimmed = degree.trim()
  const m = DEGREE_RE.exec(trimmed)
  if (!m) return trimmed
  const [, acc, num, suffix] = m
  const keyIdx = keyRootIndex(key)
  let step = DEGREE_STEPS[num] ?? 0
  if (acc === '#') step += 1
  if (acc === 'b') step -= 1
  const chordIdx = (keyIdx + step + 12) % 12
  return NOTE_NAMES[chordIdx] + (suffix || '')
}

/**
 * 和弦按弦位定义。
 *  - frets: 从 6 弦（低音 E）到 1 弦（高音 E），-1 = 不弹(X)，0 = 空弦(O)，1+ = 品位
 *  - baseFret: 图上第一根品线对应的实际品位，默认 1（open position）
 *  - barre: 横按，from/to 为弦索引（0=6弦，5=1弦），fret 为相对 baseFret 的品位
 */
export interface ChordVoicing {
  frets: [number, number, number, number, number, number]
  baseFret?: number
  barre?: { fret: number; from: number; to: number }
}

/**
 * 常用和弦指法库。覆盖 12 个大三、12 个小三、常用 7/m7/maj7/sus4/sus2/add9，
 * 以及中文流行里高频出现的转位和弦（slash chord）。
 */
export const CHORD_DB: Record<string, ChordVoicing> = {
  // ---- Major ----
  C: { frets: [-1, 3, 2, 0, 1, 0] },
  'C#': { frets: [-1, 4, 3, 1, 2, 1], barre: { fret: 1, from: 2, to: 5 } },
  D: { frets: [-1, -1, 0, 2, 3, 2] },
  'D#': { frets: [-1, -1, 1, 3, 4, 3], barre: { fret: 1, from: 2, to: 5 } },
  E: { frets: [0, 2, 2, 1, 0, 0] },
  F: { frets: [1, 3, 3, 2, 1, 1], barre: { fret: 1, from: 0, to: 5 } },
  'F#': { frets: [2, 4, 4, 3, 2, 2], baseFret: 1, barre: { fret: 2, from: 0, to: 5 } },
  G: { frets: [3, 2, 0, 0, 0, 3] },
  'G#': { frets: [4, 6, 6, 5, 4, 4], baseFret: 4, barre: { fret: 1, from: 0, to: 5 } },
  A: { frets: [-1, 0, 2, 2, 2, 0] },
  'A#': { frets: [-1, 1, 3, 3, 3, 1], barre: { fret: 1, from: 1, to: 5 } },
  B: { frets: [-1, 2, 4, 4, 4, 2], baseFret: 1, barre: { fret: 2, from: 1, to: 5 } },

  // ---- Minor ----
  Cm: { frets: [-1, 3, 5, 5, 4, 3], baseFret: 3, barre: { fret: 1, from: 1, to: 5 } },
  'C#m': { frets: [-1, 4, 6, 6, 5, 4], baseFret: 4, barre: { fret: 1, from: 1, to: 5 } },
  Dm: { frets: [-1, -1, 0, 2, 3, 1] },
  'D#m': { frets: [-1, -1, 1, 3, 4, 2], barre: { fret: 1, from: 2, to: 5 } },
  Em: { frets: [0, 2, 2, 0, 0, 0] },
  Fm: { frets: [1, 3, 3, 1, 1, 1], barre: { fret: 1, from: 0, to: 5 } },
  'F#m': { frets: [2, 4, 4, 2, 2, 2], baseFret: 2, barre: { fret: 1, from: 0, to: 5 } },
  Gm: { frets: [3, 5, 5, 3, 3, 3], baseFret: 3, barre: { fret: 1, from: 0, to: 5 } },
  'G#m': { frets: [4, 6, 6, 4, 4, 4], baseFret: 4, barre: { fret: 1, from: 0, to: 5 } },
  Am: { frets: [-1, 0, 2, 2, 1, 0] },
  'A#m': { frets: [-1, 1, 3, 3, 2, 1], barre: { fret: 1, from: 1, to: 5 } },
  Bm: { frets: [-1, 2, 4, 4, 3, 2], baseFret: 2, barre: { fret: 1, from: 1, to: 5 } },

  // ---- Dominant 7 ----
  C7: { frets: [-1, 3, 2, 3, 1, 0] },
  D7: { frets: [-1, -1, 0, 2, 1, 2] },
  E7: { frets: [0, 2, 0, 1, 0, 0] },
  F7: { frets: [1, 3, 1, 2, 1, 1], barre: { fret: 1, from: 0, to: 5 } },
  G7: { frets: [3, 2, 0, 0, 0, 1] },
  A7: { frets: [-1, 0, 2, 0, 2, 0] },
  B7: { frets: [-1, 2, 1, 2, 0, 2] },

  // ---- Minor 7 ----
  Am7: { frets: [-1, 0, 2, 0, 1, 0] },
  Bm7: { frets: [-1, 2, 4, 2, 3, 2], baseFret: 2, barre: { fret: 1, from: 1, to: 5 } },
  Cm7: { frets: [-1, 3, 5, 3, 4, 3], baseFret: 3, barre: { fret: 1, from: 1, to: 5 } },
  Dm7: { frets: [-1, -1, 0, 2, 1, 1] },
  Em7: { frets: [0, 2, 0, 0, 0, 0] },
  Fm7: { frets: [1, 3, 1, 1, 1, 1], barre: { fret: 1, from: 0, to: 5 } },
  Gm7: { frets: [3, 5, 3, 3, 3, 3], baseFret: 3, barre: { fret: 1, from: 0, to: 5 } },

  // ---- Major 7 ----
  Cmaj7: { frets: [-1, 3, 2, 0, 0, 0] },
  Dmaj7: { frets: [-1, -1, 0, 2, 2, 2] },
  Emaj7: { frets: [0, 2, 1, 1, 0, 0] },
  Fmaj7: { frets: [-1, -1, 3, 2, 1, 0] },
  Gmaj7: { frets: [3, 2, 0, 0, 0, 2] },
  Amaj7: { frets: [-1, 0, 2, 1, 2, 0] },

  // ---- sus4 / sus2 ----
  Asus4: { frets: [-1, 0, 2, 2, 3, 0] },
  Dsus4: { frets: [-1, -1, 0, 2, 3, 3] },
  Esus4: { frets: [0, 2, 2, 2, 0, 0] },
  Gsus4: { frets: [3, 3, 0, 0, 1, 3] },
  Csus4: { frets: [-1, 3, 3, 0, 1, 1] },
  Asus2: { frets: [-1, 0, 2, 2, 0, 0] },
  Dsus2: { frets: [-1, -1, 0, 2, 3, 0] },
  Esus2: { frets: [0, 2, 4, 4, 0, 0], baseFret: 2, barre: { fret: 1, from: 1, to: 2 } },

  // ---- add9 ----
  Cadd9: { frets: [-1, 3, 2, 0, 3, 0] },
  Gadd9: { frets: [3, 0, 0, 0, 0, 3] },

  // ---- 常见转位（slash chord） ----
  'C/E': { frets: [0, 3, 2, 0, 1, 0] },
  'C/G': { frets: [3, 3, 2, 0, 1, 0] },
  'D/F#': { frets: [2, -1, 0, 2, 3, 2] },
  'D/A': { frets: [-1, 0, 0, 2, 3, 2] },
  'G/B': { frets: [-1, 2, 0, 0, 0, 3] },
  'G/D': { frets: [-1, -1, 0, 0, 0, 3] },
  'Em/G': { frets: [3, 2, 2, 0, 0, 0] },
  'Am/G': { frets: [3, 0, 2, 2, 1, 0] },
  'F/C': { frets: [-1, 3, 3, 2, 1, 1], barre: { fret: 1, from: 1, to: 5 } },
}

/**
 * 把降号和弦归一成升号形式（Bb → A#），同时保留可能的 /bass 转位部分。
 */
function normalizeChordName(chord: string): string {
  if (!chord) return chord
  const trimmed = chord.trim()
  // 处理 slash 转位：主和弦和 bass 都要归一
  const slashIdx = trimmed.indexOf('/')
  if (slashIdx > 0) {
    const main = normalizeSingle(trimmed.slice(0, slashIdx))
    const bass = normalizeSingle(trimmed.slice(slashIdx + 1))
    return `${main}/${bass}`
  }
  return normalizeSingle(trimmed)
}

function normalizeSingle(chord: string): string {
  const m = /^([A-G])([b#]?)(.*)$/.exec(chord)
  if (!m) return chord
  const [, root, acc, rest] = m
  const key = acc ? root + acc : root
  const sharp = FLAT_TO_SHARP[key] ?? key
  return sharp + (rest || '')
}

/**
 * 查询和弦指法。查询顺序：
 *   1. 全名精确匹配（包含转位）
 *   2. 去掉 /bass 再匹配主和弦
 *   3. 返回 null（由调用方显示 fallback 文本）
 */
export function getVoicing(chord: string): ChordVoicing | null {
  if (!chord) return null
  const normalized = normalizeChordName(chord)
  if (CHORD_DB[normalized]) return CHORD_DB[normalized]
  const slashIdx = normalized.indexOf('/')
  if (slashIdx > 0) {
    const main = normalized.slice(0, slashIdx)
    if (CHORD_DB[main]) return CHORD_DB[main]
  }
  return null
}
