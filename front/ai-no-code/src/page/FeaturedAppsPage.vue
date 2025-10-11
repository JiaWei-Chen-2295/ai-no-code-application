<template>
    <div id="featuredAppsPage">
        <div class="page-container">
            <!-- 页面头部 -->
            <div class="page-header">
                <div class="header-content">
                    <h1 class="page-title">精选案例</h1>
                    <p class="page-description">探索 AppCraft 精工细作的优质应用</p>
                </div>
                <div class="header-stats">
                    <div class="stat-item">
                        <span class="stat-number">{{ totalApps }}</span>
                        <span class="stat-label">精选应用</span>
                    </div>
                </div>
            </div>

            <!-- 搜索和筛选 -->
            <div class="search-section">
                <div class="search-controls">
                    <a-input-search v-model:value="searchQuery" placeholder="搜索应用名称..." @search="handleSearch"
                        :loading="searching" enter-button size="large" class="search-input" />
                    <a-select v-model:value="codeGenTypeFilter" placeholder="应用类型" allowClear @change="handleSearch"
                        size="large" class="filter-select">
                        <a-select-option value="html">单文件网页</a-select-option>
                        <a-select-option value="mutiFile">多文件</a-select-option>
                    </a-select>
                    <a-select v-model:value="sortOrder" @change="handleSearch" size="large" class="sort-select">
                        <a-select-option value="desc">最新优先</a-select-option>
                        <a-select-option value="asc">最早优先</a-select-option>
                    </a-select>
                </div>
            </div>

            <!-- 应用列表 -->
            <div class="apps-section">
                <div v-if="loading && apps.length === 0" class="loading-state">
                    <a-spin size="large" />
                    <p>加载精选应用中...</p>
                </div>

                <div v-else-if="apps.length === 0" class="empty-state">
                    <a-empty description="暂无精选应用">
                        <a-button type="primary" @click="goHome">去主页创建</a-button>
                    </a-empty>
                </div>

                <div v-else class="apps-grid">
                    <a-row :gutter="[24, 24]">
                        <a-col v-for="app in apps" :key="app.id" :xs="24" :sm="12" :lg="8" :xl="6">
                            <app-card :app="app" featured @view="viewApp" />
                        </a-col>
                    </a-row>

                    <!-- 加载更多 -->
                    <div v-if="hasMore" class="load-more">
                        <a-button @click="loadMore" :loading="loading" size="large" class="load-more-btn">
                            加载更多
                        </a-button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listFeaturedAppVoByPage } from '@/api/appController'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()

// 响应式数据
const apps = ref<API.AppVO[]>([])
const loading = ref(false)
const searching = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const totalApps = ref(0)
const hasMore = ref(true)

// 搜索和筛选
const searchQuery = ref('')
const codeGenTypeFilter = ref<string>()
const sortOrder = ref('desc')

// 页面初始化
onMounted(() => {
    loadApps()
})

// 加载应用列表
const loadApps = async (isLoadMore = false) => {
    if (!isLoadMore) {
        loading.value = true
        currentPage.value = 1
        apps.value = []
    } else {
        currentPage.value++
    }

    try {
        const res = await listFeaturedAppVoByPage({
            current: currentPage.value,
            pageSize: pageSize.value,
            appName: searchQuery.value || undefined,
            codeGenType: codeGenTypeFilter.value,
            sortField: 'createTime',
            sortOrder: sortOrder.value
        })

        if (res.data.code === 0 && res.data.data) {
            const newApps = res.data.data.records || []

            if (isLoadMore) {
                apps.value.push(...newApps)
            } else {
                apps.value = newApps
            }

            totalApps.value = res.data.data.totalRow || 0
            hasMore.value = apps.value.length < totalApps.value
        } else {
            message.error('加载失败：' + res.data.message)
        }
    } catch {
        message.error('加载失败，请稍后重试')
    } finally {
        loading.value = false
        searching.value = false
    }
}

// 搜索处理
const handleSearch = () => {
    searching.value = true
    loadApps()
}

// 加载更多
const loadMore = () => {
    loading.value = true
    loadApps(true)
}

// 查看应用
const viewApp = (app: API.AppVO) => {
    router.push(`/app/chat/${app.id}`)
}

// 回到主页
const goHome = () => {
    router.push('/')
}
</script>

<style scoped>
#featuredAppsPage {
    min-height: 100vh;
    background: var(--gray-50);
}

.page-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: var(--spacing-6);
}

/* 页面头部 */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-8);
    padding: var(--spacing-8);
    background: linear-gradient(135deg, var(--primary-600) 0%, var(--secondary-600) 100%);
    border-radius: var(--radius-2xl);
    color: white;
    position: relative;
    overflow: hidden;
}

.page-header::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle at 30% 30%, rgba(255, 220, 0, 0.2), transparent 50%);
}

.header-content {
    position: relative;
    z-index: 1;
}

.page-title {
    margin: 0;
    font-size: var(--text-4xl);
    font-weight: var(--font-bold);
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.page-description {
    margin: var(--spacing-2) 0 0;
    font-size: var(--text-lg);
    opacity: 0.9;
}

.header-stats {
    position: relative;
    z-index: 1;
}

.stat-item {
    text-align: center;
    background: rgba(255, 255, 255, 0.1);
    padding: var(--spacing-4);
    border-radius: var(--radius-lg);
    backdrop-filter: blur(10px);
}

.stat-number {
    display: block;
    font-size: var(--text-3xl);
    font-weight: var(--font-bold);
    line-height: 1;
}

.stat-label {
    display: block;
    font-size: var(--text-sm);
    margin-top: var(--spacing-1);
    opacity: 0.8;
}

/* 搜索区域 */
.search-section {
    margin-bottom: var(--spacing-8);
}

.search-controls {
    display: flex;
    gap: var(--spacing-4);
    background: white;
    padding: var(--spacing-6);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    border: 2px solid var(--secondary-200);
}

.search-input {
    flex: 1;
}

.filter-select,
.sort-select {
    min-width: 120px;
}

.search-controls :deep(.ant-input-search .ant-input) {
    border-radius: var(--radius-lg);
    border: 2px solid var(--secondary-200);
    transition: var(--transition-all);
}

.search-controls :deep(.ant-input-search .ant-input:focus) {
    border-color: var(--primary-500);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-controls :deep(.ant-btn-primary) {
    background: var(--primary-600);
    border-color: var(--primary-600);
    border-radius: var(--radius-lg);
}

.search-controls :deep(.ant-select .ant-select-selector) {
    border-radius: var(--radius-lg) !important;
    border: 2px solid var(--secondary-200) !important;
}

/* 应用区域 */
.apps-section {
    margin-bottom: var(--spacing-8);
}

.loading-state,
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: var(--spacing-16);
    text-align: center;
}

.loading-state p,
.empty-state p {
    margin-top: var(--spacing-4);
    font-size: var(--text-base);
    color: var(--gray-600);
}

.empty-state :deep(.ant-btn-primary) {
    background: var(--accent-500) !important;
    border-color: var(--accent-500) !important;
    color: var(--gray-900) !important;
    font-weight: var(--font-semibold) !important;
    border-radius: var(--radius-lg) !important;
    margin-top: var(--spacing-4) !important;
}

.empty-state :deep(.ant-btn-primary:hover) {
    background: var(--primary-400) !important;
    border-color: var(--primary-400) !important;
    color: white !important;
    transform: translateY(-2px) !important;
    box-shadow: var(--shadow-lg) !important;
}

/* 应用网格 */
.apps-grid {
    margin-bottom: var(--spacing-8);
}

/* 加载更多 */
.load-more {
    text-align: center;
    margin-top: var(--spacing-8);
}

.load-more-btn {
    background: white !important;
    border: 2px solid var(--secondary-600) !important;
    color: var(--secondary-600) !important;
    font-weight: var(--font-semibold) !important;
    border-radius: var(--radius-lg) !important;
    padding: var(--spacing-2) var(--spacing-8) !important;
    height: auto !important;
    transition: var(--transition-all) !important;
}

.load-more-btn:hover {
    background: var(--secondary-600) !important;
    color: white !important;
    transform: translateY(-2px) !important;
    box-shadow: var(--shadow-md) !important;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .page-container {
        padding: var(--spacing-4);
    }
}

@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        gap: var(--spacing-4);
        text-align: center;
    }

    .page-title {
        font-size: var(--text-3xl);
    }

    .page-description {
        font-size: var(--text-base);
    }

    .search-controls {
        flex-direction: column;
        gap: var(--spacing-3);
    }

    .filter-select,
    .sort-select {
        min-width: auto;
        width: 100%;
    }
}

@media (max-width: 480px) {
    .page-container {
        padding: var(--spacing-3);
    }

    .page-header {
        padding: var(--spacing-6);
    }

    .page-title {
        font-size: var(--text-2xl);
    }

    .search-controls {
        padding: var(--spacing-4);
    }
}
</style>
