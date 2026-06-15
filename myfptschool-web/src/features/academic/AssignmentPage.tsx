import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, X } from 'lucide-react'
import { apiDelete, apiGet, apiPost } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Select } from '@/shared/components/ui/Select'
import { Button } from '@/shared/components/ui/Button'
import type { ClassroomSubject, Classroom, Semester, Subject, Teacher } from '@/shared/types/models'
import type { PageResponse } from '@/shared/types/api'

export function AssignmentPage() {
  const [semesterId, setSemesterId] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ classroomId: '', subjectId: '', teacherId: '', semesterId: '' })
  const qc = useQueryClient()

  const { data: semesters } = useQuery({
    queryKey: queryKeys.semesters.list(),
    queryFn: () => apiGet<Semester[]>('/admin/semesters'),
  })
  const { data: classrooms } = useQuery({
    queryKey: queryKeys.classrooms.list(),
    queryFn: () => apiGet<Classroom[]>('/admin/classrooms'),
    enabled: showForm,
  })
  const { data: subjects } = useQuery({
    queryKey: queryKeys.subjects.list(),
    queryFn: () => apiGet<Subject[]>('/admin/subjects'),
    enabled: showForm,
  })
  const { data: teachersPage } = useQuery({
    queryKey: queryKeys.teachers.list({ size: 100 }),
    queryFn: () => apiGet<PageResponse<Teacher>>('/admin/teachers', { size: 100 }),
    enabled: showForm,
  })

  const params = { semesterId: semesterId || undefined, size: 200 }
  const { data, isLoading } = useQuery({
    queryKey: queryKeys.classroomSubjects.list(params),
    queryFn: () => apiGet<PageResponse<ClassroomSubject>>('/admin/classroom-subjects', params),
    enabled: Boolean(semesterId),
  })

  const remove = useMutation({
    mutationFn: (id: number) => apiDelete(`/admin/classroom-subjects/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.classroomSubjects.list({}) }),
    onError: (err: unknown) => {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
      alert(msg ?? 'Xóa phân công thất bại')
    },
  })

  const create = useMutation({
    mutationFn: () => apiPost('/admin/classroom-subjects', {
      classroomId: Number(form.classroomId),
      subjectId: Number(form.subjectId),
      teacherId: Number(form.teacherId),
      semesterId: Number(form.semesterId || semesterId),
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.classroomSubjects.list({}) })
      setShowForm(false)
      setForm({ classroomId: '', subjectId: '', teacherId: '', semesterId: '' })
    },
  })

  const canSubmit = form.classroomId && form.subjectId && form.teacherId && (form.semesterId || semesterId)

  return (
    <div>
      <PageHeader
        title="Phân công giảng dạy"
        actions={
          <Button onClick={() => setShowForm(true)}><Plus size={16} /> Phân công mới</Button>
        }
      />

      {showForm && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm p-5 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold text-text-primary">Thêm phân công giảng dạy</h2>
            <button onClick={() => setShowForm(false)} className="text-text-tertiary hover:text-text-primary"><X size={18} /></button>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Select label="Học kỳ *" value={form.semesterId || semesterId} onChange={(e) => setForm((f) => ({ ...f, semesterId: e.target.value }))}>
              <option value="">-- Chọn học kỳ --</option>
              {semesters?.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.academicYear})</option>)}
            </Select>
            <Select label="Lớp *" value={form.classroomId} onChange={(e) => setForm((f) => ({ ...f, classroomId: e.target.value }))}>
              <option value="">-- Chọn lớp --</option>
              {classrooms?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </Select>
            <Select label="Môn học *" value={form.subjectId} onChange={(e) => setForm((f) => ({ ...f, subjectId: e.target.value }))}>
              <option value="">-- Chọn môn --</option>
              {subjects?.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </Select>
            <Select label="Giáo viên *" value={form.teacherId} onChange={(e) => setForm((f) => ({ ...f, teacherId: e.target.value }))}>
              <option value="">-- Chọn giáo viên --</option>
              {teachersPage?.content.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
            </Select>
          </div>
          {create.isError && <p className="text-sm text-status-danger mt-3">Tạo phân công thất bại. Kiểm tra lại (có thể đã tồn tại).</p>}
          <div className="flex gap-3 mt-4">
            <Button variant="secondary" onClick={() => setShowForm(false)}>Hủy</Button>
            <Button onClick={() => create.mutate()} loading={create.isPending} disabled={!canSubmit}>Tạo phân công</Button>
          </div>
        </div>
      )}

      <div className="flex gap-4 mb-6">
        <Select label="Học kỳ" value={semesterId} onChange={(e) => setSemesterId(e.target.value)} className="w-48">
          <option value="">-- Chọn học kỳ --</option>
          {semesters?.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.academicYear})</option>)}
        </Select>
      </div>

      {!semesterId && (
        <div className="bg-white rounded-xl border border-border-light p-8 text-center text-text-tertiary">
          Chọn học kỳ để xem phân công giảng dạy
        </div>
      )}

      {semesterId && (
        <div className="bg-white rounded-xl border border-border-light shadow-sm overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-surface-elevated">
              <tr>
                {['Lớp', 'Môn học', 'Giáo viên', 'Học kỳ', ''].map((h) => (
                  <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border-light">
              {isLoading && <tr><td colSpan={4} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
              {!isLoading && !data?.content.length && <tr><td colSpan={5} className="px-4 py-8 text-center text-text-tertiary">Chưa có phân công</td></tr>}
              {data?.content.map((cs) => (
                <tr key={cs.id} className="hover:bg-surface-bg">
                  <td className="px-4 py-3 font-medium">{cs.classroomName}</td>
                  <td className="px-4 py-3">
                    <span className="inline-flex items-center gap-1.5">
                      <span className="w-2 h-2 rounded-full" style={{ backgroundColor: cs.subjectColorHex }} />
                      {cs.subjectName}
                    </span>
                  </td>
                  <td className="px-4 py-3">{cs.teacherName}</td>
                  <td className="px-4 py-3 text-text-secondary">{cs.semesterName}</td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => {
                        if (confirm(`Xóa phân công ${cs.classroomName} — ${cs.subjectName}?`))
                          remove.mutate(cs.id)
                      }}
                      disabled={remove.isPending}
                      className="text-xs text-status-danger hover:underline disabled:opacity-50"
                    >
                      Xóa
                    </button>
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
