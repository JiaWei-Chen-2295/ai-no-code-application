import Taro from '@tarojs/taro'

export interface SafeSystemInfo {
  brand: string
  model: string
  platform: string
  screenWidth: number
  screenHeight: number
  windowWidth: number
  windowHeight: number
  pixelRatio: number
  statusBarHeight: number
}

const DEFAULT_SYSTEM_INFO: SafeSystemInfo = {
  brand: '',
  model: '',
  platform: '',
  screenWidth: 375,
  screenHeight: 667,
  windowWidth: 375,
  windowHeight: 667,
  pixelRatio: 2,
  statusBarHeight: 0,
}

let cachedSystemInfo: SafeSystemInfo | null = null

function toNumber(value: unknown, fallback: number) {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback
}

function toString(value: unknown) {
  return typeof value === 'string' ? value : ''
}

export function getSafeSystemInfo(): SafeSystemInfo {
  if (cachedSystemInfo) {
    return cachedSystemInfo
  }

  try {
    let deviceInfo: Record<string, any> = {}
    let windowInfo: Record<string, any> = {}
    let appBaseInfo: Record<string, any> = {}
    let systemInfo: Record<string, any> = {}

    try {
      if (typeof Taro.getDeviceInfo === 'function') {
        const result = Taro.getDeviceInfo()
        if (result && typeof result === 'object') {
          deviceInfo = result as Record<string, any>
        }
      }
    } catch (e) {
      console.warn('getDeviceInfo failed:', e)
    }

    try {
      if (typeof Taro.getWindowInfo === 'function') {
        const result = Taro.getWindowInfo()
        if (result && typeof result === 'object') {
          windowInfo = result as Record<string, any>
        }
      }
    } catch (e) {
      console.warn('getWindowInfo failed:', e)
    }

    try {
      if (typeof Taro.getAppBaseInfo === 'function') {
        const result = Taro.getAppBaseInfo()
        if (result && typeof result === 'object') {
          appBaseInfo = result as Record<string, any>
        }
      }
    } catch (e) {
      console.warn('getAppBaseInfo failed:', e)
    }

    if (!deviceInfo || !windowInfo || Object.keys(deviceInfo).length === 0) {
      try {
        if (typeof Taro.getSystemInfoSync === 'function') {
          const result = Taro.getSystemInfoSync()
          if (result && typeof result === 'object') {
            systemInfo = result as Record<string, any>
          }
        }
      } catch (e) {
        console.warn('getSystemInfoSync failed:', e)
      }
    }

    cachedSystemInfo = {
      brand: toString(deviceInfo.brand || systemInfo.brand || DEFAULT_SYSTEM_INFO.brand),
      model: toString(deviceInfo.model || systemInfo.model || DEFAULT_SYSTEM_INFO.model),
      platform: toString(appBaseInfo.platform || systemInfo.platform || DEFAULT_SYSTEM_INFO.platform),
      screenWidth: toNumber(deviceInfo.screenWidth || windowInfo.screenWidth || systemInfo.screenWidth, DEFAULT_SYSTEM_INFO.screenWidth),
      screenHeight: toNumber(deviceInfo.screenHeight || windowInfo.screenHeight || systemInfo.screenHeight, DEFAULT_SYSTEM_INFO.screenHeight),
      windowWidth: toNumber(windowInfo.windowWidth || systemInfo.windowWidth, DEFAULT_SYSTEM_INFO.windowWidth),
      windowHeight: toNumber(windowInfo.windowHeight || systemInfo.windowHeight, DEFAULT_SYSTEM_INFO.windowHeight),
      pixelRatio: toNumber(windowInfo.pixelRatio || systemInfo.pixelRatio, DEFAULT_SYSTEM_INFO.pixelRatio),
      statusBarHeight: toNumber(windowInfo.statusBarHeight || systemInfo.statusBarHeight, DEFAULT_SYSTEM_INFO.statusBarHeight),
    }
  } catch (error) {
    console.warn('Failed to initialize system info safely:', error)
    cachedSystemInfo = { ...DEFAULT_SYSTEM_INFO }
  }

  return cachedSystemInfo
}

export function initSystemInfo() {
  const systemInfo = getSafeSystemInfo()
  Taro.setStorageSync('systemInfo', systemInfo)
  return systemInfo
}
