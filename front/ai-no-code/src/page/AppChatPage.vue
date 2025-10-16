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
                        <Icon :icon="app?.codeGenType === 'html' ? 'mdi:file-code-outline' : 'mdi:folder-multiple-outline'" class="app-icon" />
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
    (window as unknown as { copyCodeToClipboard: Function }).copyCodeToClipboard = async (button: HTMLElement) => {
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
    (window as unknown as { toggleCodeBlock: Function }).toggleCodeBlock = (button: HTMLElement) => {
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
                return
            }

            try {
                // 解析SSE数据格式：{"d":"文本内容"}
                const chunk = JSON.parse(data)
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

        eventSource.onerror = () => {
            if (timeout) clearTimeout(timeout)
            eventSource.close()
            isStreaming.value = false

            // 清理打字效果并显示已收到的内容
            if (isTyping.value && aiMessage) {
                aiMessageObj.content = aiMessage
                clearTypingEffect()
            }

            // 如果没有收到完整的消息就断开，说明出错了
            if (aiMessageObj.content && !isGenerated.value) {
                aiMessageObj.content += '\n\n[⚠️ 连接中断，内容可能不完整]'
                message.error('AI对话连接中断，内容可能不完整')
            } else {
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
            message.error('部署失败：' + res.data.message)
        }
    } catch {
        message.error('部署失败')
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

    const baseUrl = 'http://localhost:8081'
    if (selectedVersion.value === 'latest') {
        // 获取最新版本号
        const latestVersion = getLatestVersion()
        if (latestVersion) {
            previewUrl.value = `${baseUrl}/api/static/preview/${app.value.id}/${latestVersion}/`
        } else {
            // 如果没有版本号，使用默认路径
            previewUrl.value = `${baseUrl}/api/static/preview/${app.value.id}/`
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
        window.open(previewUrl.value, '_blank')
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
const renderSafeMarkdown = (content: string) => {
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
    
    return marked(content, { renderer })
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
        'mutiFile': '多文件'
    }
    return typeMap[type || ''] || type || '未知'
}
</script>

<style scoped>
#appChatPage {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: var(--gray-50);
}

/* 顶部栏 */
.chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-4) var(--spacing-6);
    background: white;
    border-bottom: 2px solid var(--secondary-200);
    box-shadow: var(--shadow-sm);
}

.header-left {
    display: flex;
    align-items: center;
    gap: var(--spacing-4);
}

.back-btn {
    color: var(--gray-600);
    font-size: 18px;
    padding: var(--spacing-2);
    border-radius: var(--radius-lg);
    transition: var(--transition-all);
}

.back-btn:hover {
    background: var(--gray-100);
    color: var(--primary-600);
}

.back-icon {
    font-size: 20px;
}

.app-info {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-1);
}

.app-name {
    margin: 0;
    font-size: var(--text-xl);
    font-weight: var(--font-semibold);
    color: var(--deep-600);
    display: flex;
    align-items: center;
    gap: var(--spacing-2);
}

.app-icon {
    font-size: 22px;
    color: var(--primary-600);
}

.app-type {
    font-size: var(--text-sm);
    color: var(--gray-600);
    background: var(--secondary-100);
    padding: 2px var(--spacing-2);
    border-radius: var(--radius-md);
    display: inline-flex;
    align-items: center;
    gap: var(--spacing-1);
    width: fit-content;
}

.type-icon {
    font-size: 14px;
    opacity: 0.8;
}

.header-right {
    display: flex;
    align-items: center;
    gap: var(--spacing-4);
}

.version-selector {
    display: flex;
    align-items: center;
    gap: var(--spacing-2);
}

.version-selector :deep(.ant-select .ant-select-selector) {
    border-radius: var(--radius-lg) !important;
    border: 2px solid var(--secondary-200) !important;
}

.version-selector :deep(.ant-select:focus .ant-select-selector) {
    border-color: var(--primary-500) !important;
}

.version-icon {
    font-size: 16px;
    margin-right: var(--spacing-1);
}

.deploy-btn {
    background: var(--success-500) !important;
    border-color: var(--success-500) !important;
    color: white !important;
    font-weight: var(--font-semibold) !important;
    border-radius: var(--radius-lg) !important;
    display: flex !important;
    align-items: center !important;
    gap: var(--spacing-2) !important;
    transition: var(--transition-all) !important;
}

.deploy-icon {
    font-size: 18px;
}

.deploy-btn:hover {
    background: var(--success-600) !important;
    border-color: var(--success-600) !important;
    transform: translateY(-1px) !important;
    box-shadow: var(--shadow-md) !important;
}

.deploy-btn:disabled {
    background: var(--gray-400) !important;
    border-color: var(--gray-400) !important;
    transform: none !important;
    box-shadow: none !important;
}

/* 主内容区域 */
.chat-content {
    flex: 1;
    display: flex;
    height: calc(100vh - 80px);
}

/* 左侧对话面板 */
.chat-panel {
    width: 50%;
    display: flex;
    flex-direction: column;
    background: white;
    border-right: 2px solid var(--secondary-200);
}

.messages-container {
    flex: 1;
    padding: var(--spacing-4);
    overflow-y: auto;
    scroll-behavior: smooth;
}

/* 加载更多按钮样式 */
.load-more-container {
    text-align: center;
    padding: var(--spacing-3) 0;
    margin-bottom: var(--spacing-4);
}

.load-more-btn {
    background: var(--gray-100) !important;
    border-color: var(--secondary-300) !important;
    color: var(--secondary-700) !important;
    border-radius: var(--radius-full) !important;
    padding: var(--spacing-2) var(--spacing-4) !important;
    font-size: var(--text-sm) !important;
    display: inline-flex !important;
    align-items: center !important;
    gap: var(--spacing-2) !important;
    transition: var(--transition-all) !important;
}

.load-more-btn:hover {
    background: var(--secondary-100) !important;
    border-color: var(--secondary-400) !important;
    color: var(--secondary-800) !important;
    transform: translateY(-1px) !important;
    box-shadow: var(--shadow-sm) !important;
}

/* 消息样式 */
.message-item {
    display: flex;
    gap: var(--spacing-3);
    margin-bottom: var(--spacing-4);
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
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: var(--font-semibold);
}

.user-avatar {
    background: var(--primary-600);
}

.ai-avatar {
    background: var(--secondary-600);
    font-size: 18px;
}

.message-content {
    flex: 1;
    max-width: calc(100% - 48px);
}

.user-message .message-content {
    text-align: right;
}

.message-text {
    background: var(--gray-100);
    padding: var(--spacing-3) var(--spacing-4);
    border-radius: var(--radius-lg);
    line-height: var(--leading-relaxed);
    word-wrap: break-word;
    overflow-wrap: break-word;
}

.user-message .message-text {
    background: var(--primary-600);
    color: white;
    margin-left: auto;
    display: inline-block;
    max-width: 80%;
}

.ai-message .message-text {
    margin-right: auto;
    max-width: 80%;
}

.message-time {
    font-size: var(--text-xs);
    color: var(--gray-500);
    margin-top: var(--spacing-1);
    padding: 0 var(--spacing-1);
}

/* 输入中指示器 */
.typing-indicator {
    background: var(--gray-100);
    padding: var(--spacing-3) var(--spacing-4);
    border-radius: var(--radius-lg);
    display: flex;
    gap: 4px;
    width: fit-content;
}

.typing-indicator span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--gray-400);
    animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
    animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
    animation-delay: -0.16s;
}

@keyframes typing {

    0%,
    80%,
    100% {
        transform: scale(0.8);
        opacity: 0.5;
    }

    40% {
        transform: scale(1);
        opacity: 1;
    }
}

/* 输入区域 */
.input-container {
    padding: var(--spacing-4);
    border-top: 1px solid var(--secondary-200);
    background: var(--gray-50);
}

.input-wrapper {
    display: flex;
    gap: var(--spacing-3);
    align-items: flex-end;
}

.message-input {
    flex: 1;
    border-radius: var(--radius-lg) !important;
    border: 2px solid var(--secondary-200) !important;
    transition: var(--transition-all) !important;
}

.message-input:focus {
    border-color: var(--primary-500) !important;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1) !important;
}

.send-btn {
    background: var(--primary-600) !important;
    border-color: var(--primary-600) !important;
    color: white !important;
    border-radius: var(--radius-lg) !important;
    height: 40px !important;
    width: 40px !important;
    padding: 0 !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    transition: var(--transition-all) !important;
}

.send-icon {
    font-size: 18px;
    transition: var(--transition-transform);
}

.send-btn:hover {
    background: var(--primary-700) !important;
    border-color: var(--primary-700) !important;
    transform: scale(1.05) !important;
}

.send-btn:hover .send-icon {
    transform: translateX(2px);
}

.send-btn:disabled {
    background: var(--gray-400) !important;
    border-color: var(--gray-400) !important;
    transform: none !important;
}

.load-icon,
.action-icon {
    font-size: 16px;
    margin-right: var(--spacing-1);
}

/* 右侧预览面板 */
.preview-panel {
    width: 50%;
    display: flex;
    flex-direction: column;
    background: white;
}

.preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-4) var(--spacing-6);
    border-bottom: 1px solid var(--secondary-200);
    background: var(--gray-50);
}

.preview-header h3 {
    margin: 0;
    font-size: var(--text-lg);
    font-weight: var(--font-semibold);
    color: var(--deep-600);
}

.preview-actions {
    display: flex;
    gap: var(--spacing-2);
}

.preview-content {
    flex: 1;
    position: relative;
    overflow: hidden;
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
    color: var(--gray-600);
}

.placeholder-icon,
.error-icon {
    font-size: 64px;
    margin-bottom: var(--spacing-4);
    color: var(--gray-400);
    opacity: 0.8;
}

.preview-placeholder h4,
.preview-error h4 {
    font-size: var(--text-xl);
    font-weight: var(--font-semibold);
    margin: 0 0 var(--spacing-2);
    color: var(--deep-600);
}

.preview-placeholder p,
.preview-error p {
    font-size: var(--text-base);
    margin: 0;
    color: var(--gray-600);
}

.preview-loading p {
    margin-top: var(--spacing-4);
    font-size: var(--text-base);
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
    background: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .chat-content {
        flex-direction: column;
    }

    .chat-panel {
        width: 100%;
        height: 60%;
    }

    .preview-panel {
        width: 100%;
        height: 40%;
    }

    .chat-header {
        padding: var(--spacing-3) var(--spacing-4);
    }

    .app-name {
        font-size: var(--text-lg);
    }

    .header-left {
        gap: var(--spacing-3);
    }
}

@media (max-width: 480px) {
    .chat-header {
        flex-direction: column;
        gap: var(--spacing-3);
        align-items: stretch;
    }

    .header-left {
        justify-content: space-between;
    }

    .deploy-btn {
        width: 100%;
        justify-content: center;
    }
}

/* 打字机光标效果 */
.typing-cursor {
    color: var(--primary-600);
    font-weight: bold;
    animation: blink 1s infinite;
    display: inline-block;
    margin-left: 2px;
}

@keyframes blink {

    0%,
    50% {
        opacity: 1;
    }

    51%,
    100% {
        opacity: 0;
    }
}

/* Markdown内容样式 */
.markdown-content {
    line-height: 1.6;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
    margin: 0.5em 0;
    font-weight: var(--font-semibold);
    color: var(--deep-600);
}

.markdown-content h1 { font-size: 1.5em; }
.markdown-content h2 { font-size: 1.3em; }
.markdown-content h3 { font-size: 1.2em; }
.markdown-content h4 { font-size: 1.1em; }
.markdown-content h5 { font-size: 1em; }
.markdown-content h6 { font-size: 0.9em; }

.markdown-content p {
    margin: 0.5em 0;
}

.markdown-content code {
    background: var(--gray-200);
    padding: 2px 4px;
    border-radius: 3px;
    font-family: 'Courier New', monospace;
    font-size: 0.9em;
    color: var(--deep-600);
}

.markdown-content pre {
    background: #0d1117;
    padding: 0;
    border-radius: var(--radius-md);
    overflow: hidden;
    margin: 0.5em 0;
    border: 1px solid var(--secondary-200);
}

.markdown-content pre code {
    background: none;
    padding: 0;
    color: inherit;
}

/* 代码块包装器样式 - 优化流式输出显示 */
.markdown-content :deep(.code-block-wrapper) {
    margin: 1em 0;
    border-radius: var(--radius-lg);
    overflow: hidden;
    border: 1px solid #30363d;
    background: #0d1117;
    transition: var(--transition-all);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.markdown-content :deep(.code-block-wrapper.streaming-code) {
    border: 2px solid var(--primary-500);
    box-shadow: 0 0 0 3px rgba(74, 157, 117, 0.1);
}

.markdown-content :deep(.code-block-wrapper.large-code) {
    border-color: var(--warning-500);
}

.markdown-content :deep(.code-block-header) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-3) var(--spacing-4);
    background: #161b22;
    border-bottom: 1px solid #30363d;
}

.markdown-content :deep(.header-left) {
    display: flex;
    align-items: center;
    gap: var(--spacing-3);
}

.markdown-content :deep(.header-actions) {
    display: flex;
    align-items: center;
    gap: var(--spacing-2);
}

.markdown-content :deep(.code-language) {
    font-size: var(--text-sm);
    font-weight: var(--font-bold);
    color: #58a6ff;
    background: rgba(88, 166, 255, 0.1);
    padding: 2px 8px;
    border-radius: var(--radius-sm);
}

.markdown-content :deep(.line-count) {
    font-size: var(--text-xs);
    color: #8b949e;
    background: #21262d;
    padding: 2px 6px;
    border-radius: var(--radius-sm);
}

.markdown-content :deep(.size-indicator) {
    font-size: var(--text-xs);
    color: #f85149;
    background: rgba(248, 81, 73, 0.1);
    padding: 2px 6px;
    border-radius: var(--radius-sm);
}

.markdown-content :deep(.code-copy-btn) {
    background: transparent;
    border: none;
    color: #8b949e;
    cursor: pointer;
    padding: var(--spacing-1);
    border-radius: var(--radius-sm);
    transition: var(--transition-all);
    font-size: 14px;
}

.markdown-content :deep(.code-copy-btn:hover) {
    background: #30363d;
    color: #58a6ff;
}

.markdown-content :deep(.code-toggle-btn) {
    background: transparent;
    border: none;
    color: #8b949e;
    cursor: pointer;
    padding: var(--spacing-1);
    border-radius: var(--radius-sm);
    transition: var(--transition-all);
    font-size: 14px;
}

.markdown-content :deep(.code-toggle-btn:hover) {
    background: #30363d;
    color: #c9d1d9;
}

.markdown-content :deep(.toggle-icon) {
    display: inline-block;
    transition: transform 0.3s ease;
}

.markdown-content :deep(.code-block-wrapper.collapsed .toggle-icon) {
    transform: rotate(-90deg);
}

/* 代码内容区域 - 适应流式输出 */
.markdown-content :deep(.code-block-content) {
    max-height: none;
    overflow-x: auto;
    overflow-y: visible;
    transition: max-height 0.3s ease, opacity 0.3s ease;
    position: relative;
}

/* 大型代码块的特殊处理 */
.markdown-content :deep(.code-block-wrapper.large-code .code-block-content) {
    max-height: 400px;
    overflow-y: auto;
}

.markdown-content :deep(.code-block-wrapper.collapsed .code-block-content) {
    max-height: 0;
    opacity: 0;
    overflow: hidden;
}

.markdown-content :deep(.code-block-content pre) {
    margin: 0;
    padding: var(--spacing-4);
    background: #0d1117;
    border: none;
    white-space: pre-wrap;
    word-wrap: break-word;
    overflow-wrap: break-word;
}

.markdown-content :deep(.code-block-content code) {
    font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', monospace;
    font-size: 0.85em;
    line-height: 1.6;
    color: #c9d1d9;
    display: block;
}

/* 简单代码块样式 */
.markdown-content :deep(.simple-code-block) {
    margin: 1em 0;
    border-radius: var(--radius-md);
    overflow: hidden;
    border: 1px solid var(--gray-300);
    background: #f6f8fa;
}

.markdown-content :deep(.simple-header) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-2) var(--spacing-3);
    background: #f1f3f4;
    border-bottom: 1px solid var(--gray-300);
    font-size: var(--text-sm);
    font-weight: var(--font-medium);
    color: var(--gray-700);
}

.markdown-content :deep(.simple-header button) {
    background: transparent;
    border: none;
    color: var(--gray-600);
    cursor: pointer;
    padding: 2px 6px;
    border-radius: var(--radius-sm);
    font-size: var(--text-xs);
    transition: var(--transition-all);
}

.markdown-content :deep(.simple-header button:hover) {
    background: var(--gray-200);
    color: var(--primary-600);
}

.markdown-content :deep(.simple-code-block pre) {
    margin: 0;
    padding: var(--spacing-3);
    background: #f6f8fa;
    color: var(--gray-800);
    font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
    font-size: 0.85em;
    line-height: 1.5;
    overflow-x: auto;
    white-space: pre-wrap;
    word-wrap: break-word;
}

/* 代码块滚动条样式 */
.markdown-content :deep(.code-block-content::-webkit-scrollbar) {
    width: 8px;
    height: 8px;
}

.markdown-content :deep(.code-block-content::-webkit-scrollbar-track) {
    background: #161b22;
}

.markdown-content :deep(.code-block-content::-webkit-scrollbar-thumb) {
    background: #30363d;
    border-radius: 4px;
}

.markdown-content :deep(.code-block-content::-webkit-scrollbar-thumb:hover) {
    background: #484f58;
}

.markdown-content ul,
.markdown-content ol {
    margin: 0.5em 0;
    padding-left: 1.5em;
}

.markdown-content li {
    margin: 0.2em 0;
}

.markdown-content blockquote {
    border-left: 4px solid var(--primary-500);
    padding-left: var(--spacing-3);
    margin: 0.5em 0;
    color: var(--gray-700);
    background: var(--gray-50);
    padding: var(--spacing-2) var(--spacing-3);
    border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.markdown-content table {
    border-collapse: collapse;
    width: 100%;
    margin: 0.5em 0;
}

.markdown-content th,
.markdown-content td {
    border: 1px solid var(--secondary-300);
    padding: var(--spacing-2);
    text-align: left;
}

.markdown-content th {
    background: var(--gray-100);
    font-weight: var(--font-semibold);
}

.markdown-content a {
    color: var(--primary-600);
    text-decoration: none;
}

.markdown-content a:hover {
    text-decoration: underline;
}

.markdown-content strong {
    font-weight: var(--font-bold);
}

.markdown-content em {
    font-style: italic;
}

.markdown-content hr {
    border: none;
    border-top: 1px solid var(--secondary-300);
    margin: 1em 0;
}
</style>
