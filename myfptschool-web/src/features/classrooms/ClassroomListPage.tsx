import { useQuery } from '@tanstack/react-query'
import { apiGet } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import type { Classroom } from '@/shared/types/models'

export function ClassroomListPage() {
  const { data, isLoading } = useQuery({
    queryKey: queryKeys.classrooms.list(),
    queryFn: () => apiGet<Classroom[]>('/admin/classrooms'),
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
                <div key={cl.id} className="bg-white px-4 py-3 hover:bg-surface-bg transition-colors">
                  <div className="font-semibold text-text-primary">{cl.name}</div>
                  <div className="text-xs text-text-secondary mt-0.5">{cl.studentCount} học sinh</div>
                  <div className="text-xs text-text-tertiary">{cl.campusName}</div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
