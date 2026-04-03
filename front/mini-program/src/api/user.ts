/**
 * 用户 API
 */

import http from '../utils/http'

// Types
export interface LoginParams {
  userAccount: string
  userPassword: string
}

export interface RegisterParams {
  userAccount: string
  email: string
  userPassword: string
  checkPassword: string
}

export interface UserVO {
  id: number
  account: string
  email: string
  name: string
  avatar: string
  profile: string
  role: string
  createTime: string
  updateTime: string
}

export interface LoginUserVO {
  id: number
  userName: string
  userAvatar: string
  userProfile: string
  userRole: string
  createTime: string
  updateTime: string
  token?: string
}

// API
export const login = (data: LoginParams) => http.post<LoginUserVO>('/api/user/login', data)

export const register = (data: RegisterParams) => http.post<UserVO>('/api/user/register', data)

export const logout = () => http.post('/api/user/logout')

export const getLoginUser = () => http.get<LoginUserVO>('/api/user/get/login')

export const getUserInfo = (id: number) => http.get<UserVO>(`/api/user/get/vo?id=${id}`)

export const updateMyUser = (data: { userName?: string; userAvatar?: string; userProfile?: string }) =>
  http.post<UserVO>('/api/user/update/my', data)