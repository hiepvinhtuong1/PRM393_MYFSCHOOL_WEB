import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost, apiPut } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Input } from '@/shared/components/ui/Input'
import { Select } from '@/shared/components/ui/Select'
import { Button } from '@/shared/components/ui/Button'
import type { Classroom, Student } from '@/shared/types/models'
import type { PageResponse } from '@/shared/types/api'

const schema = z.object({
  fullName: z.string().min(2).max(100),
  dateOfBirth: z.string().regex(/^\d{2}\/\d{2}\/\d{4}$/, 'Định dạng dd/MM/yyyy'),
  gender: z.enum(['Nam', 'Nữ']),
  phone: z.string().regex(/^0[3-9]\d{8}$/).optional().or(z.literal('')),
  email: z.string().email().optional().or(z.literal('')),
  classroomId: z.coerce.number().min(1, 'Chọn lớp'),
  username: z.string().min(4).max(50).regex(/^[a-z0-9_]+$/),
  password: z.string().min(6).optional().or(z.literal('')),
  photoUrl: z.string().optional().or(z.literal('')),
})
type FormData = z.infer<typeof schema>

export function StudentFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const qc = useQueryClient()

  const { data: classrooms } = useQuery({
    queryKey: queryKeys.classrooms.list(),
    queryFn: () => apiGet<PageResponse<Classroom>>('/admin/classrooms', { size: 100 }),
  })

  const { data: student } = useQuery({
    queryKey: queryKeys.students.detail(Number(id)),
    queryFn: () => apiGet<Student>(`/admin/students/${id}`),
    enabled: isEdit,
  })

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
    values: student
      ? {
          fullName: student.fullName,
          dateOfBirth: student.dateOfBirth,
          gender: student.gender as 'Nam' | 'Nữ',
          phone: student.phone ?? '',
          email: student.email ?? '',
          classroomId: student.classroomId,
          username: student.username,
          password: '',
          photoUrl: student.photoUrl ?? '',
        }
      : undefined,
  })

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? apiPut(`/admin/students/${id}`, data)
        : apiPost('/admin/students', data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.students.list({}) })
      navigate('/students')
    },
  })

  return (
    <div>
      <PageHeader title={isEdit ? 'Sửa học sinh' : 'Thêm học sinh'} />
      <form onSubmit={handleSubmit((d) => mutation.mutate(d as FormData))} className="max-w-xl space-y-6">
        <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
          <h2 className="font-semibold text-text-primary">Thông tin cơ bản</h2>
          <Input label="Họ và tên *" error={errors.fullName?.message} {...register('fullName')} />
          <Input label="Ngày sinh * (dd/MM/yyyy)" placeholder="15/06/2009" error={errors.dateOfBirth?.message} {...register('dateOfBirth')} />
          <Select label="Giới tính *" error={errors.gender?.message} {...register('gender')}>
            <option value="">-- Chọn --</option>
            <option value="Nam">Nam</option>
            <option value="Nữ">Nữ</option>
          </Select>
          <Select label="Lớp *" error={errors.classroomId?.message} {...register('classroomId')}>
            <option value="">-- Chọn lớp --</option>
            {classrooms?.content.map((cl) => (
              <option key={cl.id} value={cl.id}>{cl.name}</option>
            ))}
          </Select>
        </div>

        <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
          <h2 className="font-semibold text-text-primary">Liên hệ</h2>
          <Input label="Số điện thoại" placeholder="09xxxxxxxx" error={errors.phone?.message} {...register('phone')} />
          <Input label="Email" type="email" error={errors.email?.message} {...register('email')} />
          <Input label="Ảnh (URL)" error={errors.photoUrl?.message} {...register('photoUrl')} />
        </div>

        <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
          <h2 className="font-semibold text-text-primary">Tài khoản</h2>
          <Input label="Tên đăng nhập *" error={errors.username?.message} {...register('username')} />
          <Input label={isEdit ? 'Mật khẩu mới (để trống = không đổi)' : 'Mật khẩu *'} type="password" error={errors.password?.message} {...register('password')} />
        </div>

        {mutation.isError && (
          <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-status-danger">
            Có lỗi xảy ra. Vui lòng kiểm tra lại thông tin.
          </div>
        )}

        <div className="flex gap-3">
          <Button type="button" variant="secondary" onClick={() => navigate('/students')}>Hủy</Button>
          <Button type="submit" loading={isSubmitting || mutation.isPending}>Lưu học sinh</Button>
        </div>
      </form>
    </div>
  )
}
