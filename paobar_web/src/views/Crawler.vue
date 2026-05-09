<template>
  <div class="min-h-screen bg-background pb-24">
    <!-- 管理员模式标识：仅管理员可见此页，明确告诉用户进入了受限功能 -->
    <div class="flex justify-center pt-4 px-5">
      <div class="w-full max-w-[335px] flex items-center gap-2 px-3 py-2 rounded-xl bg-primary/10 border border-primary/20">
        <Shield :size="14" class="text-primary flex-shrink-0" />
        <span class="text-xs text-primary font-medium">管理员模式</span>
        <span class="text-xs text-text-secondary truncate">扒谱仅对管理员开放</span>
      </div>
    </div>
    <!-- Tab 主入口 -->
    <div class="flex justify-center pt-3 px-5">
      <div class="w-full max-w-[335px] bg-background-card rounded-xl p-1 flex gap-1">
        <button
          @click="activeTab = 'song'"
          :class="[
            'flex-1 h-10 rounded-lg text-sm font-medium transition-all',
            activeTab === 'song' 
              ? 'bg-primary text-white' 
              : 'text-text-secondary hover:text-text-primary'
          ]"
        >
          爬单曲
        </button>
        <button
          @click="activeTab = 'account'"
          :class="[
            'flex-1 h-10 rounded-lg text-sm font-medium transition-all',
            activeTab === 'account' 
              ? 'bg-primary text-white' 
              : 'text-text-secondary hover:text-text-primary'
          ]"
        >
          爬账号
        </button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="flex flex-col items-center justify-center px-5 pt-8">
      <!-- 爬单曲 Tab -->
      <div v-if="activeTab === 'song'" class="w-full max-w-[335px]">
        <!-- 说明文字 -->
        <p class="text-sm text-text-secondary text-center mb-6">
          请输入 yopu.co 原谱链接
        </p>

        <!-- 输入框区域 -->
        <div class="space-y-4">
          <!-- 输入框 -->
          <div class="relative">
            <input
              v-model="songUrl"
              type="text"
              placeholder="https://yopu.co/view/..."
              class="w-full h-12 px-4 bg-background-card text-text-primary text-sm rounded-xl border border-white/5 focus:border-primary/50 focus:outline-none transition-colors"
              @keyup.enter="crawlSong"
            />
          </div>

          <!-- 获取按钮 -->
          <button
            @click="crawlSong"
            :disabled="songLoading || !songUrl.trim()"
            class="w-full h-12 bg-primary text-white font-semibold text-base rounded-xl hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {{ songLoading ? '获取中...' : '获取' }}
          </button>
        </div>

        <!-- 错误提示 -->
        <div v-if="songError" class="mt-6 p-4 bg-red-500/10 border border-red-500/20 rounded-lg">
          <p class="text-sm text-red-400">{{ songError }}</p>
        </div>

        <!-- 成功提示 -->
        <div v-if="songSuccess" class="mt-6 p-4 bg-green-500/10 border border-green-500/20 rounded-lg">
          <div class="space-y-2">
            <p class="text-sm text-green-400 font-medium">{{ songSuccess }}</p>
            <div v-if="crawledSong" class="text-xs text-green-300/80">
              <p>歌曲：{{ crawledSong.songName }}</p>
              <p>艺术家：{{ crawledSong.artist }}</p>
            </div>
            <button
              @click="goToLibrary"
              class="mt-2 text-xs text-green-400 hover:text-green-300 underline"
            >
              前往谱库查看 →
            </button>
          </div>
        </div>

        <!-- 使用说明 -->
        <div class="mt-12 space-y-3">
          <h3 class="text-sm font-medium text-text-primary">使用说明：</h3>
          <ol class="text-xs text-text-secondary space-y-2 list-decimal list-inside">
            <li>访问 <a href="https://yopu.co" target="_blank" class="text-primary hover:underline">yopu.co</a> 网站</li>
            <li>找到你想要的吉他谱</li>
            <li>复制页面的完整链接</li>
            <li>粘贴到上方输入框中</li>
            <li>点击"获取"按钮</li>
          </ol>
        </div>

        <!-- 示例图片说明 -->
        <div class="mt-8 space-y-4">
          <h3 class="text-sm font-medium text-text-primary">支持的谱类型：</h3>
          
          <!-- 支持的格式示例 -->
          <div class="space-y-2">
            <div class="flex items-center gap-2">
              <div class="flex-shrink-0 w-5 h-5 rounded-full bg-green-500/20 flex items-center justify-center">
                <svg class="w-3 h-3 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
                </svg>
              </div>
              <span class="text-xs text-text-secondary">支持带和弦标注的谱</span>
            </div>
            <div class="rounded-lg overflow-hidden border border-green-500/20">
              <img 
                src="/ok_tab.jpg" 
                alt="支持的谱类型示例" 
                class="w-full h-auto"
                @error="handleImageError"
              />
            </div>
            <p class="text-xs text-green-400/80 text-center">✓ 这种格式可以获取</p>
          </div>

          <!-- 不支持的格式示例 -->
          <div class="space-y-2">
            <div class="flex items-center gap-2">
              <div class="flex-shrink-0 w-5 h-5 rounded-full bg-red-500/20 flex items-center justify-center">
                <svg class="w-3 h-3 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </div>
              <span class="text-xs text-text-secondary">不支持纯图片格式的谱</span>
            </div>
            <div class="rounded-lg overflow-hidden border border-red-500/20 opacity-60">
              <img 
                src="/error_tab.jpg" 
                alt="不支持的谱类型示例" 
                class="w-full h-auto"
                @error="handleImageError"
              />
            </div>
            <p class="text-xs text-red-400/80 text-center">✗ 这种格式无法获取</p>
          </div>
        </div>
      </div>

      <!-- 爬账号 Tab -->
      <div v-if="activeTab === 'account'" class="w-full max-w-[335px]">
        <!-- 说明文字 -->
        <p class="text-sm text-text-secondary text-center mb-6">
          请输入 yopu.co 用户账号链接
        </p>

        <!-- 快选：常用账号一键填入 -->
        <div class="mb-4">
          <p class="text-xs text-text-secondary text-center mb-2">快选</p>
          <div class="flex flex-wrap gap-2 justify-center">
            <button
              v-for="pick in accountQuickPicks"
              :key="pick.label"
              type="button"
              @click="accountUrl = pick.url"
              class="px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors"
              :class="isAccountQuickPickActive(pick.url)
                ? 'bg-primary/15 border-primary text-primary'
                : 'bg-background-card border-white/10 text-text-secondary hover:border-primary/40 hover:text-primary'"
            >
              {{ pick.label }}
            </button>
          </div>
        </div>

        <!-- 输入框区域 -->
        <div class="space-y-4">
          <!-- 输入框 -->
          <div class="relative">
            <input
              v-model="accountUrl"
              type="text"
              placeholder="https://yopu.co/user#code=..."
              class="w-full h-12 px-4 bg-background-card text-text-primary text-sm rounded-xl border border-white/5 focus:border-primary/50 focus:outline-none transition-colors"
              @keyup.enter="crawlAccount"
            />
          </div>

          <!-- 获取按钮 -->
          <button
            @click="crawlAccount"
            :disabled="accountLoading || !accountUrl.trim()"
            class="w-full h-12 bg-primary text-white font-semibold text-base rounded-xl hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {{ accountLoading ? '获取中...' : '获取账号所有谱' }}
          </button>
        </div>

        <!-- 错误提示 -->
        <div v-if="accountError" class="mt-6 p-4 bg-red-500/10 border border-red-500/20 rounded-lg">
          <p class="text-sm text-red-400">{{ accountError }}</p>
        </div>

        <!-- 成功提示 -->
        <div v-if="accountSuccess" class="mt-6 p-4 bg-green-500/10 border border-green-500/20 rounded-lg">
          <div class="space-y-2">
            <p class="text-sm text-green-400 font-medium">{{ accountSuccess }}</p>
            <div v-if="crawledAccount" class="text-xs text-green-300/80 space-y-1">
              <p v-if="crawledAccount.userName">账号：{{ crawledAccount.userName }}</p>
              <p>总计发现：{{ crawledAccount.totalFound }} 首</p>
              <p v-if="crawledAccount.alreadyInLibrary > 0">已在库：{{ crawledAccount.alreadyInLibrary }} 首</p>
              <p v-if="crawledAccount.alreadyPending > 0">已在队列：{{ crawledAccount.alreadyPending }} 首</p>
              <p v-if="crawledAccount.enqueued > 0">本次入队：{{ crawledAccount.enqueued }} 首</p>
              <p>成功获取：{{ crawledAccount.successCount }} 首</p>
              <p v-if="crawledAccount.failedCount > 0" class="text-red-300">失败：{{ crawledAccount.failedCount }} 首</p>
            </div>
            <button
              @click="goToLibrary"
              class="mt-2 text-xs text-green-400 hover:text-green-300 underline"
            >
              前往谱库查看 →
            </button>
          </div>
        </div>

        <!-- 进度提示 -->
        <div
          v-if="accountProgress || accountQueueHint"
          class="mt-6 p-4 bg-blue-500/10 border border-blue-500/20 rounded-lg"
        >
          <div class="space-y-2">
            <p v-if="accountProgress" class="text-sm text-blue-400">{{ accountProgress }}</p>
            <p v-if="accountQueueHint" class="text-xs text-amber-400/90">{{ accountQueueHint }}</p>
            <div
              v-if="accountProgress"
              class="w-full bg-background-card rounded-full h-2 overflow-hidden"
            >
              <div 
                class="h-full bg-primary transition-all duration-300"
                :style="{ width: `${progressPercent}%` }"
              ></div>
            </div>
          </div>
        </div>

        <!-- 使用说明 -->
        <div class="mt-12 space-y-3">
          <h3 class="text-sm font-medium text-text-primary">使用说明：</h3>
          <ol class="text-xs text-text-secondary space-y-2 list-decimal list-inside">
            <li>访问 <a href="https://yopu.co" target="_blank" class="text-primary hover:underline">yopu.co</a> 网站</li>
            <li>进入用户的个人主页</li>
            <li>复制浏览器地址栏的链接</li>
            <li>链接格式：https://yopu.co/user#code=xxx</li>
            <li>粘贴到上方输入框中</li>
            <li>点击"获取账号所有谱"按钮</li>
          </ol>
          <div class="mt-4 p-3 bg-yellow-500/10 border border-yellow-500/20 rounded-lg">
            <p class="text-xs text-yellow-400">
              ⚠️ 账号队列由服务端定时、分批爬取，关闭页面也会继续；
              大账号耗时会较长，可稍后在谱库查看进度结果。
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Shield } from 'lucide-vue-next'
import { getToken, isLoggedIn } from '@/auth'
import { requireLogin } from '@/authBus'

const router = useRouter()

// Tab 状态：默认「爬账号」，快选「蓝先生」已预填
const activeTab = ref<'song' | 'account'>('account')

// 爬单曲
const songUrl = ref('https://yopu.co/view/3PbjYEYP')
const songLoading = ref(false)
const songError = ref('')
const songSuccess = ref('')
const crawledSong = ref<any>(null)

// 爬账号
const accountUrl = ref('https://yopu.co/user#code=rp8kWlGP')
/** 输入框上方的快选（用户主页链接）。第一项即默认选中的账号。 */
const accountQuickPicks = [
  { label: '蓝先生', url: 'https://yopu.co/user#code=rp8kWlGP' },
  { label: '杨昌建', url: 'https://yopu.co/user#code=g104Dqa1' },
  { label: '菜菜的吉他手', url: 'https://yopu.co/user#code=b19JMeQX' },
] as const

const isAccountQuickPickActive = (url: string) =>
  accountUrl.value.trim() === url
const accountLoading = ref(false)
const accountError = ref('')
const accountSuccess = ref('')
const accountProgress = ref('')
const crawledAccount = ref<any>(null)
const progressPercent = ref(0)
/** 长在跑时提示：队列由后端定时任务继续消费 */
const accountQueueHint = ref('')

let releaseAccountPoll: (() => void) | null = null

const stopAccountPoll = () => {
  releaseAccountPoll?.()
  releaseAccountPoll = null
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8001'

/** 带 token 的 fetch 封装 */
const authFetch = (url: string, options?: RequestInit) => {
  const token = getToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options?.headers as Record<string, string> || {}),
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return fetch(url, { ...options, headers })
}

// 爬取单曲
const crawlSong = async () => {
  if (!isLoggedIn()) {
    requireLogin(() => crawlSong())
    return
  }
  songError.value = ''
  songSuccess.value = ''
  crawledSong.value = null

  if (!songUrl.value.trim()) {
    songError.value = '请输入链接'
    return
  }

  if (!songUrl.value.includes('yopu.co/view')) {
    songError.value = '请输入有效的 yopu.co 歌曲链接'
    return
  }

  songLoading.value = true

  try {
    const response = await authFetch(`${API_BASE_URL}/api/crawler/crawl`, {
      method: 'POST',
      body: JSON.stringify({ url: songUrl.value.trim() })
    })

    const result = await response.json()

    if (result.code === 200) {
      crawledSong.value = result.data
      songSuccess.value = '获取成功！谱子已添加到谱库'
      songUrl.value = ''
    } else {
      songError.value = result.message || '获取失败，请稍后重试'
    }
  } catch (err) {
    console.error('爬取失败:', err)
    songError.value = '网络错误，请检查后端服务是否启动'
  } finally {
    songLoading.value = false
  }
}

// 爬取账号
const crawlAccount = async () => {
  if (!isLoggedIn()) {
    requireLogin(() => crawlAccount())
    return
  }
  stopAccountPoll()
  accountError.value = ''
  accountSuccess.value = ''
  accountProgress.value = ''
  accountQueueHint.value = ''
  crawledAccount.value = null
  progressPercent.value = 0

  if (!accountUrl.value.trim()) {
    accountError.value = '请输入链接'
    return
  }

  if (!accountUrl.value.includes('yopu.co/user')) {
    accountError.value = '请输入有效的 yopu.co 用户链接'
    return
  }

  accountLoading.value = true
  accountProgress.value = '正在提交爬取任务...'

  try {
    const userCode = extractUserCode(accountUrl.value)
    if (!userCode) {
      accountError.value = '无法提取用户代码'
      accountLoading.value = false
      return
    }

    const response = await authFetch(`${API_BASE_URL}/api/crawler/crawl-account-async`, {
      method: 'POST',
      body: JSON.stringify({ url: accountUrl.value.trim() })
    })

    const result = await response.json()

    if (result.code === 200) {
      // 后端入队完成后会立刻返回统计：发现总数 / 已在库 / 已在队列 / 新入队
      const stats = result.data || {}
      const enqueued = Number(stats.enqueued ?? 0)
      const totalFound = Number(stats.totalFound ?? 0)

      if (enqueued <= 0) {
        // 没有新链接需要爬，直接结算成功态（后端 manager 也已经标 COMPLETED）
        accountLoading.value = false
        accountProgress.value = ''
        progressPercent.value = 100
        crawledAccount.value = stats
        accountSuccess.value = totalFound > 0
          ? '账号下所有谱已在库中，无需重新爬取'
          : '该账号下未发现可爬取的谱链接'
        accountUrl.value = ''
        return
      }

      accountProgress.value = `已入队 ${enqueued} 首（发现 ${totalFound} / 已在库 ${stats.alreadyInLibrary ?? 0} / 已在队列 ${stats.alreadyPending ?? 0}），正在爬取...`
      progressPercent.value = 1
      pollCrawlStatus(userCode)
    } else {
      accountError.value = result.message || '提交任务失败'
      accountProgress.value = ''
      accountLoading.value = false
    }
  } catch (err) {
    console.error('提交爬取任务失败:', err)
    accountError.value = '网络错误，请检查后端服务是否启动'
    accountProgress.value = ''
    accountLoading.value = false
  }
}

// 提取用户代码
const extractUserCode = (url: string): string | null => {
  if (url.includes('code=')) {
    const codeIndex = url.indexOf('code=')
    let code = url.substring(codeIndex + 5)
    const ampIndex = code.indexOf('&')
    if (ampIndex > 0) {
      code = code.substring(0, ampIndex)
    }
    return code.trim()
  }
  return null
}

// 轮询任务状态
const pollCrawlStatus = async (userCode: string) => {
  stopAccountPoll()
  const hintTimer = window.setTimeout(() => {
    if (!accountLoading.value) return
    accountQueueHint.value =
      '服务端按固定间隔分批爬取队列，离开本页仍会执行；可随时到谱库查看新谱。'
  }, 90_000)
  const pollInterval = window.setInterval(async () => {
    try {
      const response = await authFetch(`${API_BASE_URL}/api/crawler/crawl-account-status/${userCode}`)
      const result = await response.json()

      if (result.code === 200) {
        const task = result.data

        if (task.total > 0) {
          progressPercent.value = task.progress || 0
          // 队列流程下 total = 本次入队数；current = 已处理（成功+失败）数
          accountProgress.value = `正在爬取: ${task.current}/${task.total}`
            + (task.failedCount > 0 ? ` (失败 ${task.failedCount})` : '')
        }

        if (task.status === 'COMPLETED') {
          stopAccountPoll()
          accountLoading.value = false
          accountProgress.value = ''
          accountQueueHint.value = ''
          crawledAccount.value = task.result
          accountSuccess.value = task.failedCount > 0
            ? `爬取完成：成功 ${task.successCount} 首，失败 ${task.failedCount} 首`
            : '获取成功！所有谱子已添加到谱库'
          progressPercent.value = 100
          accountUrl.value = ''
        } else if (task.status === 'FAILED') {
          stopAccountPoll()
          accountLoading.value = false
          accountProgress.value = ''
          accountQueueHint.value = ''
          accountError.value = task.error || '爬取失败'
          progressPercent.value = 0
        }
      } else {
        stopAccountPoll()
        accountLoading.value = false
        accountProgress.value = ''
        accountQueueHint.value = ''
      }
    } catch (err) {
      console.error('查询任务状态失败:', err)
      stopAccountPoll()
      accountLoading.value = false
      accountProgress.value = ''
      accountQueueHint.value = ''
      accountError.value = '查询任务状态失败'
    }
  }, 2000)

  releaseAccountPoll = () => {
    window.clearInterval(pollInterval)
    window.clearTimeout(hintTimer)
  }
}

onBeforeUnmount(() => {
  stopAccountPoll()
})

const goToLibrary = () => {
  router.push('/library')
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}
</script>
