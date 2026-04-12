import { defineConfig } from '@tarojs/cli'

export default defineConfig({
  framework: 'react',
  projectName: 'ai-no-code-mini-program',
  date: '2026-04-01',
  designWidth: 750,
  deviceRatio: {
    375: 2,
    640: 2.34,
    750: 1,
    828: 1.81
  },
  sourceRoot: 'src',
  outputRoot: `dist/${process.env.TARO_ENV || 'weapp'}`,
  plugins: [
    ['@tarojs/plugin-platform-weapp'],
    ['@tarojs/plugin-framework-react']
  ],
  defineConstants: {
    VITE_API_BASE_URL: JSON.stringify(process.env.VITE_API_BASE_URL || 'http://localhost:8081'),
    VITE_APP_NAME: JSON.stringify(process.env.VITE_APP_NAME || 'AI No-Code Mini Program'),
  },
  mini: {
    compile: {
      include: [],
    },
    webpackChain: (chain) => {
      // 禁用 Skyline，强制使用 WebView 渲染
    },
    postcss: {
      pxtransform: {
        enable: true,
        config: {}
      },
      url: {
        enable: true,
        config: {
          limit: 1024
        }
      },
      cssModules: {
        enable: false,
        config: {}
      }
    }
  },
  h5: {
    publicPath: '/',
    staticDirectory: 'static',
    postcss: {
      autoprefixer: {
        enable: true,
        config: {}
      },
      cssModules: {
        enable: false,
        config: {}
      }
    }
  }
})
