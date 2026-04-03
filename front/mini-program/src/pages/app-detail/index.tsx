/**
 * 应用详情页面
 */

import React, { useState, useEffect } from 'react'
import { View, Text, Image, Button } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import { getAppVO, AppVO, getAppVersions, AppVersionVO } from '../../api/app'
import './index.css'

export default function AppDetailPage() {
  const [app, setApp] = useState<AppVO | null>(null)
  const [versions, setVersions] = useState<AppVersionVO[]>([])
  const [currentVersion, setCurrentVersion] = useState('')
  const [loading, setLoading] = useState(true)
  const router = useRouter()
  const { id } = router.params

  useEffect(() => {
    if (id) {
      fetchAppDetail()
    }
  }, [id])

  const fetchAppDetail = async () => {
    if (!id) return
    setLoading(true)
    try {
      const [appData, versionData] = await Promise.all([
        getAppVO(id),
        getAppVersions(id),
      ])
      setApp(appData)
      setVersions(versionData || [])
      if (versionData && versionData.length > 0) {
        const deployed = versionData.find((v) => v.isDeployed)
        setCurrentVersion(deployed?.version || versionData[0].version)
      }
    } catch (err) {
      console.error('Failed to fetch app detail:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleChat = () => {
    if (app) {
      Taro.navigateTo({ url: `/pages/chat/index?appId=${app.id}` })
    }
  }

  const handlePreview = () => {
    if (app) {
      Taro.navigateTo({ url: `/pages/preview/index?appId=${app.id}&version=${currentVersion}` })
    }
  }

  if (loading) {
    return (
      <View className="detail-container">
        <Text className="loading">加载中...</Text>
      </View>
    )
  }

  if (!app) {
    return (
      <View className="detail-container">
        <Text className="error">应用不存在</Text>
      </View>
    )
  }

  return (
    <View className="detail-container">
      <Image
        className="detail-cover"
        src={app.cover || 'https://via.placeholder.com/750x400'}
        mode="aspectFill"
      />
      <View className="detail-content">
        <Text className="detail-title">{app.appName}</Text>
        <View className="detail-meta">
          <Text className="author">作者：{app.user?.name || '未知'}</Text>
          <Text className="type">类型：{app.codeGenType === 'vueProject' ? 'Vue项目' : 'HTML页面'}</Text>
        </View>
        <Text className="detail-desc">{app.initPrompt || '暂无描述'}</Text>

        {versions.length > 0 && (
          <View className="version-section">
            <Text className="section-title">版本历史</Text>
            <View className="version-list">
              {versions.map((v) => (
                <View
                  key={v.version}
                  className={`version-item ${currentVersion === v.version ? 'active' : ''}`}
                  onClick={() => setCurrentVersion(v.version)}
                >
                  <Text className="version-num">v{v.version}</Text>
                  {v.isDeployed && <Text className="deployed-tag">已部署</Text>}
                </View>
              ))}
            </View>
          </View>
        )}

        <View className="action-btns">
          <Button className="chat-btn" onClick={handleChat}>
            开始对话
          </Button>
          <Button className="preview-btn" onClick={handlePreview}>
            预览应用
          </Button>
        </View>
      </View>
    </View>
  )
}
