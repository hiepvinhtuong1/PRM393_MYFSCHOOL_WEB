import { createContext, useState, useCallback, type ReactNode } from 'react'
import type { AuthUser } from '@/shared/types/models'
import { logout as logoutApi } from './api'

interface AuthContextType {
  user: AuthUser | null
  token: string | null
  login: (accessToken: string, refreshToken: string, user: AuthUser) => void
  logout: () => void
  isAdmin: boolean
}

export const AuthContext = createContext<AuthContextType | null>(null)

function loadUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem('auth_user')
    return raw ? (JSON.parse(raw) as AuthUser) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUser)
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('access_token'))

  const login = useCallback((accessToken: string, refreshToken: string, authUser: AuthUser) => {
    localStorage.setItem('access_token', accessToken)
    localStorage.setItem('refresh_token', refreshToken)
    localStorage.setItem('auth_user', JSON.stringify(authUser))
    setToken(accessToken)
    setUser(authUser)
  }, [])

  const logout = useCallback(() => {
    const refreshToken = localStorage.getItem('refresh_token')
    if (refreshToken) {
      logoutApi(refreshToken) // fire-and-forget
    }
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('auth_user')
    setToken(null)
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAdmin: user?.role === 'ADMIN' }}>
      {children}
    </AuthContext.Provider>
  )
}
