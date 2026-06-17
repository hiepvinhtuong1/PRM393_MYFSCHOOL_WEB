import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut, getApiErrorMessage } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Input } from '@/shared/components/ui/Input'
import { Select } from '@/shared/components/ui/Select'
import { Button } from '@/shared/components/ui/Button'
import type { Parent, Student } from '@/shared/types/models'
import type { PageResponse } from '@/shared/types/api'

const schema = z.object({
  parentCode: z.string().regex(/^(\d{9}|\d{12})$/, 'CCCD/CMND phải là 9 hoặc 12 chữ số'),
  fullName: z.string().min(2).max(100),
  dateOfBirth: z.string().regex(/^\d{2}\/\d{2}\/\d{4}$/, 'Định dạng dd/MM/yyyy').optional().or(z.literal('')),
  gender: z.enum(['Nam', 'Nữ']).optional().or(z.literal('')),
  phone: z.string().regex(/^0[3-9]\d{8}$/).optional().or(z.literal('')),
  email: z.string().email().optional().or(z.literal('')),
  username: z.string().min(4).max(50).regex(/^[a-z0-9_]+$/).optional().or(z.literal('')),
  password: z.string().min(6).optional().or(z.literal('')),
})
type FormData = z.infer<typeof schema>

export function ParentFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const qc = useQueryClient()

  const [linkStudentId, setLinkStudentId] = useState('')
  const [linkSearch, setLinkSearch] = useState('')

  const { data: parent } = useQuery({
    queryKey: queryKeys.parents.detail(Number(id)),
    queryFn: () => apiGet<Parent>(`/admin/parents/${id}`),
    enabled: isEdit,
  })

  const { data: studentsPage } = useQuery({
    queryKey: queryKeys.students.list({ search: linkSearch, size: 10 }),
    queryFn: () => apiGet<PageResponse<Student>>('/admin/students', { search: linkSearch || undefined, size: 10 }),
    enabled: isEdit && linkSearch.length >= 2,
  })

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
    values: parent
      ? {
          parentCode: parent.parentCode,
          fullName: parent.fullName,
          dateOfBirth: parent.dateOfBirth ?? '',
          gender: (parent.gender as 'Nam' | 'Nữ' | '') ?? '',
          phone: parent.phone ?? '',
          email: parent.email ?? '',
          username: '',
          password: '',
        }
      : undefined,
  })

  const mutation = useMutation({
    mutationFn: (data: FormData) => {
      const payload = {
        ...data,
        phone: data.phone || null,
        email: data.email || null,
        dateOfBirth: data.dateOfBirth || null,
        gender: data.gender || null,
      }
      return isEdit
        ? apiPut(`/admin/parents/${id}`, { fullName: payload.fullName, dateOfBirth: payload.dateOfBirth, gender: payload.gender, phone: payload.phone, email: payload.email })
        : apiPost('/admin/parents', { ...payload, username: data.username || undefined, password: data.password || undefined })
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.parents.list({}) })
      navigate('/parents')
    },
  })

  const linkMutation = useMutation({
    mutationFn: (studentId: number) => apiPost(`/admin/students/${studentId}/parents/${id}`, {}),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.parents.detail(Number(id)) })
      setLinkStudentId('')
      setLinkSearch('')
    },
  })

  const unlinkMutation = useMutation({
    mutationFn: (studentId: number) => apiDelete(`/admin/parents/${id}/students/${studentId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.parents.detail(Number(id)) }),
  })

  return (
    <div>
      <PageHeader title={isEdit ? 'Sửa phụ huynh' : 'Thêm phụ huynh'} />
      <div className="max-w-xl space-y-6">
        <form onSubmit={handleSubmit((d) => mutation.mutate(d as FormData))} className="space-y-6">
          <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
            <h2 className="font-semibold text-text-primary">Thông tin cơ bản</h2>
            <Input label="CCCD/CMND *" placeholder="9 hoặc 12 chữ số" error={errors.parentCode?.message} {...register('parentCode')} disabled={isEdit} />
            <Input label="Họ và tên *" error={errors.fullName?.message} {...register('fullName')} />
            <Input label="Ngày sinh (dd/MM/yyyy)" placeholder="01/01/1980" error={errors.dateOfBirth?.message} {...register('dateOfBirth')} />
            <Select label="Giới tính" error={errors.gender?.message} {...register('gender')}>
              <option value="">-- Chọn --</option>
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
            </Select>
            <Input label="Số điện thoại" placeholder="09xxxxxxxx" error={errors.phone?.message} {...register('phone')} />
            <Input label="Email" type="email" error={errors.email?.message} {...register('email')} />
          </div>

          {!isEdit && (
            <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
              <h2 className="font-semibold text-text-primary">Tài khoản</h2>
              <p className="text-xs text-text-secondary">Để trống tên đăng nhập → dùng CCCD/CMND. Để trống mật khẩu → mặc định <code>Parent@123</code></p>
              <Input label="Tên đăng nhập" error={errors.username?.message} {...register('username')} />
              <Input label="Mật khẩu" type="password" error={errors.password?.message} {...register('password')} />
            </div>
          )}

          {mutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-status-danger">
              {getApiErrorMessage(mutation.error, 'Có lỗi xảy ra. Kiểm tra lại thông tin.')}
            </div>
          )}

          <div className="flex gap-3">
            <Button type="button" variant="secondary" onClick={() => navigate('/parents')}>Hủy</Button>
            <Button type="submit" loading={isSubmitting || mutation.isPending}>Lưu phụ huynh</Button>
          </div>
        </form>

        {isEdit && (
          <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
            <h2 className="font-semibold text-text-primary">Con em đã liên kết</h2>
            {parent?.children.length
              ? <ul className="space-y-1.5">
                  {parent.children.map((c) => (
                    <li key={c.id} className="flex items-center justify-between gap-2 text-sm">
                      <span className="flex items-center gap-2">
                        <span className="w-2 h-2 rounded-full bg-brand-green" />
                        {c.fullName} <span className="text-text-tertiary text-xs">({c.studentCode})</span>
                      </span>
                      <button
                        onClick={() => { if (confirm(`Gỡ liên kết với ${c.fullName}?`)) unlinkMutation.mutate(c.id) }}
                        disabled={unlinkMutation.isPending}
                        className="text-xs text-status-danger hover:underline disabled:opacity-50"
                      >
                        Gỡ
                      </button>
                    </li>
                  ))}
                </ul>
              : <p className="text-sm text-text-tertiary">Chưa có học sinh nào được liên kết.</p>
            }

            <div className="border-t border-border-light pt-4">
              <p className="text-sm font-medium text-text-primary mb-2">Liên kết thêm học sinh</p>
              <Input
                placeholder="Tìm tên học sinh (gõ ≥ 2 ký tự)..."
                value={linkSearch}
                onChange={(e) => setLinkSearch(e.target.value)}
                className="mb-2"
              />
              {studentsPage?.content.map((s) => (
                <div key={s.id} className="flex items-center justify-between py-1.5 border-b border-border-light last:border-0">
                  <span className="text-sm">{s.fullName} <span className="text-text-tertiary text-xs">{s.classroomName}</span></span>
                  <Button
                    size="sm"
                    variant="secondary"
                    loading={linkMutation.isPending && linkStudentId === String(s.id)}
                    onClick={() => { setLinkStudentId(String(s.id)); linkMutation.mutate(s.id) }}
                  >
                    Liên kết
                  </Button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
