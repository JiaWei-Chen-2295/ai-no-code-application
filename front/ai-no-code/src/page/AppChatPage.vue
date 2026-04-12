<template>
    <div id="appChatPage">
        <!-- 顶部栏 -->
        <div class="chat-header">
            <div class="header-left">
                <a-button type="text" @click="goBack" class="back-btn">
                    <Icon icon="mdi:arrow-left" class="back-icon" />
                </a-button>
                <div class="app-info">
                    <h2 class="app-name">
                        <Icon :icon="app?.codeGenType === 'html' ? 'mdi:file-code-outline' : (app?.codeGenType === 'vueProject' ? 'mdi:vuejs' : 'mdi:folder-multiple-outline')" class="app-icon" />
                        {{ app?.appName || '加载中...' }}
                    </h2>
                    <span class="app-type">
                        <Icon icon="mdi:tag-outline" class="type-icon" />
                        {{ getCodeGenTypeLabel(app?.codeGenType) }}
                    </span>
                </div>
            </div>
            <div class="header-right">
                <div class="version-selector" v-if="versions.length > 0">
                    <a-select v-model:value="selectedVersion" @change="handleVersionChange" style="width: 140px">
                        <a-select-option value="latest">
                            <Icon icon="mdi:update" class="version-icon" />
                            <span>最新版本</span>
                        </a-select-option>
                        <a-select-option v-for="version in versions" :key="version.version" :value="version.version">
                            <Icon icon="mdi:source-branch" class="version-icon" />
                            <span>版本 {{ version.version }}</span>
                        </a-select-option>
                    </a-select>
                </div>
                <a-button type="primary" @click="deployApp" :loading="deploying" :disabled="!app || !isGenerated"
                    class="deploy-btn">
                    <Icon icon="mdi:cloud-upload-outline" class="deploy-icon" />
                    <span>部署应用</span>
                </a-button>
            </div>
        </div>

        <!-- 主内容区域 -->
        <div class="chat-content">
            <!-- 左侧对话区域 -->
            <div class="chat-panel">
                <div class="messages-container" ref="messagesContainer">
                    <!-- 加载更多按钮 -->
                    <div v-if="hasMoreHistory" class="load-more-container">
                        <a-button @click="loadMoreHistory" :loading="loadingHistory" class="load-more-btn">
                            <Icon icon="mdi:chevron-up" class="load-icon" />
                            <span>加载更多历史消息</span>
                        </a-button>
                    </div>
                    
                    <!-- 消息列表 -->
                    <div v-for="(message, index) in messages" :key="message.id || index" class="message-item"
                        :class="{ 'user-message': message.role === 'user', 'ai-message': message.role === 'assistant' }">
                        <div class="message-avatar">
                            <div v-if="message.role === 'user'" class="user-avatar">
                                <Icon icon="mdi:account" />
                            </div>
                            <div v-else class="ai-avatar">
                                <Icon icon="mdi:robot-outline" />
                            </div>
                        </div>
                        <div class="message-content">
                            <div class="message-text">
                                <div v-html="renderContent(message.content)" class="markdown-content"></div>
                                <!-- 流式输出光标 -->
                                <span v-if="isTyping && index === typingMessageIndex" class="typing-cursor">|</span>
                            </div>
                            <div class="message-time">{{ formatTime(message.timestamp || (message.createTime ? new Date(message.createTime) : new Date())) }}</div>
                        </div>
                    </div>

                    <!-- 正在输入指示器 -->
                    <div v-if="isStreaming" class="message-item ai-message">
                        <div class="message-avatar">
                            <div class="ai-avatar">
                                <Icon icon="mdi:robot-outline" />
                            </div>
                        </div>
                        <div class="message-content">
                            <div class="typing-indicator">
                                <span></span>
                                <span></span>
                                <span></span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 用户输入区域 -->
                <div class="input-container">
                    <div class="input-wrapper">
                        <a-textarea v-model:value="currentMessage" :rows="1" :auto-size="{ minRows: 1, maxRows: 4 }"
                            placeholder="继续优化你的应用..." class="message-input" @keydown.enter.prevent="handleEnterKey"
                            :disabled="isStreaming" />
                        <a-button type="primary" @click="sendMessage" :loading="isStreaming"
                            :disabled="!currentMessage.trim()" class="send-btn">
                            <Icon icon="mdi:send" class="send-icon" />
                        </a-button>
                    </div>
                </div>
            </div>

            <!-- 右侧预览区域 -->
            <div class="preview-panel">
                <div class="preview-header">
                    <h3>生成预览</h3>
                    <div class="preview-actions">
                        <a-button v-if="previewUrl" @click="openInNewTab" size="small">
                            <Icon icon="mdi:open-in-new" class="action-icon" />
                            <span>新窗口打开</span>
                        </a-button>
                        <a-button @click="refreshPreview" size="small" :loading="refreshing">
                            <Icon icon="mdi:refresh" class="action-icon" />
                            <span>刷新</span>
                        </a-button>
                    </div>
                </div>
                <div class="preview-content">
                    <div v-if="!isGenerated && messages.length === 0" class="preview-placeholder">
                        <div class="placeholder-content">
                            <Icon icon="mdi:palette-outline" class="placeholder-icon" />
                            <h4>开始对话，生成你的应用</h4>
                            <p>在左侧输入你的需求，AI 将为你生成应用代码</p>
                        </div>
                    </div>
                    <div v-else-if="isStreaming" class="preview-loading">
                        <a-spin size="large" />
                        <p>AI 正在生成中...</p>
                    </div>
                    <div v-else-if="previewUrl" class="preview-iframe-container">
                        <iframe :src="previewUrl" class="preview-iframe" ref="previewIframe"
                            @load="onIframeLoad"></iframe>
                    </div>
                    <div v-else class="preview-error">
                        <div class="error-content">
                            <Icon icon="mdi:alert-circle-outline" class="error-icon" />
                            <h4>预览暂不可用</h4>
                            <p v-if="messages.length < 2">请与 AI 对话至少 2 轮生成应用代码</p>
                            <p v-else>请继续与 AI 对话优化你的应用</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import { Icon } from '@iconify/vue'
import { getAppVoById, deployApp as deployAppApi, getAppVersions } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { useUserStore } from '@/stores/userStore'

interface Message {
    id?: number
    role: 'user' | 'assistant'
    content: string
    timestamp?: Date
    createTime?: string
    messageType?: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const app = ref<API.AppVO>()
const messages = ref<Message[]>([])
const currentMessage = ref('')
const isStreaming = ref(false)
const isGenerated = ref(false)
const deploying = ref(false)
const refreshing = ref(false)
const previewUrl = ref('')
const messagesContainer = ref<HTMLElement>()
const previewIframe = ref<HTMLIFrameElement>()

// 新增的响应式数据
const loadingHistory = ref(false)
const hasMoreHistory = ref(false)
const lastCreateTime = ref<string | undefined>()
const historyLoaded = ref(false)
const versions = ref<API.AppVersionVO[]>([])
const selectedVersion = ref<string | number>('latest')

// 流式输出相关状态
const isTyping = ref(false) // 正在打字效果中
const typingMessageIndex = ref(-1) // 当前正在打字的消息索引
const typingBuffer = ref('') // 待输出的完整内容
const typingTimer = ref<number | null>(null) // 打字计时器
const typingSpeed = ref(30) // 打字速度(ms)，越小越快

// 全局函数：复制代码到剪贴板
const setupGlobalFunctions = () => {
    // 复制代码功能
    (window as unknown as { copyCodeToClipboard: (button: HTMLElement) => Promise<void> | void }).copyCodeToClipboard = async (button: HTMLElement) => {
        try {
            const code = button.getAttribute('data-code') || ''
            // 解码HTML实体
            const decodedCode = code
                .replace(/&amp;/g, "&")
                .replace(/&lt;/g, "<")
                .replace(/&gt;/g, ">")
                .replace(/&quot;/g, '"')
                .replace(/&#039;/g, "'");
            
            await navigator.clipboard.writeText(decodedCode)
            
            // 视觉反馈
            const originalText = button.innerHTML
            button.innerHTML = '<span class="copy-icon">✅</span>'
            button.style.color = '#22c55e'
            
            setTimeout(() => {
                button.innerHTML = originalText
                button.style.color = ''
            }, 2000)
        } catch (err) {
            console.error('复制失败:', err)
            // 降级方案：选中文本
            const code = button.getAttribute('data-code') || ''
            const textArea = document.createElement('textarea')
            textArea.value = code
            document.body.appendChild(textArea)
            textArea.select()
            document.execCommand('copy')
            document.body.removeChild(textArea)
        }
    }
    
    // 折叠代码块功能
    (window as unknown as { toggleCodeBlock: (button: HTMLElement) => void }).toggleCodeBlock = (button: HTMLElement) => {
        const wrapper = button.closest('.code-block-wrapper')
        if (wrapper) {
            wrapper.classList.toggle('collapsed')
        }
    }
}

// 页面初始化
onMounted(async () => {
    setupGlobalFunctions()
    await userStore.fetchLoginUser()
    await loadApp()
    await loadVersions()
    await loadChatHistory()
})

// 组件卸载时清理
onUnmounted(() => {
    clearTypingEffect()
})

// 监听消息变化，自动滚动到底部
watch(messages, () => {
    nextTick(() => {
        scrollToBottom()
    })
}, { deep: true })

// 加载应用信息
const loadApp = async () => {
    const id = route.params.id as string
    if (!id) {
        message.error('应用ID无效')
        router.push('/')
        return
    }

    try {
        const res = await getAppVoById({ id: id })
        if (res.data.code === 0) {
            app.value = res.data.data
            updatePreviewUrl()
        } else {
            message.error('加载应用信息失败：' + res.data.message)
        }
    } catch {
        message.error('加载应用信息失败')
    }
}

// 检查是否应该自动发送初始消息
const checkAndSendInitialMessage = async () => {
    if (!app.value?.initPrompt) return
    
    // 只有是自己的app，并且没有对话历史才自动发送
    const isMyApp = userStore.loginUser?.id === app.value.userId
    const hasNoHistory = messages.value.length === 0
    
    if (isMyApp && hasNoHistory) {
        await sendInitialMessage()
    }
}

// 发送初始消息
const sendInitialMessage = async () => {
    if (!app.value?.initPrompt) return

    // 添加用户消息
    messages.value.push({
        role: 'user',
        content: app.value.initPrompt,
        timestamp: new Date()
    })

    await streamChat(app.value.initPrompt)
}

// 发送消息
const sendMessage = async () => {
    if (!currentMessage.value.trim() || isStreaming.value) return

    const messageText = currentMessage.value.trim()
    currentMessage.value = ''

    // 添加用户消息
    messages.value.push({
        role: 'user',
        content: messageText,
        timestamp: new Date()
    })

    await streamChat(messageText)
}

// 清理打字效果
const clearTypingEffect = () => {
    if (typingTimer.value) {
        clearInterval(typingTimer.value)
        typingTimer.value = null
    }
    isTyping.value = false
    typingMessageIndex.value = -1
    typingBuffer.value = ''
}

// 更新打字缓冲区内容
const updateTypingBuffer = (messageIndex: number, newContent: string) => {
    typingBuffer.value = newContent

    // 如果还没开始打字，则开始打字效果
    if (!isTyping.value) {
        startTypingEffect(messageIndex)
    }
}

// 开始打字效果
const startTypingEffect = (messageIndex: number) => {
    isTyping.value = true
    typingMessageIndex.value = messageIndex

    let currentIndex = 0
    const message = messages.value[messageIndex]
    message.content = ''

    const typeNextChar = () => {
        const fullContent = typingBuffer.value
        if (currentIndex < fullContent.length) {
            message.content = fullContent.substring(0, currentIndex + 1)
            currentIndex++

            // 自动滚动到底部
            nextTick(() => {
                scrollToBottom()
            })
        }

        // 如果已经显示完当前的内容，检查是否还有更多内容或流是否已结束
        if (currentIndex >= fullContent.length) {
            if (!isStreaming.value) {
                // 流已结束，打字完成
                clearTypingEffect()
            }
            // 如果流还在进行，计时器会继续运行，等待新内容
        }
    }

    // 设置打字间隔（可以根据需要调整速度）
    typingTimer.value = setInterval(typeNextChar, typingSpeed.value) as unknown as number
}

// 流式对话
const streamChat = async (userMessage: string) => {
    if (!app.value?.id) return

    isStreaming.value = true
    let timeout: number | null = null

    try {
        // 这里使用SSE接口进行流式对话
        const eventSource = new EventSource(
            `/api/app/chat/gen/code?appId=${app.value.id}&message=${encodeURIComponent(userMessage)}`
        )

        let aiMessage = ''
        const aiMessageObj: Message = {
            role: 'assistant',
            content: '',
            timestamp: new Date()
        }

        messages.value.push(aiMessageObj)
        const messageIndex = messages.value.length - 1

        // 设置超时处理，防止长时间无响应
        timeout = setTimeout(() => {
            if (!isGenerated.value && eventSource.readyState !== EventSource.CLOSED) {
                eventSource.close()
                isStreaming.value = false
                aiMessageObj.content += '\n\n[⏰ 响应超时]'
                message.error('AI响应超时，请重试')
            }
        }, 60000) as unknown as number // 60秒超时

        eventSource.onmessage = (event) => {
            const data = event.data

            // 检查是否是结束标志
            if (data === '__DONE__') {
                if (timeout) clearTimeout(timeout)
                eventSource.close()
                isStreaming.value = false
                isGenerated.value = true
                // 确保打字效果完成
                if (isTyping.value && aiMessage) {
                    aiMessageObj.content = aiMessage
                    clearTypingEffect()
                }
                // 重新加载版本列表，获取最新版本
                loadVersions().then(() => {
                    updatePreviewUrl()
                })
                // 延迟刷新封面截图
                setTimeout(async () => {
                    try {
                        const res = await getAppVoById({ id: app.value.id })
                        if (res.data.code === 0 && res.data.data?.cover) {
                            app.value.cover = res.data.data.cover + '?t=' + Date.now()
                        }
                    } catch {
                        // 静默失败
                    }
                }, 5000)
                return
            }

            try {
                // 解析SSE数据格式：{"d":"文本内容"} 或 {"error":true,"code":...,"message":"..."}
                const chunk = JSON.parse(data)
                if (chunk.error === true) {
                    // 守卫/业务错误帧：显示错误消息并等待 __DONE__ 关闭
                    if (timeout) clearTimeout(timeout)
                    clearTypingEffect()
                    messages.value.splice(messageIndex, 1) // 移除空的 AI 消息占位
                    message.error(chunk.message || '请求被拒绝，请修改后重试')
                    isStreaming.value = false
                    return
                }
                if (chunk.d) {
                    aiMessage += chunk.d
                    // 更新打字缓冲区内容
                    updateTypingBuffer(messageIndex, aiMessage)
                }
            } catch {
                // 处理非JSON数据或解析失败的情况
                console.warn('SSE数据解析失败:', data)
            }
        }

        eventSource.onerror = (err) => {
            if (timeout) clearTimeout(timeout)
            eventSource.close()
            isStreaming.value = false

            // 清理打字效果并显示已收到的内容
            if (isTyping.value && aiMessage) {
                aiMessageObj.content = aiMessage
                clearTypingEffect()
            }

            // 根据已收到的内容和生成状态区分错误类型
            if (isGenerated.value) {
                // 已经标记为生成完成，可能是正常结束后的断开
                return
            }
            
            if (aiMessageObj.content) {
                // 收到了部分内容后断开
                aiMessageObj.content += '\n\n> ⚠️ **连接中断**：内容可能不完整，请重新发送消息继续生成。'
                message.warning('AI对话连接中断，内容可能不完整')
            } else {
                // 完全没收到内容
                aiMessageObj.content = '> ❌ **连接失败**：无法连接到AI服务，请检查网络后重试。'
                message.error('AI对话连接失败，请重试')
            }
        }

    } catch {
        if (timeout) clearTimeout(timeout)
        isStreaming.value = false
        clearTypingEffect()
        message.error('发送消息失败')
    }
}

// 处理回车键
const handleEnterKey = (e: KeyboardEvent) => {
    if (e.shiftKey) {
        // Shift+Enter 换行
        return
    }
    // Enter 发送
    sendMessage()
}

// 部署应用
const deployApp = async () => {
    if (!app.value?.id) return

    deploying.value = true
    try {
        const res = await deployAppApi({ appId: app.value.id!.toString() })
        if (res.data.code === 0) {
            message.success('部署成功！')
            message.info(`访问地址：${res.data.data}`)
            // 重新加载应用信息以获取最新的部署状态
            await loadApp()
        } else {
            const errorMsg = res.data.message || '未知错误'
            // 如果是构建失败，显示详细错误信息
            if (errorMsg.includes('构建失败')) {
                message.error({
                    content: '部署失败：项目构建出错，请继续与AI对话修复代码后重试',
                    duration: 6
                })
                // 将构建错误添加为AI消息，方便用户看到
                // 转义 errorMsg 中的 HTML 特殊字符，防止 XSS
                const safeErrorMsg = errorMsg
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;')
                    .replace(/'/g, '&#039;')
                messages.value.push({
                    role: 'assistant',
                    content: `> ❌ **部署失败 - 构建错误**\n\n${safeErrorMsg}\n\n> 💡 请描述问题或发送"修复构建错误"让AI帮你解决`,
                    timestamp: new Date()
                })
            } else {
                message.error('部署失败：' + errorMsg)
            }
        }
    } catch {
        message.error('部署失败，请检查网络后重试')
    } finally {
        deploying.value = false
    }
}

// 更新预览URL
const updatePreviewUrl = () => {
    if (!app.value?.id) return
    
    // 如果有至少2条对话记录或者已经生成了代码，才显示预览
    const hasEnoughHistory = messages.value.length >= 2
    if (!isGenerated.value && !hasEnoughHistory) return

    const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
    const isVueProject = app.value.codeGenType === 'vueProject'

    if (selectedVersion.value === 'latest') {
        // 获取最新版本号
        const latestVersion = getLatestVersion()
        if (latestVersion) {
            previewUrl.value = `${baseUrl}/api/static/preview/${app.value.id}/${latestVersion}/`
        } else {
            // latest 但还没有版本号
            // 对于 vue 项目，必须带版本号，默认 1
            previewUrl.value = `${baseUrl}/api/static/preview/${app.value.id}/${isVueProject ? 1 : ''}${isVueProject ? '/' : ''}`
        }
    } else {
        previewUrl.value = `${baseUrl}/api/static/preview/${app.value.id}/${selectedVersion.value}/`
    }
}

// 获取最新版本号
const getLatestVersion = () => {
    if (versions.value.length === 0) return null
    
    // 版本列表应该已经按版本号排序，第一个就是最新的
    // 如果需要确保获取最大版本号，可以进行排序
    const sortedVersions = [...versions.value].sort((a, b) => {
        return (b.version || 0) - (a.version || 0)
    })
    
    return sortedVersions[0]?.version || null
}

// 刷新预览
const refreshPreview = () => {
    if (!previewIframe.value) return

    refreshing.value = true
    previewIframe.value.onload = () => {
        refreshing.value = false
    }
    previewIframe.value.src = previewIframe.value.src
}

// 在新窗口打开
const openInNewTab = () => {
    if (previewUrl.value) {
        const url = previewUrl.value.startsWith('http')
            ? previewUrl.value
            : window.location.origin + previewUrl.value
        window.open(url, '_blank')
    }
}

// iframe加载完成
const onIframeLoad = () => {
    refreshing.value = false
}

// 返回上一页
const goBack = () => {
    router.go(-1)
}

// 滚动到底部
const scrollToBottom = () => {
    if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
}

// 渲染内容（根据应用类型选择渲染方式）
const renderContent = (content: string) => {
    if (!content) return ''
    
    const codeGenType = app.value?.codeGenType
    if (codeGenType === 'vueProject') {
        return renderVueProjectContent(content)
    }
    
    // HTML单文件类型：检测完整HTML代码
    if (codeGenType === 'html') {
        if (content.includes('<!DOCTYPE') || content.includes('<html')) {
            // 这是完整的HTML代码，使用代码块展示（不执行）
            return renderSafeHtmlCodeBlock(content)
        }
    }
    
    // 多文件类型：使用安全的Markdown渲染
    if (codeGenType === 'mutiFile') {
        return renderSafeMarkdown(content)
    }
    
    // 其他情况也使用安全的Markdown渲染
    return renderSafeMarkdown(content)
}

// 安全渲染HTML代码块（防止执行）
const renderSafeHtmlCodeBlock = (htmlContent: string) => {
    // HTML转义函数
    const escapeHtml = (unsafe: string) => {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
    
    try {
        const highlighted = hljs.highlight(htmlContent, { language: 'html' }).value
        const lineCount = htmlContent.split('\n').length
        const sizeKB = Math.round(htmlContent.length / 1024 * 10) / 10
        const escapedForAttribute = escapeHtml(htmlContent)
        
        return `<div class="code-block-wrapper streaming-code">
            <div class="code-block-header">
                <div class="header-left">
                    <span class="code-language">HTML</span>
                    <span class="line-count">${lineCount} 行</span>
                    ${htmlContent.length > 500 ? `<span class="size-indicator">${sizeKB}KB</span>` : ''}
                </div>
                <div class="header-actions">
                    <button class="code-copy-btn" onclick="copyCodeToClipboard(this)" data-code="${escapedForAttribute}" title="复制代码">
                        <span class="copy-icon">📋</span>
                    </button>
                    <button class="code-toggle-btn" onclick="toggleCodeBlock(this)" title="折叠/展开">
                        <span class="toggle-icon">▼</span>
                    </button>
                </div>
            </div>
            <div class="code-block-content">
                <pre><code class="hljs language-html">${highlighted}</code></pre>
            </div>
        </div>`
    } catch {
        const escapedContent = escapeHtml(htmlContent)
        return `<div class="simple-code-block">
            <div class="simple-header">
                <span>HTML代码</span>
                <button onclick="copyCodeToClipboard(this)" data-code="${escapedContent}">复制</button>
            </div>
            <pre><code>${escapedContent}</code></pre>
        </div>`
    }
}

// 安全渲染Markdown内容（防止代码执行）
const renderSafeMarkdown = (content: string): string => {
    if (!content) return ''
    
    // 配置marked选项
    marked.setOptions({
        breaks: true,
        gfm: true
    })
    
    // 自定义渲染器
    const renderer = new marked.Renderer()
    
    // 安全渲染代码块
    renderer.code = function({ text, lang }: { text: string, lang?: string }) {
        const lineCount = text.split('\n').length
        const isLargeCode = lineCount > 20 || text.length > 1000
        const sizeKB = Math.round(text.length / 1024 * 10) / 10
        
        // HTML转义函数
        const escapeHtml = (unsafe: string) => {
            return unsafe
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }
        
        const escapedText = escapeHtml(text)
        
        // 使用highlight.js进行语法高亮
        if (lang && hljs.getLanguage(lang)) {
            try {
                const highlighted = hljs.highlight(text, { language: lang }).value
                return `<div class="code-block-wrapper ${isLargeCode ? 'large-code' : ''}">
                    <div class="code-block-header">
                        <div class="header-left">
                            <span class="code-language">${lang.toUpperCase()}</span>
                            <span class="line-count">${lineCount} 行</span>
                            ${text.length > 500 ? `<span class="size-indicator">${sizeKB}KB</span>` : ''}
                        </div>
                        <div class="header-actions">
                            <button class="code-copy-btn" onclick="copyCodeToClipboard(this)" data-code="${escapedText}" title="复制代码">
                                <span class="copy-icon">📋</span>
                            </button>
                            <button class="code-toggle-btn" onclick="toggleCodeBlock(this)" title="折叠/展开">
                                <span class="toggle-icon">▼</span>
                            </button>
                        </div>
                    </div>
                    <div class="code-block-content">
                        <pre><code class="hljs language-${lang}">${highlighted}</code></pre>
                    </div>
                </div>`
            } catch {
                return `<div class="simple-code-block"><pre><code>${escapedText}</code></pre></div>`
            }
        }
        
        // 自动检测语言
        try {
            const highlighted = hljs.highlightAuto(text).value
            const detectedLang = hljs.highlightAuto(text).language || '代码'
            return `<div class="code-block-wrapper ${isLargeCode ? 'large-code' : ''}">
                <div class="code-block-header">
                    <div class="header-left">
                        <span class="code-language">${detectedLang.toUpperCase()}</span>
                        <span class="line-count">${lineCount} 行</span>
                        ${text.length > 500 ? `<span class="size-indicator">${sizeKB}KB</span>` : ''}
                    </div>
                    <div class="header-actions">
                        <button class="code-copy-btn" onclick="copyCodeToClipboard(this)" data-code="${escapedText}" title="复制代码">
                            <span class="copy-icon">📋</span>
                        </button>
                        <button class="code-toggle-btn" onclick="toggleCodeBlock(this)" title="折叠/展开">
                            <span class="toggle-icon">▼</span>
                        </button>
                    </div>
                </div>
                <div class="code-block-content">
                    <pre><code class="hljs">${highlighted}</code></pre>
                </div>
            </div>`
        } catch {
            return `<div class="simple-code-block"><pre><code>${escapedText}</code></pre></div>`
        }
    }
    
    const rawHtml = marked(content, { renderer }) as unknown as string
    return DOMPurify.sanitize(rawHtml)
}

// 格式化时间
const formatTime = (time: Date) => {
    return time.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
    })
}

// 加载对话历史
const loadChatHistory = async () => {
    if (!app.value?.id || historyLoaded.value) return
    
    loadingHistory.value = true
    try {
        const res = await listAppChatHistory({
            appId: app.value.id!.toString(),
            pageSize: 10,
            lastCreateTime: lastCreateTime.value
        })
        
        if (res.data.code === 0 && res.data.data?.records) {
            const historyMessages = res.data.data.records.map(record => ({
                id: record.id,
                role: record.messageType === 'user' ? 'user' : 'assistant',
                content: record.message || '',
                createTime: record.createTime,
                messageType: record.messageType
            } as Message))
            
            // 按时间升序排列，历史消息插入到现有消息前面
            messages.value = [...historyMessages.reverse(), ...messages.value]
            
            // 更新分页信息
            hasMoreHistory.value = res.data.data.records.length === 10
            if (res.data.data.records.length > 0) {
                lastCreateTime.value = res.data.data.records[res.data.data.records.length - 1].createTime
            }
            
            // 如果有对话历史，说明之前生成过代码
            if (historyMessages.length > 0) {
                isGenerated.value = true
            }
        }
        
        historyLoaded.value = true
        
        // 检查是否需要自动发送初始消息
        await checkAndSendInitialMessage()
        
        // 更新预览URL
        updatePreviewUrl()
        
    } catch (error) {
        console.error('加载对话历史失败:', error)
        message.error('加载对话历史失败')
        historyLoaded.value = true
        await checkAndSendInitialMessage()
    } finally {
        loadingHistory.value = false
    }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
    if (!app.value?.id || loadingHistory.value || !hasMoreHistory.value) return
    
    loadingHistory.value = true
    try {
        const res = await listAppChatHistory({
            appId: app.value.id!.toString(),
            pageSize: 10,
            lastCreateTime: lastCreateTime.value
        })
        
        if (res.data.code === 0 && res.data.data?.records) {
            const historyMessages = res.data.data.records.map(record => ({
                id: record.id,
                role: record.messageType === 'user' ? 'user' : 'assistant',
                content: record.message || '',
                createTime: record.createTime,
                messageType: record.messageType
            } as Message))
            
            // 将新的历史消息插入到消息列表前面
            messages.value = [...historyMessages.reverse(), ...messages.value]
            
            // 更新分页信息
            hasMoreHistory.value = res.data.data.records.length === 10
            if (res.data.data.records.length > 0) {
                lastCreateTime.value = res.data.data.records[res.data.data.records.length - 1].createTime
            }
        }
    } catch (error) {
        console.error('加载更多历史失败:', error)
        message.error('加载更多历史失败')
    } finally {
        loadingHistory.value = false
    }
}

// 加载版本列表
const loadVersions = async () => {
    if (!app.value?.id) return
    
    try {
        const res = await getAppVersions({ appId: app.value.id!.toString() })
        if (res.data.code === 0 && res.data.data) {
            versions.value = res.data.data
            // 加载版本后，如果当前是latest，更新预览URL以使用最新版本号
            if (selectedVersion.value === 'latest' && versions.value.length > 0) {
                updatePreviewUrl()
            }
        }
    } catch (error) {
        console.error('加载版本列表失败:', error)
    }
}

// 处理版本变化
const handleVersionChange = (value: string | number) => {
    selectedVersion.value = value
    updatePreviewUrl()
    refreshPreview()
}

// 获取代码生成类型标签
const getCodeGenTypeLabel = (type?: string) => {
    const typeMap: Record<string, string> = {
        'html': '单文件网页',
        'mutiFile': '多文件',
        'vueProject': 'Vue 项目'
    }
    return typeMap[type || ''] || type || '未知'
}

// 渲染 Vue 项目的内容：将工具调用渲染为卡片
const renderVueProjectContent = (content: string) => {
    if (!content) return ''

    const escapeHtml = (unsafe: string) => unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;")

    const toolCallRegex = /\[工具调用\]\s*(.+?)\s*\n```([\s\S]*?)```/g
    let match: RegExpExecArray | null
    let lastIndex = 0
    const htmlParts: string[] = []

    while ((match = toolCallRegex.exec(content)) !== null) {
        const beforeText = content.slice(lastIndex, match.index)
        if (beforeText.trim()) {
            htmlParts.push(renderSafeMarkdown(beforeText))
        }

        const header = match[1].trim() // 例："写入文件 src/pages"
        const body = match[2].trim() // 例："null" 或 代码内容

        let action = header
        let target = ''
        const headerParts = header.match(/^(\S+)\s+(.+)$/)
        if (headerParts) {
            action = headerParts[1]
            target = headerParts[2]
        }

        const hasContent = body && body.toLowerCase() !== 'null'
        const bodyEscaped = hasContent ? escapeHtml(body) : ''

        const cardHtml = `
<div class="tool-call-card">
  <div class="tool-call-header">
    <span class="tool-call-icon">🛠️</span>
    <span class="tool-call-title">工具调用</span>
    <span class="tool-call-action">${escapeHtml(action)}</span>
  </div>
  <div class="tool-call-meta">
    <span class="tool-call-label">目标</span>
    <span class="tool-call-target">${escapeHtml(target)}</span>
  </div>
  <div class="tool-call-body">
    ${hasContent ? `<pre><code>${bodyEscaped}</code></pre>` : `<div class="tool-call-empty">无内容</div>`}
  </div>
</div>`

        htmlParts.push(cardHtml)
        lastIndex = toolCallRegex.lastIndex
    }

    const remaining = content.slice(lastIndex)
    if (remaining.trim()) {
        htmlParts.push(renderSafeMarkdown(remaining))
    }

    return htmlParts.join('')
}
</script>

<style scoped>
/* Claude Design System Variables */
#appChatPage {
    --claude-bg: #f5f4ed; /* Parchment */
    --claude-surface: #faf9f5; /* Ivory */
    --claude-surface-white: #ffffff; /* Pure White */
    --claude-surface-sand: #e8e6dc; /* Warm Sand */
    --claude-brand: #c96442; /* Terracotta Brand */
    --claude-text-primary: #141413; /* Anthropic Near Black */
    --claude-text-secondary: #5e5d59; /* Olive Gray */
    --claude-text-tertiary: #87867f; /* Stone Gray */
    --claude-text-button: #4d4c48; /* Charcoal Warm */
    --claude-border-light: #f0eee6; /* Border Cream */
    --claude-border-warm: #e8e6dc; /* Border Warm */
    --claude-ring-warm: #d1cfc5;
    --claude-focus-blue: #3898ec;
    
    --font-serif: 'Georgia', serif;
    --font-sans: system-ui, -apple-system, sans-serif;
    --font-mono: 'JetBrains Mono', 'Fira Code', monospace;

    height: 100vh;
    display: flex;
    flex-direction: column;
    background: var(--claude-bg);
    font-family: var(--font-sans);
    color: var(--claude-text-primary);
}

/* 顶部栏 - Claude 风格 */
.chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 32px;
    background: var(--claude-bg);
    border-bottom: 1px solid var(--claude-border-warm);
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.back-btn {
    color: var(--claude-text-secondary);
    font-size: 18px;
    padding: 8px;
    border-radius: 8px;
    transition: all 0.2s ease;
    background: transparent;
    border: none;
    cursor: pointer;
}

.back-btn:hover {
    background: var(--claude-surface-sand);
    color: var(--claude-text-primary);
}

.back-icon {
    font-size: 20px;
}

.app-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.app-name {
    margin: 0;
    font-size: 32px;
    font-family: var(--font-serif);
    font-weight: 500;
    line-height: 1.1;
    color: var(--claude-text-primary);
    display: flex;
    align-items: center;
    gap: 12px;
}

.app-icon {
    font-size: 28px;
    color: var(--claude-brand);
}

.app-type {
    font-size: 12px;
    color: var(--claude-text-secondary);
    background: var(--claude-surface);
    border: 1px solid var(--claude-border-light);
    padding: 2px 8px;
    border-radius: 6px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    width: fit-content;
    letter-spacing: 0.12px;
}

.type-icon {
    font-size: 14px;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 16px;
}

.version-selector {
    display: flex;
    align-items: center;
    gap: 8px;
}

.version-selector :deep(.ant-select .ant-select-selector) {
    border-radius: 8px !important;
    border: 1px solid var(--claude-border-warm) !important;
    background: var(--claude-surface-white) !important;
    color: var(--claude-text-primary) !important;
    box-shadow: none !important;
}

.version-selector :deep(.ant-select:focus .ant-select-selector),
.version-selector :deep(.ant-select-open .ant-select-selector) {
    border-color: var(--claude-focus-blue) !important;
    box-shadow: 0 0 0 1px var(--claude-focus-blue) !important;
}

.version-icon {
    font-size: 16px;
    margin-right: 4px;
    color: var(--claude-text-secondary);
}

.deploy-btn {
    background: var(--claude-surface-white) !important;
    border: 1px solid var(--claude-border-warm) !important;
    color: var(--claude-text-primary) !important;
    font-family: var(--font-sans) !important;
    font-weight: 400 !important;
    border-radius: 12px !important;
    padding: 8px 16px 8px 12px !important;
    display: flex !important;
    align-items: center !important;
    gap: 8px !important;
    transition: all 0.2s ease !important;
    box-shadow: 0 4px 24px rgba(0,0,0,0.02) !important;
}

.deploy-icon {
    font-size: 18px;
    color: var(--claude-brand);
}

.deploy-btn:hover {
    background: var(--claude-surface-sand) !important;
    box-shadow: 0 0 0 1px var(--claude-ring-warm) !important;
}

.deploy-btn:disabled {
    background: var(--claude-surface) !important;
    color: var(--claude-text-tertiary) !important;
    border-color: var(--claude-border-light) !important;
}

/* 主内容区域 */
.chat-content {
    flex: 1;
    display: flex;
    height: calc(100vh - 100px);
    background: var(--claude-bg);
}

/* 左侧对话面板 */
.chat-panel {
    width: 50%;
    display: flex;
    flex-direction: column;
    background: var(--claude-bg);
    border-right: 1px solid var(--claude-border-warm);
}

.messages-container {
    flex: 1;
    padding: 32px 48px;
    overflow-y: auto;
    scroll-behavior: smooth;
}

/* 加载更多按钮样式 */
.load-more-container {
    text-align: center;
    padding: 12px 0;
    margin-bottom: 24px;
}

.load-more-btn {
    background: var(--claude-surface) !important;
    border: 1px solid var(--claude-border-warm) !important;
    color: var(--claude-text-button) !important;
    border-radius: 8px !important;
    padding: 4px 12px !important;
    font-size: 14px !important;
    display: inline-flex !important;
    align-items: center !important;
    gap: 8px !important;
    transition: all 0.2s ease !important;
}

.load-more-btn:hover {
    background: var(--claude-surface-sand) !important;
    box-shadow: 0 0 0 1px var(--claude-ring-warm) !important;
}

/* 消息样式 */
.message-item {
    display: flex;
    gap: 20px;
    margin-bottom: 32px;
    max-width: 100%;
}

.user-message {
    flex-direction: row-reverse;
}

.message-avatar {
    flex-shrink: 0;
}

.user-avatar,
.ai-avatar {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 500;
}

.user-avatar {
    background: var(--claude-surface-sand);
    color: var(--claude-text-primary);
}

.ai-avatar {
    background: var(--claude-brand);
    color: var(--claude-surface-white);
    font-size: 18px;
}

.message-content {
    flex: 1;
    max-width: calc(100% - 52px);
}

.user-message .message-content {
    text-align: right;
}

.message-text {
    padding: 12px 0;
    line-height: 1.6;
    font-size: 16px;
    word-wrap: break-word;
    overflow-wrap: break-word;
    color: var(--claude-text-primary);
}

.user-message .message-text {
    background: var(--claude-surface-white);
    padding: 16px 24px;
    margin-left: auto;
    display: inline-block;
    max-width: 85%;
    border: 1px solid var(--claude-border-warm);
    border-radius: 12px;
    text-align: left;
    box-shadow: 0 4px 24px rgba(0,0,0,0.02);
}

.user-message .markdown-content {
    color: var(--claude-text-primary);
}

.ai-message .message-text {
    margin-right: auto;
    max-width: 100%;
    background: transparent;
}

.message-time {
    font-size: 12px;
    color: var(--claude-text-tertiary);
    margin-top: 8px;
}

/* 输入中指示器 */
.typing-indicator {
    padding: 12px 0;
    display: flex;
    gap: 6px;
    width: fit-content;
}

.typing-indicator span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--claude-text-tertiary);
    animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
    0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
    40% { transform: scale(1); opacity: 1; }
}

/* 输入区域 */
.input-container {
    padding: 24px 48px;
    background: var(--claude-bg);
}

.input-wrapper {
    display: flex;
    gap: 12px;
    align-items: flex-end;
    background: var(--claude-surface-white);
    border: 1px solid var(--claude-border-warm);
    border-radius: 12px;
    padding: 8px 12px;
    box-shadow: 0 4px 24px rgba(0,0,0,0.03);
    transition: all 0.2s ease;
}

.input-wrapper:focus-within {
    border-color: var(--claude-focus-blue);
    box-shadow: 0 0 0 1px var(--claude-focus-blue);
}

.message-input {
    flex: 1;
    border: none !important;
    border-radius: 8px !important;
    padding: 8px 4px !important;
    font-family: var(--font-sans) !important;
    font-size: 16px !important;
    line-height: 1.6 !important;
    color: var(--claude-text-primary) !important;
    background: transparent !important;
    box-shadow: none !important;
    resize: none !important;
}

.message-input::placeholder {
    color: var(--claude-text-tertiary);
}

.send-btn {
    background: var(--claude-brand) !important;
    border: none !important;
    color: var(--claude-surface-white) !important;
    border-radius: 8px !important;
    height: 36px !important;
    width: 36px !important;
    padding: 0 !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    transition: all 0.2s ease !important;
    margin-bottom: 2px;
}

.send-icon {
    font-size: 18px;
}

.send-btn:hover:not(:disabled) {
    background: #b85a3b !important; /* Slightly darker Terracotta */
    box-shadow: 0 0 0 1px var(--claude-brand) !important;
}

.send-btn:disabled {
    background: var(--claude-surface-sand) !important;
    color: var(--claude-text-tertiary) !important;
}

.load-icon,
.action-icon {
    font-size: 16px;
    margin-right: 6px;
}

/* 右侧预览面板 */
.preview-panel {
    width: 50%;
    display: flex;
    flex-direction: column;
    background: var(--claude-surface-white);
    margin: 24px 24px 24px 0;
    border-radius: 16px;
    border: 1px solid var(--claude-border-warm);
    box-shadow: 0 4px 24px rgba(0,0,0,0.02);
    overflow: hidden;
}

.preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    border-bottom: 1px solid var(--claude-border-light);
    background: var(--claude-surface);
}

.preview-header h3 {
    margin: 0;
    font-family: var(--font-serif);
    font-size: 20px;
    font-weight: 500;
    color: var(--claude-text-primary);
}

.preview-actions {
    display: flex;
    gap: 8px;
}

.preview-actions .ant-btn {
    background: var(--claude-surface-white) !important;
    border: 1px solid var(--claude-border-warm) !important;
    color: var(--claude-text-button) !important;
    border-radius: 8px !important;
    font-family: var(--font-sans) !important;
    transition: all 0.2s ease !important;
}

.preview-actions .ant-btn:hover {
    background: var(--claude-surface-sand) !important;
    box-shadow: 0 0 0 1px var(--claude-ring-warm) !important;
}

.preview-content {
    flex: 1;
    position: relative;
    overflow: hidden;
    background: var(--claude-surface-white);
}

/* 预览状态 */
.preview-placeholder,
.preview-loading,
.preview-error {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    color: var(--claude-text-secondary);
}

.placeholder-icon,
.error-icon {
    font-size: 48px;
    margin-bottom: 24px;
    color: var(--claude-text-tertiary);
    opacity: 0.6;
}

.preview-placeholder h4,
.preview-error h4 {
    font-family: var(--font-serif);
    font-size: 25px;
    font-weight: 500;
    margin: 0 0 12px;
    color: var(--claude-text-primary);
}

.preview-placeholder p,
.preview-error p {
    font-size: 16px;
    line-height: 1.6;
    margin: 0;
    color: var(--claude-text-secondary);
}

.preview-loading p {
    margin-top: 24px;
    font-size: 16px;
    color: var(--claude-text-secondary);
}

/* iframe容器 */
.preview-iframe-container {
    height: 100%;
    width: 100%;
}

.preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
    background: var(--claude-surface-white);
}

/* 响应式设计 */
@media (max-width: 991px) {
    .chat-content {
        flex-direction: column;
    }

    .chat-panel {
        width: 100%;
        height: 60%;
        border-right: none;
        border-bottom: 1px solid var(--claude-border-warm);
    }

    .preview-panel {
        width: calc(100% - 48px);
        height: 40%;
        margin: 24px;
    }
}

@media (max-width: 640px) {
    .chat-header {
        padding: 16px 20px;
        flex-direction: column;
        gap: 16px;
        align-items: stretch;
    }

    .header-left {
        justify-content: space-between;
    }

    .app-name {
        font-size: 24px;
    }

    .messages-container {
        padding: 20px;
    }
    
    .input-container {
        padding: 16px 20px;
    }

    .deploy-btn {
        width: 100%;
        justify-content: center;
    }
}

/* Markdown内容样式 */
.markdown-content {
    line-height: 1.6;
    font-size: 16px;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
    font-family: var(--font-serif);
    font-weight: 500;
    color: var(--claude-text-primary);
    margin: 1.5em 0 0.5em;
    line-height: 1.2;
}

.markdown-content h1 { font-size: 32px; }
.markdown-content h2 { font-size: 25px; }
.markdown-content h3 { font-size: 20px; }
.markdown-content h4 { font-size: 18px; }
.markdown-content h5 { font-size: 16px; }

.markdown-content p {
    margin: 0.8em 0;
    color: var(--claude-text-primary);
}

.markdown-content code {
    font-family: var(--font-mono);
    background: var(--claude-surface-sand);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 15px;
    color: var(--claude-text-primary);
    letter-spacing: -0.32px;
}

.markdown-content pre {
    background: var(--claude-surface-white);
    padding: 0;
    border-radius: 12px;
    overflow: hidden;
    margin: 1.2em 0;
    border: 1px solid var(--claude-border-warm);
    box-shadow: 0 4px 24px rgba(0,0,0,0.02);
}

.markdown-content pre code {
    background: none;
    padding: 0;
    color: inherit;
}

/* 代码块包装器样式 */
.markdown-content :deep(.code-block-wrapper) {
    margin: 1.2em 0;
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid var(--claude-border-warm);
    background: var(--claude-surface-white);
    transition: all 0.2s ease;
    box-shadow: 0 4px 24px rgba(0,0,0,0.02);
}

.markdown-content :deep(.code-block-wrapper.streaming-code) {
    border-color: var(--claude-brand);
    box-shadow: 0 0 0 1px var(--claude-brand);
}

.markdown-content :deep(.code-block-header) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: var(--claude-surface);
    border-bottom: 1px solid var(--claude-border-warm);
}

.markdown-content :deep(.code-language) {
    font-family: var(--font-sans);
    font-size: 12px;
    font-weight: 500;
    color: var(--claude-text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.markdown-content :deep(.code-copy-btn) {
    background: transparent;
    border: none;
    color: var(--claude-text-tertiary);
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 6px;
    transition: all 0.2s ease;
    font-size: 14px;
}

.markdown-content :deep(.code-copy-btn:hover) {
    background: var(--claude-surface-sand);
    color: var(--claude-text-primary);
}

.markdown-content :deep(.code-block-content pre) {
    margin: 0;
    padding: 16px;
    background: var(--claude-surface-white);
    border: none;
    box-shadow: none;
}

.markdown-content :deep(.code-block-content code) {
    font-family: var(--font-mono);
    font-size: 15px;
    line-height: 1.6;
    color: var(--claude-text-primary);
    background: transparent;
}

/* 列表样式 */
.markdown-content ul,
.markdown-content ol {
    margin: 0.8em 0;
    padding-left: 1.5em;
    color: var(--claude-text-primary);
}

.markdown-content li {
    margin: 0.4em 0;
}

.markdown-content blockquote {
    border-left: 3px solid var(--claude-border-warm);
    padding-left: 16px;
    margin: 1em 0;
    color: var(--claude-text-secondary);
    font-style: italic;
}

/* 表格样式 */
.markdown-content table {
    border-collapse: collapse;
    width: 100%;
    margin: 1.2em 0;
    font-size: 15px;
}

.markdown-content th,
.markdown-content td {
    border: 1px solid var(--claude-border-warm);
    padding: 12px 16px;
    text-align: left;
}

.markdown-content th {
    background: var(--claude-surface);
    font-weight: 500;
    color: var(--claude-text-secondary);
}

/* 工具调用卡片样式 */
.markdown-content :deep(.tool-call-card) {
    border: 1px solid var(--claude-border-warm);
    border-radius: 12px;
    padding: 16px;
    margin: 1.2em 0;
    background: var(--claude-surface);
    box-shadow: 0 4px 24px rgba(0,0,0,0.02);
}

.markdown-content :deep(.tool-call-header) {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
}

.markdown-content :deep(.tool-call-title) {
    font-family: var(--font-sans);
    font-weight: 500;
    color: var(--claude-text-primary);
}

.markdown-content :deep(.tool-call-action) {
    margin-left: auto;
    background: var(--claude-surface-white);
    color: var(--claude-text-secondary);
    border: 1px solid var(--claude-border-warm);
    padding: 2px 8px;
    border-radius: 6px;
    font-size: 12px;
}

.markdown-content :deep(.tool-call-target) {
    font-family: var(--font-mono);
    background: var(--claude-surface-white);
    border: 1px solid var(--claude-border-warm);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 14px;
    color: var(--claude-text-secondary);
}

.markdown-content :deep(.tool-call-body pre) {
    margin: 0;
    background: var(--claude-surface-white);
    border: 1px solid var(--claude-border-warm);
    border-radius: 8px;
}
</style>
