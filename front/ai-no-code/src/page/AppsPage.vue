<template>
  <div class="apps-content">
    <div class="content-header">
      <h2 class="page-title">
        <Icon icon="mdi:folder-outline" class="title-icon" />
        <span>我的应用</span>
      </h2>
      <a-button type="primary" @click="createApp">
        <Icon icon="mdi:plus" class="btn-icon" />
        <span>创建新应用</span>
      </a-button>
    </div>

    <a-row :gutter="[24, 24]">
      <a-col :xs="24" :sm="12" :lg="8" :xl="6" v-for="app in apps" :key="app.id">
        <AppCard :app="app" @view="viewApp" @edit="editApp" @delete="deleteApp" />
      </a-col>
    </a-row>

    <div v-if="apps.length === 0" class="empty-state">
      <a-empty description="暂无应用">
        <a-button type="primary" @click="createApp">创建第一个应用</a-button>
      </a-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { Icon } from '@iconify/vue'
import { listMyAppVoByPage, deleteMyApp as deleteAppApi } from '@/api/appController'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const apps = ref<API.AppVO[]>([])

const loadApps = async () => {
  try {
    const res = await listMyAppVoByPage({
      current: 1,
      pageSize: 20,
    })
    if (res.data.code === 0) {
      apps.value = res.data.data?.records || []
    }
  } catch (error) {
    message.error('加载应用列表失败')
  }
}

const createApp = () => {
  router.push('/app/create')
}

const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

const viewApp = (app: API.AppVO) => {
  router.push(`/app/detail/${app.id}`)
}

const deleteApp = (app: API.AppVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用"${app.appName}"吗？此操作不可恢复。`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteAppApi({ deleteId: app.id! })
        if (res.data.code === 0) {
          message.success('删除成功')
          loadApps()
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

onMounted(() => {
  loadApps()
})
</script>

<style scoped>
.apps-content {
  min-height: 400px;
}

.content-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: var(--spacing-6);
}

/* 覆盖主要按钮样式 */
.content-header :deep(.ant-btn-primary) {
  background: var(--accent-500) !important;
  border-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-md) !important;
  transition: var(--transition-all) !important;
}

.content-header :deep(.ant-btn-primary:hover) {
  background: var(--primary-400) !important;
  border-color: var(--primary-400) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
}

/* 应用卡片样式 */
.apps-content :deep(.ant-card) {
  height: 100%;
  border: 2px solid var(--secondary-600);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: var(--transition-all);
  overflow: hidden;
  position: relative;
}

.apps-content :deep(.ant-card::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-600), var(--accent-500), var(--secondary-600));
}

.apps-content :deep(.ant-card:hover) {
  transform: translateY(-8px);
  box-shadow: var(--shadow-2xl);
  border-color: var(--accent-500);
}

.apps-content :deep(.ant-card-body) {
  padding: var(--spacing-4);
}

.apps-content :deep(.ant-card-meta-title) {
  color: var(--deep-600) !important;
  font-size: var(--text-lg) !important;
  font-weight: var(--font-semibold) !important;
  margin-bottom: var(--spacing-2) !important;
}

.apps-content :deep(.ant-card-meta-description) {
  color: var(--gray-700) !important;
  font-size: var(--text-sm) !important;
  line-height: var(--leading-relaxed) !important;
}

.apps-content :deep(.ant-card-actions) {
  background: var(--gray-50) !important;
  border-top: 1px solid var(--secondary-200) !important;
}

.apps-content :deep(.ant-card-actions .ant-btn-link) {
  color: var(--secondary-600) !important;
  font-weight: var(--font-medium) !important;
  transition: var(--transition-colors) !important;
}

.apps-content :deep(.ant-card-actions .ant-btn-link:hover) {
  color: var(--primary-600) !important;
}

.apps-content :deep(.ant-card-actions .ant-btn-link[danger]:hover) {
  color: var(--error-600) !important;
}

.empty-state {
  text-align: center;
  padding: var(--spacing-16) 0;
}

/* 覆盖空状态样式 */
.empty-state :deep(.ant-empty) {
  color: var(--gray-600) !important;
}

.empty-state :deep(.ant-empty-description) {
  color: var(--gray-600) !important;
  font-size: var(--text-base) !important;
}

.empty-state :deep(.ant-btn-primary) {
  background: var(--accent-500) !important;
  border-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-md) !important;
  margin-top: var(--spacing-4) !important;
}

.empty-state :deep(.ant-btn-primary:hover) {
  background: var(--primary-400) !important;
  border-color: var(--primary-400) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
}

@media (max-width: 768px) {
  .apps-content {
    padding: 0 var(--spacing-4);
  }

  .content-header {
    margin-bottom: var(--spacing-4);
  }

  .empty-state {
    padding: var(--spacing-12) 0;
  }
}
</style>