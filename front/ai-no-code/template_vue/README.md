# Vue 3 MVP 快速开发模板

一个**极简精简**的 Vue 3 项目模板，专为 AI 快速 MVP 开发而设计。

## 🎯 设计理念

- ✨ **极简架构**：移除复杂的 API 层和工具函数
- 🚀 **快速开发**：专注于核心功能，减少模板代码
- 🤖 **AI 友好**：更少的模块，更容易理解和扩展
- 📦 **开箱即用**：保留必需的核心功能

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - JavaScript 的超集，提供类型安全
- **Vite** - 下一代前端构建工具
- **Vue Router** - Vue.js 官方路由
- **Pinia** - Vue 状态管理
- **Ant Design Vue** - 企业级 UI 组件库

## 项目结构（精简版）

```
template_vue/
├── src/
│   ├── pages/            # 页面组件（只需关注这里）
│   ├── components/       # 可复用组件（可选）
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   ├── styles/           # 全局样式
│   ├── types/            # 类型定义（精简）
│   ├── utils/            # 工具函数（精简）
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── public/               # 静态资源
├── index.html            # HTML 模板
├── vite.config.ts        # Vite 配置
├── tsconfig.json         # TypeScript 配置
└── package.json          # 项目依赖

```

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
# 访问 http://localhost:5173
```

### 其他命令

```bash
npm run build        # 构建生产版本
npm run preview      # 预览生产构建
npm run type-check   # TypeScript 类型检查
npm run lint         # 代码检查
npm run format       # 代码格式化
```

## ✨ 特性

- ⚡️ 使用 Vite 实现快速的热模块替换（HMR）
- 🎯 TypeScript 支持
- 🎨 Ant Design Vue UI 组件
- 🔥 使用 `<script setup>` 语法
- 📊 Pinia 状态管理
- 🎭 开发工具集成
- 🤖 **AI 友好的精简架构**

## 🆚 与完整版的区别

**已移除（简化 MVP 开发）：**
- ❌ 复杂的 API 封装层（`src/api/`）
- ❌ 复杂的 HTTP 请求拦截器
- ❌ 大量的工具函数（防抖、节流等）
- ❌ 复杂的类型定义
- ❌ Axios 依赖

**保留核心功能：**
- ✅ Vue 3 + TypeScript 基础
- ✅ 页面路由
- ✅ 状态管理
- ✅ UI 组件库
- ✅ 基础样式

## 💡 AI 开发指南

### 1. 创建新页面（最常用）

在 `src/pages/` 创建新的 `.vue` 文件：

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'

const data = ref([])
const loading = ref(false)

// 直接使用 fetch 调用 API
const fetchData = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/data')
    if (!response.ok) throw new Error('请求失败')
    data.value = await response.json()
    message.success('获取成功')
  } catch (error) {
    message.error('获取失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <a-button type="primary" @click="fetchData" :loading="loading">
      获取数据
    </a-button>
    <a-list :data-source="data" />
  </div>
</template>

<style scoped>
.page {
  padding: 24px;
}
</style>
```

### 2. 添加路由

在 `src/router/index.ts` 添加路由：

```typescript
{
  path: '/your-page',
  name: 'YourPage',
  component: () => import('@/pages/YourPage.vue'),
}
```

### 3. API 调用示例

**GET 请求：**
```typescript
const response = await fetch('/api/users')
const users = await response.json()
```

**POST 请求：**
```typescript
const response = await fetch('/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ name: 'John' })
})
const result = await response.json()
```

**带认证：**
```typescript
const token = localStorage.getItem('token')
const response = await fetch('/api/protected', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
```

### 4. 使用 Pinia 状态管理（按需）

在 `src/stores/` 创建 store：

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMyStore = defineStore('my', () => {
  const count = ref(0)
  
  function increment() {
    count.value++
  }
  
  return { count, increment }
})
```

### 5. MVP 开发原则

- ✅ **直接调用 API**：使用 `fetch()` 而非复杂的 HTTP 封装
- ✅ **直接写逻辑**：不需要提前设计复杂架构
- ✅ **用到再加**：工具函数按需添加，不提前准备
- ✅ **组件优先**：直接使用 Ant Design Vue 组件
- ✅ **简单最好**：能用简单方式就不用复杂方式

## ⚙️ 配置说明

### API 代理配置

在 `vite.config.ts` 中配置后端 API 代理：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true,
    }
  }
}
```

### 修改端口

```typescript
server: {
  port: 3000, // 自定义端口
}
```

## License

MIT

