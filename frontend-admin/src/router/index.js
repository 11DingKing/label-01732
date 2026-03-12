import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/public/query',
    name: 'PublicQuery',
    component: () => import('@/views/public/query.vue'),
    meta: { title: '卡密查询', requiresAuth: false },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据统计', icon: 'DataLine', requiresAuth: true },
      },
      {
        path: 'card/generate',
        name: 'CardGenerate',
        component: () => import('@/views/card/generate.vue'),
        meta: { title: '一键发卡', icon: 'Plus', requiresAuth: true },
      },
      {
        path: 'card/list',
        name: 'CardList',
        component: () => import('@/views/card/list.vue'),
        meta: { title: '卡密列表', icon: 'List', requiresAuth: true },
      },
      {
        path: 'card/batch',
        name: 'CardBatch',
        component: () => import('@/views/card/batch.vue'),
        meta: { title: '批次管理', icon: 'Folder', requiresAuth: true },
      },
      {
        path: 'verify/use',
        name: 'VerifyUse',
        component: () => import('@/views/verify/use.vue'),
        meta: { title: '卡密核销', icon: 'CircleCheck', requiresAuth: true },
      },
      {
        path: 'verify/history',
        name: 'VerifyHistory',
        component: () => import('@/views/verify/history.vue'),
        meta: { title: '核销记录', icon: 'Document', requiresAuth: true },
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAuth: true, requiresAdmin: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 游戏卡密管理系统` : '游戏卡密管理系统'

  const userStore = useUserStore()

  // 不需要认证的页面直接放行
  if (to.meta.requiresAuth === false) {
    next()
    return
  }

  // 需要认证但未登录
  if (!userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Dashboard' })
    return
  }

  next()
})

export default router
