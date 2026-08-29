import { h } from 'vue'
import { ElNotification } from 'element-plus'

declare const __APP_BUILD_TIME__: string

const CHECK_INTERVAL_MS = 3 * 60 * 1000
const BUILD_TIME_RE = /<meta\s+name=["']buildTime["']\s+content=["']([^"']+)["']\s*\/?>/i

function normalizeFlag(v?: string) {
  return (v || '').trim().replace(/^['"]|['"]$/g, '')
}

function extractBuildTime(html: string): string {
  const match = html.match(BUILD_TIME_RE)
  return match?.[1]?.trim() || ''
}

async function fetchRemoteBuildTime(): Promise<string> {
  const base = import.meta.env.BASE_URL || '/'
  const url = `${base}index.html?time=${Date.now()}`
  const res = await fetch(url, {
    method: 'GET',
    cache: 'no-store',
    headers: { 'Cache-Control': 'no-cache' }
  })
  if (!res.ok) return ''
  const html = await res.text()
  return extractBuildTime(html)
}

function showUpdateNotification() {
  const key = 'app-version-update'
  ElNotification.closeAll()
  ElNotification({
    title: '发现新版本',
    message: h('div', { style: 'line-height:1.6' }, [
      h('p', { style: 'margin:0 0 10px' }, '系统已更新，请刷新页面后继续使用。'),
      h(
        'div',
        { style: 'display:flex;gap:12px;justify-content:flex-end' },
        [
          h(
            'button',
            {
              style: 'border:none;background:transparent;color:var(--el-color-info);cursor:pointer;padding:0',
              onClick: () => ElNotification.closeAll()
            },
            '稍后再说'
          ),
          h(
            'button',
            {
              style: 'border:none;background:transparent;color:var(--el-color-primary);cursor:pointer;padding:0;font-weight:600',
              onClick: () => location.reload()
            },
            '立即刷新'
          )
        ]
      )
    ]),
    type: 'warning',
    duration: 0,
    position: 'top-right',
    customClass: key
  })
}

/**
 * 生产环境轮询 index.html 的 buildTime，发现新版本后提示刷新。
 * 开关：VITE_AUTOMATICALLY_DETECT_UPDATE=Y
 */
export function setupAppVersionNotification() {
  const enabled = normalizeFlag(import.meta.env.VITE_AUTOMATICALLY_DETECT_UPDATE) === 'Y'
  if (!import.meta.env.PROD || !enabled) return

  const localBuildTime = typeof __APP_BUILD_TIME__ === 'string' ? __APP_BUILD_TIME__ : ''
  if (!localBuildTime) return

  let checking = false
  let notified = false

  const check = async () => {
    if (checking || notified || document.hidden) return
    checking = true
    try {
      const remoteBuildTime = await fetchRemoteBuildTime()
      if (remoteBuildTime && remoteBuildTime !== localBuildTime) {
        notified = true
        showUpdateNotification()
      }
    } catch {
      // 忽略网络抖动
    } finally {
      checking = false
    }
  }

  void check()
  window.setInterval(() => {
    void check()
  }, CHECK_INTERVAL_MS)

  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) void check()
  })
}
