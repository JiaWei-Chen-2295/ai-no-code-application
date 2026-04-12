<template>
  <div id="appDetailPage">
    <div class="app-content" v-if="app">
      <a-row :gutter="24">
        <a-col :span="16">
          <a-card title="应用信息" class="info-card">
            <a-descriptions :column="2">
              <a-descriptions-item label="应用名称">{{ app.appName }}</a-descriptions-item>
              <a-descriptions-item label="代码生成类型">{{ app.codeGenType }}</a-descriptions-item>
              <a-descriptions-item label="优先级">{{ app.priority }}</a-descriptions-item>
              <a-descriptions-item label="创建时间">{{ formatTime(app.createTime) }}</a-descriptions-item>
              <a-descriptions-item label="初始提示" :span="2">{{ app.initPrompt }}</a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>

        <a-col :span="8">
          <a-card title="操作" class="action-card">
            <div class="actions">
              <a-button type="primary" block style="margin-bottom: 12px" @click="startChat">
                <Icon icon="mdi:chat-processing-outline" class="action-icon" />
                开始聊天
              </a-button>
              <a-button type="primary" block style="margin-bottom: 12px" @click="editApp">
                <Icon icon="mdi:pencil-outline" class="action-icon" />
                编辑应用
              </a-button>
              <a-button block style="margin-bottom: 12px" @click="previewApp">
                <Icon icon="mdi:play-circle-outline" class="action-icon" />
                预览应用
              </a-button>
              <a-button danger block @click="deleteApp">
                <Icon icon="mdi:delete-outline" class="action-icon" />
                删除应用
              </a-button>
            </div>
          </a-card>

          <a-card title="应用图标" style="margin-top: 16px">
            <div class="app-icon-display">
              <div class="app-icon">📱</div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <div v-else class="loading-state">
      <a-spin size="large" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { Icon } from '@iconify/vue'
import { getAppVoById, deleteMyApp as deleteAppApi } from '@/api/appController'

const router = useRouter()
const route = useRoute()
const app = ref<API.AppVO>()

const startChat = () => {
  if (!app.value?.id) return
  router.push(`/app/chat/${app.value.id}`)
}

const loadApp = async () => {
  const id = route.params.id as string
  if (!id) return

  try {
    const res = await getAppVoById({ id: id })
    if (res.data.code === 0) {
      app.value = res.data.data
    } else {
      message.error('加载应用信息失败：' + res.data.message)
    }
  } catch (error) {
    message.error('加载应用信息失败')
  }
}

const editApp = () => {
  message.info('编辑功能开发中...')
}

const previewApp = () => {
  message.info('预览功能开发中...')
}

const deleteApp = () => {
  if (!app.value) return

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用"${app.value.appName}"吗？此操作不可恢复。`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteAppApi({ deleteId: app.value!.id! })
        if (res.data.code === 0) {
          message.success('删除成功')
          router.push('/apps')
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadApp()
})
</script>

<style scoped>
#appDetailPage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-4);
}

/* 覆盖ant-design卡片样式 */
#appDetailPage :deep(.ant-card) {
  border: 2px solid var(--secondary-600);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: var(--transition-all);
  overflow: hidden;
  position: relative;
}

#appDetailPage :deep(.ant-card::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-600), var(--accent-500), var(--secondary-600));
}

#appDetailPage :deep(.ant-card:hover) {
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
  border-color: var(--accent-500);
}

#appDetailPage :deep(.ant-card-head) {
  background: var(--gray-50) !important;
  border-bottom: 2px solid var(--secondary-200) !important;
}

#appDetailPage :deep(.ant-card-head-title) {
  color: var(--deep-600) !important;
  font-size: var(--text-lg) !important;
  font-weight: var(--font-semibold) !important;
}

#appDetailPage :deep(.ant-card-body) {
  padding: var(--spacing-6) !important;
}

#appDetailPage :deep(.ant-descriptions-item-label) {
  color: var(--gray-700) !important;
  font-weight: var(--font-medium) !important;
}

#appDetailPage :deep(.ant-descriptions-item-content) {
  color: var(--gray-900) !important;
}

.info-card,
.action-card {
  margin-bottom: var(--spacing-4);
}

.actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-3);
}

.action-icon {
  margin-right: var(--spacing-2);
  font-size: 18px;
  vertical-align: middle;
}

/* 覆盖按钮样式 */
.actions :deep(.ant-btn-primary) {
  background: var(--accent-500) !important;
  border-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-md) !important;
  transition: var(--transition-all) !important;
  height: 40px !important;
}

.actions :deep(.ant-btn-primary:hover) {
  background: var(--primary-400) !important;
  border-color: var(--primary-400) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
}

.actions :deep(.ant-btn:not(.ant-btn-primary):not(.ant-btn-dangerous)) {
  background: white !important;
  border: 2px solid var(--secondary-600) !important;
  color: var(--secondary-600) !important;
  font-weight: var(--font-medium) !important;
  border-radius: var(--radius-lg) !important;
  transition: var(--transition-all) !important;
  height: 40px !important;
}

.actions :deep(.ant-btn:not(.ant-btn-primary):not(.ant-btn-dangerous):hover) {
  background: var(--secondary-600) !important;
  color: white !important;
  transform: translateY(-1px) !important;
  box-shadow: var(--shadow-md) !important;
}

.actions :deep(.ant-btn-dangerous) {
  background: white !important;
  border: 2px solid var(--error-500) !important;
  color: var(--error-500) !important;
  font-weight: var(--font-medium) !important;
  border-radius: var(--radius-lg) !important;
  transition: var(--transition-all) !important;
  height: 40px !important;
}

.actions :deep(.ant-btn-dangerous:hover) {
  background: var(--error-500) !important;
  color: white !important;
  transform: translateY(-1px) !important;
  box-shadow: var(--shadow-md) !important;
}

.app-icon-display {
  text-align: center;
  padding: var(--spacing-6);
}

.app-icon {
  font-size: 64px;
  background: linear-gradient(135deg, var(--primary-600) 0%, var(--secondary-600) 100%);
  width: 120px;
  height: 120px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: white;
  box-shadow: var(--shadow-lg);
  transition: var(--transition-transform);
  position: relative;
  overflow: hidden;
}

.app-icon::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255, 220, 0, 0.3), transparent 50%);
}

.app-icon:hover {
  transform: scale(1.05) rotate(5deg);
}

.loading-state {
  text-align: center;
  padding: var(--spacing-16) 0;
}

.loading-state :deep(.ant-spin) {
  color: var(--primary-600) !important;
}

@media (max-width: 768px) {
  #appDetailPage {
    padding: 0 var(--spacing-4);
  }

  .actions {
    gap: var(--spacing-2);
  }

  .app-icon {
    width: 100px;
    height: 100px;
    font-size: 48px;
  }
}
</style>