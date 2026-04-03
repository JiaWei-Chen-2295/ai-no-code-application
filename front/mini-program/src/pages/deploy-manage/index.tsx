/**
 * 部署管理页面
 */

import React, { useState, useEffect } from 'react'
import { View, Text, Button } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import { deployApp, getAppVO, AppVO } from '../../api/app'
import './index.css'

export default function DeployManagePage() {
  const [app, setApp] = useState<AppVO | null>(null)
  const [loading, setLoading] = useState(false)
  const [deploying, setDeploying] = useState(false)
  const [appId, setAppId] = useState<string | null>(null)
  const [version, setVersion] = useState('')
  const router = useRouter()

  useEffect(() => {
    const { appId: id, version: v } = router.params
    if (id) {
      setAppId(id)
      setVersion(v || '')
      fetchAppDetail(id)
    }
  }, [])

  const fetchAppDetail = async (id: string) => {
    setLoading(true)
    try {
      const res = await getAppVO(id)
      setApp(res)
    } catch (err) {
      console.error('Failed to fetch app detail:', err)
      Taro.showToast({ title: '获取应用信息失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const handleDeploy = async () => {
    if (!appId) return

    setDeploying(true)
    try {
      await deployApp(appId, version)
      Taro.showToast({ title: '部署成功', icon: 'success' })
      fetchAppDetail(appId)
    } catch (err) {
      console.error('Failed to deploy app:', err)
      Taro.showToast({ title: '部署失败', icon: 'none' })
    } finally {
      setDeploying(false)
    }
  }

  const handleCopyUrl = () => {
    if (app?.deployKey) {
      const url = `https://your-domain.com/static/${app.deployKey}`
      Taro.setClipboardData({
        data: url,
        success: () => {
          Taro.showToast({ title: '链接已复制', icon: 'success' })
        }
      })
    }
  }

  if (loading) {
    return (
      <View className="deploy-manage-container">
        <View className="loading-state">
          <Text>加载中...</Text>
        </View>
      </View>
    )
  }

  if (!app) {
    return (
      <View className="deploy-manage-container">
        <View className="error-state">
          <Text>应用不存在</Text>
        </View>
      </View>
    )
  }

  return (
    <View className="deploy-manage-container">
      <View className="app-info-section">
        <Text className="app-name">{app.appName}</Text>
        <Text className="app-type">
          类型: {app.codeGenType === 'vueProject' ? 'Vue项目' : 'HTML页面'}
        </Text>
        {version && (
          <Text className="app-version">版本: v{version}</Text>
        )}
      </View>

      <View className="deploy-status-section">
        <Text className="section-title">部署状态</Text>
        <View className="status-item">
          <Text className="status-label">当前状态:</Text>
          <Text className="status-value">
            {app.deployKey ? '已部署' : '未部署'}
          </Text>
        </View>
        {app.deployedTime && (
          <View className="status-item">
            <Text className="status-label">部署时间:</Text>
            <Text className="status-value">{app.deployedTime?.split('T')[0]}</Text>
          </View>
        )}
      </View>

      {app.deployKey && (
        <View className="deploy-url-section">
          <Text className="section-title">部署地址</Text>
          <View className="url-box">
            <Text className="url-text">
              https://your-domain.com/static/{app.deployKey}
            </Text>
            <View className="copy-btn" onClick={handleCopyUrl}>
              <Text className="copy-text">复制</Text>
            </View>
          </View>
        </View>
      )}

      <View className="deploy-actions">
        <Button
          className="deploy-btn"
          loading={deploying}
          onClick={handleDeploy}
        >
          {deploying ? '部署中...' : '重新部署'}
        </Button>
        {app.deployKey && (
          <Button
            className="cancel-btn"
            onClick={() => {
              Taro.showToast({ title: '取消部署功能开发中', icon: 'none' })
            }}
          >
            取消部署
          </Button>
        )}
      </View>
    </View>
  )
}