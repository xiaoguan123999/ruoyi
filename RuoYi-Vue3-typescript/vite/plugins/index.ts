import vue from '@vitejs/plugin-vue'

import createAutoImport from './auto-import'
import createSvgIcon from './svg-icon'
import createCompression from './compression'
import createSetupExtend from './setup-extend'
import createBuildTime, { formatBuildTime } from './build-time'
import { PluginOption } from 'vite'

export { formatBuildTime }

export default function createVitePlugins(
  viteEnv: Record<string, string>,
  isBuild = false,
  buildTime = formatBuildTime()
) {
  const vitePlugins: PluginOption[] = [vue()]
  vitePlugins.push(createAutoImport())
  vitePlugins.push(createSetupExtend())
  vitePlugins.push(createSvgIcon(isBuild))
  vitePlugins.push(createBuildTime(buildTime))
  isBuild && vitePlugins.push(...createCompression(viteEnv))
  return vitePlugins
}
