import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus, X, Pencil } from 'lucide-react'
import { apiGet, apiPost, apiPut, getApiErrorMessage } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Button } from '@/shared/components/ui/Button'
import { Input } from '@/shared/components/ui/Input'

interface AcademicYear {
  id: number
  label: string
  startDate: string
  endDate: string
}

const schema = z.object({
  label: z.string().min(1).max(20),
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Định dạng YYYY-MM-DD'),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Định dạng YYYY-MM-DD'),
})
type FormData = z.infer<typeof schema>

export function AcademicYearListPage() {
  const qc = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<AcademicYear | null>(null)

  const { data: years, isLoading } = useQuery({
    queryKey: queryKeys.academicYears.list(),
    queryFn: () => apiGet<AcademicYear[]>('/admin/academic-years'),
  })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
  })

  function openCreate() {
    setEditing(null)
    reset({ label: '', startDate: '', endDate: '' })
    setShowForm(true)
  }

  function openEdit(ay: AcademicYear) {
    setEditing(ay)
    reset({ label: ay.label, startDate: ay.startDate, endDate: ay.endDate })
    setShowForm(true)
  }

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      editing
        ? apiPut(`/admin/academic-years/${editing.id}`, data)
        : apiPost('/admin/academic-years', data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.academicYears.list() })
      setShowForm(false)
      setEditing(null)
    },
  })

  return (
    <div>
      <PageHeader
        title="Năm học"
        actions={<Button onClick={openCreate}><Plus size={16} /> Thêm năm học</Button>}
      />

      {showForm && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm p-5 mb-6 max-w-md">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-text-primary">{editing ? 'Sửa năm học' : 'Thêm năm học'}</h2>
            <button onClick={() => setShowForm(false)}><X size={18} className="text-text-tertiary hover:text-text-primary" /></button>
          </div>
          <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-3">
            <Input label="Nhãn (VD: 2025-2026) *" error={errors.label?.message} {...register('label')} />
            <Input label="Ngày bắt đầu * (YYYY-MM-DD)" placeholder="2025-09-01" error={errors.startDate?.message} {...register('startDate')} />
            <Input label="Ngày kết thúc * (YYYY-MM-DD)" placeholder="2026-06-30" error={errors.endDate?.message} {...register('endDate')} />
            {mutation.isError && <p className="text-sm text-status-danger">{getApiErrorMessage(mutation.error, 'Có lỗi. Kiểm tra lại thông tin.')}</p>}
            <div className="flex gap-3 pt-1">
              <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>Hủy</Button>
              <Button type="submit" loading={isSubmitting || mutation.isPending}>Lưu</Button>
            </div>
          </form>
        </div>
      )}

      <div className="bg-white rounded-xl border border-border-light shadow-sm overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-surface-elevated">
            <tr>
              {['Nhãn', 'Bắt đầu', 'Kết thúc', ''].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border-light">
            {isLoading && <tr><td colSpan={4} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
            {!isLoading && !years?.length && <tr><td colSpan={4} className="px-4 py-8 text-center text-text-tertiary">Chưa có năm học nào</td></tr>}
            {years?.map((ay) => (
              <tr key={ay.id} className="hover:bg-surface-bg transition-colors">
                <td className="px-4 py-3 font-medium">{ay.label}</td>
                <td className="px-4 py-3 text-text-secondary">{ay.startDate}</td>
                <td className="px-4 py-3 text-text-secondary">{ay.endDate}</td>
                <td className="px-4 py-3">
                  <button onClick={() => openEdit(ay)} className="flex items-center gap-1 text-xs text-brand-blue hover:underline">
                    <Pencil size={12} /> Sửa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
