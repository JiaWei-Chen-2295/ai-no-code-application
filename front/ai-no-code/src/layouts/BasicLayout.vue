<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <div class="logo">
          <h2>AppCraft</h2>
        </div>
        <div class="nav">
          <a-menu theme="dark" mode="horizontal" :selected-keys="selectedKeys" style="line-height: 64px">
            <a-menu-item key="home">
              <RouterLink to="/">首页</RouterLink>
            </a-menu-item>
            <a-menu-item key="apps">
              <RouterLink to="/apps">我的应用</RouterLink>
            </a-menu-item>
            <a-menu-item key="featured">
              <RouterLink to="/featured">精选案例</RouterLink>
            </a-menu-item>
            <a-menu-item v-if="isAdmin" key="admin-apps">
              <RouterLink to="/admin/apps">应用管理</RouterLink>
            </a-menu-item>
            <a-menu-item v-if="isAdmin" key="admin-chat-history">
              <RouterLink to="/admin/chat-history">聊天记录</RouterLink>
            </a-menu-item>
          </a-menu>
        </div>
        <div class="user-info">
          <a-dropdown>
            <a class="ant-dropdown-link" @click.prevent>
              {{ userStore.loginUser?.userName || '用户中心' }}
              <DownOutlined />
            </a>
            <template #overlay>
              <a-menu v-if="!userStore.loginUser">
                <a-menu-item>
                  <RouterLink to="/user/login">登录</RouterLink>
                </a-menu-item>
                <a-menu-item>
                  <RouterLink to="/user/register">注册</RouterLink>
                </a-menu-item>
              </a-menu>
              <a-menu v-else>
                <a-menu-item>
                  <span>{{ userStore.loginUser.userName }}</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item>
                  <a @click="handleLogout">退出登录</a>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <div class="footer-content">
          AppCraft ©2024 Created by JavierChen
        </div>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { DownOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKeys = computed(() => {
  const path = route.path
  if (path === '/') return ['home']
  if (path === '/apps') return ['apps']
  if (path === '/featured') return ['featured']
  if (path.startsWith('/admin/apps')) return ['admin-apps']
  if (path.startsWith('/admin/chat-history')) return ['admin-chat-history']
  return []
})

const isAdmin = computed(() => {
  return userStore.loginUser?.userRole === 'admin'
})

const handleLogout = () => {
  // 清除用户信息
  userStore.clearLoginUser()
  message.success('退出登录成功')
  router.push('/user/login')
}

onMounted(() => {
  // 页面加载时尝试获取用户信息
  userStore.fetchLoginUser()
})
</script>

<style scoped>
#basicLayout {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-6);
  background: var(--primary-600);
  box-shadow: var(--shadow-sm);
  border-bottom: 3px solid var(--accent-500);
}

.logo h2 {
  color: white;
  margin: 0;
  font-weight: var(--font-bold);
  font-size: var(--text-xl);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.logo h2:hover {
  color: var(--accent-400);
  transition: var(--transition-colors);
}

.nav {
  flex: 1;
  margin-left: var(--spacing-10);
}

/* 覆盖ant-design菜单样式 */
.nav :deep(.ant-menu) {
  background: transparent !important;
  border-bottom: none !important;
}

.nav :deep(.ant-menu-item) {
  color: rgba(255, 255, 255, 0.9) !important;
  border-radius: var(--radius-md);
  margin: 0 var(--spacing-1);
  transition: var(--transition-all);
}

.nav :deep(.ant-menu-item:hover) {
  background-color: var(--secondary-600) !important;
  color: white !important;
}

.nav :deep(.ant-menu-item-selected) {
  background-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold);
}

.nav :deep(.ant-menu-item a) {
  color: inherit !important;
  text-decoration: none;
}

.user-info {
  color: white;
}

.user-info .ant-dropdown-link {
  color: white;
  text-decoration: none;
  padding: var(--spacing-2) var(--spacing-4);
  border-radius: var(--radius-md);
  transition: var(--transition-all);
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}

.user-info .ant-dropdown-link:hover {
  background-color: var(--secondary-600);
  color: var(--accent-400);
}

.content {
  padding: var(--spacing-6);
  background: linear-gradient(135deg, var(--gray-50) 0%, #f5f7fa 100%);
  min-height: calc(100vh - 64px - 70px);
}

.footer {
  text-align: center;
  background: var(--gray-700);
  color: white;
  padding: var(--spacing-4);
}

.footer-content {
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--text-sm);
}
</style>