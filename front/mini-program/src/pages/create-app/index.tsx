/**
 * 创建应用页面
 */

import React, { useState } from 'react'
import { View, Text, Input, Picker, Button } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { addApp } from '../../api/app'
import './index.css'

export default function CreateAppPage() {
  const [appName, setAppName] = useState('')
  const [initPrompt, setInitPrompt] = useState('')
  const [codeGenType, setCodeGenType] = useState(0)
  const [loading, setLoading] = useState(false)
  const codeGenTypes = ['HTML 单页面', 'Vue 项目']

  const handleCreate = async () => {
    if (!appName || !initPrompt) {
      Taro.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }

    setLoading(true)
    try {
      const type = codeGenType === 0 ? 'html' : 'vueProject'
      const app = await addApp({
        appName,
        initPrompt,
        codeGenType: type,
      })
      Taro.showToast({ title: '创建成功', icon: 'success' })
      Taro.redirectTo({ url: `/pages/app-detail/index?id=${app.id}` })
    } catch (err) {
      Taro.showToast({ title: '创建失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <View className="create-container">
      <Text className="create-title">创建新应用</Text>

      <View className="form-group">
        <Text className="form-label">应用名称</Text>
        <Input
          className="form-input"
          placeholder="给应用起个名字"
          value={appName}
          onInput={(e) => setAppName(e.detail.value)}
        />
      </View>

      <View className="form-group">
        <Text className="form-label">代码类型</Text>
        <Picker
          mode="selector"
          range={codeGenTypes}
          onChange={(e) => setCodeGenType(Number(e.detail.value))}
        >
          <View className="picker-display">
            {codeGenTypes[codeGenType]}
          </View>
        </Picker>
      </View>

      <View className="form-group">
        <Text className="form-label">应用描述</Text>
        <Input
          className="form-input desc"
          placeholder="描述你想要创建的应用功能..."
          value={initPrompt}
          onInput={(e) => setInitPrompt(e.detail.value)}
        />
      </View>

      <Button className="create-btn" loading={loading} onClick={handleCreate}>
        {loading ? '创建中...' : '创建应用'}
      </Button>
    </View>
  )
}
