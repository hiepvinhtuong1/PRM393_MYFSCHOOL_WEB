import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery } from '@tanstack/react-query'
import { apiGet, apiPost, getApiErrorMessage } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { useAuth } from '@/shared/hooks/useAuth'
import { PageHeader } from '@/shared/components/PageHeader'
import { Input } from '@/shared/components/ui/Input'
import { Select } from '@/shared/components/ui/Select'
import { Button } from '@/shared/components/ui/Button'
import type { Classroom, MyClassroomSubject } from '@/shared/types/models'

const schema = z.object({
  targetType: z.enum(['individual', 'classroom', 'all']),
  targetId: z.coerce.number().optional(),
  category: z.enum(['attendance', 'grade', 'homeroom', 'study', 'event']),
  title: z.string().min(1).max(200),
  body: z.string().min(1),
}).superRefine((data, ctx) => {
  if ((data.targetType === 'individual' || data.targetType === 'classroom') && !data.targetId) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, message: 'Vui lòng chọn đối tượng', path: ['targetId'] })
  }
})
type FormData = z.infer<typeof schema>

const categoryLabels = { attendance: 'Điểm danh', grade: 'Điểm số', homeroom: 'Chủ nhiệm', study: 'Học tập', event: 'Sự kiện' }

export function NotificationComposePage() {
  const navigate = useNavigate()
  const { isAdmin } = useAuth()
  const { register, handleSubmit, watch, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
    defaultValues: { targetType: 'classroom', category: 'homeroom' },
  })
  const targetType = watch('targetType')

  // Admin: all classrooms; Teacher: only their classrooms from CS assignments
  const { data: allClassrooms } = useQuery({
    queryKey: queryKeys.classrooms.list(),
    queryFn: () => apiGet<Classroom[]>('/admin/classrooms'),
    enabled: isAdmin && targetType === 'classroom',
  })
  const { data: myCS = [] } = useQuery({
    queryKey: queryKeys.me.classroomSubjects(),
    queryFn: () => apiGet<MyClassroomSubject[]>('/me/classroom-subjects'),
    enabled: !isAdmin && targetType === 'classroom',
  })

  // Deduplicate classrooms from teacher's CS list
  const myClassrooms = Array.from(
    new Map(myCS.map((cs) => [cs.classroomId, { id: cs.classroomId, name: cs.classroomName }])).values()
  )
  const classroomOptions = isAdmin ? (allClassrooms ?? []) : myClassrooms

  const send = useMutation({
    mutationFn: (data: FormData) => apiPost<{ notificationId: number; recipientCount: number }>('/admin/notifications', data),
    onSuccess: (res: { notificationId: number; recipientCount: number }) => {
      alert(`Đã gửi tới ${res.recipientCount} người nhận`)
      navigate('/notifications')
    },
  })

  return (
    <div>
      <PageHeader title="Soạn thông báo" />
      <form onSubmit={handleSubmit((d) => send.mutate(d as FormData))} className="max-w-xl space-y-6">
        <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
          <h2 className="font-semibold">Đối tượng nhận</h2>
          <div className="space-y-2">
            {(['classroom', 'all', 'individual'] as const).map((t) => (
              <label key={t} className="flex items-center gap-2 cursor-pointer">
                <input type="radio" value={t} {...register('targetType')} className="accent-brand-orange" />
                <span className="text-sm">
                  {t === 'classroom' ? 'Cả lớp' : t === 'all' ? 'Toàn trường' : 'Cá nhân (userId)'}
                </span>
              </label>
            ))}
          </div>

          {targetType === 'classroom' && (
            <Select label="Chọn lớp" error={errors.targetId?.message} {...register('targetId')}>
              <option value="">-- Chọn lớp --</option>
              {classroomOptions.map((cl) => <option key={cl.id} value={cl.id}>{cl.name}</option>)}
            </Select>
          )}
          {targetType === 'individual' && (
            <Input label="User ID" type="number" error={errors.targetId?.message} {...register('targetId')} />
          )}
        </div>

        <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm space-y-4">
          <Select label="Danh mục *" error={errors.category?.message} {...register('category')}>
            {Object.entries(categoryLabels).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
          </Select>
          <Input label="Tiêu đề *" error={errors.title?.message} {...register('title')} />
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium text-text-primary">Nội dung *</label>
            <textarea
              rows={5}
              className="rounded-lg border border-border-light px-3 py-2 text-sm resize-none focus:outline-none focus:border-brand-blue"
              {...register('body')}
            />
            {errors.body && <p className="text-xs text-status-danger">{errors.body.message}</p>}
          </div>
        </div>

        {send.isError && (
          <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-status-danger">
            {getApiErrorMessage(send.error, 'Gửi thông báo thất bại. Kiểm tra lại.')}
          </div>
        )}

        <div className="flex gap-3">
          <Button type="button" variant="secondary" onClick={() => navigate('/notifications')}>Hủy</Button>
          <Button type="submit" loading={isSubmitting || send.isPending}>Gửi thông báo</Button>
        </div>
      </form>
    </div>
  )
}
