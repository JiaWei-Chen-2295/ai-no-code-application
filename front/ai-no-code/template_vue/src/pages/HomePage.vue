<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '@/stores/appStore'
import { message } from 'ant-design-vue'

const appStore = useAppStore()
const count = ref(0)

const increment = () => {
  count.value++
}

// 示例：简单的 API 调用
const testApi = async () => {
  try {
    // 这里演示如何调用 API（不需要复杂封装）
    // const response = await fetch('/api/test')
    // const data = await response.json()
    
    // 临时演示
    message.success('这里可以直接使用 fetch 调用 API，无需复杂封装！')
  } catch (error) {
    message.error('请求失败')
    console.error(error)
  }
}
</script>

<template>
  <div class="home-page">
    <div class="container">
      <h1>{{ appStore.title }}</h1>
      <p class="subtitle">欢迎使用 Vue 3 + TypeScript + Vite 模板</p>
      
      <div class="card">
        <a-space direction="vertical" size="large" style="width: 100%;">
          <a-button type="primary" size="large" @click="increment">
            点击次数: {{ count }}
          </a-button>
          <a-button size="large" @click="testApi">
            测试 API 调用（查看控制台）
          </a-button>
        </a-space>
      </div>

      <div class="features">
        <div class="feature-item">
          <h3>⚡️ Vite</h3>
          <p>下一代前端构建工具</p>
        </div>
        <div class="feature-item">
          <h3>🎯 TypeScript</h3>
          <p>类型安全的 JavaScript</p>
        </div>
        <div class="feature-item">
          <h3>🎨 Ant Design</h3>
          <p>企业级 UI 组件库</p>
        </div>
      </div>

      <div class="links">
        <a-button type="link" href="https://vuejs.org/" target="_blank">
          Vue 文档
        </a-button>
        <a-button type="link" href="https://vitejs.dev/" target="_blank">
          Vite 文档
        </a-button>
        <a-button type="link" @click="$router.push('/about')">
          关于页面
        </a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.container {
  max-width: 800px;
  width: 100%;
  text-align: center;
}

h1 {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 1rem;
  color: #1890ff;
}

.subtitle {
  font-size: 1.25rem;
  color: #666;
  margin-bottom: 2rem;
}

.card {
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.feature-item {
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.feature-item:hover {
  transform: translateY(-4px);
}

.feature-item h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.feature-item p {
  color: #666;
}

.links {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}
</style>

