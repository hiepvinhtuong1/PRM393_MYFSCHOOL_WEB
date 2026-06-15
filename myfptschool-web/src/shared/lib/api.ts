import axios from 'axios'
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

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('access_token')
      localStorage.removeItem('auth_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
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

export default api
