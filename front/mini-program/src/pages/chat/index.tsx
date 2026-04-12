/**
 * AI 对话页面
 */

import React, { useState, useEffect } from 'react'
import { View, Text, Input, Button, ScrollView } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import { generateCode } from '../../api/app'
import { getChatHistory } from '../../api/chatHistory'
import './index.css'

type MessageType = 'ai' | 'user'

interface Message {
  id: string
  content: string
  messageType: MessageType
  isCode?: boolean
}

interface MarkdownBlock {
  type: 'text' | 'code'
  content: string
  language?: string
}

const parseMarkdownBlocks = (content: string): MarkdownBlock[] => {
  const blocks: MarkdownBlock[] = []
  const codeBlockRegex = /```([\w-]*)\n([\s\S]*?)```/g
  let lastIndex = 0
  let match: RegExpExecArray | null

  while ((match = codeBlockRegex.exec(content)) !== null) {
    const [fullMatch, lang, code] = match
    const textBefore = content.slice(lastIndex, match.index)

    if (textBefore.trim()) {
      blocks.push({ type: 'text', content: textBefore })
    }

    blocks.push({
      type: 'code',
      language: (lang || 'text').toLowerCase(),
      content: code.replace(/\n+$/g, ''),
    })

    lastIndex = match.index + fullMatch.length
  }

  const tail = content.slice(lastIndex)
  if (tail.trim()) {
    blocks.push({ type: 'text', content: tail })
  }

  if (blocks.length === 0) {
    blocks.push({ type: 'text', content })
  }

  return blocks
}

interface InlineNode {
  type: 'text' | 'strong' | 'code' | 'link'
  value: string
  href?: string
}

const parseInlineWithAutoLinks = (text: string): InlineNode[] => {
  const urlRegex = /(https?:\/\/[^\s]+)/g
  const nodes: InlineNode[] = []
  let cursor = 0
  let match: RegExpExecArray | null

  while ((match = urlRegex.exec(text)) !== null) {
    if (match.index > cursor) {
      nodes.push({ type: 'text', value: text.slice(cursor, match.index) })
    }

    nodes.push({ type: 'link', value: match[0], href: match[0] })
    cursor = match.index + match[0].length
  }

  if (cursor < text.length) {
    nodes.push({ type: 'text', value: text.slice(cursor) })
  }

  return nodes.length ? nodes : [{ type: 'text', value: text }]
}

const normalizeMessageType = (rawType: string | number | undefined): MessageType => {
  if (rawType === 'user' || rawType === 0 || rawType === '0') {
    return 'user'
  }
  return 'ai'
}

const splitTableRow = (line: string): string[] => {
  const normalized = line.trim().replace(/^\|/, '').replace(/\|$/, '')
  return normalized.split('|').map((cell) => cell.trim())
}

const isTableSeparator = (line: string): boolean => {
  const normalized = line.trim().replace(/^\|/, '').replace(/\|$/, '')
  if (!normalized.includes('|')) return false
  return normalized.split('|').every((cell) => /^:?-{3,}:?$/.test(cell.trim()))
}

const isMarkdownTableRow = (line: string): boolean => {
  const trimmed = line.trim()
  return Boolean(trimmed) && trimmed.includes('|')
}

const handleLinkPress = (url: string) => {
  if (!url) return

  if (url.startsWith('/pages/')) {
    Taro.navigateTo({ url })
    return
  }

  if (/^https?:\/\//.test(url)) {
    Taro.navigateTo({
      url: `/pages/preview/index?url=${encodeURIComponent(url)}`,
    })
    return
  }

  Taro.showToast({ title: '暂不支持该链接类型', icon: 'none' })
}

const renderInlineMarkdown = (line: string, keyPrefix: string) => {
  const nodes: InlineNode[] = []
  const tokenRegex = /(\*\*[^*]+\*\*)|(`[^`]+`)|(\[[^\]]+\]\(([^)]+)\))/g
  let cursor = 0
  let match: RegExpExecArray | null

  while ((match = tokenRegex.exec(line)) !== null) {
    if (match.index > cursor) {
      nodes.push(...parseInlineWithAutoLinks(line.slice(cursor, match.index)))
    }

    const token = match[0]
    if (match[3]) {
      const linkText = token.slice(1, token.indexOf(']('))
      const href = match[4] || ''
      nodes.push({ type: 'link', value: linkText, href })
    } else if (token.startsWith('**')) {
      nodes.push({ type: 'strong', value: token.slice(2, -2) })
    } else {
      nodes.push({ type: 'code', value: token.slice(1, -1) })
    }

    cursor = match.index + token.length
  }

  if (cursor < line.length) {
    nodes.push(...parseInlineWithAutoLinks(line.slice(cursor)))
  }

  if (nodes.length === 0) {
    return (
      <Text selectable userSelect className="md-text-line" key={keyPrefix}>
        {line}
      </Text>
    )
  }

  return (
    <Text selectable userSelect className="md-text-line" key={keyPrefix}>
      {nodes.map((node, idx) => {
        if (node.type === 'strong') {
          return (
            <Text key={`${keyPrefix}-strong-${idx}`} className="md-strong">
              {node.value}
            </Text>
          )
        }

        if (node.type === 'code') {
          return (
            <Text key={`${keyPrefix}-code-${idx}`} className="md-inline-code">
              {node.value}
            </Text>
          )
        }

        if (node.type === 'link') {
          return (
            <Text
              key={`${keyPrefix}-link-${idx}`}
              className="md-link"
              onClick={() => handleLinkPress(node.href || node.value)}
            >
              {node.value}
            </Text>
          )
        }

        return <Text key={`${keyPrefix}-text-${idx}`}>{node.value}</Text>
      })}
    </Text>
  )
}

export default function ChatPage() {
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [appId, setAppId] = useState<string | null>(null)
  const [scrollIntoView, setScrollIntoView] = useState('')
  const [expandedCodeBlocks, setExpandedCodeBlocks] = useState<Record<string, boolean>>({})
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
        messageType: normalizeMessageType(chat.messageType),
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
      { id: String(Date.now()), content: userMessage, messageType: 'user' },
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
        {
          id: String(Date.now() + 1),
          content: aiResponse,
          messageType: 'ai',
          isCode: true,
        },
      ])
    } catch (err) {
      console.error('Failed to generate code:', err)
      setMessages((prev) => [
        ...prev,
        {
          id: String(Date.now() + 1),
          content: '生成失败，请重试',
          messageType: 'ai',
        },
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

  const handleCopy = (content: string) => {
    Taro.setClipboardData({
      data: content,
      success: () => {
        Taro.vibrateShort({ type: 'light' })
        Taro.showToast({ title: '已复制', icon: 'none', duration: 1200 })
      },
      fail: () => {
        Taro.showToast({ title: '复制失败', icon: 'none' })
      },
    })
  }

  const toggleCodeBlock = (blockKey: string) => {
    setExpandedCodeBlocks((prev) => ({
      ...prev,
      [blockKey]: !prev[blockKey],
    }))
  }

  const renderMarkdownTextBlock = (content: string, keyPrefix: string) => {
    const lines = content.split('\n')
    const renderedRows: React.ReactNode[] = []

    let lineIndex = 0
    while (lineIndex < lines.length) {
      const line = lines[lineIndex]
      const lineKey = `${keyPrefix}-line-${lineIndex}`

      if (!line.trim()) {
        renderedRows.push(<View key={lineKey} className="md-empty-line" />)
        lineIndex += 1
        continue
      }

      const taskMatch = line.match(/^\s*[-*]\s+\[( |x|X)\]\s+(.+)$/)
      if (taskMatch) {
        const done = taskMatch[1].toLowerCase() === 'x'
        renderedRows.push(
          <View key={lineKey} className="md-task-row">
            <View className={`md-task-box ${done ? 'md-task-box-done' : ''}`}>
              {done && <Text className="md-task-check">✓</Text>}
            </View>
            {renderInlineMarkdown(taskMatch[2], `${lineKey}-task`) }
          </View>
        )
        lineIndex += 1
        continue
      }

      if (
        lineIndex + 1 < lines.length &&
        isMarkdownTableRow(line) &&
        isTableSeparator(lines[lineIndex + 1])
      ) {
        const headers = splitTableRow(line)
        const rows: string[][] = []
        let tableIndex = lineIndex + 2

        while (tableIndex < lines.length && isMarkdownTableRow(lines[tableIndex])) {
          rows.push(splitTableRow(lines[tableIndex]))
          tableIndex += 1
        }

        renderedRows.push(
          <ScrollView key={lineKey} className="md-table-scroll" scrollX>
            <View className="md-table">
              <View className="md-table-row md-table-head-row">
                {headers.map((header, headerIndex) => (
                  <View
                    key={`${lineKey}-head-${headerIndex}`}
                    className="md-table-cell md-table-head-cell"
                  >
                    {renderInlineMarkdown(header, `${lineKey}-head-text-${headerIndex}`)}
                  </View>
                ))}
              </View>
              {rows.map((row, rowIndex) => (
                <View className="md-table-row" key={`${lineKey}-row-${rowIndex}`}>
                  {headers.map((_, colIndex) => (
                    <View
                      key={`${lineKey}-row-${rowIndex}-col-${colIndex}`}
                      className="md-table-cell"
                    >
                      {renderInlineMarkdown(row[colIndex] || '', `${lineKey}-cell-${rowIndex}-${colIndex}`)}
                    </View>
                  ))}
                </View>
              ))}
            </View>
          </ScrollView>
        )

        lineIndex = tableIndex
        continue
      }

      const headingMatch = line.match(/^(#{1,6})\s+(.+)$/)
      if (headingMatch) {
        const level = headingMatch[1].length
        renderedRows.push(
          <Text selectable userSelect key={lineKey} className={`md-heading md-heading-${level}`}>
            {headingMatch[2]}
          </Text>
        )
        lineIndex += 1
        continue
      }

      const quoteMatch = line.match(/^>\s?(.+)$/)
      if (quoteMatch) {
        renderedRows.push(
          <View key={lineKey} className="md-quote-row">
            <Text className="md-quote-mark">|</Text>
            {renderInlineMarkdown(quoteMatch[1], lineKey)}
          </View>
        )
        lineIndex += 1
        continue
      }

      const listMatch = line.match(/^\s*([-*+]|\d+\.)\s+(.+)$/)
      if (listMatch) {
        renderedRows.push(
          <View key={lineKey} className="md-list-row">
            <Text className="md-list-mark">{listMatch[1]}</Text>
            {renderInlineMarkdown(listMatch[2], lineKey)}
          </View>
        )
        lineIndex += 1
        continue
      }

      renderedRows.push(renderInlineMarkdown(line, lineKey))
      lineIndex += 1
    }

    return (
      <View className="md-text-block">
        {renderedRows}
      </View>
    )
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
              <View key={msg.id} className={`message ${msg.messageType}`}>
                <View className="message-meta">
                  <Text className={`role-tag ${msg.messageType === 'user' ? 'role-user' : 'role-ai'}`}>
                    {msg.messageType === 'user' ? '我' : 'LLM'}
                  </Text>
                </View>
                <View className="message-bubble">
                  {parseMarkdownBlocks(msg.content).map((block, blockIndex) => {
                    if (block.type === 'code') {
                      const blockKey = `${msg.id}-code-${blockIndex}`
                      const expanded = Boolean(expandedCodeBlocks[blockKey])
                      const codeLines = block.content.split('\n').length

                      return (
                        <View className="md-code-card" key={blockKey}>
                          <View className="md-code-header">
                            <Text className="md-code-lang">{block.language || 'text'}</Text>
                            <View className="md-code-actions">
                              <Text
                                className="md-code-action"
                                onClick={() => handleCopy(block.content)}
                              >
                                复制代码
                              </Text>
                              <Text
                                className="md-code-action"
                                onClick={() => toggleCodeBlock(blockKey)}
                              >
                                {expanded ? '收起' : '展开'}
                              </Text>
                            </View>
                          </View>
                          {expanded ? (
                            <ScrollView className="md-code-scroll" scrollX>
                              <Text selectable userSelect className="md-code-content">
                                {block.content}
                              </Text>
                            </ScrollView>
                          ) : (
                            <Text className="md-code-collapsed-tip">
                              代码块已折叠（{codeLines} 行）
                            </Text>
                          )}
                        </View>
                      )
                    }

                    return (
                      <View key={`${msg.id}-text-${blockIndex}`} className="md-block">
                        {renderMarkdownTextBlock(block.content, `${msg.id}-${blockIndex}`)}
                      </View>
                    )
                  })}
                </View>
                <View className="message-actions">
                  <Text className="bubble-copy-btn" onClick={() => handleCopy(msg.content)}>
                    复制全文
                  </Text>
                </View>
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

      <View className="chat-input-shell">
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
    </View>
  )
}
