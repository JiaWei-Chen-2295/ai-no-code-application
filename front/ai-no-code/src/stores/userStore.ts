import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  const loginUser = ref<API.LoginUserVO>()

  /**
   * 获取登录用户信息
   */
  const fetchLoginUser = async () => {
    try {
      const res = await getLoginUser()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  /**
   * 设置登录用户信息
   */
  const setLoginUser = (user: API.LoginUserVO) => {
    loginUser.value = user
  }

  /**
   * 清除登录用户信息
   */
  const clearLoginUser = () => {
    loginUser.value = undefined
  }

  return {
    loginUser,
    fetchLoginUser,
    setLoginUser,
    clearLoginUser,
  }
})