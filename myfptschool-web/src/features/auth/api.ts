import api from '@/shared/lib/api'
import type { AuthUser } from '@/shared/types/models'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  user: AuthUser
}

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const res = await api.post<{ success: boolean; data: LoginResponse }>('/auth/login', data)
  return res.data.data
}
