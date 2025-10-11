<template>
    <a-card class="app-card" hoverable>
        <template #cover>
            <div class="app-cover">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="cover-image" />
                <div v-else class="default-cover">
                    <div class="app-icon">📱</div>
                    <div class="app-type-badge">{{ getCodeGenTypeLabel(app.codeGenType) }}</div>
                </div>
            </div>
        </template>

        <template #actions v-if="!featured">
            <a-tooltip title="查看应用">
                <EyeOutlined @click="$emit('view', app)" />
            </a-tooltip>
            <a-tooltip title="编辑应用">
                <EditOutlined @click="$emit('edit', app)" />
            </a-tooltip>
            <a-tooltip title="删除应用">
                <DeleteOutlined @click="$emit('delete', app)" />
            </a-tooltip>
        </template>

        <template #actions v-else>
            <a-tooltip title="查看详情">
                <EyeOutlined @click="$emit('view', app)" />
            </a-tooltip>
            <a-tooltip title="创建者">
                <UserOutlined />
            </a-tooltip>
            <a-tooltip title="点赞">
                <HeartOutlined />
            </a-tooltip>
        </template>

        <a-card-meta :title="app.appName" :description="truncateText(app.initPrompt, 60)" />

        <div class="app-meta">
            <div class="app-info">
                <span class="app-type">{{ getCodeGenTypeLabel(app.codeGenType) }}</span>
                <span v-if="featured && app.user" class="app-author">by {{ app.user.name || app.user.account }}</span>
            </div>
            <div class="app-time">{{ formatTime(app.createTime) }}</div>
            <div v-if="app.deployKey" class="app-status deployed">已部署</div>
            <div v-else class="app-status draft">草稿</div>
        </div>
    </a-card>
</template>

<script setup lang="ts">
import {
    EyeOutlined,
    EditOutlined,
    DeleteOutlined,
    UserOutlined,
    HeartOutlined
} from '@ant-design/icons-vue'

interface Props {
    app: API.AppVO
    featured?: boolean
}

defineProps<Props>()

defineEmits<{
    view: [app: API.AppVO]
    edit: [app: API.AppVO]
    delete: [app: API.AppVO]
}>()

// 获取代码生成类型标签
const getCodeGenTypeLabel = (type?: string) => {
    const typeMap: Record<string, string> = {
        'html': '单文件网页',
        'mutiFile': '多文件'
    }
    return typeMap[type || ''] || type || '未知'
}

// 截断文本
const truncateText = (text?: string, maxLength: number = 60) => {
    if (!text) return '暂无描述'
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 格式化时间
const formatTime = (time?: string) => {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    const diff = now.getTime() - date.getTime()

    const minutes = Math.floor(diff / (1000 * 60))
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))

    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`

    return date.toLocaleDateString()
}
</script>

<style scoped>
.app-card {
    height: 100%;
    border: 2px solid var(--secondary-200);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    background: white;
    transition: var(--transition-all);
    overflow: hidden;
    position: relative;
}

.app-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, var(--primary-600), var(--accent-500), var(--secondary-600));
    z-index: 1;
}

.app-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-xl);
    border-color: var(--primary-300);
}

/* 卡片封面 */
.app-cover {
    height: 140px;
    position: relative;
    overflow: hidden;
}

.cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.default-cover {
    height: 100%;
    background: linear-gradient(135deg, var(--primary-600) 0%, var(--secondary-600) 100%);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
}

.default-cover::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle at 30% 30%, rgba(255, 220, 0, 0.2), transparent 50%);
}

.app-icon {
    font-size: 48px;
    color: white;
    z-index: 1;
    position: relative;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    margin-bottom: var(--spacing-2);
}

.app-type-badge {
    background: rgba(255, 255, 255, 0.9);
    color: var(--primary-600);
    padding: var(--spacing-1) var(--spacing-2);
    border-radius: var(--radius-md);
    font-size: var(--text-xs);
    font-weight: var(--font-semibold);
    z-index: 1;
    position: relative;
}

/* 卡片内容 */
.app-card :deep(.ant-card-body) {
    padding: var(--spacing-4);
}

.app-card :deep(.ant-card-meta-title) {
    color: var(--deep-600) !important;
    font-size: var(--text-lg) !important;
    font-weight: var(--font-semibold) !important;
    margin-bottom: var(--spacing-2) !important;
    line-height: var(--leading-tight) !important;
}

.app-card :deep(.ant-card-meta-description) {
    color: var(--gray-600) !important;
    font-size: var(--text-sm) !important;
    line-height: var(--leading-relaxed) !important;
    margin-bottom: var(--spacing-3) !important;
}

/* 应用元信息 */
.app-meta {
    margin-top: var(--spacing-3);
    display: flex;
    flex-direction: column;
    gap: var(--spacing-2);
}

.app-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.app-type {
    background: var(--secondary-100);
    color: var(--secondary-700);
    padding: 2px var(--spacing-2);
    border-radius: var(--radius-md);
    font-size: var(--text-xs);
    font-weight: var(--font-medium);
}

.app-author {
    font-size: var(--text-xs);
    color: var(--gray-500);
    font-weight: var(--font-medium);
}

.app-time {
    font-size: var(--text-xs);
    color: var(--gray-500);
}

.app-status {
    font-size: var(--text-xs);
    padding: 2px var(--spacing-2);
    border-radius: var(--radius-md);
    font-weight: var(--font-medium);
    text-align: center;
    min-width: 50px;
}

.app-status.deployed {
    background: var(--success-100);
    color: var(--success-700);
}

.app-status.draft {
    background: var(--warning-100);
    color: var(--warning-700);
}

/* 操作按钮 */
.app-card :deep(.ant-card-actions) {
    background: var(--gray-50) !important;
    border-top: 1px solid var(--secondary-200) !important;
    padding: var(--spacing-2) 0 !important;
}

.app-card :deep(.ant-card-actions .anticon) {
    color: var(--secondary-600) !important;
    font-size: 16px !important;
    transition: var(--transition-colors) !important;
    cursor: pointer !important;
}

.app-card :deep(.ant-card-actions .anticon:hover) {
    color: var(--primary-600) !important;
    transform: scale(1.1);
}

.app-card :deep(.ant-card-actions li:last-child .anticon:hover) {
    color: var(--error-600) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .app-cover {
        height: 120px;
    }

    .app-icon {
        font-size: 40px;
    }

    .app-card :deep(.ant-card-body) {
        padding: var(--spacing-3);
    }

    .app-card :deep(.ant-card-meta-title) {
        font-size: var(--text-base) !important;
    }

    .app-card :deep(.ant-card-meta-description) {
        font-size: var(--text-xs) !important;
    }
}
</style>
