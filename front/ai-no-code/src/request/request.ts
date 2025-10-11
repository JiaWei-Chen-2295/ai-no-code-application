import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true, // 支持cookie
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response
    // 如果是下载文件等特殊情况，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    return response
  },
  (error) => {
    // 处理HTTP错误
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          message.error('未授权，请重新登录')
          // 可以在这里跳转到登录页
          break
        case 403:
          message.error('拒绝访问')
          break
        case 404:
          message.error('请求地址出错')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error('网络错误')
      }
    } else {
      message.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request