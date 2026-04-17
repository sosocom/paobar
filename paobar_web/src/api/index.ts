import type { Song, Playlist, UserProfile } from '@/types'
import { getToken, clearAuth } from '@/auth'
import { requireLogin } from '@/authBus'

// API基础URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8001'

// API 基础类
class API {
  private baseUrl: string

  constructor() {
    this.baseUrl = API_BASE_URL
  }

  private async request<T>(url: string, options?: RequestInit): Promise<T> {
    const token = getToken()
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options?.headers as Record<string, string>),
    }
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    const response = await fetch(`${this.baseUrl}${url}`, {
      ...options,
      headers,
    })

    const result = await response.json().catch(() => ({ code: 500, message: '网络错误' }))

    if (response.status === 401 || result.code === 401) {
      clearAuth()
      requireLogin() // 弹出全局登录弹窗
      throw new Error(result.message || '未登录或登录已过期')
    }

    if (result.code !== 200) {
      throw new Error(result.message || '请求失败')
    }

    return result.data
  }

  // 登录
  async login(username: string, password: string): Promise<{ token: string; user: UserProfile }> {
    return this.request<{ token: string; user: UserProfile }>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    })
  }

  // 注册
  async register(username: string, password: string): Promise<{ token: string; user: UserProfile }> {
    return this.request<{ token: string; user: UserProfile }>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    })
  }

  // 获取所有歌曲
  async getSongs(params?: { genre?: string; search?: string; page?: number; pageSize?: number }): Promise<Song[]> {
    const queryParams = new URLSearchParams()
    if (params?.genre && params.genre !== '全部') {
      queryParams.append('genre', params.genre)
    }
    if (params?.search) {
      queryParams.append('search', params.search)
    }
    if (params?.page) {
      queryParams.append('page', params.page.toString())
    }
    if (params?.pageSize) {
      queryParams.append('pageSize', params.pageSize.toString())
    }

    const queryString = queryParams.toString()
    const url = `/api/songs${queryString ? `?${queryString}` : ''}`
    
    return this.request<Song[]>(url)
  }

  // 根据 ID 获取单首歌曲
  async getSongById(id: string): Promise<Song | null> {
    try {
      return await this.request<Song>(`/api/songs/${id}`)
    } catch (error) {
      console.error('获取歌曲失败:', error)
      return null
    }
  }

  // 获取所有歌单
  async getPlaylists(): Promise<Playlist[]> {
    return this.request<Playlist[]>('/api/playlists')
  }

  // 根据类型获取歌单
  async getPlaylistsByType(type: 'user' | 'ai'): Promise<Playlist[]> {
    return this.request<Playlist[]>(`/api/playlists?type=${type}`)
  }

  // 根据 ID 获取歌单详情
  async getPlaylistById(id: string): Promise<Playlist | null> {
    try {
      return await this.request<Playlist>(`/api/playlists/${id}`)
    } catch (error) {
      console.error('获取歌单失败:', error)
      return null
    }
  }

  // 获取歌单中的歌曲
  async getPlaylistSongs(playlistId: string): Promise<Song[]> {
    return this.request<Song[]>(`/api/playlists/${playlistId}/songs`)
  }

  // 获取当前用户信息
  async getCurrentUser(): Promise<UserProfile> {
    return this.request<UserProfile>('/api/user/profile')
  }

  // 获取当前播放列表
  async getCurrentPlaylist(): Promise<Song[]> {
    return this.request<Song[]>('/api/songs/current')
  }

  // 创建歌单
  async createPlaylist(name: string): Promise<Playlist> {
    return this.request<Playlist>('/api/playlists', {
      method: 'POST',
      body: JSON.stringify({ name }),
    })
  }

  // 更新歌单名称
  async updatePlaylistName(playlistId: string, name: string): Promise<Playlist> {
    return this.request<Playlist>(`/api/playlists/${playlistId}`, {
      method: 'PUT',
      body: JSON.stringify({ name }),
    })
  }

  // 添加歌曲到歌单
  async addSongToPlaylist(playlistId: string, songId: string): Promise<boolean> {
    try {
      await this.request<boolean>(`/api/playlists/${playlistId}/songs`, {
        method: 'POST',
        body: JSON.stringify({ songId }),
      })
      return true
    } catch (error) {
      console.error('添加歌曲到歌单失败:', error)
      return false
    }
  }

  // 从歌单中移除歌曲
  async removeSongFromPlaylist(playlistId: string, songId: string): Promise<boolean> {
    try {
      await this.request<boolean>(`/api/playlists/${playlistId}/songs/${songId}`, {
        method: 'DELETE',
      })
      return true
    } catch (error) {
      console.error('从歌单移除歌曲失败:', error)
      return false
    }
  }

  // 歌单内歌曲置顶
  async movePlaylistSongToTop(playlistId: string, songId: string): Promise<boolean> {
    try {
      await this.request<boolean>(`/api/playlists/${playlistId}/songs/${songId}/move-to-top`, { method: 'PUT' })
      return true
    } catch (error) {
      console.error('置顶失败:', error)
      return false
    }
  }

  // 歌单内歌曲置底
  async movePlaylistSongToBottom(playlistId: string, songId: string): Promise<boolean> {
    try {
      await this.request<boolean>(`/api/playlists/${playlistId}/songs/${songId}/move-to-bottom`, { method: 'PUT' })
      return true
    } catch (error) {
      console.error('置底失败:', error)
      return false
    }
  }

  // 歌单内歌曲拖动排序，songIds 为新的顺序
  async reorderPlaylistSongs(playlistId: string, songIds: string[]): Promise<boolean> {
    try {
      await this.request<boolean>(`/api/playlists/${playlistId}/songs/reorder`, {
        method: 'PUT',
        body: JSON.stringify(songIds),
      })
      return true
    } catch (error) {
      console.error('排序失败:', error)
      return false
    }
  }

  // 收藏歌曲
  async favoriteSong(songId: string): Promise<boolean> {
    try {
      await this.request<boolean>('/api/user/favorites', {
        method: 'POST',
        body: JSON.stringify({ songId }),
      })
      return true
    } catch (error) {
      console.error('收藏歌曲失败:', error)
      return false
    }
  }

  // 取消收藏歌曲
  async unfavoriteSong(songId: string): Promise<boolean> {
    try {
      await this.request<boolean>(`/api/user/favorites/${songId}`, {
        method: 'DELETE',
      })
      return true
    } catch (error) {
      console.error('取消收藏失败:', error)
      return false
    }
  }

  // 获取收藏的歌曲列表
  async getFavoriteSongs(): Promise<Song[]> {
    return this.request<Song[]>('/api/user/favorites')
  }

  // 获取收藏歌曲ID列表（轻量）
  async getFavoriteSongIds(): Promise<string[]> {
    const ids = await this.request<number[]>('/api/user/favorite-ids')
    return ids.map(String)
  }
}

// 导出单例
export const api = new API()

// 导出类型（方便其他地方使用）
export type { API }
