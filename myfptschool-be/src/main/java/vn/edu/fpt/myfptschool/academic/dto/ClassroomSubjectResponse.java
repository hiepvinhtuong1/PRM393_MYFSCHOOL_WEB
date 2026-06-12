package vn.edu.fpt.myfptschool.academic.dto;

import vn.edu.fpt.myfptschool.academic.entity.ClassroomSubject;

public record ClassroomSubjectResponse(
        Long id,
        Long classroomId,
        String classroomName,
        Long subjectId,
        String subjectName,
        String subjectColorHex,
        Short subjectCoefficient,
        Long teacherId,
        String teacherName,
        Long semesterId,
        String semesterName
) {
    public static ClassroomSubjectResponse from(ClassroomSubject cs) {
        return new ClassroomSubjectResponse(
                cs.getId(),
                cs.getClassroom().getId(),
                cs.getClassroom().getName(),
                cs.getSubject().getId(),
                cs.getSubject().getName(),
                cs.getSubject().getColorHex(),
                cs.getSubject().getCoefficient(),
                cs.getTeacher().getId(),
                cs.getTeacher().getFullName(),
                cs.getSemester().getId(),
                cs.getSemester().getName()
        );
    }
}
