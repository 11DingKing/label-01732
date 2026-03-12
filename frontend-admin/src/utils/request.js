import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken } from '@/utils/auth'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
// 注意：这里使用 getToken 而不是 useUserStore，避免 Pinia 未初始化时的问题
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 正常响应
    if (res.code === 200) {
      return res
    }
    
    // 业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // 未授权，清除用户信息并跳转登录页
          // 避免重复弹窗
          if (!window.__401_showing__) {
            window.__401_showing__ = true
            ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning',
            }).then(async () => {
              // 动态导入避免循环依赖
              const { useUserStore } = await import('@/store/modules/user')
              const userStore = useUserStore()
              userStore.logout()
              router.push('/login')
            }).catch(() => {}).finally(() => {
              window.__401_showing__ = false
            })
          }
          break
        case 403:
          ElMessage.error(data?.message || '权限不足，请联系管理员')
          break
        case 404:
          // 可能是权限问题导致的404，给出更友好的提示
          ElMessage.error(data?.message || '请求的资源不存在或无权访问')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default service
