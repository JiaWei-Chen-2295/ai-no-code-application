/**
 * 设置页面
 */

import React from 'react'
import { View, Text } from '@tarojs/components'
import Taro from '@tarojs/taro'
import './index.css'

export default function SettingsPage() {
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