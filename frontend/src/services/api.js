import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - add token
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// Response interceptor - handle errors
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const { response } = error
    
    if (response) {
      switch (response.status) {
        case 401:
          // Token expired or invalid
          const authStore = useAuthStore()
          authStore.logout()
          router.push('/login')
          break
        case 403:
          console.error('没有访问权限')
          break
        case 404:
          console.error('资源不存在')
          break
        case 500:
          console.error('服务器错误')
          break
        default:
          console.error('请求失败:', response.data?.message || '未知错误')
      }
      
      return Promise.reject(response.data || error)
    }
    
    console.error('网络错误:', error.message)
    return Promise.reject(error)
  }
)

export default api
