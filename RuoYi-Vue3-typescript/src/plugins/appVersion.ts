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

function joinUrl(base: string, path: string) {
  const normalizedBase = base.endsWith('/') ? base : `${base}/`
  const normalizedPath = path.replace(/^\//, '')
  return `${normalizedBase}${normalizedPath}`
}

async function fetchHtml(url: string): Promise<string> {
  const res = await fetch(url, {
    method: 'GET',
    cache: 'no-store',
    headers: {
      'Cache-Control': 'no-cache',
      Pragma: 'no-cache'
    }
  })
  if (!res.ok) return ''
  return res.text()
}

async function fetchRemoteBuildTime(): Promise<string> {
  const base = import.meta.env.BASE_URL || '/'
  const stamp = Date.now()
  // 部分网关只配了 /，没有单独的 index.html 路由；两个都试
  const candidates = [
    `${joinUrl(base, 'index.html')}?t=${stamp}`,
    `${base}?t=${stamp}`
  ]

  for (const url of candidates) {
    try {
      const html = await fetchHtml(url)
      const buildTime = extractBuildTime(html)
      if (buildTime) return buildTime
    } catch {
      // try next
    }
  }
  return ''
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
 * 生产环境检测前端新版本并提示刷新。
 * 开关：VITE_AUTOMATICALLY_DETECT_UPDATE=Y
 *
 * - 切回页签：visibilitychange 且 visible 时立刻检测（部署后一回到页面就会弹）
 * - 3 分钟定时器兜底：一直停在前台不切走时也会定期检
 *
 * 正确测法：保留旧标签页不刷新 → 部署新包 → 切到别的标签再切回来。
 * 控制台可执行：window.__checkAppVersion()
 */
export function setupAppVersionNotification() {
  const enabled = normalizeFlag(import.meta.env.VITE_AUTOMATICALLY_DETECT_UPDATE) === 'Y'
  if (!import.meta.env.PROD || !enabled) {
    console.info('[appVersion] skipped', {
      prod: import.meta.env.PROD,
      flag: import.meta.env.VITE_AUTOMATICALLY_DETECT_UPDATE
    })
    return
  }

  const localBuildTime = typeof __APP_BUILD_TIME__ === 'string' ? __APP_BUILD_TIME__ : ''
  if (!localBuildTime) {
    console.warn('[appVersion] local buildTime missing')
    return
  }

  let checking = false
  let notified = false

  const checkForUpdates = async (reason = 'poll') => {
    if (checking || notified || document.visibilityState !== 'visible') return
    checking = true
    try {
      const remoteBuildTime = await fetchRemoteBuildTime()
      console.info('[appVersion] check', { reason, localBuildTime, remoteBuildTime })
      if (!remoteBuildTime) {
        console.warn('[appVersion] remote buildTime missing，请确认已部署带 meta 的 index.html，且未被强缓存')
        return
      }
      if (remoteBuildTime !== localBuildTime) {
        notified = true
        showUpdateNotification()
      }
    } catch (error) {
      console.warn('[appVersion] check failed', error)
    } finally {
      checking = false
    }
  }

  ;(window as any).__checkAppVersion = () => {
    notified = false
    return checkForUpdates('manual')
  }

  void checkForUpdates('startup')

  // 兜底：一直停在前台时每 3 分钟检一次
  window.setInterval(() => {
    void checkForUpdates('interval')
  }, CHECK_INTERVAL_MS)

  // 切回页签立刻检，不等定时器
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      void checkForUpdates('visible')
    }
  })
}
