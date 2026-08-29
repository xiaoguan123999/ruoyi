import { defineConfig, loadEnv, type ProxyOptions } from 'vite'
import path from 'path'
import createVitePlugins, { formatBuildTime } from './vite/plugins'

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd())
  // 去掉 .env 中可能带的引号/空格
  const normalize = (v?: string) => (v || '').trim().replace(/^['"]|['"]$/g, '')
  const VITE_APP_ENV = normalize(env.VITE_APP_ENV)
  const VITE_APP_BASE_API = normalize(env.VITE_APP_BASE_API)
  const VITE_APP_BASE_URL = normalize(env.VITE_APP_BASE_URL)
  const BUILD_TIME = formatBuildTime()

  // 仅开发环境：相对路径走 Vite 代理；测试/生产打包后直接请求后端完整地址
  const proxy: Record<string, string | ProxyOptions> = {}
  if (VITE_APP_BASE_URL && VITE_APP_BASE_API.startsWith('/')) {
    proxy[VITE_APP_BASE_API] = {
      target: VITE_APP_BASE_URL,
      changeOrigin: true,
      // /dev-api/captchaImage -> /captchaImage
      rewrite: (p) => (p.startsWith(VITE_APP_BASE_API) ? p.slice(VITE_APP_BASE_API.length) || '/' : p)
    }
    proxy['^/v3/api-docs/(.*)'] = {
      target: VITE_APP_BASE_URL,
      changeOrigin: true,
    }
  }

  return {
    // 部署生产环境和开发环境下的URL。
    // 默认情况下，vite 会假设你的应用是被部署在一个域名的根路径上
    // 例如 https://www.ruoyi.vip/。如果应用被部署在一个子路径上，你就需要用这个选项指定这个子路径。例如，如果你的应用被部署在 https://www.ruoyi.vip/admin/，则设置 baseUrl 为 /admin/。
    base: VITE_APP_ENV === 'production' ? '/' : '/',
    plugins: createVitePlugins(env, command === 'build', BUILD_TIME),
    define: {
      __APP_BUILD_TIME__: JSON.stringify(BUILD_TIME)
    },
    resolve: {
      // https://cn.vitejs.dev/config/#resolve-alias
      alias: {
        // 设置路径
        '~': path.resolve(__dirname, './'),
        // 设置别名
        '@': path.resolve(__dirname, './src')
      },
      // https://cn.vitejs.dev/config/#resolve-extensions
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    // 打包配置
    build: {
      // https://vite.dev/config/build-options.html
      sourcemap: command === 'build' ? false : 'inline',
      outDir: 'dist',
      assetsDir: 'assets',
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]'
        }
      }
    },
    // vite 相关配置
    server: {
      port: 80,
      host: true,
      open: true,
      proxy
    },
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule: any) => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              }
            }
          }
        ]
      }
    }
  }
})
