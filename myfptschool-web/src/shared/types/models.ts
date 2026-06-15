export type Role = 'ADMIN' | 'TEACHER'

export interface AuthUser {
  id: number
  username: string
  role: Role
  fullName: string
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
  email: string
  campusName: string
  username: string
  active: boolean
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

export type AttendanceStatus = 'present' | 'late' | 'excused_absent' | 'unexcused_absent'
export type LessonStatus = 'scheduled' | 'completed' | 'cancelled' | 'makeup'
export type NotificationCategory = 'attendance' | 'grade' | 'homeroom' | 'study' | 'event'
export type NotificationTargetType = 'individual' | 'classroom' | 'all'
