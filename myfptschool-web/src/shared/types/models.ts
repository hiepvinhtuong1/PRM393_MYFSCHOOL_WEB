export type Role = 'ADMIN' | 'TEACHER'

export interface AuthUser {
  id: number
  username: string
  role: Role
  fullName: string
}

export interface Campus {
  id: number
  name: string
}

export interface Classroom {
  id: number
  name: string
  gradeLevel: number
  campusName: string
  academicYear: string
  studentCount: number
}

export interface Student {
  id: number
  studentCode: string
  fullName: string
  dateOfBirth: string
  gender: string
  phone: string | null
  email: string | null
  photoUrl: string | null
  classroomId: number
  classroomName: string
  username: string
  active: boolean
}

export interface Teacher {
  id: number
  fullName: string
  phone: string | null
  email: string | null
  campusId: number | null
  campusName: string | null
  username: string | null
  active?: boolean
}

export interface Parent {
  id: number
  parentCode: string
  fullName: string
  dateOfBirth: string | null
  gender: string | null
  phone: string | null
  email: string | null
  username: string | null
  active: boolean
  children: StudentSummary[]
}

export interface StudentSummary {
  id: number
  studentCode: string
  fullName: string
  dateOfBirth: string | null
  gender: string | null
  phone: string | null
  email: string | null
  photoUrl: string | null
}

export interface Subject {
  id: number
  name: string
  colorHex: string | null
  coefficient: number
}

export interface Semester {
  id: number
  name: string
  academicYear: string
  startDate: string
  endDate: string
}

export interface ClassroomSubject {
  id: number
  classroomId: number
  classroomName: string
  subjectId: number
  subjectName: string
  subjectColorHex: string
  subjectCoefficient: number
  teacherId: number
  teacherName: string
  semesterId: number
  semesterName: string
}

export interface TimeSlot {
  id: number
  slotNumber: number
  startTime: string
  endTime: string
}

export interface Notification {
  id: number
  title: string
  body: string
  category: string
  isRead: boolean
  createdAt: string
}

export interface AcademicYear {
  id: number
  label: string
  startDate: string
  endDate: string
}

export interface SentNotification {
  id: number
  title: string
  body: string
  category: string
  targetType: string
  targetId: number | null
  recipientCount: number
  createdAt: string
}

export type AttendanceStatus = 'present' | 'late' | 'excused_absent' | 'unexcused_absent'
export type LessonStatus = 'scheduled' | 'completed' | 'cancelled' | 'makeup'
export type NotificationCategory = 'attendance' | 'grade' | 'homeroom' | 'study' | 'event'
export type NotificationTargetType = 'individual' | 'classroom' | 'all'
