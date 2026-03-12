import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../modules/user'
import * as authUtils from '@/utils/auth'
import * as authApi from '@/api/auth'

// Mock API 和工具函数
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  getUserInfo: vi.fn(),
}))

vi.mock('@/utils/auth', () => ({
  getToken: vi.fn(),
  setToken: vi.fn(),
  removeToken: vi.fn(),
  getUser: vi.fn(),
  setUser: vi.fn(),
  removeUser: vi.fn(),
  clearAuth: vi.fn(),
}))

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    
    // 默认返回值
    authUtils.getToken.mockReturnValue(null)
    authUtils.getUser.mockReturnValue(null)
  })

  describe('初始状态', () => {
    it('token 初始值应从 localStorage 获取', () => {
      authUtils.getToken.mockReturnValue('stored-token')
      
      // 重新创建 pinia 以获取新的初始值
      setActivePinia(createPinia())
      const store = useUserStore()
      
      expect(store.token).toBe('stored-token')
    })

    it('userInfo 初始值应从 localStorage 获取', () => {
      const mockUser = { id: 1, username: 'admin' }
      authUtils.getUser.mockReturnValue(mockUser)
      
      setActivePinia(createPinia())
      const store = useUserStore()
      
      expect(store.userInfo).toEqual(mockUser)
    })
  })

  describe('Getters', () => {
    it('isLoggedIn 有 token 时应返回 true', () => {
      const store = useUserStore()
      store.token = 'test-token'
      
      expect(store.isLoggedIn).toBe(true)
    })

    it('isLoggedIn 无 token 时应返回 false', () => {
      const store = useUserStore()
      store.token = ''
      
      expect(store.isLoggedIn).toBe(false)
    })

    it('isAdmin 角色为 admin 时应返回 true', () => {
      const store = useUserStore()
      store.userInfo = { role: 'admin' }
      
      expect(store.isAdmin).toBe(true)
    })

    it('isAdmin 角色为 operator 时应返回 false', () => {
      const store = useUserStore()
      store.userInfo = { role: 'operator' }
      
      expect(store.isAdmin).toBe(false)
    })
  })

  describe('Actions', () => {
    it('login 成功应设置 token 和 userInfo', async () => {
      const mockResponse = {
        data: {
          token: 'new-token',
          id: 1,
          username: 'admin',
          realName: '管理员',
          role: 'admin'
        }
      }
      authApi.login.mockResolvedValue(mockResponse)
      
      const store = useUserStore()
      await store.login({ username: 'admin', password: 'admin123' })
      
      expect(store.token).toBe('new-token')
      expect(store.userInfo.username).toBe('admin')
      expect(authUtils.setToken).toHaveBeenCalledWith('new-token')
      expect(authUtils.setUser).toHaveBeenCalled()
    })

    it('logout 应清除所有用户信息', () => {
      const store = useUserStore()
      store.token = 'test-token'
      store.userInfo = { id: 1 }
      
      store.logout()
      
      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(authUtils.clearAuth).toHaveBeenCalled()
    })
  })
})
