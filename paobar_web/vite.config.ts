import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import path from 'path'

export default defineConfig({
  plugins: [
    vue({
      template: {
        // 吉他谱渲染保留 yopu 原版的 xhe-* / hexi-chord 自定义标签，
        // 这里告诉 Vue 编译器不要把它们当作组件去解析/报错。
        compilerOptions: {
          isCustomElement: (tag) => tag.startsWith('xhe-') || tag === 'hexi-chord',
        },
      },
    }),
    // PWA：让用户可以"添加到主屏幕"，在 standalone 模式下完全看不到浏览器 UI
    VitePWA({
      registerType: 'autoUpdate',
      // 首次访问就注入 SW；SW 更新后页面立即切到新版本
      includeAssets: ['icon.svg', 'icon-maskable.svg', 'apple-touch-icon.svg'],
      manifest: {
        name: '泡吧吉他谱',
        short_name: '泡吧',
        description: '吉他和弦谱在线查看 · 支持级数/和弦图切换',
        lang: 'zh-CN',
        dir: 'ltr',
        start_url: '.',
        scope: '.',
        display: 'standalone',
        orientation: 'portrait',
        theme_color: '#0a0a0a',
        background_color: '#0a0a0a',
        icons: [
          {
            src: 'icon.svg',
            sizes: '192x192 512x512 any',
            type: 'image/svg+xml',
            purpose: 'any',
          },
          {
            src: 'icon-maskable.svg',
            sizes: '192x192 512x512 any',
            type: 'image/svg+xml',
            purpose: 'maskable',
          },
        ],
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,svg,png,ico,webmanifest}'],
        // 后端接口不走 SW 缓存，避免登录态/实时数据失效
        navigateFallbackDenylist: [/^\/api\//],
        // 给较大的 chord SVG 资源放宽上限
        maximumFileSizeToCacheInBytes: 4 * 1024 * 1024,
      },
      devOptions: {
        // 开发模式下也启用 SW，方便本地调试"添加到主屏幕"
        enabled: false,
      },
    }),
  ],
  base: './',
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    rollupOptions: {
      output: {
        manualChunks: undefined
      }
    }
  }
})
