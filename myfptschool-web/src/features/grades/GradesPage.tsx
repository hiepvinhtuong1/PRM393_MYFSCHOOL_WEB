import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Select } from '@/shared/components/ui/Select'
import { Button } from '@/shared/components/ui/Button'
import type { ClassroomSubject, Semester } from '@/shared/types/models'
import type { PageResponse } from '@/shared/types/api'

interface ScoreComponentDto { id: number; code: string; name: string; weight: number }
interface GradeEntry { gradeRecordId: number | null; score: number | null }
interface StudentGradeRow { studentId: number; studentCode: string; fullName: string; scores: Record<string, GradeEntry>; average: number | null }
interface GradeSheet { classroomSubjectId: number; subjectName: string; classroomName: string; teacherName: string; components: ScoreComponentDto[]; students: StudentGradeRow[] }

export function GradesPage() {
  const [semesterId, setSemesterId] = useState('')
  const [csId, setCsId] = useState('')
  const [edits, setEdits] = useState<Record<string, string>>({})
  const qc = useQueryClient()

  const { data: semesters } = useQuery({ queryKey: queryKeys.semesters.list(), queryFn: () => apiGet<Semester[]>('/admin/semesters') })
  const { data: csPage } = useQuery({
    queryKey: queryKeys.classroomSubjects.list({ semesterId }),
    queryFn: () => apiGet<PageResponse<ClassroomSubject>>('/admin/classroom-subjects', { semesterId, size: 100 }),
    enabled: Boolean(semesterId),
  })
  const { data: sheet, isLoading } = useQuery({
    queryKey: queryKeys.classroomSubjects.grades(Number(csId)),
    queryFn: () => apiGet<GradeSheet>(`/admin/classroom-subjects/${csId}/grades`),
    enabled: Boolean(csId),
    select: (d) => { setEdits({}); return d },
  })

  const save = useMutation({
    mutationFn: () => {
      const entries = Object.entries(edits).map(([key, val]) => {
        const [studentId, componentId] = key.split('_')
        return { studentId: Number(studentId), componentId: Number(componentId), score: val === '' ? null : parseFloat(val) }
      })
      return apiPost(`/admin/classroom-subjects/${csId}/grades`, { entries })
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.classroomSubjects.grades(Number(csId)) })
      setEdits({})
    },
  })

  function getScore(row: StudentGradeRow, comp: ScoreComponentDto) {
    const key = `${row.studentId}_${comp.id}`
    if (key in edits) return edits[key]
    const entry = row.scores[comp.code]
    return entry?.score != null ? String(entry.score) : ''
  }

  return (
    <div>
      <PageHeader title="Nhập điểm số" />
      <div className="flex gap-4 mb-6 flex-wrap">
        <Select label="Học kỳ" value={semesterId} onChange={(e) => { setSemesterId(e.target.value); setCsId('') }} className="w-48">
          <option value="">-- Học kỳ --</option>
          {semesters?.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </Select>
        <Select label="Môn / Lớp" value={csId} onChange={(e) => setCsId(e.target.value)} disabled={!semesterId} className="w-64">
          <option value="">-- Môn / Lớp --</option>
          {csPage?.content.map((cs) => <option key={cs.id} value={cs.id}>{cs.classroomName} — {cs.subjectName}</option>)}
        </Select>
      </div>

      {csId && sheet && (
        <>
          <div className="flex items-center justify-between mb-4">
            <div className="text-sm text-text-secondary">
              {sheet.classroomName} · {sheet.subjectName} · GV: {sheet.teacherName}
            </div>
            <Button onClick={() => save.mutate()} loading={save.isPending} disabled={Object.keys(edits).length === 0}>
              Lưu điểm
            </Button>
          </div>

          <div className="bg-white rounded-xl border border-border-light shadow-sm overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface-elevated">
                <tr>
                  <th className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase w-28">Mã HS</th>
                  <th className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase">Họ tên</th>
                  {sheet.components.map((c) => (
                    <th key={c.id} className="px-3 py-3 text-center font-semibold text-text-secondary text-xs uppercase w-20" title={c.name}>
                      {c.code}<br /><span className="font-normal normal-case text-text-tertiary">×{c.weight}</span>
                    </th>
                  ))}
                  <th className="px-3 py-3 text-center font-semibold text-text-secondary text-xs uppercase w-16">ĐTK</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-light">
                {isLoading && <tr><td colSpan={(sheet?.components.length ?? 0) + 3} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
                {sheet.students.map((row) => (
                  <tr key={row.studentId} className="hover:bg-surface-bg">
                    <td className="px-4 py-2 font-mono text-xs">{row.studentCode}</td>
                    <td className="px-4 py-2 font-medium">{row.fullName}</td>
                    {sheet.components.map((c) => {
                      const val = getScore(row, c)
                      const key = `${row.studentId}_${c.id}`
                      return (
                        <td key={c.id} className="px-2 py-1">
                          <input
                            type="number"
                            min="0" max="10" step="0.25"
                            value={val}
                            onChange={(e) => setEdits((prev) => ({ ...prev, [key]: e.target.value }))}
                            placeholder="—"
                            className="w-16 h-7 text-center text-sm border border-border-light rounded focus:outline-none focus:border-brand-blue tabular-nums"
                          />
                        </td>
                      )
                    })}
                    <td className="px-3 py-2 text-center font-semibold tabular-nums">
                      {row.average != null ? row.average.toFixed(1) : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {!csId && (
        <div className="bg-white rounded-xl border border-border-light p-8 text-center text-text-tertiary">
          Chọn học kỳ → môn/lớp để nhập điểm
        </div>
      )}
    </div>
  )
}
