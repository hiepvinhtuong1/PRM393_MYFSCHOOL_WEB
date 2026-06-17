import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPatch, apiPost, apiPut, getApiErrorMessage } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Input } from '@/shared/components/ui/Input'
import { Select } from '@/shared/components/ui/Select'
import { Button } from '@/shared/components/ui/Button'
import type { Campus, Teacher } from '@/shared/types/models'

const schema = z.object({
  fullName: z.string().min(2).max(100),
  phone: z.string().regex(/^0[3-9]\d{8}$/).optional().or(z.literal('')),
  email: z.string().email().optional().or(z.literal('')),
  campusId: z.coerce.number().min(1, 'Chọn cơ sở'),
  username: z.string().min(4).max(50).regex(/^[a-z0-9_]+$/).optional().or(z.literal('')),
  password: z.string().min(6).optional().or(z.literal('')),
})
type FormData = z.infer<typeof schema>

export function TeacherFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const qc = useQueryClient()

  const { data: campuses } = useQuery({
    queryKey: queryKeys.campuses.list(),
    queryFn: () => apiGet<Campus[]>('/admin/campuses'),
  })

  const { data: teacher } = useQuery({
    queryKey: queryKeys.teachers.detail(Number(id)),
    queryFn: () => apiGet<Teacher>(`/admin/teachers/${id}`),
    enabled: isEdit,
  })

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
    values: teacher
      ? {
          fullName: teacher.fullName,
          phone: teacher.phone ?? '',
          email: teacher.email ?? '',
          campusId: teacher.campusId ?? 0,
          username: '',
          password: '',
        }
      : undefined,
  })

  const [newPwd, setNewPwd] = useState('')
  const [pwdSuccess, setPwdSuccess] = useState(false)

  const resetPwdMutation = useMutation({
    mutationFn: () => apiPatch<void>(`/admin/teachers/${id}/password`, { newPassword: newPwd }),
    onSuccess: () => { setNewPwd(''); setPwdSuccess(true); setTimeout(() => setPwdSuccess(false), 3000) },
  })

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? apiPut(`/admin/teachers/${id}`, { fullName: data.fullName, phone: data.phone || null, email: data.email || null, campusId: data.campusId })
        : apiPost('/admin/teachers', { ...data, phone: data.phone || null, email: data.email || null, username: data.username, password: data.password || undefined }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.teachers.list({}) })
      navigate('/teachers')
    },
  })

  return (
    <div>
      <PageHeader title={isEdit ? 'Sửa giáo viên' : 'Thêm giáo viên'} />
      <form onSubmit={handleSubmit((d) => mutation.mutate(d as FormData))} className="max-w-xl space-y-6">
        <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
          <h2 className="font-semibold text-text-primary">Thông tin cơ bản</h2>
          <Input label="Họ và tên *" error={errors.fullName?.message} {...register('fullName')} />
          <Input label="Số điện thoại" placeholder="09xxxxxxxx" error={errors.phone?.message} {...register('phone')} />
          <Input label="Email" type="email" error={errors.email?.message} {...register('email')} />
          <Select label="Cơ sở *" error={errors.campusId?.message} {...register('campusId')}>
            <option value="">-- Chọn cơ sở --</option>
            {campuses?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </Select>
        </div>

        {!isEdit && (
          <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
            <h2 className="font-semibold text-text-primary">Tài khoản</h2>
            <Input label="Tên đăng nhập *" error={errors.username?.message} {...register('username')} />
            <Input label="Mật khẩu (mặc định: Teacher@123)" type="password" error={errors.password?.message} {...register('password')} />
          </div>
        )}

        {isEdit && (
          <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
            <h2 className="font-semibold text-text-primary">Đặt lại mật khẩu</h2>
            <div className="flex gap-2 items-end">
              <div className="flex-1">
                <Input
                  label="Mật khẩu mới (tối thiểu 6 ký tự)"
                  type="password"
                  value={newPwd}
                  onChange={(e) => setNewPwd(e.target.value)}
                />
              </div>
              <Button
                type="button"
                variant="secondary"
                loading={resetPwdMutation.isPending}
                disabled={newPwd.length < 6}
                onClick={() => resetPwdMutation.mutate()}
              >
                Đặt lại
              </Button>
            </div>
            {pwdSuccess && <p className="text-sm text-green-700">Đã đặt lại mật khẩu thành công.</p>}
            {resetPwdMutation.isError && <p className="text-sm text-status-danger">{getApiErrorMessage(resetPwdMutation.error, 'Đặt lại mật khẩu thất bại.')}</p>}
          </div>
        )}

        {mutation.isError && (
          <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-status-danger">
            {getApiErrorMessage(mutation.error, 'Có lỗi xảy ra. Vui lòng kiểm tra lại thông tin.')}
          </div>
        )}

        <div className="flex gap-3">
          <Button type="button" variant="secondary" onClick={() => navigate('/teachers')}>Hủy</Button>
          <Button type="submit" loading={isSubmitting || mutation.isPending}>Lưu giáo viên</Button>
        </div>
      </form>
    </div>
  )
}
