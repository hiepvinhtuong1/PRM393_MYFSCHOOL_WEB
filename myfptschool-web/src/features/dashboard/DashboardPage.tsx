import { useAuth } from '@/shared/hooks/useAuth'
import { PageHeader } from '@/shared/components/PageHeader'
import { Users, GraduationCap, School, BookOpen } from 'lucide-react'

const statCards = [
  { label: 'Học sinh', value: '~1000', icon: Users, color: 'text-brand-orange' },
  { label: 'Giáo viên', value: '~75', icon: GraduationCap, color: 'text-brand-blue' },
  { label: 'Lớp học', value: '30', icon: School, color: 'text-brand-green' },
  { label: 'Môn học', value: '8', icon: BookOpen, color: 'text-purple-500' },
]

export function DashboardPage() {
  const { user } = useAuth()

  return (
    <div>
      <PageHeader
        title="Dashboard"
        subtitle={`Xin chào, ${user?.fullName ?? 'Admin'}`}
      />

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {statCards.map((card) => {
          const Icon = card.icon
          return (
            <div key={card.label} className="bg-white rounded-xl border border-border-light p-5 shadow-sm">
              <div className={`mb-3 ${card.color}`}>
                <Icon size={20} />
              </div>
              <div className="text-2xl font-bold text-text-primary">{card.value}</div>
              <div className="text-sm text-text-secondary mt-0.5">{card.label}</div>
            </div>
          )
        })}
      </div>

      <div className="bg-white rounded-xl border border-border-light p-6 shadow-sm">
        <h2 className="text-base font-semibold text-text-primary mb-2">Thao tác nhanh</h2>
        <p className="text-sm text-text-secondary">
          Dùng menu bên trái để điều hướng đến các tính năng quản trị.
        </p>
      </div>
    </div>
  )
}
