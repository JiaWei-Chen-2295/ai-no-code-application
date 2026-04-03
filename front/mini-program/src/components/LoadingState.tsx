/**
 * 加载状态组件
 */

import React from 'react'
import { View, Text } from '@tarojs/components'
import './LoadingState.css'

interface LoadingStateProps {
  text?: string
}

export default function LoadingState({ text = '加载中...' }: LoadingStateProps) {
  return (
    <View className="loading-state">
      <View className="loading-spinner">
        <View className="spinner-dot"></View>
        <View className="spinner-dot"></View>
        <View className="spinner-dot"></View>
      </View>
      <Text className="loading-text">{text}</Text>
    </View>
  )
}