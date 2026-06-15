import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { X } from 'lucide-react'
import { apiGet } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import type { Classroom, StudentSummary } from '@/shared/types/models'

export function ClassroomListPage() {
  const [selectedId, setSelectedId] = useState<number | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.classrooms.list(),
    queryFn: () => apiGet<Classroom[]>('/admin/classrooms'),
  })

  const selectedClassroom = data?.find((cl) => cl.id === selectedId)

  const { data: studentsData, isLoading: loadingStudents } = useQuery({
    queryKey: queryKeys.classrooms.students(selectedId!),
    queryFn: () => apiGet<{ content: StudentSummary[] }>(`/admin/classrooms/${selectedId}/students`, { size: 50 }),
    enabled: selectedId != null,
  })

  const byGrade = [10, 11, 12].map((g) => ({
    grade: g,
    classrooms: data?.filter((cl) => cl.gradeLevel === g) ?? [],
  }))

  return (
    <div>
      <PageHeader title="Lớp học" subtitle="30 lớp cố định — 3 khối × 10 lớp" />
      {isLoading && <p className="text-text-tertiary">Đang tải...</p>}

      <div className="space-y-6">
        {byGrade.map(({ grade, classrooms }) => (
          <div key={grade} className="bg-white rounded-xl border border-border-light shadow-sm">
            <div className="px-5 py-3 border-b border-border-light font-semibold text-text-primary">
              Khối {grade}
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-px bg-border-light">
              {classrooms.map((cl) => (
                <button
                  key={cl.id}
                  onClick={() => setSelectedId(selectedId === cl.id ? null : cl.id)}
                  className={`bg-white px-4 py-3 transition-colors text-left w-full ${
                    selectedId === cl.id
                      ? 'bg-orange-50 ring-inset ring-1 ring-brand-orange'
                      : 'hover:bg-surface-bg'
                  }`}
                >
                  <div className="font-semibold text-text-primary">{cl.name}</div>
                  <div className="text-xs text-text-secondary mt-0.5">{cl.studentCount} học sinh</div>
                  <div className="text-xs text-text-tertiary">{cl.campusName}</div>
                </button>
              ))}
            </div>
          </div>
        ))}
      </div>

      {selectedId && (
        <div className="mt-6 bg-white rounded-xl border border-border-light shadow-sm">
          <div className="px-5 py-3 border-b border-border-light flex items-center justify-between">
            <h2 className="font-semibold text-text-primary">
              Danh sách học sinh — {selectedClassroom?.name}
              <span className="ml-2 text-sm font-normal text-text-secondary">
                ({selectedClassroom?.studentCount} HS)
              </span>
            </h2>
            <button
              onClick={() => setSelectedId(null)}
              className="text-text-tertiary hover:text-text-primary"
            >
              <X size={18} />
            </button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface-elevated">
                <tr>
                  {['Mã HS', 'Họ tên', 'Ngày sinh', 'Giới tính', 'SĐT', 'Email'].map((h) => (
                    <th key={h} className="px-4 py-3 text-left font-semibold text-text-secondary text-xs uppercase tracking-wide">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-border-light">
                {loadingStudents && (
                  <tr><td colSpan={6} className="px-4 py-8 text-center text-text-tertiary">Đang tải...</td></tr>
                )}
                {!loadingStudents && !studentsData?.content.length && (
                  <tr><td colSpan={6} className="px-4 py-8 text-center text-text-tertiary">Chưa có học sinh</td></tr>
                )}
                {studentsData?.content.map((s) => (
                  <tr key={s.id} className="hover:bg-surface-bg transition-colors">
                    <td className="px-4 py-3 font-mono text-xs">{s.studentCode}</td>
                    <td className="px-4 py-3 font-medium">{s.fullName}</td>
                    <td className="px-4 py-3">{s.dateOfBirth ?? '—'}</td>
                    <td className="px-4 py-3">{s.gender ?? '—'}</td>
                    <td className="px-4 py-3">{s.phone ?? '—'}</td>
                    <td className="px-4 py-3">{s.email ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
