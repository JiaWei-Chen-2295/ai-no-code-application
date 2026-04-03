/**
 * 聊天气泡组件
 */

import React from 'react'
import { View, Text } from '@tarojs/components'
import './ChatBubble.css'

interface Message {
  id: number
  content: string
  isUser: boolean
  isCode?: boolean
}

interface ChatBubbleProps {
  message: Message
}

export default function ChatBubble({ message }: ChatBubbleProps) {
  return (
    <View className={`chat-bubble ${message.isUser ? 'user' : 'ai'}`}>
      <View className={`bubble-content ${message.isCode ? 'code' : ''}`}>
        <Text className="bubble-text">{message.content}</Text>
      </View>
    </View>
  )
}