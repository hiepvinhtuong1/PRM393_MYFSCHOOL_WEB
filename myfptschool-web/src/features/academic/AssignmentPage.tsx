import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { apiGet } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import { Select } from '@/shared/components/ui/Select'
import type { ClassroomSubject, Semester } from '@/shared/types/models'
import type { PageResponse } from '@/shared/types/api'

export function AssignmentPage() {
  const [semesterId, setSemesterId] = useState('')

  const { data: semesters } = useQuery({
    queryKey: queryKeys.semesters.list(),
    queryFn: () => apiGet<Semester[]>('/admin/semesters'),
  })

  const params = {
    semesterId: semesterId || undefined,
    size: 100,
  }

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.classroomSubjects.list(params),
    queryFn: () => apiGet<PageResponse<ClassroomSubject>>('/admin/classroom-subjects', params),
    enabled: Boolean(semesterId),
  })

  return (
    <div>
      <PageHeader title="Phân công giảng dạy" />

      <div className="flex gap-4 mb-6">
        <Select
          label="Học kỳ"
          value={semesterId}
          onChange={(e) => setSemesterId(e.target.value)}
          className="w-48"
        >
          <option value="">-- Chọn học kỳ --</option>
          {semesters?.map((s) => (
            <option key={s.id} value={s.id}>{s.name} ({s.academicYear})</option>
          ))}
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
                {['Lớp', 'Môn học', 'Giáo viên', 'Học kỳ'].map((h) => (
                  <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border-light">
              {isLoading && <tr><td colSpan={4} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>}
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
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
