/**
 * 版本管理页面
 */

import React, { useState, useEffect } from 'react'
import { View, Text, ScrollView } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import { getAppVersions, deleteAppVersion, AppVersionVO } from '../../api/app'
import './index.css'

export default function VersionManagePage() {
  const [versions, setVersions] = useState<AppVersionVO[]>([])
  const [loading, setLoading] = useState(false)
  const [appId, setAppId] = useState<string | null>(null)
  const router = useRouter()

  useEffect(() => {
    const { appId: id } = router.params
    if (id) {
      setAppId(id)
      fetchVersions(id)
    }
  }, [])

  const fetchVersions = async (id: string) => {
    setLoading(true)
    try {
      const res = await getAppVersions(id)
      setVersions(res || [])
    } catch (err) {
      console.error('Failed to fetch versions:', err)
      Taro.showToast({ title: '获取版本列表失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (version: string) => {
    if (!appId) return

    try {
      await Taro.showModal({
        title: '确认删除',
        content: `确定要删除版本 ${version} 吗？此操作不可撤销。`,
        confirmColor: '#ff4d4f'
      })

      await deleteAppVersion(appId, version)
      Taro.showToast({ title: '删除成功', icon: 'success' })
      fetchVersions(appId)
    } catch (err) {
      if (err.errMsg && !err.errMsg.includes('cancel')) {
        console.error('Failed to delete version:', err)
        Taro.showToast({ title: '删除失败', icon: 'none' })
      }
    }
  }

  const handleDeploy = (version: string) => {
    if (!appId) return
    Taro.navigateTo({ url: `/pages/deploy-manage/index?appId=${appId}&version=${version}` })
  }

  const handlePreview = (version: string) => {
    if (!appId) return
    Taro.navigateTo({ url: `/pages/preview/index?appId=${appId}&version=${version}` })
  }

  return (
    <View className="version-manage-container">
      <View className="app-info-header">
        <Text className="app-title">版本管理</Text>
        <Text className="app-id">应用ID: {appId}</Text>
      </View>

      <ScrollView className="version-list" scrollY>
        {loading ? (
          <View className="loading-state">
            <Text>加载中...</Text>
          </View>
        ) : versions.length === 0 ? (
          <View className="empty-state">
            <Text>暂无版本</Text>
          </View>
        ) : (
          versions.map((version) => (
            <View key={version.version} className="version-item">
              <View className="version-info">
                <View className="version-header">
                  <Text className="version-number">v{version.version}</Text>
                  {version.isDeployed && (
                    <View className="deployed-tag">
                      <Text className="tag-text">已部署</Text>
                    </View>
                  )}
                </View>
                <Text className="version-time">{version.createTime?.split('T')[0]}</Text>
                {version.message && (
                  <Text className="version-message">{version.message}</Text>
                )}
                {version.fileSize > 0 && (
                  <Text className="version-size">
                    文件大小: {(version.fileSize / 1024).toFixed(2)} KB
                  </Text>
                )}
              </View>
              <View className="version-actions">
                <View className="action-btn preview-btn" onClick={() => handlePreview(version.version)}>
                  <Text className="action-text">预览</Text>
                </View>
                <View className="action-btn deploy-btn" onClick={() => handleDeploy(version.version)}>
                  <Text className="action-text">部署</Text>
                </View>
                <View className="action-btn delete-btn" onClick={() => handleDelete(version.version)}>
                  <Text className="action-text">删除</Text>
                </View>
              </View>
            </View>
          ))
        )}
      </ScrollView>
    </View>
  )
}