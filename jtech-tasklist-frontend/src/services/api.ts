import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
})

api.interceptors.request.use((config) => {
  if (config.url === '/api/v1/auth/refresh') return config
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

let isRefreshing = false
let pendingRequests: Array<{
  resolve: (token: string) => void
  reject: (error: unknown) => void
}> = []

// default implementation — test can override via (api as any).refreshFn = vi.fn()
;(api as any).refreshFn = async (refreshToken: string) => {
  const { data } = await axios.post(
    `${api.defaults.baseURL}/api/v1/auth/refresh`,
    { refreshToken },
  )
  return data as { accessToken: string; refreshToken: string }
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    const auth = useAuthStore()

    if (!auth.refreshToken) {
      auth.logout()
      return Promise.reject(error)
    }

    if (isRefreshing) {
      originalRequest._retry = true
      return new Promise<string>((resolve, reject) => {
        pendingRequests.push({ resolve, reject })
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`
        return api(originalRequest)
      })
    }

    originalRequest._retry = true
    isRefreshing = true

    try {
      const data = await (api as any).refreshFn(auth.refreshToken)

      auth.accessToken = data.accessToken
      auth.refreshToken = data.refreshToken

      pendingRequests.forEach(({ resolve }) => resolve(data.accessToken))
      pendingRequests = []

      originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      pendingRequests.forEach(({ reject }) => reject(refreshError))
      pendingRequests = []
      auth.logout()
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  },
)

export default api
