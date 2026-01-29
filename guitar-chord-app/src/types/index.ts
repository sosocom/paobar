export interface Song {
  id: string
  title: string
  artist: string
  coverUrl: string
  difficulty: number // 1-5
  key: string
  chordCount: number
  bpm: number
  genre: string
  chordSheet: ChordSheet
}

export interface ChordSheet {
  sections: ChordSection[]
}

export interface ChordSection {
  name: string // "主歌", "副歌", "间奏"
  lines: ChordLine[]
}

export interface ChordLine {
  chords: string // "Am    C    G    F"
  lyrics: string // "想带你去看晴空万里"
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
