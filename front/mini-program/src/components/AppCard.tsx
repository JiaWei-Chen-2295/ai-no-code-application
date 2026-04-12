/**
 * 应用卡片组件
 */

import React from 'react'
import { View, Text, Image } from '@tarojs/components'
import { AppVO } from '../../api/app'
import { getApiBaseUrl } from '../../utils/http'
import './AppCard.css'

interface AppCardProps {
  app: AppVO
  onClick: () => void
}

export default function AppCard({ app, onClick }: AppCardProps) {
  const coverUrl = app.cover ? getApiBaseUrl() + app.cover : 'https://img.yzcdn.cn/vant/cat.jpeg'
  return (
    <View className="app-card" onClick={onClick}>
      <Image
        className="app-cover"
        src={coverUrl}
        mode="aspectFill"
      />
      <View className="app-info">
        <Text className="app-name">{app.appName}</Text>
        <Text className="app-desc">{app.initPrompt || '暂无描述'}</Text>
        <View className="app-meta">
          <Text className="app-author">{app.user?.name || '未知作者'}</Text>
          <Text className="app-time">{app.createTime?.split('T')[0]}</Text>
        </View>
      </View>
    </View>
  )
}