/**
 * AI 对话页面
 */

import React, { useState, useEffect } from 'react'
import { View, Text, Input, Button, ScrollView } from '@tarojs/components'
import { useRouter } from '@tarojs/taro'
import { generateCode } from '../../api/app'
import { getChatHistory } from '../../api/chatHistory'
import './index.css'

interface Message {
  id: string
  content: string
  isUser: boolean
  isCode?: boolean
}

export default function ChatPage() {
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [appId, setAppId] = useState<string | null>(null)
  const [scrollIntoView, setScrollIntoView] = useState('')
  const router = useRouter()

  useEffect(() => {
    const { appId: id } = router.params
    if (id) {
      setAppId(id)
      loadHistory(id)
    }
  }, [])

  useEffect(() => {
    if (messages.length > 0 || loading) {
      setScrollIntoView('chat-bottom-anchor')
    }
  }, [messages])

  useEffect(() => {
    if (loading) {
      setScrollIntoView('chat-bottom-anchor')
    }
  }, [loading])

  const loadHistory = async (id: string) => {
    try {
      const res = await getChatHistory({ appId: id, pageSize: 50 })
      const history: Message[] = (res.records || []).reverse().map((chat) => ({
        id: chat.id,
        content: chat.message,
        isUser: chat.messageType === 0,
        isCode: chat.isCode,
      }))
      setMessages(history)
    } catch (err) {
      console.error('Failed to load chat history:', err)
    }
  }

  const handleSend = async () => {
    if (!input.trim() || !appId || loading) return

    const userMessage = input.trim()
    setInput('')

    // 添加用户消息
    setMessages((prev) => [
      ...prev,
      { id: String(Date.now()), content: userMessage, isUser: true },
    ])

    setLoading(true)

    try {
      let aiResponse = ''

      // 流式生成代码
      await generateCode(appId, userMessage, (chunk) => {
        aiResponse += chunk
        // 最后一次性更新 UI
      })

      // 添加 AI 响应
      setMessages((prev) => [
        ...prev,
        { id: String(Date.now() + 1), content: aiResponse, isUser: false, isCode: true },
      ])
    } catch (err) {
      console.error('Failed to generate code:', err)
      setMessages((prev) => [
        ...prev,
        { id: String(Date.now() + 1), content: '生成失败，请重试', isUser: false },
      ])
    } finally {
      setLoading(false)
    }
  }

  const handlePreview = () => {
    if (appId) {
      // 跳转到预览页面
    }
  }

  return (
    <View className="chat-container">
      <ScrollView className="chat-messages" scrollY scrollIntoView={scrollIntoView}>
        <View className="chat-messages-inner">
          {messages.length === 0 ? (
            <View className="empty-chat">
              <Text>开始与 AI 对话，生成你的应用</Text>
            </View>
          ) : (
            messages.map((msg) => (
              <View key={msg.id} className={`message ${msg.isUser ? 'user' : 'ai'}`}>
                <Text className={`message-content ${msg.isCode ? 'code' : ''}`}>
                  {msg.content}
                </Text>
              </View>
            ))
          )}
          {loading && (
            <View className="message ai">
              <Text className="message-content loading">AI 正在生成代码...</Text>
            </View>
          )}
          <View id="chat-bottom-anchor" className="chat-bottom-anchor" />
        </View>
      </ScrollView>

      <View className="chat-input-area">
        <Input
          className="chat-input"
          placeholder="描述你想要的应用功能..."
          value={input}
          onInput={(e) => setInput(e.detail.value)}
          onConfirm={handleSend}
        />
        <Button className="send-btn" onClick={handleSend} loading={loading}>
          发送
        </Button>
      </View>
    </View>
  )
}
