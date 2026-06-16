import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, X } from 'lucide-react'
import { apiDelete, apiGet, apiPatch, apiPost } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Select } from '@/shared/components/ui/Select'
import { Input } from '@/shared/components/ui/Input'
import { Button } from '@/shared/components/ui/Button'
import { Badge } from '@/shared/components/ui/Badge'
import type { ClassroomSubject, Semester, TimeSlot } from '@/shared/types/models'
import type { PageResponse } from '@/shared/types/api'

interface Room { id: number; code: string; campusName: string }
interface Lesson {
  id: number; classroomSubjectId: number; lessonDate: string
  startSlotNumber: number; endSlotNumber: number; startTime: string; endTime: string
  slotLabel: string; roomId: number | null; roomCode: string | null; status: string; note: string | null
}

const statusBadge: Record<string, 'success' | 'default' | 'danger' | 'warning'> = {
  scheduled: 'info' as never, completed: 'success', cancelled: 'danger', makeup: 'warning',
}
const statusLabel: Record<string, string> = {
  scheduled: 'Lịch học', completed: 'Đã học', cancelled: 'Đã hủy', makeup: 'Bù',
}

export function TimetablePage() {
  const [semesterId, setSemesterId] = useState('')
  const [csId, setCsId] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ lessonDate: '', startSlotId: '', endSlotId: '', roomId: '' })
  const [editLesson, setEditLesson] = useState<Lesson | null>(null)
  const [editForm, setEditForm] = useState({ status: '', roomId: '', note: '' })
  const qc = useQueryClient()

  const { data: semesters } = useQuery({
    queryKey: queryKeys.semesters.list(),
    queryFn: () => apiGet<Semester[]>('/admin/semesters'),
  })
  const { data: csPage } = useQuery({
    queryKey: queryKeys.classroomSubjects.list({ semesterId }),
    queryFn: () => apiGet<PageResponse<ClassroomSubject>>('/admin/classroom-subjects', { semesterId, size: 100 }),
    enabled: Boolean(semesterId),
  })
  const { data: lessons, isLoading } = useQuery({
    queryKey: queryKeys.classroomSubjects.lessons(Number(csId)),
    queryFn: () => apiGet<Lesson[]>(`/admin/classroom-subjects/${csId}/lessons`),
    enabled: Boolean(csId),
  })
  const { data: timeSlots } = useQuery({
    queryKey: queryKeys.timeSlots.list(),
    queryFn: () => apiGet<TimeSlot[]>('/admin/time-slots'),
    enabled: showForm,
  })
  const { data: rooms } = useQuery({
    queryKey: queryKeys.rooms.list(),
    queryFn: () => apiGet<Room[]>('/admin/rooms'),
    enabled: showForm || Boolean(editLesson),
  })

  const invalidateLessons = () => qc.invalidateQueries({ queryKey: queryKeys.classroomSubjects.lessons(Number(csId)) })

  const createLesson = useMutation({
    mutationFn: () => apiPost(`/admin/classroom-subjects/${csId}/lessons`, {
      lessonDate: form.lessonDate,
      startSlotId: Number(form.startSlotId),
      endSlotId: Number(form.endSlotId),
      roomId: form.roomId ? Number(form.roomId) : null,
    }),
    onSuccess: () => { invalidateLessons(); setShowForm(false); setForm({ lessonDate: '', startSlotId: '', endSlotId: '', roomId: '' }) },
  })

  const deleteLesson = useMutation({
    mutationFn: (id: number) => apiDelete(`/admin/lessons/${id}`),
    onSuccess: invalidateLessons,
  })

  const markDone = useMutation({
    mutationFn: (id: number) => apiPatch(`/admin/lessons/${id}`, { status: 'completed' }),
    onSuccess: invalidateLessons,
  })
  const cancel = useMutation({
    mutationFn: (id: number) => apiPatch(`/admin/lessons/${id}`, { status: 'cancelled' }),
    onSuccess: invalidateLessons,
  })
  const updateLesson = useMutation({
    mutationFn: () => apiPatch(`/admin/lessons/${editLesson!.id}`, {
      status: editForm.status || editLesson!.status,
      roomId: editForm.roomId ? Number(editForm.roomId) : null,
      note: editForm.note || null,
    }),
    onSuccess: () => { invalidateLessons(); setEditLesson(null) },
  })

  function openEdit(l: Lesson) {
    setEditLesson(l)
    setEditForm({ status: l.status, roomId: l.roomId ? String(l.roomId) : '', note: l.note ?? '' })
  }

  return (
    <div>
      <PageHeader
        title="Thời khóa biểu"
        actions={csId ? <Button onClick={() => setShowForm(true)}><Plus size={16} /> Thêm tiết học</Button> : undefined}
      />

      <div className="flex gap-4 mb-6 flex-wrap">
        <Select label="Học kỳ" value={semesterId} onChange={(e) => { setSemesterId(e.target.value); setCsId(''); setShowForm(false) }} className="w-52">
          <option value="">-- Chọn học kỳ --</option>
          {semesters?.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.academicYear})</option>)}
        </Select>
        <Select label="Môn / Lớp" value={csId} onChange={(e) => { setCsId(e.target.value); setShowForm(false) }} className="w-64" disabled={!semesterId}>
          <option value="">-- Chọn môn / lớp --</option>
          {csPage?.content.map((cs) => (
            <option key={cs.id} value={cs.id}>{cs.classroomName} — {cs.subjectName} ({cs.teacherName})</option>
          ))}
        </Select>
      </div>

      {showForm && csId && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm p-5 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-text-primary">Thêm tiết học mới</h2>
            <button onClick={() => setShowForm(false)} className="text-text-tertiary hover:text-text-primary"><X size={18} /></button>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Ngày học * (dd/MM/yyyy)"
              placeholder="15/06/2025"
              value={form.lessonDate}
              onChange={(e) => setForm((f) => ({ ...f, lessonDate: e.target.value }))}
            />
            <Select label="Phòng học" value={form.roomId} onChange={(e) => setForm((f) => ({ ...f, roomId: e.target.value }))}>
              <option value="">-- Không có / Chọn sau --</option>
              {rooms?.map((r) => <option key={r.id} value={r.id}>{r.code} ({r.campusName})</option>)}
            </Select>
            <Select label="Tiết bắt đầu *" value={form.startSlotId} onChange={(e) => setForm((f) => ({ ...f, startSlotId: e.target.value }))}>
              <option value="">-- Chọn tiết --</option>
              {timeSlots?.map((ts) => <option key={ts.id} value={ts.id}>Tiết {ts.slotNumber} ({ts.startTime})</option>)}
            </Select>
            <Select label="Tiết kết thúc *" value={form.endSlotId} onChange={(e) => setForm((f) => ({ ...f, endSlotId: e.target.value }))}>
              <option value="">-- Chọn tiết --</option>
              {timeSlots?.map((ts) => <option key={ts.id} value={ts.id}>Tiết {ts.slotNumber} ({ts.endTime})</option>)}
            </Select>
          </div>
          {createLesson.isError && <p className="text-sm text-status-danger mt-3">Tạo tiết học thất bại. Kiểm tra lại ngày và số tiết.</p>}
          <div className="flex gap-3 mt-4">
            <Button variant="secondary" onClick={() => setShowForm(false)}>Hủy</Button>
            <Button
              onClick={() => createLesson.mutate()}
              loading={createLesson.isPending}
              disabled={!form.lessonDate || !form.startSlotId || !form.endSlotId}
            >
              Tạo tiết học
            </Button>
          </div>
        </div>
      )}

      {editLesson && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm p-5 mb-6 max-w-lg">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-text-primary">Sửa tiết học — {editLesson.lessonDate} ({editLesson.slotLabel})</h2>
            <button onClick={() => setEditLesson(null)}><X size={18} className="text-text-tertiary hover:text-text-primary" /></button>
          </div>
          <div className="space-y-3">
            <Select label="Trạng thái" value={editForm.status} onChange={(e) => setEditForm(f => ({ ...f, status: e.target.value }))}>
              <option value="scheduled">Lịch học</option>
              <option value="completed">Đã học</option>
              <option value="cancelled">Đã hủy</option>
              <option value="makeup">Bù</option>
            </Select>
            <Select label="Phòng học" value={editForm.roomId} onChange={(e) => setEditForm(f => ({ ...f, roomId: e.target.value }))}>
              <option value="">-- Không có / Chọn sau --</option>
              {rooms?.map((r) => <option key={r.id} value={r.id}>{r.code} ({r.campusName})</option>)}
            </Select>
            <Input label="Ghi chú" value={editForm.note} onChange={(e) => setEditForm(f => ({ ...f, note: e.target.value }))} />
          </div>
          {updateLesson.isError && <p className="text-sm text-status-danger mt-3">Cập nhật thất bại.</p>}
          <div className="flex gap-3 mt-4">
            <Button variant="secondary" onClick={() => setEditLesson(null)}>Hủy</Button>
            <Button onClick={() => updateLesson.mutate()} loading={updateLesson.isPending}>Lưu thay đổi</Button>
          </div>
        </div>
      )}

      {csId && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-surface-elevated">
              <tr>
                {['Ngày', 'Tiết', 'Giờ', 'Phòng', 'Trạng thái', 'Ghi chú', ''].map((h) => (
                  <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border-light">
              {isLoading && <tr><td colSpan={7} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
              {!isLoading && !lessons?.length && <tr><td colSpan={7} className="px-4 py-8 text-center text-text-tertiary">Chưa có tiết học nào</td></tr>}
              {lessons?.map((l) => (
                <tr key={l.id} className="hover:bg-surface-bg">
                  <td className="px-4 py-3">{l.lessonDate}</td>
                  <td className="px-4 py-3">{l.slotLabel}</td>
                  <td className="px-4 py-3 text-text-secondary">{l.startTime}–{l.endTime}</td>
                  <td className="px-4 py-3">{l.roomCode ?? '—'}</td>
                  <td className="px-4 py-3">
                    <Badge variant={statusBadge[l.status] ?? 'default'}>{statusLabel[l.status] ?? l.status}</Badge>
                  </td>
                  <td className="px-4 py-3 text-text-secondary">{l.note ?? '—'}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 flex-wrap">
                      <button
                        onClick={() => openEdit(l)}
                        className="text-xs text-brand-blue hover:underline"
                      >
                        Sửa
                      </button>
                      {l.status === 'scheduled' && (
                        <>
                          <Button size="sm" variant="secondary" onClick={() => markDone.mutate(l.id)} loading={markDone.isPending}>Đã học</Button>
                          <Button size="sm" variant="danger" onClick={() => cancel.mutate(l.id)} loading={cancel.isPending}>Hủy</Button>
                        </>
                      )}
                      <button
                        onClick={() => {
                          if (confirm(`Xóa tiết học ngày ${l.lessonDate} (${l.slotLabel})?`))
                            deleteLesson.mutate(l.id)
                        }}
                        disabled={deleteLesson.isPending}
                        className="text-xs text-status-danger hover:underline disabled:opacity-50"
                      >
                        Xóa
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
