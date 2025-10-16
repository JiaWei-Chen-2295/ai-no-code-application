<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <div class="logo" @click="router.push('/')">
          <Icon icon="mdi:application-brackets" class="logo-icon" />
          <h2>AppCraft</h2>
        </div>
        <div class="nav">
          <a-menu theme="dark" mode="horizontal" :selected-keys="selectedKeys" style="line-height: 64px">
            <a-menu-item key="home">
              <RouterLink to="/" class="nav-link">
                <Icon icon="mdi:home-outline" class="nav-icon" />
                <span>首页</span>
              </RouterLink>
            </a-menu-item>
            <a-menu-item key="apps">
              <RouterLink to="/apps" class="nav-link">
                <Icon icon="mdi:folder-outline" class="nav-icon" />
                <span>我的应用</span>
              </RouterLink>
            </a-menu-item>
            <a-menu-item key="featured">
              <RouterLink to="/featured" class="nav-link">
                <Icon icon="mdi:star-outline" class="nav-icon" />
                <span>精选案例</span>
              </RouterLink>
            </a-menu-item>
            <a-menu-item v-if="isAdmin" key="admin-apps">
              <RouterLink to="/admin/apps" class="nav-link">
                <Icon icon="mdi:cog-outline" class="nav-icon" />
                <span>应用管理</span>
              </RouterLink>
            </a-menu-item>
            <a-menu-item v-if="isAdmin" key="admin-chat-history">
              <RouterLink to="/admin/chat-history" class="nav-link">
                <Icon icon="mdi:message-text-outline" class="nav-icon" />
                <span>聊天记录</span>
              </RouterLink>
            </a-menu-item>
          </a-menu>
        </div>
        <div class="user-info">
          <a-dropdown>
            <a class="ant-dropdown-link user-avatar" @click.prevent>
              <Icon icon="mdi:account-circle-outline" class="user-icon" />
              <span>{{ userStore.loginUser?.userName || '用户中心' }}</span>
              <Icon icon="mdi:chevron-down" class="dropdown-icon" />
            </a>
            <template #overlay>
              <a-menu v-if="!userStore.loginUser" class="user-dropdown">
                <a-menu-item>
                  <RouterLink to="/user/login">
                    <Icon icon="mdi:login" class="menu-icon" />
                    <span>登录</span>
                  </RouterLink>
                </a-menu-item>
                <a-menu-item>
                  <RouterLink to="/user/register">
                    <Icon icon="mdi:account-plus-outline" class="menu-icon" />
                    <span>注册</span>
                  </RouterLink>
                </a-menu-item>
              </a-menu>
              <a-menu v-else class="user-dropdown">
                <a-menu-item disabled>
                  <Icon icon="mdi:account" class="menu-icon" />
                  <span>{{ userStore.loginUser.userName }}</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item>
                  <a @click="handleLogout">
                    <Icon icon="mdi:logout" class="menu-icon" />
                    <span>退出登录</span>
                  </a>
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
import { message } from 'ant-design-vue'
import { Icon } from '@iconify/vue'
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

/* 简约风格导航栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-8);
  background: white;
  box-shadow: 0 1px 0 0 rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid var(--gray-200);
  height: 64px;
}

/* Logo 简约设计 */
.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  cursor: pointer;
  user-select: none;
}

.logo-icon {
  font-size: 28px;
  color: var(--primary-600);
  transition: var(--transition-transform);
}

.logo:hover .logo-icon {
  transform: rotate(-5deg) scale(1.05);
}

.logo h2 {
  color: var(--gray-900);
  margin: 0;
  font-weight: var(--font-bold);
  font-size: var(--text-2xl);
  letter-spacing: -0.5px;
  transition: var(--transition-colors);
}

.logo:hover h2 {
  color: var(--primary-600);
}

/* 导航菜单 */
.nav {
  flex: 1;
  margin-left: var(--spacing-12);
}

/* 覆盖ant-design菜单样式 - 简约风格 */
.nav :deep(.ant-menu) {
  background: transparent !important;
  border-bottom: none !important;
  font-size: var(--text-base);
}

.nav :deep(.ant-menu-item) {
  color: var(--gray-700) !important;
  border-radius: 0 !important;
  margin: 0 var(--spacing-4);
  padding: 0 var(--spacing-2);
  border-bottom: 2px solid transparent;
  transition: var(--transition-colors);
  font-weight: var(--font-medium);
}

.nav :deep(.ant-menu-item:hover) {
  background-color: transparent !important;
  color: var(--primary-600) !important;
  border-bottom-color: var(--primary-200);
}

.nav :deep(.ant-menu-item-selected) {
  background-color: transparent !important;
  color: var(--primary-600) !important;
  border-bottom-color: var(--primary-600);
  font-weight: var(--font-semibold);
}

.nav :deep(.ant-menu-item::after) {
  display: none !important;
}

.nav :deep(.ant-menu-item a) {
  color: inherit !important;
  text-decoration: none;
}

/* 导航链接样式 */
.nav-link {
  display: flex !important;
  align-items: center;
  gap: var(--spacing-2);
}

.nav-icon {
  font-size: 18px;
  opacity: 0.85;
  transition: var(--transition-all);
}

.nav :deep(.ant-menu-item:hover) .nav-icon {
  opacity: 1;
  transform: translateY(-1px);
}

.nav :deep(.ant-menu-item-selected) .nav-icon {
  opacity: 1;
}

/* 用户信息 - 简约设计 */
.user-info {
  color: var(--gray-700);
}

.user-avatar {
  color: var(--gray-700);
  text-decoration: none;
  padding: var(--spacing-2) var(--spacing-4);
  border-radius: var(--radius-lg);
  transition: var(--transition-all);
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-weight: var(--font-medium);
  border: 1px solid transparent;
}

.user-avatar:hover {
  background-color: var(--gray-50);
  color: var(--primary-600);
  border-color: var(--gray-200);
}

.user-icon {
  font-size: 22px;
  opacity: 0.9;
}

.user-avatar:hover .user-icon {
  opacity: 1;
}

.dropdown-icon {
  font-size: 16px;
  opacity: 0.6;
  transition: var(--transition-transform);
}

.user-avatar:hover .dropdown-icon {
  opacity: 1;
  transform: translateY(1px);
}

/* 用户下拉菜单 */
.user-dropdown :deep(.ant-dropdown-menu-item) {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-4);
}

.user-dropdown :deep(.ant-dropdown-menu-item a) {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  color: inherit;
  text-decoration: none;
}

.menu-icon {
  font-size: 18px;
  opacity: 0.8;
}

.user-dropdown :deep(.ant-dropdown-menu-item:hover) .menu-icon {
  opacity: 1;
  color: var(--primary-600);
}

.content {
  padding: var(--spacing-6);
  background: linear-gradient(135deg, var(--gray-50) 0%, #f5f7fa 100%);
  min-height: calc(100vh - 64px - 70px);
}

/* 页脚 - 简约风格 */
.footer {
  text-align: center;
  background: white;
  color: var(--gray-600);
  padding: var(--spacing-6);
  border-top: 1px solid var(--gray-200);
}

.footer-content {
  color: var(--gray-500);
  font-size: var(--text-sm);
  font-weight: var(--font-normal);
}
</style>