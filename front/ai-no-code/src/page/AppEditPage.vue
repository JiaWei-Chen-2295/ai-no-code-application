<template>
    <div id="appEditPage">
        <div class="edit-container">
            <!-- 页面头部 -->
            <div class="page-header">
                <div class="header-left">
                    <a-button type="text" @click="goBack" class="back-btn">
                        <Icon icon="mdi:arrow-left" />
                    </a-button>
                    <div class="page-title">
                        <h1>
                            <Icon icon="mdi:pencil" class="title-icon" />
                            <span>编辑应用</span>
                        </h1>
                        <p>{{ isAdmin ? '管理员编辑模式' : '编辑我的应用' }}</p>
                    </div>
                </div>
                <div class="header-actions">
                    <a-button @click="goBack">
                        <Icon icon="mdi:close" class="btn-icon" />
                        <span>取消</span>
                    </a-button>
                    <a-button type="primary" @click="handleSubmit" :loading="saving">
                        <Icon icon="mdi:content-save" class="btn-icon" />
                        <span>保存修改</span>
                    </a-button>
                </div>
            </div>

            <!-- 编辑表单 -->
            <div class="edit-content">
                <a-card v-if="app" class="edit-form-card">
                    <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical" @finish="handleSubmit">
                        <!-- 基本信息 -->
                        <div class="form-section">
                            <h3 class="section-title">基本信息</h3>

                            <a-row :gutter="24">
                                <a-col :span="24">
                                    <a-form-item label="应用名称" name="appName" required>
                                        <a-input v-model:value="formData.appName" placeholder="请输入应用名称" :maxlength="50"
                                            show-count />
                                    </a-form-item>
                                </a-col>
                            </a-row>

                            <a-row :gutter="24">
                                <a-col :span="12">
                                    <a-form-item label="代码生成类型">
                                        <a-select v-model:value="formData.codeGenType" disabled>
                                            <a-select-option value="html">单文件网页</a-select-option>
                                            <a-select-option value="mutiFile">多文件</a-select-option>
                                        </a-select>
                                    </a-form-item>
                                </a-col>
                                <a-col :span="12">
                                    <a-form-item label="创建时间">
                                        <a-input :value="formatTime(app.createTime)" disabled />
                                    </a-form-item>
                                </a-col>
                            </a-row>

                            <a-row :gutter="24">
                                <a-col :span="24">
                                    <a-form-item label="初始提示词">
                                        <a-textarea :value="app.initPrompt" :rows="3" disabled
                                            placeholder="初始提示词不可修改" />
                                    </a-form-item>
                                </a-col>
                            </a-row>
                        </div>

                        <!-- 管理员专属字段 -->
                        <div v-if="isAdmin" class="form-section">
                            <h3 class="section-title">管理员设置</h3>

                            <a-row :gutter="24">
                                <a-col :span="12">
                                    <a-form-item label="应用封面" name="cover">
                                        <a-input v-model:value="formData.cover" placeholder="请输入封面图片URL" />
                                    </a-form-item>
                                </a-col>
                                <a-col :span="12">
                                    <a-form-item label="优先级" name="priority">
                                        <a-input-number v-model:value="formData.priority" :min="0" :max="999"
                                            placeholder="数字越大优先级越高" style="width: 100%" />
                                        <div class="field-tip">
                                            设置为99可将应用标记为精选
                                        </div>
                                    </a-form-item>
                                </a-col>
                            </a-row>

                            <a-row :gutter="24">
                                <a-col :span="12">
                                    <a-form-item label="应用状态">
                                        <a-tag v-if="app.deployKey" color="success">
                                            <CheckCircleOutlined />
                                            已部署
                                        </a-tag>
                                        <a-tag v-else color="warning">
                                            <ExclamationCircleOutlined />
                                            草稿
                                        </a-tag>
                                    </a-form-item>
                                </a-col>
                                <a-col :span="12">
                                    <a-form-item label="应用作者">
                                        <div class="author-info">
                                            <a-avatar :size="24" :src="app.user?.avatar">
                                                {{ app.user?.name?.[0] || 'U' }}
                                            </a-avatar>
                                            <span>{{ app.user?.name || app.user?.account || '未知用户' }}</span>
                                        </div>
                                    </a-form-item>
                                </a-col>
                            </a-row>
                        </div>

                        <!-- 应用预览 -->
                        <div class="form-section">
                            <h3 class="section-title">应用预览</h3>
                            <div class="app-preview">
                                <app-card :app="previewApp" :featured="formData.priority === 99" />
                            </div>
                        </div>
                    </a-form>
                </a-card>

                <!-- 加载状态 -->
                <div v-else class="loading-container">
                    <a-spin size="large" />
                    <p>加载应用信息中...</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { Icon } from '@iconify/vue'
import {
    getAppVoById,
    updateMyApp,
    updateApp,
    getAppById
} from '@/api/appController'
import { useUserStore } from '@/stores/userStore'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()

// 响应式数据
const app = ref<API.AppVO>()
const saving = ref(false)

// 表单数据
const formData = ref({
    appName: '',
    cover: '',
    priority: 0,
    codeGenType: ''
})

// 表单验证规则
const formRules = {
    appName: [
        { required: true, message: '请输入应用名称', trigger: 'blur' },
        { max: 50, message: '应用名称不能超过50个字符', trigger: 'blur' }
    ],
    cover: [
        { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }
    ],
    priority: [
        { type: 'number', min: 0, max: 999, message: '优先级范围为0-999', trigger: 'blur' }
    ]
}

// 计算属性
const isAdmin = computed(() => {
    return userStore.loginUser?.userRole === 'admin'
})

const canEdit = computed(() => {
    if (!app.value || !userStore.loginUser) return false
    return isAdmin.value || app.value.userId === userStore.loginUser.id
})

const previewApp = computed((): API.AppVO => {
    if (!app.value) return {} as API.AppVO

    return {
        ...app.value,
        appName: formData.value.appName || app.value.appName,
        cover: formData.value.cover || app.value.cover,
        priority: formData.value.priority ?? app.value.priority,
    }
})

// 监听表单数据变化，实时更新预览
watch(formData, () => {
    // 触发预览更新
}, { deep: true })

// 页面初始化
onMounted(async () => {
    await loadApp()
    if (!canEdit.value) {
        message.error('您没有编辑此应用的权限')
        router.go(-1)
    }
})

// 加载应用信息
const loadApp = async () => {
    const id = route.params.id as string
    if (!id) {
        message.error('应用ID无效')
        router.go(-1)
        return
    }

    try {
        // 根据用户角色选择不同的API
        const res = isAdmin.value
            ? await getAppById({ id: id })
            : await getAppVoById({ id: id })

        if (res.data.code === 0 && res.data.data) {
            app.value = res.data.data as API.AppVO

            // 初始化表单数据
            formData.value = {
                appName: app.value.appName || '',
                cover: app.value.cover || '',
                priority: app.value.priority || 0,
                codeGenType: app.value.codeGenType || ''
            }
        } else {
            message.error('加载应用信息失败：' + res.data.message)
            router.go(-1)
        }
    } catch {
        message.error('加载应用信息失败')
        router.go(-1)
    }
}

// 提交表单
const handleSubmit = async () => {
    if (!app.value || !formRef.value) return

    try {
        await formRef.value.validate()
    } catch {
        return
    }

    saving.value = true
    try {
        let res

        if (isAdmin.value) {
            // 管理员使用完整更新接口
            res = await updateApp({
                id: app.value.id,
                appName: formData.value.appName,
                cover: formData.value.cover || undefined,
                priority: formData.value.priority
            })
        } else {
            // 普通用户只能更新应用名称
            res = await updateMyApp({
                id: app.value.id,
                appName: formData.value.appName
            })
        }

        if (res.data.code === 0) {
            message.success('保存成功')
            router.go(-1)
        } else {
            message.error('保存失败：' + res.data.message)
        }
    } catch {
        message.error('保存失败，请稍后重试')
    } finally {
        saving.value = false
    }
}

// 返回上一页
const goBack = () => {
    router.go(-1)
}

// 格式化时间
const formatTime = (time?: string) => {
    if (!time) return ''
    return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
#appEditPage {
    min-height: 100vh;
    background: var(--gray-50);
}

.edit-container {
    max-width: 1000px;
    margin: 0 auto;
    padding: var(--spacing-6);
}

/* 页面头部 */
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-8);
    padding: var(--spacing-6);
    background: white;
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    border: 2px solid var(--secondary-200);
}

.header-left {
    display: flex;
    align-items: center;
    gap: var(--spacing-4);
}

.back-btn {
    color: var(--secondary-600);
    font-size: 18px;
    padding: var(--spacing-2);
    border-radius: var(--radius-lg);
    transition: var(--transition-all);
}

.back-btn:hover {
    background: var(--secondary-100);
    color: var(--secondary-700);
}

.page-title h1 {
    margin: 0;
    font-size: var(--text-2xl);
    font-weight: var(--font-bold);
    color: var(--deep-600);
}

.page-title p {
    margin: var(--spacing-1) 0 0;
    font-size: var(--text-sm);
    color: var(--gray-600);
}

.header-actions {
    display: flex;
    gap: var(--spacing-3);
}

.header-actions :deep(.ant-btn) {
    border-radius: var(--radius-lg);
    font-weight: var(--font-medium);
    transition: var(--transition-all);
}

.header-actions :deep(.ant-btn-primary) {
    background: var(--primary-600);
    border-color: var(--primary-600);
}

.header-actions :deep(.ant-btn-primary:hover) {
    background: var(--primary-700);
    border-color: var(--primary-700);
    transform: translateY(-1px);
    box-shadow: var(--shadow-md);
}

/* 编辑内容 */
.edit-content {
    margin-bottom: var(--spacing-8);
}

.edit-form-card {
    border: 2px solid var(--secondary-200);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    overflow: hidden;
}

.edit-form-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, var(--primary-600), var(--accent-500), var(--secondary-600));
    z-index: 1;
}

.edit-form-card :deep(.ant-card-body) {
    padding: var(--spacing-8);
}

/* 表单区域 */
.form-section {
    margin-bottom: var(--spacing-8);
}

.form-section:last-child {
    margin-bottom: 0;
}

.section-title {
    font-size: var(--text-xl);
    font-weight: var(--font-semibold);
    color: var(--deep-600);
    margin: 0 0 var(--spacing-6);
    padding-bottom: var(--spacing-3);
    border-bottom: 2px solid var(--secondary-200);
}

.field-tip {
    font-size: var(--text-xs);
    color: var(--gray-500);
    margin-top: var(--spacing-1);
}

/* 表单样式覆盖 */
.edit-form-card :deep(.ant-form-item-label label) {
    font-weight: var(--font-medium);
    color: var(--gray-700);
}

.edit-form-card :deep(.ant-input) {
    border-radius: var(--radius-lg);
    border: 2px solid var(--secondary-200);
    transition: var(--transition-all);
}

.edit-form-card :deep(.ant-input:focus) {
    border-color: var(--primary-500);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.edit-form-card :deep(.ant-select .ant-select-selector) {
    border-radius: var(--radius-lg) !important;
    border: 2px solid var(--secondary-200) !important;
}

.edit-form-card :deep(.ant-select:not(.ant-select-disabled):hover .ant-select-selector) {
    border-color: var(--primary-500) !important;
}

.edit-form-card :deep(.ant-input-number) {
    width: 100%;
    border-radius: var(--radius-lg);
    border: 2px solid var(--secondary-200);
}

.edit-form-card :deep(.ant-input-number:hover) {
    border-color: var(--primary-500);
}

.edit-form-card :deep(.ant-input-number-focused) {
    border-color: var(--primary-500);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 作者信息 */
.author-info {
    display: flex;
    align-items: center;
    gap: var(--spacing-2);
    font-size: var(--text-sm);
    color: var(--gray-700);
}

/* 应用预览 */
.app-preview {
    max-width: 400px;
    margin: 0 auto;
}

/* 标签样式 */
.edit-form-card :deep(.ant-tag) {
    border-radius: var(--radius-md);
    padding: var(--spacing-1) var(--spacing-2);
    font-size: var(--text-sm);
    display: inline-flex;
    align-items: center;
    gap: var(--spacing-1);
}

/* 加载状态 */
.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: var(--spacing-16);
    background: white;
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    color: var(--gray-600);
}

.loading-container p {
    margin-top: var(--spacing-4);
    font-size: var(--text-base);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .edit-container {
        padding: var(--spacing-4);
    }

    .page-header {
        flex-direction: column;
        gap: var(--spacing-4);
        align-items: stretch;
    }

    .header-left {
        justify-content: space-between;
    }

    .header-actions {
        width: 100%;
        justify-content: stretch;
    }

    .header-actions :deep(.ant-btn) {
        flex: 1;
    }

    .edit-form-card :deep(.ant-card-body) {
        padding: var(--spacing-4);
    }

    .app-preview {
        max-width: 100%;
    }
}

@media (max-width: 480px) {
    .edit-container {
        padding: var(--spacing-3);
    }

    .page-header {
        padding: var(--spacing-4);
    }

    .page-title h1 {
        font-size: var(--text-xl);
    }

    .section-title {
        font-size: var(--text-lg);
    }
}
</style>
