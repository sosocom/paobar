import type { Song, Playlist, UserProfile } from '@/types'

// 模拟网络延迟
const delay = (ms: number = 300) => new Promise(resolve => setTimeout(resolve, ms))

// Mock API 基础类
class MockAPI {
  // 获取所有歌曲
  async getSongs(params?: { genre?: string; search?: string }): Promise<Song[]> {
    await delay()
    
    const { mockSongs } = await import('@/data/mock')
    let result = [...mockSongs]
    
    // 筛选类型
    if (params?.genre && params.genre !== '全部') {
      result = result.filter(song => song.genre === params.genre)
    }
    
    // 搜索
    if (params?.search) {
      const query = params.search.toLowerCase()
      result = result.filter(song => 
        song.title.toLowerCase().includes(query) || 
        song.artist.toLowerCase().includes(query)
      )
    }
    
    return result
  }
  
  // 根据 ID 获取单首歌曲
  async getSongById(id: string): Promise<Song | null> {
    await delay()
    
    const { mockSongs } = await import('@/data/mock')
    return mockSongs.find(song => song.id === id) || null
  }
  
  // 获取所有歌单
  async getPlaylists(): Promise<Playlist[]> {
    await delay()
    
    const { mockPlaylists } = await import('@/data/mock')
    return [...mockPlaylists]
  }
  
  // 根据类型获取歌单
  async getPlaylistsByType(type: 'user' | 'ai'): Promise<Playlist[]> {
    await delay()
    
    const { mockPlaylists } = await import('@/data/mock')
    return mockPlaylists.filter(p => p.type === type)
  }
  
  // 根据 ID 获取歌单详情
  async getPlaylistById(id: string): Promise<Playlist | null> {
    await delay()
    
    const { mockPlaylists } = await import('@/data/mock')
    return mockPlaylists.find(p => p.id === id) || null
  }
  
  // 获取歌单中的歌曲
  async getPlaylistSongs(playlistId: string): Promise<Song[]> {
    await delay()
    
    const { mockPlaylists, mockSongs } = await import('@/data/mock')
    const playlist = mockPlaylists.find(p => p.id === playlistId)
    
    if (!playlist) return []
    
    return playlist.songs
      .map(songId => mockSongs.find(s => s.id === songId))
      .filter(song => song !== undefined) as Song[]
  }
  
  // 获取当前用户信息
  async getCurrentUser(): Promise<UserProfile> {
    await delay()
    
    const { mockUser } = await import('@/data/mock')
    return { ...mockUser }
  }
  
  // 获取当前播放列表
  async getCurrentPlaylist(): Promise<Song[]> {
    await delay()
    
    const { currentPlaylist } = await import('@/data/mock')
    return [...currentPlaylist]
  }
  
  // 创建歌单
  async createPlaylist(name: string): Promise<Playlist> {
    await delay(500)
    
    const newPlaylist: Playlist = {
      id: `p${Date.now()}`,
      name,
      gradient: ['#DC2626', '#7C2D12'],
      songCount: 0,
      songs: [],
      type: 'user'
    }
    
    return newPlaylist
  }
  
  // 添加歌曲到歌单
  async addSongToPlaylist(playlistId: string, songId: string): Promise<boolean> {
    await delay(300)
    
    // 这里只是模拟，实际应该更新数据
    return true
  }
  
  // 从歌单中移除歌曲
  async removeSongFromPlaylist(playlistId: string, songId: string): Promise<boolean> {
    await delay(300)
    
    return true
  }
  
  // 收藏歌曲
  async favoriteSong(songId: string): Promise<boolean> {
    await delay(300)
    
    return true
  }
  
  // 取消收藏歌曲
  async unfavoriteSong(songId: string): Promise<boolean> {
    await delay(300)
    
    return true
  }
  
  // 获取收藏的歌曲列表
  async getFavoriteSongs(): Promise<Song[]> {
    await delay()
    
    const { mockSongs } = await import('@/data/mock')
    // 模拟返回前20首作为收藏的歌曲
    return mockSongs.slice(0, 20)
  }
}

// 导出单例
export const api = new MockAPI()

// 导出类型（方便其他地方使用）
export type API = MockAPI
