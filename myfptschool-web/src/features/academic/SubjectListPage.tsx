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
import type { Subject } from '@/shared/types/models'

const schema = z.object({
  name: z.string().min(1).max(50),
  colorHex: z.string().regex(/^#[0-9A-Fa-f]{6}$/).optional().or(z.literal('')),
  coefficient: z.coerce.number().int().min(1),
})
type FormData = z.infer<typeof schema>

export function SubjectListPage() {
  const qc = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Subject | null>(null)

  const { data: subjects, isLoading } = useQuery({
    queryKey: queryKeys.subjects.list(),
    queryFn: () => apiGet<Subject[]>('/admin/subjects'),
  })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
  })

  function openCreate() {
    setEditing(null)
    reset({ name: '', colorHex: '', coefficient: 1 })
    setShowForm(true)
  }

  function openEdit(s: Subject) {
    setEditing(s)
    reset({ name: s.name, colorHex: s.colorHex ?? '', coefficient: s.coefficient })
    setShowForm(true)
  }

  const mutation = useMutation({
    mutationFn: (data: FormData) => {
      const payload = { ...data, colorHex: data.colorHex || null }
      return editing
        ? apiPut(`/admin/subjects/${editing.id}`, payload)
        : apiPost('/admin/subjects', payload)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.subjects.list() })
      setShowForm(false)
      setEditing(null)
    },
  })

  return (
    <div>
      <PageHeader
        title="Môn học"
        actions={<Button onClick={openCreate}><Plus size={16} /> Thêm môn học</Button>}
      />

      {showForm && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm p-5 mb-6 max-w-md">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-text-primary">{editing ? 'Sửa môn học' : 'Thêm môn học'}</h2>
            <button onClick={() => setShowForm(false)}><X size={18} className="text-text-tertiary hover:text-text-primary" /></button>
          </div>
          <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-3">
            <Input label="Tên môn học *" error={errors.name?.message} {...register('name')} />
            <Input label="Hệ số *" type="number" min={1} error={errors.coefficient?.message} {...register('coefficient')} />
            <Input label="Màu (#HEX)" placeholder="#4A90E2" error={errors.colorHex?.message} {...register('colorHex')} />
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
              {['Tên môn học', 'Hệ số', 'Màu', ''].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border-light">
            {isLoading && <tr><td colSpan={4} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
            {!isLoading && !subjects?.length && <tr><td colSpan={4} className="px-4 py-8 text-center text-text-tertiary">Chưa có môn học nào</td></tr>}
            {subjects?.map((s) => (
              <tr key={s.id} className="hover:bg-surface-bg transition-colors">
                <td className="px-4 py-3 font-medium">{s.name}</td>
                <td className="px-4 py-3">{s.coefficient}</td>
                <td className="px-4 py-3">
                  {s.colorHex
                    ? <span className="flex items-center gap-2">
                        <span className="inline-block w-4 h-4 rounded" style={{ backgroundColor: s.colorHex }} />
                        <span className="font-mono text-xs text-text-secondary">{s.colorHex}</span>
                      </span>
                    : <span className="text-text-tertiary text-xs">—</span>}
                </td>
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
