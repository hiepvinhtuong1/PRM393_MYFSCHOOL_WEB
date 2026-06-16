export const queryKeys = {
  campuses: {
    list: () => ['campuses'] as const,
  },
  classrooms: {
    all: ['classrooms'] as const,
    list: () => ['classrooms', 'list'] as const,
    students: (id: number) => ['classrooms', id, 'students'] as const,
  },
  students: {
    list: (params: object) => ['students', 'list', params] as const,
    detail: (id: number) => ['students', id] as const,
  },
  teachers: {
    list: (params: object) => ['teachers', 'list', params] as const,
    detail: (id: number) => ['teachers', id] as const,
  },
  parents: {
    list: (params: object) => ['parents', 'list', params] as const,
    detail: (id: number) => ['parents', id] as const,
  },
  semesters: {
    list: () => ['semesters'] as const,
  },
  subjects: {
    list: () => ['subjects'] as const,
  },
  classroomSubjects: {
    list: (params: object) => ['classroom-subjects', 'list', params] as const,
    lessons: (csId: number) => ['classroom-subjects', csId, 'lessons'] as const,
    grades: (csId: number) => ['classroom-subjects', csId, 'grades'] as const,
  },
  lessons: {
    attendance: (id: number) => ['lessons', id, 'attendance'] as const,
  },
  rooms: {
    list: () => ['rooms'] as const,
  },
  timeSlots: {
    list: () => ['time-slots'] as const,
  },
  academicYears: {
    list: () => ['academic-years'] as const,
  },
  notifications: {
    list: (params: object) => ['notifications', 'list', params] as const,
    sent: (params: object) => ['notifications', 'sent', params] as const,
  },
}
