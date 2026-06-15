import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPatch } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Select } from '@/shared/components/ui/Select'
import { Button } from '@/shared/components/ui/Button'
import { Badge } from '@/shared/components/ui/Badge'
import type { ClassroomSubject, Semester } from '@/shared/types/models'
import type { PageResponse } from '@/shared/types/api'

interface Lesson {
  id: number
  classroomSubjectId: number
  lessonDate: string
  startSlotNumber: number
  endSlotNumber: number
  startTime: string
  endTime: string
  slotLabel: string
  roomId: number | null
  roomCode: string | null
  status: string
  hasMaterials: boolean
  note: string | null
}

const statusBadge: Record<string, 'success' | 'default' | 'danger' | 'warning'> = {
  scheduled: 'info' as never,
  completed: 'success',
  cancelled: 'danger',
  makeup: 'warning',
}
const statusLabel: Record<string, string> = {
  scheduled: 'Lịch học',
  completed: 'Đã học',
  cancelled: 'Đã hủy',
  makeup: 'Bù',
}

export function TimetablePage() {
  const [semesterId, setSemesterId] = useState('')
  const [csId, setCsId] = useState('')
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

  const markDone = useMutation({
    mutationFn: (id: number) => apiPatch(`/admin/lessons/${id}`, { status: 'completed' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.classroomSubjects.lessons(Number(csId)) }),
  })

  const cancel = useMutation({
    mutationFn: (id: number) => apiPatch(`/admin/lessons/${id}`, { status: 'cancelled' }),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.classroomSubjects.lessons(Number(csId)) }),
  })

  return (
    <div>
      <PageHeader title="Thời khóa biểu" />

      <div className="flex gap-4 mb-6 flex-wrap">
        <Select label="Học kỳ" value={semesterId} onChange={(e) => { setSemesterId(e.target.value); setCsId('') }} className="w-52">
          <option value="">-- Chọn học kỳ --</option>
          {semesters?.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.academicYear})</option>)}
        </Select>

        <Select label="Môn / Lớp" value={csId} onChange={(e) => setCsId(e.target.value)} className="w-64" disabled={!semesterId}>
          <option value="">-- Chọn môn / lớp --</option>
          {csPage?.content.map((cs) => (
            <option key={cs.id} value={cs.id}>{cs.classroomName} — {cs.subjectName} ({cs.teacherName})</option>
          ))}
        </Select>
      </div>

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
                    {l.status === 'scheduled' && (
                      <div className="flex gap-2">
                        <Button size="sm" variant="secondary" onClick={() => markDone.mutate(l.id)} loading={markDone.isPending}>Đã học</Button>
                        <Button size="sm" variant="danger" onClick={() => cancel.mutate(l.id)} loading={cancel.isPending}>Hủy</Button>
                      </div>
                    )}
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
