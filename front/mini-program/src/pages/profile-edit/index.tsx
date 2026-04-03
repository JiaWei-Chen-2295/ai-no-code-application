/**
 * 用户资料编辑页面
 */

import React, { useState, useEffect } from 'react'
import { View, Text, Input, Button, Image } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useUser } from '../../store/user'
import { updateMyUser } from '../../api/user'
import './index.css'

export default function ProfileEditPage() {
  const { user, updateUser } = useUser()
  const [userName, setUserName] = useState('')
  const [userAvatar, setUserAvatar] = useState('')
  const [userProfile, setUserProfile] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (user) {
      setUserName(user.userName || '')
      setUserAvatar(user.userAvatar || '')
      setUserProfile(user.userProfile || '')
    }
  }, [user])

  const handleChooseAvatar = async () => {
    try {
      const res = await Taro.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera']
      })
      if (res.tempFilePaths && res.tempFilePaths.length > 0) {
        setUserAvatar(res.tempFilePaths[0])
      }
    } catch (err) {
      console.error('Failed to choose avatar:', err)
    }
  }

  const handleSave = async () => {
    if (!userName.trim()) {
      Taro.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }

    setLoading(true)
    try {
      await updateMyUser({
        userName: userName.trim(),
        userAvatar,
        userProfile: userProfile.trim()
      })
      
      // 更新本地状态
      updateUser({
        userName: userName.trim(),
        userAvatar,
        userProfile: userProfile.trim()
      })
      
      Taro.showToast({ title: '保存成功', icon: 'success' })
      Taro.navigateBack()
    } catch (err) {
      console.error('Failed to update profile:', err)
      Taro.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <View className="profile-edit-container">
      <View className="avatar-section">
        <Text className="section-label">头像</Text>
        <View className="avatar-wrapper" onClick={handleChooseAvatar}>
          <Image
            className="avatar-image"
            src={userAvatar || 'https://via.placeholder.com/120'}
            mode="aspectFill"
          />
          <Text className="avatar-hint">点击更换</Text>
        </View>
      </View>

      <View className="form-section">
        <View className="form-item">
          <Text className="form-label">昵称</Text>
          <Input
            className="form-input"
            placeholder="请输入昵称"
            value={userName}
            onInput={(e) => setUserName(e.detail.value)}
          />
        </View>

        <View className="form-item">
          <Text className="form-label">简介</Text>
          <Input
            className="form-input"
            placeholder="请输入个人简介"
            value={userProfile}
            onInput={(e) => setUserProfile(e.detail.value)}
          />
        </View>
      </View>

      <Button
        className="save-btn"
        loading={loading}
        onClick={handleSave}
      >
        {loading ? '保存中...' : '保存'}
      </Button>
    </View>
  )
}