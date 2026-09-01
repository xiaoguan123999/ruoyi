import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import type { Lang } from './data'

const copy = {
  zh: {
    brand: '星帆智联',
    store: '应用商店',
    slogan: '连接星空 · 智联未来',
    subSlogan: '以科技连接万物 · 让星辰触手可及',
    search: '搜索应用',
    all: '全部',
    empty: '没有找到相关应用',
    back: '返回',
    get: '获取',
    downloadTitle: 'APP 下载',
    androidGet: 'Android 版本下载',
    iosGet: 'iOS 版本下载',
    webGet: '打开网页版',
    noticeTag: '公告',
    notice: '点获取即可下载 iOS、Android 或打开网页版',
  },
  en: {
    brand: 'StarSail',
    store: 'App Store',
    slogan: 'Connect the stars · Link the future',
    subSlogan: 'Technology that brings the stars within reach',
    search: 'Search apps',
    all: 'All',
    empty: 'No apps found',
    back: 'Back',
    get: 'Get',
    downloadTitle: 'Download',
    androidGet: 'Download for Android',
    iosGet: 'Download for iOS',
    webGet: 'Open web version',
    noticeTag: 'News',
    notice: 'Tap Get for iOS, Android, or web',
  },
}

interface Ctx {
  lang: Lang
  t: (typeof copy)['zh']
  toggleLang: () => void
}

const UI = createContext<Ctx | null>(null)

export function useUI() {
  const ctx = useContext(UI)
  if (!ctx) throw new Error('useUI')
  return ctx
}

function read(key: string, fallback: string) {
  try {
    return localStorage.getItem(key) || fallback
  } catch {
    return fallback
  }
}

export function UIProvider({ children }: { children: ReactNode }) {
  const [lang, setLang] = useState<Lang>(() => (read('sf-lang', 'zh') === 'en' ? 'en' : 'zh'))

  useEffect(() => {
    document.documentElement.lang = lang === 'zh' ? 'zh-CN' : 'en'
    localStorage.setItem('sf-lang', lang)
  }, [lang])

  const value = useMemo<Ctx>(
    () => ({
      lang,
      t: copy[lang],
      toggleLang: () => setLang((v) => (v === 'zh' ? 'en' : 'zh')),
    }),
    [lang],
  )

  return <UI.Provider value={value}>{children}</UI.Provider>
}
