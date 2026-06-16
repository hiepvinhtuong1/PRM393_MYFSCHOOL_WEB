import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus, X, Pencil } from 'lucide-react'
import { apiGet, apiPost, apiPut } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Button } from '@/shared/components/ui/Button'
import { Input } from '@/shared/components/ui/Input'
import { Select } from '@/shared/components/ui/Select'
import type { Campus } from '@/shared/types/models'

interface Room { id: number; code: string; campusName: string }

const schema = z.object({
  code: z.string().min(1).max(30),
  campusId: z.string().min(1, 'Chọn cơ sở'),
})
type FormData = z.infer<typeof schema>

export function RoomListPage() {
  const qc = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Room | null>(null)

  const { data: rooms, isLoading } = useQuery({
    queryKey: queryKeys.rooms.list(),
    queryFn: () => apiGet<Room[]>('/admin/rooms'),
  })
  const { data: campuses } = useQuery({
    queryKey: queryKeys.campuses.list(),
    queryFn: () => apiGet<Campus[]>('/admin/campuses'),
    enabled: showForm || Boolean(editing),
  })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
  })

  function openCreate() {
    setEditing(null)
    reset({ code: '', campusId: '' })
    setShowForm(true)
  }

  function openEdit(r: Room) {
    setEditing(r)
    reset({ code: r.code, campusId: '' })
    setShowForm(true)
  }

  const mutation = useMutation({
    mutationFn: (data: FormData) => {
      const payload = { ...data, campusId: Number(data.campusId) }
      return editing
        ? apiPut(`/admin/rooms/${editing.id}`, payload)
        : apiPost('/admin/rooms', payload)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.rooms.list() })
      setShowForm(false)
      setEditing(null)
    },
  })

  return (
    <div>
      <PageHeader
        title="Phòng học"
        actions={<Button onClick={openCreate}><Plus size={16} /> Thêm phòng</Button>}
      />

      {showForm && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm p-5 mb-6 max-w-md">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-text-primary">{editing ? 'Sửa phòng học' : 'Thêm phòng học'}</h2>
            <button onClick={() => setShowForm(false)}><X size={18} className="text-text-tertiary hover:text-text-primary" /></button>
          </div>
          <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="space-y-3">
            <Input label="Mã phòng *" placeholder="A101" error={errors.code?.message} {...register('code')} />
            <Select label="Cơ sở *" error={errors.campusId?.message} {...register('campusId')}>
              <option value="">-- Chọn cơ sở --</option>
              {campuses?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </Select>
            {mutation.isError && <p className="text-sm text-status-danger">Có lỗi. Kiểm tra lại thông tin.</p>}
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
              {['Mã phòng', 'Cơ sở', ''].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border-light">
            {isLoading && <tr><td colSpan={3} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
            {!isLoading && !rooms?.length && <tr><td colSpan={3} className="px-4 py-8 text-center text-text-tertiary">Chưa có phòng học nào</td></tr>}
            {rooms?.map((r) => (
              <tr key={r.id} className="hover:bg-surface-bg transition-colors">
                <td className="px-4 py-3 font-medium font-mono">{r.code}</td>
                <td className="px-4 py-3">{r.campusName}</td>
                <td className="px-4 py-3">
                  <button onClick={() => openEdit(r)} className="flex items-center gap-1 text-xs text-brand-blue hover:underline">
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
