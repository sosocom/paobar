/**
 * 规范化吉他谱文档（schemaVersion=1）。
 *
 * 后端爬虫入库时即把原始 HTML 规范化为这份 JSON；前端不再解析 HTML，
 * 直接按 blocks / segments 渲染。字段与 Java 侧 `com.sosocom.tabdoc.TabDocument`
 * 一一对应，discriminator 统一用 `type` 字段。
 */
export interface TabDocument {
  schemaVersion: number
  title?: string
  info?: InfoItem[]
  meter?: string        // 拍号，如 "4/4"
  bpm?: string          // 拍速，如 "70"
  capoKey?: string      // 选调（实际弹奏调），如 "G"
  originalKey?: string  // 原唱调，如 "E"
  chordStyle?: string   // 数字谱 / 字母谱
  instrument?: string   // 乐器标识
  blocks: SheetBlock[]
}

export interface InfoItem {
  label: string
  text: string
}

export type SheetBlock =
  | SheetHeadline
  | SheetParagraph
  | SheetBlank

export interface SheetHeadline {
  type: 'headline'
  text: string
}

export interface SheetParagraph {
  type: 'paragraph'
  segments: LineSegment[]
}

export interface SheetBlank {
  type: 'blank'
}

export type LineSegment = PlainTextSegment | ChordSegment

export interface PlainTextSegment {
  type: 'text'
  text: string
}

export interface ChordSegment {
  type: 'chord'
  chord: string
  text: string
}

export interface Song {
  id: string
  title: string
  artist: string
  originalUrl?: string
  lyrics?: string
  /** 规范化吉他谱文档，仅在歌曲详情接口里返回；列表接口为空。 */
  tabDocument?: TabDocument
  tabImageUrl?: string
  difficulty?: string
  tuning?: string
  capo?: number
  playKey?: string
  originalKey?: string
  beat?: string
  viewCount?: number
  favoriteCount?: number
}

export interface Playlist {
  id: string
  name: string
  coverUrl?: string
  gradient?: string[]
  songCount: number
  songs: string[] // song ids
  type: 'user' | 'ai'
  chordProgression?: string // for AI playlists "1-5-6-4"
}

export interface UserProfile {
  id: string
  username: string
  avatar: string
  points: number
  /** 是否管理员（仅管理员可见管理员专属功能）。 */
  isAdmin?: boolean
  stats: {
    collected: number
    playlists: number
    practiceHours: number
  }
}

/** 管理员后台返回的用户列表项。 */
export interface AdminUser {
  id: string
  username: string
  avatar?: string
  points?: number
  isAdmin: boolean
  /** 1-启用，0-禁用。 */
  status: number
  collected?: number
  playlistsCount?: number
  createTime?: string
}

export interface AdminUserPage {
  records: AdminUser[]
  total: number
  page: number
  pageSize: number
}
