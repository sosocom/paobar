/**
 * PWA 安装总线（单例）
 *
 * 统一持有：
 *   - beforeinstallprompt 事件对象（deferredPrompt），让 Android/桌面 Chrome 可以 programmatic install
 *   - 平台判定（isIOS / isStandalone）
 *   - 自动底部条的 cooldown（7 天）
 *   - 手动居中向导（从"全部/已收藏"行的 App 按钮触发）开关
 *
 * 让 InstallPrompt.vue（底部自动条）和 InstallWizardDialog.vue（居中手动向导）
 * 共用同一套状态，不用各写一遍事件监听。
 *
 * 调用方：
 *   - main.ts 在启动时调一次 `initInstallBus()` 注册全局事件监听
 *   - 首页"App"按钮调 `openInstallWizard()` 打开居中向导
 *   - 向导里"立即安装"调 `triggerNativeInstall()` 触发浏览器原生弹窗
 */
import { ref, computed } from 'vue'

const DISMISS_KEY = 'pwa-install-dismissed-at'
const DISMISS_TTL_MS = 7 * 24 * 60 * 60 * 1000 // 7 天

/** Chrome / Edge 原生安装事件 */
interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>
}

// ---- 设备 / 运行态判定（SSR-safe：无 window 时回落到 false） ----
const ua = typeof navigator !== 'undefined' ? navigator.userAgent : ''

/** 原生 iOS Safari：iPhone/iPad UA + 不带第三方浏览器标记 */
export const isIOS =
  /iPhone|iPad|iPod/i.test(ua) && !/CriOS|EdgiOS|FxiOS/i.test(ua)

/** iOS 上的第三方浏览器（Chrome/Edge/Firefox/UC/QQ 浏览器等）：
 *  都是 WebKit 壳，但完全不支持"加到主屏"。需要引导用户换回 Safari。 */
export const isIOSNonSafari =
  /iPhone|iPad|iPod/i.test(ua) && /CriOS|EdgiOS|FxiOS|UCBrowser|QQBrowser/i.test(ua)

/** 国内常见 App 内置浏览器：完全禁用 PWA 安装入口，只能引导到系统浏览器打开。
 *  - 微信：MicroMessenger
 *  - QQ/QQ 空间：QQ/ ; Qzone
 *  - 微博：Weibo
 *  - 抖音：aweme
 *  - 小红书：xhsdsl / xhs（小红书不同版本写法不同）
 *  - 钉钉：DingTalk
 *  - 飞书：Lark
 *  - 企业微信：wxwork */
export const isInAppBrowser =
  /MicroMessenger|QQ\/|Qzone|Weibo|aweme|xhsdsl|xhs|DingTalk|Lark|wxwork/i.test(ua)

const checkStandalone = (): boolean => {
  if (typeof window === 'undefined') return false
  if (window.matchMedia?.('(display-mode: standalone)').matches) return true
  // iOS Safari 专用属性
  if ((navigator as unknown as { standalone?: boolean }).standalone) return true
  return false
}

// ---- 响应式状态 ----
/** 拿到 beforeinstallprompt 后保存，点击"安装"时复用 */
const deferredPrompt = ref<BeforeInstallPromptEvent | null>(null)
/** 是否已以 PWA（standalone）方式打开 */
export const isStandalone = ref<boolean>(checkStandalone())
/** 自动底部条是否可见（由 InstallPrompt.vue 渲染） */
export const autoBarVisible = ref<boolean>(false)
/** 手动居中向导是否可见（由 InstallWizardDialog.vue 渲染） */
export const wizardVisible = ref<boolean>(false)

/** 当前平台是否支持"一键调起原生安装弹窗"——只有 Android/Chrome 拿到事件后才 true */
export const canPromptNative = computed<boolean>(() => deferredPrompt.value !== null)

/** 页面上是否还有必要露出"安装 App"按钮：已安装则直接隐藏 */
export const shouldShowInstallEntry = computed<boolean>(() => !isStandalone.value)

// ---- cooldown ----
const isRecentlyDismissed = (): boolean => {
  try {
    const raw = localStorage.getItem(DISMISS_KEY)
    if (!raw) return false
    const ts = Number(raw)
    if (!Number.isFinite(ts)) return false
    return Date.now() - ts < DISMISS_TTL_MS
  } catch {
    return false
  }
}

const rememberDismiss = () => {
  try {
    localStorage.setItem(DISMISS_KEY, String(Date.now()))
  } catch {
    // 隐私模式等写入失败，忽略
  }
}

// ---- 对外操作 ----

/** 打开/关闭居中安装向导（手动触发入口） */
export const openInstallWizard = () => {
  // 已经是 PWA 了就无意义，直接无操作
  if (isStandalone.value) return
  // 手动打开时也顺便把底部条收起，避免两层 UI 同时占屏
  autoBarVisible.value = false
  wizardVisible.value = true
}

export const closeInstallWizard = () => {
  wizardVisible.value = false
}

/** 关闭底部自动条并记入 7 天冷静期 */
export const dismissAutoBar = () => {
  autoBarVisible.value = false
  rememberDismiss()
}

/**
 * 触发浏览器原生安装弹窗（仅 Android/Chrome 等支持 BIP 的环境有效）。
 * 返回值：
 *   - 'accepted' | 'dismissed'：拿到了 choice
 *   - 'unavailable'：当前环境没有 deferredPrompt（iOS 或浏览器已经错过事件）
 */
export const triggerNativeInstall = async (): Promise<
  'accepted' | 'dismissed' | 'unavailable'
> => {
  const p = deferredPrompt.value
  if (!p) return 'unavailable'
  try {
    await p.prompt()
    const choice = await p.userChoice
    if (choice.outcome === 'accepted') {
      rememberDismiss()
      autoBarVisible.value = false
      wizardVisible.value = false
    }
    return choice.outcome
  } catch {
    return 'dismissed'
  } finally {
    // prompt 事件是一次性的，不论结果都要清掉
    deferredPrompt.value = null
  }
}

/**
 * 复制当前页面完整 URL 到剪贴板。
 * 用于"iOS 非 Safari"、"微信内置浏览器"等无法直接安装的分支，
 * 引导用户把链接粘贴到 Safari / 系统浏览器里重新打开。
 *
 * 优先走 Clipboard API；在微信/低版本浏览器里回落到 execCommand('copy')。
 * 返回 true 表示写入成功。
 */
export const copyCurrentUrl = async (): Promise<boolean> => {
  if (typeof window === 'undefined') return false
  const url = window.location.href
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(url)
      return true
    }
  } catch {
    // 继续回落
  }
  try {
    const ta = document.createElement('textarea')
    ta.value = url
    ta.setAttribute('readonly', '')
    ta.style.position = 'fixed'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    const ok = document.execCommand('copy')
    document.body.removeChild(ta)
    return ok
  } catch {
    return false
  }
}

/**
 * 由 InstallPrompt.vue 调用，决定首屏是否自动弹出底部条。
 * 显示的场景：
 *   - iOS Safari：没事件可等，直接显示（CTA 打开向导显示分步引导）
 *   - Android/桌面 Chrome 拿到 deferredPrompt：可以真一键装
 *   - 微信 / QQ 等 App 内置浏览器：显示引导"改用系统浏览器打开"
 *   - iOS 非 Safari：显示引导"改用 Safari 打开"
 * 已 standalone / 7 天内刚点过"稍后" → 不再打扰
 */
export const maybeShowAutoBar = () => {
  if (isStandalone.value) return
  if (isRecentlyDismissed()) return
  if (isIOS || isIOSNonSafari || isInAppBrowser || deferredPrompt.value) {
    autoBarVisible.value = true
  }
}

// ---- 初始化（只执行一次） ----
let initialized = false
export const initInstallBus = () => {
  if (initialized || typeof window === 'undefined') return
  initialized = true

  window.addEventListener('beforeinstallprompt', (e) => {
    // 拦截原生横幅，改由我们的 UI 触发
    e.preventDefault()
    deferredPrompt.value = e as BeforeInstallPromptEvent
  })

  window.addEventListener('appinstalled', () => {
    isStandalone.value = true
    autoBarVisible.value = false
    wizardVisible.value = false
    rememberDismiss()
  })

  // standalone 状态可能因窗口模式变化（极少见但存在，桌面端 Chrome 会有）
  window
    .matchMedia?.('(display-mode: standalone)')
    ?.addEventListener?.('change', (ev) => {
      isStandalone.value = ev.matches
    })
}
