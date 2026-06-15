import { createBrowserRouter, Navigate } from 'react-router-dom'
import { AppShell } from '@/shared/components/layout/AppShell'
import { ProtectedRoute } from './ProtectedRoute'
import { LoginPage } from '@/features/auth/LoginPage'
import { DashboardPage } from '@/features/dashboard/DashboardPage'
import { StudentListPage } from '@/features/students/StudentListPage'
import { StudentFormPage } from '@/features/students/StudentFormPage'
import { TeacherListPage } from '@/features/teachers/TeacherListPage'
import { TeacherFormPage } from '@/features/teachers/TeacherFormPage'
import { ParentListPage } from '@/features/parents/ParentListPage'
import { ParentFormPage } from '@/features/parents/ParentFormPage'
import { ClassroomListPage } from '@/features/classrooms/ClassroomListPage'
import { AssignmentPage } from '@/features/academic/AssignmentPage'
import { TimetablePage } from '@/features/timetable/TimetablePage'
import { AttendancePage } from '@/features/attendance/AttendancePage'
import { GradesPage } from '@/features/grades/GradesPage'
import { NotificationListPage } from '@/features/notifications/NotificationListPage'
import { NotificationComposePage } from '@/features/notifications/NotificationComposePage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppShell />,
        children: [
          { path: '/', element: <Navigate to="/dashboard" replace /> },
          { path: '/dashboard', element: <DashboardPage /> },
          { path: '/students', element: <StudentListPage /> },
          { path: '/students/new', element: <StudentFormPage /> },
          { path: '/students/:id/edit', element: <StudentFormPage /> },
          { path: '/teachers', element: <TeacherListPage /> },
          { path: '/teachers/new', element: <TeacherFormPage /> },
          { path: '/teachers/:id/edit', element: <TeacherFormPage /> },
          { path: '/parents', element: <ParentListPage /> },
          { path: '/parents/new', element: <ParentFormPage /> },
          { path: '/parents/:id/edit', element: <ParentFormPage /> },
          { path: '/classrooms', element: <ClassroomListPage /> },
          { path: '/assignments', element: <AssignmentPage /> },
          { path: '/timetable', element: <TimetablePage /> },
          { path: '/attendance', element: <AttendancePage /> },
          { path: '/grades', element: <GradesPage /> },
          { path: '/notifications', element: <NotificationListPage /> },
          { path: '/notifications/new', element: <NotificationComposePage /> },
        ],
      },
    ],
  },
])
