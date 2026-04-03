/**
 * 用户状态管理
 */

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import Taro from '@tarojs/taro'
import { login as loginApi, getLoginUser, logout as logoutApi, LoginUserVO } from '../api/user'
import http from '../utils/http'

interface UserContextType {
  user: LoginUserVO | null
  loading: boolean
  login: (account: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

const UserContext = createContext<UserContextType | undefined>(undefined)

export const UserProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<LoginUserVO | null>(null)
  const [loading, setLoading] = useState(true)

  const refreshUser = async () => {
    try {
      const userInfo = await getLoginUser()
      setUser(userInfo)
      http.setAuthMode('user')
    } catch (error) {
      console.warn('Failed to refresh user info:', error)
      setUser(null)
      // 如果获取用户信息失败，切换到游客模式
      http.setAuthMode('guest')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    http.initAuthFromStorage()
    refreshUser()
  }, [])

  const login = async (account: string, password: string) => {
    try {
      const userInfo = await loginApi({ userAccount: account, userPassword: password })
      setUser(userInfo)
      // 保存 JWT token
      if (userInfo.token) {
        http.setToken(userInfo.token)
      }
      // 登录成功后设置用户模式
      http.setAuthMode('user')
    } catch (error) {
      console.error('Login failed:', error)
      // 登录失败时确保使用游客模式
      http.setAuthMode('guest')
      throw error
    }
  }

  const logout = async () => {
    await logoutApi()
    setUser(null)
    http.clearToken()
    http.setAuthMode('guest')
    Taro.removeStorageSync('token')
  }

  return (
    <UserContext.Provider value={{ user, loading, login, logout, refreshUser }}>
      {children}
    </UserContext.Provider>
  )
}

export const useUser = () => {
  const context = useContext(UserContext)
  if (!context) {
    throw new Error('useUser must be used within UserProvider')
  }
  return context
}
