import { ref, computed } from 'vue'
import type { UserProfile } from '@/types'

const TOKEN_KEY = 'paobar_token'
const USER_KEY = 'paobar_user'

function readStoredUser(): UserProfile | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    return null
  }
}

/** 全局响应式当前用户。BottomNav / Profile / 路由守卫均读这一份。 */
export const currentUser = ref<UserProfile | null>(readStoredUser())

/** 当前用户是否管理员（仅前端 UI 决策使用，权限以后端为准）。 */
export const isAdmin = computed(() => currentUser.value?.isAdmin === true)

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  currentUser.value = null
}

export function getStoredUser(): UserProfile | null {
  return currentUser.value
}

export function setStoredUser(user: UserProfile) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
  currentUser.value = user
}

export function isLoggedIn(): boolean {
  return !!getToken()
}
