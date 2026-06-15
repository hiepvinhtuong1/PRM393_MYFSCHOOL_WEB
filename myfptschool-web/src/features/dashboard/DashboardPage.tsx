import { useQuery } from '@tanstack/react-query'
import { Users, GraduationCap, School, BookOpen } from 'lucide-react'
import { useAuth } from '@/shared/hooks/useAuth'
import { apiGet } from '@/shared/lib/api'
import { queryKeys } from '@/shared/lib/queryKeys'
import { PageHeader } from '@/shared/components/PageHeader'
import type { PageResponse } from '@/shared/types/api'
import type { Classroom, Student, Teacher, Subject } from '@/shared/types/models'

export function DashboardPage() {
  const { user } = useAuth()

  const { data: students } = useQuery({
    queryKey: queryKeys.students.list({ size: 1 }),
    queryFn: () => apiGet<PageResponse<Student>>('/admin/students', { size: 1 }),
  })
  const { data: teachers } = useQuery({
    queryKey: queryKeys.teachers.list({ size: 1 }),
    queryFn: () => apiGet<PageResponse<Teacher>>('/admin/teachers', { size: 1 }),
  })
  const { data: classrooms } = useQuery({
    queryKey: queryKeys.classrooms.list(),
    queryFn: () => apiGet<Classroom[]>('/admin/classrooms'),
  })
  const { data: subjects } = useQuery({
    queryKey: queryKeys.subjects.list(),
    queryFn: () => apiGet<Subject[]>('/admin/subjects'),
  })

  const stats = [
    { label: 'Học sinh', value: students?.totalElements ?? '—', icon: Users, color: 'text-brand-orange', bg: 'bg-orange-50' },
    { label: 'Giáo viên', value: teachers?.totalElements ?? '—', icon: GraduationCap, color: 'text-brand-blue', bg: 'bg-blue-50' },
    { label: 'Lớp học', value: classrooms?.length ?? '—', icon: School, color: 'text-brand-green', bg: 'bg-green-50' },
    { label: 'Môn học', value: subjects?.length ?? '—', icon: BookOpen, color: 'text-purple-500', bg: 'bg-purple-50' },
  ]

  return (
    <div>
      <PageHeader
        title="Tổng quan"
        subtitle={`Xin chào, ${user?.fullName ?? 'Admin'}`}
      />

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {stats.map((card) => {
          const Icon = card.icon
          return (
            <div key={card.label} className="bg-white rounded-xl border border-border-light p-5 shadow-sm">
              <div className={`inline-flex items-center justify-center w-9 h-9 rounded-lg mb-3 ${card.bg}`}>
                <Icon size={18} className={card.color} />
              </div>
              <div className="text-2xl font-bold text-text-primary">{card.value}</div>
              <div className="text-sm text-text-secondary mt-0.5">{card.label}</div>
            </div>
          )
        })}
      </div>

      <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm">
        <h2 className="text-base font-semibold text-text-primary mb-3">Thao tác nhanh</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {[
            { label: 'Thêm học sinh', href: '/students/new', color: 'border-brand-orange text-brand-orange' },
            { label: 'Thêm giáo viên', href: '/teachers/new', color: 'border-brand-blue text-brand-blue' },
            { label: 'Phân công mới', href: '/assignments', color: 'border-brand-green text-brand-green' },
            { label: 'Soạn thông báo', href: '/notifications/new', color: 'border-purple-400 text-purple-500' },
          ].map((item) => (
            <a
              key={item.label}
              href={item.href}
              className={`flex items-center justify-center rounded-lg border-2 px-4 py-3 text-sm font-medium hover:bg-surface-bg transition-colors ${item.color}`}
            >
              {item.label}
            </a>
          ))}
        </div>
      </div>
    </div>
  )
}
