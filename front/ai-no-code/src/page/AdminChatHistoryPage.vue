<template>
    <div id="adminChatHistoryPage">
        <div class="page-container">
            <!-- 页面头部 -->
            <div class="page-header">
                <div class="header-content">
                    <h1 class="page-title">对话管理</h1>
                    <p class="page-description">管理员专用 - 管理 AppCraft 平台所有对话记录</p>
                </div>
                <div class="header-stats">
                    <div class="stat-item">
                        <span class="stat-number">{{ totalChatHistory }}</span>
                        <span class="stat-label">总对话数</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">{{ codeVersionCount }}</span>
                        <span class="stat-label">代码版本</span>
                    </div>
                </div>
            </div>

            <!-- 搜索和筛选区域 -->
            <div class="search-section">
                <a-card class="search-card">
                    <a-form :model="searchForm" layout="inline" @finish="handleSearch" class="search-form">
                        <a-form-item label="应用ID">
                            <a-input-number v-model:value="searchForm.appId" placeholder="应用ID" :min="1"
                                @change="handleSearch" style="width: 120px" />
                        </a-form-item>

                        <a-form-item label="消息内容">
                            <a-input v-model:value="searchForm.message" placeholder="搜索消息内容" allow-clear
                                @change="handleSearch" style="width: 200px" />
                        </a-form-item>

                        <a-form-item label="消息类型">
                            <a-select v-model:value="searchForm.messageType" placeholder="选择类型" allow-clear
                                @change="handleSearch" style="width: 120px">
                                <a-select-option value="user">用户</a-select-option>
                                <a-select-option value="assistant">AI助手</a-select-option>
                            </a-select>
                        </a-form-item>

                        <a-form-item label="用户ID">
                            <a-input-number v-model:value="searchForm.userId" placeholder="用户ID" :min="1"
                                @change="handleSearch" style="width: 120px" />
                        </a-form-item>

                        <a-form-item label="排序">
                            <a-select v-model:value="searchForm.sortField" @change="handleSearch" style="width: 120px">
                                <a-select-option value="createTime">创建时间</a-select-option>
                                <a-select-option value="updateTime">更新时间</a-select-option>
                                <a-select-option value="codeVersion">代码版本</a-select-option>
                            </a-select>
                        </a-form-item>

                        <a-form-item>
                            <a-select v-model:value="searchForm.sortOrder" @change="handleSearch" style="width: 80px">
                                <a-select-option value="desc">降序</a-select-option>
                                <a-select-option value="asc">升序</a-select-option>
                            </a-select>
                        </a-form-item>

                        <a-form-item>
                            <a-button type="primary" @click="handleSearch" :loading="loading">
                                <SearchOutlined />
                                搜索
                            </a-button>
                        </a-form-item>

                        <a-form-item>
                            <a-button @click="resetSearch">
                                <ReloadOutlined />
                                重置
                            </a-button>
                        </a-form-item>
                    </a-form>
                </a-card>
            </div>

            <!-- 对话记录列表 -->
            <div class="table-section">
                <a-card class="table-card">
                    <template #title>
                        <div class="table-header">
                            <span>对话记录列表</span>
                            <div class="table-actions">
                                <a-button @click="refreshData" :loading="loading">
                                    <ReloadOutlined />
                                    刷新
                                </a-button>
                            </div>
                        </div>
                    </template>

                    <a-table :columns="columns" :data-source="chatHistory" :loading="loading" :pagination="paginationConfig"
                        @change="handleTableChange" row-key="id" scroll="{ x: 1400 }">
                        <!-- ID列 -->
                        <template #id="{ record }">
                            <span class="record-id">#{{ record.id }}</span>
                        </template>

                        <!-- 应用信息列 -->
                        <template #app="{ record }">
                            <div class="app-info">
                                <a-tag color="blue">ID: {{ record.appId }}</a-tag>
                            </div>
                        </template>

                        <!-- 消息内容列 -->
                        <template #message="{ record }">
                            <div class="message-content">
                                <div class="message-text">{{ truncateText(record.message, 100) }}</div>
                                <a-tooltip v-if="record.message && record.message.length > 100" :title="record.message">
                                    <a-button type="link" size="small">查看全部</a-button>
                                </a-tooltip>
                            </div>
                        </template>

                        <!-- 消息类型列 -->
                        <template #messageType="{ record }">
                            <a-tag :color="getMessageTypeColor(record.messageType)">
                                <UserOutlined v-if="record.messageType === 'user'" />
                                <RobotOutlined v-else />
                                {{ getMessageTypeLabel(record.messageType) }}
                            </a-tag>
                        </template>

                        <!-- 用户ID列 -->
                        <template #user="{ record }">
                            <a-tag>用户 {{ record.userId }}</a-tag>
                        </template>

                        <!-- 代码版本列 -->
                        <template #codeVersion="{ record }">
                            <a-tag v-if="(record as any).isCode === 1" color="success">
                                <CodeOutlined />
                                v{{ (record as any).codeVersion || 0 }}
                            </a-tag>
                            <span v-else class="no-code">无代码</span>
                        </template>

                        <!-- 时间列 -->
                        <template #time="{ record }">
                            <div class="time-info">
                                <div>创建：{{ formatTime(record.createTime) }}</div>
                                <div v-if="record.updateTime !== record.createTime">
                                    更新：{{ formatTime(record.updateTime) }}
                                </div>
                            </div>
                        </template>

                        <!-- 操作列 -->
                        <template #actions="{ record }">
                            <div class="action-buttons">
                                <a-tooltip title="查看详情">
                                    <a-button type="link" @click="viewDetail(record)" size="small">
                                        <EyeOutlined />
                                    </a-button>
                                </a-tooltip>

                                <a-tooltip title="查看应用">
                                    <a-button type="link" @click="viewApp(record)" size="small">
                                        <AppstoreOutlined />
                                    </a-button>
                                </a-tooltip>

                                <a-tooltip title="删除记录" v-if="userStore.loginUser?.userRole === 'admin'">
                                    <a-popconfirm title="确定要删除这条对话记录吗？" @confirm="deleteRecord(record)"
                                        okText="确定" cancelText="取消">
                                        <a-button type="link" danger size="small" :loading="record._deleting">
                                            <DeleteOutlined />
                                        </a-button>
                                    </a-popconfirm>
                                </a-tooltip>
                            </div>
                        </template>
                    </a-table>
                </a-card>
            </div>

            <!-- 查看详情模态框 -->
            <a-modal v-model:open="detailVisible" title="对话详情" :footer="null" width="800px">
                <div v-if="selectedRecord" class="detail-content">
                    <a-descriptions :column="2" bordered>
                        <a-descriptions-item label="记录ID">{{ selectedRecord.id }}</a-descriptions-item>
                        <a-descriptions-item label="应用ID">{{ selectedRecord.appId }}</a-descriptions-item>
                        <a-descriptions-item label="用户ID">{{ selectedRecord.userId }}</a-descriptions-item>
                        <a-descriptions-item label="消息类型">
                            <a-tag :color="getMessageTypeColor(selectedRecord.messageType)">
                                {{ getMessageTypeLabel(selectedRecord.messageType) }}
                            </a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="是否生成代码">
                            <a-tag :color="(selectedRecord as any)?.isCode === 1 ? 'success' : 'default'">
                                {{ (selectedRecord as any)?.isCode === 1 ? '是' : '否' }}
                            </a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="代码版本" v-if="(selectedRecord as any)?.isCode === 1">
                            {{ (selectedRecord as any)?.codeVersion || 0 }}
                        </a-descriptions-item>
                        <a-descriptions-item label="创建时间">{{ formatTime(selectedRecord.createTime) }}</a-descriptions-item>
                        <a-descriptions-item label="更新时间">{{ formatTime(selectedRecord.updateTime) }}</a-descriptions-item>
                    </a-descriptions>
                    
                    <div class="message-detail">
                        <h4>消息内容：</h4>
                        <div class="message-text-full">{{ selectedRecord.message }}</div>
                    </div>
                </div>
            </a-modal>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'
import {
    SearchOutlined,
    ReloadOutlined,
    EyeOutlined,
    DeleteOutlined,
    UserOutlined,
    RobotOutlined,
    CodeOutlined,
    AppstoreOutlined
} from '@ant-design/icons-vue'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
import { useUserStore } from '@/stores/userStore'

interface ChatHistoryWithStatus extends API.ChatHistory {
    _deleting?: boolean
}

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const chatHistory = ref<ChatHistoryWithStatus[]>([])
const loading = ref(false)
const totalChatHistory = ref(0)
const detailVisible = ref(false)
const selectedRecord = ref<API.ChatHistory | null>(null)

// 搜索表单
const searchForm = reactive({
    appId: undefined as number | undefined,
    message: '',
    messageType: undefined as string | undefined,
    userId: undefined as number | undefined,
    sortField: 'createTime',
    sortOrder: 'desc'
})

// 分页配置
const paginationConfig = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number, range: [number, number]) =>
        `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
    pageSizeOptions: ['20', '50', '100', '200']
})

// 表格列配置
const columns: TableColumnsType = [
    {
        title: 'ID',
        key: 'id',
        width: 80,
        slots: { customRender: 'id' },
        sorter: true
    },
    {
        title: '应用',
        key: 'app',
        width: 100,
        slots: { customRender: 'app' }
    },
    {
        title: '消息内容',
        key: 'message',
        width: 300,
        slots: { customRender: 'message' }
    },
    {
        title: '类型',
        key: 'messageType',
        width: 100,
        slots: { customRender: 'messageType' }
    },
    {
        title: '用户',
        key: 'user',
        width: 100,
        slots: { customRender: 'user' }
    },
    {
        title: '代码版本',
        key: 'codeVersion',
        width: 120,
        slots: { customRender: 'codeVersion' }
    },
    {
        title: '时间',
        key: 'time',
        width: 180,
        slots: { customRender: 'time' }
    },
    {
        title: '操作',
        key: 'actions',
        width: 150,
        fixed: 'right',
        slots: { customRender: 'actions' }
    }
]

// 计算属性
const codeVersionCount = computed(() => {
    return chatHistory.value.filter((record: any) => record.isCode === 1).length
})

// 页面初始化
onMounted(() => {
    // 检查管理员权限
    if (userStore.loginUser?.userRole !== 'admin') {
        message.error('您没有访问此页面的权限')
        router.push('/')
        return
    }

    loadChatHistory()
})

// 加载对话记录列表
const loadChatHistory = async () => {
    loading.value = true
    try {
        const res = await listAllChatHistoryByPageForAdmin({
            current: paginationConfig.current,
            pageSize: paginationConfig.pageSize,
            sortField: searchForm.sortField,
            sortOrder: searchForm.sortOrder,
            appId: searchForm.appId,
            message: searchForm.message || undefined,
            messageType: searchForm.messageType,
            userId: searchForm.userId
        })

        if (res.data.code === 0 && res.data.data) {
            chatHistory.value = res.data.data.records || []
            totalChatHistory.value = res.data.data.totalRow || 0
            paginationConfig.total = totalChatHistory.value
        } else {
            message.error('加载失败：' + res.data.message)
        }
    } catch {
        message.error('加载失败，请稍后重试')
    } finally {
        loading.value = false
    }
}

// 处理搜索
const handleSearch = () => {
    paginationConfig.current = 1
    loadChatHistory()
}

// 重置搜索
const resetSearch = () => {
    Object.assign(searchForm, {
        appId: undefined,
        message: '',
        messageType: undefined,
        userId: undefined,
        sortField: 'createTime',
        sortOrder: 'desc'
    })
    handleSearch()
}

// 刷新数据
const refreshData = () => {
    loadChatHistory()
}

// 处理表格变化
const handleTableChange = (pagination: { current: number; pageSize: number }, _filters: Record<string, unknown>, sorter: { field?: string; order?: string }) => {
    paginationConfig.current = pagination.current
    paginationConfig.pageSize = pagination.pageSize

    if (sorter.field) {
        searchForm.sortField = sorter.field
        searchForm.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
    }

    loadChatHistory()
}

// 查看详情
const viewDetail = (record: API.ChatHistory) => {
    selectedRecord.value = record
    detailVisible.value = true
}

// 查看应用
const viewApp = (record: API.ChatHistory) => {
    router.push(`/app/detail/${record.appId}`)
}

// 删除记录
const deleteRecord = async () => {
    // 注意：这里需要后端提供删除接口，暂时显示提示
    message.info('删除功能需要后端提供相应的API接口')
    // record._deleting = true
    // try {
    //     const res = await deleteChatHistoryApi({ id: record.id! })
    //     if (res.data.code === 0) {
    //         message.success('删除成功')
    //         loadChatHistory()
    //     } else {
    //         message.error('删除失败：' + res.data.message)
    //     }
    // } catch {
    //     message.error('删除失败')
    // } finally {
    //     record._deleting = false
    // }
}

// 工具函数
const getMessageTypeLabel = (type?: string) => {
    const typeMap: Record<string, string> = {
        'user': '用户',
        'assistant': 'AI助手'
    }
    return typeMap[type || ''] || type || '未知'
}

const getMessageTypeColor = (type?: string) => {
    const colorMap: Record<string, string> = {
        'user': 'blue',
        'assistant': 'green'
    }
    return colorMap[type || ''] || 'default'
}

const truncateText = (text?: string, maxLength: number = 100) => {
    if (!text) return '暂无内容'
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const formatTime = (time?: string) => {
    if (!time) return ''
    return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
#adminChatHistoryPage {
    min-height: 100vh;
    background: var(--gray-50);
}

.page-container {
    max-width: 1600px;
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
    display: flex;
    gap: var(--spacing-4);
    position: relative;
    z-index: 1;
}

.stat-item {
    text-align: center;
    background: rgba(255, 255, 255, 0.1);
    padding: var(--spacing-4);
    border-radius: var(--radius-lg);
    backdrop-filter: blur(10px);
    min-width: 80px;
}

.stat-number {
    display: block;
    font-size: var(--text-2xl);
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
    margin-bottom: var(--spacing-6);
}

.search-card {
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    border: 2px solid var(--secondary-200);
}

.search-form :deep(.ant-form-item) {
    margin-bottom: var(--spacing-3);
}

.search-form :deep(.ant-input) {
    border-radius: var(--radius-lg);
}

.search-form :deep(.ant-select .ant-select-selector) {
    border-radius: var(--radius-lg) !important;
}

/* 表格区域 */
.table-section {
    margin-bottom: var(--spacing-8);
}

.table-card {
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    border: 2px solid var(--secondary-200);
    overflow: hidden;
}

.table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.table-actions {
    display: flex;
    gap: var(--spacing-2);
}

/* 表格内容样式 */
.record-id {
    font-weight: var(--font-semibold);
    color: var(--secondary-600);
}

.app-info {
    display: flex;
    gap: var(--spacing-2);
}

.message-content {
    max-width: 300px;
}

.message-text {
    font-size: var(--text-sm);
    color: var(--gray-700);
    line-height: var(--leading-normal);
    word-break: break-word;
}

.no-code {
    color: var(--gray-500);
    font-size: var(--text-sm);
}

.time-info {
    font-size: var(--text-xs);
    color: var(--gray-600);
    line-height: var(--leading-relaxed);
}

.action-buttons {
    display: flex;
    gap: var(--spacing-1);
}

/* 详情模态框 */
.detail-content {
    padding: var(--spacing-4) 0;
}

.message-detail {
    margin-top: var(--spacing-6);
}

.message-detail h4 {
    margin-bottom: var(--spacing-3);
    font-weight: var(--font-semibold);
    color: var(--deep-600);
}

.message-text-full {
    background: var(--gray-50);
    padding: var(--spacing-4);
    border-radius: var(--radius-lg);
    border: 1px solid var(--secondary-200);
    font-size: var(--text-sm);
    line-height: var(--leading-relaxed);
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 400px;
    overflow-y: auto;
}

/* 表格样式覆盖 */
.table-card :deep(.ant-table) {
    font-size: var(--text-sm);
}

.table-card :deep(.ant-table-thead > tr > th) {
    background: var(--gray-50);
    font-weight: var(--font-semibold);
    color: var(--deep-600);
    border-bottom: 2px solid var(--secondary-200);
}

.table-card :deep(.ant-table-tbody > tr:hover > td) {
    background: var(--primary-50);
}

.table-card :deep(.ant-btn-link) {
    padding: var(--spacing-1);
    height: auto;
    font-size: 16px;
}

.table-card :deep(.ant-btn-link.ant-btn-dangerous) {
    color: var(--error-600);
}

.table-card :deep(.ant-btn-link.ant-btn-dangerous:hover) {
    color: var(--error-700);
}

/* 标签样式 */
.table-card :deep(.ant-tag) {
    border-radius: var(--radius-md);
    font-size: var(--text-xs);
    display: inline-flex;
    align-items: center;
    gap: 2px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .page-container {
        padding: var(--spacing-4);
    }

    .search-form {
        flex-wrap: wrap;
    }
}

@media (max-width: 768px) {
    .page-header {
        flex-direction: column;
        gap: var(--spacing-4);
        text-align: center;
    }

    .header-stats {
        justify-content: center;
    }

    .page-title {
        font-size: var(--text-3xl);
    }

    .search-form :deep(.ant-form-item) {
        margin-right: 0;
        width: 100%;
    }

    .table-header {
        flex-direction: column;
        gap: var(--spacing-2);
        align-items: stretch;
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

    .header-stats {
        flex-direction: column;
    }

    .stat-item {
        min-width: auto;
    }
}
</style>
