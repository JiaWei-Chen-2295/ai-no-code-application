<template>
  <div id="homePage">
    <!-- 主标题和描述 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="main-title">
          一句话 <span class="ai-icon">🤖</span> 实所想
        </h1>
        <p class="subtitle">与 AI 对话，精工细作每个应用</p>
      </div>
    </div>

    <!-- 提示词输入区域 -->
    <div class="input-section">
      <div class="input-container">
        <a-textarea v-model:value="prompt" :rows="3" placeholder="告诉 AppCraft 你想要什么应用，让我们精心为你打造......"
          class="prompt-input" :maxlength="500" show-count />
        <div class="input-actions">
          <div class="left-actions">
            <a-button class="upload-btn" :icon="h(UploadOutlined)">
              上传
            </a-button>
            <a-button class="optimize-btn" :icon="h(BulbOutlined)">
              优化
            </a-button>
            <a-select v-model:value="codeGenType" class="type-selector" placeholder="选择代码类型">
              <a-select-option value="html">
                <span class="option-icon">📄</span>
                <span>单文件网页</span>
              </a-select-option>
              <a-select-option value="mutiFile">
                <span class="option-icon">📁</span>
                <span>多文件项目</span>
              </a-select-option>
            </a-select>
          </div>
          <a-button type="primary" class="generate-btn" @click="createApp" :loading="creating">
            <template #icon>
              <span class="go-icon">↗</span>
            </template>
          </a-button>
        </div>
      </div>
    </div>

    <!-- 快捷模板按钮 -->
    <div class="quick-templates">
      <a-button v-for="template in templates" :key="template.name" class="template-btn" @click="useTemplate(template)">
        {{ template.name }}
      </a-button>
    </div>

    <!-- 我的作品 -->
    <div class="my-apps-section">
      <div class="section-header">
        <h2 class="section-title">我的作品</h2>
        <a-button type="link" @click="viewAllMyApps">查看全部 →</a-button>
      </div>
      <div class="apps-grid">
        <div v-if="myAppsLoading" class="loading-placeholder">
          <a-spin size="large" />
        </div>
        <div v-else-if="myApps.length === 0" class="empty-placeholder">
          <a-empty description="还没有创建任何应用">
            <a-button type="primary" @click="scrollToInput">立即创建</a-button>
          </a-empty>
        </div>
        <a-row v-else :gutter="[16, 16]">
          <a-col v-for="app in myApps.slice(0, 3)" :key="app.id" :xs="24" :sm="12" :lg="8">
            <app-card :app="app" @view="viewApp" @edit="editApp" @delete="deleteApp" />
          </a-col>
        </a-row>
      </div>
    </div>

    <!-- 精选案例 -->
    <div class="featured-apps-section">
      <div class="section-header">
        <h2 class="section-title">精选案例</h2>
        <a-button type="link" @click="viewAllFeaturedApps">查看更多 →</a-button>
      </div>
      <div class="apps-grid">
        <div v-if="featuredAppsLoading" class="loading-placeholder">
          <a-spin size="large" />
        </div>
        <a-row v-else :gutter="[16, 16]">
          <a-col v-for="app in featuredApps.slice(0, 6)" :key="app.id" :xs="24" :sm="12" :lg="8">
            <app-card :app="app" @view="viewApp" featured />
          </a-col>
        </a-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { UploadOutlined, BulbOutlined } from '@ant-design/icons-vue'
import {
  addApp,
  listMyAppVoByPage,
  listFeaturedAppVoByPage,
  deleteMyApp as deleteAppApi
} from '@/api/appController'
import { useUserStore } from '@/stores/userStore'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const prompt = ref('')
const codeGenType = ref<'html' | 'mutiFile'>('html') // 默认为单文件网页
const creating = ref(false)
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const myAppsLoading = ref(false)
const featuredAppsLoading = ref(false)

// 快捷模板
const templates = ref([
  { name: '设备风电商品页面', prompt: '用 AppCraft 精心设计一个设备风电商品展示页面，包含产品图片、参数介绍、价格信息和购买按钮' },
  { name: '企业网站', prompt: '用 AppCraft 打造一个现代化的企业官网，包含首页、关于我们、产品服务、联系我们等页面' },
  { name: '电商运营后台', prompt: '用 AppCraft 构建一个电商运营管理后台，包含订单管理、商品管理、用户管理、数据统计等功能' },
  { name: '暗黑试题社区', prompt: '用 AppCraft 创建一个暗黑主题的在线试题社区，支持用户发布题目、答题、讨论交流' },
])

// 创建应用
const createApp = async () => {
  if (!prompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!userStore.loginUser) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      appName: `AppCraft应用-${Date.now()}`,
      initPrompt: prompt.value.trim(),
      codeGenType: codeGenType.value
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功！')
      // 跳转到对话页面
      router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch {
    message.error('创建失败，请稍后重试')
  } finally {
    creating.value = false
  }
}

// 使用模板
const useTemplate = (template: { name: string; prompt: string }) => {
  prompt.value = template.prompt
}

// 滚动到输入框
const scrollToInput = () => {
  const inputSection = document.querySelector('.input-section')
  inputSection?.scrollIntoView({ behavior: 'smooth' })
}

// 查看应用
const viewApp = (app: API.AppVO) => {
  router.push(`/app/chat/${app.id}`)
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 删除应用
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
          loadMyApps()
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch {
        message.error('删除失败')
      }
    },
  })
}

// 查看所有我的应用
const viewAllMyApps = () => {
  router.push('/apps')
}

// 查看所有精选应用
const viewAllFeaturedApps = () => {
  router.push('/featured')
}

// 加载我的应用
const loadMyApps = async () => {
  if (!userStore.loginUser) return

  myAppsLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      current: 1,
      pageSize: 3,
      sortField: 'createTime',
      sortOrder: 'desc'
    })
    if (res.data.code === 0) {
      myApps.value = res.data.data?.records || []
    }
  } catch (error) {
    console.error('加载我的应用失败:', error)
  } finally {
    myAppsLoading.value = false
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  featuredAppsLoading.value = true
  try {
    const res = await listFeaturedAppVoByPage({
      current: 1,
      pageSize: 6,
      sortField: 'priority',
      sortOrder: 'desc'
    })
    if (res.data.code === 0) {
      featuredApps.value = res.data.data?.records || []
    }
  } catch (error) {
    console.error('加载精选应用失败:', error)
  } finally {
    featuredAppsLoading.value = false
  }
}

// 页面初始化
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<style scoped>
#homePage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-4);
}

/* 主标题区域 */
.hero-section {
  text-align: center;
  padding: var(--spacing-16) 0 var(--spacing-8);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(147, 51, 234, 0.1) 100%);
  border-radius: var(--radius-3xl);
  margin-bottom: var(--spacing-12);
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255, 220, 0, 0.1), transparent 50%);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.main-title {
  font-size: var(--text-6xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--spacing-4);
  background: linear-gradient(135deg, var(--primary-600), var(--secondary-600));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.ai-icon {
  font-size: var(--text-5xl);
  margin: 0 var(--spacing-2);
}

.subtitle {
  font-size: var(--text-xl);
  color: var(--gray-700);
  margin: 0;
}

/* 输入区域 */
.input-section {
  margin-bottom: var(--spacing-12);
}

.input-container {
  background: white;
  border-radius: var(--radius-2xl);
  padding: var(--spacing-6);
  box-shadow: var(--shadow-xl);
  border: 2px solid var(--secondary-200);
  position: relative;
  overflow: hidden;
}

.input-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-600), var(--accent-500), var(--secondary-600));
}

.input-container :deep(.ant-input) {
  border: 2px solid var(--secondary-200);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  line-height: var(--leading-relaxed);
  transition: var(--transition-all);
}

.input-container :deep(.ant-input:focus) {
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-3);
  margin-top: var(--spacing-4);
  align-items: center;
}

.left-actions {
  display: flex;
  gap: var(--spacing-3);
  align-items: center;
  flex: 1;
}

.upload-btn,
.optimize-btn {
  border: 2px solid var(--secondary-300);
  color: var(--secondary-600);
  border-radius: var(--radius-lg);
  font-weight: var(--font-medium);
  transition: var(--transition-all);
}

.upload-btn:hover,
.optimize-btn:hover {
  border-color: var(--secondary-600);
  color: var(--secondary-700);
  transform: translateY(-1px);
}

/* 类型选择器样式 */
.type-selector {
  min-width: 160px !important;
  border-radius: var(--radius-lg) !important;
}

.type-selector :deep(.ant-select-selector) {
  border: 2px solid var(--secondary-300) !important;
  border-radius: var(--radius-lg) !important;
  font-weight: var(--font-medium) !important;
  transition: var(--transition-all) !important;
  height: 40px !important;
  display: flex !important;
  align-items: center !important;
}

.type-selector :deep(.ant-select-selector:hover) {
  border-color: var(--secondary-600) !important;
}

.type-selector :deep(.ant-select-focused .ant-select-selector) {
  border-color: var(--primary-500) !important;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1) !important;
}

.type-selector :deep(.ant-select-selection-item) {
  display: flex !important;
  align-items: center !important;
  gap: var(--spacing-2) !important;
}

.option-icon {
  font-size: 16px;
  margin-right: var(--spacing-1);
}

.generate-btn {
  background: var(--accent-500) !important;
  border-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold) !important;
  border-radius: var(--radius-lg) !important;
  height: 40px !important;
  width: 50px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: var(--transition-all) !important;
}

.generate-btn:hover {
  background: var(--primary-400) !important;
  border-color: var(--primary-400) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
}

.go-icon {
  font-size: 20px;
  font-weight: bold;
}

/* 快捷模板 */
.quick-templates {
  display: flex;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-16);
  flex-wrap: wrap;
  justify-content: center;
}

.template-btn {
  background: white;
  border: 2px solid var(--secondary-300);
  color: var(--secondary-600);
  border-radius: var(--radius-xl);
  padding: var(--spacing-2) var(--spacing-4);
  font-weight: var(--font-medium);
  transition: var(--transition-all);
  white-space: nowrap;
}

.template-btn:hover {
  background: var(--secondary-600);
  border-color: var(--secondary-600);
  color: white;
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

/* 区域样式 */
.my-apps-section,
.featured-apps-section {
  margin-bottom: var(--spacing-16);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-8);
}

.section-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  margin: 0;
  color: var(--deep-600);
}

.section-header :deep(.ant-btn-link) {
  color: var(--primary-600);
  font-weight: var(--font-medium);
  font-size: var(--text-base);
  transition: var(--transition-colors);
}

.section-header :deep(.ant-btn-link:hover) {
  color: var(--primary-700);
}

/* 应用网格 */
.apps-grid {
  min-height: 200px;
}

.loading-placeholder,
.empty-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.empty-placeholder :deep(.ant-empty-description) {
  color: var(--gray-600);
  font-size: var(--text-base);
  margin-bottom: var(--spacing-4);
}

.empty-placeholder :deep(.ant-btn-primary) {
  background: var(--accent-500) !important;
  border-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold) !important;
  border-radius: var(--radius-lg) !important;
}

.empty-placeholder :deep(.ant-btn-primary:hover) {
  background: var(--primary-400) !important;
  border-color: var(--primary-400) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #homePage {
    padding: 0 var(--spacing-3);
  }

  .hero-section {
    padding: var(--spacing-12) 0 var(--spacing-6);
    margin-bottom: var(--spacing-8);
  }

  .main-title {
    font-size: var(--text-4xl);
  }

  .subtitle {
    font-size: var(--text-lg);
  }

  .input-container {
    padding: var(--spacing-4);
  }

  .input-actions {
    flex-direction: column;
    gap: var(--spacing-2);
  }

  .quick-templates {
    gap: var(--spacing-2);
  }

  .template-btn {
    font-size: var(--text-sm);
    padding: var(--spacing-1) var(--spacing-3);
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-2);
  }

  .section-title {
    font-size: var(--text-2xl);
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: var(--text-3xl);
  }

  .ai-icon {
    font-size: var(--text-3xl);
  }

  .input-actions {
    flex-direction: row;
    justify-content: space-between;
  }

  .upload-btn,
  .optimize-btn {
    flex: 1;
  }

  .quick-templates {
    flex-direction: column;
  }

  .template-btn {
    width: 100%;
    text-align: center;
  }
}
</style>