export const queryKeys = {
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
  },
  semesters: {
    list: () => ['semesters'] as const,
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
}
