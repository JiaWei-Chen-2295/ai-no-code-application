/**
 * 我的页面
 */

import React from 'react'
import { View, Text, Button, Image } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useUser } from '../../store/user'
import './index.css'

export default function MyPage() {
  const { user, logout } = useUser()

  const handleLogout = async () => {
    try {
      await logout()
      Taro.reLaunch({ url: '/pages/login/index' })
    } catch (err) {
      Taro.showToast({ title: '退出失败', icon: 'none' })
    }
  }

  if (!user) {
    return (
      <View className="my-container">
        <View className="not-login">
          <Text>请先登录</Text>
        </View>
      </View>
    )
  }

  return (
    <View className="my-container">
      <View className="my-header">
        <Image
          className="avatar"
          src={user.userAvatar || 'https://via.placeholder.com/120'}
        />
        <Text className="nickname">{user.userName}</Text>
        <Text className="role">{user.userRole === 'admin' ? '管理员' : '用户'}</Text>
      </View>

      <View className="my-menu">
        <View className="menu-item">
          <Text className="menu-text">我的应用</Text>
          <Text className="menu-arrow">{'>'}</Text>
        </View>
        <View className="menu-item">
          <Text className="menu-text">设置</Text>
          <Text className="menu-arrow">{'>'}</Text>
        </View>
        <View className="menu-item">
          <Text className="menu-text">关于我们</Text>
          <Text className="menu-arrow">{'>'}</Text>
        </View>
      </View>

      <Button className="logout-btn" onClick={handleLogout}>
        退出登录
      </Button>
    </View>
  )
}
