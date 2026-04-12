/**
 * 设置页面
 */

import React, { useState } from 'react'
import { View, Text, Input } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { setApiBaseUrl, getApiBaseUrl } from '../../utils/http'
import './index.css'

export default function SettingsPage() {
  const [apiUrl, setApiUrl] = useState(getApiBaseUrl())

  const handleAccountSecurity = () => {
    Taro.showToast({ title: '账号安全功能开发中', icon: 'none' })
  }

  const handleNotificationSettings = () => {
    Taro.showToast({ title: '通知设置功能开发中', icon: 'none' })
  }

  const handlePrivacySettings = () => {
    Taro.showToast({ title: '隐私设置功能开发中', icon: 'none' })
  }

  const handleClearCache = () => {
    Taro.showModal({
      title: '清除缓存',
      content: '确定要清除缓存吗？',
      success: (res) => {
        if (res.confirm) {
          Taro.showToast({ title: '缓存已清除', icon: 'success' })
        }
      }
    })
  }

  const handleApiUrlChange = (value: string) => {
    setApiUrl(value)
  }

  const handleSaveApiUrl = () => {
    if (!apiUrl.trim()) {
      Taro.showToast({ title: '请输入有效的API地址', icon: 'none' })
      return
    }
    
    try {
      new URL(apiUrl) // 验证URL格式
      setApiBaseUrl(apiUrl.trim())
      Taro.showToast({ title: 'API地址已保存', icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: '请输入有效的URL格式', icon: 'none' })
    }
  }

  return (
    <View className="settings-container">
      <View className="settings-section">
        <View className="settings-item" onClick={handleAccountSecurity}>
          <Text className="item-text">账号安全</Text>
          <Text className="item-arrow">{'>'}</Text>
        </View>
        <View className="settings-item" onClick={handleNotificationSettings}>
          <Text className="item-text">通知设置</Text>
          <Text className="item-arrow">{'>'}</Text>
        </View>
        <View className="settings-item" onClick={handlePrivacySettings}>
          <Text className="item-text">隐私设置</Text>
          <Text className="item-arrow">{'>'}</Text>
        </View>
      </View>

      <View className="settings-section">
        <Text className="section-title">API 配置</Text>
        <View className="api-config">
          <Text className="config-label">API 基础地址</Text>
          <Input
            className="config-input"
            value={apiUrl}
            onInput={(e) => handleApiUrlChange(e.detail.value)}
            placeholder="请输入API地址"
          />
          <View className="config-actions">
            <Text className="save-btn" onClick={handleSaveApiUrl}>保存</Text>
          </View>
        </View>
      </View>

      <View className="settings-section">
        <View className="settings-item" onClick={handleClearCache}>
          <Text className="item-text">清除缓存</Text>
          <Text className="item-arrow">{'>'}</Text>
        </View>
      </View>

      <View className="settings-section">
        <View className="settings-item" onClick={() => Taro.navigateTo({ url: '/pages/about/index' })}>
          <Text className="item-text">版本信息</Text>
          <Text className="item-arrow">{'>'}</Text>
        </View>
      </View>
    </View>
  )
}