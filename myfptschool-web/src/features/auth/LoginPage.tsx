import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Eye, EyeOff } from 'lucide-react'
import { useAuth } from '@/shared/hooks/useAuth'
import { Input } from '@/shared/components/ui/Input'
import { Button } from '@/shared/components/ui/Button'
import { login as loginApi } from './api'

interface FormData {
  username: string
  password: string
}

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [showPwd, setShowPwd] = useState(false)
  const [error, setError] = useState('')

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>()

  async function onSubmit(data: FormData) {
    setError('')
    try {
      const res = await loginApi(data)
      login(res.accessToken, res.user)
      navigate('/dashboard', { replace: true })
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message
        ?? 'Tên đăng nhập hoặc mật khẩu không đúng'
      setError(msg)
    }
  }

  return (
    <div className="min-h-screen bg-surface-bg flex items-center justify-center p-4">
      <div className="w-full max-w-sm bg-white rounded-2xl shadow-md p-8">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="text-3xl font-bold text-brand-orange mb-1">
            MyFPT<span className="text-text-primary">School</span>
          </div>
          <p className="text-sm text-text-secondary">Cổng thông tin quản trị nhà trường</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          <Input
            label="Tài khoản"
            placeholder="Nhập tên đăng nhập"
            error={errors.username?.message}
            {...register('username', { required: 'Vui lòng nhập tài khoản' })}
          />

          <div className="relative">
            <Input
              label="Mật khẩu"
              type={showPwd ? 'text' : 'password'}
              placeholder="Nhập mật khẩu"
              error={errors.password?.message}
              {...register('password', { required: 'Vui lòng nhập mật khẩu' })}
            />
            <button
              type="button"
              onClick={() => setShowPwd((p) => !p)}
              className="absolute right-3 top-9 text-text-tertiary hover:text-text-secondary"
            >
              {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          </div>

          {error && (
            <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-status-danger">
              {error}
            </div>
          )}

          <Button type="submit" loading={isSubmitting} className="mt-2 w-full">
            Đăng nhập
          </Button>
        </form>
      </div>
    </div>
  )
}
