<template>
    <a-card class="app-card" hoverable>
        <template #cover>
            <div class="app-cover">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="cover-image" />
                <div v-else class="default-cover">
                    <Icon :icon="app.codeGenType === 'html' ? 'mdi:file-code' : (app.codeGenType === 'vueProject' ? 'mdi:vuejs' : 'mdi:folder-multiple')" class="app-icon" />
                    <div class="app-type-badge">
                        <Icon :icon="app.codeGenType === 'html' ? 'mdi:language-html5' : (app.codeGenType === 'vueProject' ? 'mdi:vuejs' : 'mdi:folder-open')" class="badge-icon" />
                        <span>{{ getCodeGenTypeLabel(app.codeGenType) }}</span>
                    </div>
                </div>
            </div>
        </template>

        <template #actions v-if="!featured">
            <a-tooltip title="开始聊天">
                <Icon icon="mdi:chat-processing-outline" class="action-icon" @click="$emit('chat', app)" />
            </a-tooltip>
            <a-tooltip title="查看应用">
                <Icon icon="mdi:eye-outline" class="action-icon" @click="$emit('view', app)" />
            </a-tooltip>
            <a-tooltip title="编辑应用">
                <Icon icon="mdi:pencil-outline" class="action-icon" @click="$emit('edit', app)" />
            </a-tooltip>
            <a-tooltip title="删除应用">
                <Icon icon="mdi:delete-outline" class="action-icon delete-icon" @click="$emit('delete', app)" />
            </a-tooltip>
        </template>

        <template #actions v-else>
            <a-tooltip title="查看详情">
                <Icon icon="mdi:eye-outline" class="action-icon" @click="$emit('view', app)" />
            </a-tooltip>
            <a-tooltip title="创建者">
                <Icon icon="mdi:account-outline" class="action-icon" />
            </a-tooltip>
            <a-tooltip title="点赞">
                <Icon icon="mdi:heart-outline" class="action-icon" />
            </a-tooltip>
        </template>

        <a-card-meta :title="app.appName" :description="truncateText(app.initPrompt, 60)" />

        <div class="app-meta">
            <div class="app-info">
                <span class="app-type">{{ getCodeGenTypeLabel(app.codeGenType) }}</span>
                <span v-if="featured && app.user" class="app-author">by {{ app.user.name || app.user.account }}</span>
            </div>
            <div class="app-time">{{ formatTime(app.createTime) }}</div>
            <div v-if="app.deployKey" class="app-status deployed">
                <Icon icon="mdi:check-circle" class="status-icon" />
                <span>已部署</span>
            </div>
            <div v-else class="app-status draft">
                <Icon icon="mdi:file-document-edit-outline" class="status-icon" />
                <span>草稿</span>
            </div>
        </div>
    </a-card>
</template>

<script setup lang="ts">
import { Icon } from '@iconify/vue'

interface Props {
    app: API.AppVO
    featured?: boolean
}

defineProps<Props>()

defineEmits<{
    view: [app: API.AppVO]
    edit: [app: API.AppVO]
    delete: [app: API.AppVO]
    chat: [app: API.AppVO]
}>()

// 获取代码生成类型标签
const getCodeGenTypeLabel = (type?: string) => {
    const typeMap: Record<string, string> = {
        'html': '单文件网页',
        'mutiFile': '多文件',
        'vueProject': 'Vue 项目'
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
/* AppCard - Creator Studio 项目瓷砖风格 */
.app-card {
    height: 100%;
    border: 1px solid var(--color-line);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-sm);
    background: var(--color-surface);
    transition: var(--transition-all);
    overflow: hidden;
    position: relative;
    backdrop-filter: blur(14px);
}

.app-card:hover {
    transform: translateY(-3px);
    box-shadow: var(--shadow-lg);
    border-color: var(--color-line-strong);
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
    background: linear-gradient(135deg, var(--primary-500) 0%, var(--primary-700) 100%);
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
    background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.12), transparent 60%);
}

.app-icon {
    font-size: 56px;
    color: white;
    z-index: 1;
    position: relative;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.15));
    margin-bottom: var(--spacing-2);
    opacity: 0.95;
}

.app-type-badge {
    background: rgba(255, 255, 255, 0.92);
    color: var(--primary-600);
    padding: var(--spacing-1) var(--spacing-3);
    border-radius: var(--radius-full);
    font-size: var(--text-xs);
    font-weight: var(--font-semibold);
    z-index: 1;
    position: relative;
    display: flex;
    align-items: center;
    gap: var(--spacing-1);
    backdrop-filter: blur(10px);
}

.badge-icon {
    font-size: 14px;
}

/* 卡片内容 */
.app-card :deep(.ant-card-body) {
    padding: var(--spacing-5);
}

.app-card :deep(.ant-card-meta-title) {
    color: var(--color-text) !important;
    font-size: var(--text-lg) !important;
    font-weight: var(--font-semibold) !important;
    margin-bottom: var(--spacing-2) !important;
    line-height: var(--leading-tight) !important;
}

.app-card :deep(.ant-card-meta-description) {
    color: var(--color-text-soft) !important;
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
    background: var(--primary-50);
    color: var(--primary-600);
    padding: 2px var(--spacing-2);
    border-radius: var(--radius-full);
    font-size: var(--text-xs);
    font-weight: var(--font-medium);
}

.app-author {
    font-size: var(--text-xs);
    color: var(--color-text-muted);
    font-weight: var(--font-medium);
}

.app-time {
    font-size: var(--text-xs);
    color: var(--color-text-muted);
}

/* 状态标签 - 安静的 pill */
.app-status {
    font-size: var(--text-xs);
    padding: 3px var(--spacing-3);
    border-radius: var(--radius-full);
    font-weight: var(--font-medium);
    text-align: center;
    min-width: 60px;
    display: inline-flex;
    align-items: center;
    gap: var(--spacing-1);
}

.status-icon {
    font-size: 14px;
}

.app-status.deployed {
    background: var(--success-50);
    color: var(--success-600);
}

.app-status.draft {
    background: var(--warning-50);
    color: var(--warning-600);
}

/* 操作按钮 */
.app-card :deep(.ant-card-actions) {
    background: var(--color-bg-elevated) !important;
    border-top: 1px solid var(--color-line) !important;
    padding: var(--spacing-2) 0 !important;
}

.action-icon {
    color: var(--color-text-muted);
    font-size: 20px;
    transition: var(--transition-all);
    cursor: pointer;
}

.action-icon:hover {
    color: var(--primary-500);
    transform: scale(1.1);
}

.delete-icon:hover {
    color: var(--error-500);
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
