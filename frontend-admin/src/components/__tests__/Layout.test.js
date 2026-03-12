import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import Layout from '../Layout/index.vue'
import * as authUtils from '@/utils/auth'

// Mock auth utils
vi.mock('@/utils/auth', () => ({
  getToken: vi.fn().mockReturnValue('test-token'),
  getUser: vi.fn().mockReturnValue({ id: 1, username: 'admin', role: 'admin', realName: '管理员' }),
}))

// 创建测试用的 router
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: { template: '<div>Dashboard</div>' },
      meta: { title: '数据概览' }
    },
    {
      path: '/card/generate',
      name: 'CardGenerate',
      component: { template: '<div>Card Generate</div>' },
      meta: { title: '一键发卡' }
    }
  ]
})

describe('Layout 组件', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    await router.push('/dashboard')
    await router.isReady()
  })

  it('应该正确渲染布局结构', () => {
    const wrapper = mount(Layout, {
      global: {
        plugins: [router, ElementPlus],
        stubs: {
          'router-view': true,
        }
      }
    })

    // 检查基本结构
    expect(wrapper.find('.layout-container').exists()).toBe(true)
    expect(wrapper.find('.sidebar').exists()).toBe(true)
    expect(wrapper.find('.main-container').exists()).toBe(true)
  })

  it('应该显示用户名', () => {
    const wrapper = mount(Layout, {
      global: {
        plugins: [router, ElementPlus],
        stubs: {
          'router-view': true,
        }
      }
    })

    // 应该显示用户名
    expect(wrapper.text()).toContain('管理员')
  })
})
