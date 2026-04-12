/**
 * 聊天记录 API
 */

import http from '../utils/http'

// Types
export interface ChatHistory {
  id: string
  message: string
  messageType: number
  appId: string
  userId: string
  isCode: boolean
  codeVersion: string
  createTime: string
  updateTime: string
  isDelete: number
}

export interface PageResult<T> {
  records: T[]
  pageNumber: number
  pageSize: number
  totalPage: number
  totalRow: number
}

// API
export const getChatHistory = (data: { appId: string; pageSize?: number; lastCreateTime?: string }) =>
  http.get<PageResult<ChatHistory>>('/api/chatHistory/app/' + data.appId + '?pageSize=' + (data.pageSize || 10) + (data.lastCreateTime ? '&lastCreateTime=' + data.lastCreateTime : ''))

export const getAdminChatHistory = (data: {
  current?: number
  pageSize?: number
  appId?: string
  message?: string
  messageType?: number
  userId?: string
}) => http.post<PageResult<ChatHistory>>('/api/chatHistory/admin/list/page/vo', data)