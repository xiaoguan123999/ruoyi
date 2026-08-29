import type { Plugin } from 'vite'

/** 格式化为 YYYY-MM-DD HH:mm:ss.SSS，保证同分钟多次打包也能区分 */
export function formatBuildTime(date = new Date()): string {
  const pad = (n: number, len = 2) => String(n).padStart(len, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}.${pad(date.getMilliseconds(), 3)}`
}

/**
 * 构建时把 BUILD_TIME 写入 index.html meta，并 define 到运行时代码。
 */
export default function createBuildTime(buildTime: string): Plugin {
  return {
    name: 'vite-plugin-build-time',
    transformIndexHtml(html) {
      if (html.includes('name="buildTime"')) {
        return html.replace(
          /<meta\s+name="buildTime"\s+content="[^"]*"\s*\/?>/i,
          `<meta name="buildTime" content="${buildTime}">`
        )
      }
      return html.replace(
        /<head>/i,
        `<head>\n  <meta name="buildTime" content="${buildTime}">`
      )
    }
  }
}
