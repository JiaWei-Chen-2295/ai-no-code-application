/**
 * 登录页面
 */

import React, { useState } from 'react'
import { View, Text, Input, Button } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useUser } from '../../store/user'
import './index.css'

export default function LoginPage() {
  const [account, setAccount] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const { login } = useUser()
  const handleLogin = async () => {
    if (!account || !password) {
      setError('请输入账号和密码')
      return
    }

    setLoading(true)
    setError('')

    try {
      await login(account, password)
      Taro.showToast({ title: '登录成功', icon: 'success' })
      Taro.reLaunch({ url: '/pages/home/index' })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '登录失败'
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <View className="login-container">
      <View className="login-header">
        <Text className="login-title">AI No-Code</Text>
        <Text className="login-subtitle">登录您的账号</Text>
      </View>

      <View className="login-form">
        <View className="input-group">
          <Text className="input-label">账号</Text>
          <Input
            className="input-field"
            placeholder="请输入账号"
            value={account}
            onInput={(e) => setAccount(e.detail.value)}
          />
        </View>

        <View className="input-group">
          <Text className="input-label">密码</Text>
          <Input
            className="input-field"
            password
            placeholder="请输入密码"
            value={password}
            onInput={(e) => setPassword(e.detail.value)}
          />
        </View>

        {error && <Text className="error-text">{error}</Text>}

        <Button
          className="login-btn"
          loading={loading}
          onClick={handleLogin}
        >
          {loading ? '登录中...' : '登录'}
        </Button>

        <View className="register-link">
          <Text>还没有账号？</Text>
          <Text className="link" onClick={() => Taro.navigateTo({ url: '/pages/register/index' })}>
            立即注册
          </Text>
        </View>
      </View>
    </View>
  )
}
