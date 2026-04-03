/**
 * 注册页面
 */

import React, { useState } from 'react'
import { View, Text, Input, Button } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { register } from '../../api/user'
import './index.css'

export default function RegisterPage() {
  const [account, setAccount] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [checkPassword, setCheckPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const handleRegister = async () => {
    if (!account || !email || !password || !checkPassword) {
      setError('请填写所有字段')
      return
    }

    if (password !== checkPassword) {
      setError('两次密码不一致')
      return
    }

    setLoading(true)
    setError('')

    try {
      await register({ userAccount: account, email, userPassword: password, checkPassword })
      Taro.showToast({ title: '注册成功', icon: 'success' })
      Taro.redirectTo({ url: '/pages/login/index' })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '注册失败'
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <View className="register-container">
      <View className="register-header">
        <Text className="register-title">注册账号</Text>
      </View>

      <View className="register-form">
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
          <Text className="input-label">邮箱</Text>
          <Input
            className="input-field"
            placeholder="请输入邮箱"
            value={email}
            onInput={(e) => setEmail(e.detail.value)}
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

        <View className="input-group">
          <Text className="input-label">确认密码</Text>
          <Input
            className="input-field"
            password
            placeholder="请再次输入密码"
            value={checkPassword}
            onInput={(e) => setCheckPassword(e.detail.value)}
          />
        </View>

        {error && <Text className="error-text">{error}</Text>}

        <Button className="register-btn" loading={loading} onClick={handleRegister}>
          {loading ? '注册中...' : '注册'}
        </Button>

        <View className="login-link">
          <Text>已有账号？</Text>
          <Text className="link" onClick={() => Taro.navigateBack()}>
            立即登录
          </Text>
        </View>
      </View>
    </View>
  )
}
