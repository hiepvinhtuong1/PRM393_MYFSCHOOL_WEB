import { NavLink } from 'react-router-dom'
import { cn } from '@/shared/lib/utils'
import { useAuth } from '@/shared/hooks/useAuth'
import {
  LayoutDashboard, Users, GraduationCap, School, BookOpen,
  CalendarDays, ClipboardList, BarChart3, Bell, LogOut, UserCheck,
} from 'lucide-react'

const navItems = [
  { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
  { group: 'Học vụ' },
  { label: 'Học sinh', to: '/students', icon: Users },
  { label: 'Phụ huynh', to: '/parents', icon: UserCheck, adminOnly: true },
  { label: 'Giáo viên', to: '/teachers', icon: GraduationCap, adminOnly: true },
  { label: 'Lớp học', to: '/classrooms', icon: School },
  { group: 'Giảng dạy' },
  { label: 'Phân công', to: '/assignments', icon: BookOpen, adminOnly: true },
  { label: 'Thời khóa biểu', to: '/timetable', icon: CalendarDays },
  { group: 'Nghiệp vụ' },
  { label: 'Điểm danh', to: '/attendance', icon: ClipboardList },
  { label: 'Điểm số', to: '/grades', icon: BarChart3 },
  { label: 'Thông báo', to: '/notifications', icon: Bell },
]

export function Sidebar() {
  const { user, isAdmin, logout } = useAuth()

  return (
    <aside className="w-60 shrink-0 bg-white border-r border-border-light flex flex-col h-screen sticky top-0">
      {/* Logo */}
      <div className="h-16 flex items-center px-5 border-b border-border-light">
        <span className="text-brand-orange font-bold text-lg leading-tight">
          MyFPT<span className="text-text-primary">School</span>
        </span>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-4 px-3">
        {navItems.map((item, i) => {
          if ('group' in item) {
            return (
              <p key={i} className="px-2 pt-4 pb-1 text-xs font-semibold uppercase tracking-wider text-text-tertiary">
                {item.group}
              </p>
            )
          }
          if ('adminOnly' in item && item.adminOnly && !isAdmin) return null
          const Icon = item.icon!
          return (
            <NavLink
              key={item.to}
              to={item.to!}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium mb-0.5 transition-colors',
                  isActive
                    ? 'bg-orange-50 text-brand-orange'
                    : 'text-text-secondary hover:bg-surface-elevated hover:text-text-primary'
                )
              }
            >
              <Icon size={16} />
              {item.label}
            </NavLink>
          )
        })}
      </nav>

      {/* User + Logout */}
      <div className="border-t border-border-light p-4">
        <div className="text-xs text-text-secondary mb-1">{user?.role}</div>
        <div className="text-sm font-medium text-text-primary truncate">{user?.fullName}</div>
        <button
          onClick={logout}
          className="mt-3 flex items-center gap-2 text-xs text-text-tertiary hover:text-status-danger transition-colors"
        >
          <LogOut size={13} /> Đăng xuất
        </button>
      </div>
    </aside>
  )
}
