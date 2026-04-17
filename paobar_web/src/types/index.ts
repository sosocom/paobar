export interface Song {
  id: string
  title: string
  artist: string
  originalUrl?: string
  lyrics?: string
  tabContent?: string // HTML格式的吉他谱内容
  tabImageUrl?: string
  meta?: string // xhe-meta 元信息
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
  stats: {
    collected: number
    playlists: number
    practiceHours: number
  }
}
