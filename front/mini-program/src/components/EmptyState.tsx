/**
 * 空状态组件
 */

import React from 'react'
import { View, Text } from '@tarojs/components'
import './EmptyState.css'

interface EmptyStateProps {
  title: string
  description?: string
  actionText?: string
  onAction?: () => void
}

export default function EmptyState({ title, description, actionText, onAction }: EmptyStateProps) {
  return (
    <View className="empty-state">
      <View className="empty-icon">
        <Text className="icon-text">📭</Text>
      </View>
      <Text className="empty-title">{title}</Text>
      {description && (
        <Text className="empty-description">{description}</Text>
      )}
      {actionText && onAction && (
        <View className="empty-action" onClick={onAction}>
          <Text className="action-text">{actionText}</Text>
        </View>
      )}
    </View>
  )
}