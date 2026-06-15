import axios, { type InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/shared/types/api'

const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Auto-refresh on 401
let isRefreshing = false
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = []

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token!)))
  failedQueue = []
}

function clearSession() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('auth_user')
  window.location.href = '/login'
}

interface RetryConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config as RetryConfig

    if (err.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(err)
    }

    const refreshToken = localStorage.getItem('refresh_token')
    if (!refreshToken) {
      clearSession()
      return Promise.reject(err)
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({
          resolve: (token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(api(originalRequest))
          },
          reject,
        })
      })
    }

    originalRequest._retry = true
    isRefreshing = true

    try {
      const res = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string }>>(
        '/api/v1/auth/refresh',
        { refreshToken },
      )
      const { accessToken, refreshToken: newRefreshToken } = res.data.data
      localStorage.setItem('access_token', accessToken)
      localStorage.setItem('refresh_token', newRefreshToken)
      api.defaults.headers.common.Authorization = `Bearer ${accessToken}`
      processQueue(null, accessToken)
      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return api(originalRequest)
    } catch (refreshErr) {
      processQueue(refreshErr, null)
      clearSession()
      return Promise.reject(refreshErr)
    } finally {
      isRefreshing = false
    }
  },
)

export async function apiGet<T>(url: string, params?: object): Promise<T> {
  const res = await api.get<ApiResponse<T>>(url, { params })
  return res.data.data
}

export async function apiPost<T>(url: string, data?: unknown): Promise<T> {
  const res = await api.post<ApiResponse<T>>(url, data)
  return res.data.data
}

export async function apiPut<T>(url: string, data?: unknown): Promise<T> {
  const res = await api.put<ApiResponse<T>>(url, data)
  return res.data.data
}

export async function apiPatch<T>(url: string, data?: unknown): Promise<T> {
  const res = await api.patch<ApiResponse<T>>(url, data)
  return res.data.data
}

export async function apiDelete(url: string): Promise<void> {
  await api.delete(url)
}

export async function apiDownload(url: string, filename: string): Promise<void> {
  const res = await api.get(url, { responseType: 'blob' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(new Blob([res.data]))
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

export async function apiUpload<T>(url: string, formData: FormData): Promise<T> {
  const res = await api.post<ApiResponse<T>>(url, formData, {
    headers: { 'Content-Type': undefined },
    validateStatus: (s) => s < 500,
  })
  return res.data.data
}

export default api
