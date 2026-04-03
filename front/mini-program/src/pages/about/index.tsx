/**
 * 关于页面
 */

import React from 'react'
import { View, Text } from '@tarojs/components'
import './index.css'

export default function AboutPage() {
  return (
    <View className="about-container">
      <View className="about-header">
        <View className="app-logo">
          <Text className="logo-text">AI</Text>
        </View>
        <Text className="app-name">AI No-Code</Text>
        <Text className="app-version">v1.0.0</Text>
      </View>

      <View className="about-content">
        <View className="about-section">
          <Text className="section-title">应用介绍</Text>
          <Text className="section-content">
            AI No-Code是一个AI驱动的无代码应用平台，允许用户通过自然语言对话创建和部署应用程序。
          </Text>
        </View>

        <View className="about-section">
          <Text className="section-title">主要功能</Text>
          <Text className="section-content">
            • AI对话生成代码
          </Text>
          <Text className="section-content">
            • 实时预览应用
          </Text>
          <Text className="section-content">
            • 一键部署到公网
          </Text>
          <Text className="section-content">
            • 版本管理
          </Text>
        </View>

        <View className="about-section">
          <Text className="section-title">联系我们</Text>
          <Text className="section-content">
            邮箱：contact@ai-no-code.com
          </Text>
          <Text className="section-content">
            官网：https://ai-no-code.com
          </Text>
        </View>
      </View>

      <View className="about-footer">
        <Text className="footer-text">© 2026 AI No-Code. All rights reserved.</Text>
      </View>
    </View>
  )
}