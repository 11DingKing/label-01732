import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { 
  getToken, 
  setToken, 
  removeToken, 
  getUser, 
  setUser, 
  removeUser, 
  clearAuth 
} from '../auth'

describe('Auth Utils', () => {
  beforeEach(() => {
    // 清理 localStorage
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('Token 操作', () => {
    it('setToken 应该正确存储 token', () => {
      const token = 'test-jwt-token'
      setToken(token)
      expect(localStorage.getItem('card_manager_token')).toBe(token)
    })

    it('getToken 应该正确获取 token', () => {
      const token = 'test-jwt-token'
      localStorage.setItem('card_manager_token', token)
      expect(getToken()).toBe(token)
    })

    it('getToken 不存在时应返回 null', () => {
      expect(getToken()).toBeNull()
    })

    it('removeToken 应该正确移除 token', () => {
      localStorage.setItem('card_manager_token', 'test-token')
      removeToken()
      expect(localStorage.getItem('card_manager_token')).toBeNull()
    })
  })

  describe('User 操作', () => {
    const testUser = {
      id: 1,
      username: 'admin',
      realName: '管理员',
      role: 'admin'
    }

    it('setUser 应该正确存储用户信息', () => {
      setUser(testUser)
      const stored = localStorage.getItem('card_manager_user')
      expect(JSON.parse(stored)).toEqual(testUser)
    })

    it('getUser 应该正确获取用户信息', () => {
      localStorage.setItem('card_manager_user', JSON.stringify(testUser))
      expect(getUser()).toEqual(testUser)
    })

    it('getUser 不存在时应返回 null', () => {
      expect(getUser()).toBeNull()
    })

    it('removeUser 应该正确移除用户信息', () => {
      localStorage.setItem('card_manager_user', JSON.stringify(testUser))
      removeUser()
      expect(localStorage.getItem('card_manager_user')).toBeNull()
    })
  })

  describe('clearAuth', () => {
    it('应该同时清除 token 和用户信息', () => {
      localStorage.setItem('card_manager_token', 'test-token')
      localStorage.setItem('card_manager_user', JSON.stringify({ id: 1 }))
      
      clearAuth()
      
      expect(localStorage.getItem('card_manager_token')).toBeNull()
      expect(localStorage.getItem('card_manager_user')).toBeNull()
    })
  })
})
