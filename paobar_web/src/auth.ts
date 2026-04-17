const TOKEN_KEY = 'paobar_token'
const USER_KEY = 'paobar_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getStoredUser(): import('@/types').UserProfile | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as import('@/types').UserProfile
  } catch {
    return null
  }
}

export function setStoredUser(user: import('@/types').UserProfile) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function isLoggedIn(): boolean {
  return !!getToken()
}
