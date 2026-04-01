import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        // 禁用代码分割，所有代码打包到一个文件
        manualChunks: undefined,
        // 自定义文件名
        entryFileNames: 'assets/index.[hash].js',
        chunkFileNames: 'assets/index.[hash].js',
        assetFileNames: 'assets/index.[hash].[ext]'
      }
    },
    // 禁用CSS代码分割
    cssCodeSplit: false,
    // 跳过压缩，加速构建（AI生成的预览代码不需要压缩）
    minify: false,
    // 设置chunk大小警告限制
    chunkSizeWarningLimit: 1000
  }
})

