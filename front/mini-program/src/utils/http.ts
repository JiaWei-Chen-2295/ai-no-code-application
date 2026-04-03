/**
 * 请求封装
 * 基于 Taro.request 封装网络请求
 */

import Taro from '@tarojs/taro'

const BASE_URL = 'http://localhost:8081'
const TOKEN_STORAGE_KEY = 'token'
const AUTH_MODE_STORAGE_KEY = 'authMode'

type AuthMode = 'guest' | 'user'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown>
  header?: Record<string, string>
  _retryWithoutToken?: boolean
}

interface ResponseData<T = unknown> {
  code: number
  data: T
  message: string
}

class HttpRequest {
  private baseUrl: string
  private token: string = ''
  private authMode: AuthMode = 'guest'

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl
  }

  initAuthFromStorage() {
    const token = Taro.getStorageSync<string>(TOKEN_STORAGE_KEY)
    const authMode = Taro.getStorageSync<AuthMode>(AUTH_MODE_STORAGE_KEY)
    this.token = typeof token === 'string' ? token : ''
    this.authMode = authMode === 'user' ? 'user' : 'guest'
    console.log('[HTTP] initAuthFromStorage - token:', this.token ? `${this.token.substring(0, 20)}...` : '(empty)')
  }

  setToken(token: string) {
    this.token = token
    Taro.setStorageSync(TOKEN_STORAGE_KEY, token)
    console.log('[HTTP] setToken - token saved:', token ? `${token.substring(0, 20)}...` : '(empty)')
  }

  setAuthMode(mode: AuthMode) {
    this.authMode = mode
    Taro.setStorageSync(AUTH_MODE_STORAGE_KEY, mode)
  }

  clearToken() {
    this.token = ''
    Taro.removeStorageSync(TOKEN_STORAGE_KEY)
  }

  private clearAuthState() {
    this.clearToken()
    this.setAuthMode('guest')
  }

  private redirectToLogin() {
    const pages = Taro.getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const currentRoute = currentPage?.route || ''
    
    // 如果当前页面不是登录页面，则跳转到登录页面
    if (!currentRoute.includes('pages/login/index')) {
      // 使用 reLaunch 关闭所有页面再跳转，避免页面栈问题
      setTimeout(() => {
        Taro.reLaunch({
          url: '/pages/login/index'
        })
      }, 100)
    }
  }

  private isTokenExpired(response: Taro.request.SuccessCallbackResult<ResponseData<unknown>>) {
    const message = response.data?.message || ''
    const code = response.data?.code
    return response.statusCode === 401 || 
           code === 4100 || 
           /access_token|token.*expired|登录态已过期|令牌过期|未登录/i.test(message)
  }

  private async retryAsGuestIfNeeded<T>(
    response: Taro.request.SuccessCallbackResult<ResponseData<T>>,
    options: RequestOptions
  ) {
    if (!this.isTokenExpired(response) || options._retryWithoutToken || this.authMode !== 'guest') {
      return null
    }

    this.clearAuthState()
    return this.request<T>({
      ...options,
      _retryWithoutToken: true,
    })
  }

  async request<T = unknown>(options: RequestOptions): Promise<T> {
    const { url, method = 'GET', data, header = {} } = options

    // 添加 token 到请求头
    if (this.token) {
      header['Authorization'] = `Bearer ${this.token}`
      console.log('[HTTP] request - adding Authorization header for:', url)
    } else {
      console.log('[HTTP] request - no token, request without auth:', url)
    }

    header['Content-Type'] = header['Content-Type'] || 'application/json'

    try {
      const response = await Taro.request<ResponseData<T>>({
        url: url.startsWith('http') ? url : `${this.baseUrl}${url}`,
        method,
        data,
        header,
      })

      const retriedResult = await this.retryAsGuestIfNeeded(response, options)
      if (retriedResult !== null) {
        return retriedResult
      }

      const result = response.data

      if (result.code === 0) {
        return result.data as T
      } else {
        if (this.isTokenExpired(response)) {
          this.clearAuthState()
          this.redirectToLogin()
        }
        Taro.showToast({
          title: result.message || '请求失败',
          icon: 'none',
        })
        throw new Error(result.message)
      }
    } catch (error) {
      console.error('Request error:', error)
      Taro.showToast({
        title: '网络错误',
        icon: 'none',
      })
      throw error
    }
  }

  get<T = unknown>(url: string, data?: Record<string, unknown>): Promise<T> {
    return this.request<T>({ url, method: 'GET', data })
  }

  post<T = unknown>(url: string, data?: Record<string, unknown>): Promise<T> {
    return this.request<T>({ url, method: 'POST', data })
  }

  put<T = unknown>(url: string, data?: Record<string, unknown>): Promise<T> {
    return this.request<T>({ url, method: 'PUT', data })
  }

  delete<T = unknown>(url: string, data?: Record<string, unknown>): Promise<T> {
    return this.request<T>({ url, method: 'DELETE', data })
  }
}

export const http = new HttpRequest(BASE_URL)

export default http
