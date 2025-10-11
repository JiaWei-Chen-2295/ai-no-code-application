<script setup lang="ts">
import { UserOutlined } from '@ant-design/icons-vue';
import { ref } from 'vue';
import type { MenuProps } from 'ant-design-vue';
import IconWrapper from '@/components/shared/IconWrapper.vue';

// Define menu items with default configuration
const items: MenuProps['items'] = [
  {
    key: 'dashboard',
    label: 'Dashboard',
  },
  {
    key: 'apps',
    label: 'Apps',
  },
  {
    key: 'settings',
    label: 'Settings',
  },
];

const collapsed = ref(false);
</script>

<template>
  <div class="global-header"
    style="display: flex; align-items: center; justify-content: space-between; padding: 0 20px;">
    <div class="logo-section" style="display: flex; align-items: center;">
      <img src="/logo.png" alt="Logo" style="height: 32px; margin-right: 12px;" />
      <span class="site-title" style="font-size: 18px; font-weight: bold; color: #fff;">AppCraft</span>
    </div>

    <div class="menu-section">
      <a-menu v-model:collapsed="collapsed" :items="items" mode="horizontal" theme="dark"
        :style="{ flex: 1, minWidth: 0 }" />
    </div>

    <div class="user-section" style="display: flex; align-items: center;">
      <a-button type="primary">
        <IconWrapper :size="16" :border-radius="'50%'" :background-color="'#fff'">
          <UserOutlined style="color: #1890ff;" />
        </IconWrapper>
        Login
      </a-button>
    </div>
  </div>
</template>

<style scoped>
.global-header {
  height: 64px;
  background: var(--primary-600);
  box-shadow: var(--shadow-lg);
  border-bottom: 3px solid var(--accent-500);
  position: relative;
  overflow: hidden;
}

.global-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(255, 220, 0, 0.1) 50%, transparent 100%);
  pointer-events: none;
}

.logo-section {
  flex: 0 0 auto;
  z-index: 2;
  position: relative;
}

.logo-section img {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  transition: var(--transition-transform);
}

.logo-section img:hover {
  transform: scale(1.05);
}

.site-title {
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  transition: var(--transition-colors);
}

.site-title:hover {
  color: var(--accent-400) !important;
}

.menu-section {
  flex: 1;
  min-width: 0;
  margin: 0 var(--spacing-8);
  z-index: 2;
  position: relative;
}

/* 覆盖ant-design菜单样式 */
.menu-section :deep(.ant-menu) {
  background: transparent !important;
  border-bottom: none !important;
}

.menu-section :deep(.ant-menu-item) {
  color: rgba(255, 255, 255, 0.9) !important;
  border-radius: var(--radius-md);
  margin: 0 var(--spacing-1);
  transition: var(--transition-all);
  font-weight: var(--font-medium);
}

.menu-section :deep(.ant-menu-item:hover) {
  background-color: var(--secondary-600) !important;
  color: white !important;
  transform: translateY(-1px);
}

.menu-section :deep(.ant-menu-item-selected) {
  background-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold);
}

.user-section {
  flex: 0 0 auto;
  z-index: 2;
  position: relative;
}

/* 覆盖登录按钮样式 */
.user-section :deep(.ant-btn-primary) {
  background: var(--accent-500) !important;
  border-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-md) !important;
  transition: var(--transition-all) !important;
  display: flex !important;
  align-items: center !important;
  gap: var(--spacing-2) !important;
}

.user-section :deep(.ant-btn-primary:hover) {
  background: white !important;
  border-color: white !important;
  color: var(--primary-600) !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
}

/* 图标包装器样式 */
.user-section :deep(.icon-wrapper) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: white;
  transition: var(--transition-all);
}

.user-section :deep(.ant-btn-primary:hover .icon-wrapper) {
  background-color: var(--primary-600);
}

.user-section :deep(.ant-btn-primary:hover .anticon) {
  color: white !important;
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 var(--spacing-4) !important;
  }

  .menu-section {
    margin: 0 var(--spacing-4);
  }

  .site-title {
    font-size: var(--text-base) !important;
  }
}
</style>