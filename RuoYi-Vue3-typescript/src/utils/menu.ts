import { getNormalPath } from '@/utils/ruoyi'
import { isHttp } from '@/utils/validate'
import usePermissionStore from '@/store/modules/permission'

interface FlatMenu {
  title: string
  path: string
  icon: string
}

function flattenMenus(routes: any[], basePath = ''): FlatMenu[] {
  const list: FlatMenu[] = []
  for (const route of routes || []) {
    if (route.hidden) continue
    const raw = route.path || ''
    const segment = raw.startsWith('/') || isHttp(raw) ? raw : `/${raw}`
    const fullPath = isHttp(raw) ? raw : getNormalPath(basePath + segment)
    const title = route.meta?.title as string | undefined
    const hasChildren = Array.isArray(route.children) && route.children.length > 0

    if (title && !hasChildren && !isHttp(fullPath) && route.redirect !== 'noRedirect') {
      list.push({
        title,
        path: fullPath,
        icon: (route.meta?.icon as string) || ''
      })
    }
    if (hasChildren) {
      list.push(...flattenMenus(route.children, fullPath))
    }
  }
  return list
}

/** 按菜单名称解析当前账号动态路由中的真实路径（上级目录变更后仍可用） */
export function resolveMenuPath(...titles: string[]): string {
  const map = new Map<string, string>()
  flattenMenus(usePermissionStore().defaultRoutes).forEach((item) => {
    if (!map.has(item.title)) map.set(item.title, item.path)
  })
  for (const title of titles) {
    const path = map.get(title)
    if (path) return path
  }
  return ''
}
