import { pinyin } from 'pinyin-pro'
import type { Song } from '@/types'

/**
 * 与后端 Hutool 拼音排序尽量一致：歌名全拼小写（无空格），用于收藏列表本地排序。
 */
export function songTitleSortKey(title: string): string {
  const t = (title ?? '').trim()
  if (!t) return ''
  try {
    return pinyin(t, { toneType: 'none', type: 'string' })
      .replace(/\s+/g, '')
      .toLowerCase()
  } catch {
    return t.toLowerCase()
  }
}

/** 通讯录式首字母：取歌名全拼首字符，a-z→A-Z，否则 # */
export function songIndexLetter(title: string): string {
  const key = songTitleSortKey(title)
  if (!key) return '#'
  const c = key[0]!
  if (c >= 'a' && c <= 'z') return c.toUpperCase()
  return '#'
}

export function compareSongsByTitlePinyin(a: Song, b: Song): number {
  const ka = songTitleSortKey(a.title)
  const kb = songTitleSortKey(b.title)
  const cmp = ka.localeCompare(kb)
  if (cmp !== 0) return cmp
  return (a.artist ?? '').localeCompare(b.artist ?? '', 'zh-Hans-CN')
}

/**
 * 假定 songs 已按拼音序排好，按首字母分组成块（用于分段标题 + 右侧索引）。
 */
export function groupSongsByIndexLetter(songs: Song[]): { letter: string; songs: Song[] }[] {
  if (songs.length === 0) return []
  const groups: { letter: string; songs: Song[] }[] = []
  let letter = ''
  let bucket: Song[] = []
  for (const song of songs) {
    const L = songIndexLetter(song.title)
    if (L !== letter) {
      if (bucket.length > 0) {
        groups.push({ letter, songs: bucket })
      }
      letter = L
      bucket = [song]
    } else {
      bucket.push(song)
    }
  }
  if (bucket.length > 0) {
    groups.push({ letter, songs: bucket })
  }
  return groups
}

/** DOM id：# 用专用后缀，避免非法 id */
export function indexSectionDomId(letter: string): string {
  return letter === '#' ? 'song-index-sharp' : `song-index-${letter}`
}

/**
 * 右侧导航条字母顺序：A–Z 升序，不在 A–Z 的记为 # 且排在最后（与链路上分组顺序无关）。
 */
export function sortIndexNavLetters(letters: string[]): string[] {
  const uniq = [...new Set(letters)]
  const alpha = uniq.filter((l) => l !== '#').sort((a, b) => a.localeCompare(b))
  const hasSharp = uniq.includes('#')
  return hasSharp ? [...alpha, '#'] : alpha
}
