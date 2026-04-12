/**
 * 首页 - 应用列表
 */

import React, { useState, useEffect } from 'react'
import { View, Text, Image, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getFeaturedAppList, getMyAppList, AppVO } from '../../api/app'
import { useUser } from '../../store/user'
import { getApiBaseUrl } from '../../utils/http'
import './index.css'

export default function HomePage() {
  const [activeTab, setActiveTab] = useState(0)
  const [featuredApps, setFeaturedApps] = useState<AppVO[]>([])
  const [myApps, setMyApps] = useState<AppVO[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { user } = useUser()
  
  const fetchApps = async () => {
    setLoading(true)
    setError(null)
    try {
      const [featuredRes, myRes] = await Promise.all([
        getFeaturedAppList({ current: 1, pageSize: 20 }),
        getMyAppList({ current: 1, pageSize: 20 }),
      ])
      setFeaturedApps(featuredRes.records || [])
      setMyApps(myRes.records || [])
    } catch (err) {
      console.error('Failed to fetch apps:', err)
      setError('获取应用列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchApps()
  }, [])

  const handleAppClick = (appId: string) => {
    Taro.navigateTo({ url: `/pages/app-detail/index?id=${appId}` })
  }

  const handleCreateApp = () => {
    Taro.navigateTo({ url: '/pages/create-app/index' })
  }

  const renderAppCard = (app: AppVO) => {
    const coverUrl = app.cover ? getApiBaseUrl() + app.cover : 'https://img.yzcdn.cn/vant/cat.jpeg'
    return (
      <View className="app-card" key={app.id} onClick={() => handleAppClick(app.id)}>
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
  return (
    <View className="home-container">
      <View className="home-header">
        <Text className="header-title">AI No-Code</Text>
        <Text className="header-subtitle">发现和创建 AI 应用</Text>
      </View>

      <View className="tab-bar">
        <View
          className={`tab-item ${activeTab === 0 ? 'active' : ''}`}
          onClick={() => setActiveTab(0)}
        >
          <Text>精选</Text>
        </View>
        <View
          className={`tab-item ${activeTab === 1 ? 'active' : ''}`}
          onClick={() => setActiveTab(1)}
        >
          <Text>我的应用</Text>
        </View>
      </View>

      <ScrollView className="app-list" scrollY>
        {error && (
          <View className="error-state">
            <Text>{error}</Text>
            <Text className="retry-text" onClick={fetchApps}>重新加载</Text>
          </View>
        )}
        {!error && activeTab === 0 && (
          <View className="app-grid">
            {featuredApps.length > 0 ? (
              featuredApps.map(renderAppCard)
            ) : (
              <View className="empty-state">
                <Text>暂无精选应用</Text>
              </View>
            )}
          </View>
        )}
        {!error && activeTab === 1 && (
          <View className="app-grid">
            {user ? (
              myApps.length > 0 ? (
                myApps.map(renderAppCard)
              ) : (
                <View className="empty-state">
                  <Text>暂无应用</Text>
                  <Text className="create-hint" onClick={handleCreateApp}>
                    创建一个？
                  </Text>
                </View>
              )
            ) : (
              <View className="empty-state">
                <Text>请先登录</Text>
              </View>
            )}
          </View>
        )}
      </ScrollView>

      <View className="create-btn" onClick={handleCreateApp}>
        <Text className="create-icon">+</Text>
      </View>
    </View>
  )
}
