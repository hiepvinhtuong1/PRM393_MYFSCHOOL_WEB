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
import { Select } from '@/shared/components/ui/Select'
import type { Semester, AcademicYear } from '@/shared/types/models'

const schema = z.object({
  academicYearId: z.string().min(1, 'Chọn năm học'),
  name: z.string().min(1).max(10),
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Định dạng YYYY-MM-DD'),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Định dạng YYYY-MM-DD'),
})
type FormData = z.infer<typeof schema>

export function SemesterListPage() {
  const qc = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Semester | null>(null)

  const { data: semesters, isLoading } = useQuery({
    queryKey: queryKeys.semesters.list(),
    queryFn: () => apiGet<Semester[]>('/admin/semesters'),
  })
  const { data: academicYears } = useQuery({
    queryKey: queryKeys.academicYears.list(),
    queryFn: () => apiGet<AcademicYear[]>('/admin/academic-years'),
    enabled: showForm || Boolean(editing),
  })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
  })

  function openCreate() {
    setEditing(null)
    reset({ academicYearId: '', name: '', startDate: '', endDate: '' })
    setShowForm(true)
  }

  function openEdit(s: Semester) {
    setEditing(s)
    reset({
      academicYearId: String(s.academicYearId),
      name: s.name,
      startDate: s.startDate,
      endDate: s.endDate,
    })
    setShowForm(true)
  }

  const mutation = useMutation({
    mutationFn: (data: FormData) => {
      const payload = { ...data, academicYearId: Number(data.academicYearId) }
      return editing
        ? apiPut(`/admin/semesters/${editing.id}`, payload)
        : apiPost('/admin/semesters', payload)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.semesters.list() })
      setShowForm(false)
      setEditing(null)
    },
  })

  return (
    <div>
      <PageHeader
        title="Học kỳ"
        actions={<Button onClick={openCreate}><Plus size={16} /> Thêm học kỳ</Button>}
      />

      {showForm && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm p-5 mb-6 max-w-lg">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-text-primary">{editing ? 'Sửa học kỳ' : 'Thêm học kỳ'}</h2>
            <button onClick={() => setShowForm(false)}><X size={18} className="text-text-tertiary hover:text-text-primary" /></button>
          </div>
          <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-3">
            <Select label="Năm học *" error={errors.academicYearId?.message} {...register('academicYearId')}>
              <option value="">-- Chọn năm học --</option>
              {academicYears?.map((ay) => <option key={ay.id} value={ay.id}>{ay.label}</option>)}
            </Select>
            <Input label="Tên học kỳ * (e.g. HK1, HK2)" error={errors.name?.message} {...register('name')} />
            <Input label="Ngày bắt đầu * (YYYY-MM-DD)" placeholder="2025-09-01" error={errors.startDate?.message} {...register('startDate')} />
            <Input label="Ngày kết thúc * (YYYY-MM-DD)" placeholder="2026-01-15" error={errors.endDate?.message} {...register('endDate')} />
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
              {['Tên học kỳ', 'Năm học', 'Ngày bắt đầu', 'Ngày kết thúc', ''].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border-light">
            {isLoading && <tr><td colSpan={5} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
            {!isLoading && !semesters?.length && <tr><td colSpan={5} className="px-4 py-8 text-center text-text-tertiary">Chưa có học kỳ nào</td></tr>}
            {semesters?.map((s) => (
              <tr key={s.id} className="hover:bg-surface-bg transition-colors">
                <td className="px-4 py-3 font-medium">{s.name}</td>
                <td className="px-4 py-3">{s.academicYear}</td>
                <td className="px-4 py-3 text-text-secondary">{s.startDate}</td>
                <td className="px-4 py-3 text-text-secondary">{s.endDate}</td>
                <td className="px-4 py-3">
                  <button onClick={() => openEdit(s)} className="flex items-center gap-1 text-xs text-brand-blue hover:underline">
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
