<template>
    <div id="adminAppsPage">
        <div class="page-container">
            <!-- 页面头部 -->
            <div class="page-header">
                <div class="header-content">
                    <h1 class="page-title">应用管理</h1>
                    <p class="page-description">管理员专用 - 管理 AppCraft 平台所有应用</p>
                </div>
                <div class="header-stats">
                    <div class="stat-item">
                        <span class="stat-number">{{ totalApps }}</span>
                        <span class="stat-label">总应用数</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">{{ featuredCount }}</span>
                        <span class="stat-label">精选应用</span>
                    </div>
                </div>
            </div>

            <!-- 搜索和筛选区域 -->
            <div class="search-section">
                <a-card class="search-card">
                    <a-form :model="searchForm" layout="inline" @finish="handleSearch" class="search-form">
                        <a-form-item label="应用名称">
                            <a-input v-model:value="searchForm.appName" placeholder="搜索应用名称" allow-clear
                                @change="handleSearch" />
                        </a-form-item>

                        <a-form-item label="应用类型">
                            <a-select v-model:value="searchForm.codeGenType" placeholder="选择类型" allow-clear
                                @change="handleSearch" style="width: 120px">
                                <a-select-option value="html">单文件网页</a-select-option>
                                <a-select-option value="mutiFile">多文件</a-select-option>
                            </a-select>
                        </a-form-item>

                        <a-form-item label="用户ID">
                            <a-input-number v-model:value="searchForm.userId" placeholder="用户ID" :min="1"
                                @change="handleSearch" style="width: 120px" />
                        </a-form-item>

                        <a-form-item label="优先级">
                            <a-input-number v-model:value="searchForm.priority" placeholder="优先级" :min="0" :max="999"
                                @change="handleSearch" style="width: 120px" />
                        </a-form-item>

                        <a-form-item label="排序">
                            <a-select v-model:value="searchForm.sortField" @change="handleSearch" style="width: 120px">
                                <a-select-option value="createTime">创建时间</a-select-option>
                                <a-select-option value="updateTime">更新时间</a-select-option>
                                <a-select-option value="priority">优先级</a-select-option>
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

            <!-- 应用列表 -->
            <div class="table-section">
                <a-card class="table-card">
                    <template #title>
                        <div class="table-header">
                            <span>应用列表</span>
                            <div class="table-actions">
                                <a-button @click="refreshData" :loading="loading">
                                    <ReloadOutlined />
                                    刷新
                                </a-button>
                            </div>
                        </div>
                    </template>

                    <a-table :columns="columns" :data-source="apps" :loading="loading" :pagination="paginationConfig"
                        @change="handleTableChange" row-key="id" scroll="{ x: 1200 }">
                        <!-- 应用信息列 -->
                        <template #app="{ record }">
                            <div class="app-info">
                                <div class="app-details">
                                    <h4 class="app-name">{{ record.appName }}</h4>
                                    <p class="app-prompt">{{ truncateText(record.initPrompt, 50) }}</p>
                                </div>
                            </div>
                        </template>

                        <!-- 类型列 -->
                        <template #type="{ record }">
                            <a-tag :color="getTypeColor(record.codeGenType)">
                                {{ getCodeGenTypeLabel(record.codeGenType) }}
                            </a-tag>
                        </template>

                        <!-- 状态列 -->
                        <template #status="{ record }">
                            <a-tag v-if="record.deployKey" color="success">
                                <CheckCircleOutlined />
                                已部署
                            </a-tag>
                            <a-tag v-else color="default">
                                <ExclamationCircleOutlined />
                                草稿
                            </a-tag>
                        </template>

                        <!-- 优先级列 -->
                        <template #priority="{ record }">
                            <a-tag v-if="record.priority === 99" color="gold">
                                <StarOutlined />
                                精选
                            </a-tag>
                            <span v-else>{{ record.priority || 0 }}</span>
                        </template>

                        <!-- 作者列 -->
                        <template #user="{ record }">
                            <div class="user-info">
                                <a-avatar :size="24" :src="record.user?.avatar">
                                    {{ record.user?.name?.[0] || 'U' }}
                                </a-avatar>
                                <span>{{ record.user?.name || record.user?.account || '未知' }}</span>
                            </div>
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
                                    <a-button type="link" @click="viewApp(record)" size="small">
                                        <EyeOutlined />
                                    </a-button>
                                </a-tooltip>

                                <a-tooltip title="编辑应用">
                                    <a-button type="link" @click="editApp(record)" size="small">
                                        <EditOutlined />
                                    </a-button>
                                </a-tooltip>

                                <a-tooltip :title="record.priority === 99 ? '取消精选' : '设为精选'">
                                    <a-button type="link" @click="toggleFeatured(record)" size="small"
                                        :loading="record._featuring">
                                        <StarOutlined v-if="record.priority === 99" style="color: #faad14" />
                                        <StarOutlined v-else />
                                    </a-button>
                                </a-tooltip>

                                <a-tooltip title="删除应用">
                                    <a-button type="link" danger @click="deleteApp(record)" size="small"
                                        :loading="record._deleting">
                                        <DeleteOutlined />
                                    </a-button>
                                </a-tooltip>
                            </div>
                        </template>
                    </a-table>
                </a-card>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'
import {
    SearchOutlined,
    ReloadOutlined,
    EyeOutlined,
    EditOutlined,
    DeleteOutlined,
    StarOutlined,
    CheckCircleOutlined,
    ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import {
    listAppByPage,
    deleteApp as deleteAppApi,
    updateApp
} from '@/api/appController'
import { useUserStore } from '@/stores/userStore'

interface AppWithStatus extends API.App {
    user?: API.UserVO
    _featuring?: boolean
    _deleting?: boolean
}

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const apps = ref<AppWithStatus[]>([])
const loading = ref(false)
const totalApps = ref(0)

// 搜索表单
const searchForm = reactive({
    appName: '',
    codeGenType: undefined as string | undefined,
    userId: undefined as number | undefined,
    priority: undefined as number | undefined,
    sortField: 'createTime',
    sortOrder: 'desc'
})

// 分页配置
const paginationConfig = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number, range: [number, number]) =>
        `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
    pageSizeOptions: ['10', '20', '50', '100']
})

// 表格列配置
const columns: TableColumnsType = [
    {
        title: 'ID',
        dataIndex: 'id',
        width: 80,
        sorter: true
    },
    {
        title: '应用信息',
        key: 'app',
        width: 300,
        slots: { customRender: 'app' }
    },
    {
        title: '类型',
        key: 'type',
        width: 100,
        slots: { customRender: 'type' }
    },
    {
        title: '状态',
        key: 'status',
        width: 100,
        slots: { customRender: 'status' }
    },
    {
        title: '优先级',
        key: 'priority',
        width: 100,
        slots: { customRender: 'priority' },
        sorter: true
    },
    {
        title: '作者',
        key: 'user',
        width: 150,
        slots: { customRender: 'user' }
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
const featuredCount = computed(() => {
    return apps.value.filter(app => app.priority === 99).length
})

// 页面初始化
onMounted(() => {
    // 检查管理员权限
    if (userStore.loginUser?.userRole !== 'admin') {
        message.error('您没有访问此页面的权限')
        router.push('/')
        return
    }

    loadApps()
})

// 加载应用列表
const loadApps = async () => {
    loading.value = true
    try {
        const res = await listAppByPage({
            current: paginationConfig.current,
            pageSize: paginationConfig.pageSize,
            sortField: searchForm.sortField,
            sortOrder: searchForm.sortOrder,
            appName: searchForm.appName || undefined,
            codeGenType: searchForm.codeGenType,
            userId: searchForm.userId,
            priority: searchForm.priority
        })

        if (res.data.code === 0 && res.data.data) {
            apps.value = res.data.data.records || []
            totalApps.value = res.data.data.totalRow || 0
            paginationConfig.total = totalApps.value
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
    loadApps()
}

// 重置搜索
const resetSearch = () => {
    Object.assign(searchForm, {
        appName: '',
        codeGenType: undefined,
        userId: undefined,
        priority: undefined,
        sortField: 'createTime',
        sortOrder: 'desc'
    })
    handleSearch()
}

// 刷新数据
const refreshData = () => {
    loadApps()
}

// 处理表格变化
const handleTableChange = (pagination: { current: number; pageSize: number }, _filters: Record<string, unknown>, sorter: { field?: string; order?: string }) => {
    paginationConfig.current = pagination.current
    paginationConfig.pageSize = pagination.pageSize

    if (sorter.field) {
        searchForm.sortField = sorter.field
        searchForm.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
    }

    loadApps()
}

// 查看应用
const viewApp = (app: AppWithStatus) => {
    router.push(`/app/detail/${app.id}`)
}

// 编辑应用
const editApp = (app: AppWithStatus) => {
    router.push(`/app/edit/${app.id}`)
}

// 切换精选状态
const toggleFeatured = async (app: AppWithStatus) => {
    const newPriority = app.priority === 99 ? 0 : 99
    const action = newPriority === 99 ? '设为精选' : '取消精选'

    app._featuring = true
    try {
        const res = await updateApp({
            id: app.id,
            appName: app.appName,
            cover: app.cover,
            priority: newPriority
        })

        if (res.data.code === 0) {
            app.priority = newPriority
            message.success(`${action}成功`)
        } else {
            message.error(`${action}失败：` + res.data.message)
        }
    } catch {
        message.error(`${action}失败`)
    } finally {
        app._featuring = false
    }
}

// 删除应用
const deleteApp = (app: AppWithStatus) => {
    Modal.confirm({
        title: '确认删除',
        content: `确定要删除应用"${app.appName}"吗？此操作不可恢复。`,
        okText: '确认删除',
        okType: 'danger',
        cancelText: '取消',
        onOk: async () => {
            app._deleting = true
            try {
                const res = await deleteAppApi({ deleteId: app.id! })
                if (res.data.code === 0) {
                    message.success('删除成功')
                    loadApps()
                } else {
                    message.error('删除失败：' + res.data.message)
                }
            } catch {
                message.error('删除失败')
            } finally {
                app._deleting = false
            }
        }
    })
}

// 工具函数
const getCodeGenTypeLabel = (type?: string) => {
    const typeMap: Record<string, string> = {
        'html': '单文件网页',
        'mutiFile': '多文件'
    }
    return typeMap[type || ''] || type || '未知'
}

const getTypeColor = (type?: string) => {
    const colorMap: Record<string, string> = {
        'html': 'orange',
        'mutiFile': 'blue'
    }
    return colorMap[type || ''] || 'default'
}

const truncateText = (text?: string, maxLength: number = 50) => {
    if (!text) return '暂无描述'
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const formatTime = (time?: string) => {
    if (!time) return ''
    return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
#adminAppsPage {
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
    background: linear-gradient(135deg, var(--error-600) 0%, var(--warning-600) 100%);
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
.app-info {
    display: flex;
    gap: var(--spacing-3);
}

.app-details {
    flex: 1;
}

.app-name {
    margin: 0;
    font-size: var(--text-base);
    font-weight: var(--font-semibold);
    color: var(--deep-600);
    line-height: var(--leading-tight);
}

.app-prompt {
    margin: var(--spacing-1) 0 0;
    font-size: var(--text-sm);
    color: var(--gray-600);
    line-height: var(--leading-normal);
}

.user-info {
    display: flex;
    align-items: center;
    gap: var(--spacing-2);
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
