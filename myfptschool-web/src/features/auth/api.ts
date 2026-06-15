import api from '@/shared/lib/api'
import type { AuthUser, Role } from '@/shared/types/models'

export interface LoginRequest {
  username: string
  password: string
}

interface LoginApiResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  role: string
  userId: number
  username: string
  fullName: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  user: AuthUser
}

export async function login(data: LoginRequest): Promise<LoginResult> {
  const res = await api.post<{ code: number; data: LoginApiResponse }>('/auth/login', {
    ...data,
    platform: 'web',
  })
  const d = res.data.data
  return {
    accessToken: d.accessToken,
    refreshToken: d.refreshToken,
    user: {
      id: d.userId,
      username: d.username,
      role: d.role as Role,
      fullName: d.fullName,
    },
  }
}

export async function logout(refreshToken: string): Promise<void> {
  try {
    await api.post('/auth/logout', { refreshToken })
  } catch {
    // best-effort — proceed with local logout even if server call fails
  }
}
