/**
 * 应用管理页面
 */

import React, { useState, useEffect } from 'react'
import { View, Text, Input, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getAdminAppList, deleteAppAdmin, AppVO } from '../../api/app'
import './index.css'

export default function AppManagePage() {
  const [apps, setApps] = useState<AppVO[]>([])
  const [loading, setLoading] = useState(false)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)

  useEffect(() => {
    fetchApps()
  }, [currentPage])

  const fetchApps = async () => {
    setLoading(true)
    try {
      const res = await getAdminAppList({
        current: currentPage,
        pageSize: 20,
        appName: searchKeyword || undefined
      })
      setApps(res.records || [])
      setTotalPages(res.totalPage || 1)
    } catch (err) {
      console.error('Failed to fetch apps:', err)
      Taro.showToast({ title: '获取应用列表失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    setCurrentPage(1)
    fetchApps()
  }

  const handleDelete = async (appId: string) => {
    try {
      await Taro.showModal({
        title: '确认删除',
        content: '确定要删除这个应用吗？此操作不可撤销。',
        confirmColor: '#ff4d4f'
      })
      
      await deleteAppAdmin(appId)
      Taro.showToast({ title: '删除成功', icon: 'success' })
      fetchApps()
    } catch (err) {
      if (err.errMsg && !err.errMsg.includes('cancel')) {
        console.error('Failed to delete app:', err)
        Taro.showToast({ title: '删除失败', icon: 'none' })
      }
    }
  }

  const handleEdit = (appId: string) => {
    Taro.navigateTo({ url: `/pages/app-detail/index?id=${appId}` })
  }

  return (
    <View className="app-manage-container">
      <View className="search-section">
        <Input
          className="search-input"
          placeholder="搜索应用名称"
          value={searchKeyword}
          onInput={(e) => setSearchKeyword(e.detail.value)}
          onConfirm={handleSearch}
        />
        <View className="search-btn" onClick={handleSearch}>
          <Text className="search-btn-text">搜索</Text>
        </View>
      </View>

      <ScrollView className="app-list" scrollY>
        {loading ? (
          <View className="loading-state">
            <Text>加载中...</Text>
          </View>
        ) : apps.length === 0 ? (
          <View className="empty-state">
            <Text>暂无应用</Text>
          </View>
        ) : (
          apps.map((app) => (
            <View key={app.id} className="app-item">
              <View className="app-info">
                <Text className="app-name">{app.appName}</Text>
                <Text className="app-type">{app.codeGenType === 'vueProject' ? 'Vue项目' : 'HTML页面'}</Text>
                <Text className="app-time">{app.createTime?.split('T')[0]}</Text>
              </View>
              <View className="app-actions">
                <View className="action-btn edit-btn" onClick={() => handleEdit(app.id)}>
                  <Text className="action-text">编辑</Text>
                </View>
                <View className="action-btn delete-btn" onClick={() => handleDelete(app.id)}>
                  <Text className="action-text">删除</Text>
                </View>
              </View>
            </View>
          ))
        )}
      </ScrollView>

      {totalPages > 1 && (
        <View className="pagination">
          <View
            className={`page-btn ${currentPage === 1 ? 'disabled' : ''}`}
            onClick={() => currentPage > 1 && setCurrentPage(currentPage - 1)}
          >
            <Text>上一页</Text>
          </View>
          <Text className="page-info">{currentPage}/{totalPages}</Text>
          <View
            className={`page-btn ${currentPage === totalPages ? 'disabled' : ''}`}
            onClick={() => currentPage < totalPages && setCurrentPage(currentPage + 1)}
          >
            <Text>下一页</Text>
          </View>
        </View>
      )}
    </View>
  )
}