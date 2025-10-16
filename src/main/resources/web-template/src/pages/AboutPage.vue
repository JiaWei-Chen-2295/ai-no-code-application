<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<template>
  <div class="about-page">
    <div class="container">
      <h1>关于</h1>
      <div class="content">
        <p>这是一个极简的 Vue 3 项目模板。</p>
        <p>包含以下技术栈：</p>
        <ul>
          <li>Vue 3 - 渐进式 JavaScript 框架</li>
          <li>TypeScript - JavaScript 的超集</li>
          <li>Vite - 下一代前端构建工具</li>
          <li>Vue Router - 官方路由</li>
          <li>Pinia - 状态管理</li>
          <li>Ant Design Vue - UI 组件库</li>
        </ul>
        <a-button type="primary" @click="goBack">
          返回首页
        </a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.container {
  max-width: 600px;
  width: 100%;
}

h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 2rem;
  color: #1890ff;
  text-align: center;
}

.content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.content p {
  font-size: 1.1rem;
  color: #333;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.content ul {
  list-style: none;
  padding: 0;
  margin: 1.5rem 0;
}

.content ul li {
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
  color: #666;
}

.content ul li:last-child {
  border-bottom: none;
}

.content button {
  margin-top: 1.5rem;
}
</style>

