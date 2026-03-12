import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken, getUser, setUser, removeUser, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUser() || null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.role === 'admin',
    username: (state) => state.userInfo?.username || '',
    realName: (state) => state.userInfo?.realName || '',
    role: (state) => state.userInfo?.role || '',
    roleName: (state) => state.userInfo?.roleName || '',
  },

  actions: {
    /**
     * 登录
     */
    async login(loginData) {
      const res = await loginApi(loginData)
      const { token, ...userInfo } = res.data
      
      this.token = token
      this.userInfo = userInfo
      
      setToken(token)
      setUser(userInfo)
      
      return res
    },

    /**
     * 获取用户信息
     */
    async fetchUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data
      setUser(res.data)
      return res
    },

    /**
     * 登出
     */
    logout() {
      this.token = ''
      this.userInfo = null
      clearAuth()
    },
  },
})
