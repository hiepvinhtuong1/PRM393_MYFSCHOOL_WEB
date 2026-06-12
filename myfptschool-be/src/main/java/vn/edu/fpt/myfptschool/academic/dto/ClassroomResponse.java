package vn.edu.fpt.myfptschool.academic.dto;

import vn.edu.fpt.myfptschool.academic.entity.Classroom;

public record ClassroomResponse(
        Long id,
        String name,
        Short gradeLevel,
        String campusName,
        String academicYear,
        long studentCount
) {
    public static ClassroomResponse from(Classroom classroom, long studentCount) {
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getGradeLevel(),
                classroom.getCampus().getName(),
                classroom.getAcademicYear().getLabel(),
                studentCount
        );
    }
}
