/**
 * 应用 API
 */

import http from '../utils/http'
import Taro from '@tarojs/taro'

// Types
export interface AppVO {
  id: string
  appName: string
  cover: string
  initPrompt: string
  codeGenType: 'html' | 'vueProject'
  deployKey: string
  deployedTime: string
  priority: number
  userId: string
  editTime: string
  createTime: string
  updateTime: string
  user?: UserVO
}

export interface UserVO {
  id: string
  account: string
  email: string
  name: string
  avatar: string
  profile: string
  role: string
  createTime: string
  updateTime: string
}

export interface AppVersionVO {
  version: string
  createTime: string
  message: string
  isDeployed: boolean
  fileSize: number
  previewUrl: string
  chatHistoryId: string
}

export interface PageResult<T> {
  records: T[]
  pageNumber: number
  pageSize: number
  totalPage: number
  totalRow: number
}

export interface AddAppParams {
  appName: string
  initPrompt: string
  codeGenType: 'html' | 'vueProject'
}

export interface UpdateAppParams {
  id: string
  appName: string
}

// API
export const addApp = (data: AddAppParams) => http.post<AppVO>('/api/app/add', data)

export const updateMyApp = (data: UpdateAppParams) => http.post<AppVO>('/api/app/update/my', data)

export const deleteMyApp = (deleteId: string) => http.post('/api/app/delete', { deleteId })

export const getAppVO = (id: string) => http.get<AppVO>(`/api/app/get/vo?id=${id}`)

export const getMyAppList = (data: { current?: number; pageSize?: number }) =>
  http.post<PageResult<AppVO>>('/api/app/my/list/page/vo', data)

export const getFeaturedAppList = (data: { current?: number; pageSize?: number }) =>
  http.post<PageResult<AppVO>>('/api/app/featured/list/page/vo', data)

export const getAdminAppList = (data: { current?: number; pageSize?: number; appName?: string }) =>
  http.post<PageResult<AppVO>>('/api/app/admin/list/page/vo', data)

export const deleteAppAdmin = (id: string) => http.post('/api/app/admin/delete', { id })

export const getAppVersions = (appId: string) => http.get<AppVersionVO[]>(`/api/app/${appId}/versions`)

export const getAppVersion = (appId: string, version: string) =>
  http.get<AppVersionVO>(`/api/app/${appId}/versions/${version}`)

export const deleteAppVersion = (appId: string, version: string) =>
  http.delete(`/api/app/${appId}/versions/${version}`)

export const deployApp = (appId: string, version: string) =>
  http.post(`/api/app/deploy/${appId}/${version}`)

// 获取流式 AI 响应 (SSE)
export const generateCode = (
  appId: string,
  message: string,
  onMessage: (text: string) => void
): Promise<string> => {
  return new Promise((resolve, reject) => {
    let fullText = ''
    const url = `http://localhost:8081/api/app/chat/gen/code?appId=${appId}&message=${encodeURIComponent(message)}`

    const task = Taro.request({
      url,
      method: 'GET',
      enableChunked: true,
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(fullText)
        } else {
          reject(new Error(`Request failed with status ${res.statusCode}`))
        }
      },
      fail: reject,
    })

    task.onChunkReceived = (res) => {
      const chunk = Taro.util.arrayBufferToString(res.data)
      fullText += chunk
      onMessage(chunk)
    }
  })
}