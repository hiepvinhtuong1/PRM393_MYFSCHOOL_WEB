import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { apiPatch, getApiErrorMessage } from '@/shared/lib/api'
import { useAuth } from '@/shared/hooks/useAuth'
import { PageHeader } from '@/shared/components/PageHeader'
import { Input } from '@/shared/components/ui/Input'
import { Button } from '@/shared/components/ui/Button'

const schema = z.object({
  currentPassword: z.string().min(1, 'Vui lòng nhập mật khẩu hiện tại'),
  newPassword: z.string().min(6, 'Mật khẩu mới tối thiểu 6 ký tự'),
  confirmPassword: z.string().min(1, 'Vui lòng xác nhận mật khẩu'),
}).refine((d) => d.newPassword === d.confirmPassword, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['confirmPassword'],
})
type FormData = z.infer<typeof schema>

const roleLabel: Record<string, string> = {
  TEACHER: 'Giáo viên',
  HOMEROOM_TEACHER: 'Giáo viên chủ nhiệm',
  ADMIN: 'Quản trị viên',
}

export function ProfilePage() {
  const { user } = useAuth()
  const [success, setSuccess] = useState(false)

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
  })

  const changePwd = useMutation({
    mutationFn: (data: FormData) =>
      apiPatch<void>('/me/password', {
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      }),
    onSuccess: () => {
      setSuccess(true)
      reset()
    },
  })

  return (
    <div>
      <PageHeader title="Hồ sơ của tôi" />

      {/* Profile info */}
      <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm mb-6 max-w-xl">
        <h2 className="font-semibold text-text-primary mb-4">Thông tin tài khoản</h2>
        <dl className="space-y-3 text-sm">
          <div className="flex">
            <dt className="w-36 text-text-secondary shrink-0">Họ tên</dt>
            <dd className="font-medium text-text-primary">{user?.fullName ?? '—'}</dd>
          </div>
          <div className="flex">
            <dt className="w-36 text-text-secondary shrink-0">Tên đăng nhập</dt>
            <dd className="font-mono text-text-primary">{user?.username ?? '—'}</dd>
          </div>
          <div className="flex">
            <dt className="w-36 text-text-secondary shrink-0">Vai trò</dt>
            <dd className="text-text-primary">{user?.role ? roleLabel[user.role] ?? user.role : '—'}</dd>
          </div>
        </dl>
      </div>

      {/* Change password */}
      <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm max-w-xl">
        <h2 className="font-semibold text-text-primary mb-4">Đổi mật khẩu</h2>
        <form onSubmit={handleSubmit((d) => { setSuccess(false); changePwd.mutate(d) })} className="space-y-4">
          <Input
            label="Mật khẩu hiện tại"
            type="password"
            autoComplete="current-password"
            error={errors.currentPassword?.message}
            {...register('currentPassword')}
          />
          <Input
            label="Mật khẩu mới"
            type="password"
            autoComplete="new-password"
            error={errors.newPassword?.message}
            {...register('newPassword')}
          />
          <Input
            label="Xác nhận mật khẩu mới"
            type="password"
            autoComplete="new-password"
            error={errors.confirmPassword?.message}
            {...register('confirmPassword')}
          />

          {changePwd.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-status-danger">
              {getApiErrorMessage(changePwd.error, 'Đổi mật khẩu thất bại. Vui lòng thử lại.')}
            </div>
          )}
          {success && (
            <div className="rounded-lg bg-green-50 border border-green-200 px-4 py-3 text-sm text-green-800">
              Đổi mật khẩu thành công!
            </div>
          )}

          <Button type="submit" loading={isSubmitting || changePwd.isPending}>
            Cập nhật mật khẩu
          </Button>
        </form>
      </div>
    </div>
  )
}
