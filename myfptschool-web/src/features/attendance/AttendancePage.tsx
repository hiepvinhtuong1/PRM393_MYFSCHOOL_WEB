import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { useAuth } from '@/shared/hooks/useAuth'
import { PageHeader } from '@/shared/components/PageHeader'
import { Select } from '@/shared/components/ui/Select'
import { Button } from '@/shared/components/ui/Button'
import type { ClassroomSubject, MyClassroomSubject, Semester, AttendanceStatus } from '@/shared/types/models'
import type { PageResponse } from '@/shared/types/api'

interface Lesson {
  id: number
  lessonDate: string
  slotLabel: string
  startTime: string
  endTime: string
  status: string
}
interface AttendanceEntry {
  studentId: number
  studentCode: string
  fullName: string
  status: AttendanceStatus | null
  note: string | null
  recorded: boolean
}
interface LessonAttendance {
  lessonId: number
  subjectName: string
  classroomName: string
  teacherName: string
  totalStudents: number
  totalPresent: number
  totalAbsent: number
  totalUnrecorded: number
  entries: AttendanceEntry[]
}

const statusOptions: { value: AttendanceStatus; label: string }[] = [
  { value: 'present', label: 'Có mặt' },
  { value: 'late', label: 'Đi muộn' },
  { value: 'excused_absent', label: 'Vắng có phép' },
  { value: 'unexcused_absent', label: 'Vắng không phép' },
]

export function AttendancePage() {
  const { isAdmin } = useAuth()
  const [searchParams] = useSearchParams()
  const deepLinkLessonId = searchParams.get('lessonId')

  const [semesterId, setSemesterId] = useState('')
  const [csId, setCsId] = useState('')
  const [lessonId, setLessonId] = useState(deepLinkLessonId ?? '')
  const [localEntries, setLocalEntries] = useState<Record<number, { status: AttendanceStatus; note: string }>>({})
  const qc = useQueryClient()

  // Admin mode: 3-step (semester → CS → lesson)
  const { data: semesters } = useQuery({
    queryKey: queryKeys.semesters.list(),
    queryFn: () => apiGet<Semester[]>('/admin/semesters'),
    enabled: isAdmin,
  })
  const { data: adminCsPage } = useQuery({
    queryKey: queryKeys.classroomSubjects.list({ semesterId }),
    queryFn: () => apiGet<PageResponse<ClassroomSubject>>('/admin/classroom-subjects', { semesterId, size: 100 }),
    enabled: isAdmin && Boolean(semesterId),
  })

  // Teacher mode: 2-step (my CS → lesson)
  const { data: myCS = [] } = useQuery({
    queryKey: queryKeys.me.classroomSubjects(),
    queryFn: () => apiGet<MyClassroomSubject[]>('/me/classroom-subjects'),
    enabled: !isAdmin,
  })

  const { data: lessons } = useQuery({
    queryKey: queryKeys.classroomSubjects.lessons(Number(csId)),
    queryFn: () => apiGet<Lesson[]>(`/admin/classroom-subjects/${csId}/lessons`),
    enabled: Boolean(csId),
  })

  const { data: attendance, isLoading } = useQuery({
    queryKey: queryKeys.lessons.attendance(Number(lessonId)),
    queryFn: () => apiGet<LessonAttendance>(`/admin/lessons/${lessonId}/attendance`),
    enabled: Boolean(lessonId),
  })

  // When deep-linked: if we have csId from attendance data, keep it; reset local edits
  useEffect(() => { setLocalEntries({}) }, [lessonId])

  // If deep-linked, load attendance info to backfill csId for the lesson selector
  useEffect(() => {
    if (deepLinkLessonId && attendance && !csId) {
      // csId not needed when deep-linked — we already have the lesson
    }
  }, [deepLinkLessonId, attendance, csId])

  const submit = useMutation({
    mutationFn: (entries: { studentId: number; status: AttendanceStatus; note: string | null }[]) =>
      apiPost(`/admin/lessons/${lessonId}/attendance`, { entries }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.lessons.attendance(Number(lessonId)) })
      setLocalEntries({})
    },
  })

  function getStatus(entry: AttendanceEntry): AttendanceStatus | null {
    return localEntries[entry.studentId]?.status ?? entry.status
  }

  function handleMarkAll() {
    if (!attendance) return
    const all: Record<number, { status: AttendanceStatus; note: string }> = {}
    attendance.entries.forEach((e) => {
      all[e.studentId] = { status: 'present', note: localEntries[e.studentId]?.note ?? e.note ?? '' }
    })
    setLocalEntries(all)
  }

  function handleSave() {
    const entries = attendance!.entries.map((e) => ({
      studentId: e.studentId,
      status: localEntries[e.studentId]?.status ?? (e.status || 'present'),
      note: localEntries[e.studentId]?.note ?? e.note ?? null,
    }))
    submit.mutate(entries)
  }

  const csOptions = isAdmin
    ? (adminCsPage?.content ?? []).map((cs) => ({ id: cs.id, label: `${cs.classroomName} — ${cs.subjectName}` }))
    : myCS.map((cs) => ({ id: cs.id, label: `${cs.classroomName} — ${cs.subjectName} (${cs.semesterName} ${cs.academicYear})` }))

  return (
    <div>
      <PageHeader title="Điểm danh" />

      {/* Deep-link mode: show lesson info header, skip dropdowns */}
      {deepLinkLessonId && attendance && (
        <div className="mb-6 bg-white rounded-xl border border-border-light px-5 py-3 text-sm text-text-secondary">
          <span className="font-semibold text-text-primary">{attendance.classroomName}</span>
          {' · '}{attendance.subjectName}
          {' · GV: '}{attendance.teacherName}
        </div>
      )}

      {/* Selector: only show when NOT in deep-link mode */}
      {!deepLinkLessonId && (
        <div className="flex gap-4 mb-6 flex-wrap">
          {isAdmin && (
            <Select
              label="Học kỳ"
              value={semesterId}
              onChange={(e) => { setSemesterId(e.target.value); setCsId(''); setLessonId('') }}
              className="w-52"
            >
              <option value="">-- Học kỳ --</option>
              {semesters?.map((s) => (
                <option key={s.id} value={s.id}>{s.name} ({s.academicYear})</option>
              ))}
            </Select>
          )}

          <Select
            label="Môn / Lớp"
            value={csId}
            onChange={(e) => { setCsId(e.target.value); setLessonId('') }}
            disabled={isAdmin && !semesterId}
            className="w-72"
          >
            <option value="">-- Môn / Lớp --</option>
            {csOptions.map((cs) => (
              <option key={cs.id} value={cs.id}>{cs.label}</option>
            ))}
          </Select>

          <Select
            label="Tiết học"
            value={lessonId}
            onChange={(e) => setLessonId(e.target.value)}
            disabled={!csId}
            className="w-72"
          >
            <option value="">-- Chọn tiết --</option>
            {lessons?.filter((l) => l.status !== 'cancelled').map((l) => (
              <option key={l.id} value={l.id}>
                {l.lessonDate} — {l.slotLabel} ({l.startTime.slice(0, 5)}–{l.endTime.slice(0, 5)})
              </option>
            ))}
          </Select>
        </div>
      )}

      {/* Attendance table */}
      {lessonId && attendance && (
        <>
          <div className="flex gap-4 mb-4 flex-wrap items-end">
            {[
              { label: 'Có mặt', val: attendance.totalPresent, color: 'text-green-600' },
              { label: 'Vắng', val: attendance.totalAbsent, color: 'text-red-600' },
              { label: 'Chưa ghi', val: attendance.totalUnrecorded, color: 'text-amber-600' },
              { label: 'Tổng', val: attendance.totalStudents, color: 'text-text-primary' },
            ].map((s) => (
              <div key={s.label} className="bg-white rounded-lg border border-border-light px-4 py-3 min-w-[90px]">
                <div className={`text-xl font-bold ${s.color}`}>{s.val}</div>
                <div className="text-xs text-text-secondary">{s.label}</div>
              </div>
            ))}
            <div className="ml-auto flex gap-2">
              <Button variant="secondary" onClick={handleMarkAll}>Có mặt tất cả</Button>
              <Button onClick={handleSave} loading={submit.isPending}>Lưu điểm danh</Button>
            </div>
          </div>

          {submit.isError && (
            <div className="mb-4 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-status-danger">
              Lưu thất bại. Vui lòng thử lại.
            </div>
          )}
          {submit.isSuccess && (
            <div className="mb-4 rounded-lg bg-green-50 border border-green-200 px-4 py-3 text-sm text-green-800">
              Đã lưu điểm danh thành công.
            </div>
          )}

          <div className="bg-white rounded-xl border border-border-light shadow-sm overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface-elevated">
                <tr>
                  {['STT', 'Mã HS', 'Họ tên', 'Trạng thái', 'Ghi chú'].map((h) => (
                    <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-border-light">
                {isLoading && (
                  <tr><td colSpan={5} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>
                )}
                {attendance.entries.map((e, i) => {
                  const current = getStatus(e)
                  return (
                    <tr key={e.studentId} className="hover:bg-surface-bg">
                      <td className="px-4 py-2 text-text-tertiary">{i + 1}</td>
                      <td className="px-4 py-2 font-mono text-xs">{e.studentCode}</td>
                      <td className="px-4 py-2 font-medium">{e.fullName}</td>
                      <td className="px-4 py-2 w-52">
                        <select
                          value={current ?? ''}
                          onChange={(ev) =>
                            setLocalEntries((prev) => ({
                              ...prev,
                              [e.studentId]: {
                                status: ev.target.value as AttendanceStatus,
                                note: prev[e.studentId]?.note ?? e.note ?? '',
                              },
                            }))
                          }
                          className="w-full h-8 text-xs border border-border-light rounded px-2 focus:outline-none focus:border-brand-blue"
                        >
                          <option value="">— Chọn —</option>
                          {statusOptions.map((o) => (
                            <option key={o.value} value={o.value}>{o.label}</option>
                          ))}
                        </select>
                      </td>
                      <td className="px-4 py-2">
                        <input
                          type="text"
                          value={localEntries[e.studentId]?.note ?? e.note ?? ''}
                          onChange={(ev) =>
                            setLocalEntries((prev) => ({
                              ...prev,
                              [e.studentId]: {
                                status: prev[e.studentId]?.status ?? e.status ?? 'present',
                                note: ev.target.value,
                              },
                            }))
                          }
                          placeholder="Ghi chú..."
                          className="w-full h-8 text-xs border border-border-light rounded px-2 focus:outline-none focus:border-brand-blue"
                        />
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </>
      )}

      {!lessonId && (
        <div className="bg-white rounded-xl border border-border-light p-8 text-center text-text-tertiary">
          {isAdmin
            ? 'Chọn học kỳ → môn/lớp → tiết học để bắt đầu điểm danh'
            : 'Chọn môn/lớp → tiết học để bắt đầu điểm danh'}
        </div>
      )}
    </div>
  )
}
