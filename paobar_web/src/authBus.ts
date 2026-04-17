import { ref } from 'vue'

/** 全局登录弹窗控制 */
export const showLoginDialog = ref(false)

/** 登录成功后的回调队列 */
const callbacks: Array<() => void> = []

/**
 * 要求登录：弹出登录弹窗，登录成功后执行回调
 * @param onSuccess 登录成功后执行的回调（可选）
 */
export function requireLogin(onSuccess?: () => void) {
  if (onSuccess) {
    callbacks.push(onSuccess)
  }
  showLoginDialog.value = true
}

/** 登录成功时调用，执行并清空所有回调 */
export function onLoginSuccess() {
  showLoginDialog.value = false
  const cbs = callbacks.splice(0)
  cbs.forEach(cb => {
    try { cb() } catch (e) { console.error(e) }
  })
}

/** 关闭登录弹窗（取消） */
export function dismissLoginDialog() {
  showLoginDialog.value = false
  callbacks.splice(0) // 清空回调
}
